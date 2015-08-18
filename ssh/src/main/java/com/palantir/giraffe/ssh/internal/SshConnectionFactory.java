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
package com.palantir.giraffe.ssh.internal;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import com.palantir.giraffe.file.MoreFiles;
import com.palantir.giraffe.file.TempPath;
import com.palantir.giraffe.ssh.SshAuthenticator;
import com.palantir.giraffe.ssh.SshCredential;
import com.palantir.giraffe.ssh.SshSystemRequest;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

final class SshConnectionFactory {

    private final Config config;

    public SshConnectionFactory(Config sshjConfiguration) {
        this.config = sshjConfiguration;
    }

    public SSHClient newAuthedConnection(SshSystemRequest request) throws IOException {
        SSHClient sshClient = new SSHClient(config);
        sshClient.getTransport().addHostKeyVerifier(new PromiscuousVerifier());

        try {
            sshClient.connect(request.uri().getHost(), request.getPort());
            request.getCredential().authenticate(new Authenticator(sshClient));
        } catch (IOException e) {
            IOUtils.closeQuietly(sshClient);
            throw e;
        }

        return sshClient;
    }

    private final class Authenticator implements SshAuthenticator {
        private final SSHClient client;

        public Authenticator(SSHClient client) {
            this.client = client;
        }

        @Override
        public void authByPassword(SshCredential credential, char[] password)
                throws IOException {
            client.authPassword(credential.getUsername(), password);
        }

        @Override
        public void authByPublicKey(SshCredential credential, byte[] privateKey)
                throws IOException {
            KeyProvider keyProvider = client.loadKeys(new String(privateKey), null, null);
            client.authPublickey(credential.getUsername(), keyProvider);
        }

        private static final String REALM_PROPERTY = "java.security.krb5.realm";
        private static final String KDC_PROPERTY = "java.security.krb5.kdc";
        private static final String CONFIG_PROPERTY = "java.security.auth.login.config";

        @Override
        public void authByKerberos(SshCredential credential, String realm, String kdcHostname)
                throws IOException {

            final String originalRealm = System.getProperty(REALM_PROPERTY);
            final String originalKdc = System.getProperty(KDC_PROPERTY);
            final String originalConfig = System.getProperty(CONFIG_PROPERTY);

            try (TempPath jaasConfFile = TempPath.createFile()) {
                MoreFiles.write(jaasConfFile.path(),
                        "Krb5LoginContext { com.sun.security.auth.module.Krb5LoginModule " +
                        "required refreshKrb5Config=true useTicketCache=true debug=true ; };",
                        Charset.defaultCharset()
                );

                // set properties necessary for Krb5LgoinContext
                System.setProperty(REALM_PROPERTY, realm);
                System.setProperty(KDC_PROPERTY, kdcHostname);
                System.setProperty(CONFIG_PROPERTY,
                        jaasConfFile.path().toAbsolutePath().toString());

                LoginContext lc = null;
                try {
                    lc = new LoginContext("Krb5LoginContext");
                    lc.login();
                } catch (LoginException e) {
                    throw new UserAuthException(e);
                }

                Oid krb5Oid;
                try {
                    krb5Oid = new Oid("1.2.840.113554.1.2.2");
                } catch (GSSException e) {
                    throw new UserAuthException("Failed to create Kerberos OID", e);
                }

                client.authGssApiWithMic(credential.getUsername(), lc, krb5Oid);
            } finally {
                restoreProperty(REALM_PROPERTY, originalRealm);
                restoreProperty(KDC_PROPERTY, originalKdc);
                restoreProperty(CONFIG_PROPERTY, originalConfig);
            }
        }

        private void restoreProperty(String key, String originalValue) {
            if (originalValue != null) {
                System.setProperty(key, originalValue);
            } else {
                System.clearProperty(key);
            }
        }
    }
}
