import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("val snackbarHostState = remember { SnackbarHostState() }", "val snackbarHostState = remember { SnackbarHostState() }\n                    val context = androidx.compose.ui.platform.LocalContext.current")
content = content.replace("this@MainActivity", "context")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
