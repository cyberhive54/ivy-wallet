package com.ivy.design.api.systems

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Look
import com.ivy.base.legacy.Theme
import com.ivy.design.api.IvyDesign
import com.ivy.design.l0_system.*
import com.ivy.ui.R

@Deprecated("Old design system. Use `:ivy-design` and Material3")
abstract class IvyWalletDesign : IvyDesign {
    companion object {
        const val OPEN_SANS_BASELINE_SHIFT = 0.075f
        const val RALEWAY_BASELINE_SHIFT = 0.2f
    }

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    override fun typography(): IvyTypography {
        val openSans = FontFamily(
            Font(R.font.opensans_regular, FontWeight.Normal),
            Font(R.font.opensans_regular, FontWeight.Medium),
            Font(R.font.opensans_bold, FontWeight.Black),
            Font(R.font.opensans_semibold, FontWeight.SemiBold),
            Font(R.font.opensans_bold, FontWeight.Bold),
            Font(R.font.opensans_extrabold, FontWeight.ExtraBold),
        )

        val raleWay = FontFamily(
            Font(R.font.raleway_regular, FontWeight.Normal),
            Font(R.font.raleway_medium, FontWeight.Medium),
            Font(R.font.raleway_black, FontWeight.Black),
            Font(R.font.raleway_light, FontWeight.Light),
            Font(R.font.raleway_semibold, FontWeight.SemiBold),
            Font(R.font.raleway_bold, FontWeight.Bold),
            Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
        )

        val h1 = 40.sp
        val h2 = 32.sp
        val b1 = 20.sp
        val b2 = 16.sp
        val c = 12.sp

        return object : IvyTypography {
            override val h1 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.Black,
                fontSize = h1,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val h2 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.ExtraBold,
                fontSize = h2,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val b1 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.Bold,
                fontSize = b1,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val b2 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.Medium,
                fontSize = b2,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val c = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.ExtraBold,
                fontSize = c,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )

            override val nH1 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = h1,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nH2 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = h2,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nB1 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = b1,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nB2 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Normal,
                fontSize = b2,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nC = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = c,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
        }
    }

    // -----------------------------------------------------------------------------------------
    // Materials (the Look axis). Each material maps its paper, ink and accent posture into
    // the same IvyColors tokens; the mode axis (Theme) only decides light / dark / AMOLED.
    // -----------------------------------------------------------------------------------------

    // Forest - the field notebook
    private val ForestPaper = Color(0xFFF7F9F3)
    private val ForestInk = Color(0xFF1B231C)
    private val ForestSage = Color(0xFF7A8278)
    private val ForestCard = Color(0xFFECF1E4)
    private val ForestChip = Color(0xFF26302A)
    private val ForestPrimary = Color(0xFF178563)
    private val ForestPrimaryDeep = Color(0xFF0F5C44)
    private val ForestNight = Color(0xFF0E1512)
    private val ForestNightCard = Color(0xFF182119)
    private val ForestNightInk = Color(0xFFE8EFE6)
    private val ForestNightChip = Color(0xFFD3DECB)
    private val ForestPrimaryPale = Color(0xFF8FD9BC)
    private val ForestAmoledCard = Color(0xFF121813)

    // Graphite - the instrument
    private val GraphitePaper = Color(0xFFF3F3F5)
    private val GraphiteInk = Color(0xFF17181A)
    private val GraphiteCard = Color(0xFFE9E9EC)
    private val GraphiteChip = Color(0xFF2C2C31)
    private val GraphitePrimary = Color(0xFF2F2F35)
    private val GraphitePrimary1 = Color(0xFF5B5B63)
    private val GraphiteNight = Color(0xFF131315)
    private val GraphiteNightCard = Color(0xFF1D1D20)
    private val GraphiteNightInk = Color(0xFFEDEDEF)
    private val GraphiteNightChip = Color(0xFFDEDEE1)
    private val GraphiteNightPrimary = Color(0xFF3D3D44)
    private val GraphiteNightPrimary1 = Color(0xFF78787F)
    private val GraphiteAmoledCard = Color(0xFF0F0F11)

    // Glass - the window. Cards carry alpha so surfaces read as panes over the wash.
    private val GlassPaper = Color(0xFFEDF0F6)
    private val GlassInk = Color(0xFF1A1D26)
    private val GlassSlate = Color(0xFF8A8F9E)
    private val GlassCard = Color(0xCCFFFFFF)
    private val GlassChip = Color(0xFF2A2E3A)
    private val GlassPrimary = Color(0xFF0A6BD8)
    private val GlassPrimaryDeep = Color(0xFF084B99)
    private val GlassNight = Color(0xFF0C1017)
    private val GlassNightCard = Color(0xCC1A2230)
    private val GlassNightInk = Color(0xFFE8ECF4)
    private val GlassNightChip = Color(0xFFDCE2EE)
    private val GlassPrimaryNight = Color(0xFF0A84FF)
    private val GlassPrimaryPale = Color(0xFF9ECDFF)
    private val GlassAmoledCard = Color(0xCC151A24)

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    override fun colors(theme: Theme, isDarkModeEnabled: Boolean, look: Look): IvyColors {
        return when (look) {
            Look.IVY -> ivyPalette(theme, isDarkModeEnabled, look)
            Look.FOREST -> forestPalette(theme, isDarkModeEnabled)
            Look.GRAPHITE -> graphitePalette(theme, isDarkModeEnabled)
            Look.GLASS -> glassPalette(theme, isDarkModeEnabled)
        }
    }

    private fun ivyPalette(theme: Theme, isDarkModeEnabled: Boolean, look: Look): IvyColors {
        return when (theme) {
            Theme.LIGHT -> ivyColorsOf(
                look = look,
                isLight = true,
                pure = White,
                pureInverse = Black,
                gray = Gray,
                medium = MediumWhite,
                mediumInverse = MediumBlack,
                primary = Purple,
                primary1 = IvyDark,
                green = Green,
                green1 = GreenLight,
                orange = Orange,
                orange1 = OrangeLight,
                red = Red,
                red1 = RedLight,
                red1Inverse = RedDark,
            )

            Theme.DARK -> ivyColorsOf(
                look = look,
                isLight = false,
                pure = Black,
                pureInverse = White,
                gray = Gray,
                medium = MediumBlack,
                mediumInverse = MediumWhite,
                primary = Purple,
                primary1 = IvyLight,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AMOLED_DARK -> ivyColorsOf(
                look = look,
                isLight = false,
                pure = TrueBlack,
                pureInverse = White,
                gray = Gray,
                medium = MediumBlack,
                mediumInverse = MediumWhite,
                primary = Purple,
                primary1 = IvyLight,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AUTO -> if (isDarkModeEnabled) {
                ivyPalette(Theme.DARK, true, look)
            } else {
                ivyPalette(Theme.LIGHT, false, look)
            }
        }
    }

    private fun forestPalette(theme: Theme, isDarkModeEnabled: Boolean): IvyColors {
        return when (theme) {
            Theme.LIGHT -> ivyColorsOf(
                look = Look.FOREST,
                isLight = true,
                pure = ForestPaper,
                pureInverse = ForestInk,
                gray = ForestSage,
                medium = ForestCard,
                mediumInverse = ForestChip,
                primary = ForestPrimary,
                primary1 = ForestPrimaryDeep,
                green = Green,
                green1 = GreenLight,
                orange = Orange,
                orange1 = OrangeLight,
                red = Red,
                red1 = RedLight,
                red1Inverse = RedDark,
            )

            Theme.DARK -> ivyColorsOf(
                look = Look.FOREST,
                isLight = false,
                pure = ForestNight,
                pureInverse = ForestNightInk,
                gray = ForestSage,
                medium = ForestNightCard,
                mediumInverse = ForestNightChip,
                primary = ForestPrimary,
                primary1 = ForestPrimaryPale,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AMOLED_DARK -> ivyColorsOf(
                look = Look.FOREST,
                isLight = false,
                pure = TrueBlack,
                pureInverse = ForestNightInk,
                gray = ForestSage,
                medium = ForestAmoledCard,
                mediumInverse = ForestNightChip,
                primary = ForestPrimary,
                primary1 = ForestPrimaryPale,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AUTO -> if (isDarkModeEnabled) {
                forestPalette(Theme.DARK, true)
            } else {
                forestPalette(Theme.LIGHT, false)
            }
        }
    }

    private fun graphitePalette(theme: Theme, isDarkModeEnabled: Boolean): IvyColors {
        return when (theme) {
            Theme.LIGHT -> ivyColorsOf(
                look = Look.GRAPHITE,
                isLight = true,
                pure = GraphitePaper,
                pureInverse = GraphiteInk,
                gray = Gray,
                medium = GraphiteCard,
                mediumInverse = GraphiteChip,
                primary = GraphitePrimary,
                primary1 = GraphitePrimary1,
                green = Green,
                green1 = GreenLight,
                orange = Orange,
                orange1 = OrangeLight,
                red = Red,
                red1 = RedLight,
                red1Inverse = RedDark,
            )

            Theme.DARK -> ivyColorsOf(
                look = Look.GRAPHITE,
                isLight = false,
                pure = GraphiteNight,
                pureInverse = GraphiteNightInk,
                gray = Gray,
                medium = GraphiteNightCard,
                mediumInverse = GraphiteNightChip,
                primary = GraphiteNightPrimary,
                primary1 = GraphiteNightPrimary1,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AMOLED_DARK -> ivyColorsOf(
                look = Look.GRAPHITE,
                isLight = false,
                pure = TrueBlack,
                pureInverse = GraphiteNightInk,
                gray = Gray,
                medium = GraphiteAmoledCard,
                mediumInverse = GraphiteNightChip,
                primary = GraphiteNightPrimary,
                primary1 = GraphiteNightPrimary1,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AUTO -> if (isDarkModeEnabled) {
                graphitePalette(Theme.DARK, true)
            } else {
                graphitePalette(Theme.LIGHT, false)
            }
        }
    }

    private fun glassPalette(theme: Theme, isDarkModeEnabled: Boolean): IvyColors {
        return when (theme) {
            Theme.LIGHT -> ivyColorsOf(
                look = Look.GLASS,
                isLight = true,
                pure = GlassPaper,
                pureInverse = GlassInk,
                gray = GlassSlate,
                medium = GlassCard,
                mediumInverse = GlassChip,
                primary = GlassPrimary,
                primary1 = GlassPrimaryDeep,
                green = Green,
                green1 = GreenLight,
                orange = Orange,
                orange1 = OrangeLight,
                red = Red,
                red1 = RedLight,
                red1Inverse = RedDark,
            )

            Theme.DARK -> ivyColorsOf(
                look = Look.GLASS,
                isLight = false,
                pure = GlassNight,
                pureInverse = GlassNightInk,
                gray = GlassSlate,
                medium = GlassNightCard,
                mediumInverse = GlassNightChip,
                primary = GlassPrimaryNight,
                primary1 = GlassPrimaryPale,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AMOLED_DARK -> ivyColorsOf(
                look = Look.GLASS,
                isLight = false,
                pure = TrueBlack,
                pureInverse = GlassNightInk,
                gray = GlassSlate,
                medium = GlassAmoledCard,
                mediumInverse = GlassNightChip,
                primary = GlassPrimaryNight,
                primary1 = GlassPrimaryPale,
                green = Green,
                green1 = GreenDark,
                orange = Orange,
                orange1 = OrangeDark,
                red = Red,
                red1 = RedDark,
                red1Inverse = RedLight,
            )

            Theme.AUTO -> if (isDarkModeEnabled) {
                glassPalette(Theme.DARK, true)
            } else {
                glassPalette(Theme.LIGHT, false)
            }
        }
    }

    @Suppress("LongParameterList")
    private fun ivyColorsOf(
        look: Look,
        isLight: Boolean,
        pure: Color,
        pureInverse: Color,
        gray: Color,
        medium: Color,
        mediumInverse: Color,
        primary: Color,
        primary1: Color,
        green: Color,
        green1: Color,
        orange: Color,
        orange1: Color,
        red: Color,
        red1: Color,
        red1Inverse: Color,
    ): IvyColors {
        return object : IvyColors {
            override val pure = pure
            override val pureInverse = pureInverse
            override val gray = gray
            override val medium = medium
            override val mediumInverse = mediumInverse

            override val primary = primary
            override val primary1 = primary1

            override val green = green
            override val green1 = green1

            override val orange = orange
            override val orange1 = orange1

            override val red = red
            override val red1 = red1
            override val red1Inverse = red1Inverse

            override val look = look
            override val isLight = isLight
        }
    }

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    override fun shapes(): IvyShapes {
        return object : IvyShapes() {
            override val r1 = RoundedCornerShape(32.dp)
            override val r1Top = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            override val r1Bot = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

            override val r2 = RoundedCornerShape(24.dp)
            override val r2Top = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            override val r2Bot = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

            override val r3 = RoundedCornerShape(20.dp)
            override val r3Top = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            override val r3Bot = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

            override val r4 = RoundedCornerShape(16.dp)
            override val r4Top = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            override val r4Bot = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        }
    }
}
