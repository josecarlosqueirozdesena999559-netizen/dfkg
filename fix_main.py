import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# I will fix the end of onCreate. It should have just closed `when (currentScreen) { ... }` then `Box` `Scaffold` etc.
# Original end of `when(currentScreen)` was:
#                         ScreenType.HELP_SUPPORT -> {
#                             HelpSupportScreen(
#                                 onBackClick = { viewModel.selectScreen(ScreenType.MAIN) }
#                             )
#                         }
#                     }
#                 }
#             }
#         }
#     }
# }

# Find the HELP_SUPPORT block
match = re.search(r'ScreenType\.HELP_SUPPORT -> \{\s+HelpSupportScreen\(\s+onBackClick = \{ viewModel\.selectScreen\(ScreenType\.MAIN\) \}\s+\)\s+\}\s+\}\s+\}\s+\}\s+\}\s+\}\s+\}\s+\}', content)
if match:
    # We replace the extra braces
    new_tail = """                        ScreenType.HELP_SUPPORT -> {
                            HelpSupportScreen(
                                onBackClick = { viewModel.selectScreen(ScreenType.MAIN) }
                            )
                        }
                            }
                        }
                    }
                }
            }
        }
    }
}
"""
    content = content.replace(match.group(0), new_tail)

# Then there is the end of the file which got broken.
end_broken = """                            }
                        }
                    }
                }
            }
        }
    }
}"""
content = re.sub(r'                            }\s+                        }\s+                    }\s+                }\s+            }\s+        }\s+    }\s+\}$', end_broken.replace("                            }\n                        }\n                    }\n", ""), content)


with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
