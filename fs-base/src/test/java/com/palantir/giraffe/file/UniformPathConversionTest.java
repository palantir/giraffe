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
package com.palantir.giraffe.file;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Tests conversion between {@code UniformPath}s and {@code Path}s. Path string
 * parsing is tested by {@code ImmutableListPathCoreParserTest}.
 *
 * @author bkeyes
 */
public class UniformPathConversionTest {

    @Test
    public void fromRelativePath() {
        String pathString = "path/to/file.txt";

        UniformPath direct = UniformPath.get(pathString);
        UniformPath fromPath = UniformPath.fromPath(Paths.get(pathString));
        assertEquals(direct, fromPath);
    }

    @Test
    public void fromAbsolutePath() {
        String pathString = "/home/user/path/to/file.txt";

        UniformPath direct = UniformPath.get(pathString);
        UniformPath fromPath = UniformPath.fromPath(Paths.get(pathString));
        assertEquals(direct, fromPath);
    }

    @Test
    public void toRelativePath() {
        String pathString = "path/to/file.txt";

        Path direct = Paths.get(pathString);
        Path toPath = UniformPath.get(pathString).toPath();
        assertEquals(direct, toPath);
    }

    @Test
    public void toAbsolutePath() {
        String pathString = "/home/user/path/to/file.txt";

        Path direct = Paths.get(pathString);
        Path toPath = UniformPath.get(pathString).toPath();
        assertEquals(direct, toPath);
    }

    @Test
    public void toPathEqualsFromPath() {
        Path path = Paths.get("path/to/file.txt");
        assertEquals(path, UniformPath.fromPath(path).toPath(path.getFileSystem()));
    }

}
