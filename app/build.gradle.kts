plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id ("kotlin-parcelize")
    kotlin("plugin.serialization") version "1.9.23"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.gigzz.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gigzz.android"
        minSdk = 28
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    composeOptions{
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.sdp.android)

    //data store
    implementation(libs.androidx.datastore.preferences)
    implementation (libs.kotlinx.collections.immutable)
    implementation (libs.kotlinx.serialization.json)

    //okHttp
    //implementation(libs.okhttp.bom)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //aws sdk
    implementation(libs.aws.android.sdk.s3)
    implementation(libs.aws.android.sdk.cognito)
    implementation(libs.aws.android.sdk.cognitoidentityprovider)

    //coroutine
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    //retrofit-gson
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //nav graph
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation (libs.androidx.hilt.navigation.fragment)

    //viewModel & livedata
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
//    ksp(libs.hilt.android.compiler)

    //Glide
    implementation(libs.glide)
    implementation(libs.core.ktx)

    // Firebase Analytics and messaging
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    //permission library
    implementation(libs.dexter)

    //firebase
    implementation(platform(libs.firebase.bom))

    //Serialization
    implementation(libs.kotlinx.serialization.json.v160)

    // Socket.IO
    implementation(libs.okhttp.v500alpha6)
    implementation(libs.socket.io.client)

    //Multiple imagePicker
    implementation (libs.ucrop)

    //ccp
    implementation (libs.ccp)

    //swipe refresh
    implementation (libs.androidx.swiperefreshlayout)

    //otpView
    implementation("com.github.aabhasr1:OtpView:v1.1.2-ktx")

    implementation (libs.exoplayer.core)
   // implementation (libs.exoplayer.dash)
    implementation (libs.exoplayer.ui)

    //emoji Library
    implementation (libs.androidx.emoji2.emojipicker)

    //Flexbox
    implementation (libs.flexbox)


    //places google
    implementation (libs.places)
    // Places SDK for Android KTX Library
    implementation (libs.places.ktx)

    //imagePicker
    implementation("com.github.Drjacky:ImagePicker:2.3.22")
}