/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Tests which test {@link AbstractStreamWatcher} and its tokenization behavior.
 *
 * @author alake
 */
public class AbstractStreamWatcherTests {
    private static final int BUFFER_SIZE = 65536;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private InstrumentedStreamWatcher streamWatcher;
    private PipedInputStream receiverStream;
    private PipedOutputStream senderStream;
    private BarrierByteWriter byteWriter;

    @Before
    public void setup() throws IOException {
        receiverStream = new PipedInputStream(BUFFER_SIZE);
        senderStream = new PipedOutputStream();
        receiverStream.connect(senderStream);

        CyclicBarrier readWriteBarrier = new CyclicBarrier(2);
        streamWatcher = new InstrumentedStreamWatcher(receiverStream, CHARSET, readWriteBarrier);
        byteWriter = new BarrierByteWriter(senderStream, CHARSET, readWriteBarrier);
    }

    @After
    public void closeStreams() {
        try {
            receiverStream.close();
        } catch (IOException e) {
            // Swallow - this is a best effort cleanup
        }
        try {
            senderStream.close();
        } catch (IOException e) {
            // Swallow - this is a best effort cleanup
        }
    }

    /*
     * Tests
     */

    @Test
    public void testCompleteTokens() {
        byteWriter
                .writeChunk("Token1\n")
                .writeChunk("Token2\r")
                .writeChunk("Token3\r\n")
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(1, 1, 1));

        String[] expectedTokens = { "Token1", "Token2", "Token3" };
        Future<?> streamProcessing = streamWatcher.start();
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testCustomTokenization() {
        byteWriter
                .writeChunk("Token1\n")
                .writeChunk("Token2\r")
                .writeChunk("Token3\r\n")
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(2, 2, 2));

        String[] expectedTokens = { "Token", "\n", "Token", "\r", "Token", "\r\n" };

        Future<Void> streamProcessing = streamWatcher.start("[0-9]");
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testTokenAccumulation() {
        byteWriter
                .writeChunk("Token1\n")
                .writeChunk("Token2\r")
                .writeChunk("Token3\r\n")
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(2, 2, 2));

        String[] expectedTokens = { "Token", "\n", "\nToken", "\r", "\rToken", "\r\n" };
        streamWatcher.addResponse("\r", false);
        streamWatcher.addResponse("\n", false);

        Future<Void> streamProcessing = streamWatcher.start("[0-9]");
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testEmptyStrings() {
        byteWriter
                .writeChunk("Token1\n")
                .writeChunk("\r")
                .writeChunk("\r\n\r\n\n\r")
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(1, 1, 4));

        String[] expectedTokens = { "Token1", "", "", "", "", "" };

        Future<Void> streamProcessing = streamWatcher.start();
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testBufferResizing() {
        // Note: this test makes assumptions about the implementation of
        // AbstractStreamWatcher

        StringBuilder reallyLongStringBuilder = new StringBuilder(BUFFER_SIZE);
        for (int i = 0; i < BUFFER_SIZE - 1; i++) {
            reallyLongStringBuilder.append("a");
        }
        reallyLongStringBuilder.append("\n");
        String reallyLongString = reallyLongStringBuilder.toString();

        byteWriter
                .writeChunk(reallyLongString)
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(1));

        String[] expectedTokens = { reallyLongString.trim() };

        Future<Void> streamProcessing = streamWatcher.start();
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testSplitChar() {
        // "€" = 0xE2 0x82 0xAC (3 bytes)
        String utf8Char = "€";

        byte[] utf8CharBytes = utf8Char.getBytes(CHARSET);
        byte[] firstPart = new byte[2];
        byte[] secondPart = new byte[1];

        firstPart[0] = utf8CharBytes[0];
        firstPart[1] = utf8CharBytes[1];
        secondPart[0] = utf8CharBytes[2];

        byteWriter
                // Two UTF8 chunks should be reassembled
                .writeChunk(firstPart, false)
                .writeChunk(secondPart, true)
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(1));

        String[] expectedTokens = { "€" };

        Future<Void> streamProcessing = streamWatcher.start();
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testEncoding() {
        // Test some random UTF8 chars with varying numbers of bytes
        // a, b, and newline (1 byte)
        // Ђ = U+0402 (2 bytes)
        // ञ = U+091E (3 bytes)
        // 𡥋 = U+2194B (4 bytes)
        String utf8CharMix = "€a\nbЂञ\n𡥋";

        byteWriter
                .writeChunk(utf8CharMix)
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(3));

        String[] expectedTokens = { "€a", "bЂञ", "𡥋" };

        Future<Void> streamProcessing = streamWatcher.start();
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testIOExceptionFailure() throws IOException {
        // Test that an IOException during the stream processing causes it to
        // stop and the exception is retrievable from the streamProcessing
        // Future<?>
        receiverStream.close();

        Future<Void> streamProcessing = streamWatcher.start();

        try {
            streamProcessing.get(3000L, TimeUnit.MILLISECONDS);
            fail("Stream processing should not complete successfully.");
        } catch (InterruptedException e) {
            fail("Stream processing unexpectedly interrupted.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IOException);
        } catch (TimeoutException e) {
            fail("Stream processing should not hang.");
        }
    }

    @Test
    public void testImpossibleDelimiter() {
        byteWriter
                .writeChunk("Token1\n")
                .writeChunk("Token2\r")
                .writeChunk("Token3\r\n")
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(1, 1, 1));

        streamWatcher.addResponse("Token1\n", false);
        streamWatcher.addResponse("Token1\nToken2\r", false);

        String[] expectedTokens = { "Token1\n", "Token1\nToken2\r", "Token1\nToken2\rToken3\r\n" };

        Future<Void> streamProcessing = streamWatcher.start("$^");
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    @Test
    public void testBufferedInputNotFlushedOnClose() {
        byteWriter
                .writeChunk("Token1\n")
                .writeChunk("Token2\n")
                .start();
        streamWatcher.setBarrierBlockPoints(Lists.newArrayList(1, 1));

        // Fail to process any tokens.
        streamWatcher.addResponse("Token1\n", false);
        streamWatcher.addResponse("Token1\nToken2\n", false);

        // This will leave Token1\nToken2\n buffered in the
        // AbstractStreamWatcher when the stream reports end of input. Since
        // "Token1\nToken2\n" was already sent for processing, it should not be
        // re-sent for processing even though the blocking read has returned
        // because no new input has arrived on the stream to modify the token.
        // AbstractStreamWatcher assumes that calls to processToken will return
        // the same result for the same token if no other calls to processToken
        // have been made with different tokens.

        String[] expectedTokens = { "Token1\n", "Token1\nToken2\n", };

        Future<Void> streamProcessing = streamWatcher.start(" ");
        waitForProcessing(streamProcessing, 3000L);

        List<String> processedTokens = streamWatcher.getReceivedTokens();
        assertEquals(Arrays.asList(expectedTokens), processedTokens);
    }

    /*
     * Private Helpers
     */

    private void waitForProcessing(Future<?> streamProcessing, long timeoutMillis) {
        try {
            streamProcessing.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Unexpected thread interruption.");
        } catch (ExecutionException e) {
            fail("Unexpected exception while waiting for AbstractStreamWatcher: "
                    + e.getCause().getMessage());
        } catch (TimeoutException e) {
            fail("Timed out waiting for AbstractStreamWatcher to complete.");
        }
    }
}
