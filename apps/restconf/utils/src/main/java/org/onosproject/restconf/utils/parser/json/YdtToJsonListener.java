package org.onosproject.restconf.utils.parser.json;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.onosproject.restconf.utils.parser.api.JsonBuilder;
import org.onosproject.yms.ydt.YdtContext;
import org.onosproject.yms.ydt.YdtListener;

public class YdtToJsonListener implements YdtListener {

    private JsonBuilder jsonBuilder;

    public YdtToJsonListener(JsonBuilder jsonBuilder) {
        this.jsonBuilder = jsonBuilder;
    }

    @Override
    public void enterYdtNode(YdtContext ydtContext) {

        switch (ydtContext.getYdtType()) {

            case SINGLE_INSTANCE_NODE:
                jsonBuilder.addNodeTopHalf(ydtContext.getName(), JsonNodeType.OBJECT);
                break;
            case MULTI_INSTANCE_NODE:
                jsonBuilder.addNodeTopHalf(ydtContext.getName(), JsonNodeType.ARRAY);
                break;
            case SINGLE_INSTANCE_LEAF_VALUE_NODE:
                jsonBuilder.addNodeWithValueTopHalf(ydtContext.getName(), ydtContext.getValue());
                break;
            case MULTI_INSTANCE_LEAF_VALUE_NODE:
                jsonBuilder.addNodeWithSetTopHalf(ydtContext.getName(), ydtContext.getValueSet());
                break;
        }

    }

    @Override
    public void exitYdtNode(YdtContext ydtContext) {
        switch (ydtContext.getYdtType()) {

            case SINGLE_INSTANCE_NODE:
                jsonBuilder.addNodeBottomHalf(JsonNodeType.OBJECT);
                break;
            case MULTI_INSTANCE_NODE:
                jsonBuilder.addNodeBottomHalf(JsonNodeType.ARRAY);
                break;
            case SINGLE_INSTANCE_LEAF_VALUE_NODE:
                jsonBuilder.addNodeBottomHalf(JsonNodeType.STRING);
                break;
            case MULTI_INSTANCE_LEAF_VALUE_NODE:
                jsonBuilder.addNodeBottomHalf(JsonNodeType.ARRAY);
                break;
        }
    }
}
