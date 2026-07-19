import os

with open('app/src/main/java/com/example/network/Models.kt', 'a') as f:
    f.write("""

@JsonClass(generateAdapter = true)
data class CommentDto(
    @Json(name = "id") val id: String,
    @Json(name = "postId") val postId: String,
    @Json(name = "authorId") val authorId: String,
    @Json(name = "authorName") val authorName: String,
    @Json(name = "authorUsername") val authorUsername: String,
    @Json(name = "authorAvatarUrl") val authorAvatarUrl: String? = null,
    @Json(name = "parentCommentId") val parentCommentId: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "likesCount") val likesCount: Long = 0,
    @Json(name = "status") val status: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class CreateCommentRequest(
    @Json(name = "content") val content: String,
    @Json(name = "parentCommentId") val parentCommentId: String? = null
)
""")

