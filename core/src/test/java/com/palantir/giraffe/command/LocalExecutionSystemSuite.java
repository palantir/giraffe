package com.palantir.giraffe.command;

import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.Suite.SuiteClasses;

import com.palantir.giraffe.command.test.ExecutionSystemArgumentsTest;
import com.palantir.giraffe.command.test.ExecutionSystemCancellationTest;
import com.palantir.giraffe.command.test.ExecutionSystemContextTest;
import com.palantir.giraffe.command.test.ExecutionSystemIoTest;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;
import com.palantir.giraffe.test.runner.SystemSuite;
import com.palantir.giraffe.test.runner.SystemSuite.Filterable;
import com.palantir.giraffe.test.runner.SystemSuite.SystemRule;

/**
 * Test suite for the local execution system implementation.
 *
 * @author bkeyes
 */
@RunWith(SystemSuite.class)
@SystemRule(ExecutionSystemTestRule.class)
@SuiteClasses({
    ExecutionSystemIoTest.class,
    ExecutionSystemArgumentsTest.class,
    ExecutionSystemCancellationTest.class,
    ExecutionSystemContextTest.class,
    LocalExecutionSystemConfigurationTest.class
})
public class LocalExecutionSystemSuite implements Filterable {

    @ClassRule
    public static final ExecutionSystemTestRule ES_RULE =
            new LocalExecutionSystemRule(Paths.get("build/system-test-files/exec"));

    @Override
    public Filter getFilter() {
        return Filter.ALL;
    }

}
