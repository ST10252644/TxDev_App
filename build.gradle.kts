// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Detekt plugin
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false

    // OWASP Dependency Check plugin
    id("org.owasp.dependencycheck") version "12.1.5" apply false
}



tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt on all modules"
}

tasks.register("dependencyCheckAll") {
    group = "verification"
    description = "Runs dependency check on all modules"
}


