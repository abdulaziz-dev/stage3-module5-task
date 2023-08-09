package com.mjc.school.service;

import static com.mjc.school.service.util.Constant.MAPSTRUCT_MAPPER_PATH;
import static com.mjc.school.service.util.Utils.getClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = ServiceTest.TestConfig.class)
public class ServiceTest {
    private static final String BASE_SERVICE = "com.mjc.school.service.BaseService";
    private static final String BASE_REPOSITORY = "com.mjc.school.repository.BaseRepository";

    @Autowired private GenericApplicationContext ctx;

    @Test
    void contextShouldHave2Services() throws ClassNotFoundException {
        var services = ctx.getBeansOfType(Class.forName(BASE_SERVICE));
        assertEquals(2, services.size(), "There should be 2 services in context: Author and News.");
    }

    @Test
    void servicesShouldDependOnRepositories() throws ClassNotFoundException {
        var services = ctx.getBeansOfType(Class.forName(BASE_SERVICE));
        var repositories = ctx.getBeansOfType(Class.forName(BASE_REPOSITORY));

        Set<String> servicesDependencies =
                services.keySet().stream()
                        .flatMap(key -> Arrays.stream(ctx.getBeanFactory().getDependenciesForBean(key)))
                        .collect(Collectors.toSet());

        assertTrue(
                servicesDependencies.containsAll(repositories.keySet()),
                "'Services' should access or store data via 'Repositories'.");
    }

    @Test
    public void serviceLayerShouldHaveClassesValidatorsForInputData() {
        boolean hasValidators =
                getClasses().stream()
                        .anyMatch(c -> c.getName().contains("Valid") || c.getName().contains("Check"));
        assertTrue(
                hasValidators,
                "Service layer should have separate classes-validators for input data with names containing 'Valid' or 'Check'");
    }

    @Test
    void serviceLayerShouldHaveModelDtoMapperClassesOrUseMapstructMapperAnnotation()
            throws ClassNotFoundException {
        boolean hasMapperClass = getClasses().stream().anyMatch(c -> c.getName().contains("Mapper"));
        Class mapperAnnotation = Class.forName(MAPSTRUCT_MAPPER_PATH);
        boolean isMapperAnnotationUsed =
                getClasses().stream()
                        .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                        .filter(m -> !m.isBridge())
                        .anyMatch(m -> m.isAnnotationPresent(mapperAnnotation));

        assertTrue(
                hasMapperClass || isMapperAnnotationUsed,
                "Service layer should have model-dto mapper classes or use mapstruct mapper annotation");
    }

    @Configuration
    @ComponentScan(basePackages = "com.mjc.school")
    public static class TestConfig {}
}