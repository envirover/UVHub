package com.envirover.spl.providers;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * @author secret
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {
    private ObjectMapper objectMapper;

    public JacksonContextResolver() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.objectMapper
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType) {
        return objectMapper;
    }

}