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
package com.palantir.giraffe.ssh.suite;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.Suite.SuiteClasses;

import com.palantir.giraffe.command.test.ExecutionSystemArgumentsTest;
import com.palantir.giraffe.command.test.ExecutionSystemContextTest;
import com.palantir.giraffe.command.test.ExecutionSystemIoTest;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;
import com.palantir.giraffe.ssh.util.RemoteSshExecutionSystemRule;
import com.palantir.giraffe.test.runner.SystemSuite;
import com.palantir.giraffe.test.runner.SystemSuite.Filterable;
import com.palantir.giraffe.test.runner.SystemSuite.SystemRule;

/**
 * Test suite for the SSH execution system implementation against Linux.
 *
 * @author bkeyes
 */
@RunWith(SystemSuite.class)
@SystemRule(ExecutionSystemTestRule.class)
@SuiteClasses({
    ExecutionSystemIoTest.class,
    ExecutionSystemArgumentsTest.class,
    ExecutionSystemContextTest.class
})
public class LinuxExecutionSystemSuite implements Filterable {

    @ClassRule
    public static final ExecutionSystemTestRule ES_RULE =
            new RemoteSshExecutionSystemRule("linux", "exec");

    @Override
    public Filter getFilter() {
        return Filter.ALL;
    }

}
