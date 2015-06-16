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
package com.palantir.giraffe.ssh.internal.base;

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.concurrent.GuardedBy;

import net.schmizz.sshj.SSHClient;

/**
 * Ensures that a {@link SSHClient} used by both a file system and execution
 * system is only closed when both systems are closed.
 *
 * @author bkeyes
 */
public class SharedSshClient implements Closeable {

    private final SSHClient client;

    @GuardedBy("this")
    private int users = 1;

    public SharedSshClient(SSHClient client) {
        this.client = client;
    }

    /**
     * Increments the number of users using this client.
     *
     * @return {@code true} if the increment was successful, {@code false} if
     *         the connection is already closed.
     */
    public synchronized boolean addUser() {
        if (users == 0) {
            return false;
        } else {
            users++;
            return true;
        }
    }

    public SSHClient getClient() {
        return client;
    }

    @Override
    public void close() throws IOException {
        boolean close = false;

        synchronized (this) {
            if (users > 0) {
                users--;
                close = (users == 0);
            }
        }

        if (close) {
            client.close();
        }
    }

}
