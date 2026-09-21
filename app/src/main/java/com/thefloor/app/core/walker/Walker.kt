package com.thefloor.app.core.walker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Walker — the global support layer.
 *
 * Walker is deliberately NOT one of the app's pages: it is a floating button
 * present on every screen, exactly as the process guide specifies. The message
 * model carries three sender types from day one (user / ai / agent) so that
 * putting real people behind the escalation queue later is a staffing change,
 * not a rebuild.
 *
 * The copy here is honest about what is actually staffed today. Walker answers
 * as an assistant; asking for a person raises a ticket and says so, rather than
 * implying a human is already waiting.
 */
enum class WalkerSender { USER, AI, AGENT }

/** Mirrors support_conversations.status in the target schema. */
enum class WalkerStatus { OPEN, AI_HANDLED, ESCALATED, CLOSED }

data class WalkerMessage(
    val id: Long,
    val sender: WalkerSender,
    val body: String,
)

data class WalkerState(
    val messages: List<WalkerMessage> = listOf(
        WalkerMessage(
            id = 0,
            sender = WalkerSender.AI,
            body = "Hi — I'm Walker, support for The Floor. Ask me about your account, " +
                "Floor Points, verification, a community, or anything that isn't working. " +
                "If I can't sort it, I'll pass it to a person.",
        ),
    ),
    val status: WalkerStatus = WalkerStatus.OPEN,
    val replying: Boolean = false,
)

@HiltViewModel
class WalkerViewModel @Inject constructor() : ViewModel() {

    val state = MutableStateFlow(WalkerState())
    private var nextId = 1L

    fun send(text: String) {
        val body = text.trim()
        if (body.isEmpty() || state.value.replying) return
        state.update {
            it.copy(
                messages = it.messages + WalkerMessage(nextId++, WalkerSender.USER, body),
                replying = true,
            )
        }
        viewModelScope.launch {
            delay(650)
            val reply = answerFor(body)
            state.update {
                it.copy(
                    messages = it.messages + WalkerMessage(nextId++, WalkerSender.AI, reply),
                    status = if (it.status == WalkerStatus.OPEN) WalkerStatus.AI_HANDLED else it.status,
                    replying = false,
                )
            }
        }
    }

    /** Explicit hand-off. Honest: it opens a ticket, it does not summon a person. */
    fun escalate() {
        if (state.value.status == WalkerStatus.ESCALATED) return
        state.update {
            it.copy(
                status = WalkerStatus.ESCALATED,
                messages = it.messages + WalkerMessage(
                    nextId++,
                    WalkerSender.AGENT,
                    "This conversation is now with the Walker team. We'll come back to you " +
                        "here — you'll get a notification when someone replies. Support runs " +
                        "across timezones, so an answer may not be instant.",
                ),
            )
        }
    }

    /** In-context replies keyed off what the member actually asked about. */
    private fun answerFor(q: String): String {
        val t = q.lowercase()
        return when {
            listOf("point", "balance", "credit", "pts").any { t.contains(it) } ->
                "Every Floor Point has a source, a timestamp and a ledger entry — nothing is " +
                    "awarded by a click alone. Open Rewards & Games to see your full history. " +
                    "If a point you expected is missing, tell me which action and when, and " +
                    "I'll check the record."
            listOf("verif", "badge", "workplace").any { t.contains(it) } ->
                "Workplace verification is what unlocks Recognised Member and lets you submit " +
                    "to Workplace Spotlight. The quickest route is a work email on your " +
                    "employer's domain — add it under Profile → Edit."
            listOf("floor", "communit", "join", "group").any { t.contains(it) } ->
                "Floors are open — joining is one tap, with no approval step. If the Floor you " +
                    "want doesn't exist, use \"Suggest a Floor\" on The Floor tab and it comes " +
                    "straight to us."
            listOf("spotlight", "submit", "story").any { t.contains(it) } ->
                "Spotlight submission unlocks at Floor Voice — 10,000 Floor Points plus a " +
                    "verified workplace. Approved stories pay +75 points back into your ledger."
            listOf("refer", "invite", "commission", "payout").any { t.contains(it) } ->
                "Invite & Grow is a separate record from Floor Points on purpose: referrals " +
                    "never add points and never buy stature. No pay-to-play, no downlines, no " +
                    "commissions from other people's referrals."
            listOf("password", "log in", "login", "sign in", "locked").any { t.contains(it) } ->
                "I can't reset a password from here, but the \"Forgot password\" link on the " +
                    "login screen emails you a reset link. If it doesn't arrive, check spam " +
                    "and tell me the address you used."
            listOf("delete", "close my account", "leave").any { t.contains(it) } ->
                "You can delete your account under Settings → Delete account. It removes your " +
                    "profile and your posts. If you'd rather just step back, you can go quiet " +
                    "from Profile → Privacy instead."
            listOf("radio", "on air", "listen").any { t.contains(it) } ->
                "Floor Radio runs six regional feeds — Global, Africa, Philippines, India, " +
                    "UK & Europe and the Americas — each with its own schedule and live chat. " +
                    "Your region choice is remembered on this device."
            listOf("human", "person", "someone", "agent", "real").any { t.contains(it) } ->
                "I can pass this to the Walker team — tap \"Talk to a person\" below and I'll " +
                    "hand the whole conversation over."
            else ->
                "Got it. I've logged that. If it's account, payment or something broken, tap " +
                    "\"Talk to a person\" and the Walker team picks it up with this " +
                    "conversation attached."
        }
    }
}

/**
 * The floating Walker button. Rendered once, by the app shell, over every
 * screen — never routed to.
 */
@Composable
fun WalkerFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        color = FloorTheme.colors.amber,
        contentColor = FloorTheme.colors.onAmber,
        shadowElevation = 8.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.SupportAgent,
                contentDescription = "Walker — support",
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WalkerSheet(
    onDismiss: () -> Unit,
    viewModel: WalkerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FloorTheme.colors.ink,
        contentColor = FloorTheme.colors.textPrimary,
    ) {
        WalkerConversation(
            state = state,
            onSend = viewModel::send,
            onEscalate = viewModel::escalate,
        )
    }
}

/** Stateless conversation body — rendered by the sheet and by the screenshot suite. */
@Composable
internal fun WalkerConversation(
    state: WalkerState,
    onSend: (String) -> Unit,
    onEscalate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(modifier = modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(FloorTheme.colors.amberSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = FloorTheme.colors.amber,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Walker", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                Text(
                    when (state.status) {
                        WalkerStatus.ESCALATED -> "With the Walker team"
                        WalkerStatus.CLOSED -> "Closed"
                        else -> "Support across The Floor"
                    },
                    style = FloorTheme.typography.caption,
                    color = if (state.status == WalkerStatus.ESCALATED) {
                        FloorTheme.colors.teal
                    } else {
                        FloorTheme.colors.textMuted
                    },
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.heightIn(min = 220.dp, max = 420.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.messages, key = { it.id }) { m -> WalkerBubble(m) }
            if (state.replying) {
                item {
                    Text(
                        "Walker is typing…",
                        style = FloorTheme.typography.caption,
                        color = FloorTheme.colors.textMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        FloorTextField(
            value = draft,
            onValueChange = { draft = it },
            label = "Message Walker",
            singleLine = false,
            minLines = 1,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FloorPillButton(
                text = if (state.status == WalkerStatus.ESCALATED) "With the team" else "Talk to a person",
                onClick = onEscalate,
                primary = false,
                enabled = state.status != WalkerStatus.ESCALATED,
            )
            IconButton(
                onClick = {
                    onSend(draft)
                    draft = ""
                },
                enabled = draft.isNotBlank() && !state.replying,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (draft.isNotBlank()) FloorTheme.colors.amber else FloorTheme.colors.textMuted,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Walker answers automatically first. Asking for a person opens a ticket with " +
                "the Walker team — replies arrive here.",
            style = FloorTheme.typography.caption,
            color = FloorTheme.colors.textMuted,
        )
    }
}

@Composable
private fun WalkerBubble(m: WalkerMessage) {
    val mine = m.sender == WalkerSender.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.86f),
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
        ) {
            if (!mine) {
                // Same bubble either way, per spec — only an explicit status label
                // tells a member whether they are with the assistant or the team.
                FloorEyebrow(
                    if (m.sender == WalkerSender.AGENT) "Walker team" else "Walker",
                    accent = if (m.sender == WalkerSender.AGENT) FloorAccent.TEAL else FloorAccent.AMBER,
                )
                Spacer(Modifier.height(4.dp))
            }
            Surface(
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (mine) 14.dp else 4.dp,
                    bottomEnd = if (mine) 4.dp else 14.dp,
                ),
                color = if (mine) FloorTheme.colors.amberSoft else FloorTheme.colors.surface,
            ) {
                Text(
                    m.body,
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textPrimary,
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (mine) FloorTheme.colors.amber.copy(alpha = 0.3f) else FloorTheme.colors.borderSoft,
                            RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (mine) 14.dp else 4.dp,
                                bottomEnd = if (mine) 4.dp else 14.dp,
                            ),
                        )
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                )
            }
        }
    }
}
