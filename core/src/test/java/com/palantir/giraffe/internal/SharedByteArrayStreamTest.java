/**
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.giraffe.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests basic functionality of {@link SharedByteArrayStream}.
 *
 * @author bkeyes
 */
public class SharedByteArrayStreamTest {

    private Random random = new Random(9166476269L);
    private SharedByteArrayStream stream;

    @Before
    public void setup() {
        stream = new SharedByteArrayStream();
    }

    @Test
    public void readPreservesBufferContent() throws IOException {
        byte[] writeBuf = new byte[32];
        random.nextBytes(writeBuf);

        byte[] readBuf = new byte[16];

        stream.getOutputStream().write(writeBuf);
        stream.getInputStream().read(readBuf, 0, 16);

        assertArrayEquals("incorrect buffer data", writeBuf, stream.getBufferedData());
    }

    @Test
    public void readWithWindow() throws IOException {
        byte[] writeBuf = new byte[40];
        random.nextBytes(writeBuf);

        byte[] readBuf = new byte[16];

        SharedByteArrayStream sbas = new SharedByteArrayStream(16, 32);
        InputStream is = sbas.getInputStream();
        OutputStream os = sbas.getOutputStream();

        os.write(writeBuf, 0, 8);
        is.read(readBuf, 0, 8);
        assertArrayRange(writeBuf, 0, 8, readBuf);

        os.write(writeBuf, 8, 32);
        is.read(readBuf, 0, 16);
        assertArrayRange(writeBuf, 24, 16, readBuf);
    }

    @Test
    public void resizeWithWindow() throws IOException {
        byte[] writeBuf = new byte[64];
        random.nextBytes(writeBuf);

        SharedByteArrayStream sbas = new SharedByteArrayStream(32, 16);
        OutputStream os = sbas.getOutputStream();

        assertEquals("incorrect capacity", 15, sbas.capacity());
        os.write(writeBuf, 0, 32);
        assertEquals("incorrect capacity", 63, sbas.capacity());
        os.write(writeBuf, 32, 32);
        assertEquals("incorrect capacity", 63, sbas.capacity());
    }

    @Test
    public void zeroSizeWindow() throws IOException {
        byte[] writeBuf = new byte[32];
        random.nextBytes(writeBuf);

        byte[] readBuf = new byte[16];

        SharedByteArrayStream sbas = new SharedByteArrayStream(0, 16);
        InputStream is = sbas.getInputStream();
        OutputStream os = sbas.getOutputStream();

        assertEquals("incorrect capacity", 15, sbas.capacity());
        os.write(writeBuf);
        assertEquals("incorrect capacity", 15, sbas.capacity());
        os.close();

        int r = is.read(readBuf);
        assertEquals("read did not return EOF", -1, r);
    }

    @Test
    public void computeResizeGrowsByPowersOf2() {
        int newLength = stream.computeResize(5, 100, 10);
        assertEquals("length is incorrect", 160, newLength);
    }

    @Test
    public void computeResizeCapsAtMaxArraySize() {
        int newLength = stream.computeResize(0, 64, Integer.MAX_VALUE / 2 + 1);
        assertEquals("length is incorrect", Integer.MAX_VALUE - 8, newLength);
    }

    @Test
    public void computeResizeNeededOverflow() {
        int newLength = stream.computeResize(512, Integer.MAX_VALUE, 1024);
        assertEquals("length is incorrect", -1, newLength);
    }

    @Test
    public void computeResizeLengthOverflow() {
        int newLength = stream.computeResize(0, Integer.MAX_VALUE / 2 + 1, Integer.MAX_VALUE / 2);
        assertEquals("length is incorrect", -1, newLength);
    }

    private static void assertArrayRange(byte[] expected, int off, int len, byte[] actual) {
        assertArrayEquals(
                "incorrect data",
                Arrays.copyOfRange(expected, off, off + len),
                Arrays.copyOfRange(actual, 0, len));
    }

    @Test
    public void testEvenPowerOfTwoExpansion() {
        assertTrue(stream.computeResize(0, 3, 1) > 0);
    }

    @Test
    public void testEvenPowerOfTwoExpansionIndirect() throws IOException {
        stream.getOutputStream().write(new byte[2047]);
    }
}
