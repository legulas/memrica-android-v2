package app.enjoylearning504

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.enjoylearning504.core.domain.usecase.PrepareAppStartUseCase
import app.enjoylearning504.core.domain.usecase.StartDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Holds the splash screen until the legacy data is migrated and the first screen is known. */
class MainViewModel(prepareAppStart: PrepareAppStartUseCase) : ViewModel() {
    private val _startDestination = MutableStateFlow<StartDestination?>(null)
    val startDestination: StateFlow<StartDestination?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch { _startDestination.value = prepareAppStart().first }
    }
}
