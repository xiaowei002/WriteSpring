package com.xiaowei.spring.beanDefinition;

/**
 * BeanDefinition对象，bean定义对象。
 */
public class BeanDefinition {
    private Class clazz;
    private String scope;
    /**
     * 还有一些bean是否懒加载之类的属性
     */
    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
