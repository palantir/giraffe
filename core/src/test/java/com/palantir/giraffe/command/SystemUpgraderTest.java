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
package com.palantir.giraffe.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.junit.Before;
import org.junit.Test;

import com.palantir.giraffe.SystemUpgrader;
import com.palantir.giraffe.host.HostAccessors;
import com.palantir.giraffe.host.HostControlSystem;
import com.palantir.giraffe.host.HostControlSystemUpgradeable;

/**
 * Tests basic functionality of {@link SystemUpgrader} methods.
 *
 * @author bkeyes
 */
public class SystemUpgraderTest {

    private abstract static class ConvertibleFileSystem extends FileSystem
            implements HostControlSystemUpgradeable {
        // implementation is mocked
    }

    private abstract static class ConvertibleExecutionSystem extends ExecutionSystem
            implements HostControlSystemUpgradeable {
        // implementation is mocked
    }

    private ConvertibleFileSystem fs;
    private ConvertibleExecutionSystem es;
    private HostControlSystem hcs;

    @Before
    public void setup() throws IOException {
        fs = mock(ConvertibleFileSystem.class);
        es = mock(ConvertibleExecutionSystem.class);
        hcs = mock(HostControlSystem.class);

        when(fs.asHostControlSystem()).thenReturn(hcs);
        when(es.asHostControlSystem()).thenReturn(hcs);
    }

    @Test
    public void upgradeFileSystem() throws IOException {
        assertEquals("upgrade is incorrect", hcs, SystemUpgrader.upgrade(fs));
    }

    @Test
    public void upgradeLocalFileSystem() throws IOException {
        HostControlSystem upgraded = SystemUpgrader.upgrade(FileSystems.getDefault());
        assertEquals("upgrade is incorrect", HostAccessors.getDefault().open(), upgraded);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedFileSystem() throws IOException {
        SystemUpgrader.upgrade(mock(FileSystem.class));
    }

    @Test
    public void fileSystemIsUpgradeable() {
        assertTrue("system is not upgradeable", SystemUpgrader.isUpgradeable(fs));

        FileSystem defaultFs = FileSystems.getDefault();
        assertTrue("system is not upgradaeble", SystemUpgrader.isUpgradeable(defaultFs));
    }

    @Test
    public void upgradeExecutionSystem() throws IOException {
        assertEquals("upgrade is incorrect", hcs, SystemUpgrader.upgrade(es));
    }

    @Test
    public void upgradeLocalExecutionSystem() throws IOException {
        HostControlSystem upgraded = SystemUpgrader.upgrade(ExecutionSystems.getDefault());
        assertEquals("conversion is incorrect", HostAccessors.getDefault().open(), upgraded);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedExecutionSystem() throws IOException {
        SystemUpgrader.upgrade(mock(ExecutionSystem.class));
    }

    @Test
    public void executionSystemIsUpgradeable() {
        assertTrue("system is not upgradeable", SystemUpgrader.isUpgradeable(es));

        ExecutionSystem defaultEs = ExecutionSystems.getDefault();
        assertTrue("system is not upgradeable", SystemUpgrader.isUpgradeable(defaultEs));
    }
}
