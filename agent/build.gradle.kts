plugins {
    kotlin("jvm").version(kotlinVersion)
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlinStdlib)
    implementation(kotlinxCoroutines)
    implementation(usb4Javax)
    implementation(config4k)
}
