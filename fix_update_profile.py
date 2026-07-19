import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

old_catch = """            } catch (e: Exception) {
                Log.e("DecisaoViewModel", "Error updating profile", e)
            }
        }
    }

    // Privacy settings states"""

new_catch = """            } catch (e: Exception) {
                emitEvent(UiEvent.Error("Erro ao atualizar perfil: " + parseError(e)))
            }
        }
    }

    // Privacy settings states"""

content = content.replace(old_catch, new_catch)
with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
