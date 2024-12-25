package org.congcong.multi.proxy.monitor.handler;

import io.netty.channel.ChannelHandlerContext;
import org.congcong.multi.proxy.common.Const;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.entity.Context;
import org.congcong.multi.proxy.monitor.traffic.ITrafficReport;
import org.congcong.multi.proxy.monitor.traffic.TrafficReportService;

public class MonitorHandler {

    private static final ITrafficReport trafficReport = SingletonUtil.getInstance(TrafficReportService.class);

    public static void closeAndFlush(ChannelHandlerContext ctx) throws Exception {
        Context context = ctx.channel().attr(Const.CONTEXT).get();
        if (context != null) {
            context.setDurationTime((int) (System.currentTimeMillis() - context.getStartTime()));
            trafficReport.report(context);
        }
    }




}
