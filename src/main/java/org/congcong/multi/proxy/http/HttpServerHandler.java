package org.congcong.multi.proxy.http;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.congcong.multi.proxy.Exception.HostAbsentException;
import org.congcong.multi.proxy.common.HttpUtils;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.common.SocketUtils;
import org.congcong.multi.proxy.entity.HttpMessage;
import org.congcong.multi.proxy.entity.Pair;

import java.net.URISyntaxException;

@Slf4j
@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

//    public static final HttpServerHandler INSTANCE = new HttpServerHandler();
//
//    private HttpServerHandler() {
//
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        if (HttpUtils.isConnectMethod(httpRequest)) {
            // web tunnel proxy
            try {
                Pair<String, Integer> hostIpAndPort = HttpUtils.getHostIpAndPort(httpRequest);
                HttpMessage httpMessage = new HttpMessage();
                httpMessage.setRemoteIp(hostIpAndPort.getFst());
                httpMessage.setPort(hostIpAndPort.getSnd());
                httpMessage.setKeepalive(HttpUtils.isKeepalive(httpRequest));
                // 先添加再移除，httpMessage才可以传递给WebTunnelConnectorHandler处理
                // 添加web隧道的处理器
                ctx.pipeline().addLast(SingletonUtil.getInstance(WebTunnelConnectorHandler.class));
                // 后续需要盲转数据了，不用去解析http协议
                ctx.pipeline().remove(this);
                ctx.pipeline().remove(HttpRequestDecoder.class);
                ctx.pipeline().remove(HttpObjectAggregator.class);
                ctx.fireChannelRead(httpMessage);
            } catch (HostAbsentException e) {
                log.error("host absent {}", httpRequest.headers());
                SocketUtils.close(ctx.channel());
            }
        } else {
            // http proxy http透明代理
            try {
                Pair<String, Integer> uriAndPort = HttpUtils.getUriAndPort(httpRequest);
                HttpMessage httpMessage = new HttpMessage();
                httpMessage.setRemoteIp(uriAndPort.getFst());
                httpMessage.setPort(uriAndPort.getSnd());
                // netty自动管理对象的引用计数，如果这里不retain，传递给下一个handler处理前，可能会被回收
                httpRequest.retain();
                httpMessage.setFirstRequest(httpRequest);
                httpMessage.setKeepalive(HttpUtils.isKeepalive(httpRequest));
                //添加透明代理的处理器
                ctx.pipeline().addLast(SingletonUtil.getInstance(HttpProxyConnectorHandler.class));
                // 后续需要盲转数据了，不用去解析http协议
                ctx.pipeline().remove(this);
                ctx.pipeline().remove(HttpRequestDecoder.class);
                ctx.pipeline().remove(HttpObjectAggregator.class);
                ctx.fireChannelRead(httpMessage);
            } catch (URISyntaxException uriSyntaxException) {
                log.error("uriSyntaxException {}", httpRequest.uri());
                SocketUtils.close(ctx.channel());
            }
        }
    }


}
