import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

vote_new = """
        viewModelScope.launch {
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
        }
"""

old_vote_revert = """        viewModelScope.launch {
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
        }"""

content = content.replace(old_vote_revert, vote_new)
with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
