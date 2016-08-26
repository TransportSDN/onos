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
package org.onosproject.restconf.ymsadapter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.event.AbstractListenerManager;
import org.onosproject.event.ListenerTracker;
import org.onosproject.restconf.api.RestconfException;
import org.onosproject.restconf.common.DataResourceIdentifier;
import org.onosproject.restconf.ymsadapter.api.YangNotificationEvent;
import org.onosproject.restconf.ymsadapter.api.YangNotificationListener;
import org.onosproject.restconf.ymsadapter.api.YmsAdapterResponse;
import org.onosproject.restconf.ymsadapter.api.YmsAdapterService;
import org.onosproject.restconf.ymsadapter.api.YmsDataOperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;

/**
 * Implementation of the YMS Adapter.
 */
@Service
@Component(immediate = true)
public class YmsAdapterServiceManager extends AbstractListenerManager<YangNotificationEvent, YangNotificationListener>
        implements YmsAdapterService {

    private final Logger log = LoggerFactory.getLogger(getClass());


    // TODO: YMS service
    //@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    //protected YmsService ymsService;

    private ListenerTracker listeners;

    @Activate
    protected void activate() {
        eventDispatcher.addSink(YangNotificationEvent.class, listenerRegistry);

        //TODO: register to YMS event notification
        //listeners = new ListenerTracker();
        //listeners.addListener(ymsService, new InternalYmsNotificationListener());
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        listeners.removeListeners();
        eventDispatcher.removeSink(YangNotificationEvent.class);

        log.info("Stopped");
    }

    /* TODO: YMS notification handler
    private class InternalYmsNotificationListener implements YmsNotificationListener {
        @Override
        public void event(YmsNotificationEvent event) {
            switch (event.type()) {
                case YANG_NOTIFICATION:
                    ObjectNode data = null;

                    post(new YangNotificationEvent(Type.YANG_NOTIFICATION,
                                                   new YangNotificationData(data)));

                default:
                    break;
            }
        }
    }
    */

    @Override
    public YmsAdapterResponse dataResourceOperation(DataResourceIdentifier rid,
                                                    YmsDataOperationType op,
                                                    ObjectNode inData)
            throws RestconfException {

        if (!rid.isValid()) {
            throw new RestconfException("Invalid data resource id",
                                        Status.INTERNAL_SERVER_ERROR);
        }

        YmsAdapterResponse result = null;

        switch (op) {
            case READ:
                result = retrieveDataResource(rid);
                break;
            case CREATE:
                result = createDataResource(rid, inData);
                break;
            case DELETE:
                result = deleteDataResource(rid);
                break;
            default:
                throw new RestconfException("unsupported YANG data operation",
                                            Status.INTERNAL_SERVER_ERROR);

        }

        return result;
    }

    /**
     * @param rid
     * @return
     */
    private YmsAdapterResponse deleteDataResource(DataResourceIdentifier rid) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param rid
     * @param inData
     * @return
     */
    private YmsAdapterResponse createDataResource(DataResourceIdentifier rid,
                                                  ObjectNode inData) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param rid
     * @return
     */
    private YmsAdapterResponse retrieveDataResource(DataResourceIdentifier rid) {
        // TODO Auto-generated method stub
        return null;
    }
}
