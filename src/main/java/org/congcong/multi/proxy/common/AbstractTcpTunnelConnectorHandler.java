package org.congcong.multi.proxy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.congcong.multi.proxy.entity.Context;
import org.congcong.multi.proxy.entity.Message;
import org.congcong.multi.proxy.monitor.connect.ConnectionMonitor;
import org.congcong.multi.proxy.monitor.connect.IConnectionMonitor;
import org.congcong.multi.proxy.monitor.handler.MonitorHandler;

import java.net.InetSocketAddress;

@Slf4j
@ChannelHandler.Sharable
public abstract class AbstractTcpTunnelConnectorHandler<T extends Message> extends SimpleChannelInboundHandler<Message> {
    //连接成功后给客户端返回的消息
    protected abstract Object getConnectSuccessMessage(Message message);
    //连接失败后给客户端返回的消息
    protected abstract Object getConnectFailedMessage(Message message);

    protected abstract String getType();

    protected abstract ChannelHandler getRemoveClass();


    private static final IConnectionMonitor connectionMonitor = SingletonUtil.getInstance(ConnectionMonitor.class);


    private void setRelay(Channel inboundChannel, Channel outboundChannel, Message message) {
        if (inboundChannel.isActive()) {
            inboundChannel.pipeline().addLast(new RelayHandler(outboundChannel));
            outboundChannel.pipeline().addLast(new RelayHandler(inboundChannel));
            // 隧道建立成功后，移除不需要的handler，添加中继作用的handler
            inboundChannel.pipeline().remove(getRemoveClass());
            if (message.firstRequest() != null) {
                log.debug("send first request");
                // 判断 firstRequest 的类型，并进行相应的处理
                Object firstRequest = message.firstRequest();
                if (firstRequest instanceof ByteBuf) {
                    // 如果 firstRequest 是 ByteBuf，直接写入到 outboundChannel
                    outboundChannel.writeAndFlush(firstRequest);
                } else if (firstRequest instanceof byte[]) {
                    // 如果 firstRequest 是字节数组，先将其转换为 ByteBuf，然后写入
                    byte[] byteArray = (byte[]) firstRequest;
                    // 使用池化的 ByteBuf 分配内存并写入字节数组
                    ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(byteArray.length);
                    byteBuf.writeBytes(byteArray);
                    outboundChannel.writeAndFlush(byteBuf);
                } else {
                    // 处理其他类型的 firstRequest（如果有的话）
                    log.warn("Unsupported type for firstRequest: {}", firstRequest.getClass().getName());
                }
            }
        } else {
            SocketUtils.close(outboundChannel);
            log.error("client close the channel, we should close remote channel {}", message.getRemoteIp());
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        // 设置连接server成功后的回调
        Promise<Channel> promise = ctx.executor().newPromise();
        // 如果连接目标服务器成功，需要返回connect establish 给客户端
        promise.addListener(future -> {
            Channel outboundChannel = (Channel) future.getNow();
            if (future.isSuccess()) {
                // socks5协议与http proxy需要在隧道建立成功时返回一个数据
                if (getConnectSuccessMessage(message) != null) {
                    ctx.channel().writeAndFlush(getConnectSuccessMessage(message))
                            .addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                    setRelay(ctx.channel(), outboundChannel, message);
                                }
                            });
                }
                // shadow sock协议在隧道建立成功时不需要返回数据
                else {
                    setRelay(ctx.channel(), outboundChannel, message);
                }
                log.info("{} tunnel connect {} success", getType(), message.getRemoteIp());
            } else {
                log.error("{} tunnel connect {} failed, send bad gate way to client then close channel, reason {}",getType(), message, future.cause().getMessage());
                ctx.writeAndFlush(getConnectFailedMessage(message));
                SocketUtils.close(ctx.channel());
            }
        });

        // do connect remote
        Channel inboundChannel = ctx.channel();
        Bootstrap b = new Bootstrap();
        // 直接使用处理client的线程组去连接目标服务器
        b.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, message.keepalive())
                .handler(new DirectClientHandler(promise));
        enrichContext(ctx, message.getRemoteIp());
        b.connect(message.getRemoteIp(), message.getRemotePort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (!channelFuture.isSuccess()) {
                            log.error("{} tunnel connect {} failed", getType(), message.getRemoteIp());
                            connectionMonitor.connectRemoteFailedIncrement();
                            ctx.writeAndFlush(getConnectFailedMessage(message));
                            SocketUtils.close(ctx.channel());
                        }
                    }
                });
        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                MonitorHandler.closeAndFlush(ctx);
            }
        });
    }

    private void enrichContext(ChannelHandlerContext ctx, String remoteIp) {
        Context context = ctx.channel().attr(Const.CONTEXT).get();
        if (context != null) {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIp = remoteAddress.getAddress().getHostAddress(); // 获取客户端IP
            context.setRemoteUrl(remoteIp);
            context.setClientIp(clientIp);
        }
    }


}
