package com.ivy.settings

/**
 * Appearance options for the redesigned Settings screen.
 *
 * [MOCK_THEMES] are visual presets that are persisted but not applied yet -
 * the actual theming will be wired in a follow-up release.
 * [ACCENTS] are accent color choices, also persisted-only for now.
 */
object SettingsAppearance {
    const val MOCK_THEME_IVY = "ivy"
    const val MOCK_THEME_FOREST = "forest"
    const val MOCK_THEME_GLASS = "glass"
    const val MOCK_THEME_GRAPHITE = "graphite"

    val MOCK_THEMES = listOf(
        MOCK_THEME_IVY,
        MOCK_THEME_FOREST,
        MOCK_THEME_GLASS,
        MOCK_THEME_GRAPHITE
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
