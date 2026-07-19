import os

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'r') as f:
    content = f.read()

# I will add deletePostUseCase
repos = """    private val deletePostUseCase: com.example.usecase.DeletePostUseCase by lazy {
        com.example.usecase.DeletePostUseCase(postRepository)
    }
"""
content = content.replace("    private val createPostUseCase", repos + "\n    private val createPostUseCase")

# Add deletePost function replacing editPost if it exists, or just append
delete_func = """
    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = deletePostUseCase(postId)
            if (result.isSuccess) {
                // Remove from feed
                _feedPosts.update { list ->
                    list.filterNot { it.id == postId }
                }
                if (_selectedPost.value?.id == postId) {
                    closePostDetail()
                }
            }
        }
    }
"""

if "fun editPost(" in content:
    import re
    pattern = re.compile(r'    fun editPost\(postId: String, newText: String\) \{.*?\n    \}', re.DOTALL)
    content = re.sub(pattern, delete_func, content)
else:
    content = content.replace("    fun toggleSavePost(postId: String) {", delete_func + "\n    fun toggleSavePost(postId: String) {")

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'w') as f:
    f.write(content)
