import re

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'r') as f:
    content = f.read()

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

pattern = re.compile(r'    fun editPost\(postId: String, newContent: String\) \{.*?\n    \}', re.DOTALL)
content = re.sub(pattern, delete_func, content)

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'w') as f:
    f.write(content)
