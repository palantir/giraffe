package com.palantir.giraffe.command;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.palantir.giraffe.command.spi.ExecutionSystemProvider;

/**
 * Tests basic functionality of {@link Commands} methods.
 *
 * @author bkeyes
 */
public class CommandsTest {

    private MockCommandFuture commandFuture;

    private Command command;
    private CommandContext commandContext;

    private CountDownLatch startLatch;
    private AtomicReference<Thread> actionThread;

    private ExecutorService executor;

    @Before
    public void setup() {
        commandFuture = new MockCommandFuture(null, null, null);

        commandContext = CommandContext.defaultContext();
        command = newMockCommand(commandContext, commandFuture);

        startLatch = new CountDownLatch(1);
        actionThread = new AtomicReference<>();

        executor = Executors.newSingleThreadExecutor();
    }

    @After
    public void teardown() {
        executor.shutdownNow();
    }

    @Test
    public void waitsForSuccess() throws InterruptedException {
        testFutureSuccess("waitFor", waitForAction());
    }

    @Test
    public void waitsForException() throws InterruptedException {
        testFutureFailure("waitFor", waitForAction());
    }

    @Test
    public void waitsForIsUninterruptible() throws InterruptedException {
        testUninterruptible("waitFor", waitForAction());
    }

    private Callable<ActionResult> waitForAction() {
        return new Callable<ActionResult>() {
            @Override
            public ActionResult call() throws IOException {
                actionThread.set(Thread.currentThread());
                startLatch.countDown();
                return new ActionResult(Commands.waitFor(commandFuture));
            }
        };
    }

    @Test
    public void executeSucceeds() throws InterruptedException {
        testFutureSuccess("execute", executeCommandAction());
    }

    @Test
    public void executeFailure() throws InterruptedException {
        testFutureFailure("execute", executeCommandAction());
    }

    @Test
    public void executeIsUninterruptible() throws InterruptedException {
        testUninterruptible("execute", executeCommandAction());
    }

    private Callable<ActionResult> executeCommandAction() {
        return new Callable<ActionResult>() {
            @Override
            public ActionResult call() throws Exception {
                actionThread.set(Thread.currentThread());
                startLatch.countDown();
                return new ActionResult(Commands.execute(command, commandContext));
            }
        };
    }

    @Test
    public void executeTimeoutSucceeds() throws InterruptedException {
        testFutureSuccess("execute", executeCommandAction(50));
    }

    @Test
    public void executeTimeoutFailure() throws InterruptedException {
        testFutureFailure("execute", executeCommandAction(50));
    }

    @Test
    public void executeTimeoutExceeded() throws InterruptedException {
        testTimeout("execute", executeCommandAction(10));
    }

    @Test
    public void executeTimeoutIsUninterruptible() throws InterruptedException {
        testUninterruptible("execute", executeCommandAction(500));
    }

    private Callable<ActionResult> executeCommandAction(final long timeout) {
        return new Callable<ActionResult>() {
            @Override
            public ActionResult call() throws Exception {
                actionThread.set(Thread.currentThread());
                startLatch.countDown();
                return new ActionResult(Commands.execute(
                        command, commandContext,
                        timeout, TimeUnit.MILLISECONDS));
            }
        };
    }

    // TODO(bkeyes): These methods don't use the common setup at all

    @Test
    public void executeAsyncCallsProvider() throws IOException {
        Command c = mock(Command.class);
        ExecutionSystem es = mock(ExecutionSystem.class);
        ExecutionSystemProvider provider = mock(ExecutionSystemProvider.class);

        when(c.getExecutionSystem()).thenReturn(es);
        when(es.provider()).thenReturn(provider);

        CommandContext context = CommandContext.defaultContext();
        Commands.executeAsync(c, context);

        verify(provider).execute(c, context);
    }

    @Test
    public void toResultUsesResolved() throws IOException {
        CommandResult expected = new CommandResult(0, "", "");
        commandFuture.succeed(expected, 0);

        CommandResult actual = Commands.toResult(commandFuture);
        assertThat(actual, sameInstance(expected));
    }

    @Test
    public void toResultReadsAvailableOutput() throws IOException {
        CommandResult expected = new CommandResult(0, "standard out", "standard error");

        InputStream outStream = toStream(expected.getStdOut());
        InputStream errStream = toStream(expected.getStdErr());
        commandFuture = new MockCommandFuture(outStream, errStream, null);

        CommandResult actual = Commands.toResult(commandFuture, 0);
        assertEquals("wrong exit status", expected.getExitStatus(), actual.getExitStatus());
        assertEquals("wrong stdout", expected.getStdOut(), actual.getStdOut());
        assertEquals("wrong stderr", expected.getStdErr(), actual.getStdErr());
    }

    private static InputStream toStream(final String s) {
        return new InputStream() {
            private int pos;

            @Override
            public int available() throws IOException {
                return Math.max(0, s.length() - pos);
            }

            @Override
            public int read() throws IOException {
                if (pos < s.length()) {
                    return s.charAt(pos++);
                } else {
                    throw new IOException("read() called with no available data");
                }
            }
        };
    }

    private static final class ActionResult {
        public final CommandResult result;
        public final boolean interrupted;

        ActionResult(CommandResult result) {
            this.result = result;
            this.interrupted = Thread.interrupted();
        }
    }

    private void testFutureSuccess(String name, Callable<ActionResult> action)
            throws InterruptedException {
        Future<ActionResult> future = executor.submit(action);
        startLatch.await();

        CommandResult result = new CommandResult(0, "", "");
        commandFuture.succeed(result, 10);

        try {
            ActionResult actionResult = future.get(100, TimeUnit.MILLISECONDS);
            assertEquals("wrong result", result, actionResult.result);
        } catch (TimeoutException e) {
            fail("after success, " + name + " did not return in time");
        } catch (ExecutionException e) {
            throw new AssertionError("unexpected exception", e.getCause());
        }
    }

    private void testFutureFailure(String name, Callable<ActionResult> action)
            throws InterruptedException {
        Future<ActionResult> future = executor.submit(action);
        startLatch.await();

        IOException exception = new IOException();
        commandFuture.failWithException(exception, 10);

        try {
            future.get(100, TimeUnit.MILLISECONDS);
            fail(name + " returned successfully");
        } catch (TimeoutException e) {
            fail("after failure, " + name + " did not return in time");
        } catch (ExecutionException e) {
            assertEquals("unexpected exception", exception, Throwables.getRootCause(e));
        }
    }

    private void testTimeout(String name, Callable<ActionResult> action)
            throws InterruptedException {
        Future<ActionResult> future = executor.submit(action);
        startLatch.await();

        try {
            future.get(100, TimeUnit.MILLISECONDS);
            fail(name + " returned successfully");
        } catch (TimeoutException e) {
            fail("after timeout, " + name + " did not return in time");
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof TimeoutException)) {
                throw new AssertionError("unexpected exception", e.getCause());
            }
        }
    }

    private void testUninterruptible(String name, Callable<ActionResult> action)
            throws InterruptedException {
        Future<ActionResult> future = executor.submit(action);

        startLatch.await();
        commandFuture.awaitGet();
        actionThread.get().interrupt();

        try {
            future.get(25, TimeUnit.MILLISECONDS);
            fail(name + " returned after interruption");
        } catch (TimeoutException expected) {
            commandFuture.succeed(null, 0);
            try {
                ActionResult actionResult = future.get(100, TimeUnit.MILLISECONDS);
                assertTrue(name + " thread was not interrupted", actionResult.interrupted);
            } catch (TimeoutException e) {
                fail("after success, " + name + " did not return in time");
            } catch (ExecutionException e) {
                throw new AssertionError("unexpected exception", e.getCause());
            }
        } catch (ExecutionException e) {
            throw new AssertionError("unexpected exception", e.getCause());
        }
    }

    private static Command newMockCommand(CommandContext context, CommandFuture future) {
        Command c = mock(Command.class);
        ExecutionSystem es = mock(ExecutionSystem.class);
        ExecutionSystemProvider provider = mock(ExecutionSystemProvider.class);

        when(c.getExecutionSystem()).thenReturn(es);
        when(es.provider()).thenReturn(provider);
        when(provider.execute(c, context)).thenReturn(future);
        return c;
    }

}
