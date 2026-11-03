package com.ivy.base.legacy

import androidx.compose.runtime.Immutable

/**
 * The user-chosen accent, orthogonal to [Look] (the material axis) and [Theme] (the mode axis).
 *
 * [DEFAULT] keeps each material's signature accent (Ivy purple, Forest leaf, Graphite ink,
 * Glass blue). Every other value globally remaps the primary token family across all
 * materials, which drives the liquid dock pill, switches in their on state, and every
 * future selection surface.
 */
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Immutable
enum class Accent(val code: String) {
    /** Follow the active [Look]'s signature accent. */
    DEFAULT("default"),

    /** The classic Ivy purple, forced across all materials. */
    IVY("ivy"),

    /** Apple-family green. */
    GREEN("green"),

    /** Apple-family blue. */
    BLUE("blue"),

    /** Apple-family orange. */
    ORANGE("orange"),

    /** Apple-family pink. */
    PINK("pink");

    companion object {
        fun parse(value: String?): Accent? =
            entries.firstOrNull { it.code.equals(value ?: "", ignoreCase = true) }
    }
}
