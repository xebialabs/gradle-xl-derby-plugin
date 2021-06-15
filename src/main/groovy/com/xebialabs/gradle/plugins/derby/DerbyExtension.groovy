package com.xebialabs.gradle.plugins.derby

import org.gradle.api.Project

class DerbyExtension {
    Project project
    String dataDir = "$project.buildDir/derbydb"
    String hostname = "0.0.0.0"
    int port = 1527
    Integer waitTimeout
    Integer deadlockTimeout

    DerbyExtension(Project project) {
        this.project = project
    }
}
