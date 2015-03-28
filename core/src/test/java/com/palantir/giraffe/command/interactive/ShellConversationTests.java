/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.palantir.giraffe.command.MockCommandFuture;
import com.palantir.giraffe.command.interactive.ResponseProvider.OutputMatcher;

/**
 * Tests for testing {@link ShellConversation} and its error handling.
 *
 * @author alake
 */
public class ShellConversationTests {
    private static final int BUFFER_SIZE = 65536;

    // Std out
    private PipedInputStream stdOutReceiverStream;
    private PipedOutputStream stdOutSenderStream;
    private Writer stdOutWriter;

    // Std in
    private PipedInputStream stdInReceiverStream;
    private PipedOutputStream stdInSenderStream;

    private MockCommandFuture mockCommandFuture;

    @Before
    public void setup() throws IOException {
        this.stdOutReceiverStream = new PipedInputStream(BUFFER_SIZE);
        this.stdOutSenderStream = new PipedOutputStream();
        stdOutReceiverStream.connect(stdOutSenderStream);

        this.stdInReceiverStream = new PipedInputStream(BUFFER_SIZE);
        this.stdInSenderStream = new PipedOutputStream();
        stdInReceiverStream.connect(stdInSenderStream);

        this.stdOutWriter = new OutputStreamWriter(stdOutSenderStream, StandardCharsets.UTF_8);

        this.mockCommandFuture = new MockCommandFuture(
                stdOutReceiverStream,
                null,
                stdInSenderStream);
    }

    @After
    public void closeStreams() {
        try {
            stdOutReceiverStream.close();
        } catch (IOException e) {
            // Swallow - this is a best effort cleanup
        }
        try {
            stdOutSenderStream.close();
        } catch (IOException e) {
            // Swallow - this is a best effort cleanup
        }
        try {
            stdInReceiverStream.close();
        } catch (IOException e) {
            // Swallow - this is a best effort cleanup
        }
        try {
            stdInSenderStream.close();
        } catch (IOException e) {
            // Swallow - this is a best effort cleanup
        }
    }

    @Test
    public void testEmptySequentialConversation() throws IOException {
        AbstractResponseProvider<String> script = OrderedResponseProvider.<String>builder().build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        // Ignored
        stdOutWriter.write("Hello\n");
        // Ignored
        stdOutWriter.write("Bye\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to detect closed stream
        waitForProcessing(streamProcessing, 3000L);

        byte[] buf = new byte[1024];
        int numChars = 0;
        try {
            stdInSenderStream.flush();
            stdInSenderStream.close();
            numChars = stdInReceiverStream.read(buf);
        } catch (IOException e) {
            fail("Unexpected IOException.");
        }

        assertEquals(-1, numChars);
    }

    @Test
    public void testSequentialConversation() throws IOException {
        AbstractResponseProvider<String> script = OrderedResponseProvider.<String>builder()
                .addExact("Hello", "Hello to you too!\n")
                .addExact("Bye", "Bye for now!\n")
                .build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        // Processed
        stdOutWriter.write("Hello\n");
        // Ignored
        stdOutWriter.write("Hello\n");
        // Ignored
        stdOutWriter.write("Don't match me\n");
        // Processed
        stdOutWriter.write("Bye\n");
        // Ignored
        stdOutWriter.write("Hello\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to detect closed stream
        waitForProcessing(streamProcessing, 3000L);

        byte[] buf = new byte[1024];
        int numChars = 0;
        try {
            numChars = stdInReceiverStream.read(buf);
        } catch (IOException e) {
            fail("Unexpected IOException.");
        }

        assertFalse(numChars == -1);

        String expected = "Hello to you too!\nBye for now!\n";
        String actual = new String(buf, 0, numChars, StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    @Test
    public void testEmptyRandomAccessConversation() throws IOException {
        AbstractResponseProvider<String> script =
                UnorderedResponseProvider.<String>builder().build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        // Ignored
        stdOutWriter.write("Hello\n");
        // Ignored
        stdOutWriter.write("Bye\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to detect closed stream
        waitForProcessing(streamProcessing, 3000L);

        byte[] buf = new byte[1024];
        int numChars = 0;
        try {
            stdInSenderStream.flush();
            stdInSenderStream.close();
            numChars = stdInReceiverStream.read(buf);
        } catch (IOException e) {
            fail("Unexpected IOException.");
        }

        assertEquals(-1, numChars);
    }

    @Test
    public void testUnorderedConversation() throws IOException {
        AbstractResponseProvider<String> script = UnorderedResponseProvider.<String>builder()
                .addExact("Hello", "Hello to you too!\n")
                .addExact("Bye", "Bye for now!\n")
                .build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        // Processed
        stdOutWriter.write("Bye\n");
        // Processed
        stdOutWriter.write("Hello\n");
        // Ignored
        stdOutWriter.write("Don't match me\n");
        // Processed
        stdOutWriter.write("Hello\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to detect closed stream
        waitForProcessing(streamProcessing, 3000L);

        byte[] buf = new byte[1024];
        int numChars = 0;
        try {
            numChars = stdInReceiverStream.read(buf);
        } catch (IOException e) {
            fail("Unexpected IOException.");
        }

        assertFalse(numChars == -1);

        String expected = "Bye for now!\nHello to you too!\nHello to you too!\n";
        String actual = new String(buf, 0, numChars, StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    @Test
    public void testOutputLookupTwoMatches() throws IOException {
        AbstractResponseProvider<String> script = UnorderedResponseProvider.<String>builder()
                .addExact("Hello", "More than one match")
                .addExact("Hello", "is an illegal state!")
                .build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        stdOutWriter.write("Hello\n");

        // Flush and close stream
        stdOutWriter.close();

        // Wait for stream processing to fail
        try {
            streamProcessing.get(3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException unexpected) {
            fail("Unexpected thread interruption.");
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause() instanceof IllegalStateException);
        } catch (TimeoutException unexpected) {
            fail("Timed out waiting for ShellConversation to complete.");
        }
    }

    @Test
    public void testPrompts() throws IOException {
        AbstractResponseProvider<String> script = UnorderedResponseProvider.<String>builder()
                .addExact(
                        "Hello",
                        "Hello to you too!\n")
                .addRegex(
                        "I am [1-9][0-9]* years old.",
                        "Good for you!\n")
                .addExact(
                        "Pie is good.",
                        new Function<String, String>() {
                            @Override
                            public String apply(String input) {
                                return "It sure is!\n";
                            }
                        })
                .addPredicate(
                        Predicates.equalTo("Bye"),
                        new Function<String, String>() {
                            @Override
                            public String apply(String input) {
                                return "Bye for now!\n";
                            }
                        })
                .build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        // Processed
        stdOutWriter.write("Pie is good.\n");
        // Ignored
        stdOutWriter.write("I am twenty-five years old!\n");
        // Processed
        stdOutWriter.write("I am 25 years old!\n");
        // Processed
        stdOutWriter.write("Hello\n");
        // Ignored
        stdOutWriter.write("Byee\n");
        // Processed
        stdOutWriter.write("Bye\n");
        // Ignored
        stdOutWriter.write("Pie is bad.\n");
        // Processed
        stdOutWriter.write("Pie is good.\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to detect closed stream
        waitForProcessing(streamProcessing, 3000L);

        byte[] buf = new byte[1024];
        int numChars = 0;
        try {
            numChars = stdInReceiverStream.read(buf);
        } catch (IOException e) {
            fail("Unexpected IOException.");
        }

        assertFalse(numChars == -1);

        String expected =
                "It sure is!\nGood for you!\nHello to you too!\nBye for now!\nIt sure is!\n";
        String actual =
                new String(buf, 0, numChars, StandardCharsets.UTF_8);

        assertEquals(expected, actual);
    }

    @Test
    public void testFailingCallable() throws IOException {
        AbstractResponseProvider<String> script = UnorderedResponseProvider.<String>builder()
                .add(new OutputMatcher<String>() {
                    @Override
                    public boolean tokenMatches(String token) {
                        return token.equals("Boom");
                    }

                    @Override
                    public String getResponse(String matchedToken) {
                        throw new RuntimeException(
                                "Someone poured water on our server racks again.");
                    }
                })
                .build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        // Cause a failure
        stdOutWriter.write("Boom\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to fail
        try {
            streamProcessing.get(3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException unexpected) {
            fail("Unexpected thread interruption.");
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause() instanceof RuntimeException);
        } catch (TimeoutException unexpected) {
            fail("Timed out waiting for ShellConversation to complete.");
        }
    }

    @Test
    public void testCorrectMatchedToken() throws IOException {
        AbstractResponseProvider<String> script = UnorderedResponseProvider.<String>builder()
                .addRegex(".*", new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return input;
                    }
                })
                .build();

        ShellConversation shellConvo = new ShellConversation(script, mockCommandFuture);
        Future<Void> streamProcessing = shellConvo.start();

        String inputString = "I should get this back";
        stdOutWriter.write(inputString + "\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to detect closed stream
        waitForProcessing(streamProcessing, 3000L);

        byte[] buf = new byte[1024];
        int numChars = stdInReceiverStream.read(buf);
        assertFalse(numChars == -1);

        String actual = new String(buf, 0, numChars, StandardCharsets.UTF_8);
        assertEquals(inputString, actual);
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
            fail("Unexpected exception while waiting for ShellConversation: "
                    + e.getCause().getMessage());
        } catch (TimeoutException e) {
            fail("Timed out waiting for ShellConversation to complete.");
        }
    }

}
