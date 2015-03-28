/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.palantir.giraffe.command.MockCommandFuture;

/**
 * Tests for testing {@link CommandOutputTrigger} and its error handling.
 *
 * @author alake
 */
public class CommandOutputTriggerTests {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int BUFFER_SIZE = 65536;

    // Std out
    private PipedInputStream stdOutReceiverStream;
    private PipedOutputStream stdOutSenderStream;
    private Writer stdOutWriter;

    private MockCommandFuture mockCommandFuture;

    @Before
    public void setup() throws IOException {
        this.stdOutReceiverStream = new PipedInputStream(BUFFER_SIZE);
        this.stdOutSenderStream = new PipedOutputStream();
        stdOutReceiverStream.connect(stdOutSenderStream);

        this.stdOutWriter = new OutputStreamWriter(stdOutSenderStream, CHARSET);
        this.mockCommandFuture = new MockCommandFuture(stdOutReceiverStream, null, null);
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
    }

    @Test
    public void testCallback() throws IOException {
        final AtomicInteger value = new AtomicInteger(0);

        AbstractResponseProvider<Runnable> cbMap = UnorderedResponseProvider.<Runnable>builder()
                .addExact(
                        "Increment",
                        new Runnable() {
                            @Override
                            public void run() {
                                value.incrementAndGet();
                            }
                        })
                .build();

        CommandOutputTrigger trigger = new CommandOutputTrigger(cbMap, mockCommandFuture);
        Future<Void> streamProcessing = trigger.start();

        // Processed
        stdOutWriter.write("Increment\n");
        // Ignored
        stdOutWriter.write("Ignore me\n");
        // Processed
        stdOutWriter.write("Increment\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to detect closed stream
        waitForProcessing(streamProcessing, 3000L);

        assertEquals(2, value.get());
    }

    @Test
    public void testFailingCallback() throws IOException {
        final RuntimeException runtimeException = new RuntimeException();
        AbstractResponseProvider<Runnable> cbMap = UnorderedResponseProvider.<Runnable>builder()
                .addExact(
                        "Boom",
                        new Runnable() {
                            @Override
                            public void run() {
                                throw runtimeException;
                            }
                        })
                .build();

        CommandOutputTrigger trigger = new CommandOutputTrigger(cbMap, mockCommandFuture);
        Future<Void> streamProcessing = trigger.start();

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
            assertEquals(expected.getCause(), runtimeException);
        } catch (TimeoutException unexpected) {
            fail("Timed out waiting for ShellConversation to complete.");
        }
    }

    @Test
    public void testMatching() throws IOException {
        final AtomicInteger value = new AtomicInteger(0);

        final Runnable increment = new Runnable() {
            @Override
            public void run() {
                value.incrementAndGet();
            }
        };

        AbstractResponseProvider<Runnable> cbMap = UnorderedResponseProvider.<Runnable>builder()
                .addExact("Exact", increment)
                .addRegex("[rR]ege[xX] are t[e3]h coolz[0-9]", increment)
                .addPredicate(Predicates.equalTo("Equal"),
                        new Function<String, Runnable>() {
                            @Override
                            public Runnable apply(@Nullable String input) {
                                return increment;
                            }
                        })
                .build();

        CommandOutputTrigger trigger = new CommandOutputTrigger(cbMap, mockCommandFuture);
        Future<Void> streamProcessing = trigger.start();

        // No match
        stdOutWriter.write("No match\n");
        // Match
        stdOutWriter.write("Exact\n");
        // Match
        stdOutWriter.write("Regex are teh coolz1\n");
        // Match
        stdOutWriter.write("regeX are t3h coolz2\n");
        // No match
        stdOutWriter.write("Regex are not teh coolz1\n");
        // Match
        stdOutWriter.write("Equal\n");
        // No match
        stdOutWriter.write("Equal!\n");

        // Close and flush the stream
        stdOutWriter.close();

        // Wait for stream processing to complete
        waitForProcessing(streamProcessing, 3000L);

        assertEquals(4, value.get());
    }

    @Test
    public void testCallbackMultipleMatch() throws IOException {
        AbstractResponseProvider<Runnable> cbMap = UnorderedResponseProvider.<Runnable>builder()
                .addExact(
                        "Boom",
                        new Runnable() {
                            @Override
                            public void run() {
                                return;
                            }
                        })
                .addRegex(
                        "B[o0][o0]m",
                        new Runnable() {
                            @Override
                            public void run() {
                                return;
                            }
                        })
                .build();

        CommandOutputTrigger trigger = new CommandOutputTrigger(cbMap, mockCommandFuture);
        Future<Void> streamProcessing = trigger.start();

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
            assertTrue(expected.getCause() instanceof IllegalStateException);
        } catch (TimeoutException unexpected) {
            fail("Timed out waiting for ShellConversation to complete.");
        }
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
