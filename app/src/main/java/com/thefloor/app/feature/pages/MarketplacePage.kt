package com.thefloor.app.feature.pages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.thefloor.app.R
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorSectionHeader

internal val marketplaceCategories = listOf(
    "Electronics", "Food", "Travel", "Clothing", "Education",
    "Wellness", "Insurance", "Financial", "Entertainment", "Lifestyle",
)

internal val lifestyleSubcategories = listOf(
    "Groceries", "Transportation", "Travel", "Fitness", "Beauty & Personal Care",
    "Home & Living", "Entertainment", "Dining", "USA & UK Specials",
)

internal data class Deal(val brand: String, val title: String, val desc: String, val image: Int?)

internal val dealsByCategory: Map<String, List<Deal>> = mapOf(
    "Electronics" to listOf(
        Deal("Illustration partner", "Noise-cancelling headsets", "The headset is the tool. Member pricing on the ones that survive a full shift.", null),
        Deal("Illustration partner", "Home-office bundles", "Webcam, ring light and a chair that does not end your back.", null),
    ),
    "Food" to listOf(
        Deal("Illustration partner", "Shift-hour delivery", "Discounts that apply at 3am, not only at lunchtime.", null),
        Deal("Illustration partner", "Canteen top-ups", "Prepaid meal credit for on-site teams.", null),
    ),
    "Travel" to listOf(
        Deal("Illustration partner", "Flight Deals", "Get home for the holidays for less — member fares across regions.", R.drawable.img_flight_deals),
        Deal("Illustration partner", "Hotel Stays", "Rest days done properly, at member rates.", R.drawable.img_hotel_stays),
        Deal("Illustration partner", "Fly Anywhere", "Compare and book the whole trip in one place.", R.drawable.img_fly_anywhere),
    ),
    "Clothing" to listOf(
        Deal("Illustration partner", "Workwear that lasts", "Smart-casual basics for on-site floors.", null),
    ),
    "Education" to listOf(
        Deal("Illustration partner", "Paid course credit", "Discounted seats on paid professional courses.", null),
    ),
    "Wellness" to listOf(
        Deal("Illustration partner", "Calm Premium", "Sleep, focus and wind-down tools built for rotating shifts.", R.drawable.img_calm_premium),
        Deal("Illustration partner", "BetterUp Coaching", "One-to-one coaching for stress, confidence and career direction.", R.drawable.img_betterup_coaching),
    ),
    "Insurance" to listOf(
        Deal("Illustration partner", "Health Insurance", "Cover that works around shift patterns.", R.drawable.img_health_insurance),
        Deal("Illustration partner", "Life Cover", "Straightforward protection for the people who depend on you.", R.drawable.img_life_cover),
        Deal("Illustration partner", "Funeral Cover", "Cover that families in several Floor regions ask for first.", R.drawable.img_funeral_cover),
    ),
    "Financial" to listOf(
        Deal("Illustration partner", "Invest & Save", "Put shift pay to work without gambling it.", R.drawable.img_invest_save),
        Deal("Illustration partner", "Budget & Save Like a Pro", "Make shift pay stretch — plan, save, invest, grow.", R.drawable.img_budget_save_like_a_pro),
    ),
    "Entertainment" to listOf(
        Deal("Illustration partner", "Experiences", "Things worth doing on your days off.", R.drawable.img_experiences),
    ),
)

@Composable
fun MarketplaceScreen(onBack: () -> Unit) {
    var category by remember { mutableStateOf(marketplaceCategories.first()) }
    var sub by remember { mutableStateOf(lifestyleSubcategories.first()) }

    EditorialScaffold("Marketplace", onBack) {
        item {
            FloorHero(
                eyebrow = "Member commerce · 10 categories",
                title = "Marketplace",
                subtitle = "Member deals around the BPO lifestyle — the gear, cover, travel and " +
                    "services people on the floor actually use.",
            )
        }
        item {
            PlaceholderNote(
                "Every brand shown here is a prospective affiliate partner The Floor intends to " +
                    "onboard — not a confirmed live partnership, sponsor or endorser.",
            )
        }
        item { ChipRow(marketplaceCategories, category) { category = it } }

        if (category == "Lifestyle") {
            item {
                FloorSectionHeader(
                    title = "Lifestyle",
                    subtitle = "The broadest category — it splits into nine of its own.",
                )
            }
            item { PhotoCard(R.drawable.img_lifestyle_hero, "Lifestyle deals", "Everything that makes the days between shifts better.", "Featured") }
            item { ChipRow(lifestyleSubcategories, sub) { sub = it } }
            item {
                FeatureCard(
                    Icons.Filled.Star,
                    sub,
                    "Member offers across $sub. Prospective partners only during Beta — " +
                        "nothing here is a confirmed commercial relationship yet.",
                    FloorAccent.AMBER,
                )
            }
        } else {
            val deals = dealsByCategory[category].orEmpty()
            item { FloorSectionHeader(title = category, subtitle = "${deals.size} offers in this category") }
            items(deals.size) { i ->
                val d = deals[i]
                if (d.image != null) {
                    PhotoCard(d.image, d.title, d.desc, d.brand)
                } else {
                    FeatureCard(Icons.Filled.Star, d.title, d.desc, FloorAccent.AMBER, trailing = "Deal")
                }
            }
        }

        item {
            RoadmapPanel(
                "From placeholder card to real deal",
                listOf(
                    "Confirmed affiliate partners replacing prospective ones, marked as such.",
                    "Commission tracked back to the member who introduced the partner.",
                    "Revenue routed into the Community Growth Fund.",
                ),
            )
        }
    }
}
