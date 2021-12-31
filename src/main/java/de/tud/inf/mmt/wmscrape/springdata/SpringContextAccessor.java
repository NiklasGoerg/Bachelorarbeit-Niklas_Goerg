package de.tud.inf.mmt.wmscrape.springdata;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SpringContextAccessor implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * allows accessing a spring created bean outside the spring context
     *
     * @param beanClass the requested class
     * @param <T> some class
     * @return the requested spring bean instance
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (context != null && context.containsBean(String.valueOf(beanClass))) {
            return context.getBean(beanClass);
        }
        return null;
    }

    /**
     * stores the spring context
     *
     * @param context the spring context
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        SpringContextAccessor.context = context;
    }
}