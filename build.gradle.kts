buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Detekt plugin
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false

    // OWASP Dependency Check plugin
    id("org.owasp.dependencycheck") version "12.1.5" apply false

    id("com.google.gms.google-services") version "4.4.0" apply false

}



tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt on all modules"
}

tasks.register("dependencyCheckAll") {
    group = "verification"
    description = "Runs dependency check on all modules"
}


