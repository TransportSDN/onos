package org.onosproject.restconf.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure describing a node in the path of a
 * data resource identifier. A data node can be identified with
 * its node name and its name space. If the node can be replicated,
 * then a key (or multiple keys) of the instance is also needed.
 */
public class DataResourceIdentifierElement {
    private String nodeName;
    private String nameSpace;
    private DataResourceIdentifierElementType nodeType;
    private List<String> keyList;

    public DataResourceIdentifierElement(String name, DataResourceIdentifierElementType type) {
        this.nodeName = name;
        this.nodeType = type;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public DataResourceIdentifierElementType getNodeType() {
        return nodeType;
    }

    public void setNodeType(DataResourceIdentifierElementType nodeType) {
        this.nodeType = nodeType;
    }

    public List<String> getKeyList() {
        return keyList;
    }

    public void addKeyValue(String val) {
        if (keyList == null) {
            keyList = new ArrayList<String>();
        }
        keyList.add(val);
    }

}
