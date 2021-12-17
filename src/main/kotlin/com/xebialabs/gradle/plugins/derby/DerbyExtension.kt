package com.xebialabs.gradle.plugins.derby

import org.gradle.api.provider.Property

interface DerbyExtension {
    val dataDir: Property<String>
    val hostname: Property<String>
    val port: Property<Int>
    val waitTimeout: Property<Int>
    val deadlockTimeout: Property<Int>
}
