package com.example.model

data class Story(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isCurrentUser: Boolean = false,
    val hasUnread: Boolean = true
)

data class PollOption(
    val id: String,
    val text: String,
    val imageResId: Int? = null, // Mock image identifier
    val imageUrl: String? = null, // Modern web image url
    val votes: Int
)

data class Comment(
    val id: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: String,
    val likes: Int = 0,
    val hasLiked: Boolean = false
)

data class FeedPost(
    val id: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String,
    val timeAgo: String,
    val tag: String, // e.g., "Enquete", "Depoimento", "Pensamento"
    val content: String,
    val isPoll: Boolean,
    val pollOptions: List<PollOption> = emptyList(),
    val totalVotes: Int = 0,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val hasLiked: Boolean = false,
    val userSelectedOptionId: String? = null,
    val imageUrl: String? = null, // Optional image for the post / thought
    val postComments: List<Comment> = emptyList(),
    val likedByUsers: List<UserProfile> = emptyList(),
    val category: String = "Outros",
    val isSaved: Boolean = false
)

data class ChatMessage(
    val id: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val imageUrl: String? = null,
    val isSingleView: Boolean = false,
    val isOpened: Boolean = false,
    val isDeleted: Boolean = false,
    val status: String = "LIDO" // "ENVIADO", "ENTREGUE", "LIDO"
)

data class ChatRoom(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val isGroup: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val isMuted: Boolean = false,
    val isBlocked: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeen: String = "visto por último hoje às 11:20"
)

data class UserProfile(
    val id: String = "",
    val name: String,
    val username: String,
    val avatarUrl: String,
    val bio: String,
    val publicationsCount: Int,
    val followersCount: String,
    val followingCount: Int,
    val isVerified: Boolean = true,
    val isFollowing: Boolean = false,
    val coverUrl: String = "purple"
)

enum class NotificationType {
    FOLLOW,
    LIKE,
    COMMENT,
    REPORT_DECISION,
    NEARBY_PEOPLE
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val userName: String,
    val userUsername: String,
    val userAvatar: String,
    val text: String,
    val timestamp: String,
    val isFollowing: Boolean = false,
    val reportTitle: String? = null,
    val reportDecision: String? = null,
    val isUnread: Boolean = true
)

