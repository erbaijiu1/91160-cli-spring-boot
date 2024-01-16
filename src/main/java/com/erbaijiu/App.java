package com.erbaijiu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class App {
    private static final Scanner in = new Scanner(System.in);
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        System.out.println("hello.");

        System.out.print("请输入密码: ");
        String input = in.nextLine();
        System.out.println(input);
    }
}
