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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.restconf.utils.parser.api.JsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents implementation of interfaces to build and obtain JSON data tree.
 */
public class DefaultJsonBuilder implements JsonBuilder {
    private Logger log = LoggerFactory.getLogger(getClass());
    private StringBuilder treeString;
    private static final String LEFT_BRACE = "{";
    private static final String RIGHT_BRACE = "}";
    private static final String LEFT_BRACKET = "[";
    private static final String RIGHT_BRACKET = "]";
    private static final String COMMA = ",";
    private static final String COLON = ":";
    private static final String QUOTE = "\"";


    public DefaultJsonBuilder(String rootName) {
        checkNotNull(rootName);
        this.treeString = new StringBuilder(rootName);
    }

    public DefaultJsonBuilder() {
        this.treeString = new StringBuilder();
    }

    @Override
    public void addNodeTopHalf(String fieldName, JsonNodeType nodeType) {
        appendField(fieldName);

        switch (nodeType) {
            case OBJECT:
                treeString.append(LEFT_BRACE);
                break;
            case ARRAY:
                treeString.append(LEFT_BRACKET);
                break;
            default:
                log.error("Unknown support type {} for this method.", nodeType);
        }

    }

    @Override
    public void addNodeWithValueTopHalf(String fieldName, String value) {
        if (isNullOrEmpty(fieldName)) {
            return;
        }
        appendField(fieldName);
        if (value.isEmpty()) {
            return;
        }
        treeString.append(QUOTE);
        treeString.append(value);
        treeString.append(QUOTE);
        treeString.append(COMMA);
    }

    @Override
    public void addNodeWithSetTopHalf(String fieldName, Set<String> sets) {
        if (isNullOrEmpty(fieldName)) {
            return;
        }
        appendField(fieldName);
        treeString.append(LEFT_BRACKET);
        for (String el : sets) {
            treeString.append(QUOTE);
            treeString.append(el);
            treeString.append(QUOTE);
            treeString.append(COMMA);
        }
    }

    @Override
    public void addNodeBottomHalf(JsonNodeType nodeType) {

        switch (nodeType) {
            case OBJECT:
                removeCommaIfExist();
                treeString.append(RIGHT_BRACE);
                treeString.append(COMMA);
                break;
            case ARRAY:
                removeCommaIfExist();
                treeString.append(RIGHT_BRACKET);
                treeString.append(COMMA);
                break;
            case BINARY:
            case BOOLEAN:
            case MISSING:
            case NULL:
            case NUMBER:
            case POJO:
            case STRING:
                break;
            default:
                log.info("Unknown json node type {}", nodeType);
        }
    }

    @Override
    public String getTreeString() {
        removeCommaIfExist();
        return treeString.toString();
    }

    @Override
    public ObjectNode getTreeNode() {
        ObjectNode node = null;
        try {
            node = (ObjectNode) (new ObjectMapper()).readTree(treeString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

    private void appendField(String fieldName) {
        if (!fieldName.isEmpty()) {
            treeString.append(QUOTE);
            treeString.append(fieldName);
            treeString.append(QUOTE);
            treeString.append(COLON);
        }
    }

    private void removeCommaIfExist() {
        if (treeString.charAt(treeString.length() - 1) == COMMA.charAt(0)) {
            treeString.deleteCharAt(treeString.length() - 1);
        }
    }
}
