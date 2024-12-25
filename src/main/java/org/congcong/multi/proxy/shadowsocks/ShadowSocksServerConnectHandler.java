package org.congcong.multi.proxy.shadowsocks;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import org.congcong.multi.proxy.common.AbstractTcpTunnelConnectorHandler;
import org.congcong.multi.proxy.entity.Message;
import org.congcong.multi.proxy.entity.SocketMessage;

public class ShadowSocksServerConnectHandler extends AbstractTcpTunnelConnectorHandler<SocketMessage>  {


    @Override
    protected Object getConnectSuccessMessage(Message message) {
        return null;
    }

    @Override
    protected Object getConnectFailedMessage(Message message) {
        return Unpooled.EMPTY_BUFFER;
    }

    @Override
    protected String getType() {
        return "ShadowSocks";
    }

    @Override
    protected ChannelHandler getRemoveClass() {
        return this;
    }
}
