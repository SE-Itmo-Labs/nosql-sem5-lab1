package org.seitmolabs.demo;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true")
@ComponentScan(
        basePackageClasses = Demo.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DemoScenario.class)
)
@RequiredArgsConstructor
public class Demo implements CommandLineRunner {
    private final List<DemoScenario> scenarios;

    @Override
    public void run(String... args) {
        // Spring sorts the injected scenarios by @Order before running them.
        scenarios.forEach(DemoScenario::run);
    }
}
