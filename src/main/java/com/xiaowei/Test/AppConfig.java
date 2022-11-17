package com.xiaowei.Test;

import com.xiaowei.spring.annonation.ComponentScan;

/***
 * 自定义配置类，类似classPathContext
 * 该类需要被扫描，自定义注解
 */
@ComponentScan("com.xiaowei.service")
public class AppConfig {
}
