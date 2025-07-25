plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.10"
}

android {
    namespace = "com.amanpathak.digipoint"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        
        // Library version
        buildConfigField("String", "SDK_VERSION", "\"0.1.0\"")
        buildConfigField("String", "SDK_NAME", "\"Digipin Android SDK\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// JitPack publishing configuration
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.itsamanpathak"
            artifactId = "DigiPointSDK"
            version = "0.1.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("DigiPoint Android SDK")
                description.set("Android SDK for India's Digital Postal Index Number (DIGIPIN) system - 4m×4m precision geo-coding")
                url.set("https://github.com/itsamanpathak/DigiPointSDK")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/itsamanpathak/DigiPointSDK.git")
                    developerConnection.set("scm:git:ssh://github.com/itsamanpathak/DigiPointSDK.git")
                    url.set("https://github.com/itsamanpathak/DigiPointSDK")
                }
            }
        }
    }
}