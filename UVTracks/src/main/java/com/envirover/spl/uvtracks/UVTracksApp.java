package com.envirover.spl.uvtracks;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.envirover.spl.providers.DefaultExceptionMapper;
import com.envirover.spl.rest.UVTracksResource;

public class UVTracksApp extends Application {

    public UVTracksApp() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(UVTracksResource.class);
        s.add(DefaultExceptionMapper.class);
        return s;
    }

}