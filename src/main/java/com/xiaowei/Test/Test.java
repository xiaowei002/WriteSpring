package com.xiaowei.Test;

import com.xiaowei.spring.ApplicationContext.MyApplicationContext;
import com.xiaowei.service.UserService;

/***
 * 测试手写spring
 */
public class Test {
    public static void main(String[] args) {
        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}
