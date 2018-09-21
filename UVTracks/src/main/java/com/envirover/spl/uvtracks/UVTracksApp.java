package com.envirover.spl.uvtracks;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

//import com.envirover.spl.filters.CORSFilter;
import com.envirover.spl.rest.UVTracks;

public class UVTracksApp extends Application {

	public UVTracksApp() {
		// packages("com.envirover.spl.rest", "com.envirover.spl.filters");
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(UVTracks.class);
		return s;
	}

}