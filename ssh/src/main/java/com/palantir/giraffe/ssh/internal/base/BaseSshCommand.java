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

import com.palantir.giraffe.internal.AbstractCommand;

/**
 * Abstract {@code Command} implementation for SSH-based execution systems.
 *
 * @author bkeyes
 *
 * @param <C> the type of the implementing class
 */
public abstract class BaseSshCommand<C extends BaseSshCommand<C>> extends AbstractCommand {

    /**
     * Builds {@code BaseSshCommand} instances.
     *
     * @param <C> the type of the build command
     */
    protected abstract static class Builder<C extends BaseSshCommand<C>>
            extends AbstractCommand.Builder {

        protected Builder(String command) {
            super(command);
        }

        @Override
        public abstract C build();
    }

    private final BaseSshExecutionSystem<C> es;

    protected BaseSshCommand(BaseSshExecutionSystem<C> es, Builder<C> builder) {
        super(builder);
        this.es = es;
    }

    @Override
    public BaseSshExecutionSystem<C> getExecutionSystem() {
        return es;
    }
}
