import os
import re

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'r') as f:
    content = f.read()

new_follow = """    fun toggleFollowUser(username: String) {
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
    }

    private fun loadFollowersAndFollowing(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val followingResult = getFollowingUseCase(userId)
            if (followingResult.isSuccess) {
                val list = followingResult.getOrNull() ?: emptyList()
                val uiList = list.map { dto ->
                    UserProfile(
                        id = dto.userId,
                        name = dto.displayName,
                        username = "@${dto.displayName.lowercase().replace(" ", "")}",
                        avatarUrl = dto.profileImageUrl ?: "purple",
                        bio = dto.bio ?: "",
                        publicationsCount = dto.postsCount.toInt(),
                        followersCount = dto.followersCount.toString(),
                        followingCount = dto.followingCount.toInt(),
                        isVerified = dto.verified,
                        isFollowing = true // Since we fetched our following list
                    )
                }
                _followingUsers.value = uiList
            }
        }
    }"""

# Replace toggleFollowUser block
pattern = re.compile(r'    fun toggleFollowUser\(username: String\) \{.*?\n    \}', re.DOTALL)
content = re.sub(pattern, new_follow, content)

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'w') as f:
    f.write(content)
