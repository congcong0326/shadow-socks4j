package org.congcong.multi.proxy.monitor.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.congcong.multi.proxy.monitor.UserTrafficType;

@ChannelHandler.Sharable
public class UpTrafficHandler extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            // 如果是 ByteBuf 类型的数据，则累加它的可读字节长度
            ByteBuf buf = (ByteBuf) msg;
            int readableBytes = buf.readableBytes();
            TrafficHandlerUtil.handleTraffic(ctx, readableBytes, UserTrafficType.DOWN);
        }
        ctx.write(msg, promise);
    }
}
