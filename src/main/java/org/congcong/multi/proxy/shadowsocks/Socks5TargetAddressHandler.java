package org.congcong.multi.proxy.shadowsocks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import lombok.extern.slf4j.Slf4j;
import org.congcong.multi.proxy.common.DnsMappingUtil;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.config.ConfigProvider;
import org.congcong.multi.proxy.config.ServiceConfig;
import org.congcong.multi.proxy.entity.Pair;
import org.congcong.multi.proxy.entity.SocketMessage;

import java.util.List;
import java.util.Objects;

@Slf4j
public class Socks5TargetAddressHandler extends ByteToMessageDecoder {

    //[1-byte type][variable-length host][2-byte port]
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 检查可读字节数是否足够至少包含1字节类型和2字节端口
        if (byteBuf.readableBytes() < 3) {
            return;  // 等待更多数据
        }

        byteBuf.markReaderIndex();  // 标记当前位置以便在数据不足时恢复

        byte type = byteBuf.readByte();
        SocketMessage socketMessage = new SocketMessage();
        Socks5AddressType socks5AddressType = Socks5AddressType.valueOf(type);
        socketMessage.setType(socks5AddressType);

        String remoteIp;
        if (socks5AddressType == Socks5AddressType.IPv4) {
            if (byteBuf.readableBytes() < 4) {
                byteBuf.resetReaderIndex();  // 恢复读取位置
                return;  // 等待更多数据
            }
            byte[] hostBytes = new byte[4];
            byteBuf.readBytes(hostBytes);
            remoteIp = (hostBytes[0] & 0xFF) + "." + (hostBytes[1] & 0xFF) + "." +
                    (hostBytes[2] & 0xFF) + "." + (hostBytes[3] & 0xFF);
        } else if (socks5AddressType == Socks5AddressType.DOMAIN) {
            if (byteBuf.readableBytes() < 1) {
                byteBuf.resetReaderIndex();
                return;  // 等待更多数据
            }
            int domainLength = byteBuf.readByte();
            if (byteBuf.readableBytes() < domainLength) {
                byteBuf.resetReaderIndex();
                return;  // 等待更多数据
            }
            byte[] hostBytes = new byte[domainLength];
            byteBuf.readBytes(hostBytes);
            remoteIp = new String(hostBytes);
        } else if (socks5AddressType == Socks5AddressType.IPv6) {
            if (byteBuf.readableBytes() < 16) {
                byteBuf.resetReaderIndex();
                return;  // 等待更多数据
            }
            byte[] hostBytes = new byte[16];
            byteBuf.readBytes(hostBytes);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i += 2) {
                sb.append(String.format("%02x%02x", hostBytes[i], hostBytes[i + 1]));
                if (i < 14) sb.append(":");
            }
            remoteIp = sb.toString();
        } else {
            throw new IllegalArgumentException("Unknown address type: " + type);
        }

        if (byteBuf.readableBytes() < 2) {
            byteBuf.resetReaderIndex();  // 等待端口数据
            return;
        }

        // 解析端口
        socketMessage.setRemoteIp(remoteIp);
        socketMessage.setPort(byteBuf.readUnsignedShort());
        socketMessage.setKeepalive(true);
        // 拷贝剩下的负载数据
        int readableBytes = byteBuf.readableBytes();
        if (readableBytes != 0) {
            byte[] byteArray = new byte[readableBytes];  // 创建字节数组
            byteBuf.readBytes(byteArray);  // 将 byteBuf 数据拷贝到字节数组中
            socketMessage.setFirstRequest(byteArray);
            log.debug("first request ready");
        } else {
            log.debug("wait for first request");
            byteBuf.resetReaderIndex();
            return;
        }
        //解析dns
        Pair<String, Integer> mapping = DnsMappingUtil.mapping(socketMessage.getRemoteIp(), socketMessage.getPort());

        socketMessage.setRemoteIp(mapping.getFst());
        socketMessage.setPort(mapping.getSnd());


        // 将消息传递到下一个处理器
        ctx.pipeline().addLast(SingletonUtil.getInstance(ShadowSocksServerConnectHandler.class));
        ctx.channel().pipeline().remove(this);
        // 代理请求到本地管理端
        ctx.fireChannelRead(socketMessage);
    }




}
