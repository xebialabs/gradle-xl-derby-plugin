package com.xebialabs.gradle.plugins.derby

import org.gradle.api.Project

class DerbyPluginUtil {

  static checkIfTaskStopped(Project project, String startTaskName, String stopTaskName, Closure<Boolean> doCheck = { true }) {
    project.gradle.buildFinished {
      if (doCheck() && project.tasks[startTaskName]?.state?.executed && !project.tasks[stopTaskName]?.state?.executed) {
        def startTask = project.tasks[startTaskName]
        project.logger.lifecycle(
            "Forcing stop derby for start task $startTaskName with dataDir: ${startTask.dataDir}, hostname: ${startTask.hostname}, port: ${startTask.port}")
        StopDerbyTask.stoppingServer(project, startTask.dataDir, startTask.hostname, startTask.port, true)
      } else {
        project.logger.debug("No need for stop derby for start task $startTaskName")
      }
    }
  }
}
