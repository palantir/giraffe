/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for performing interactive execution. Operates on an
 * {@code InputStream} and performs input parsing requiring subclasses to only
 * implement the logic for processing tokens.
 *
 * @author alake
 */
/* package */abstract class AbstractStreamWatcher {
    private static final int INITIAL_TOKENIZER_BUFFER_SIZE = 8192;
    private static final int CHAR_BUFFER_SIZE = 8192;
    private static final String MATCH_END_OF_LINE = "(\r\n|\r|\n)";

    private final ExecutorService inputStreamProcessor;
    private final InputStreamReader streamReader;

    /*
     * Protected Constructor
     */

    protected AbstractStreamWatcher(InputStream streamToWatch, Charset streamCharset) {
        checkNotNull(streamToWatch);
        checkNotNull(streamCharset);
        this.streamReader = new InputStreamReader(streamToWatch, streamCharset);
        this.inputStreamProcessor = Executors.newSingleThreadExecutor();
    }

    /*
     * Public Methods
     */

    /**
     * Asynchronously starts an interaction. Input is tokenized by line (any
     * string of text ending in \r, \n, or \r\n). Calling {@code start} again
     * has no effect.
     *
     * @return A {@code Future<?>} which represents the asynchronous execution
     *         of the stream processing.
     */
    public final Future<Void> start() {
        return start(MATCH_END_OF_LINE);
    }

    /**
     * Asynchronously starts an interaction. Input is tokenized by the provided
     * regular expression. Calling {@code start} again has no effect.
     *
     * @return A {@code Future<?>} which represents the asynchronous execution
     *         of the stream processing.
     */
    public final Future<Void> start(final String tokenDelimiterRegex) {
        Callable<Void> streamTokenizer = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tokenizeStream(tokenDelimiterRegex);
                return null;
            }
        };

        Future<Void> processorFuture = inputStreamProcessor.submit(streamTokenizer);

        // Auto-cleanup when input stream processors are done or fail
        // This will happen as long as the InputStream is closed
        inputStreamProcessor.shutdown();

        return processorFuture;
    }

    /*
     * Protected Methods
     */

    /**
     * @param matchedToken The string token
     *
     * @return true if the token was successfully processed; false otherwise
     */
    protected abstract boolean processToken(String matchedToken) throws Exception;

    /*
     * Private Helpers
     */

    private void tokenizeStream(String tokenDelimiterRegex) throws Exception {
        Pattern delim = Pattern.compile(tokenDelimiterRegex);

        // Workspace buffer for bulk get methods
        final CharBuffer charBuffer = CharBuffer.allocate(CHAR_BUFFER_SIZE);

        // Tokenizer buffer
        final StringBuilder tokenizerBuffer = new StringBuilder(INITIAL_TOKENIZER_BUFFER_SIZE);

        while (true) {
            do {
                // Blocking read
                int numCharsRead = streamReader.read(charBuffer);
                if (numCharsRead == -1) {
                    // -1 signifies end of stream
                    return;
                } else {
                    charBuffer.flip();
                    tokenizerBuffer.append(charBuffer, 0, numCharsRead);
                }
            } while (streamReader.ready());

            // Process tokens
            int start = 0;
            Matcher m = delim.matcher(tokenizerBuffer);
            while (m.find()) {
                String tokenToProcess = tokenizerBuffer.substring(start, m.start());
                processToken(tokenToProcess);
                start = m.end();
            }

            if (start < tokenizerBuffer.length()) {
                // Incomplete token at end of string.
                // Check to see if can be processed, otherwise buffer it in
                // hopes that future input will complete the token.
                String incompleteToken = tokenizerBuffer.substring(start);
                if (processToken(incompleteToken)) {
                    tokenizerBuffer.delete(0, tokenizerBuffer.length());
                } else {
                    tokenizerBuffer.delete(0, start);
                }
            } else {
                tokenizerBuffer.delete(0, tokenizerBuffer.length());
            }
        }
    }
}
