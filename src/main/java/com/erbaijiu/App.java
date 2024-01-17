package com.erbaijiu;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.erbaijiu.cmd.CmdFactory;
import com.erbaijiu.cmd.Init;
import com.erbaijiu.cmd.Register;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.MutablePropertySources;

import java.util.Scanner;

@SpringBootApplication
@ComponentScan(value = "com")
public class App {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println(StringUtils.join(args, ","));
        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
        MutablePropertySources mutablePropertySources = context.getEnvironment().getPropertySources();

        System.out.println("hello.");

        String runType = System.getProperty("run_type");
        if(StringUtils.isEmpty(runType)){
            System.out.println("empty run_type, do nothing.");
            return;
        }
        // 从Spring应用上下文中获取CmdFactory的实例
        CmdFactory cmdFactory = context.getBean(CmdFactory.class);
//        CmdFactory factory = new CmdFactory();
        cmdFactory.doCmd(runType);
    }
}
