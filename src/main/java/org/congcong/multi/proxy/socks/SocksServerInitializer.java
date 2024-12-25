package org.congcong.multi.proxy.socks;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.monitor.handler.TcpConnectionHandler;

public class SocksServerInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                TcpConnectionHandler.INSTANCE,
                new LoggingHandler(LogLevel.DEBUG),
                Socks5ServerEncoder.DEFAULT,//响应的编码
                new Socks5InitialRequestDecoder(),//请求的解码器
                SingletonUtil.getInstance(SocksServerHandler.class)
        );
    }
}
