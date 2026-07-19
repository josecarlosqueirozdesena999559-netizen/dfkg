import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

# Fix likePost
old_like = """        viewModelScope.launch {
            try {
                likePostUseCase.execute(postId, targetShouldLike)
            } catch (e: Exception) {
                // Revert optimistic update
                val revertHasLiked = !targetShouldLike
                _feedPosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val revertedLikes = if (revertHasLiked) post.likes + 1 else post.likes - 1
                            post.copy(likes = revertedLikes, hasLiked = revertHasLiked)
                        } else post
                    }
                }
                _profilePosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val revertedLikes = if (revertHasLiked) post.likes + 1 else post.likes - 1
                            post.copy(likes = revertedLikes, hasLiked = revertHasLiked)
                        } else post
                    }
                }
                val currentPost = _selectedPost.value
                if (currentPost?.id == postId) {
                    _selectedPost.value = _feedPosts.value.find { it.id == postId }
                }
                emitEvent(UiEvent.Error("Erro ao curtir: " + parseError(e)))
            }
        }"""

new_like = """        viewModelScope.launch {
            val result = likePostUseCase.execute(postId, targetShouldLike)
            if (result.isFailure) {
                // Revert optimistic update
                val revertHasLiked = !targetShouldLike
                _feedPosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val revertedLikes = if (revertHasLiked) post.likes + 1 else post.likes - 1
                            post.copy(likes = revertedLikes, hasLiked = revertHasLiked)
                        } else post
                    }
                }
                _profilePosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val revertedLikes = if (revertHasLiked) post.likes + 1 else post.likes - 1
                            post.copy(likes = revertedLikes, hasLiked = revertHasLiked)
                        } else post
                    }
                }
                val currentPost = _selectedPost.value
                if (currentPost?.id == postId) {
                    _selectedPost.value = _feedPosts.value.find { it.id == postId }
                }
                emitEvent(UiEvent.Error("Erro ao curtir: " + parseError(result.exceptionOrNull() ?: Exception("Unknown error"))))
            }
        }"""

content = content.replace(old_like, new_like)

# Fix votePoll
old_vote = """        viewModelScope.launch {
            try {
                voteUseCase.execute(postId, optionId)
            } catch (e: Exception) {
                // Revert optimistic update
                _feedPosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val newOptionIndex = post.pollOptions.indexOfFirst { it.id == optionId }
                            var revertedOptions = post.pollOptions
                            if (newOptionIndex != -1) {
                                val option = revertedOptions[newOptionIndex]
                                revertedOptions = revertedOptions.toMutableList().apply {
                                    set(newOptionIndex, option.copy(votes = option.votes - 1))
                                }
                            }
                            post.copy(
                                userSelectedOptionId = null,
                                pollOptions = revertedOptions,
                                totalVotes = post.totalVotes - 1
                            )
                        } else post
                    }
                }
                _profilePosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val newOptionIndex = post.pollOptions.indexOfFirst { it.id == optionId }
                            var revertedOptions = post.pollOptions
                            if (newOptionIndex != -1) {
                                val option = revertedOptions[newOptionIndex]
                                revertedOptions = revertedOptions.toMutableList().apply {
                                    set(newOptionIndex, option.copy(votes = option.votes - 1))
                                }
                            }
                            post.copy(
                                userSelectedOptionId = null,
                                pollOptions = revertedOptions,
                                totalVotes = post.totalVotes - 1
                            )
                        } else post
                    }
                }
                emitEvent(UiEvent.Error("Erro ao votar: " + parseError(e)))
            }
        }"""

new_vote = """        viewModelScope.launch {
            val result = voteUseCase.execute(postId, optionId)
            if (result.isFailure) {
                // Revert optimistic update
                _feedPosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val newOptionIndex = post.pollOptions.indexOfFirst { it.id == optionId }
                            var revertedOptions = post.pollOptions
                            if (newOptionIndex != -1) {
                                val option = revertedOptions[newOptionIndex]
                                revertedOptions = revertedOptions.toMutableList().apply {
                                    set(newOptionIndex, option.copy(votes = option.votes - 1))
                                }
                            }
                            post.copy(
                                userSelectedOptionId = null,
                                pollOptions = revertedOptions,
                                totalVotes = post.totalVotes - 1
                            )
                        } else post
                    }
                }
                _profilePosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val newOptionIndex = post.pollOptions.indexOfFirst { it.id == optionId }
                            var revertedOptions = post.pollOptions
                            if (newOptionIndex != -1) {
                                val option = revertedOptions[newOptionIndex]
                                revertedOptions = revertedOptions.toMutableList().apply {
                                    set(newOptionIndex, option.copy(votes = option.votes - 1))
                                }
                            }
                            post.copy(
                                userSelectedOptionId = null,
                                pollOptions = revertedOptions,
                                totalVotes = post.totalVotes - 1
                            )
                        } else post
                    }
                }
                emitEvent(UiEvent.Error("Erro ao votar: " + parseError(result.exceptionOrNull() ?: Exception("Unknown error"))))
            }
        }"""

content = content.replace(old_vote, new_vote)

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
