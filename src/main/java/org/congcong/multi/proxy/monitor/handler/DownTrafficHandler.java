package org.congcong.multi.proxy.monitor.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.congcong.multi.proxy.common.SocketUtils;
import org.congcong.multi.proxy.monitor.UserTrafficType;

@ChannelHandler.Sharable
public class DownTrafficHandler extends ChannelInboundHandlerAdapter {


    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            // 如果接收到的数据是 ByteBuf 类型，则累加它的可读字节长度
            ByteBuf byteBuf = (ByteBuf) msg;
            int readableBytes = byteBuf.readableBytes();
            TrafficHandlerUtil.handleTraffic(ctx, readableBytes, UserTrafficType.UP);
        }
        ctx.fireChannelRead(msg);
    }

//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
//            throws Exception {
//        SocketUtils.close(ctx.channel());
//        //ctx.fireExceptionCaught(cause);
//    }
}
