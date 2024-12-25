package org.congcong.multi.proxy.monitor.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.congcong.multi.proxy.common.Const;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.config.ServiceConfig;
import org.congcong.multi.proxy.entity.Context;

@Slf4j
public class ContextInitHandler extends ChannelInboundHandlerAdapter  {

    private final ServiceConfig.Service serviceConfig;

    public ContextInitHandler(ServiceConfig.Service serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Context context = new Context();
        if (serviceConfig.getCredentials() != null) {
            for (ServiceConfig.Credentials credential : serviceConfig.getCredentials()) {
                context.setUserName(credential.getUsername());
            }
        }
        if (context.getUserName() == null &&context.getUserName().isEmpty()) {
            context.setUserName("Anonymous user");
        }
        ctx.channel().attr(Const.CONTEXT).set(context);
        ctx.channel().pipeline().addFirst(SingletonUtil.getInstance(DownTrafficHandler.class));
        ctx.channel().pipeline().addLast(SingletonUtil.getInstance(UpTrafficHandler.class));
        ctx.channel().pipeline().remove(this);
    }

}
