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

package org.onosproject.restconf.common;

/**
 * Type of Resource Identifier Nodes.
 */
public enum DataResourceIdentifierElementType {
    /*
     * Denotes that data resource that is identified by the current
     * identifier element cannot be replicated.
     */
    SINGLE_INSTANCE_NODE,

    /*
     * Denotes that the data resource that is represented by the current
     * identifier element can be replicated. This tells the reader of this
     * identifier element to read further into the element to retrieve the
     * instance key of the replicated resource node.
     */
    MULTI_INSTANCE_NODE,
}
