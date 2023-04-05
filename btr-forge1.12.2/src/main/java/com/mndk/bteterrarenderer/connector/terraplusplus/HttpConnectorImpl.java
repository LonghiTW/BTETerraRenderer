package com.mndk.bteterrarenderer.connector.terraplusplus;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import io.netty.buffer.ByteBufInputStream;
import net.buildtheearth.terraplusplus.util.http.Http;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@ConnectorImpl
public class HttpConnectorImpl implements HttpConnector {
    public InputStream download(String url) throws ExecutionException, InterruptedException {
        return new ByteBufInputStream(Http.get(url).get());
    }
}