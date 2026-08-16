package com.thefloor.app.feature.auth

import androidx.lifecycle.SavedStateHandle
import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.common.AppError
import com.thefloor.app.core.common.AppResult
import com.thefloor.app.core.data.AuthRepository
import com.thefloor.app.core.data.ConfigRepository
import com.thefloor.app.core.datastore.ReferralStore
import com.thefloor.app.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val referralStore: ReferralStore = mockk(relaxed = true)
    private val configRepository: ConfigRepository = mockk(relaxed = true)
    private val analytics: AnalyticsTracker = mockk(relaxed = true)

    private fun viewModel(navCode: String? = null): SignUpViewModel {
        coEvery { referralStore.pending() } returns null
        coEvery { configRepository.resolveReferral(any()) } returns
            AppResult.Success(true to "Maria")
        return SignUpViewModel(
            savedStateHandle = SavedStateHandle(if (navCode != null) mapOf("code" to navCode) else emptyMap()),
            authRepository = authRepository,
            referralStore = referralStore,
            configRepository = configRepository,
            analytics = analytics,
        )
    }

    @Test
    fun `invalid input never reaches the repository`() = runTest {
        val vm = viewModel()
        vm.onEmail("bad-email")
        vm.onPassword("short")
        vm.onName("")
        vm.submit()

        val state = vm.state.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertNotNull(state.nameError)
        assertFalse(state.done)
        coVerify(exactly = 0) { authRepository.signUp(any(), any(), any()) }
    }

    @Test
    fun `valid signup completes and reports done`() = runTest {
        coEvery { authRepository.signUp(any(), any(), any()) } returns AppResult.Success(Unit)
        val vm = viewModel()
        vm.onEmail("alex@example.com")
        vm.onPassword("longenough1!")
        vm.onName("Alex B")
        vm.submit()

        assertTrue(vm.state.value.done)
        assertNull(vm.state.value.generalError)
        coVerify { authRepository.signUp("alex@example.com", "longenough1!", "Alex B") }
    }

    @Test
    fun `server rejection surfaces user message without crashing`() = runTest {
        coEvery { authRepository.signUp(any(), any(), any()) } returns
            AppResult.Error(AppError.Api("EMAIL_IN_USE", "An account with this email already exists"))
        val vm = viewModel()
        vm.onEmail("alex@example.com")
        vm.onPassword("longenough1!")
        vm.onName("Alex B")
        vm.submit()

        assertFalse(vm.state.value.done)
        assertEquals("An account with this email already exists", vm.state.value.generalError)
    }

    @Test
    fun `deep-link referral code resolves inviter chip`() = runTest {
        val vm = viewModel(navCode = "ABCDEFGH")
        assertEquals("ABCDEFGH", vm.state.value.inviteCode)
        assertEquals("Maria", vm.state.value.inviterName)
        coVerify { referralStore.save("ABCDEFGH", "DEEP_LINK") }
    }

    @Test
    fun `dismissing inviter clears the pending code`() = runTest {
        val vm = viewModel(navCode = "ABCDEFGH")
        vm.dismissInviter()
        assertNull(vm.state.value.inviterName)
        coVerify { referralStore.clear() }
    }
}
