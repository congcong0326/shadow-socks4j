package org.congcong.multi.proxy.entity;


import lombok.Data;

public interface Message {

//    private String hostname;
//
//    private int port;
//
//    private Object firstRequest;
//
//    private boolean keepalive;

    String getRemoteIp();

    int getRemotePort();

    Object firstRequest();

    boolean keepalive();

}
