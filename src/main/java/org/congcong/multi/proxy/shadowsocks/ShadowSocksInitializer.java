package org.congcong.multi.proxy.shadowsocks;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.congcong.multi.proxy.config.ServiceConfig;
import org.congcong.multi.proxy.encryption.CryptoProcessorFactory;
import org.congcong.multi.proxy.monitor.handler.ContextInitHandler;
import org.congcong.multi.proxy.monitor.handler.TcpConnectionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShadowSocksInitializer extends ChannelInitializer<SocketChannel>  {


    private final ServiceConfig.Service service;

    public ShadowSocksInitializer(ServiceConfig.Service service) {
        this.service = service;
    }



    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        if (service.isSupportDecryption()) {
            socketChannel.pipeline().addLast(
                    TcpConnectionHandler.INSTANCE,
                    new ContextInitHandler(service),
                    // 解密数据
                    new DecryptedSocksHandler(CryptoProcessorFactory.createProcessor(service.getDecryptionAlgorithm(), service.getDecryptionKey())),
                    new LoggingHandler(LogLevel.DEBUG),
                    new Socks5TargetAddressHandler(),
                    // 加密数据
                    new EncryptedSocksHandler(CryptoProcessorFactory.createProcessor(service.getDecryptionAlgorithm(), service.getDecryptionKey())));
        } else {
            socketChannel.pipeline().addLast(
                    TcpConnectionHandler.INSTANCE,
                    new LoggingHandler(LogLevel.DEBUG),
                    new Socks5TargetAddressHandler());
        }

    }
}
