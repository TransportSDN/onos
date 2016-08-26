/*
 * Copyright 2016 Open Networking Laboratory
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
 * Embodiment of YANG notification information. The notification is received by the
 * YMS Adapter from YMS. The data sent from YMS is in the YDT format. The YMS Adapter
 * converts it into JSON format.
 *
 * NOTE: A shortcoming of this class implementation is that it binds to the JSON data format,
 * while the RESTCONF Server should support both JSON and XML. So, the implementation will be
 * changed to use generic type when the RESTCONF Server supports XML.
 */
public class YangNotificationData {

    private ObjectNode data;

    public YangNotificationData(ObjectNode data) {
        this.data = data;
    }

    public ObjectNode getData() {
        return data;
    }

    public void setData(ObjectNode data) {
        this.data = data;
    }

}
