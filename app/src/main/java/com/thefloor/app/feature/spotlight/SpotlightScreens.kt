package com.thefloor.app.feature.spotlight

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.SpotlightRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.floorListPadding
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private data class Rule(val n: Int, val title: String, val body: String)

/** This is the "how it works" explainer, not the submission standards. */
private val rulesOfEngagement = listOf(
    Rule(
        1,
        "Spotlight is a showcase, not a feed",
        "It exists to put real workplace achievement on the record — awards, milestones, culture, " +
            "community impact. Every published story is attributable to a verified member and a " +
            "verified workplace. Nothing posts instantly.",
    ),
    Rule(
        2,
        "Access is earned, and it is earned through participation",
        "Submission unlocks at Floor Voice: 10,000 Floor Points and a verified workplace. Those " +
            "points come from taking part — discussions, helpful answers, verified learning, events. " +
            "Access cannot be bought, and Invite & Grow never contributes to it.",
    ),
    Rule(
        3,
        "Every submission is reviewed by a person",
        "Submitting puts your story in a moderation queue. A reviewer checks that the proof supports " +
            "the claim and that you had the standing to share it. Nothing is auto-approved, including " +
            "submissions from Workplace Ambassadors.",
    ),
    Rule(
        4,
        "Approval pays back into your ledger",
        "An approved story awards +75 Floor Points, once per submission, written to your ledger with " +
            "its own source and timestamp. A rejected story costs you nothing.",
    ),
    Rule(
        5,
        "Rejection is not a strike",
        "Most rejections are about missing proof, not bad faith. The reviewer says what was missing " +
            "and you can resubmit. Deliberately misrepresenting a workplace is different, and is " +
            "handled as a standing issue.",
    ),
    Rule(
        6,
        "Your workplace is attached to it",
        "A published story carries your verified employer. That is what gives it weight, and it is " +
            "why the standards are strict. If you would not put your name and your employer's name " +
            "on it in a room full of colleagues, do not submit it.",
    ),
    Rule(
        7,
        "Disputes do not belong here",
        "Spotlight is not a review site and not a complaints board. Grievances, pay disputes and " +
            "anything confidential go to Walker, which is the support record of the platform.",
    ),
    Rule(
        8,
        "Reviews take time, and the queue is honest about it",
        "Moderation is people, across timezones. A submission sits in review until someone gets to " +
            "it — you will see its status change rather than be left guessing.",
    ),
)

@Composable
fun SpotlightRulesScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Rules of engagement", onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = floorListPadding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FloorHero(
                    eyebrow = "How Workplace Spotlight operates",
                    title = "Earned access, human review, real proof.",
                    subtitle = "Eight rules that govern how the feature runs. The standards for what " +
                        "belongs in a story sit on the submission screen itself.",
                )
            }
            items(rulesOfEngagement.size) { i ->
                val rule = rulesOfEngagement[i]
                FloorCard(contentPadding = 18.dp) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "%02d".format(rule.n),
                            style = FloorTheme.typography.mono,
                            color = FloorTheme.colors.amber,
                            modifier = Modifier.width(34.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                rule.title,
                                style = FloorTheme.typography.titleSm,
                                color = FloorTheme.colors.textPrimary,
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                rule.body,
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                        }
                    }
                }
            }
            item {
                FloorInfoNote(accent = FloorAccent.TEAL) {
                    Text(
                        "Not sure whether something qualifies?",
                        style = FloorTheme.typography.bodyStrong,
                        color = FloorTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Ask Walker before you write it up. The button travels with you on every screen.",
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

private val submissionStandards = listOf(
    "You must be verified — your identity and current workplace, and the story must relate to that workplace.",
    "Access is earned, never bought — Floor Voice or higher, subject to good standing.",
    "Share workplace proof, not personal social posts — personal updates belong on Pulse.",
    "You must have authority to share it — nothing confidential, nothing under embargo.",
    "Attach evidence — a certificate, an announcement, photographs, a letter, results.",
    "Name real people only with their agreement.",
    "No competitor comparisons and no employer criticism — that is not what this is for.",
    "One submission per achievement. Duplicates are closed, not stacked.",
)

val SPOTLIGHT_CATEGORIES = listOf(
    "Awards & Recognition",
    "People & Culture",
    "Community Impact",
    "Career Growth",
    "Innovation",
    "Workplace Events",
)

@HiltViewModel
class SubmitSpotlightViewModel @javax.inject.Inject constructor(
    private val spotlight: SpotlightRepository,
) : ViewModel() {
    val sending = MutableStateFlow(false)
    val submitted = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun submit(category: String, title: String, story: String, proofText: String, mediaUrl: String?) {
        if (sending.value) return
        sending.value = true
        viewModelScope.launch {
            spotlight.submit(category, title, story, proofText, mediaUrl)
                .onSuccess { submitted.value = true; sending.value = false }
                .onError { e -> error.value = e.userMessage; sending.value = false }
        }
    }
}

@Composable
fun SubmitSpotlightScreen(
    onBack: () -> Unit,
    eligible: Boolean = true,
    employer: String = "",
    country: String = "",
    viewModel: SubmitSpotlightViewModel? = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    var category by remember { mutableStateOf(SPOTLIGHT_CATEGORIES.first()) }
    var title by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    var proof by remember { mutableStateOf("") }
    var media by remember { mutableStateOf<String?>(null) }
    val submitted by (viewModel?.submitted ?: MutableStateFlow(false))
        .collectAsStateWithLifecycle()
    val sending by (viewModel?.sending ?: MutableStateFlow(false))
        .collectAsStateWithLifecycle()

    val pickProof = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) media = uri.toString() }

    val ready = title.isNotBlank() && story.isNotBlank() && proof.isNotBlank()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Submit a Spotlight", onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = floorListPadding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (submitted) {
                item {
                    FloorInfoNote(accent = FloorAccent.TEAL) {
                        FloorEyebrow("In review", accent = FloorAccent.TEAL)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Your story is with the moderation queue.",
                            style = FloorTheme.typography.title,
                            color = FloorTheme.colors.textPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "A reviewer checks the proof against the claim. It is listed under Your " +
                                "submissions on the Spotlight page with its status, and an approved " +
                                "story pays +75 Floor Points into your ledger. Nothing is published " +
                                "before that.",
                            style = FloorTheme.typography.body,
                            color = FloorTheme.colors.textSecondary,
                        )
                    }
                }
                return@LazyColumn
            }

            if (!eligible) {
                item {
                    FloorInfoNote(accent = FloorAccent.AMBER) {
                        FloorEyebrow("Locked · Floor Voice required", accent = FloorAccent.AMBER)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Submission unlocks at 10,000 Floor Points with a verified workplace. " +
                                "Keep taking part and it opens on its own.",
                            style = FloorTheme.typography.body,
                            color = FloorTheme.colors.textSecondary,
                        )
                    }
                }
            }

            item {
                FloorSectionHeader(
                    title = "What kind of story is it?",
                    subtitle = "Pick the one it fits best. A reviewer may move it.",
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SPOTLIGHT_CATEGORIES.size) { i ->
                        FloorChip(
                            text = SPOTLIGHT_CATEGORIES[i],
                            selected = SPOTLIGHT_CATEGORIES[i] == category,
                            onClick = { category = SPOTLIGHT_CATEGORIES[i] },
                        )
                    }
                }
            }

            item {
                FloorTextField(
                    value = title,
                    onValueChange = { title = it.take(90) },
                    label = "Headline",
                    supporting = "One line. What actually happened.",
                )
            }
            item {
                FloorTextField(
                    value = story,
                    onValueChange = { story = it.take(1200) },
                    label = "The story",
                    supporting = "${story.length} / 1200 — who, what, when, and why it mattered.",
                    singleLine = false,
                    minLines = 5,
                )
            }
            item {
                FloorTextField(
                    value = proof,
                    onValueChange = { proof = it.take(400) },
                    label = "Proof",
                    supporting = "What backs this up — a certificate, an announcement, results, a letter.",
                    singleLine = false,
                    minLines = 2,
                )
            }

            item {
                FloorCard(contentPadding = 16.dp) {
                    Text(
                        "Attach evidence",
                        style = FloorTheme.typography.titleSm,
                        color = FloorTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "A photo of the certificate, the team, the announcement. Submissions stay " +
                            "private until they are approved.",
                        style = FloorTheme.typography.caption,
                        color = FloorTheme.colors.textMuted,
                    )
                    Spacer(Modifier.height(12.dp))
                    if (media == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 4.dp),
                        ) {
                            IconButton(
                                onClick = {
                                    pickProof.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts
                                                .PickVisualMedia.ImageOnly,
                                        ),
                                    )
                                },
                            ) {
                                Icon(
                                    Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Attach evidence",
                                    tint = FloorTheme.colors.amber,
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Add a photo",
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = media,
                                contentDescription = "Attached evidence",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Evidence attached",
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { media = null }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    tint = FloorTheme.colors.textMuted,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            item {
                FloorCard(contentPadding = 18.dp) {
                    FloorEyebrow("Submitted as", accent = FloorAccent.FAINT)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        listOf(employer, country).filter { it.isNotBlank() }.joinToString(" · ")
                            .ifBlank { "Your verified workplace" },
                        style = FloorTheme.typography.bodyStrong,
                        color = FloorTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Taken from your verified profile. A story is always attached to the " +
                            "workplace it is about.",
                        style = FloorTheme.typography.caption,
                        color = FloorTheme.colors.textMuted,
                    )
                }
            }

            item { FloorSectionHeader(title = "Submission standards", subtitle = "All eight apply, without exception.") }
            items(submissionStandards.size) { i ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        "${i + 1}",
                        style = FloorTheme.typography.monoTag,
                        color = FloorTheme.colors.teal,
                        modifier = Modifier.width(24.dp),
                    )
                    Text(
                        submissionStandards[i],
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FloorBadge("+75 on approval", tone = BadgeTone.AMBER)
                    Spacer(Modifier.weight(1f))
                }
            }
            item {
                FloorPrimaryButton(
                    text = "Send for review",
                    loading = sending,
                    onClick = {
                        viewModel?.submit(category, title.trim(), story.trim(), proof.trim(), media)
                    },
                    enabled = eligible && ready && viewModel != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item { Box(Modifier.height(2.dp)) }
        }
    }
}
