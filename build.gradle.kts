import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "it.pagopa.qi"

version = "0.2.0"

description = "pagopa-qi-fdr-kpi-service"

plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.3.6"
  id("io.spring.dependency-management") version "1.1.6"
  id("com.diffplug.spotless") version "6.18.0"
  id("org.sonarqube") version "4.0.0.2929"
  id("com.dipien.semantic-version") version "2.0.0" apply false
  id("org.openapi.generator") version "6.6.0"
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
    exclude(module = "spring-boot-starter-webflux")
    exclude("org.apache.tomcat")
    exclude(group = "org.slf4j", module = "slf4j-simple")
  }
  compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

springBoot {
  mainClass.set("it.pagopa.qi.fdrkpi.PagopaQiFdrKpiServiceApplicationKt")
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
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("jakarta.validation:jakarta.validation-api")
  implementation("jakarta.annotation:jakarta.annotation-api")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  // Kotlin dependencies
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("co.elastic.logging:logback-ecs-encoder:${Dependencies.ecsLoggingVersion}")
  implementation("com.microsoft.azure.kusto:kusto-data:6.0.0")
  implementation("org.openapitools:jackson-databind-nullable:0.2.6")
  implementation("io.swagger.core.v3:swagger-annotations:2.2.8")
  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
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

sourceSets {
  main {
    java { srcDirs(layout.buildDirectory.dir("generated/src/main/java")) }
    kotlin { srcDirs("src/main/kotlin", layout.buildDirectory.dir("generated/src/main/kotlin")) }
    resources { srcDirs("src/resources") }
  }
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("fdrkpi-v1") {
  description =
    "Generates API interfaces and DTOs from OpenAPI specification for the FDR KPI service"
  group = "openapi tools"
  generatorName.set("spring")
  inputSpec.set("$rootDir/api-spec/v1/openapi.yaml")
  outputDir.set(
    layout.buildDirectory.dir("generated").get().asFile.absolutePath
  ) // buildDir is deprecated
  apiPackage.set("it.pagopa.generated.qi.fdrkpi.v1.api")
  modelPackage.set("it.pagopa.generated.qi.fdrkpi.v1.model")
  generateApiTests.set(false)
  generateApiDocumentation.set(false)
  generateApiTests.set(false)
  generateModelTests.set(false)
  library.set("spring-boot")
  modelNameSuffix.set("Dto")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "true",
      "interfaceOnly" to "true",
      "hideGenerationTimestamp" to "true",
      "skipDefaultInterface" to "true",
      "useSwaggerUI" to "false",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "oas3" to "true",
      "generateSupportingFiles" to "true",
      "enumPropertyNaming" to "UPPERCASE"
    )
  )
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<KotlinCompile> {
  dependsOn("fdrkpi-v1")
  kotlinOptions.jvmTarget = "21"
}

tasks.named("build") { dependsOn("spotlessApply") }

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
          exclude("it/pagopa/qi/fdrkpi/PagopaQiFdrKpiServiceApplicationKt.class")
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
