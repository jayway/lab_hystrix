package com.jayway.hystrixlab.http;

import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * A Jersey exception mapper that maps most exceptions (with a few exceptions) to internal error.
 */
@Provider
public class GlobalExceptionMapper implements ExtendedExceptionMapper<Throwable> {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public boolean isMappable(Throwable exception) {
        return !exception.getClass().getName().startsWith("javax.ws.rs");
    }

    @Override
    public Response toResponse(Throwable exception) {
        log.error("Error", exception);
        return Response.status(INTERNAL_SERVER_ERROR).header("Content-Type", TEXT_PLAIN).build();
    }
}
