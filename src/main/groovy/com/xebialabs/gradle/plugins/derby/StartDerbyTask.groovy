package com.xebialabs.gradle.plugins.derby

import java.net.InetAddress
import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException

class StartDerbyTask extends AbstractDerbyTask {

    @TaskAction
    void startServer() {
        resolveParameters()

        if (dataDir != null) {
            dataDir = dataDir.trim();
            if (dataDir.isEmpty()) {
                dataDir = null
            }
        }
        if (dataDir != null && !dataDir.isEmpty()) {
            File dataDirFile = project.file(dataDir)
            System.setProperty(DERBY_WRK_DIR, dataDirFile.getAbsolutePath())
        }
        try {
            logger.info("Starting derby server on $hostname:$port")
            def nsc = new NetworkServerControl(InetAddress.getByName(hostname), port)
            nsc.start(new PrintWriter(System.out))
            waitForStart(nsc, 100, 100);
        } catch (Exception e) {
            throw new GradleException("Cannot start derby server", e)
        }
    }
}