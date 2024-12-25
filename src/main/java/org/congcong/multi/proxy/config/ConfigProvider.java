package org.congcong.multi.proxy.config;

import lombok.extern.slf4j.Slf4j;
import org.congcong.Main;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

@Slf4j
public class ConfigProvider {

    private static final ServiceConfig conf;

    static {
        String jarDir = System.getProperty("user.dir");  // 获取当前工作目录（JAR所在的目录）
        File configFile = new File(jarDir, "application.yml");

        if (!configFile.exists()) {
            log.info("load inner application.yml");
            Yaml yaml = new Yaml();
            InputStream inputStream = Main.class
                    .getClassLoader()
                    .getResourceAsStream("application.yml");

            conf = yaml.loadAs(inputStream, ServiceConfig.class);
        } else {
            log.info("load your application.yml");
            Yaml yaml = new Yaml();
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                conf = yaml.loadAs(inputStream, ServiceConfig.class);
                // 使用 conf 进行后续处理
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ServiceConfig getConfig() {
        return conf;
    }



}
