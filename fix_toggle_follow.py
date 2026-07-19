import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

old_toggle = """    fun toggleFollowUser(username: String) {
        val targetProfile = _allUserProfiles.value[username] ?: return
        val isFollowing = targetProfile.isFollowing
        val userId = targetProfile.id

        if (userId.isBlank()) {
            // Cannot toggle follow without userId
            return
        }

        viewModelScope.launch {
            val result = if (isFollowing) {
                unfollowUserUseCase(userId)
            } else {
                followUserUseCase(userId)
            }

            if (result.isSuccess) {
                // Update local counts optimistically or reload profiles
                val currentFollowersInt = try {
                    val cleaned = targetProfile.followersCount.replace("K", "").toDouble()
                    if (targetProfile.followersCount.contains("K")) (cleaned * 1000).toInt() else targetProfile.followersCount.toInt()
                } catch (e: Exception) {
                    0
                }
                
                val newIsFollowing = !isFollowing
                val newFollowersCountInt = if (newIsFollowing) currentFollowersInt + 1 else currentFollowersInt - 1
                val newFollowersCountStr = if (newFollowersCountInt >= 1000) {
                    String.format("%.1fK", newFollowersCountInt / 1000.0).replace(",", ".")
                } else {
                    newFollowersCountInt.toString()
                }
                
                val updatedProfile = targetProfile.copy(
                    isFollowing = newIsFollowing,
                    followersCount = newFollowersCountStr
                )
                
                _allUserProfiles.update { map ->
                    map + (username to updatedProfile)
                }

                // If this is the selected profile, update it
                if (_selectedUserProfile.value?.username == username) {
                    _selectedUserProfile.value = updatedProfile
                }

                // Load followers and following for current user to reflect the change
                loadFollowersAndFollowing(_userProfile.value.id)
            }
        }
    }"""

new_toggle = """    fun toggleFollowUser(username: String) {
        val targetProfile = _allUserProfiles.value[username] ?: return
        val userId = targetProfile.id

        if (userId.isBlank()) return

        viewModelScope.launch {
            val isFollowing = targetProfile.isFollowing
            val result = if (isFollowing) {
                unfollowUserUseCase(userId)
            } else {
                followUserUseCase(userId)
            }

            if (result.isSuccess) {
                try {
                    val updatedProfile = getUserProfileUseCase.execute(username)
                    
                    _allUserProfiles.update { map ->
                        map + (username to updatedProfile)
                    }

                    // If this is the selected profile, update it
                    if (_selectedUserProfile.value?.username == username) {
                        _selectedUserProfile.value = updatedProfile
                    }

                    // Load followers and following for current user to reflect the change
                    loadFollowersAndFollowing(_userProfile.value.id)
                } catch (e: Exception) {
                    // Refetch failed, revert to original profile
                    // Optional: show error toast
                }
            } else {
                emitEvent(UiEvent.Error("Erro ao seguir/deixar de seguir: " + parseError(result.exceptionOrNull() ?: Exception("Unknown error"))))
            }
        }
    }"""

content = content.replace(old_toggle, new_toggle)

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
