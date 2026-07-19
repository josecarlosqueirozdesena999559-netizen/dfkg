import os

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_call = """                            val selectedPost by viewModel.selectedPost.collectAsState()
                            selectedPost?.let { post ->
                                PostDetailScreen(
                                    post = post,"""
new_call = """                            val selectedPost by viewModel.selectedPost.collectAsState()
                            val currentPostComments by viewModel.currentPostComments.collectAsState()
                            selectedPost?.let { post ->
                                PostDetailScreen(
                                    post = post,
                                    comments = currentPostComments,"""

content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
