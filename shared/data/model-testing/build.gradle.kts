plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.data.model.testing"
}

dependencies {
    implementation(projects.shared.data.model)
    // The domain model exposes `com.ivy.base.model.Necessity` on Expense/Income,
    // so the base module must be on the compile classpath.
    implementation(projects.shared.base)

    implementation(libs.bundles.testing)
}