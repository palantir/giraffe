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
/**
 * Provides SSH access for remote file systems and command execution.
 * <p>
 * The SSH implementation is provided by the {@code giraffe-ssh} library.
 *
 * <h3>System Lifecycle</h3>
 * <p>
 * Every SSH system is associated with an open, active SSH connection until it is
 * closed. For this reason, systems should always be closed after use. Multiple SSH
 * systems can be open for a given host at the same time, but it is recommended that
 * applications save and share systems to reduce the cost associated with establishing
 * connections. SSH systems are never cached internally and the {@code get} methods
 * on system providers always throw exceptions.
 *
 * <h3>System Conversion</h3>
 * <p>
 * {@code FileSystem} and {@code ExecutionSystem} instances from the SSH
 * provider support conversion using {@link com.palantir.giraffe.SystemConverter}
 * Converted systems share a connection with the original system. While closing
 * one of these systems does not affect the other, any event that corrupts the
 * connection will affect both systems. Changes made by one system are visible
 * to the other system provided the modification <em>happens-before</em> the
 * read. Note that the reverse is not true: detecting a modification does
 * <em>not</em> imply a <em>happens-before</em> relation.
 *
 * <h3>File Attributes</h3>
 * <p>
 * The file system supports {@link java.nio.file.attribute.BasicFileAttributes basic}
 * and {@link java.nio.file.attribute.PosixFileAttributes posix} file attributes.
 * File creation time is not supported: reading it returns the last modified time
 * while setting it throws an exception. User and group principals are represented
 * by their numeric IDs and translation between names and IDs is not supported. If
 * the user or group ID of a file is not available for some reason, the file system
 * returns a principal with ID {@code -1}.
 *
 * <h3>Logging</h3>
 * <p>
 * SSH systems log at the debug level for most operations. By default, this uses the
 * {@code com.palantir.giraffe.ssh} logger. Users may provide a custom logger by setting
 * the {@code logger} key to a valid {@link org.slf4j.Logger Logger} instance in the
 * environment map used to create new systems.
 * <p>
 * Log statements are associated with the host of the originating system using the MDC
 * key {@code giraffe-ssh-host}. Users are encouraged to include this information when
 * configuring logging.
 */
package com.palantir.giraffe.ssh;

