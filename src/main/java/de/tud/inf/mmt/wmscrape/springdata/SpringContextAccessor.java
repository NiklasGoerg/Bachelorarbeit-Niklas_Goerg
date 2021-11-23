package de.tud.inf.mmt.wmscrape.springdata;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SpringContextAccessor implements ApplicationContextAware {

    private static ApplicationContext context;

    public static <T> T getBean(Class<T> beanClass) {
        if (context != null && context.containsBean(String.valueOf(beanClass))) {
            return context.getBean(beanClass);
        }
        return null;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        SpringContextAccessor.context = context;
    }
}