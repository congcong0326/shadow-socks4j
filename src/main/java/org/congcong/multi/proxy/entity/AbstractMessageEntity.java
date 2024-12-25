package org.congcong.multi.proxy.entity;

import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

@Data
public class AbstractMessageEntity implements Message {

    private String remoteIp;

    private int port;

    private Object firstRequest;

    private boolean keepalive;


    @Override
    public String getRemoteIp() {
        return remoteIp;
    }

    @Override
    public int getRemotePort() {
        return port;
    }

    @Override
    public Object firstRequest() {
        return firstRequest;
    }

    @Override
    public boolean keepalive() {
        return keepalive;
    }
}
