package com.ivy.base.legacy

import androidx.compose.runtime.Immutable

/**
 * The material axis of the app's appearance, orthogonal to [Theme] (the mode axis).
 *
 * A [Look] defines the paper (surfaces), the ink, and the accent posture of the whole app,
 * while [Theme] only decides light / dark / AMOLED rendering. Every [Look] supplies both a
 * light and a dark palette; AMOLED applies on top of the dark one.
 */
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Immutable
enum class Look {
    /** The classic Ivy identity - the reference material. */
    IVY,

    /** The field notebook - leaf-tinted paper, pine-dark surfaces, bottle-leaf accent. */
    FOREST,

    /** The instrument - all graphite, chroma reserved for meaning. */
    GRAPHITE,

    /** Cupertino liquid glass - translucent surfaces over a tinted wash. Not applied yet. */
    GLASS;

    companion object {
        fun parse(value: String?): Look? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
