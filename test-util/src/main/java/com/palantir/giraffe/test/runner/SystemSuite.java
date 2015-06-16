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
package com.palantir.giraffe.test.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Test runner for file system implementation tests.
 * <p>
 * Test classes or suites run with this runner must have a
 * {@link SystemSuite.SystemRule} annotation and define a {@code ClassRule} of
 * the type specified by the annotation. All test classes run by this runner
 * must have a single-argument constructor that accepts objects of the type
 * given by the {@code @SystemRule} annotation. Use the {@code @SuiteClasses}
 * annotation to specify the classes to run as part of a suite.
 * <p>
 * Test classes or suites may also implement the {@link SystemSuite.Filterable}
 * interface to provide a filter that removes specific tests.
 *
 * @author bkeyes
 */
public class SystemSuite extends Suite {

    private static final List<Runner> NO_RUNNERS = Collections.emptyList();

    private static Class<?>[] getSuiteClasses(Class<?> klass) throws InitializationError {
        SuiteClasses classes = klass.getAnnotation(SuiteClasses.class);
        if (classes == null) {
            return new Class<?>[] { klass };
        } else {
            return classes.value();
        }
    }

    /**
     * Specified the type of {@code @ClassRule} used by this runner.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface SystemRule {
        Class<? extends SystemTestRule> value();
    }

    /**
     * Implementations provide a filter which is used by the runner to keep or
     * remove specific tests.
     */
    public interface Filterable {
        Filter getFilter();
    }

    private final Class<? extends SystemTestRule> ruleClass;
    private final List<Runner> runners = new ArrayList<>();

    public SystemSuite(Class<?> klass) throws InitializationError {
        super(klass, NO_RUNNERS);

        SystemRule ruleAnnotation = klass.getAnnotation(SystemRule.class);
        if (ruleAnnotation != null) {
            ruleClass = ruleAnnotation.value();
        } else {
            throw new InitializationError(String.format(
                    "class '%s' must have a @SystemRule annotation", klass.getName()));
        }

        createRunners(getSuiteClasses(klass), getRule());

        if (Filterable.class.isAssignableFrom(klass)) {
            try {
                Object suite = getTestClass().getOnlyConstructor().newInstance();
                filter(((Filterable) suite).getFilter());
            } catch (Exception e) {
                throw new InitializationError(e);
            }
        }
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    private SystemTestRule getRule() throws InitializationError {
        SystemTestRule rule = null;
        for (TestRule candidate : classRules()) {
            if (ruleClass.isInstance(candidate)) {
                if (rule != null) {
                    throw ruleError();
                }
                rule = (SystemTestRule) candidate;
            }
        }
        if (rule == null) {
            throw ruleError();
        }
        return rule;
    }

    private InitializationError ruleError() throws InitializationError {
        String testName = getTestClass().getName();
        throw new InitializationError(String.format(
                "class '%s' must have a single @ClassRule of type '%s'",
                testName, ruleClass.getName()));
    }

    private void createRunners(Class<?>[] classes, SystemTestRule fsRule)
            throws InitializationError {
        for (Class<?> test : classes) {
            String name = "[" + fsRule.name() + "]";
            runners.add(new TestClassRunnerForSystem(test, fsRule, name));
        }
    }

    private class TestClassRunnerForSystem extends BlockJUnit4ClassRunner {

        private final SystemTestRule rule;
        private final String name;

        TestClassRunnerForSystem(Class<?> type, SystemTestRule rule, String name)
                throws InitializationError {
            super(type);
            this.rule = rule;
            this.name = name;

            if (Filterable.class.isAssignableFrom(type)) {
                try {
                    filter(((Filterable) createTest()).getFilter());
                } catch (Exception e) {
                    throw new InitializationError(e);
                }
            }
        }

        @Override
        protected Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(rule);
        }

        @Override
        protected String testName(FrameworkMethod method) {
            return method.getName() + name;
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
            validateConstructorArgType(errors);
        }

        private void validateConstructorArgType(List<Throwable> errors) {
            Class<?>[] params = getTestClass().getOnlyConstructor().getParameterTypes();
            if (params.length != 1 || !params[0].isAssignableFrom(ruleClass)) {
                String gripe = String.format(
                        "Test class constructor must take exactly one argument "
                                + "assignable from '%s'", ruleClass.getName());
                errors.add(new Exception(gripe));
            }
        }
    }

}
