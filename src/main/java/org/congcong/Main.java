package org.congcong;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.congcong.multi.proxy.common.ProxyServer;
import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.config.ConfigProvider;
import org.congcong.multi.proxy.config.ServiceConfig;
import org.congcong.multi.proxy.config.ServiceType;
import org.congcong.multi.proxy.http.HttpServerInitializer;
import org.congcong.multi.proxy.monitor.serverResource.SystemMonitor;
import org.congcong.multi.proxy.monitor.web.MonitorAdminWebInitializer;
import org.congcong.multi.proxy.shadowsocks.ShadowSocksInitializer;
import org.congcong.multi.proxy.socks.SocksServerInitializer;

import java.util.List;
@Slf4j
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //触发下SystemMonitor类的加载，初始化下程序启动时间
        SingletonUtil.getInstance(SystemMonitor.class);
        // 读取配置启动代理
        ServiceConfig config = ConfigProvider.getConfig();
        List<ServiceConfig.Service> services = config.getServices();
        for (ServiceConfig.Service service : services) {
            if (!service.isEnable()) {
                log.info("not load service {}", service.getName());
                continue;
            }
            log.info("load service {}", service.getName());
            new Thread(() -> {
                ProxyServer proxyServer = new ProxyServer() {
                    @Override
                    public int getPort() {
                        return service.getPort();
                    }

                    @Override
                    public String getIp() {
                        //不要把admin界面暴露出去，通过ShadowSocks来转发到本机
                        if (service.getType() == ServiceType.ADMIN) {
                            return "127.0.0.1";
                        }
                        return "0.0.0.0";
                    }

                    @Override
                    public String getServerName() {
                        return service.getName();
                    }

                    @Override
                    public ChannelInitializer<SocketChannel> getChildHandler() {
                        switch (service.getType()) {
                            case HTTP:
                                return new HttpServerInitializer();
                            case SOCKS:
                                return new SocksServerInitializer();
                            case SHADOW_SOCKS:
                                return new ShadowSocksInitializer(service);
                            case ADMIN:
                                return new MonitorAdminWebInitializer();
                            default:
                                log.error("not support server type {}", service.getType());
                                System.exit(0);
                        }
                        return null;
                    }
                };
                try {
                    proxyServer.start();
                } catch (InterruptedException ignored) {

                }
            }).start();
        }
    }
}