package com.envirover.spl.uvtracks;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.envirover.spl.providers.DefaultExceptionMapper;
import com.envirover.spl.providers.JacksonContextResolver;
import com.envirover.spl.rest.UVTracksResourceV3;

public class UVTracksApp extends Application {

    public UVTracksApp() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(UVTracksResourceV3.class);
        s.add(DefaultExceptionMapper.class);
        s.add(JacksonContextResolver.class);
        return s;
    }

}