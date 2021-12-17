package com.xebialabs.gradle.plugins.derby

import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.net.InetAddress

open class StopDerbyTask: AbstractDerbyTask() {
    companion object {
        const val NAME = "derbyStop"

        fun stoppingServer(project: Project, dataDir: String, hostname: String, port: Int, ignoreStopFailure: Boolean) {
            if (dataDir.isNotEmpty()) {
                val dataDirFile = project.file(dataDir)
                System.setProperty(DERBY_WRK_DIR, dataDirFile.absolutePath)
            }
            try {
                project.logger.info("Stopping derby server on $hostname:$port")
                val nsc = NetworkServerControl(InetAddress.getByName(hostname), port)
                nsc.shutdown()
                waitForShutdown(project, nsc, 100, 100)
                project.logger.lifecycle("Stopped derby server on $hostname:$port")
            } catch ( e: Exception) {
                if (!ignoreStopFailure) {
                    throw GradleException("Cannot stop derby server", e)
                }
            }
        }
    }

    @Input
    val ignoreStopFailure = project.objects.property<Boolean>().value(true)

    @TaskAction
    fun stopServer() {
        stoppingServer(
                project,
                dataDir.map { it.trim() }.getOrElse(""),
                hostname.get(),
                port.get(),
                ignoreStopFailure.get())
    }
}
