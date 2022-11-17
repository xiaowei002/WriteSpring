package com.xiaowei.spring.beanPostProcessor;

/***
 * spring提供的初始化前后做一些操作的接口
 */
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean,String beanName);

    Object postProcessAfterInitialization(Object bean,String beanName);
}
