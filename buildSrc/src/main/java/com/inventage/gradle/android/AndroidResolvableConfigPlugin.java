package com.inventage.gradle.android;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Dominik Menzi
 */
public class AndroidResolvableConfigPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidResolvableConfigPlugin.class);

    @Override
    public void apply(Project project) {
        final Configuration scanning = project.getConfigurations().create("iqServerScanning");

        copyReleaseCompileClasspathDependencies(project, scanning);
    }

    private void copyReleaseCompileClasspathDependencies(Project target, Configuration scanning) {
        LOGGER.debug("configuring project {}", target.getPath());
        target.getConfigurations().all(configuration -> {
            LOGGER.trace("configuration in {} -> {}", target.getPath(), configuration.getName());
            if (Objects.equals(configuration.getName(), "releaseCompileClasspath")) {
                LOGGER.debug("found releaseCompileClasspath in {}", target.getPath());
                configuration.getAllDependencies().all(dependency -> {
                    LOGGER.trace("found dependency: {}", dependency);
                    if (dependency instanceof ProjectDependency) {
                        final Project dependencyProject = ((ProjectDependency) dependency).getDependencyProject();
                        LOGGER.debug("found project: {}", ((ProjectDependency) dependency).getDependencyProject().getPath());
                        target.evaluationDependsOn(dependencyProject.getPath());
                        copyReleaseCompileClasspathDependencies(((ProjectDependency) dependency).getDependencyProject(), scanning);
                    }
                    else {
                        scanning.getDependencies().add(dependency);
                    }
                });
            }
        });
    }
}
