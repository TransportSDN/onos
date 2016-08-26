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
import org.onosproject.event.ListenerService;
import org.onosproject.restconf.api.RestconfException;
import org.onosproject.restconf.common.DataResourceIdentifier;

/**
 * YMS Adapter Interface. YMS Adapter provides services to the RESTCONF
 * Manager.
 */
public interface YmsAdapterService extends ListenerService<YangNotificationEvent, YangNotificationListener> {

    /**
     * Conduct an operation on a YANG data resource node.
     *
     * @param drId   Data Resource Identifier which is used to locate the target data node.
     * @param opType Type of operation (e.g., read, create, or delete) against the target data node.
     * @param data   Input JSON data. If operation type is READ or DELETE, the value is ignored. If the
     *               operation type is CREATE, the value is the JSON encoding of the node to be created.
     * @return YmsAdapterResponse object.
     * @throws RestconfException
     */
    YmsAdapterResponse dataResourceOperation(DataResourceIdentifier drId,
                                             YmsDataOperationType opType,
                                             ObjectNode data)
            throws RestconfException;

}
