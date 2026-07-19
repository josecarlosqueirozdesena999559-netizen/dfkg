import os
import glob

def replace_in_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Signatures
    content = content.replace("onEditPost: (String, String) -> Unit = { _, _ -> }", "onDeletePost: (String) -> Unit = { _ -> }")
    content = content.replace("onEditPost: (String, String) -> Unit,", "onDeletePost: (String) -> Unit,")
    
    # Calls
    content = content.replace("onEdit = { newContent -> onEditPost(post.id, newContent) }", "onDelete = { onDeletePost(post.id) }")
    content = content.replace("onEdit = { newContent -> onEditPost(it, newContent) }", "onDelete = { onDeletePost(it) }")
    
    # PostCard signature
    content = content.replace("onEdit: (String) -> Unit = {},", "onDelete: () -> Unit = {},")
    content = content.replace("onEdit: (String) -> Unit,", "onDelete: () -> Unit,")
    
    # Fix the menu click that I already changed to onEdit(post.id)
    content = content.replace("onEdit(post.id) // This actually calls deletePost now", "onDelete()")

    with open(filepath, 'w') as f:
        f.write(content)

for file in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    replace_in_file(file)

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("onEditPost = { id, text -> viewModel.editPost(id, text) }", "onDeletePost = { id -> viewModel.deletePost(id) }")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

