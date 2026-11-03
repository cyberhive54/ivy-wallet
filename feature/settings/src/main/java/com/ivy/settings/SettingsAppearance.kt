package com.ivy.settings

/**
 * Appearance options for the redesigned Settings screen.
 *
 * [LOOKS] are the material presets (the Look axis) that are actually applied at runtime:
 * each selection persists [com.ivy.base.legacy.SharedPrefs.SETTINGS_LOOK] and switches
 * the app-wide palette through [com.ivy.base.legacy.IvyContext.switchLook].
 * GLASS renders translucent surfaces with window blur (API 31+).
 *
 * [ACCENTS] are accent color choices applied at runtime: each selection persists
 * [com.ivy.base.legacy.SharedPrefs.SETTINGS_ACCENT_COLOR] and switches the app-wide primary
 * token family through [com.ivy.base.legacy.IvyContext.switchAccent] - the dock pill,
 * switches in their on state, and future selection surfaces all follow it.
 * [ACCENT_DEFAULT] keeps each material's signature accent.
 */
object SettingsAppearance {
    const val LOOK_IVY = "ivy"
    const val LOOK_FOREST = "forest"
    const val LOOK_GLASS = "glass"
    const val LOOK_GRAPHITE = "graphite"

    val LOOKS = listOf(
        LOOK_IVY,
        LOOK_FOREST,
        LOOK_GLASS,
        LOOK_GRAPHITE
    )

    const val ACCENT_DEFAULT = "default"
    const val ACCENT_IVY = "ivy"
    const val ACCENT_GREEN = "green"
    const val ACCENT_BLUE = "blue"
    const val ACCENT_ORANGE = "orange"
    const val ACCENT_PINK = "pink"

    val ACCENTS = listOf(
        ACCENT_DEFAULT,
        ACCENT_IVY,
        ACCENT_GREEN,
        ACCENT_BLUE,
        ACCENT_ORANGE,
        ACCENT_PINK
    )
}
