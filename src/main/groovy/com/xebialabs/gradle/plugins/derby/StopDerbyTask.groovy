package com.xebialabs.gradle.plugins.derby

import org.gradle.api.Project
import java.net.InetAddress;
import org.apache.derby.drda.NetworkServerControl;
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException

class StopDerbyTask extends AbstractDerbyTask {

    boolean ignoreStopFailure = false

    @TaskAction
    void stopServer() {
        resolveParameters()
        stoppingServer(project, dataDir, hostname, port, ignoreStopFailure)
    }

    static stoppingServer(Project project, String dataDir, String hostname, Integer port, Boolean ignoreStopFailure) {

        if (dataDir != null && !dataDir.isEmpty()) {
            File dataDirFile = project.file(dataDir)
            System.setProperty(DERBY_WRK_DIR, dataDirFile.getAbsolutePath())
        }
        try {
            project.logger.info("Stopping derby server on $hostname:$port")
            def nsc = new NetworkServerControl(InetAddress.getByName(hostname), port)
            nsc.shutdown()
            waitForShutdown(project, nsc, 100, 100)
        } catch (Exception e) {
            if (!ignoreStopFailure) {
                throw new GradleException("Cannot stop derby server", e)
            }
        }
    }
}
