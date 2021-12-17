package com.xebialabs.gradle.plugins.derby

import org.gradle.api.Plugin
import org.gradle.api.Project

class DerbyPlugin: Plugin<Project> {

    companion object {
        const val NAME = "derby"

        fun getExtension(project: Project):DerbyExtension  {
            return project.extensions.getByName(NAME) as DerbyExtension
        }
    }

    override fun apply(project: Project) {
        project.extensions.create(NAME, DerbyExtension::class.java)

        val derbyExtension = getExtension(project)
        derbyExtension.dataDir.convention("${project}.buildDir/derbydb")
        derbyExtension.hostname.convention("0.0.0.0")
        derbyExtension.port.convention(1527)
        derbyExtension.waitTimeout.convention(-1)
        derbyExtension.deadlockTimeout.convention(-1)

        project.tasks.create(StartDerbyTask.NAME, StartDerbyTask::class.java)
        project.tasks.create(StopDerbyTask.NAME, StopDerbyTask::class.java)
    }
}
