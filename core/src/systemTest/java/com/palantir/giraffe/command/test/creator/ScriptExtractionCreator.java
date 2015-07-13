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
package com.palantir.giraffe.command.test.creator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.palantir.giraffe.test.ScriptWriter;
import com.palantir.giraffe.test.TestFileCreatorCli.Creator;

/**
 * Extracts scripts included with the creator jar for execution system tests.
 *
 * @author bkeyes
 */
public class ScriptExtractionCreator implements Creator {

    public static final String HELLO_OUTPUT = "hello_output.sh";
    public static final String HELLO_ERROR = "hello_error.sh";
    public static final String EXIT = "exit.sh";
    public static final String STREAM = "stream.sh";
    public static final String ECHO_LINE = "echo_line.sh";
    public static final String ECHO_MULTILINE = "echo_multiline.sh";
    public static final String PRINT_ARGS = "print_args.sh";
    public static final String SLEEP_60 = "sleep60.sh";

    @Override
    public void createScript(ScriptWriter script) throws IOException {
        String[] sourceScripts = new String[] {
            HELLO_OUTPUT, HELLO_ERROR, EXIT, STREAM, ECHO_LINE, ECHO_MULTILINE,
            PRINT_ARGS,
            SLEEP_60
        };

        for (String src : sourceScripts) {
            script.printf("cat <<'EOF' > %s%n", src);

            String line;
            BufferedReader reader = getResource(src);
            while ((line = reader.readLine()) != null) {
                script.println(line);
            }

            script.println("EOF");
            script.setPermissions(src, 0755);
            script.println();
        }
    }

    private BufferedReader getResource(String name) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new IllegalStateException(String.format("no resource with name \"%s\"", name));
        }
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

}
