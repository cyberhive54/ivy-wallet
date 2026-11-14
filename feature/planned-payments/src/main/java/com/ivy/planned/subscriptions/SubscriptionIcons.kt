package com.ivy.planned.subscriptions

import com.ivy.ui.R

/**
 * Keys stored in PlannedPaymentRuleEntity.icon → glyph.
 * Keep in sync between the board rows and the editor's picker grid.
 */
internal val SUBSCRIPTION_ICONS: Map<String, Int> = mapOf(
    "film" to R.drawable.ic_vue_media_film_play,
    "music" to R.drawable.ic_vue_media_music,
    "sports" to R.drawable.ic_custom_sports_m,
    "shopping" to R.drawable.ic_vue_shop_cart,
    "cloud" to R.drawable.ic_vue_files_folder_cloud,
    "games" to R.drawable.ic_custom_game_m,
    "news" to R.drawable.ic_vue_edu_book,
    "food" to R.drawable.ic_custom_coffee_m,
    "car" to R.drawable.ic_vue_transport_car,
    "home" to R.drawable.ic_vue_main_home,
    "apps" to R.drawable.ic_vue_brands_google_play,
    "wifi" to R.drawable.ic_vue_pc_wifi,
    "energy" to R.drawable.ic_vue_main_flash,
)

/** Stable order for the picker grid. */
internal val SUBSCRIPTION_ICON_KEYS: List<String> = listOf(
    "film", "music", "sports", "shopping", "cloud", "games", "news",
    "food", "car", "home", "apps", "wifi", "energy",
)
