import re

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "r") as f:
    content = f.read()

# Add _followersUsers flow
old_following_flow = """    private val _followingUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val followingUsers: StateFlow<List<UserProfile>> = _followingUsers.asStateFlow()"""

new_followers_flow = """    private val _followingUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val followingUsers: StateFlow<List<UserProfile>> = _followingUsers.asStateFlow()

    private val _followersUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val followersUsers: StateFlow<List<UserProfile>> = _followersUsers.asStateFlow()"""

content = content.replace(old_following_flow, new_followers_flow)

# Update loadFollowersAndFollowing
old_load = """    private fun loadFollowersAndFollowing(userId: String) {
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

new_load = """    private fun loadFollowersAndFollowing(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val followingResult = getFollowingUseCase(userId)
            val followersResult = getFollowersUseCase(userId)
            
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
                        isFollowing = true
                    )
                }
                _followingUsers.value = uiList
            }

            if (followersResult.isSuccess) {
                val list = followersResult.getOrNull() ?: emptyList()
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
                        isFollowing = false // Will be updated if we follow back, or need API status
                    )
                }
                _followersUsers.value = uiList
            }
        }
    }"""

content = content.replace(old_load, new_load)

with open("app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt", "w") as f:
    f.write(content)
