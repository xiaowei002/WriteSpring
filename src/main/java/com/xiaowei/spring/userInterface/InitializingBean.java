package com.xiaowei.spring.userInterface;

/**
 * spring初始化的接口
 */
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
