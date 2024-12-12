group = "it.pagopa.qi"

version = "0.0.1-SNAPSHOT"

description = "pagopa-qi-fdr-kpi-service"

plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.3.6"
  id("io.spring.dependency-management") version "1.1.6"
  id("com.diffplug.spotless") version "6.18.0"
  id("org.sonarqube") version "4.0.0.2929"
  id("com.dipien.semantic-version") version "2.0.0" apply false
  jacoco
  application // configures the JAR manifest, handles classpath dependencies etc.
}

repositories {
  mavenCentral()
  mavenLocal()
}

object Dependencies {
  const val ecsLoggingVersion = "1.5.0"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

configurations { compileOnly { extendsFrom(configurations.annotationProcessor.get()) } }

configurations {
  implementation.configure {
    exclude(module = "spring-boot-starter-web")
    exclude("org.apache.tomcat")
    exclude(group = "org.slf4j", module = "slf4j-simple")
  }
  compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

springBoot {
  mainClass.set("it.pagopa.qi.fdrkpiservice.PagopaQiFdrKpiServiceApplicationKt")
  buildInfo {
    properties {
      additional.set(mapOf("description" to (project.description ?: "Default description")))
    }
  }
}

tasks.named<Jar>("jar") { enabled = false }

tasks.create("applySemanticVersionPlugin") {
  group = "semantic-versioning"
  description = "Semantic versioning plugin"
  dependsOn("prepareKotlinBuildScriptModel")
  apply(plugin = "com.dipien.semantic-version")
}

dependencyLocking { lockAllConfigurations() }

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("co.elastic.logging:logback-ecs-encoder:${Dependencies.ecsLoggingVersion}")
  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    toggleOffOn()
    targetExclude("build/**/*")
    ktfmt().kotlinlangStyle()
  }
  kotlinGradle {
    toggleOffOn()
    targetExclude("build/**/*.kts")
    ktfmt().googleStyle()
  }
  java {
    target("**/*.java")
    targetExclude("build/**/*")
    eclipse().configFile("eclipse-style.xml")
    toggleOffOn()
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.test {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
  jvmArgs(listOf("--enable-preview"))
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report

  classDirectories.setFrom(
    files(
      classDirectories.files.map {
        fileTree(it).matching {
          exclude("it/pagopa/qi/fdrkpiservice/PagopaQiFdrKpiServiceApplicationKt.class")
        }
      }
    )
  )

  reports { xml.required.set(true) }
}

/**
 * Task used to expand application properties with build specific properties such as artifact name
 * and version
 */
tasks.processResources { filesMatching("application.properties") { expand(project.properties) } }
