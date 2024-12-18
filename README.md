# PagoPA QI FDR KPI Service

This repository is designed to manage and process Key Performance Indicators (KPIs) related to FDR (Flusso di Rendicontazione) within the PagoPA ecosystem. The service calculates and monitors various performance metrics to ensure efficient processing and reporting of FDR data.

- [PagoPA QI FDR KPI Service](#pagopa-qi-fdr-kpi-service)
    * [Api Documentation 📖](#api-documentation-)
    * [Technology Stack 🛠️](#technology-stack-)
    * [Platform Compatibility ⚙️](#platform-compatibility-)
    * [Working with Windows 🪟](#working-with-windows-)
    * [Start Project Locally 🚀](#start-project-locally-)
        + [Prerequisites](#prerequisites)
        + [Populate the environment](#populate-the-environment)
        + [Run docker container](#run-docker-container)
    * [Develop Locally 💻](#develop-locally-)
        + [Prerequisites](#prerequisites-1)
        + [Run the project](#run-the-project)
        + [Testing 🧪](#testing-)
            - [Unit testing](#unit-testing)
    * [Dependency management 🔧](#dependency-management-)
        + [Dependency lock](#dependency-lock)
        + [Dependency verification](#dependency-verification)
    * [Contributors 👥](#contributors-)
        + [Maintainers](#maintainers)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>

---

## Api Documentation 📖

See the [OpenAPI 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-qi-fdr-kpi-service/main/api-spec/v1/openapi.yaml)

---

## Technology Stack 🛠️

- Kotlin
- Spring Boot

---

## Platform Compatibility ⚙️

**Important Note:** This project is currently only compatible with AMD64/x86_64 processor architecture. It will not run on:
- ARM-based processors
- ARM64 architecture

This limitation is due to the Docker images used in the project not supporting ARM architecture. We are working on adding support for multiple architectures in future releases.

If you are using a machine with an ARM processor, you will need to either:
- Use a different machine with an AMD64 processor
- Set up a remote development environment on an AMD64 machine

## Working with Windows 🪟

If you are developing on Windows, it is recommended the use of WSL2 combined with IntelliJ IDEA.

The IDE should be installed on Windows, with the repository cloned into a folder in WSL2. All the necessary tools will be installed in the Linux distro of your choice.

You can find more info on how to set up the environment following the link below.

https://www.jetbrains.com/help/idea/how-to-use-wsl-development-environment-in-product.html

After setting up the WSL environment, you can test the application by building it through either Docker or Spring Boot (useful for local development).


## Start Project Locally 🚀

### Prerequisites

- docker

### Populate the environment

The microservice needs a valid `.env` file in order to be run.

If you want to start the application without too much hassle, you can just copy `.env.local` with

```shell
$ cp .env.local .env
```

to get a good default configuration.

If you want to customize the application environment, reference this table:


| Variable name                                | Description                                                              | type         | default |
|---------------------------------------------|--------------------------------------------------------------------------|--------------|---------|
| AZURE_DATA_EXPLORER_RE_ENDPOINT              | Azure Data Explorer endpoint URL for the resource                        | URL (string) |         |
| AZURE_DATA_EXPLORER_RE_CLIENT_ID             | Client ID for Azure Data Explorer authentication                         | string       |         |
| AZURE_DATA_EXPLORER_RE_APPLICATION_KEY       | Application key/secret for Azure Data Explorer authentication            | string       |         |
| AZURE_DATA_EXPLORER_RE_APPLICATION_TENANT_ID | Azure tenant ID for Azure Data Explorer authentication                   | string       |         |

### Run docker container

```shell
$ docker compose up --build
```

---

## Develop Locally 💻

### Prerequisites

- git
- gradle
- jdk-21

### Run the project

```shell
export $(grep -v '^#' .env.local | xargs)$ 
$ ./gradlew bootRun
```

### Testing 🧪

#### Unit testing

To run the **Junit** tests:

```shell
$ ./gradlew test
```

### Dependency management 🔧

To support reproducible builds, this project has the following Gradle features enabled:

- [dependency lock](https://docs.gradle.org/8.1/userguide/dependency_locking.html)
- [dependency verification](https://docs.gradle.org/8.1/userguide/dependency_verification.html)

#### Dependency lock

This feature uses the content of `gradle.lockfile` to check the declared dependencies against the locked ones.

If a transitive dependency has been upgraded, the build will fail because of the locked version mismatch.

The following command can be used to upgrade dependency lockfile:

```shell
./gradlew dependencies --write-locks 
```

Running the above command will cause the `gradle.lockfile` to be updated against the current project dependency configuration.

#### Dependency verification
This feature is enabled by adding the Gradle `./gradle/verification-metadata.xml` configuration file.
It performs checksum comparisons against dependency artifacts (jar files, zip, etc.) and metadata (pom.xml, gradle module
metadata, etc.) used during build against the ones stored in the `verification-metadata.xml` file, raising errors during build if there are mismatches.

The following command can be used to recalculate dependency checksums:
```shell
./gradlew --write-verification-metadata sha256 clean spotlessApply build --no-build-cache --refresh-dependencies
```

In the above command, the `clean`, `spotlessApply`, and `build` tasks were chosen to run
in order to discover all transitive dependencies used during build, including those used by the
spotless apply task for source code formatting.

The command will upgrade the `verification-metadata.xml` by adding all newly discovered dependency checksums.
These checksums should be verified against trusted sources to confirm they match the checksums published by the library authors.

Note that the `./gradlew --write-verification-metadata sha256` command appends all new dependencies to the verification files but does
not remove entries for unused dependencies.
This can cause the file to grow each time a dependency is upgraded.

To detect and remove old dependencies, follow these steps:
1. Delete the `gradle/verification-metadata.dryrun.xml` file if it exists
2. Run Gradle write-verification-metadata in dry-mode (this will generate a verification-metadata-dryrun.xml file
   while leaving the original verification file untouched)
3. Compare the verification-metadata file with the verification-metadata.dryrun file to identify differences and remove
   old unused dependencies

You can perform steps 1-2 with these commands:
```shell
rm -f ./gradle/verification-metadata.dryrun.xml 
./gradlew --write-verification-metadata sha256 clean spotlessApply build --dry-run
```

The resulting `verification-metadata.xml` modifications must be reviewed carefully by checking the generated
dependency checksums against official websites or other secure sources.

If a dependency is not discovered during command execution, it will cause build errors.
You can manually add such dependencies by modifying the `verification-metadata.xml`
file with the following component structure:

```xml
<verification-metadata>
    <!-- other configurations... -->
    <components>
        <!-- other components -->
        <component group="GROUP_ID" name="ARTIFACT_ID" version="VERSION">
            <artifact name="artifact-full-name.jar">
                <sha256 value="sha value"
                        origin="Description of the source of the checksum value"/>
            </artifact>
            <artifact name="artifact-pom-file.pom">
                <sha256 value="sha value"
                        origin="Description of the source of the checksum value"/>
            </artifact>
        </component>
    </components>
</verification-metadata>
```

After adding components at the end of the components list, run:
```shell
./gradlew --write-verification-metadata sha256 clean spotlessApply build --no-build-cache --refresh-dependencies
```

This will reorder the file with the added dependency checksums in the expected order.

Finally, you can add new dependencies to both gradle.lockfile and verification metadata by running:
```shell
./gradlew dependencies --write-locks --write-verification-metadata sha256 --no-build-cache --refresh-dependencies
```

For more information, read the
following [article](https://docs.gradle.org/8.1/userguide/dependency_verification.html#sec:checksum-verification)

## Contributors 👥

Made with ❤️ by PagoPA S.p.A.

### Maintainers

See `CODEOWNERS` file