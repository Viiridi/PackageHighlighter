plugins {
    kotlin("android") version "1.9.10" apply false
    id("com.android.application") version "8.6.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
