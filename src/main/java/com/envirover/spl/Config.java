package com.envirover.spl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private Properties props = new Properties();

    // Configuration properties
    private final String PROP_HTTP_PORT = "http.port";

    // default property values
    private final Integer DEFAULT_HTTP_PORT = 8000;

    public void init() throws IOException {
        init("app.properties");
    }

    public void init(String propertiesFile) throws IOException {
        Properties props = new Properties();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        InputStream in = null;
        try {
            in = loader.getResourceAsStream(propertiesFile);
            props.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public Integer getHttpPort() {
        String str = props.getProperty(PROP_HTTP_PORT);

        if (str == null)
            return DEFAULT_HTTP_PORT;

        return Integer.valueOf(str);
    }

}
