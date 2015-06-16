package com.palantir.giraffe.ssh.suite;

import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.Suite.SuiteClasses;

import com.palantir.giraffe.command.test.ExecutionSystemArgumentsTest;
import com.palantir.giraffe.command.test.ExecutionSystemContextTest;
import com.palantir.giraffe.command.test.ExecutionSystemIoTest;
import com.palantir.giraffe.command.test.runner.ExecutionSystemTestRule;
import com.palantir.giraffe.ssh.ExecutionSystemConversionTest;
import com.palantir.giraffe.ssh.util.MinaSshdExecutionSystemRule;
import com.palantir.giraffe.test.runner.SystemSuite;
import com.palantir.giraffe.test.runner.SystemSuite.Filterable;
import com.palantir.giraffe.test.runner.SystemSuite.SystemRule;

/**
 * Test suite for the SSH execution system implementation.
 *
 * @author bkeyes
 */
@RunWith(SystemSuite.class)
@SystemRule(ExecutionSystemTestRule.class)
@SuiteClasses({
    ExecutionSystemIoTest.class,
    ExecutionSystemArgumentsTest.class,
    ExecutionSystemContextTest.class,
    ExecutionSystemConversionTest.class
})
public class EmbeddedSshExecutionSystemSuite implements Filterable {

    @ClassRule
    public static final ExecutionSystemTestRule ES_RULE =
            new MinaSshdExecutionSystemRule(Paths.get("build/system-test-files/exec"));

    @Override
    public Filter getFilter() {
        return Filter.ALL;
    }

}
