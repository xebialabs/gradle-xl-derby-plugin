package com.xebialabs.gradle.plugins.derby

import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

abstract class AbstractDerbyTask : DefaultTask() {

    companion object {
        const val DERBY_WRK_DIR = "derby.system.home"

        @Throws(GradleException::class)
        fun waitForShutdown(project: Project, nsc: NetworkServerControl, count: Int, sleep: Int) {
            for (i in 1..count) {
                try {
                    project.logger.info("derby ping #$i")
                    nsc.ping()
                    Thread.sleep(sleep.toLong())
                } catch (e: Exception) {
                    return
                }
            }
            throw GradleException("derby server stop timed out (${count * sleep} ms)")
        }

        @Throws(GradleException::class)
        fun waitForStart(project: Project, nsc: NetworkServerControl, count: Int, sleep: Int) {
            for (i in 1..count) {
                try {
                    project.logger.info("derby ping #$i")
                    nsc.ping()
                    return
                } catch (e: Exception) {
                    project.logger.info("  derby ping failed: ${e.message}")
                    Thread.sleep(sleep.toLong())
                }
            }
            throw GradleException("derby server start timed out (${count * sleep} ms)")
        }
    }

    @Input
    val dataDir = project.objects.property<String>().value(DerbyPlugin.getExtension(project).dataDir)

    @Input
    val hostname = project.objects.property<String>().value(DerbyPlugin.getExtension(project).hostname)

    @Input
    val port = project.objects.property<Int>().value(DerbyPlugin.getExtension(project).port)
}
