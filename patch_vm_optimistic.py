import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

# Fix refreshCachedToken exception
content = content.replace("""
    fun refreshCachedToken() {
        viewModelScope.launch {
            try {
                cachedToken = authRepository.getCurrentIdToken()
            } catch (e: Exception) {
                // ignore
            }
        }
    }""", """
    fun refreshCachedToken() {
        viewModelScope.launch {
            try {
                cachedToken = authRepository.getCurrentIdToken()
            } catch (e: Exception) {
                Log.e("DecisaoViewModel", "Error refreshing token", e)
            }
        }
    }""")

# Fix likePostUseCase.execute(postId, targetShouldLike)
like_post_old = """
        viewModelScope.launch {
            try {
                likePostUseCase.execute(postId, targetShouldLike)
            } catch (e: Exception) {
                // ignore
            }
        }
"""
like_post_new = """
        viewModelScope.launch {
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
        }
"""
content = content.replace(like_post_old, like_post_new)

# Fix voteUseCase.execute(postId, optionId)
vote_old = """
        viewModelScope.launch {
            try {
                voteUseCase.execute(postId, optionId)
            } catch (e: Exception) {
                // ignore
            }
        }
"""
vote_new = """
        viewModelScope.launch {
            try {
                voteUseCase.execute(postId, optionId)
            } catch (e: Exception) {
                // Revert optimistic update
                _feedPosts.update { list ->
                    list.map { post ->
                        if (post.id == postId) {
                            val oldOptionIndex = post.pollOptions.indexOfFirst { it.text == oldOptionId }
                            val newOptionIndex = post.pollOptions.indexOfFirst { it.id == optionId }
                            
                            var revertedOptions = post.pollOptions
                            
                            if (newOptionIndex != -1) {
                                val option = revertedOptions[newOptionIndex]
                                revertedOptions = revertedOptions.toMutableList().apply {
                                    set(newOptionIndex, option.copy(votes = option.votes - 1))
                                }
                            }
                            
                            if (oldOptionIndex != -1) {
                                val option = revertedOptions[oldOptionIndex]
                                revertedOptions = revertedOptions.toMutableList().apply {
                                    set(oldOptionIndex, option.copy(votes = option.votes + 1))
                                }
                            }

                            post.copy(
                                userVotedOptionId = oldOptionId,
                                pollOptions = revertedOptions,
                                totalVotes = if (oldOptionId == null) post.totalVotes - 1 else post.totalVotes
                            )
                        } else post
                    }
                }
                emitEvent(UiEvent.Error("Erro ao votar: " + parseError(e)))
            }
        }
"""
content = content.replace(vote_old, vote_new)

# Fix createPostUseCase
create_old = """
            try {
                val apiPost = createPostUseCase.execute(post)
                _feedPosts.update { list ->
                    list.map { if (it.id == tempId) apiPost else it }
                }
            } catch (e: Exception) {
                // Keep the local optimistic post
            }
"""
create_new = """
            try {
                val apiPost = createPostUseCase.execute(post)
                _feedPosts.update { list ->
                    list.map { if (it.id == tempId) apiPost else it }
                }
            } catch (e: Exception) {
                // Remove the local optimistic post
                _feedPosts.update { list ->
                    list.filterNot { it.id == tempId }
                }
                emitEvent(UiEvent.Error("Erro ao criar post: " + parseError(e)))
            }
"""
content = content.replace(create_old, create_new)

# Fix API error catch in loadFeed()
loadFeed_catch_old = """
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Sessão expirada. Por favor, faça login novamente."
                    403 -> "Acesso negado."
                    404 -> "Recurso não encontrado."
                    409 -> "Conflito de dados."
                    422 -> "Dados inválidos fornecidos."
                    500 -> "Erro interno do servidor."
                    else -> "Erro no servidor: ${e.code()}"
                }
                _feedUiState.value = FeedUiState.Error(errorMsg)
            } catch (e: java.io.IOException) {
                _feedUiState.value = FeedUiState.Error("Sem conexão com a internet. Verifique sua rede.")
            } catch (e: Exception) {
                _feedUiState.value = FeedUiState.Error("Erro inesperado: ${e.localizedMessage ?: "desconhecido"}")
            }
"""

loadFeed_catch_new = """
            } catch (e: Exception) {
                val errorMsg = parseError(e)
                _feedUiState.value = FeedUiState.Error(errorMsg)
            }
"""
content = content.replace(loadFeed_catch_old, loadFeed_catch_new)


with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
