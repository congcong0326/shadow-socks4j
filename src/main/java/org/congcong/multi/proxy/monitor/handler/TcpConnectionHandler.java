package org.congcong.multi.proxy.monitor.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.common.SocketUtils;
import org.congcong.multi.proxy.monitor.connect.ConnectionMonitor;
import org.congcong.multi.proxy.monitor.connect.IConnectionMonitor;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ChannelHandler.Sharable
public class TcpConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final IConnectionMonitor connectionMonitor = SingletonUtil.getInstance(ConnectionMonitor.class);


    public static TcpConnectionHandler INSTANCE = new TcpConnectionHandler();

    private TcpConnectionHandler() {

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 当连接建立时增加计数
        connectionMonitor.tcpConnectIncrement();
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 当连接断开时减少计数
        connectionMonitor.tcpConnectDecrement();
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        SocketUtils.close(ctx.channel());
        ctx.fireExceptionCaught(cause);
    }

}
