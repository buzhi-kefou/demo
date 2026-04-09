package com.parallel.learn.flowable.core.deployment;

import java.io.IOException;
import java.io.InputStream;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FlowableXmlDeploymentRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlowableXmlDeploymentRunner.class);

    private final RepositoryService repositoryService;
    private final FlowableDeploymentProperties properties;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public FlowableXmlDeploymentRunner(RepositoryService repositoryService, FlowableDeploymentProperties properties) {
        this.repositoryService = repositoryService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isAutoDeployEnabled()) {
            log.info("Flowable XML auto deployment is disabled");
            return;
        }
        for (String location : properties.getResourceLocations()) {
            deployResources(location);
        }
    }

    private void deployResources(String location) throws IOException {
        Resource[] resources = resolver.getResources(location);
        if (resources.length == 0) {
            log.info("No Flowable XML resources found for pattern={}", location);
            return;
        }
        var builder = repositoryService.createDeployment()
            .name(properties.getDeploymentName())
            .key(properties.getDeploymentName())
            .enableDuplicateFiltering();

        int added = 0;
        for (Resource resource : resources) {
            if (!resource.exists()) {
                continue;
            }
            try (InputStream inputStream = resource.getInputStream()) {
                builder.addInputStream(resource.getFilename(), inputStream);
                added++;
            }
        }
        if (added == 0) {
            log.info("No deployable Flowable XML resources found for pattern={}", location);
            return;
        }
        Deployment deployment = builder.deploy();
        log.info("Flowable XML deployment finished, pattern={}, deploymentId={}, resourceCount={}",
            location, deployment.getId(), added);
    }
}
