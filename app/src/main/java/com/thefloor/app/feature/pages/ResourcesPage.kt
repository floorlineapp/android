package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.R
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorSectionHeader

@Composable
fun ResourcesScreen(onBack: () -> Unit) {
    EditorialScaffold("Resources", onBack) {
        item {
            FloorHero(
                eyebrow = "Free instant toolbox",
                title = "The tools people actually need on shift.",
                subtitle = "Calculators, templates, QA tools, WFM references and curated BPO guides — " +
                    "instant and ungated.",
            )
        }
        item {
            FloorInfoNote(accent = FloorAccent.TEAL) {
                Text(
                    "Resources and Academy do different jobs.",
                    style = FloorTheme.typography.titleSm,
                    color = FloorTheme.colors.textPrimary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Nothing here is tracked, pointed or added to your learning record. Grab it, " +
                        "use it, close it. Academy is where structured, tracked development lives.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
        item { FloorSectionHeader(title = "Browse by what you need") }
        item { FeatureCard(Icons.Filled.Calculate, "Calculators", "Shrinkage, occupancy, AHT and SLA calculators for the floor.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.Checklist, "Templates & checklists", "Coaching forms, QA scorecards and shift handover templates.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.AutoStories, "Guides & references", "Plain-language WFM, quality and operations references.", FloorAccent.CORAL) }
        item { FeatureCard(Icons.Filled.Psychology, "Scripts & phrasing", "De-escalation lines, hold phrasing and handover wording that works.", FloorAccent.AMBER) }

        item {
            FloorSectionHeader(
                title = "Featured toolkit directory",
                subtitle = "Curated from an external source library — credited, never reproduced.",
            )
        }
        item { PhotoCard(R.drawable.img_open_your_first_bank_account, "Open Your First Bank Account", "A starter guide for new agents getting paid properly.", "New agent starter") }
        item { PhotoCard(R.drawable.img_budget_save_like_a_pro, "Budget & Save Like a Pro", "Make shift pay stretch — plan, save, invest, grow.", "Save smarter") }
        item { PhotoCard(R.drawable.img_money_tips_that_matter, "Money Tips That Matter", "Small habits that change what payday feels like.", "Financial tips") }
        item { PhotoCard(R.drawable.img_start_trading_the_right_way, "Start Trading the Right Way", "Understand the risk before you put money in.", "Learn to trade") }

        item {
            RoadmapPanel(
                "Three desks, not one shelf",
                listOf(
                    "An agent desk — everything needed on the phones today.",
                    "A team leader desk — coaching, one-to-ones and calibration.",
                    "An operations desk — forecasting, WFM and reporting.",
                ),
            )
        }
    }
}

internal data class Employer(val name: String, val market: String, val focus: String)

/** Eleven illustration profiles. */
internal val employers = listOf(
    Employer("Meridian Contact Solutions", "South Africa · Johannesburg", "Voice · financial services"),
    Employer("Northbank Support Group", "Philippines · Manila", "Omnichannel · retail"),
    Employer("Lighthouse Customer Care", "Colombia · Bogotá", "Bilingual voice · travel"),
    Employer("Vantage Voice", "India · Bengaluru", "Technical support"),
    Employer("Arcadia BPO", "Kenya · Nairobi", "Back office · fintech"),
    Employer("Harbour Line Services", "Poland · Kraków", "Multilingual · logistics"),
    Employer("Summit Care Partners", "Mexico · Guadalajara", "Nearshore voice"),
    Employer("Delta Bridge Outsourcing", "Egypt · Cairo", "Multilingual · telecom"),
    Employer("Copperfield Contact", "United Kingdom · Manchester", "Regulated · insurance"),
    Employer("Riverstone Global", "United States · remote", "Remote-first · SaaS support"),
    Employer("Atlas Shift Group", "Brazil · São Paulo", "Voice · e-commerce"),
)

internal data class Job(val title: String, val company: String, val location: String, val mode: String)

internal val jobs = listOf(
    Job("Senior Agent · Voice", "Meridian Contact Solutions", "Johannesburg", "Hybrid"),
    Job("Team Leader · Customer Care", "Northbank Support Group", "Manila", "On-site"),
    Job("QA Analyst", "Lighthouse Customer Care", "Bogotá", "Remote"),
    Job("WFM Analyst", "Vantage Voice", "Bengaluru", "Hybrid"),
    Job("Customer Support Specialist", "Riverstone Global", "Remote", "Remote"),
)
