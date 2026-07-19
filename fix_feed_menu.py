import os
import re

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'r') as f:
    content = f.read()

# Replace Editar com Excluir
old_menu = """                            DropdownMenuItem(
                                text = { Text("Editar publicação") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showEditDialog = true
                                },
                                modifier = Modifier.testTag("feed_post_menu_edit_${post.id}")
                            )"""

new_menu = """                            DropdownMenuItem(
                                text = { Text("Excluir publicação") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit(post.id) // This actually calls deletePost now
                                },
                                modifier = Modifier.testTag("feed_post_menu_delete_${post.id}")
                            )"""

content = content.replace(old_menu, new_menu)

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'w') as f:
    f.write(content)
