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
import com.palantir.giraffe.test.runner.RemoveTestsFilter;
import com.palantir.giraffe.test.runner.SystemSuite;
import com.palantir.giraffe.test.runner.SystemSuite.Filterable;
import com.palantir.giraffe.test.runner.SystemSuite.SystemRule;

/**
 * Test suite for the SSH execution system implementation against Cygwin.
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
public class CygwinExecutionSystemSuite implements Filterable {

    @ClassRule
    public static final ExecutionSystemTestRule ES_RULE =
            new RemoteSshExecutionSystemRule("cygwin", "exec");

    @Override
    public Filter getFilter() {
        String contextTest = "com.palantir.giraffe.command.test.ExecutionSystemContextTest";
        return new RemoveTestsFilter().remove(
                // On Cygwin, "env -i" retains two Windows variables
                contextTest + ".emptyEnvironment",
                contextTest + ".emptyEnvironmentWithChanges");
    }

}
