package app.enjoylearning504.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.designsystem.component.OtpField
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.model.AppError
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute(upgradedFromOldApp: Boolean, onDone: () -> Unit, viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onDone() }
    AuthScreen(
        state = state,
        upgradedFromOldApp = upgradedFromOldApp,
        onPhone = viewModel::onPhone,
        onCode = viewModel::onCode,
        onSubmit = viewModel::submit,
        onEditPhone = viewModel::editPhone,
        onResend = viewModel::resend,
        onSkip = viewModel::skip,
    )
    if (state.step == AuthStep.EnterCode) SmsCodeListener(key = state.codeRequest, onCode = viewModel::onCode)
}

@Composable
internal fun AuthScreen(
    state: AuthUiState,
    upgradedFromOldApp: Boolean,
    onPhone: (String) -> Unit,
    onCode: (String) -> Unit,
    onSubmit: () -> Unit,
    onEditPhone: () -> Unit,
    onResend: () -> Unit,
    onSkip: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))
                // People coming from the original app have progress to lose, so they get the pitch
                Text(
                    stringResource(if (upgradedFromOldApp) R.string.auth_title_upgrade else R.string.auth_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MemricaColors.TextStrong,
                    modifier = Modifier.semantics { heading() },
                )
                if (upgradedFromOldApp) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.auth_subtitle_upgrade),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Form(state, onPhone, onCode, onSubmit, onEditPhone, onResend)
                state.error?.let {
                    Text(
                        stringResource(it.message()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(20.dp))
                if (state.isBusy) {
                    CircularProgressIndicator(Modifier.size(40.dp))
                } else {
                    PrimaryButton(stringResource(state.submitLabel()), onSubmit, Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onSkip, enabled = !state.isBusy) {
                    Text(stringResource(if (upgradedFromOldApp) R.string.auth_continue_as_guest else R.string.auth_sign_in_as_guest))
                }
            }
        }
    }
}

@Composable
private fun Form(
    state: AuthUiState,
    onPhone: (String) -> Unit,
    onCode: (String) -> Unit,
    onSubmit: () -> Unit,
    onEditPhone: () -> Unit,
    onResend: () -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (state.step) {
            AuthStep.EnterPhone -> PhoneField(state, onPhone, onSubmit)
            AuthStep.EnterCode -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.auth_code_sent_phone, state.phone),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onEditPhone) { Text(stringResource(R.string.auth_change)) }
                }
                val codeDescription = stringResource(R.string.auth_code)
                OtpField(
                    value = state.code,
                    onValueChange = onCode,
                    isError = state.fieldError == FieldError.Code || state.error == AppError.InvalidCode,
                    enabled = !state.isBusy,
                    autoFocus = true,
                    onDone = onSubmit,
                    modifier = Modifier.semantics { contentDescription = codeDescription },
                )
                if (state.fieldError == FieldError.Code) {
                    Text(
                        stringResource(R.string.auth_error_code_format),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                ResendRow(state, onResend)
            }
        }
    }
}

/** Time left on the sent code, and resending once it has expired. */
@Composable
private fun ResendRow(state: AuthUiState, onResend: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onResend, enabled = state.resendSeconds == 0 && !state.isBusy) {
            Text(stringResource(R.string.auth_resend))
        }
        Spacer(Modifier.weight(1f))
        if (state.resendSeconds > 0) {
            Text(
                stringResource(R.string.auth_timer, state.resendSeconds / 60, state.resendSeconds % 60),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PhoneField(state: AuthUiState, onPhone: (String) -> Unit, onSubmit: () -> Unit) {
    OutlinedTextField(
        value = state.phone,
        onValueChange = onPhone,
        label = { Text(stringResource(R.string.auth_phone)) },
        placeholder = { Text(stringResource(R.string.auth_phone_hint)) },
        isError = state.fieldError == FieldError.Phone,
        supportingText = errorText(state.fieldError == FieldError.Phone, R.string.auth_error_phone),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun errorText(show: Boolean, res: Int): (@Composable () -> Unit)? =
    if (show) ({ Text(stringResource(res)) }) else null

private fun AuthUiState.submitLabel(): Int = when (step) {
    AuthStep.EnterPhone -> R.string.auth_sign_in_up
    AuthStep.EnterCode -> R.string.auth_verify
}

private fun AppError.message(): Int = when (this) {
    AppError.NoConnection -> R.string.auth_error_connection
    AppError.InvalidCode -> R.string.auth_error_code
    AppError.RateLimited -> R.string.auth_error_rate
    AppError.InvalidCredentials, AppError.AccountExists,
    is AppError.Validation, is AppError.Unexpected -> R.string.auth_error_unexpected
}
