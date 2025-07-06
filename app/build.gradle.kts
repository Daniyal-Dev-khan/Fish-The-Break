plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("androidx.room")
}
room {
    schemaDirectory("$projectDir/schemas")
}
android {
    namespace = "com.cp.fishthebreak"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fishthebreak.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "Fish The Break-v $versionCode($versionName)")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {

        dataBinding = true

        // for view binding:
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    //firebase
    implementation("com.google.firebase:firebase-crashlytics:18.6.4")
    implementation("com.google.firebase:firebase-analytics:21.6.2")
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //hilt
    implementation("com.google.dagger:hilt-android:2.49")
    kapt("com.google.dagger:hilt-android-compiler:2.49")
    implementation("androidx.hilt:hilt-work:1.2.0")
    // When using Kotlin.
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //Room Db
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$2.6.1")


    //ssp library
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    //sdp library
    implementation("com.intuit.sdp:sdp-android:1.1.0")

    //Alerter
    implementation("com.github.tapadoo:alerter:7.2.4")
    //Map
//    implementation("com.esri:arcgis-maps-kotlin:200.2.0")
    implementation("com.esri.arcgisruntime:arcgis-android:100.15.5")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.15.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.0")
    // for app permissions
    implementation("com.karumi:dexter:6.2.3")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.android.gms:play-services-auth:21.1.0")

    implementation("com.google.android.gms:play-services-location:21.2.0")

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("com.github.cachapa:ExpandableLayout:2.9.2")
    //Time ago api
    api("org.ocpsoft.prettytime:prettytime:5.0.4.Final")

    implementation("com.android.billingclient:billing-ktx:6.2.1")

    implementation("com.hbb20:ccp:2.7.0")

    implementation("com.facebook.android:facebook-login:17.0.0")
}
// Allow references to generated code
kapt {
    correctErrorTypes = true
}