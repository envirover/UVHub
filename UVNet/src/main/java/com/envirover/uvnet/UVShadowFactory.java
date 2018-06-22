package com.envirover.uvnet;

public class UVShadowFactory {
	
    private static UVShadow instance = null;

    public static UVShadow getUVShadow() {
        if(instance == null) {
           instance = new InMemoryUVShadow();
        }

        return instance;
    }
}
