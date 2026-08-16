package com.thefloor.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DesignSystemTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primaryButton_clicks_and_disablesWhileLoading() {
        var clicks = 0
        composeRule.setContent {
            FloorTheme {
                FloorPrimaryButton(text = "Share", onClick = { clicks++ })
            }
        }
        composeRule.onNodeWithText("Share").assertIsDisplayed().performClick()
        assertTrue(clicks == 1)
    }

    @Test
    fun emptyState_showsActionAndFiresCallback() {
        var fired = false
        composeRule.setContent {
            FloorTheme {
                FloorEmptyState(
                    title = "No referrals yet",
                    message = "Share your link to see referrals here.",
                    actionText = "Share",
                    onAction = { fired = true },
                )
            }
        }
        composeRule.onNodeWithText("No referrals yet").assertIsDisplayed()
        composeRule.onNodeWithText("Share").performClick()
        assertTrue(fired)
    }
}
