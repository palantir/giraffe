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

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import com.google.common.collect.ImmutableMap;
import com.palantir.giraffe.ssh.KerberosSshCredential;
import com.palantir.giraffe.ssh.PasswordSshCredential;
import com.palantir.giraffe.ssh.PublicKeySshCredential;
import com.palantir.giraffe.ssh.SshAuthenticator;
import com.palantir.giraffe.ssh.SshSystemRequest;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

final class SshConnectionFactory {

    private static final String KRB_ENTRY_NAME = "GiraffeKrb5";

    private final Config config;

    public SshConnectionFactory(Config sshjConfiguration) {
        this.config = sshjConfiguration;
    }

    public SSHClient newAuthedConnection(SshSystemRequest request) throws IOException {
        SSHClient sshClient = new SSHClient(config);
        sshClient.getTransport().addHostKeyVerifier(new PromiscuousVerifier());
        if (request.getKeepaliveInterval() > 0) {
            sshClient.getConnection().getKeepAlive().setKeepAliveInterval(
                    request.getKeepaliveInterval());
        }

        try {
            sshClient.connect(request.uri().getHost(), request.getPort());
            request.getCredential().authenticate(new Authenticator(sshClient));
        } catch (IOException e) {
            IOUtils.closeQuietly(sshClient);
            throw e;
        }

        return sshClient;
    }

    private static final class Authenticator implements SshAuthenticator {

        private final SSHClient client;

        public Authenticator(SSHClient client) {
            this.client = client;
        }

        @Override
        public void authByPassword(PasswordSshCredential credential) throws IOException {
            client.authPassword(credential.getUsername(), credential.getPassword());
        }

        @Override
        public void authByPublicKey(PublicKeySshCredential credential) throws IOException {
            String privateKey = new String(credential.getPrivateKey());
            KeyProvider keyProvider = client.loadKeys(privateKey, null, null);
            client.authPublickey(credential.getUsername(), keyProvider);
        }

        @Override
        public void authByKerberos(KerberosSshCredential credential) throws IOException {
            String user = credential.getUsername();
            if (kerberosDebugEnabled()) {
                System.setProperty("sun.security.krb5.debug", "true");
            } else {
                System.clearProperty("sun.security.krb5.debug");
            }

            LoginContext lc = null;
            try {
                lc = new LoginContext(KRB_ENTRY_NAME, null, null, new KrbAuthConfiguration(user));
                lc.login();
            } catch (LoginException e) {
                throw new UserAuthException(e);
            }

            Oid krb5Oid;
            try {
                krb5Oid = new Oid("1.2.840.113554.1.2.2");
            } catch (GSSException e) {
                // this will never happen, krb5 OID is always valid
                throw new AssertionError();
            }
            client.authGssApiWithMic(user, lc, krb5Oid);
        }
    }

    private static final class KrbAuthConfiguration extends Configuration {
        private final AppConfigurationEntry krbEntry;

        KrbAuthConfiguration(String principal) {
            ImmutableMap.Builder<String, String> options = ImmutableMap.builder();
            options.put("refreshKrb5Config", "true");
            options.put("useTicketCache", "true");
            options.put("doNotPrompt", "true");
            options.put("principal", principal);
            if (kerberosDebugEnabled()) {
                options.put("debug", "true");
            }

            krbEntry = new AppConfigurationEntry(
                    "com.sun.security.auth.module.Krb5LoginModule",
                    LoginModuleControlFlag.REQUIRED,
                    options.build());
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if (KRB_ENTRY_NAME.equals(name)) {
                return new AppConfigurationEntry[] { krbEntry };
            } else {
                return null;
            }
        }
    }

    private static boolean kerberosDebugEnabled() {
        return Boolean.getBoolean(KerberosSshCredential.DEBUG_PROPERTY);
    }
}
