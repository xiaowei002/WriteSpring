package com.xiaowei.service.useImpl;

import com.xiaowei.service.impl.UserServiceImpl;
import com.xiaowei.spring.ApplicationContext.MyApplicationContext;
import com.xiaowei.spring.annonation.Component;
import com.xiaowei.spring.beanPostProcessor.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 自定义处理bean初始化前以及初始化后的操作类，所有的bean对象共有一个beanPostProcessor类
 */
@Component()
public class MyBeanPostProcessor implements BeanPostProcessor {
    /**
     * 初始化前的操作
     * @param bean
     * @param beanName
     * @return
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if(beanName.equals("userService")){
            System.out.println("初始化前");
            ((UserServiceImpl)bean).setName(beanName);
        }
        return bean;
    }

    /***
     * 初始化后的操作
     * @param bean
     * @param beanName
     * @return
     */
    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) {
        if(beanName.equals("userService")){
            System.out.println("初始化后");
            Object proxyInstance = Proxy.newProxyInstance(MyApplicationContext.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");
                    return method.invoke(bean,args);
                }
            });
            return proxyInstance;

        }
        return bean;
    }
}
