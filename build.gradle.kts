import com.diffplug.gradle.spotless.SpotlessApply
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  application
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.arrowGradleConfig.formatter)
  alias(libs.plugins.arrowGradleConfig.versioning)
  alias(libs.plugins.dokka)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kover)
  alias(libs.plugins.kotlin.binaryCompatibilityValidator)
}

application {
  mainClass.set("org.example.MainKt")
}

allprojects {
  group = "org.example"

  repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
  }

  listOf(
    libs.plugins.detekt,
    libs.plugins.kover,
    libs.plugins.kotlin.jvm,
    libs.plugins.arrowGradleConfig.formatter,
    libs.plugins.arrowGradleConfig.versioning,
    libs.plugins.kotest.multiplatform,
    libs.plugins.kotlin.binaryCompatibilityValidator
  ).forEach {
    plugins.apply(it.get().pluginId)
  }
  apply(plugin = "org.gradle.idea")

  // doc configurations
  extra.set("dokka.outputDirectory", rootDir.resolve("docs"))

  tasks.configureEach {
    if (name == "build") { // a few convenience tasks when running "build"
      dependsOn(tasks.withType<Detekt>())
      dependsOn(tasks.withType<SpotlessApply>())
      dependsOn(tasks.named("apiDump"))
    }
  }

  // static analysis tool
  detekt {
    parallel = true
    buildUponDefaultConfig = true
    allRules = true
  }

  spotless {
    val userData: Map<String, String> =
      mapOf(
        "indent_size" to "2",
        "disabled_rules" to "import-ordering, no-unit-return, curly-spacing",
        "tab_width" to "2",
        "ij_continuation_indent_size" to "2",
        "max_line_length" to "off"
      )
    kotlin {
      // spotless doesn't consider .editorconfig, so instead use a userData https://github.com/diffplug/spotless/tree/main/plugin-gradle#ktlint
      ktlint().userData(userData)
      endWithNewline()
    }
  }

  kotlin {
    explicitApi()
  }

  tasks {
    withType<KotlinCompile>().configureEach {
      kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xcontext-receivers"
      }
      sourceCompatibility = JavaVersion.VERSION_1_8.toString()
      targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    test {
      maxParallelForks = Runtime.getRuntime().availableProcessors()
      useJUnitPlatform()
      // test coverage tool
      extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        includes = listOf("org.example.*")
      }
      testLogging {
        setExceptionFormat("full")
        setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
      }
    }

    withType<Detekt>().configureEach {
      jvmTarget = "1.8"
      reports {
        html.required.set(true)
        sarif.required.set(true)
        txt.required.set(false)
        xml.required.set(false)
      }
    }

    withType<DetektCreateBaselineTask>().configureEach {
      jvmTarget = "1.8"
    }
  }

  dependencies {
    implementation(libs.arrow.fx)
    implementation(libs.coroutines.core)
    implementation(libs.kotlin.stdlibCommon)

    testImplementation(libs.kotest.frameworkEngine)
    testImplementation(libs.kotest.runnerJUnit5)
    testImplementation(libs.kotest.arrow)
  }
}

dependencies {
  implementation(libs.postgresql)
  implementation(libs.hikari)
  implementation(libs.coroutines.reactive)
  implementation(libs.coroutines.reactor)
  implementation(libs.spring.boot.starter.webflux)
  implementation(libs.reactor.kotlin.extensions)
  implementation(libs.netty.transport.native.kqueue)

  testImplementation(libs.testcontainers.postgresql)
}
