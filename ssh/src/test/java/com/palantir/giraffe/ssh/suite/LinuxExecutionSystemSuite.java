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
