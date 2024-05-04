import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    java
    idea
    eclipse
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.github.ben-manes.versions")
    id("com.diffplug.spotless")

    jacoco
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    create("functionalTest") {
        java {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            srcDir("src/functional-test/java")
        }
    }
}

idea {
    module {
        testSources.from(sourceSets["functionalTest"].java.srcDirs)
    }
}

val functionalTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val functionalTestRuntimeOnly: Configuration by configurations.getting

configurations {
    configurations["functionalTestImplementation"].extendsFrom(configurations.testImplementation.get())
    configurations["functionalTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())
}


val functionalTest = task<Test>("functionalTest") {
    description = "Runs functional tests."
    group = "verification"

    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events ("failed", "passed", "skipped", "standard_out")
    }
}

val excludesPackages: Iterable<String> by extra {
    listOf(
        "uk/tw/energy/App**",
        "uk/tw/energy/SeedingApplicationDataConfiguration**"
    )
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.95".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(excludesPackages)
        }
    )
}
tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(file("$buildDir/reports/jacoco/test"))
    }
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(excludesPackages)
        }
    )
}

dependencies {
    /* Spring Boot */
    implementation ("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude (group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events ("failed", "passed", "skipped", "standard_out")
    }
}

tasks.check {
    dependsOn(functionalTest)
    finalizedBy(tasks.jacocoTestCoverageVerification, tasks.jacocoTestReport)
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
    gradleReleaseChannel="current"
}

spotless {
    java {
        googleJavaFormat()
        formatAnnotations()
    }
}