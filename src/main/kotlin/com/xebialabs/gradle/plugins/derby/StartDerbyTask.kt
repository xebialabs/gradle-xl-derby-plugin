package com.xebialabs.gradle.plugins.derby

import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.PrintWriter
import java.net.InetAddress

open class StartDerbyTask : AbstractDerbyTask() {
    companion object {
        const val NAME = "derbyStart"

        private const val DERBY_DEADLOCK_TIMEOUT = "derby.locks.deadlockTimeout"
        private const val DERBY_WAIT_TIMEOUT = "derby.locks.waitTimeout"

        fun startingServer(project: Project, dataDir: String, hostname: String, port: Int, waitTimeout: Int, deadlockTimeout: Int) {
            if (dataDir.isNotEmpty()) {
                val dataDirFile = project.file(dataDir)
                System.setProperty(DERBY_WRK_DIR, dataDirFile.absolutePath)
            }
            if (waitTimeout > 0) {
                System.setProperty(DERBY_WAIT_TIMEOUT, waitTimeout.toString())
                project.logger.lifecycle("Using derby wait timeout $waitTimeout")
            }
            if (deadlockTimeout > 0) {
                System.setProperty(DERBY_DEADLOCK_TIMEOUT, deadlockTimeout.toString())
                project.logger.lifecycle("Using derby wait timeout $deadlockTimeout")
            }

            try {
                project.logger.lifecycle("Starting derby server on $hostname:$port")
                val nsc = NetworkServerControl(InetAddress.getByName(hostname), port)
                nsc.start(PrintWriter(System.out))
                waitForStart(project, nsc, 100, 100)
                project.logger.lifecycle("Started derby server on $hostname:$port")
            } catch (e: Exception) {
                throw GradleException("Cannot start derby server", e)
            }
        }
    }

    @Input
    val waitTimeout = project.objects.property<Int>().value(DerbyPlugin.getExtension(project).waitTimeout)

    @Input
    val deadlockTimeout = project.objects.property<Int>().value(DerbyPlugin.getExtension(project).deadlockTimeout)

    @TaskAction
    fun startServer() {
        startingServer(
                project,
                dataDir.map { it.trim() }.getOrElse(""),
                hostname.get(),
                port.get(),
                waitTimeout.get(),
                deadlockTimeout.get()
        )
    }
}
