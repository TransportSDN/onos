/*
 *
 *  * Copyright 2016 Open Networking Laboratory
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.onosproject.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.api.json.JsonBuilder;
import org.onosproject.api.json.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents implementation of interfaces to build and obtain JSON data tree.
 */
public class JsonBuilderImpl implements JsonBuilder {
    private Logger log = LoggerFactory.getLogger(getClass());
    private StringBuilder treeString = new StringBuilder();
    private static final  String LEFT_BRACE = "{";
    private static final  String RIGHT_BRACE = "}";
    private static final  String LEFT_BRACKET = "[";
    private static final  String RIGHT_BRACKET = "]";
    private static final  String COMMA = ",";
    private static final  String COLON = ":";


    public JsonBuilderImpl(StringBuilder initStringBuilder) {
        checkNotNull(initStringBuilder);
        this.treeString = initStringBuilder.toString().isEmpty() ? initStringBuilder.append("{") : initStringBuilder;
    }

    public JsonBuilderImpl() {
        this.treeString = new StringBuilder("{");
    }

    @Override
    public void addNodeTopHalf(String fieldName, NodeType nodeType) {

        switch (nodeType) {
            case Object:
                treeString.append(fieldName);
                treeString.append(COLON);
                treeString.append(LEFT_BRACE);
                break;
            case Array:
                treeString.append(fieldName);
                treeString.append(COLON);
                treeString.append(LEFT_BRACKET);
                break;
            case Text:
                break;
            default:
                log.error("Unknown node type {} for {}", nodeType, getClass());
        }

    }

    @Override
    public void addNodeWithValueTopHalf(String fieldName, String value) {
        treeString.append(fieldName);
        treeString.append(COLON);
        treeString.append(value);
        treeString.append(COMMA);
    }

    @Override
    public void addNodeWithSetTopHalf(String fieldName, Set<String> sets) {
        treeString.append(fieldName);
        treeString.append(COLON);
        treeString.append(LEFT_BRACKET);
        for (String el : sets) {
            treeString.append(el);
            treeString.append(COMMA);
        }
    }

    @Override
    public void addNodeBottomHalf(NodeType nodeType) {

        switch (nodeType) {
            case Object:
                removeCommaIfExist();
                treeString.append(RIGHT_BRACE);
                break;
            case Array:
                removeCommaIfExist();
                treeString.append(RIGHT_BRACKET);
                break;
            case Text:
                break;
            default:
                log.error("unknown node type {}", nodeType);
        }
    }

    @Override
    public String getTreeString() {
        return treeString.toString();
    }

    @Override
    public ObjectNode getTreeNode() {
        return (new ObjectMapper()).valueToTree(treeString.toString());
    }

    void removeCommaIfExist() {
        if (treeString.charAt(treeString.length() - 1) == COMMA.charAt(0)) {
            treeString.deleteCharAt(treeString.length() - 1);
        }
    }
}
