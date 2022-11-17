package com.xiaowei.spring.ApplicationContext;

import com.xiaowei.spring.beanDefinition.BeanDefinition;
import com.xiaowei.spring.userInterface.BeanNameAware;
import com.xiaowei.spring.userInterface.InitializingBean;
import com.xiaowei.spring.annonation.Autowired;
import com.xiaowei.spring.annonation.Component;
import com.xiaowei.spring.annonation.ComponentScan;
import com.xiaowei.spring.annonation.Scope;
import com.xiaowei.spring.beanPostProcessor.BeanPostProcessor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义一个application类,
 */
public class MyApplicationContext {
    //配置文件
    private Class appConfig;

    //单例池，用来存放单例bean对象
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    //用来存放原型bean对象。
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    //用来存放beanPostProcessor对象
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * 构造方法
     * @param appConfig
     */
    public MyApplicationContext(Class appConfig) {
        this.appConfig = appConfig;

        /**
         * 读取配置文件，并解析配置文件
         * 1.获取到包扫描的路径，@ComponentScan
         * 2.通过1获取到path路径
         */
        scanConfig(appConfig);

        /**
         * 如果bean是一个单例bean的话，需要在扫描之后就把所有的单例bean给创建出来
         */
        for (Map.Entry<String,BeanDefinition> entry: beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = creatBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }
    }

    /**
     * 创建bean对象。
     * @param beanDefinition
     * @return
     */
    private Object creatBean(String beanName,BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            /**
             * 此时通过无参构造获取的bean对象的属性为空，为了达到自动装配的效果，需要依赖注入
             */
            //获取所有的属性
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                //判断属性是否被@Autowired标注，
                if(field.isAnnotationPresent(Autowired.class)){
                   //使用自动装配byName实现
                    Object bean = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(instance,bean);
                }
            }

            //bean 依赖注入完成之后，回填beanName给属性值
            if(instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            //初始化前的操作
            for (BeanPostProcessor beanPostProcessor :beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }

            //初始化bean
            if(instance instanceof InitializingBean) {
                try {
                    ((InitializingBean)instance).afterPropertiesSet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            //初始化后的操作
            for(BeanPostProcessor beanPostProcessor: beanPostProcessorList){
                beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }


            return instance;

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 扫描配置文件
     * @param appConfig
     */
    private void scanConfig(Class appConfig) {

        String path = getPath(appConfig);
        /**
         * 获取到得路径是一个com.xxx.xxx，需要转换为目录格式com/xxx/xxx
         */
        path = path.replace(".","/");

        /**
         * 通过应用类加载器获取到classpath，然后根据path获取到service包下的所有类，然后再去判断包中是否有@Component注解
         */
        ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);//获取到路径，然后将这个路径下的文件转换为file文件
        /**
         * 如果file是一个目录的话，获取到file下边的所有文件，也就是获取到了service包下面的所有类的class文件。
         */
        if(resource.getFile() != null){
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                /**
                 * 遍历class文件，判断class文件是否有被@component注解标识，如果有的话，在做后续操作
                 * 这里的f是一个class对象
                 */
                for (File f : files) {
                    /**
                     * 处理绝对路径字符串，让其在类加载时使用
                     */
                    String absolutePath = f.getAbsolutePath();
                    //判断是否为类文件
                    if(absolutePath.endsWith(".class")) {
                        absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                        //路径转换为com.xxx.xxx
                        String fileName = absolutePath.replace("\\", ".");
                        try {
                            Class<?> clazz = classLoader.loadClass(fileName);
                            //判断class文件是否被@Component注解标注
                            if (clazz.isAnnotationPresent(Component.class)) {

                                /**
                                 * 判断被@Component标注的类是否实现了BeanPostProcessor接口
                                 */
                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                    beanPostProcessorList.add(beanPostProcessor);
                                }


                                /**
                                 * 解析类，判断类是一个单例bean还是原型bean，由于getBean也得去解析类，而且每次都得去解析
                                 * 引申出一个概念beanDefinition ,bean定义。
                                 */

                                Component component = clazz.getDeclaredAnnotation(Component.class);
                                String beanName = component.value();

                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setClazz(clazz);

                                if(clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scope = clazz.getDeclaredAnnotation(Scope.class);
                                    beanDefinition.setScope(scope.value());
                                }else {
                                    beanDefinition.setScope("singleton");
                                }

                                //beanDefinitionMap 对象中存放的是beanDefinition对象。而不是原型对象。Spring扫描到的所有的bean的定义
                                beanDefinitionMap.put(beanName,beanDefinition);

                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取包扫描的路径。
     * @param appConfig
     * @return
     */
    private static String getPath(Class appConfig) {
        ComponentScan componentScan = (ComponentScan) appConfig.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScan.value();//获取到扫描的路径
        return path;
    }

    /***
     * 获取bean对象的getBean方法
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        //判断beanDefinitionMap中是否存有beanName的key
         if(beanDefinitionMap.containsKey(beanName)){
             BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
             if(beanDefinition.getScope().equals("singleton")){
                 Object bean = singletonObjects.get(beanName);
                 return bean;
             }else {
                 //创建bean对象。
                 Object bean = creatBean(beanName,beanDefinition);
                 return bean;
             }
         }else {
             throw new RuntimeException("不存在对应的bean对象");
         }
    }
}
