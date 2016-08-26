/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.restconf.ymsadapter.api;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Data structure that encapsulates the results
 * from YMS Adapter operations.
 */
public final class YmsAdapterResponse {
    private ObjectNode resultData;
    private boolean success;

    public static class Builder {
        private ObjectNode resultData;
        private boolean success;

        public Builder() {

        }

        public Builder resultData(ObjectNode data) {
            this.resultData = data;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public YmsAdapterResponse build() {
            return new YmsAdapterResponse(this);
        }
    }

    private YmsAdapterResponse(Builder builder) {
        this.resultData = builder.resultData;
        this.success = builder.success;
    }

    public boolean isSuccessful() {
        return this.success;
    }

    public ObjectNode getJsonData() {
        return resultData;
    }

    public boolean hasData() {
        return resultData == null;
    }

}
