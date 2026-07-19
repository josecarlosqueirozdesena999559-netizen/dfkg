import os

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'r') as f:
    content = f.read()

comments_state = """    private val _currentPostComments = MutableStateFlow<List<Comment>>(emptyList())
    val currentPostComments: StateFlow<List<Comment>> = _currentPostComments.asStateFlow()

    fun loadComments(postId: String) {
        viewModelScope.launch {
            val result = getCommentsUseCase(postId)
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                val uiComments = list.map { dto ->
                    Comment(
                        id = dto.id,
                        authorName = dto.authorName,
                        authorUsername = dto.authorUsername,
                        authorAvatar = dto.authorAvatarUrl ?: "purple",
                        text = dto.content,
                        timestamp = dto.createdAt,
                        likes = dto.likesCount.toInt()
                    )
                }
                _currentPostComments.value = uiComments
            }
        }
    }
"""

# Replace the old `addComment` with a new one that calls `createCommentUseCase` and reloads comments.
old_add_comment = """    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            authorName = _userProfile.value.name,
            authorUsername = _userProfile.value.username,
            authorAvatar = _userProfile.value.avatarUrl,
            text = text,
            timestamp = "Agora"
        )
        _feedPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val updatedComments = post.postComments + newComment
                    post.copy(
                        postComments = updatedComments,
                        comments = updatedComments.size
                    )
                } else post
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
            }
        }
    }"""

content = content.replace(old_add_comment, comments_state + "\n" + new_add_comment)

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'w') as f:
    f.write(content)
