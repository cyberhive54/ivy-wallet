package com.ivy.design.l0_system

import androidx.compose.ui.graphics.Color
import com.ivy.base.legacy.Look

@Deprecated("Old design system. Use `:ivy-design` and Material3")
interface IvyColors {
    val pure: Color
    val pureInverse: Color

    val gray: Color
    val medium: Color
    val mediumInverse: Color

    val primary: Color
    val primary1: Color

    val green: Color
    val green1: Color

    val orange: Color
    val orange1: Color

    val red: Color
    val red1: Color
    val red1Inverse: Color

    /** The material these colors belong to. Lets components branch for glass / graphite. */
    val look: Look

    val isLight: Boolean
}
