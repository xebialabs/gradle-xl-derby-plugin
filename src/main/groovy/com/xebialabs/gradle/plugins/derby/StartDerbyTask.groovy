package com.xebialabs.gradle.plugins.derby

import java.net.InetAddress
import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException

class StartDerbyTask extends AbstractDerbyTask {
    static final String DERBY_DEADLOCK_TIMEOUT = "derby.locks.deadlockTimeout"
    static final String DERBY_WAIT_TIMEOUT = "derby.locks.waitTimeout"

    Integer waitTimeout
    Integer deadlockTimeout

    @TaskAction
    void startServer() {
        resolveParameters()

        if (getWaitTimeout() == null) {
            setWaitTimeout(project.derby.waitTimeout)
        }
        if (getDeadlockTimeout() == null) {
            setDeadlockTimeout(project.derby.deadlockTimeout)
        }

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
        if (getWaitTimeout()) {
            System.setProperty(DERBY_WAIT_TIMEOUT, getWaitTimeout().toString())
            logger.lifecycle("Using derby wait timeout ${getWaitTimeout()}")
        }
        if (getDeadlockTimeout()) {
            System.setProperty(DERBY_DEADLOCK_TIMEOUT, getDeadlockTimeout().toString())
            logger.lifecycle("Using derby wait timeout ${getDeadlockTimeout()}")
        }

        try {
            logger.lifecycle("Starting derby server on $hostname:$port")
            def nsc = new NetworkServerControl(InetAddress.getByName(hostname), port)
            nsc.start(new PrintWriter(System.out))
            waitForStart(nsc, 100, 100);
        } catch (Exception e) {
            throw new GradleException("Cannot start derby server", e)
        }
    }
}
