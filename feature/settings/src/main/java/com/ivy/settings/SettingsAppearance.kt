package com.ivy.settings

/**
 * Appearance options for the redesigned Settings screen.
 *
 * [LOOKS] are the material presets (the Look axis) that are actually applied at runtime:
 * each selection persists [com.ivy.base.legacy.SharedPrefs.SETTINGS_LOOK] and switches
 * the app-wide palette through [com.ivy.base.legacy.IvyContext.switchLook].
 * [LOOK_GLASS] ships with the translucent-surface pass and is hidden from the picker until then.
 *
 * [ACCENTS] are accent color choices, persisted for a follow-up release.
 */
object SettingsAppearance {
    const val LOOK_IVY = "ivy"
    const val LOOK_FOREST = "forest"
    const val LOOK_GLASS = "glass"
    const val LOOK_GRAPHITE = "graphite"

    val LOOKS = listOf(
        LOOK_IVY,
        LOOK_FOREST,
        LOOK_GRAPHITE
    )

    const val ACCENT_IVY = "ivy"
    const val ACCENT_GREEN = "green"
    const val ACCENT_BLUE = "blue"
    const val ACCENT_ORANGE = "orange"
    const val ACCENT_PINK = "pink"

    val ACCENTS = listOf(
        ACCENT_IVY,
        ACCENT_GREEN,
        ACCENT_BLUE,
        ACCENT_ORANGE,
        ACCENT_PINK
    )
}
