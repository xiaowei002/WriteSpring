package com.xiaowei.service.impl;

import com.xiaowei.service.UserService;
import com.xiaowei.spring.userInterface.BeanNameAware;
import com.xiaowei.spring.userInterface.InitializingBean;
import com.xiaowei.spring.annonation.Autowired;
import com.xiaowei.spring.annonation.Component;
import com.xiaowei.spring.annonation.Scope;

/**
 * 用户的userService业务类，需要被标识为bean对象
 */
@Component("userService")
@Scope("prototype")
public class UserServiceImpl implements BeanNameAware, InitializingBean, UserService {

    @Autowired
    private OrderService orderService;

    private String beanName;


    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void test(){
        System.out.println(orderService);
        System.out.println(beanName);
        System.out.println(name);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("初始化");
    }
}
