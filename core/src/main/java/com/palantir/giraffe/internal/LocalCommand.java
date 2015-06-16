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
package com.palantir.giraffe.internal;

import java.util.Objects;

final class LocalCommand extends AbstractCommand {

    static final class Builder extends AbstractCommand.Builder {
        private final LocalExecutionSystem es;

        Builder(String command, LocalExecutionSystem es) {
            super(command);
            this.es = es;
        }

        @Override
        public LocalCommand build() {
            return new LocalCommand(es, this);
        }
    }

    private final LocalExecutionSystem es;

    private LocalCommand(LocalExecutionSystem es, Builder builder) {
        super(builder);
        this.es = es;
    }

    @Override
    public LocalExecutionSystem getExecutionSystem() {
        return es;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExecutionSystem(), getExecutable(), getArguments());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof LocalCommand)) {
            return false;
        } else {
            LocalCommand that = (LocalCommand) obj;
            return getExecutionSystem().equals(that.getExecutionSystem())
                    && getExecutable().equals(that.getExecutable())
                    && getArguments().equals(that.getArguments());
        }
    }

}
