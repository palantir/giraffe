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
package com.palantir.giraffe.ssh.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;

import com.palantir.giraffe.host.Host;
import com.palantir.giraffe.ssh.SshHost;

/**
 * An embeded SSH server for testing SSH systems against the local host.
 *
 * @author bkeyes
 */
public class MinaTestServer {

    private static final String USERNAME = "giraffe";
    private static final String PASSWORD = "l0ngN3ck";

    private final Path workingDir;

    private int port;
    private SshServer sshd;

    public MinaTestServer(Path workingDir) {
        this.workingDir = workingDir;
    }

    public void start() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        sshd.setCommandFactory(new CommandFactory() {
            @Override
            public Command createCommand(String command) {
                String[] shellCommand = new String[] {
                    "/bin/sh", "-c", "cd " + workingDir + " && " + command
                };
                return new ProcessShellFactory(shellCommand).create();
            }
        });

        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                if (USERNAME.equals(username)) {
                    return PASSWORD.equals(password);
                } else {
                    return false;
                }
            }
        });
        List<NamedFactory<UserAuth>> authMethods = new ArrayList<>();
        authMethods.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(authMethods);

        sshd.setFileSystemFactory(new FileSystemFactory() {
            @Override
            public FileSystemView createFileSystemView(Session session) throws IOException {
                if (Files.notExists(workingDir)) {
                    Files.createDirectory(workingDir);
                }

                // TODO(bkeyes): this is "private", are there other options?
                return new NativeFileSystemView(
                        session.getUsername(),
                        Collections.singletonMap("/", "/"),
                        workingDir.toString());
            }
        });

        List<NamedFactory<Command>> subsystems = new ArrayList<>();
        subsystems.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(subsystems);

        sshd.start();
    }

    public void stop() {
        try {
            sshd.stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while stopping sshd", e);
        }
    }

    public SshHost<?> getHost() {
        char[] password = PASSWORD.toCharArray();
        return SshHost.authWithPassword(Host.localhost(), USERNAME, port, password);
    }

}
