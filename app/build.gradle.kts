plugins { id("com.android.application") }
android {
    namespace = "com.ebs.tarayici"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.ebs.tarayici"
        minSdk = 26
        targetSdk = 28
        versionCode = 2
        versionName = "2.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
// Eski AndroidX kitapliklarinin getirdigi Kotlin 1.7.20 jdk7/jdk8
// kutuphanelerini Kotlin 1.8.10 ile esitleyerek Duplicate class hatasini onler.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" &&
            requested.name in listOf("kotlin-stdlib", "kotlin-stdlib-common", "kotlin-stdlib-jdk7", "kotlin-stdlib-jdk8")) {
            useVersion("1.8.10")
            because("Avoid duplicate Kotlin stdlib classes from mismatched 1.7.x and 1.8.x dependencies")
        }
    }
}

dependencies {
    implementation("androidx.work:work-runtime:2.8.1")
    implementation("androidx.core:core:1.10.1")
    implementation("androidx.activity:activity:1.7.2")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
}
