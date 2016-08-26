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
package org.onosproject.restconf.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A data structure describing the location of a dada resource node
 * in the logical YANG datastore. The logical YANG datastore is a tree structure.
 * As a result, DataResourceIdentifier needs to provide a xpath-like structure to
 * represent the path from root node to the target node.
 * <p>
 * NOTE: This data structure should only be used to locate data resources. For other resources
 * defined in the RESTCONF RFC, this class is not applicable.
 */
public final class DataResourceIdentifier {

    /*
     * RESTCONF Root path is a pre-configured constant set by the
     * RESTCONF Server. It's value looks like, for example, "/onos/restconf".
     */
    private final String restconfRootPath;

    /*
     * Ordered list of node IDs from root node to current
     * node.
     */
    private List<DataResourceIdentifierElement> nodeIdList;


    /*
     * Since DataResourceIdentifier is designed to be a general method for various applications
     * (not only limited to RestconfMgr) to use, A builder that builds an identifier from various input
     * formats is offered here. Currently the Builder can take 2 input formats:
     *
     * 1. A string representing a data resource path. (The format of the path is
     *    described in section 3.5.1.1 of the RESTCONF RFC.) The input string should start with
     *    the top level node. For example, to identify a target node in the following location:
     *
     *    /onos/restconf/data/top-component/some-leaflist=fred
     *
     *    the application can make the following function call:
     *
     *    resourceId = new DataResourceIdentifier.Builder("/top-component/some-leaflist=fred").build;
     *
     * 2. The URI of the target data resource node. For example, assuming that the RESTCONF Root Path is
     *    "/onos/restconf", the following call will create a resource identifier of a target data node with
     *    a uri.
     *
     *    resourceId = new DataResourceIdentifier.Builder("/onos/restconf", uri).build;
     *
     */
    public static class Builder {
        private URI uri;
        private String restconfRootPath;
        private String dataResourcePath;

        private List<DataResourceIdentifierElement> nodeIdList;

        public Builder(String dataResourcePath) {
            this.dataResourcePath = dataResourcePath;
            this.nodeIdList = new ArrayList<DataResourceIdentifierElement>();
        }

        public Builder(String rootPath, URI uri) {
            this.restconfRootPath = rootPath;
            this.uri = uri;
            this.nodeIdList = new ArrayList<DataResourceIdentifierElement>();
        }

        public DataResourceIdentifier build() {
            buildNodeIdList();
            return new DataResourceIdentifier(this);
        }

        private void buildNodeIdList() {
            if (this.dataResourcePath == null) {
                buildDataResourcePathFromUri();
            }

            buildNodeIdListFromDataResourcePath();
        }

        private void buildDataResourcePathFromUri() {
            checkNotNull(this.restconfRootPath, "restconf root path must be specified");
            checkNotNull(this.uri, "uri must be specified");

            // The URI path is expected to start with the root path.
            String path = this.uri.getPath();
            checkNotNull(path, "uri path cannot be null");
            if (!path.startsWith(this.restconfRootPath)) {
                // URI has wrong root path
                return;
            }

            path = path.substring(this.restconfRootPath.length()); // skip the leading root path
            checkNotNull(path, "uri's data path cannot be null");
            if (!path.startsWith("/data")) {
                //path is not a data resource path
                return;
            }

            this.dataResourcePath = path.substring("/data".length()); // skip "/data"
        }


        private void buildNodeIdListFromDataResourcePath() {
            checkNotNull(this.dataResourcePath, "dataresource path must be specified");

            String[] tokens = this.dataResourcePath.split("/");
            for (String token : tokens) {
                if (token.isEmpty()) {
                    /*
                     * Leading or trailing slashes in dataResourcePath can
                     * cause the split function to create empty tokens, which
                     * should be skipped.
                     */
                    continue;
                }
                addNodeIdentifierToNodeList(token);
            }
        }

        private void addNodeIdentifierToNodeList(String identifier) {
            checkNotNull(identifier, "identifier cannot be null");

            String idStr = identifier; // make a copy of the input, as we need to modify it

            /*
             * Check if the identifier contains name space string.
             * If namespace is found in this identifier, then use it.
             * Otherwise, it is inherited from the previous node in the list.
             */
            String nameSpace = null;
            int idx = idStr.indexOf(":");
            if (idx > 0) {
                // We have found a name space string.
                nameSpace = idStr.substring(0, idx);
                idStr = idStr.substring(idx + 1, idStr.length());
            } else {
                // Get namespace from the last node in the list
                if (nodeIdList != null && nodeIdList.size() > 0) {
                    nameSpace = nodeIdList.get(nodeIdList.size() - 1).getNameSpace();
                }
            }

            /*
             * Next check if the identifier has any key values.
             * i.e., for multi-instance nodes, their names will have
             * key values, which are separated from name by equal (=) sign.
             */
            idx = idStr.indexOf("=");
            if (idx > 0) {
                // This is a multi-instance node.
                String listNodeName = idStr.substring(0, idx);
                DataResourceIdentifierElement listNode =
                        new DataResourceIdentifierElement(listNodeName,
                                                          DataResourceIdentifierElementType.MULTI_INSTANCE_NODE);
                listNode.setNameSpace(nameSpace);
                idStr = idStr.substring(idx + 1, idStr.length());

                // Add keys to the list node.
                String[] keys = idStr.split(",");
                for (String key : keys) {
                    listNode.addKeyValue(key.equals("") ? "null" : key);
                }

                this.nodeIdList.add(listNode);

            } else {
                // This is a single instance node
                DataResourceIdentifierElement singleNode =
                        new DataResourceIdentifierElement(idStr,
                                                          DataResourceIdentifierElementType.SINGLE_INSTANCE_NODE);
                singleNode.setNameSpace(nameSpace);
                this.nodeIdList.add(singleNode);
            }
        }
    }

    private DataResourceIdentifier(Builder builder) {
        this.restconfRootPath = builder.restconfRootPath;
        this.nodeIdList = builder.nodeIdList;
    }

    public List<DataResourceIdentifierElement> getNodeIdList() {
        return nodeIdList;
    }

    public boolean isValid() {
        /*
         * If nodeIdList is not initialized or empty, then that means the
         * builder did not run properly.
         */
        if (this.nodeIdList == null || this.nodeIdList.isEmpty()) {
            return false;
        }

        return true;
    }
}
