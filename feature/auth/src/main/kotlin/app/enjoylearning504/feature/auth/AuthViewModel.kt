package app.enjoylearning504.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.enjoylearning504.core.domain.repository.AuthRepository
import app.enjoylearning504.core.domain.usecase.CompleteAuthPromptUseCase
import app.enjoylearning504.core.model.AppError
import app.enjoylearning504.core.model.Outcome
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Where the user is in the phone sign-in flow. Signing in and signing up are the same flow. */
enum class AuthStep {
    /** Asking for the mobile number. */
    EnterPhone,
    /** A code was sent; asking for it. */
    EnterCode,
}

enum class FieldError { Phone, Code }

data class AuthUiState(
    val step: AuthStep = AuthStep.EnterPhone,
    val phone: String = "",
    val code: String = "",
    /** Seconds until the sent code expires; resending is possible at 0. */
    val resendSeconds: Int = 0,
    /** Goes up with every code sent, so the SMS listener restarts for the new SMS. */
    val codeRequest: Int = 0,
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
    private var countdown: Job? = null

    fun onPhone(value: String) = _uiState.update {
        it.copy(phone = value.toLatinDigits().filter { c -> c.isDigit() || c == '+' }, fieldError = null, error = null)
    }

    /** Typed or read from the SMS. A complete code is verified right away. */
    fun onCode(value: String) {
        val code = value.toLatinDigits().filter(Char::isDigit).take(CODE_LENGTH)
        val changed = code != _uiState.value.code
        _uiState.update { it.copy(code = code, fieldError = null, error = null) }
        if (changed && code.length == CODE_LENGTH) submit()
    }

    /** Back from the code step to change the number. */
    fun editPhone() {
        countdown?.cancel()
        _uiState.update { it.copy(step = AuthStep.EnterPhone, code = "", resendSeconds = 0, error = null) }
    }

    fun resend() {
        val s = _uiState.value
        if (s.isBusy || s.resendSeconds > 0) return
        val phone = normalizeIranMobile(s.phone) ?: return
        _uiState.update { it.copy(isBusy = true, code = "", fieldError = null, error = null) }
        viewModelScope.launch {
            when (val result = auth.sendOtp(phone)) {
                is Outcome.Failure -> _uiState.update { it.copy(isBusy = false, error = result.error) }
                is Outcome.Success -> codeSent()
            }
        }
    }

    fun submit() {
        val s = _uiState.value
        if (s.isBusy) return
        val phone = normalizeIranMobile(s.phone)
        if (phone == null) {
            _uiState.update { it.copy(fieldError = FieldError.Phone) }
            return
        }
        if (s.step == AuthStep.EnterCode && s.code.length != CODE_LENGTH) {
            _uiState.update { it.copy(fieldError = FieldError.Code) }
            return
        }
        _uiState.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val result: Outcome<*> = when (s.step) {
                AuthStep.EnterPhone -> auth.sendOtp(phone)
                AuthStep.EnterCode -> auth.verifyOtp(phone, s.code)
            }
            when (result) {
                is Outcome.Failure -> _uiState.update { it.copy(isBusy = false, error = result.error) }
                is Outcome.Success -> when (s.step) {
                    AuthStep.EnterPhone -> codeSent()
                    AuthStep.EnterCode -> finish()
                }
            }
        }
    }

    private fun codeSent() {
        _uiState.update {
            it.copy(isBusy = false, step = AuthStep.EnterCode, code = "", codeRequest = it.codeRequest + 1)
        }
        countdown?.cancel()
        val expiry = TimeSource.Monotonic.markNow() + CODE_LIFETIME
        countdown = viewModelScope.launch {
            do {
                val left = -expiry.elapsedNow()
                val seconds = ((left.inWholeMilliseconds + 999) / 1000).coerceAtLeast(0).toInt()
                _uiState.update { it.copy(resendSeconds = seconds) }
                delay((left.inWholeMilliseconds % 1000).let { if (it > 0) it else 1000 })
            } while (seconds > 0)
        }
    }

    fun skip() {
        viewModelScope.launch { finish() }
    }

    private suspend fun finish() {
        completePrompt()
        _uiState.update { it.copy(isBusy = false, finished = true) }
    }

    private companion object {
        const val CODE_LENGTH = 6

        // How long the backend keeps a phone code (phoneOTPService in cmd/api/main.go)
        val CODE_LIFETIME = 2.minutes
    }
}

private val IRAN_MOBILE = Regex("^9\\d{9}$")

/**
 * Turns 09121112223, 9121112223 or +989121112223 into the E.164 form the backend
 * expects (+989121112223). Returns null for anything else.
 */
internal fun normalizeIranMobile(input: String): String? {
    val number = input.trim()
    val national = when {
        number.startsWith("+98") -> number.removePrefix("+98")
        number.startsWith("0") -> number.removePrefix("0")
        else -> number
    }
    return if (IRAN_MOBILE.matches(national)) "+98$national" else null
}

/** Persian and Arabic-Indic keyboards type their own digits; the backend wants 0-9. */
private fun String.toLatinDigits(): String = map { c ->
    when (c) {
        in '۰'..'۹' -> '0' + (c - '۰')
        in '٠'..'٩' -> '0' + (c - '٠')
        else -> c
    }
}.joinToString("")
