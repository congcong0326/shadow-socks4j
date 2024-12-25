package org.congcong.multi.proxy.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.monitor.handler.TcpConnectionHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel>  {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                TcpConnectionHandler.INSTANCE,
                new LoggingHandler(LogLevel.DEBUG),
                new HttpRequestDecoder(),
                // 需要聚合下，如果一次没有解析出完整的http请求，容易导致后续流程报错
                new HttpObjectAggregator(1048576),
                SingletonUtil.getInstance(HttpServerHandler.class)
        );
    }
}
