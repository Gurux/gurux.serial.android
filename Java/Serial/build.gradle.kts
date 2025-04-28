plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "gurux.serial"
    compileSdk = 35

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.guruxCommonAndroid)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "org.gurux"
                artifactId = "gurux.serial.android"
                version = "2.0.11"

                pom {
                    name.set("gurux.serial.android")
                    description.set("gurux.serial.android package implements serial port for android OS. Purpose of Gurux Device Framework is help you to read your devices, meters and sensors easier")
                    url.set("https://www.gurux.fi")
                    licenses {
                        license {
                            name.set("GNU General Public License, version 2")
                            url.set("http://www.gnu.org/licenses/gpl-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("gurux")
                            name.set("Gurux ltd")
                            email.set("gurux@gurux.fi")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/gurux/gurux.serial.android.git")
                        developerConnection.set("scm:git:https://github.com/gurux/gurux.serial.android.git")
                        url.set("https://github.com/gurux/gurux.serial.android")
                    }
                }

                // Sign if credentials are available
                if (project.hasProperty("sonatypeUsername") && project.hasProperty("sonatypePassword")) {
                    signing {
                        sign(this@create)
                    }

                    repositories {
                        maven {
                            name = "MavenCentral"
                            url =
                                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                            credentials {
                                username = findProperty("sonatypeUsername") as String
                                password = findProperty("sonatypePassword") as String
                            }
                        }
                    }
                }
            }
        }
    }
}