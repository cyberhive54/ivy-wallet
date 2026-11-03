plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.contributors"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.navigation)
    implementation(projects.temp.legacyCode)
    implementation(projects.temp.oldDesign)

    implementation(libs.bundles.ktor)

    testImplementation(projects.shared.ui.testing)
}
