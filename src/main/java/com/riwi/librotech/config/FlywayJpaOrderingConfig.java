package com.riwi.librotech.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Fuerza el orden de inicialización para evitar que Hibernate valide el esquema
 * antes de que Flyway ejecute las migraciones.
 *
 * Objetivo: Flyway (flywayInitializer) -> JPA (entityManagerFactory)
 */
@Configuration
public class FlywayJpaOrderingConfig implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBeanDefinition("entityManagerFactory")) {
            return;
        }
        if (!beanFactory.containsBeanDefinition("flywayInitializer")) {
            return;
        }

        // Asegurar que Flyway no dependa de JPA (evita inversiones de orden y ciclos)
        removeDependsOn(beanFactory, "flyway", "entityManagerFactory");
        removeDependsOn(beanFactory, "flywayInitializer", "entityManagerFactory");

        // Forzar: Flyway migra (flywayInitializer) -> JPA valida (entityManagerFactory)
        BeanDefinition emf = beanFactory.getBeanDefinition("entityManagerFactory");
        Set<String> dependsOn = new LinkedHashSet<>();
        if (emf.getDependsOn() != null) {
            dependsOn.addAll(Arrays.asList(emf.getDependsOn()));
        }
        dependsOn.add("flywayInitializer");
        emf.setDependsOn(dependsOn.toArray(new String[0]));
    }

    private void removeDependsOn(ConfigurableListableBeanFactory beanFactory, String beanName, String dependsOnToRemove) {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            return;
        }
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition.getDependsOn() == null) {
            return;
        }
        Set<String> dependsOn = new LinkedHashSet<>(Arrays.asList(beanDefinition.getDependsOn()));
        dependsOn.remove(dependsOnToRemove);
        beanDefinition.setDependsOn(dependsOn.isEmpty() ? null : dependsOn.toArray(new String[0]));
    }
}
