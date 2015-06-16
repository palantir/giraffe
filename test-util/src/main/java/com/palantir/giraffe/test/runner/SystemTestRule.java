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

import java.nio.file.Path;

import org.junit.rules.TestRule;

/**
 * Provides access to a file or execution system implementation for tests.
 *
 * @author bkeyes
 */
public interface SystemTestRule extends TestRule {

    String name();

    Path getTestFilesRoot();

}
