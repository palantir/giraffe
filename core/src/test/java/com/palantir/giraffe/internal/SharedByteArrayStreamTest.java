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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests basic functionality of {@link SharedByteArrayStream}.
 *
 * @author bkeyes
 */
public class SharedByteArrayStreamTest {

    private SharedByteArrayStream stream;

    @Before
    public void setup() {
        stream = new SharedByteArrayStream();
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
}
