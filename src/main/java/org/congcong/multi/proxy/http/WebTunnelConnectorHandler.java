package org.congcong.multi.proxy.http;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.congcong.multi.proxy.common.AbstractTcpTunnelConnectorHandler;
import org.congcong.multi.proxy.common.HttpUtils;
import org.congcong.multi.proxy.entity.HttpMessage;
import org.congcong.multi.proxy.entity.Message;

@Slf4j
@ChannelHandler.Sharable
public class WebTunnelConnectorHandler extends AbstractTcpTunnelConnectorHandler<HttpMessage> {



//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, HttpMessage httpMessage) throws Exception {
//        // 设置连接server成功后的回调
//        Promise<Channel> promise = ctx.executor().newPromise();
//        // 如果连接目标服务器成功，需要返回connect establish 给客户端
//        promise.addListener(future -> {
//            Channel outboundChannel = (Channel) future.getNow();
//            if (future.isSuccess()) {
//                ctx.channel().writeAndFlush(HttpUtils.CONNECT_ESTABLISHED_BUF())
//                        .addListener(new ChannelFutureListener() {
//                            @Override
//                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                                // 防止浏览器在此时机已经主动断开连接
//                                if (ctx.channel().isActive()) {
//                                    ctx.pipeline().addLast(new RelayHandler(outboundChannel));
//                                    outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
//                                    // 隧道建立成功后，移除不需要的handler，添加中继作用的handler
//                                    ctx.pipeline().remove(WebTunnelConnectorHandler.class);
//                                } else {
//                                    SocketUtils.close(outboundChannel);
//                                    log.error("client close the channel, we should close remote channel {}", httpMessage.getHostname());
//                                }
//                            }
//                        });
//                log.info("web tunnel connect {} success", httpMessage.getHostname());
//            } else {
//                log.error("web tunnel connect {} failed, send bad gate way to client then close channel, reason {}", httpMessage, future.cause().getMessage());
//                ctx.writeAndFlush(HttpUtils.BAD_GATEWAY_BUF(httpMessage.getHostname()));
//                SocketUtils.close(ctx.channel());
//            }
//        });
//
//        // do connect remote
//        Channel inboundChannel = ctx.channel();
//        Bootstrap b = new Bootstrap();
//        // 直接使用处理client的线程组去连接目标服务器
//        b.group(inboundChannel.eventLoop())
//                .channel(NioSocketChannel.class)
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
//                .option(ChannelOption.SO_KEEPALIVE, httpMessage.isKeepalive())
//                .handler(new DirectClientHandler(promise));
//        b.connect(httpMessage.getHostname(), httpMessage.getPort())
//                .addListener(new ChannelFutureListener() {
//                    @Override
//                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                        if (!channelFuture.isSuccess()) {
//                            ctx.writeAndFlush(HttpUtils.BAD_GATEWAY_BUF(httpMessage.getHostname()));
//                            SocketUtils.close(ctx.channel());
//                        }
//                    }
//                });
//    }

    @Override
    protected Object getConnectSuccessMessage(Message message) {
        return HttpUtils.CONNECT_ESTABLISHED_BUF();
    }

    @Override
    protected Object getConnectFailedMessage(Message message) {
        return HttpUtils.BAD_GATEWAY_BUF();
    }

    @Override
    protected String getType() {
        return "web tunnel";
    }

    @Override
    protected ChannelHandler getRemoveClass() {
        return this;
    }


}
