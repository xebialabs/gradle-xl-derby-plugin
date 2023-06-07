package com.xebialabs.gradle.plugins.derby

import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.konan.file.File
import java.io.PrintWriter
import java.net.InetAddress

open class StartDerbyTask : AbstractDerbyTask() {
    companion object {
        const val NAME = "derbyStart"

        private const val DERBY_DEADLOCK_TIMEOUT = "derby.locks.deadlockTimeout"
        private const val DERBY_WAIT_TIMEOUT = "derby.locks.waitTimeout"

        private fun runCommand(project: Project, hostname: String, port: Int, env: Map<String, String>, workingDir: File): Process? = runCatching {
            val classpath = project.configurations.getByName("derbynet").asPath
            val jvmPath = "${File.javaHome}${File.separator}bin${File.separator}java"
            val pb = ProcessBuilder(jvmPath, "-cp", classpath, NetworkServerControl::class.java.name, "start", "-h", hostname, "-p", port.toString(), "-noSecurityManager")
                    .directory(java.io.File(workingDir.path))
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
            pb.environment().putAll(env)
            pb.start()

        }.onFailure {
            throw GradleException("Cannot start derby server", it)
        }.getOrNull()

        fun startingServer(project: Project, dataDir: String, hostname: String, port: Int, waitTimeout: Int, deadlockTimeout: Int, externalProcess: Boolean) {
            val env = mutableMapOf<String, String>()
            if (dataDir.isNotEmpty()) {
                val dataDirFile = project.file(dataDir)
                env[DERBY_WRK_DIR] = dataDirFile.absolutePath
            }
            if (waitTimeout > 0) {
                env[DERBY_WAIT_TIMEOUT] = waitTimeout.toString()
                project.logger.lifecycle("Using derby wait timeout $waitTimeout")
            }
            if (deadlockTimeout > 0) {
                env[DERBY_DEADLOCK_TIMEOUT] = deadlockTimeout.toString()
                project.logger.lifecycle("Using derby wait timeout $deadlockTimeout")
            }

            try {
                project.logger.lifecycle("Starting derby server on $hostname:$port with work dir $dataDir")
                if (externalProcess) {
                    runCommand(project, hostname, port, env, File(env[DERBY_WRK_DIR] ?: "./"))
                    val nsc = NetworkServerControl(InetAddress.getByName(hostname), port)
                    waitForStart(project, nsc, 100, 100)
                    project.logger.lifecycle("Started derby external server on $hostname:$port")
                } else {
                    env.forEach {
                        System.setProperty(it.key, it.value)
                    }
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
    val externalProcess = project.objects.property<Boolean>().value(DerbyPlugin.getExtension(project).externalProcess)

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
