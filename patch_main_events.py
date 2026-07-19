import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

import_statement = "import androidx.compose.material3.SnackbarHostState\nimport androidx.compose.material3.SnackbarHost\n"
if "SnackbarHostState" not in content:
    content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\n" + import_statement)

import_event = "import com.example.viewmodel.UiEvent\n"
if "UiEvent" not in content:
    content = content.replace("import com.example.viewmodel.DecisaoViewModel", "import com.example.viewmodel.DecisaoViewModel\n" + import_event)


setup_events = """
                    val snackbarHostState = remember { SnackbarHostState() }

                    LaunchedEffect(Unit) {
                        viewModel.uiEvents.collect { event ->
                            when (event) {
                                is UiEvent.ShowSnackbar -> {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                                is UiEvent.Error -> {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                                is UiEvent.ShowToast -> {
                                    android.widget.Toast.makeText(this@MainActivity, event.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            when (currentScreen) {
"""

if "snackbarHostState = remember" not in content:
    content = content.replace("""                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {""", setup_events)
    content = content.replace("""                    }
                }
            }
        }
    }
}""", """                            }
                        }
                    }
                }
            }
        }
    }
}""")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
