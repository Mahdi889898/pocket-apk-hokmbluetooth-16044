package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("حکم بلوتوثی", appName)
    }

    @Test
    fun `home screen displays mahlin dedication, theme picker, and opens solo game`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("host_room_button").assertIsDisplayed()
        composeRule.onNodeWithTag("join_room_button").assertIsDisplayed()

        composeRule.onNodeWithTag("mahlin_dedication_card")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("تقدیم به دختر قشنگم ماهلین", substring = true)
            .assertIsDisplayed()

        composeRule.onNodeWithTag("table_theme_sapphire_lounge")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithTag("solo_practice_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("exit_game_button").assertIsDisplayed()
        composeRule.onNodeWithTag("game_theme_picker_button").assertIsDisplayed()
    }
}
