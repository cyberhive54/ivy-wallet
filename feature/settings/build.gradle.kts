plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.settings"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.core)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.navigation)
    implementation(projects.temp.legacyCode)
    implementation(projects.temp.oldDesign)
    implementation(projects.widget.balance)

    // In-tab settings child pages (dock-aware hosting)
    implementation(projects.feature.attributions)
    implementation(projects.feature.contributors)
    implementation(projects.feature.exchangeRates)
    implementation(projects.feature.features)
    implementation(projects.feature.importData)
    implementation(projects.feature.releases)

    testImplementation(projects.shared.ui.testing)
}
