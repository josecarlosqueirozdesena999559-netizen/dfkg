package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

import kotlinx.coroutines.launch
import java.util.UUID
import android.util.Log

enum class ScreenType {
    WELCOME,
    MAIN,
    CHAT_DETAIL,
    POST_DETAIL,
    USER_PROFILE,
    EDIT_PROFILE,
    CREATE_SELECTION,
    CREATE_POLL,
    CREATE_THOUGHT,
    NOTIFICATIONS,
    SETTINGS,
    SAVED_POSTS,
    FOLLOWERS_LIST,
    HELP_SUPPORT
}

enum class MainTab {
    INICIO,
    EXPLORAR,
    CRIAR,
    MENSAGENS,
    PERFIL
}


sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Error(val message: String) : UiEvent()
}

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<FeedPost>, val hasMore: Boolean) : FeedUiState()
    object Empty : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class DecisaoViewModel : ViewModel() {

    val authRepository: com.example.repository.AuthRepository by lazy {
        val useDevAuth = try {
            com.example.BuildConfig.USE_DEV_AUTH.toBoolean()
        } catch (e: Exception) {
            true
        }
        if (useDevAuth) {
            com.example.repository.DevAuthRepositoryImpl()
        } else {
            com.example.repository.FirebaseAuthRepositoryImpl()
        }
    }

    @Volatile
    private var cachedToken: String? = null

    suspend fun refreshCachedTokenSuspending() {
        try {
            cachedToken = authRepository.getCurrentIdToken(true)
        } catch (e: Exception) {
            Log.e("DecisaoViewModel", "Error refreshing token", e)
        }
    }

    fun refreshCachedToken() {
        viewModelScope.launch {
            refreshCachedTokenSuspending()
        }
    }

    private val apiService by lazy {
        com.example.network.NetworkModule.createApiService {
            kotlinx.coroutines.runBlocking {
                try {
                    val token = authRepository.getCurrentIdToken(true)
                    if (token == null) {
                         Log.w("DecisaoViewModel", "Token provider returned null")
                    }
                    token
                } catch (e: Exception) {
                    Log.e("DecisaoViewModel", "Error in token provider", e)
                    null
                }
            } ?: "dev_token_${_userProfile.value.username.replace("@", "").replace(" ", "")}"
        }
    }

    private val userProfileRepository: com.example.repository.UserProfileRepository by lazy {
        com.example.repository.ApiUserProfileRepository(apiService)
    }

    private val getMeUseCase by lazy { com.example.usecase.GetMeUseCase(userProfileRepository) }
    private val getUserProfileUseCase by lazy { com.example.usecase.GetUserProfileUseCase(userProfileRepository) }
    private val updateProfileUseCase by lazy { com.example.usecase.UpdateProfileUseCase(userProfileRepository) }

    private val postRemoteDataSource by lazy {
        com.example.network.PostRemoteDataSource(apiService)
    }

        private val commentRepository: com.example.repository.CommentRepository by lazy {
        com.example.repository.ApiCommentRepository(apiService)
    }

    private val followRepository: com.example.repository.FollowRepository by lazy {
        com.example.repository.ApiFollowRepository(apiService)
    }

    private val getCommentsUseCase: com.example.usecase.GetCommentsUseCase by lazy {
        com.example.usecase.GetCommentsUseCase(commentRepository)
    }

    private val createCommentUseCase: com.example.usecase.CreateCommentUseCase by lazy {
        com.example.usecase.CreateCommentUseCase(commentRepository)
    }

    private val followUserUseCase: com.example.usecase.FollowUserUseCase by lazy {
        com.example.usecase.FollowUserUseCase(followRepository)
    }

    private val unfollowUserUseCase: com.example.usecase.UnfollowUserUseCase by lazy {
        com.example.usecase.UnfollowUserUseCase(followRepository)
    }

    private val getFollowersUseCase: com.example.usecase.GetFollowersUseCase by lazy {
        com.example.usecase.GetFollowersUseCase(followRepository)
    }

    private val getFollowingUseCase: com.example.usecase.GetFollowingUseCase by lazy {
        com.example.usecase.GetFollowingUseCase(followRepository)
    }

    private val postRepository: com.example.repository.PostRepository by lazy {
        com.example.repository.ApiPostRepository(postRemoteDataSource)
    }

    private val getFeedUseCase by lazy { com.example.usecase.GetFeedUseCase(postRepository) }
    private val deletePostUseCase: com.example.usecase.DeletePostUseCase by lazy {
        com.example.usecase.DeletePostUseCase(postRepository)
    }

    private val createPostUseCase by lazy { com.example.usecase.CreatePostUseCase(postRepository) }
    private val likePostUseCase by lazy { com.example.usecase.LikePostUseCase(postRepository) }
    private val voteUseCase by lazy { com.example.usecase.VoteUseCase(postRepository) }


    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private fun emitEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }

    private fun parseError(e: Throwable): String {
        return when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    401 -> "Sessão expirada. Por favor, faça login novamente."
                    403 -> "Acesso negado."
                    404 -> "Recurso não encontrado."
                    409 -> "Conflito de dados."
                    422 -> "Dados inválidos fornecidos."
                    500 -> "Erro interno do servidor."
                    else -> "Erro no servidor: ${e.code()}"
                }
            }
            is java.io.IOException -> "Sem conexão com a internet. Verifique sua rede."
            else -> "Erro inesperado: ${e.localizedMessage ?: "desconhecido"}"
        }
    }

    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()

    private var currentCursor: String? = null
    private var isLoadingMore = false
    private var isFirstLoad = true

    fun loadFeed(isRefresh: Boolean = false, silent: Boolean = false) {
        if (isRefresh) {
            currentCursor = null
            _feedUiState.value = FeedUiState.Loading
        } else if (isLoadingMore) {
            return
        }
        isLoadingMore = true

        viewModelScope.launch {
            try {
                val limit = 10
                val posts = getFeedUseCase.execute(limit, currentCursor)
                
                if (isRefresh) {
                    _feedPosts.value = posts
                } else {
                    _feedPosts.value = _feedPosts.value + posts
                }

                currentCursor = posts.lastOrNull()?.id

                if (_feedPosts.value.isEmpty()) {
                    _feedUiState.value = FeedUiState.Empty
                } else {
                    _feedUiState.value = FeedUiState.Success(_feedPosts.value, hasMore = posts.size >= limit)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Sessão expirada. Por favor, faça login novamente."
                    403 -> "Acesso negado."
                    404 -> "Conteúdo não encontrado."
                    409 -> "Conflito nos dados."
                    422 -> "Dados inválidos."
                    429 -> "Muitas requisições. Tente mais tarde."
                    else -> "Erro no servidor (Código ${e.code()})."
                }
                _feedUiState.value = FeedUiState.Error(errorMsg)
            } catch (e: java.io.IOException) {
                if (!silent) {
                    _feedUiState.value = FeedUiState.Error("Sem conexão com a internet. Verifique sua rede.")
                }
            } catch (e: Exception) {
                _feedUiState.value = FeedUiState.Error("Erro inesperado: ${e.localizedMessage ?: "desconhecido"}")
            } finally {
                isFirstLoad = false
                isLoadingMore = false
            }
        }
    }

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenType.WELCOME)
    val currentScreen: StateFlow<ScreenType> = _currentScreen.asStateFlow()

    private val _currentMainTab = MutableStateFlow(MainTab.INICIO)
    val currentMainTab: StateFlow<MainTab> = _currentMainTab.asStateFlow()

    private val _selectedChatRoom = MutableStateFlow<ChatRoom?>(null)
    val selectedChatRoom: StateFlow<ChatRoom?> = _selectedChatRoom.asStateFlow()

    // User profile state loaded dynamically from backend API
    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "Carregando...",
            username = "@carregando",
            avatarUrl = "marina",
            bio = "Carregando perfil do usuário...",
            publicationsCount = 0,
            followersCount = "0",
            followingCount = 0,
            coverUrl = "purple"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _stories = MutableStateFlow(
        listOf(
            Story("1", "Seu story", "marina", isCurrentUser = true, hasUnread = false),
            Story("2", "Gabriel", "gabriel", hasUnread = true),
            Story("3", "Juliana", "juliana", hasUnread = true),
            Story("4", "Beatriz", "beatriz", hasUnread = true),
            Story("5", "Lucas", "lucas", hasUnread = true),
            Story("6", "Ana Clara", "ana_clara", hasUnread = true)
        )
    )
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _feedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    val feedPosts: StateFlow<List<FeedPost>> = _feedPosts.asStateFlow()

    // Specific user polls shown on profile
    private val _profilePosts = MutableStateFlow(
        listOf(
            FeedPost(
                id = "profile-post-1",
                authorName = "Marina Souza",
                authorUsername = "@marinasouza",
                authorAvatar = "marina",
                timeAgo = "1d",
                tag = "Enquete",
                content = "Qual o maior desafio da educação hoje?",
                isPoll = true,
                pollOptions = listOf(
                    PollOption("popt-1a", "Falta de investimentos", votes = 1152),
                    PollOption("popt-1b", "Metodologias ultrapassadas", votes = 768),
                    PollOption("popt-1c", "Desigualdade de acesso", votes = 640)
                ),
                totalVotes = 2560,
                likes = 312,
                comments = 128,
                shares = 36,
                category = "Educação"
            )
        )
    )
    val profilePosts: StateFlow<List<FeedPost>> = _profilePosts.asStateFlow()

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    private val _selectedPost = MutableStateFlow<FeedPost?>(null)
    val selectedPost: StateFlow<FeedPost?> = _selectedPost.asStateFlow()

    private val _selectedUserProfile = MutableStateFlow<UserProfile?>(null)
    val selectedUserProfile: StateFlow<UserProfile?> = _selectedUserProfile.asStateFlow()

    private val _savedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    val savedPosts: StateFlow<List<FeedPost>> = _savedPosts.asStateFlow()

    private val _followingUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val followingUsers: StateFlow<List<UserProfile>> = _followingUsers.asStateFlow()

    private val _followersUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val followersUsers: StateFlow<List<UserProfile>> = _followersUsers.asStateFlow()


    init {
        _followingUsers.value = emptyList()
        viewModelScope.launch {
            try {
                cachedToken = authRepository.getCurrentIdToken(true)
            } catch (e: Exception) {
                // ignore
            }
            loadFeed(isRefresh = true, silent = true)
            loadMeProfile()
        }
    }

    private val _allUserProfiles = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val allUserProfiles: StateFlow<Map<String, UserProfile>> = _allUserProfiles.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun toggleFollowNotification(notificationId: String) {
        _notifications.update { list ->
            list.map { notif ->
                if (notif.id == notificationId) {
                    val newIsFollowing = !notif.isFollowing
                    toggleFollowUser(notif.userUsername)
                    notif.copy(isFollowing = newIsFollowing)
                } else notif
            }
        }
    }

    fun markNotificationsAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isUnread = false) }
        }
    }

    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }

    fun selectScreen(screen: ScreenType) {
        _currentScreen.value = screen
    }

    fun selectTab(tab: MainTab) {
        _currentMainTab.value = tab
    }

    fun openPostDetail(post: FeedPost) {
        _selectedPost.value = post
        loadComments(post.id)
        _currentScreen.value = ScreenType.POST_DETAIL
    }

    fun closePostDetail() {
        _selectedPost.value = null
        _currentScreen.value = ScreenType.MAIN
    }

    fun openUserProfileByUsername(username: String) {
        val currentUserUname = _userProfile.value.username
        if (username.equals(currentUserUname, ignoreCase = true) || username == "@marinasouza") {
            // It's the current user! Go to profile tab!
            _currentMainTab.value = MainTab.PERFIL
            _currentScreen.value = ScreenType.MAIN
        } else {
            viewModelScope.launch {
                try {
                    val profile = getUserProfileUseCase.execute(username)
                    _allUserProfiles.update { map ->
                        map + (profile.username to profile)
                    }
                    _selectedUserProfile.value = profile
                    _currentScreen.value = ScreenType.USER_PROFILE
                } catch (e: Exception) {
                    Log.e("DecisaoViewModel", "Error fetching user profile for $username", e)
                }
            }
        }
    }

    fun closeUserProfile() {
        _selectedUserProfile.value = null
        _currentScreen.value = ScreenType.MAIN
    }

    fun toggleFollowUser(username: String) {
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
    }

    private fun loadFollowersAndFollowing(userId: String) {
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
    }

    private val _currentPostComments = MutableStateFlow<List<Comment>>(emptyList())
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
            } else {
                emitEvent(UiEvent.Error("Erro ao carregar comentários: " + parseError(result.exceptionOrNull() ?: Exception("Unknown error"))))
            }
        }
    }

    fun addComment(postId: String, text: String) {
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
    }


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


    fun toggleLikeComment(postId: String, commentId: String) {
        _feedPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val updatedComments = post.postComments.map { comment ->
                        if (comment.id == commentId) {
                            val newHasLiked = !comment.hasLiked
                            val newLikes = if (newHasLiked) comment.likes + 1 else comment.likes - 1
                            comment.copy(likes = newLikes, hasLiked = newHasLiked)
                        } else comment
                    }
                    post.copy(postComments = updatedComments)
                } else post
            }
        }
        // Also update selectedPost if it's currently opened
        val currentPost = _selectedPost.value
        if (currentPost?.id == postId) {
            _selectedPost.value = _feedPosts.value.find { it.id == postId }
        }
    }

    fun startChatWithUser(username: String) {
        val existingRoom = _chatRooms.value.find { it.username == username }
        if (existingRoom != null) {
            _selectedChatRoom.value = existingRoom
            _currentScreen.value = ScreenType.CHAT_DETAIL
        } else {
            val user = _allUserProfiles.value[username]
            if (user != null) {
                val newRoom = ChatRoom(
                    id = UUID.randomUUID().toString(),
                    name = user.name,
                    username = user.username,
                    avatarUrl = user.avatarUrl,
                    lastMessage = "Comece a conversar com ${user.name}!",
                    timestamp = "Agora",
                    unreadCount = 0,
                    messages = emptyList()
                )
                _chatRooms.update { it + newRoom }
                _selectedChatRoom.value = newRoom
                _currentScreen.value = ScreenType.CHAT_DETAIL
            }
        }
    }

    fun likePost(postId: String) {
        val currentUserProfile = _userProfile.value
        var targetShouldLike = false
        // Toggle like on general feed posts
        _feedPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val newHasLiked = !post.hasLiked
                    targetShouldLike = newHasLiked
                    val newLikes = if (newHasLiked) post.likes + 1 else post.likes - 1
                    
                    var updatedLikedBy = post.likedByUsers.toMutableList()
                    if (newHasLiked) {
                        if (updatedLikedBy.none { it.username == currentUserProfile.username }) {
                            updatedLikedBy.add(0, currentUserProfile)
                        }
                        // Add another random mock user if available
                        val otherAvailable = _allUserProfiles.value.values.filter { other ->
                            updatedLikedBy.none { it.username == other.username }
                        }
                        if (otherAvailable.isNotEmpty()) {
                            updatedLikedBy.add(otherAvailable.random())
                        }
                    } else {
                        updatedLikedBy.removeAll { it.username == currentUserProfile.username }
                    }
                    
                    post.copy(
                        likes = newLikes, 
                        hasLiked = newHasLiked,
                        likedByUsers = updatedLikedBy,
                        comments = post.postComments.size
                    )
                } else post
            }
        }
        // Toggle like on profile posts too
        _profilePosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val newHasLiked = !post.hasLiked
                    targetShouldLike = newHasLiked
                    val newLikes = if (newHasLiked) post.likes + 1 else post.likes - 1
                    post.copy(likes = newLikes, hasLiked = newHasLiked)
                } else post
            }
        }
        // Also update selectedPost if currently open
        val currentPost = _selectedPost.value
        if (currentPost?.id == postId) {
            _selectedPost.value = _feedPosts.value.find { it.id == postId }
        }

        viewModelScope.launch {
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
        }
    }

    fun toggleSavePost(postId: String) {
        _feedPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(isSaved = !post.isSaved)
                    updated
                } else post
            }
        }
        _profilePosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val updated = post.copy(isSaved = !post.isSaved)
                    updated
                } else post
            }
        }
        // Sync saved posts state flow
        _savedPosts.value = _feedPosts.value.filter { it.isSaved }
        
        // Also update selectedPost if currently open
        val currentPost = _selectedPost.value
        if (currentPost?.id == postId) {
            _selectedPost.value = _feedPosts.value.find { it.id == postId }
        }
    }

    fun reportPost(postId: String, reason: String, details: String) {
        val post = _feedPosts.value.find { it.id == postId } ?: _profilePosts.value.find { it.id == postId }
        val author = post?.authorName ?: "Publicação"
        val snippet = post?.content?.take(30)?.let { "\"$it...\"" } ?: ""
        val detailsText = if (details.isNotBlank()) "\n\nDetalhes adicionados por você: \"$details\"" else ""

        val newNotif = NotificationItem(
            id = "report_${System.currentTimeMillis()}",
            type = NotificationType.REPORT_DECISION,
            userName = "Suporte Decisões",
            userUsername = "@suporte",
            userAvatar = "time",
            text = "Recebemos sua denúncia contra a publicação de $author por '$reason'.",
            timestamp = "Agora",
            reportTitle = "Denúncia de Publicação Recebida",
            reportDecision = "Sua denúncia contra a publicação de $author ($snippet) pelo motivo de '$reason' foi registrada e encaminhada para a equipe de moderação.$detailsText\n\nNossos moderadores analisarão o conteúdo em até 24 horas. Caso seja constatada violação aos termos de uso da plataforma, o post será removido de forma definitiva.\n\nMuito obrigado por colaborar com um ecossistema saudável!",
            isUnread = true
        )

        _notifications.update { listOf(newNotif) + it }
    }

    fun reportComment(commentId: String, postId: String, reason: String, details: String) {
        val post = _feedPosts.value.find { it.id == postId } ?: _profilePosts.value.find { it.id == postId }
        val comment = post?.postComments?.find { it.id == commentId }
        val author = comment?.authorName ?: "Comentário"
        val snippet = comment?.text?.take(30)?.let { "\"$it...\"" } ?: ""
        val detailsText = if (details.isNotBlank()) "\n\nDetalhes adicionados por você: \"$details\"" else ""

        val newNotif = NotificationItem(
            id = "report_comm_${System.currentTimeMillis()}",
            type = NotificationType.REPORT_DECISION,
            userName = "Suporte Decisões",
            userUsername = "@suporte",
            userAvatar = "time",
            text = "Recebemos sua denúncia contra o comentário de $author por '$reason'.",
            timestamp = "Agora",
            reportTitle = "Denúncia de Comentário Recebida",
            reportDecision = "Sua denúncia contra o comentário de $author ($snippet) pelo motivo de '$reason' foi registrada e encaminhada para o nosso time de integridade.$detailsText\n\nQualquer agressão, assédio, ou discurso nocivo é estritamente proibido. Se violado, o comentário será removido definitivamente.\n\nAgradecemos por ajudar a proteger a comunidade!",
            isUnread = true
        )

        _notifications.update { listOf(newNotif) + it }
    }

    fun votePoll(postId: String, optionId: String) {
        // Vote on general feed posts
        _feedPosts.update { list ->
            list.map { post ->
                if (post.id == postId && post.userSelectedOptionId == null) {
                    val updatedOptions = post.pollOptions.map { opt ->
                        if (opt.id == optionId) {
                            opt.copy(votes = opt.votes + 1)
                        } else opt
                    }
                    val newTotal = post.totalVotes + 1
                    post.copy(
                        pollOptions = updatedOptions,
                        totalVotes = newTotal,
                        userSelectedOptionId = optionId
                    )
                } else post
            }
        }
        // Vote on profile posts too
        _profilePosts.update { list ->
            list.map { post ->
                if (post.id == postId && post.userSelectedOptionId == null) {
                    val updatedOptions = post.pollOptions.map { opt ->
                        if (opt.id == optionId) {
                            opt.copy(votes = opt.votes + 1)
                        } else opt
                    }
                    val newTotal = post.totalVotes + 1
                    post.copy(
                        pollOptions = updatedOptions,
                        totalVotes = newTotal,
                        userSelectedOptionId = optionId
                    )
                } else post
            }
        }


        viewModelScope.launch {
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
        }

    }

    fun openChatRoom(roomId: String) {
        val room = _chatRooms.value.find { it.id == roomId }
        if (room != null) {
            _selectedChatRoom.value = room
            _currentScreen.value = ScreenType.CHAT_DETAIL

            // Clear unread count when opening
            _chatRooms.update { list ->
                list.map { r ->
                    if (r.id == roomId) r.copy(unreadCount = 0) else r
                }
            }
        }
    }

    fun closeChatRoom() {
        _selectedChatRoom.value = null
        _currentScreen.value = ScreenType.MAIN
        _currentMainTab.value = MainTab.MENSAGENS
    }

    fun sendChatMessage(text: String) {
        val currentRoom = _selectedChatRoom.value ?: return
        if (text.isBlank()) return

        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            timestamp = "Agora",
            isFromMe = true,
            status = "ENTREGUE"
        )

        val updatedMessages = currentRoom.messages + newMessage
        val updatedRoom = currentRoom.copy(
            lastMessage = "Você: $text",
            timestamp = "Agora",
            messages = updatedMessages
        )

        _selectedChatRoom.value = updatedRoom

        _chatRooms.update { list ->
            list.map { r ->
                if (r.id == currentRoom.id) {
                    updatedRoom
                } else r
            }
        }
    }

    fun deleteChatRoom(roomId: String) {
        _chatRooms.update { list ->
            list.filter { it.id != roomId }
        }
        if (_selectedChatRoom.value?.id == roomId) {
            closeChatRoom()
        }
    }

    fun toggleMuteChatRoom(roomId: String) {
        _chatRooms.update { list ->
            list.map { r ->
                if (r.id == roomId) {
                    r.copy(isMuted = !r.isMuted)
                } else r
            }
        }
        val current = _selectedChatRoom.value
        if (current?.id == roomId) {
            _selectedChatRoom.value = current.copy(isMuted = !current.isMuted)
        }
    }

    fun toggleBlockChatRoom(roomId: String) {
        _chatRooms.update { list ->
            list.map { r ->
                if (r.id == roomId) {
                    r.copy(isBlocked = !r.isBlocked)
                } else r
            }
        }
        val current = _selectedChatRoom.value
        if (current?.id == roomId) {
            _selectedChatRoom.value = current.copy(isBlocked = !current.isBlocked)
        }
    }

    fun deleteMessage(roomId: String, messageId: String, forEveryone: Boolean) {
        _chatRooms.update { list ->
            list.map { r ->
                if (r.id == roomId) {
                    val updatedMessages = r.messages.map { msg ->
                        if (msg.id == messageId) {
                            if (forEveryone) {
                                msg.copy(isDeleted = true, text = "🚫 Mensagem apagada para todos")
                            } else {
                                msg.copy(isDeleted = true, text = "🚫 Mensagem apagada para mim")
                            }
                        } else msg
                    }
                    r.copy(
                        messages = updatedMessages,
                        lastMessage = updatedMessages.lastOrNull()?.let { 
                            if (it.isDeleted) "Mensagem apagada" else "${if (it.isFromMe) "Você: " else ""}${it.text}"
                        } ?: ""
                    )
                } else r
            }
        }
        val current = _selectedChatRoom.value
        if (current?.id == roomId) {
            val updatedMessages = current.messages.map { msg ->
                if (msg.id == messageId) {
                    if (forEveryone) {
                        msg.copy(isDeleted = true, text = "🚫 Mensagem apagada para todos")
                    } else {
                        msg.copy(isDeleted = true, text = "🚫 Mensagem apagada para mim")
                    }
                } else msg
            }
            _selectedChatRoom.value = current.copy(
                messages = updatedMessages,
                lastMessage = updatedMessages.lastOrNull()?.let { 
                    if (it.isDeleted) "Mensagem apagada" else "${if (it.isFromMe) "Você: " else ""}${it.text}"
                } ?: ""
            )
        }
    }

    fun sendChatImage(roomId: String, imageUrl: String, isSingleView: Boolean) {
        val currentRoom = _selectedChatRoom.value ?: return
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = if (isSingleView) "📷 Foto de visualização única" else "📷 Imagem",
            timestamp = "Agora",
            isFromMe = true,
            imageUrl = imageUrl,
            isSingleView = isSingleView,
            isOpened = false,
            status = "ENTREGUE"
        )
        val updatedMessages = currentRoom.messages + newMessage
        val updatedRoom = currentRoom.copy(
            lastMessage = "Você: Enviou uma foto",
            timestamp = "Agora",
            messages = updatedMessages
        )
        _selectedChatRoom.value = updatedRoom
        _chatRooms.update { list ->
            list.map { r ->
                if (r.id == roomId) updatedRoom else r
            }
        }
    }

    fun openSingleViewImage(roomId: String, messageId: String) {
        _chatRooms.update { list ->
            list.map { r ->
                if (r.id == roomId) {
                    val updatedMessages = r.messages.map { msg ->
                        if (msg.id == messageId) msg.copy(isOpened = true) else msg
                    }
                    r.copy(messages = updatedMessages)
                } else r
            }
        }
        val current = _selectedChatRoom.value
        if (current?.id == roomId) {
            val updatedMessages = current.messages.map { msg ->
                if (msg.id == messageId) msg.copy(isOpened = true) else msg
            }
            _selectedChatRoom.value = current.copy(messages = updatedMessages)
        }
    }

    fun createPoll(content: String, option1: String, option2: String) {
        if (content.isBlank() || option1.isBlank() || option2.isBlank()) return
        createCustomPost(
            content = content,
            isPoll = true,
            pollOptions = listOf(
                PollOption("opt-new-1", option1, votes = 0),
                PollOption("opt-new-2", option2, votes = 0)
            )
        )
    }

    fun createCustomPost(
        content: String,
        isPoll: Boolean,
        pollOptions: List<PollOption> = emptyList(),
        category: String = "Outros",
        imageUrl: String? = null
    ) {
        if (content.isBlank()) return

        val tempId = "user-post-${java.util.UUID.randomUUID()}"
        val newPost = FeedPost(
            id = tempId,
            authorName = _userProfile.value.name,
            authorUsername = _userProfile.value.username,
            authorAvatar = _userProfile.value.avatarUrl,
            timeAgo = "Agora",
            tag = if (isPoll) "Enquete" else "Pensamento",
            content = content,
            isPoll = isPoll,
            pollOptions = pollOptions,
            totalVotes = 0,
            likes = 0,
            comments = 0,
            shares = 0,
            category = category,
            imageUrl = imageUrl
        )

        _feedPosts.update { listOf(newPost) + it }

        // Also increment publicacoes count
        _userProfile.update {
            it.copy(publicationsCount = it.publicationsCount + 1)
        }

        viewModelScope.launch {
            try {
                val apiPost = createPostUseCase.execute(content, isPoll, pollOptions, category, imageUrl)
                _feedPosts.update { list ->
                    list.map { if (it.id == tempId) apiPost else it }
                }
            } catch (e: Exception) {
                _feedPosts.update { list -> list.filter { it.id != tempId } }
                _userProfile.update { it.copy(publicationsCount = it.publicationsCount - 1) }
            }
        }
    }

    fun loadMeProfile() {
        viewModelScope.launch {
            try {
                val profile = getMeUseCase.execute()
                _userProfile.value = profile
                
                // Synchronize stories with our own profile
                _stories.update { stories ->
                    stories.map {
                        if (it.isCurrentUser) it.copy(name = "Seu story", avatarUrl = profile.avatarUrl) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("DecisaoViewModel", "Error loading my profile", e)
                _userProfile.update {
                    it.copy(
                        name = "Erro ao carregar",
                        username = "@erro",
                        bio = "Verifique sua conexão e tente novamente."
                    )
                }
            }
        }
    }

    fun updateProfile(name: String, username: String, bio: String, avatarUrl: String = "marina", coverUrl: String = "purple") {
        if (name.isBlank() || username.isBlank()) return
        val formattedUsername = if (username.startsWith("@")) username else "@$username"
        viewModelScope.launch {
            try {
                val updated = updateProfileUseCase.execute(
                    name = name,
                    bio = bio,
                    avatarUrl = avatarUrl,
                    coverUrl = coverUrl
                )
                _userProfile.value = updated

                // Sync with stories and feed posts authored by user
                _stories.update { stories ->
                    stories.map {
                        if (it.isCurrentUser) it.copy(name = "Seu story", avatarUrl = avatarUrl) else it
                    }
                }

                _feedPosts.update { posts ->
                    posts.map {
                        if (it.authorUsername == updated.username || it.authorUsername == username || it.authorName == name) {
                            it.copy(authorName = updated.name, authorUsername = updated.username, authorAvatar = updated.avatarUrl)
                        } else it
                    }
                }

                _profilePosts.update { posts ->
                    posts.map {
                        it.copy(authorName = updated.name, authorUsername = updated.username, authorAvatar = updated.avatarUrl)
                    }
                }
                
                refreshCachedToken()
            } catch (e: Exception) {
                Log.e("DecisaoViewModel", "Error updating profile", e)
                emitEvent(UiEvent.Error("Erro ao sincronizar com o servidor: " + parseError(e)))
            }
        }
    }

    // Privacy settings states
    private val _seeOnline = MutableStateFlow(true)
    val seeOnline: StateFlow<Boolean> = _seeOnline.asStateFlow()

    private val _lastSeen = MutableStateFlow(true)
    val lastSeen: StateFlow<Boolean> = _lastSeen.asStateFlow()

    private val _delivered = MutableStateFlow(true)
    val delivered: StateFlow<Boolean> = _delivered.asStateFlow()

    private val _readReceipt = MutableStateFlow(true)
    val readReceipt: StateFlow<Boolean> = _readReceipt.asStateFlow()

    private val _typing = MutableStateFlow(true)
    val typing: StateFlow<Boolean> = _typing.asStateFlow()

    private val _recordingAudio = MutableStateFlow(true)
    val recordingAudio: StateFlow<Boolean> = _recordingAudio.asStateFlow()

    private val _blockedContacts = MutableStateFlow(listOf<String>())
    val blockedContacts: StateFlow<List<String>> = _blockedContacts.asStateFlow()

    fun togglePrivacy(setting: String) {
        when (setting) {
            "seeOnline" -> _seeOnline.update { !it }
            "lastSeen" -> _lastSeen.update { !it }
            "delivered" -> _delivered.update { !it }
            "readReceipt" -> _readReceipt.update { !it }
            "typing" -> _typing.update { !it }
            "recordingAudio" -> _recordingAudio.update { !it }
        }
    }

    fun unblockContact(contact: String) {
        _blockedContacts.update { list -> list.filter { it != contact } }
    }

    fun deleteAccountPermanently() {
        // Reset state, clear session, go to welcome screen
        _currentScreen.value = ScreenType.WELCOME
        _currentMainTab.value = MainTab.INICIO
        // Re-initialize default profile
        _userProfile.value = UserProfile(
            name = "Marina Souza",
            username = "@marinasouza",
            avatarUrl = "marina",
            bio = "Apaixonada por ideias, pessoas e mudanças. Vamos construir um futuro melhor juntos? 💜",
            publicationsCount = 128,
            followersCount = "2.4K",
            followingCount = 512
        )
    }
}
