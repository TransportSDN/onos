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
package org.onosproject.restconf.restconfmgr;

import java.io.IOException;
import java.net.URI;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.glassfish.jersey.server.ChunkedOutput;
import org.onosproject.event.ListenerTracker;
import org.onosproject.restconf.api.RestconfException;
import org.onosproject.restconf.api.RestconfService;
import org.onosproject.restconf.common.DataResourceIdentifier;
import org.onosproject.restconf.utils.parser.json.ParserUtils;
import org.onosproject.restconf.ymsadapter.api.YmsAdapterResponse;
import org.onosproject.restconf.ymsadapter.api.YmsAdapterService;
import org.onosproject.restconf.ymsadapter.api.YangNotificationEvent;
import org.onosproject.restconf.ymsadapter.api.YangNotificationListener;
import org.onosproject.restconf.ymsadapter.api.YmsDataOperationType;
import org.onosproject.yms.ydt.YdtBuilder;
import org.onosproject.yms.ydt.YdtResponse;
import org.onosproject.yms.ydt.YmsOperationExecutionStatus;
import org.onosproject.yms.ymsm.YmsOperationType;
import org.onosproject.yms.ymsm.YmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Skeletal ONOS RESTCONF Server application. The RESTCONF Manager
 * implements the main logic of the RESTCONF Server.
 * <p/>
 * The design of the RESTCONF subsystem contains contains 3 major bundles:
 * <p/>
 * 1. RESTCONF Protocol Proxy (RPP). This bundle is implemented as a JAX-RS application.
 * It acts as the frond-end of the the RESTCONF server. It handles
 * HTTP requests that are sent to the RESTCONF Root Path. It then calls the RESTCONF Manager
 * to process the requests.
 * <p/>
 * 2. RESTCONF Manager. This is the back-end. It provides the main logic of the RESTCONF server.
 * It calls the YMS Adapter to operate on the YANG data objects.
 * <p/>
 * 3. YMS Adapter. This bundle is a shim layer between the RESTCONF Manager and the YANG Management
 * System (YMS). It converts RESTCONF Server specific data structure, such as JSON objects, into
 * YMS data structure (e.g., YDT), and vice versa. Another reason for creating this adaptation layer is
 * to decouple RESTCONF Server from YMS, so that should the YMS interface change, the RESTCONF Manager code
 * won't be affected.
 */
@Component(immediate = true)
@Service
public class RestconfMgr implements RestconfService {

    private static final String RESTCONF_ROOT = "/onos/restconf";

    private final int maxNumOfWorkerThreads = 5;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected YmsAdapterService ymsAdapterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected YmsService ymsService;

    private ListenerTracker listeners;

    private ConcurrentMap<String, BlockingQueue<ObjectNode>> eventQueueList =
            new ConcurrentHashMap<>();

    private ExecutorService workerThreadPool;

    @Activate
    protected void activate() {
        workerThreadPool = Executors.newFixedThreadPool(maxNumOfWorkerThreads,
                new ThreadFactoryBuilder()
                        .setNameFormat("restconf-worker")
                        .build());
        listeners = new ListenerTracker();
        listeners.addListener(ymsAdapterService, new InternalYangNotificationListener());
        log.info("RestconfMgr Started");
    }

    @Deactivate
    protected void deactivate() {
        listeners.removeListeners();
        shutdownAndAwaitTermination(workerThreadPool);
        log.info("RestconfMgr Stopped");
    }

    @Override
    public ObjectNode doGetOperation(URI uri) throws RestconfException {
        String identifier = uri.getPath().substring(getRestconfRootPath().length());
        //Get a root ydtBuilder
        YdtBuilder ydtBuilder = ymsService.getYdtBuilder(getRestconfRootPath(), null, YmsOperationType.QUERY);
        //Convert the URI to ydtBuilder
        ParserUtils.convertUriToYdt(identifier, ydtBuilder);
        //Execute the query operation
        YdtResponse ydtResponse = ymsService.executeOperation(ydtBuilder);

        YmsOperationExecutionStatus executionStatus = ydtResponse.getYmsOperationResult();
        if (executionStatus != YmsOperationExecutionStatus.EXECUTION_SUCCESS) {
            throw new RestconfException("YMS query operation failed",
                    Status.INTERNAL_SERVER_ERROR);
        }
        return ParserUtils.convertYdtToJson(ydtResponse.getRootNode(), ymsService.getYdtWalker());
    }

    @Override
    public void doPostOperation(URI uri, ObjectNode rootNode) {
        String identifier = uri.getPath().substring(getRestconfRootPath().length());
        //Get a root ydtBuilder
        YdtBuilder ydtBuilder = ymsService.getYdtBuilder(getRestconfRootPath(), null, YmsOperationType.EDIT_CONFIG);
        //Convert the URI to ydtBuilder
        ParserUtils.convertUriToYdt(identifier, ydtBuilder);
        ParserUtils.convertJsonToYdt(rootNode, ydtBuilder);
        //Execute the query operation
        YdtResponse ydtResponse = ymsService.executeOperation(ydtBuilder);
        YmsOperationExecutionStatus executionStatus = ydtResponse.getYmsOperationResult();
        if (executionStatus != YmsOperationExecutionStatus.EXECUTION_SUCCESS) {
            throw new RestconfException("YMS post operation failed.",
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doPutOperation(URI uri, ObjectNode rootNode) throws RestconfException {
        String identifier = uri.getPath().substring(getRestconfRootPath().length());
        //Get a root ydtBuilder
        YdtBuilder ydtBuilder = ymsService.getYdtBuilder(getRestconfRootPath(), null, YmsOperationType.EDIT_CONFIG);
        //Convert the URI to ydtBuilder
        ParserUtils.convertUriToYdt(identifier, ydtBuilder);
        ParserUtils.convertJsonToYdt(rootNode, ydtBuilder);
        //Execute the query operation
        YdtResponse ydtResponse = ymsService.executeOperation(ydtBuilder);
        YmsOperationExecutionStatus executionStatus = ydtResponse.getYmsOperationResult();
        if (executionStatus != YmsOperationExecutionStatus.EXECUTION_SUCCESS) {
            throw new RestconfException("YMS put operation failed.",
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doDeleteOperation(URI uri) throws RestconfException {
        String identifier = uri.getPath().substring(getRestconfRootPath().length());
        //Get a root ydtBuilder
        YdtBuilder ydtBuilder = ymsService.getYdtBuilder(getRestconfRootPath(), null, YmsOperationType.EDIT_CONFIG);
        //Convert the URI to ydtBuilder
        ParserUtils.convertUriToYdt(identifier, ydtBuilder);
        //Execute the query operation
        YdtResponse ydtResponse = ymsService.executeOperation(ydtBuilder);
        YmsOperationExecutionStatus executionStatus = ydtResponse.getYmsOperationResult();
        if (executionStatus != YmsOperationExecutionStatus.EXECUTION_SUCCESS) {
            throw new RestconfException("YMS put operation failed.",
                    Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getRestconfRootPath() {
        return RESTCONF_ROOT;
    }

    /**
     * Creates a worker thread to listen to events and write to chunkedOutput.
     * The worker thread blocks if no events arrive.
     *
     * @param streamId ID of the RESTCONF stream to subscribe.
     * @param output   A string data stream
     * @throws RestconfException
     */
    @Override
    public void subscribeEventStream(String streamId, ChunkedOutput<String> output) throws RestconfException {
        BlockingQueue<ObjectNode> eventQueue = new LinkedBlockingQueue<ObjectNode>();
        workerThreadPool.submit(new EventConsumer(output, eventQueue));
    }

    // Copied from apps/demo/DemoInstaller.java

    /**
     * Shutdown a pool cleanly if possible.
     *
     * @param pool an executorService
     */
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (Exception ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private class EventConsumer implements Runnable {

        private String queueId;
        private ChunkedOutput<String> output;
        private BlockingQueue<ObjectNode> bqueue;

        public EventConsumer(ChunkedOutput<String> output, BlockingQueue<ObjectNode> q) {
            this.queueId = Thread.currentThread().getName();
            this.output = output;
            this.bqueue = q;
            eventQueueList.put(queueId, bqueue);
        }

        @Override
        public void run() {
            try {
                ObjectNode chunk;
                while (true) {
                    chunk = bqueue.take();
                    output.write(chunk.toString().concat("\r\n"));
                }
            } catch (IOException e) {
                log.info("chunkedOuput is closed: {}", this.bqueue.toString());
                /*
                 * Remove queue from the queue list, so that the event producer
                 * (i.e., listener) would stop working.
                 */
                eventQueueList.remove(this.queueId);
            } catch (Exception e) {
                log.error("ERROR: EventConsumer: ", e);
            } finally {
                try {
                    output.close();
                    log.info("EventConsumer thread terminated: {}", queueId);
                } catch (IOException e) {
                    log.error("ERROR: EventConsumer: ", e);
                }
            }
        }

    }

    /**
     * The listener class acts as the event producer for the event queues. The
     * queues are created by the event consumer threads and are removed when the
     * threads terminate.
     */
    private class InternalYangNotificationListener implements YangNotificationListener {

        @Override
        public void event(YangNotificationEvent event) {
            if (event.type() != YangNotificationEvent.Type.YANG_NOTIFICATION) {
                // For now, we only handle YANG notification events.
                return;
            }

            if (eventQueueList.isEmpty()) {
                /*
                 * There is no consumer waiting to consume, so don't have to
                 * produce this event.
                 */
                return;
            }

            try {
                /*
                 * Put the event to every queue out there. Each queue is
                 * corresponding to an event stream session. The queue is
                 * removed when the session terminates.
                 */
                for (Entry<String, BlockingQueue<ObjectNode>> entry : eventQueueList
                        .entrySet()) {
                    entry.getValue().put(event.subject().getData());
                }
            } catch (InterruptedException e) {
                Log.error("ERROR", e);
                throw new RestconfException("queue", Status.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
