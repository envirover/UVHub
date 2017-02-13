package com.envirover.spl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

public class Mk2MAVLinkTest {

    //Test receiving messages from your RockBLOCK
    @Test
    public void testPostRockBLOCKMessage() throws URISyntaxException, ClientProtocolException, IOException {
        final Config config = new Config();
        config.init();

        HttpClient httpclient = HttpClients.createDefault();

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http");
        builder.setHost("127.0.0.1");
        builder.setPort(config.getHttpPort());
        builder.setPath("/test");

        URI uri = builder.build();
        System.out.println(uri.toString());
        HttpPost httppost = new HttpPost(uri);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("imei", "300234010753370"));
        params.add(new BasicNameValuePair("momsn", "12345"));
        params.add(new BasicNameValuePair("transmit_time", "12-10-10 10:41:50"));
        params.add(new BasicNameValuePair("iridium_latitude", "52.3867"));
        params.add(new BasicNameValuePair("iridium_longitude", "0.2938"));
        params.add(new BasicNameValuePair("iridium_cep", "9"));
        params.add(new BasicNameValuePair("data", "48656c6c6f20576f726c6420526f636b424c4f434b"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        // Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream responseStream = entity.getContent();
            try {
                String responseString = IOUtils.toString(responseStream);
                System.out.println(responseString);
            } finally {
                responseStream.close();
            }
        }
    }

}
