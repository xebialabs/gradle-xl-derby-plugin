package com.xebialabs.gradle.plugins.derby

import org.apache.derby.drda.NetworkServerControl
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

abstract class AbstractDerbyTask extends DefaultTask {
    static final String DERBY_WRK_DIR = "derby.system.home"

    String group = 'derbyDb'
    String dataDir
    String hostname
    Integer port

    protected String resolveParameters() {
        if (dataDir == null || dataDir.isEmpty()) {
            dataDir = project.derby.dataDir
        }
        if (hostname == null || hostname.isEmpty()) {
            hostname = project.derby.hostname
        }
        if (port == null) {
            port = project.derby.port
        }
    }

    void waitForStart(NetworkServerControl nsc, int count, int sleep) {
        for (int i = 0; i < count; i++) {
            try {
                logger.info("derby ping #" + i)
                nsc.ping()
                return
            } catch (Exception e) {
                logger.info("  derby ping failed: " + e.getMessage())
                Thread.sleep(sleep)
            }
        }
        throw new GradleException("derby server start timed out (" + (count*sleep) + " ms)")
    }

    void waitForShutdown(NetworkServerControl nsc, int count, int sleep) {
        for (int i = 0; i < count; i++) {
            try {
                logger.info("derby ping #" + i)
                nsc.ping()
                Thread.sleep(sleep)
            } catch (Exception e) {
                return
            }
        }
        throw new GradleException("derby server stop timed out (" + (count*sleep) + " ms)")
    }
}