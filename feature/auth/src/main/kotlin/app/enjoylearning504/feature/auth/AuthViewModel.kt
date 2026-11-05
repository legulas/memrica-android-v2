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

    fun onPhone(value: String) = _uiState.update {
        it.copy(phone = value.toLatinDigits().filter { c -> c.isDigit() || c == '+' }, fieldError = null, error = null)
    }

    fun onCode(value: String) = _uiState.update {
        it.copy(code = value.toLatinDigits().filter(Char::isDigit).take(CODE_LENGTH), fieldError = null, error = null)
    }

    /** Back from the code step to change the number. */
    fun editPhone() = _uiState.update { it.copy(step = AuthStep.EnterPhone, code = "", error = null) }

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
                    AuthStep.EnterPhone -> _uiState.update { it.copy(isBusy = false, step = AuthStep.EnterCode, code = "") }
                    AuthStep.EnterCode -> finish()
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

    private companion object {
        const val CODE_LENGTH = 6
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
