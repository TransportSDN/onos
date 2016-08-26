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
package org.onosproject.restconf.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.server.ChunkedOutput;

import java.net.URI;

/**
 * Abstraction of RESTCONF Server functionality according to the
 * RESTCONF RFC (no official RFC number yet).
 */
public interface RestconfService {
    /**
     * Process GET request against a data resource.
     *
     * @param uri URI of the data resource.
     * @return JSON representation of the data resource.
     * @throws RestconfException
     */
    ObjectNode doGetOperation(URI uri) throws RestconfException;

    /**
     * Process POST request against a data resource.
     *
     * @param uri      URI of the data resource to be created.
     * @param rootNode JSON representation of the data resource.
     */
    void doPostOperation(URI uri, ObjectNode rootNode);

    /**
     * Process PUT request against a data resource.
     *
     * @param uri      URI of the data resource to be created.
     * @param rootNode JSON representation of the data resource.
     * @throws RestconfException
     */
    void doPutOperation(URI uri, ObjectNode rootNode) throws RestconfException;

    /**
     * Process the delete operation on a data resource.
     *
     * @param uri URI of the data resource to be deleted.
     */
    void doDeleteOperation(URI uri) throws RestconfException;

    /**
     * Retrieve the RESTCONF Root directory.
     *
     * @return RESTCONF Root directory
     */
    String getRestconfRootPath();

    /**
     * Creates a worker thread to listen to events and write to chunkedOutput.
     * The worker thread blocks if no events arrive.
     *
     * @param streamId ID of the RESTCONF stream to subscribe.
     * @param output   A string data stream
     * @throws RestconfException
     */
    void subscribeEventStream(String streamId, ChunkedOutput<String> output) throws RestconfException;
}
