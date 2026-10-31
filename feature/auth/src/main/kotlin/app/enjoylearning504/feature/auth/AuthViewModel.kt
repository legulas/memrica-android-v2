package app.enjoylearning504.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.enjoylearning504.core.domain.repository.AuthRepository
import app.enjoylearning504.core.domain.usecase.CompleteAuthPromptUseCase
import app.enjoylearning504.core.model.AppError
import app.enjoylearning504.core.model.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthTab { SignUp, SignIn }
enum class AuthMethod { Email, Phone }

/** Where the user is in the current flow. */
enum class AuthStep {
    /** Email sign-up, phone sign-up/in, forgot password: asking for the address or number. */
    EnterIdentifier,
    /** A code was sent; asking for it (plus a password for email sign-up and reset). */
    EnterCode,
    /** Email sign-in: email and password together. */
    EnterCredentials,
    /** Forgot password: email entry, then [EnterCode]. */
    ForgotPassword,
}

enum class FieldError { Email, Phone, Password, Code }

data class AuthUiState(
    val tab: AuthTab = AuthTab.SignUp,
    val method: AuthMethod = AuthMethod.Email,
    val step: AuthStep = AuthStep.EnterIdentifier,
    val resettingPassword: Boolean = false,
    val email: String = "",
    val phone: String = "",
    val code: String = "",
    val password: String = "",
    val isBusy: Boolean = false,
    val fieldError: FieldError? = null,
    val error: AppError? = null,
    val finished: Boolean = false,
)

class AuthViewModel(
    private val auth: AuthRepository,
    private val completePrompt: CompleteAuthPromptUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun selectTab(tab: AuthTab) = _uiState.update {
        it.copy(tab = tab, step = initialStep(tab, it.method), resettingPassword = false, code = "", password = "", fieldError = null, error = null)
    }

    fun selectMethod(method: AuthMethod) = _uiState.update {
        it.copy(method = method, step = initialStep(it.tab, method), resettingPassword = false, code = "", password = "", fieldError = null, error = null)
    }

    fun onEmail(value: String) = _uiState.update { it.copy(email = value, fieldError = null, error = null) }
    fun onPhone(value: String) = _uiState.update { it.copy(phone = value.filter { c -> c.isDigit() || c == '+' }, fieldError = null, error = null) }
    fun onCode(value: String) = _uiState.update { it.copy(code = value.filter(Char::isDigit).take(CODE_LENGTH), fieldError = null, error = null) }
    fun onPassword(value: String) = _uiState.update { it.copy(password = value, fieldError = null, error = null) }

    fun startForgotPassword() = _uiState.update {
        it.copy(step = AuthStep.ForgotPassword, resettingPassword = true, code = "", password = "", fieldError = null, error = null)
    }

    /** Back from the code step to change the email or number. */
    fun editIdentifier() = _uiState.update {
        it.copy(step = if (it.resettingPassword) AuthStep.ForgotPassword else AuthStep.EnterIdentifier, code = "", error = null)
    }

    fun submit() {
        val s = _uiState.value
        if (s.isBusy) return
        validate(s)?.let { error -> _uiState.update { it.copy(fieldError = error) }; return }
        _uiState.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val email = s.email.trim()
            val phone = s.phone.trim()
            val result: Outcome<*> = when (s.step) {
                AuthStep.EnterIdentifier -> when (s.method) {
                    AuthMethod.Email -> auth.registerWithEmail(email)
                    AuthMethod.Phone -> auth.sendOtp(phone)
                }
                AuthStep.ForgotPassword -> auth.forgotPassword(email)
                AuthStep.EnterCredentials -> auth.loginWithEmail(email, s.password)
                AuthStep.EnterCode -> when {
                    s.method == AuthMethod.Phone -> auth.verifyOtp(phone, s.code)
                    s.resettingPassword -> auth.resetPassword(email, s.code, s.password)
                    else -> auth.verifyEmail(email, s.code, s.password)
                }
            }
            when (result) {
                is Outcome.Failure -> _uiState.update { it.copy(isBusy = false, error = result.error) }
                is Outcome.Success -> if (s.step == AuthStep.EnterIdentifier || s.step == AuthStep.ForgotPassword) {
                    _uiState.update { it.copy(isBusy = false, step = AuthStep.EnterCode, code = "", password = "") }
                } else {
                    finish()
                }
            }
        }
    }

    fun skip() {
        viewModelScope.launch { finish() }
    }

    private suspend fun finish() {
        completePrompt()
        _uiState.update { it.copy(isBusy = false, finished = true) }
    }

    private fun validate(s: AuthUiState): FieldError? {
        val needsEmail = s.method == AuthMethod.Email && s.step != AuthStep.EnterCode
        val needsPhone = s.method == AuthMethod.Phone && s.step == AuthStep.EnterIdentifier
        val needsCode = s.step == AuthStep.EnterCode
        val needsPassword = s.step == AuthStep.EnterCredentials ||
            (s.step == AuthStep.EnterCode && s.method == AuthMethod.Email)
        return when {
            needsEmail && !EMAIL.matches(s.email.trim()) -> FieldError.Email
            needsPhone && !PHONE.matches(s.phone.trim()) -> FieldError.Phone
            needsCode && s.code.length != CODE_LENGTH -> FieldError.Code
            needsPassword && s.password.toByteArray().size !in PASSWORD_RANGE -> FieldError.Password
            else -> null
        }
    }

    private fun initialStep(tab: AuthTab, method: AuthMethod) =
        if (tab == AuthTab.SignIn && method == AuthMethod.Email) AuthStep.EnterCredentials else AuthStep.EnterIdentifier

    private companion object {
        const val CODE_LENGTH = 6
        val PASSWORD_RANGE = 8..72

        // Same rules as the backend's internal/validator/validator.go
        val EMAIL = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
        val PHONE = Regex("^\\+[1-9]\\d{6,14}$")
    }
}
