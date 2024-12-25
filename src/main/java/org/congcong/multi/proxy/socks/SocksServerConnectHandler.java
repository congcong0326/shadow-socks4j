package org.congcong.multi.proxy.socks;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import org.congcong.multi.proxy.common.AbstractTcpTunnelConnectorHandler;
import org.congcong.multi.proxy.entity.Message;
import org.congcong.multi.proxy.entity.SocketMessage;

public class SocksServerConnectHandler extends AbstractTcpTunnelConnectorHandler<SocketMessage> {

    @Override
    protected Object getConnectSuccessMessage(Message message) {
        SocketMessage socketMessage = (SocketMessage) message;
        return new DefaultSocks5CommandResponse(
                Socks5CommandStatus.SUCCESS,
                socketMessage.getType(),
                socketMessage.getRemoteIp(),
                ((SocketMessage) message).getPort());
    }

    @Override
    protected Object getConnectFailedMessage(Message message) {
        return new DefaultSocks5CommandResponse(
                Socks5CommandStatus.FAILURE, ((SocketMessage) message).getType());
    }

    @Override
    protected String getType() {
        return "socks tunnel";
    }

    @Override
    protected ChannelHandler getRemoveClass() {
        return this;
    }
}
