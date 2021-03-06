/*
The MIT License (MIT)

Copyright (c) 2016 CSC - IT Center for Science

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package fi.csc.kapaVirtaAS;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtaClient {

    private static final ASConfiguration conf = new ASConfiguration();
    private static final Logger log = LoggerFactory.getLogger(VirtaClient.class);
    private PoolingHttpClientConnectionManager cm;
    private HttpClient client;

    public VirtaClient(ASConfiguration conf) {
        cm = new PoolingHttpClientConnectionManager();
        Integer poolSize = new Integer(conf.getAdapterServiceConnectionPoolSize());
        cm.setMaxTotal(poolSize != null ? poolSize.intValue() : 10);
        client = HttpClients.custom().setConnectionManager(cm).build();
    }

    public HttpResponse getVirtaWS(String virtaRequestMessage, String authString) throws Exception {
        HttpPost post = new HttpPost(conf.getVirtaSOAPURL());
        post.setHeader("Content-Type","text/xml;charset=utf-8");
        post.setHeader("X-Forwarded-For", authString);
        HttpEntity entity = new ByteArrayEntity(virtaRequestMessage.getBytes());
        post.setEntity(entity);
        return client.execute(post);
    }
}
