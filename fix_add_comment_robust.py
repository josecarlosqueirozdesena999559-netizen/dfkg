import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

old_add_comment = """    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val result = createCommentUseCase(postId, text)
            if (result.isSuccess) {
                // reload comments
                loadComments(postId)
                // Optionally update feedPosts comment count
                _feedPosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            post.copy(comments = post.comments + 1)
                        } else post
                    }
                }
            }
        }
    }"""

new_add_comment = """    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val result = createCommentUseCase(postId, text)
            if (result.isSuccess) {
                // reload comments
                loadComments(postId)
                // Optionally update feedPosts comment count
                _feedPosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            post.copy(comments = post.comments + 1)
                        } else post
                    }
                }
            } else {
                emitEvent(UiEvent.Error("Erro ao comentar: " + parseError(result.exceptionOrNull() ?: Exception("Unknown error"))))
            }
        }
    }"""

content = content.replace(old_add_comment, new_add_comment)

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
