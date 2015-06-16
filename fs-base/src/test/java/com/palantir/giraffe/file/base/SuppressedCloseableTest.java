package com.palantir.giraffe.file.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.Closeable;
import java.io.IOException;

import org.junit.Test;
import org.mockito.InOrder;

/**
 * Tests basic functionality of {@link SuppressedCloseable}.
 *
 * @author bkeyes
 */
public class SuppressedCloseableTest {

    @Test
    public void closedInOrder() throws IOException {
        Closeable c1 = mock(Closeable.class, "c1");
        Closeable c2 = mock(Closeable.class, "c2");
        Closeable c3 = mock(Closeable.class, "c3");

        Closeable suppressed = SuppressedCloseable.create(c1, c2, c3);
        suppressed.close();

        InOrder inOrder = inOrder(c1, c2, c3);
        inOrder.verify(c1).close();
        inOrder.verify(c2).close();
        inOrder.verify(c3).close();
    }

    @Test(expected = IOException.class)
    public void singleException() throws IOException {
        Closeable first = mock(Closeable.class);
        doThrow(IOException.class).when(first).close();

        SuppressedCloseable.create(first).close();
    }

    @Test
    public void suppressesExceptions() throws IOException {
        Closeable c1 = mock(Closeable.class, "c1");
        Closeable c2 = mock(Closeable.class, "c2");
        Closeable c3 = mock(Closeable.class, "c3");

        IOException ioe1 = new IOException();
        IOException ioe2 = new IOException();
        IOException ioe3 = new IOException();

        doThrow(ioe1).when(c1).close();
        doThrow(ioe2).when(c2).close();
        doThrow(ioe3).when(c3).close();

        Closeable suppressed = SuppressedCloseable.create(c1, c2, c3);
        try {
            suppressed.close();
            fail("close() did not throw an exception");
        } catch (IOException e) {
            assertEquals("incorrect thrown exception", ioe1, e);
            assertArrayEquals("incorrect suppressed exceptions",
                    new Throwable[] { ioe2, ioe3 },
                    e.getSuppressed());
        }
    }
}
