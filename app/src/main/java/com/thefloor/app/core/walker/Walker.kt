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
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorTextField
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val messages: List<WalkerMessage> = emptyList(),
    val status: WalkerStatus = WalkerStatus.OPEN,
    /** Quotable ticket reference once a human has it. */
    val reference: String? = null,
    val sending: Boolean = false,
)

/**
 * The questions people actually arrive with. Offering them is not decoration:
 * a support box with no prompts gets "it doesn't work", and a support box with
 * the right five prompts gets an answerable question.
 */
val WALKER_TOPICS = listOf(
    "Where are my points?",
    "Am I verified?",
    "What tier am I?",
    "How do I submit a Spotlight?",
    "How does Invite & Grow pay?",
)

/**
 * Lets any screen raise Walker without owning it. Walker is rendered once by
 * the app shell; a "Suggest a Floor" card or a support prompt deep inside a
 * feature just flips this.
 */
@javax.inject.Singleton
class WalkerBus @Inject constructor() {
    val open = MutableStateFlow(false)
    fun open() { open.value = true }
    fun close() { open.value = false }
}

@HiltViewModel
class WalkerViewModel @Inject constructor(
    private val support: com.thefloor.app.core.data.SupportRepository,
) : ViewModel() {

    val state = MutableStateFlow(WalkerState())

    init {
        viewModelScope.launch {
            support.conversation().onSuccess { apply(it) }
        }
    }

    fun send(text: String) {
        val body = text.trim()
        if (body.isEmpty() || state.value.sending) return
        state.update { it.copy(sending = true) }
        viewModelScope.launch {
            support.send(body)
                .onSuccess { apply(it) }
                .onError { state.update { s -> s.copy(sending = false) } }
        }
    }

    /** Hands the whole thread over and comes back with a reference. */
    fun escalate() {
        if (state.value.status == WalkerStatus.ESCALATED || state.value.sending) return
        state.update { it.copy(sending = true) }
        viewModelScope.launch {
            support.send(body = "", escalate = true)
                .onSuccess { apply(it) }
                .onError { state.update { s -> s.copy(sending = false) } }
        }
    }

    private fun apply(dto: com.thefloor.app.core.network.SupportConversationDto) {
        state.value = WalkerState(
            messages = dto.messages.mapIndexed { index, m ->
                WalkerMessage(
                    id = index.toLong(),
                    sender = when (m.sender) {
                        "user" -> WalkerSender.USER
                        "agent" -> WalkerSender.AGENT
                        else -> WalkerSender.AI
                    },
                    body = m.body,
                )
            },
            status = when (dto.status) {
                "escalated" -> WalkerStatus.ESCALATED
                "closed" -> WalkerStatus.CLOSED
                "ai_handled" -> WalkerStatus.AI_HANDLED
                else -> WalkerStatus.OPEN
            },
            reference = dto.reference,
            sending = false,
        )
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
                        WalkerStatus.ESCALATED ->
                            state.reference?.let { "With the Walker team · $it" } ?: "With the Walker team"
                        WalkerStatus.CLOSED -> "Closed"
                        else -> "Automated first line · a person on request"
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
            if (state.sending) {
                item {
                    Text(
                        "Walker is checking…",
                        style = FloorTheme.typography.caption,
                        color = FloorTheme.colors.textMuted,
                    )
                }
            }
        }

        // Prompts, while the thread is still short enough for them to help.
        if (state.messages.count { it.sender == WalkerSender.USER } < 2) {
            Spacer(Modifier.height(10.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(WALKER_TOPICS) { topic ->
                    FloorChip(text = topic, selected = false, onClick = { onSend(topic) })
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
                enabled = draft.isNotBlank() && !state.sending,
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
            if (state.reference != null) {
                "Reference ${state.reference}. Replies from the Walker team arrive in this thread."
            } else {
                "Walker answers from what The Floor knows — your points, your verification, your " +
                    "tier. It is automated, not a person. Ask for one any time."
            },
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
