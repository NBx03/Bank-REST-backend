package com.example.bankcards.testutil;

import java.io.IOException;
import java.util.List;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Реализация PropertySourceFactory, умеющая читать YAML-файлы.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    private static final String DEFAULT_NAME = "yaml";

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        var loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> propertySources = loader.load(getName(name, resource), resource.getResource());

        if (propertySources.isEmpty()) {
            return new CompositePropertySource(getName(name, resource));
        }

        if (propertySources.size() == 1) {
            return propertySources.get(0);
        }

        CompositePropertySource composite = new CompositePropertySource(getName(name, resource));
        propertySources.forEach(composite::addPropertySource);
        return composite;
    }

    private String getName(String name, EncodedResource resource) {
        if (name != null) {
            return name;
        }
        if (resource.getResource().getFilename() != null) {
            return resource.getResource().getFilename();
        }
        return DEFAULT_NAME;
    }
}