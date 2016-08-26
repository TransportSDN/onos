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

package org.onosproject.restconf.rpp;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ChunkedOutput;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.restconf.api.RestconfException;
import org.onosproject.restconf.api.RestconfService;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Main implementation of the RESTCONF Protocol Proxy module. It currently only
 * handles basic operations on data resource nodes. However, the structure of the code
 * allows new methods/functionality to be easily added.
 */
@Path("/")
public class RestconfWebResource extends AbstractWebResource {

    @Context
    UriInfo uriInfo;

    private final RestconfService service = get(RestconfService.class);
    private final Logger log = getLogger(getClass());

    /**
     * Handle the RESTCONF GET Operation against a data resource.
     *
     * @param uriString URI of the data resource.
     * @return "200 OK" on success.
     * "400 Bad Request" on error.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("data/{identifier : .+}")
    public Response handleGetRequest(@PathParam("identifier") String uriString) {
        URI uri = uriInfo.getRequestUri();

        log.info("handleGetRequest: {}", uriString);

        try {
            ObjectNode node = service.doGetOperation(uri);
            return ok(node).build();
        } catch (RestconfException e) {
            log.error("ERROR: handleGetRequest: {}", e.getMessage(), e);
            return e.getResponse();
        }
    }

    /**
     * Handle the RESTCONF Event Notification Subscription request.
     *
     * @param streamId Event stream ID
     * @return A string data stream over HTTP keep-alive session.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("streams/{streamId}")
    public ChunkedOutput<String> handleNotificationRegistration(@PathParam("streamId") String streamId) {
        final ChunkedOutput<String> output = new ChunkedOutput<String>(String.class);
        try {
            service.subscribeEventStream(streamId, output);
        } catch (RestconfException e) {
            log.error("ERROR: handleNotificationRegistration: {}", e.getMessage(), e);
            try {
                output.close();
            } catch (IOException ex) {
                log.error("ERROR: handleNotificationRegistration:", ex);
            }
        }

        return output;
    }

    /**
     * Handle the RESTCONF POST Operation against a data resource.
     *
     * @param uriString URI of the data resource.
     * @param stream    Input JSON object
     * @return "201 Created" on success and there is no response message-body.
     * "409 Conflict" if the data resource already exists.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("data/{identifier : .+}")
    public Response handlePostRequest(@PathParam("identifier") String uriString, InputStream stream) {
        URI uri = uriInfo.getRequestUri();

        log.info("handlePostRequest: {}", uriString);

        try {
            ObjectNode rootNode = (ObjectNode) mapper().readTree(stream);

            service.doPostOperation(uri, rootNode);
            return Response.created(uriInfo.getRequestUri()).build();
        } catch (JsonProcessingException e) {
            log.error("ERROR: handlePostRequest ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (RestconfException e) {
            log.error("ERROR: handlePostRequest: {}", e.getMessage(), e);
            return e.getResponse();
        } catch (IOException ex) {
            log.error("ERROR: handlePostRequest ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handle the RESTCONF PUT Operation against a data resource.
     *
     * @param uriString URI of the data resource.
     * @param stream    Input JSON object
     * @return "201 Created" if a new resource is created
     * "204 No Content" if an existing resource is modified.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("data/{identifier : .+}")
    public Response handlePutRequest(@PathParam("identifier") String uriString, InputStream stream) {
        URI uri = uriInfo.getRequestUri();

        log.info("handlePutRequest: {}", uriString);

        try {
            ObjectNode rootNode = (ObjectNode) mapper().readTree(stream);

            service.doPutOperation(uri, rootNode);
            return Response.created(uriInfo.getRequestUri()).build();
        } catch (JsonProcessingException e) {
            log.error("ERROR: handlePutRequest ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (RestconfException e) {
            log.error("ERROR: handlePutRequest: {}", e.getMessage(), e);
            return e.getResponse();
        } catch (IOException ex) {
            log.error("ERROR: handlePutRequest ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handle the RESTCONF DELETION Operation against a data resource.
     *
     * @param uriString URI of the data resource to be deleted.
     * @return "204 No Content" on success
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("data/{identifier : .+}")
    public Response handleDeleteRequest(@PathParam("identifier") String uriString) {
        URI uri = uriInfo.getRequestUri();

        log.info("handleDeleteRequest: {}", uriString);

        try {
            service.doDeleteOperation(uri);
            return Response.ok().build();
        } catch (RestconfException e) {
            log.error("ERROR: handleDeleteRequest: {}", e.getMessage(), e);
            return e.getResponse();
        }
    }

}
