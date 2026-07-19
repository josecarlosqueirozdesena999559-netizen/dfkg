import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

events_class = """
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Error(val message: String) : UiEvent()
}
"""

if "sealed class UiEvent" not in content:
    content = content.replace("sealed class FeedUiState {", events_class + "\nsealed class FeedUiState {")

import_sharedflow = """
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
"""

if "MutableSharedFlow" not in content:
    content = content.replace("import kotlinx.coroutines.flow.update", "import kotlinx.coroutines.flow.update\n" + import_sharedflow)

events_flow = """
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private fun emitEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }

    private fun parseError(e: Exception): String {
        return when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    401 -> "Sessão expirada. Por favor, faça login novamente."
                    403 -> "Acesso negado."
                    404 -> "Recurso não encontrado."
                    409 -> "Conflito de dados."
                    422 -> "Dados inválidos fornecidos."
                    500 -> "Erro interno do servidor."
                    else -> "Erro no servidor: ${e.code()}"
                }
            }
            is java.io.IOException -> "Sem conexão com a internet. Verifique sua rede."
            else -> "Erro inesperado: ${e.localizedMessage ?: "desconhecido"}"
        }
    }
"""

if "_uiEvents = MutableSharedFlow" not in content:
    content = content.replace("    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)", events_flow + "\n    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)")

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
