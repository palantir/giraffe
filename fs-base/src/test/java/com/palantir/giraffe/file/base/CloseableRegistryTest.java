package com.palantir.giraffe.file.base;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.Closeable;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * Tests basic functionality of {@link CloseableRegistry}.
 *
 * @author bkeyes
 */
public class CloseableRegistryTest {

    private CloseableRegistry registry;

    @Before
    public void setup() {
        registry = new CloseableRegistry();
    }

    @Test
    public void registersCloseable() throws IOException {
        Closeable c = mock(Closeable.class);

        registry.register(c);
        registry.close();

        verify(c).close();
    }

    @Test
    public void unregistersCloseable() throws IOException {
        Closeable c = mock(Closeable.class);

        registry.register(c);
        registry.unregister(c);
        registry.close();

        verify(c, never()).close();
    }

    @Test
    public void registersWithPriority() throws IOException {
        Closeable c = mock(Closeable.class);

        registry.register(c, 9001);
        registry.close();

        verify(c).close();
    }

    @Test
    public void unregistersWithPriority() throws IOException {
        Closeable c = mock(Closeable.class);

        registry.register(c, 9001);
        registry.unregister(c);
        registry.close();

        verify(c, never()).close();
    }

    @Test
    public void closesInPriorityOrder() throws IOException {
        Closeable c1 = mock(Closeable.class, "c1");
        Closeable c2 = mock(Closeable.class, "c2");
        Closeable c3 = mock(Closeable.class, "c3");

        registry.register(c2, 4);
        registry.register(c1, -2300);
        registry.register(c3, 9002);
        registry.close();

        InOrder inOrder = inOrder(c1, c2, c3);
        inOrder.verify(c1).close();
        inOrder.verify(c2).close();
        inOrder.verify(c3).close();
    }

}
