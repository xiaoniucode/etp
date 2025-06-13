package cn.xilio.vine.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpringHelper implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringHelper.applicationContext = applicationContext;
    }

    /**
     * 获取指定类型的所有Bean
     *
     * @param type Bean类型
     * @param <T>  泛型类型
     * @return Bean Map (name -> instance)
     */
    public static <T>  T getBean(Class<T> type) {
        return applicationContext.getBean(type);
    }

}
