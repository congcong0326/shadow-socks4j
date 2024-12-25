package org.congcong.multi.proxy.common;

import lombok.extern.slf4j.Slf4j;
import org.congcong.Main;
import org.congcong.multi.proxy.config.ServiceConfig;
import org.congcong.multi.proxy.entity.DNSMapping;
import org.congcong.multi.proxy.entity.Pair;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DnsMappingUtil {

    private static final Map<String, Pair<String, Integer>> mappingCache = new HashMap<>();

    static {
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class
                .getClassLoader()
                .getResourceAsStream("dns.yml");
        DNSMapping dnsMapping = yaml.loadAs(inputStream, DNSMapping.class);
        log.info("load dns mapping {}", dnsMapping);
        for (DNSMapping.DnsMappingEntry mapping : dnsMapping.getDnsMappings()) {
            mappingCache.put(mapping.getDomain(), new Pair<>(mapping.getIp(), mapping.getPort()));
        }
    }

    public static Pair<String, Integer> mapping(String remoteDomain, int port) {
        Pair<String, Integer> mapping = mappingCache.get(remoteDomain);
        if (mapping == null) {
            return new Pair<>(remoteDomain, port);
        }
        return mapping;
    }

}
