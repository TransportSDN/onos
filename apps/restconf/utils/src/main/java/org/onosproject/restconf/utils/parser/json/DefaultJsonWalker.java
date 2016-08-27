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

package org.onosproject.restconf.utils.parser.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.restconf.utils.parser.api.JsonListener;
import org.onosproject.restconf.utils.parser.api.JsonWalker;

import java.util.Iterator;
import java.util.Map;

/**
 * Represents implementation of JSON walk, which walks the JSON object node.
 */
public class DefaultJsonWalker implements JsonWalker {
    @Override
    public void walk(JsonListener jsonListener, String fieldName, ObjectNode objectNode) {

        JsonNodeType nodeType = objectNode.getNodeType();
        //enter the object node, the original ObjectNode should have a module name as fieldName.
        jsonListener.enterJsonNode(fieldName, objectNode);
        //the node has no children, then exist and return.
        if (!objectNode.isContainerNode()) {
            jsonListener.exitJsonNode(nodeType);
            return;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            //get the children entry of the node
            Map.Entry<String, JsonNode> currentChild = fields.next();
            String key = currentChild.getKey();
            JsonNode value = currentChild.getValue();
            //if the entry's value has its own children, do a recursion.
            //if the entry has no children, store the key and value.
            //for we don't know the specific type of the entry's value, we should
            //give it to a method which can handle JsonNode
            if (value.isContainerNode()) {
                walk(jsonListener, key, value);
            } else {
                jsonListener.enterJsonNode(key, value);
                jsonListener.exitJsonNode(value.getNodeType());
            }
        }
        jsonListener.exitJsonNode(nodeType);
    }


    /**
     * Walks the JSON data tree. This method is called when we don't know
     * the exact type of a json node.
     *
     * @param jsonListener Json listener implemented by the user
     * @param fieldName    the original object node field
     * @param rootNode     the json node which needs to be walk
     */
    private void walk(JsonListener jsonListener, String fieldName, JsonNode rootNode) {
        if (rootNode.isObject()) {
            walk(jsonListener, fieldName, (ObjectNode) rootNode);
            return;
        }

        if (rootNode.isArray()) {
            walk(jsonListener, fieldName, (ArrayNode) rootNode);
        }
    }

    /**
     * Walks the JSON data tree. This method is called when the user knows the the json
     * node type is ArrayNode.
     *
     * @param jsonListener Json listener implemented by the user
     * @param fieldName    the original object node field
     * @param rootNode     the json node which needs to be walk
     */
    private void walk(JsonListener jsonListener, String fieldName, ArrayNode rootNode) {
        if (rootNode == null) {
            return;
        }
        //enter the array node.
        jsonListener.enterJsonNode(fieldName, rootNode);
        //the node has no children, then exist and return.
        if (!rootNode.isContainerNode()) {
            jsonListener.exitJsonNode(rootNode.getNodeType());
            return;
        }

        Iterator<JsonNode> children = rootNode.elements();
        while (children.hasNext()) {
            JsonNode currentChild = children.next();
            if (currentChild.isContainerNode()) {
                walk(jsonListener, "", currentChild);
            } else {
                jsonListener.enterJsonNode("", currentChild);
                jsonListener.exitJsonNode(currentChild.getNodeType());
            }
        }
        jsonListener.exitJsonNode(rootNode.getNodeType());
    }
}
