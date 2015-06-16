/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.giraffe.command.interactive;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;

/**
 * An instrumented {@link AbstractStreamWatcher} to test tokenization and
 * matching behavior. Intended to work with a {@link BarrierByteWriter}.
 *
 * @author alake
 */
public final class InstrumentedStreamWatcher extends AbstractStreamWatcher {
    private final List<String> processTokenCallStrings;
    private final CyclicBarrier barrier;
    private final Map<String, Boolean> processTokenReplies;
    private Queue<Integer> processTokenBlockCounts;
    private int remainingCallsUntilBlock;

    public InstrumentedStreamWatcher(InputStream iStream,
                                     Charset charset,
                                     CyclicBarrier barrier) {
        super(iStream, charset);
        this.processTokenCallStrings = new ArrayList<>();
        this.processTokenReplies = new HashMap<>();
        this.processTokenBlockCounts = new LinkedList<>();
        this.barrier = barrier;
        this.remainingCallsUntilBlock = -1;
    }

    @Override
    protected boolean processToken(String matchedToken) throws Exception {
        boolean wasProcessed = true;
        processTokenCallStrings.add(matchedToken);
        if (processTokenReplies.containsKey(matchedToken)) {
            wasProcessed = processTokenReplies.get(matchedToken);
        }

        remainingCallsUntilBlock--;
        if (remainingCallsUntilBlock == 0) {
            barrier.await();
            if (!processTokenBlockCounts.isEmpty()) {
                remainingCallsUntilBlock = processTokenBlockCounts.remove();
            }
        }
        return wasProcessed;
    }

    /*
     * Public Methods
     */

    public void setBarrierBlockPoints(List<Integer> blockPoints) {
        checkNotNull(blockPoints);
        checkArgument(blockPoints.size() >= 1);
        this.processTokenBlockCounts = new ArrayDeque<>(blockPoints);
        this.remainingCallsUntilBlock = processTokenBlockCounts.remove();
    }

    public void addResponse(String token, boolean response) {
        processTokenReplies.put(token, response);
    }

    public List<String> getReceivedTokens() {
        return processTokenCallStrings;
    }
}
