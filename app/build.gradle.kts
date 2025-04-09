plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.wuyeapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wuyeapp"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    // 添加 ViewPager2 和 GridLayout 的依赖
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.google.zxing:core:3.4.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation("org.mockito:mockito-core:4.0.0")
    // 修改为旧版MySQL JDBC驱动
    implementation("mysql:mysql-connector-java:5.1.45")
    // Retrofit库
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
}
