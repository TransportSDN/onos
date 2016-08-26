package org.onosproject.restconf.api;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


/**
 * Exceptions raised during RESTCONF operations. This class extends
 * WebApplicationException. The design intention is to create a place holder
 * for RESTCONF specific errors and to be able to add more functions as the
 * subsystem grows.
 */
public class RestconfException extends WebApplicationException {

    // This is a randomly generated value. A WebApplicationException class is required to define it.
    private static final long serialVersionUID = 3275970397584007046L;

    /**
     * Construct a new RESTCONF server error exception.
     *
     * @param message the detailed message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param status  HTTP error status.
     * @throws IllegalArgumentException in case the status code is {@code null} or is not from
     *                                  {@link javax.ws.rs.core.Response.Status.Family#CLIENT_ERROR} status code
     *                                  family.
     */
    public RestconfException(String message, Response.Status status) {
        super(message, null, Response.status(status).build());
    }

    /**
     * Construct a new RESTCONF server error exception.
     *
     * @param status HTTP error status.
     * @throws IllegalArgumentException in case the status code is not a valid HTTP status code or
     *                                  if it is not from the
     *                                  {@link javax.ws.rs.core.Response.Status.Family#CLIENT_ERROR}
     *                                  status code family.
     */
    public RestconfException(int status) {
        super((Throwable) null, Response.status(status).build());
    }
}
