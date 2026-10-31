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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.enjoylearning504.core.designsystem.component.PrimaryButton
import app.enjoylearning504.core.designsystem.theme.MemricaColors
import app.enjoylearning504.core.model.AppError
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute(onDone: () -> Unit, viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onDone() }
    AuthScreen(
        state = state,
        onTab = viewModel::selectTab,
        onMethod = viewModel::selectMethod,
        onEmail = viewModel::onEmail,
        onPhone = viewModel::onPhone,
        onCode = viewModel::onCode,
        onPassword = viewModel::onPassword,
        onSubmit = viewModel::submit,
        onForgotPassword = viewModel::startForgotPassword,
        onEditIdentifier = viewModel::editIdentifier,
        onSkip = viewModel::skip,
    )
}

@Composable
internal fun AuthScreen(
    state: AuthUiState,
    onTab: (AuthTab) -> Unit,
    onMethod: (AuthMethod) -> Unit,
    onEmail: (String) -> Unit,
    onPhone: (String) -> Unit,
    onCode: (String) -> Unit,
    onPassword: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onEditIdentifier: () -> Unit,
    onSkip: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onSkip, enabled = !state.isBusy) { Text(stringResource(R.string.auth_skip)) }
                }
                Text(
                    stringResource(R.string.auth_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MemricaColors.TextStrong,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.auth_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Segmented(
                    options = listOf(AuthTab.SignUp to R.string.auth_tab_sign_up, AuthTab.SignIn to R.string.auth_tab_sign_in),
                    selected = state.tab,
                    onSelect = onTab,
                )
                Spacer(Modifier.height(12.dp))
                if (!state.resettingPassword) {
                    Segmented(
                        options = listOf(AuthMethod.Email to R.string.auth_method_email, AuthMethod.Phone to R.string.auth_method_phone),
                        selected = state.method,
                        onSelect = onMethod,
                    )
                    Spacer(Modifier.height(20.dp))
                }
                Form(state, onEmail, onPhone, onCode, onPassword, onSubmit, onEditIdentifier)
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
                if (state.tab == AuthTab.SignIn && state.step == AuthStep.EnterCredentials) {
                    TextButton(onClick = onForgotPassword) { Text(stringResource(R.string.auth_forgot_password)) }
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onSkip, enabled = !state.isBusy) { Text(stringResource(R.string.auth_skip)) }
            }
        }
    }
}

@Composable
private fun <T> Segmented(options: List<Pair<T, Int>>, selected: T, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label)) }
        }
    }
}

@Composable
private fun Form(
    state: AuthUiState,
    onEmail: (String) -> Unit,
    onPhone: (String) -> Unit,
    onCode: (String) -> Unit,
    onPassword: (String) -> Unit,
    onSubmit: () -> Unit,
    onEditIdentifier: () -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (state.step) {
            AuthStep.EnterIdentifier, AuthStep.ForgotPassword ->
                if (state.method == AuthMethod.Phone && !state.resettingPassword) PhoneField(state, onPhone, onSubmit)
                else EmailField(state, onEmail, ImeAction.Done, onSubmit)
            AuthStep.EnterCredentials -> {
                EmailField(state, onEmail, ImeAction.Next, onSubmit)
                PasswordField(state, onPassword, R.string.auth_password, onSubmit)
            }
            AuthStep.EnterCode -> {
                val target = if (state.method == AuthMethod.Phone && !state.resettingPassword) state.phone else state.email
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.auth_code_sent_email, target),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onEditIdentifier) { Text(stringResource(R.string.auth_change)) }
                }
                OutlinedTextField(
                    value = state.code,
                    onValueChange = onCode,
                    label = { Text(stringResource(R.string.auth_code)) },
                    isError = state.fieldError == FieldError.Code,
                    supportingText = errorText(state.fieldError == FieldError.Code, R.string.auth_error_code_format),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.method == AuthMethod.Email || state.resettingPassword) {
                    PasswordField(
                        state,
                        onPassword,
                        if (state.resettingPassword) R.string.auth_new_password else R.string.auth_password,
                        onSubmit,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmailField(state: AuthUiState, onEmail: (String) -> Unit, ime: ImeAction, onSubmit: () -> Unit) {
    OutlinedTextField(
        value = state.email,
        onValueChange = onEmail,
        label = { Text(stringResource(R.string.auth_email)) },
        isError = state.fieldError == FieldError.Email,
        supportingText = errorText(state.fieldError == FieldError.Email, R.string.auth_error_email),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ime),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onSubmit() }),
        modifier = Modifier.fillMaxWidth(),
    )
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
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onSubmit() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(state: AuthUiState, onPassword: (String) -> Unit, label: Int, onSubmit: () -> Unit) {
    OutlinedTextField(
        value = state.password,
        onValueChange = onPassword,
        label = { Text(stringResource(label)) },
        isError = state.fieldError == FieldError.Password,
        supportingText = {
            Text(stringResource(if (state.fieldError == FieldError.Password) R.string.auth_error_password else R.string.auth_password_hint))
        },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onSubmit() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun errorText(show: Boolean, res: Int): (@Composable () -> Unit)? =
    if (show) ({ Text(stringResource(res)) }) else null

private fun AuthUiState.submitLabel(): Int = when (step) {
    AuthStep.EnterIdentifier, AuthStep.ForgotPassword -> R.string.auth_send_code
    AuthStep.EnterCredentials -> R.string.auth_sign_in
    AuthStep.EnterCode -> when {
        resettingPassword -> R.string.auth_reset_password
        method == AuthMethod.Email -> R.string.auth_create_account
        else -> R.string.auth_verify
    }
}

private fun AppError.message(): Int = when (this) {
    AppError.NoConnection -> R.string.auth_error_connection
    AppError.InvalidCredentials -> R.string.auth_error_credentials
    AppError.InvalidCode -> R.string.auth_error_code
    AppError.AccountExists -> R.string.auth_error_exists
    AppError.RateLimited -> R.string.auth_error_rate
    is AppError.Validation, is AppError.Unexpected -> R.string.auth_error_unexpected
}
