/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.api.json;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Abstraction of an entity which provides interfaces for Json walk. This interface serve as common tools
 * for anyone who needs to parse the json node with depth-first algorithm.
 */
public interface JsonWalker {

    /**
     * Walks the JSON data tree. Protocols implements JSON listener service
     * and walks JSON tree with input as implemented object. JSON walker provides
     * call backs to implemented methods.
     *
     * @param jsonListener Json listener implemented by the user
     * @param node         the json node which needs to be walk
     */
    void walker(JsonListener jsonListener, ObjectNode node);
}
