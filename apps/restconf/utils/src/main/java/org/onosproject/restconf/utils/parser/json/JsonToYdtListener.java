package org.onosproject.restconf.utils.parser.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.onosproject.restconf.utils.exceptions.JsonParseException;
import org.onosproject.restconf.utils.parser.api.JsonListener;
import org.onosproject.yms.ydt.YdtBuilder;
import org.onosproject.yms.ydt.YdtType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

public class JsonToYdtListener implements JsonListener {
    private YdtBuilder ydtBuilder;

    public JsonToYdtListener(YdtBuilder ydtBuilder) {
        this.ydtBuilder = ydtBuilder;
    }

    @Override
    public void enterJsonNode(String fieldName, JsonNode node) {
        if (isNullOrEmpty(fieldName)) {
            return;
        }

        switch (node.getNodeType()) {
            case OBJECT:
                ydtBuilder.addChild(fieldName, null, YdtType.SINGLE_INSTANCE_NODE);
                break;
            case ARRAY:
                processArrayNode(fieldName, node);
                break;
            //TODO for now, just process the following three node type, check if there are other types later.
            case STRING:
            case NUMBER:
            case BOOLEAN:
                ydtBuilder.addLeaf(fieldName, null, node.asText());
                break;
            case BINARY:
            case MISSING:
            case NULL:
            case POJO:
                throw new JsonParseException(String.format("Unsupported node type %s filed name is %s",
                        node.getNodeType(), fieldName));
            default:

        }

    }

    @Override
    public void exitJsonNode(JsonNodeType nodeType) {
        ydtBuilder.traverseToParent();
    }

    private void processArrayNode(String fieldName, JsonNode node) {
        ArrayNode arrayNode = (ArrayNode) node;
        Set<String> sets = new HashSet<>();
        Iterator<JsonNode> elements = arrayNode.elements();
        boolean isLeafList = true;
        while (elements.hasNext()) {
            JsonNode element = elements.next();
            JsonNodeType eleType = element.getNodeType();
            if (eleType == JsonNodeType.STRING ||
                    eleType == JsonNodeType.NUMBER
                    ) {
                sets.add(element.asText());
            } else {
                isLeafList = false;
            }
        }
        if (isLeafList) {
            ydtBuilder.addLeaf(fieldName, null, sets);
        } else {
            ydtBuilder.addChild(fieldName, null, YdtType.MULTI_INSTANCE_NODE);
        }
    }
}
