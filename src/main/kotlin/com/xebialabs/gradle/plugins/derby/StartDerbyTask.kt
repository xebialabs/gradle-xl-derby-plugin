package com.xebialabs.gradle.plugins.derby

import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.library.impl.javaFile
import java.io.PrintWriter
import java.net.InetAddress

open class StartDerbyTask : AbstractDerbyTask() {
    companion object {
        const val NAME = "derbyStart"

        private const val DERBY_DEADLOCK_TIMEOUT = "derby.locks.deadlockTimeout"
        private const val DERBY_WAIT_TIMEOUT = "derby.locks.waitTimeout"

        private fun runCommand(project: Project, hostname: String, port: Int, workingDir: File = File("./")): Process? = runCatching {
            val classpath = project.configurations.getByName("derbynet").asPath
            val jvmPath = "${File.javaHome}${File.separator}bin${File.separator}java"
            ProcessBuilder(jvmPath, "-cp", classpath, NetworkServerControl::class.java.name, "start", "-h", hostname, "-p", port.toString(), "-noSecurityManager")
                    .directory(workingDir.javaFile())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()

        }.onFailure {
            throw GradleException("Cannot start derby server", it)
        }.getOrNull()

        fun startingServer(project: Project, dataDir: String, hostname: String, port: Int, waitTimeout: Int, deadlockTimeout: Int, externalProcess: Boolean) {
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
                if (externalProcess) {
                    runCommand(project, hostname, port)
                    val nsc = NetworkServerControl(InetAddress.getByName(hostname), port)
                    waitForStart(project, nsc, 100, 100)
                    project.logger.lifecycle("Started derby external server on $hostname:$port")
                } else {
                    val nsc = NetworkServerControl(InetAddress.getByName(hostname), port)
                    nsc.start(PrintWriter(System.out))
                    waitForStart(project, nsc, 100, 100)
                    project.logger.lifecycle("Started derby server on $hostname:$port")
                }
            } catch (e: Exception) {
                throw GradleException("Cannot start derby server", e)
            }
        }
    }

    @Input
    val waitTimeout = project.objects.property<Int>().value(DerbyPlugin.getExtension(project).waitTimeout)

    @Input
    val deadlockTimeout = project.objects.property<Int>().value(DerbyPlugin.getExtension(project).deadlockTimeout)

    @Input
    val externalProcess = project.objects.property<Boolean>().value(false)

    init {
        project.afterEvaluate {
            if (externalProcess.get()) {
                val derbynet = configurations.create("derbynet")
                project.dependencies.add(derbynet.name, "org.apache.derby:derbynet:${properties["derbyVersion"]}")
            }
        }
    }

    @TaskAction
    fun startServer() {
        startingServer(
                project,
                dataDir.map { it.trim() }.getOrElse(""),
                hostname.get(),
                port.get(),
                waitTimeout.get(),
                deadlockTimeout.get(),
                externalProcess.get()
        )
    }
}
