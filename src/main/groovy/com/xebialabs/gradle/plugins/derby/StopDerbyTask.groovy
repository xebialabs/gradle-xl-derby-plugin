package com.xebialabs.gradle.plugins.derby

import java.net.InetAddress;
import org.apache.derby.drda.NetworkServerControl;
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException

class StopDerbyTask extends AbstractDerbyTask {

    @TaskAction
    void stopServer() {
        resolveParameters()
        if (dataDir != null && !dataDir.isEmpty()) {
            File dataDirFile = project.file(dataDir)
            System.setProperty(DERBY_WRK_DIR, dataDirFile.getAbsolutePath())
        }
        try {
            logger.info("Stopping derby server on $hostname:$port")
            def nsc = new NetworkServerControl(InetAddress.getByName(hostname), port)
            nsc.shutdown()
            waitForShutdown(nsc, 100, 100)
        } catch (Exception e) {
            throw new GradleException("Cannot stop derby server", e)
        }
    }
}
