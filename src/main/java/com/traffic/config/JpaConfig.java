package com.traffic.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return beanFactory -> {
            String[] jpaRepositoryBeans = beanFactory.getBeanNamesForType(EntityManagerFactory.class);
            for (String beanName : jpaRepositoryBeans) {
                var beanDefinition = beanFactory.getBeanDefinition(beanName);
                beanDefinition.setDependsOn("flyway");
            }
        };
    }
}
