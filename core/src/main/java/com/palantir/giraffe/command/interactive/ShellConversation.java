/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.palantir.giraffe.command.CommandFuture;

/**
 * Asynchronously responds to interactive command execution, as represented by a
 * {@code CommandFuture}, using a {@code ResponseProvider}.
 *
 * @author alake
 */
public final class ShellConversation extends AbstractStreamWatcher {
    private final ResponseProvider<String> outputLookup;
    private final OutputStream stdIn;

    public ShellConversation(ResponseProvider<String> convoScript,
                             CommandFuture commandExecution) {
        super(commandExecution.getStdOut(), StandardCharsets.UTF_8);
        this.outputLookup = convoScript;
        this.stdIn = commandExecution.getStdIn();
        checkNotNull(convoScript);
        checkNotNull(stdIn);
    }

    @Override
    protected boolean processToken(String matchedToken) throws Exception {
        String reply = outputLookup.lookupResponse(matchedToken);

        if (reply == null) {
            // The prompt in not in the script... we don't know how to answer
            // Return false to indicate we did not know how to process the token
            return false;
        }

        byte[] replyBytes = reply.getBytes(StandardCharsets.UTF_8);
        stdIn.write(replyBytes, 0, replyBytes.length);
        stdIn.flush();

        return true;
    }
}
