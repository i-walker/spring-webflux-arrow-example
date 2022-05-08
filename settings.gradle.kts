enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "spring-webflux-arrow-example"

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("libs.versions.toml"))
    }
  }

  repositories {
    mavenCentral()
  }
}

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.enterprise") version "3.10"
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}
