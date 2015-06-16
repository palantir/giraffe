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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * A {@link Filter} that removes test methods based on their fully-qualified
 * names.
 *
 * @author bkeyes
 */
public class RemoveTestsFilter extends Filter {

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\s*([A-Z0-9a-z$_.]+)(?:\\(\\))?\\s*");

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\[.*\\]$");

    private final Set<String> removeMethods = new HashSet<>();

    public RemoveTestsFilter remove(String fullyQualifiedMethodName) {
        removeMethods.add(santizeMethod(fullyQualifiedMethodName));
        return this;
    }

    private String santizeMethod(String name) {
        Matcher m = METHOD_PATTERN.matcher(name);
        if (m.matches()) {
            return m.group(1);
        } else {
            throw new IllegalArgumentException("No method found in \"" + name + "\"");
        }
    }

    public RemoveTestsFilter remove(String... fullyQualifiedMethodNames) {
        for (String method : fullyQualifiedMethodNames) {
            remove(method);
        }
        return this;
    }

    @Override
    public boolean shouldRun(Description description) {
        String method = getCleanFullyQualifiedName(description);
        if (method != null) {
            return !removeMethods.contains(method);
        }

        for (Description child : description.getChildren()) {
            if (shouldRun(child)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String describe() {
        return "Removes " + removeMethods;
    }

    private static String getCleanFullyQualifiedName(Description d) {
        if (d.getClassName() == null || d.getMethodName() == null) {
            return null;
        }

        String name = d.getClassName() + "." + d.getMethodName();
        Matcher m = PARAMETER_PATTERN.matcher(name);
        if (m.find()) {
            name = name.substring(0, m.start());
        }
        return name;
    }

}
