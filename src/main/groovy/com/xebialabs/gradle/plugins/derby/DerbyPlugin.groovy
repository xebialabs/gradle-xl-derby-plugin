package com.xebialabs.gradle.plugins.derby

import org.gradle.api.Plugin
import org.gradle.api.Project

class DerbyPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create("derby", DerbyExtension, project)
        project.task('derbyStart', type: StartDerbyTask)
        project.task('derbyStop', type: StopDerbyTask)
    }
}