import os

with open('app/src/main/java/com/example/network/ApiService.kt', 'r') as f:
    content = f.read()

new_endpoints = """
    @DELETE("api/v1/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String
    ): Any

    @GET("api/v1/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String
    ): List<CommentDto>

    @POST("api/v1/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body request: CreateCommentRequest
    ): CommentDto

    @POST("api/v1/users/{userId}/follow")
    suspend fun followUser(
        @Path("userId") userId: String
    ): Any

    @DELETE("api/v1/users/{userId}/follow")
    suspend fun unfollowUser(
        @Path("userId") userId: String
    ): Any

    @GET("api/v1/users/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: String
    ): List<UserProfileDto>

    @GET("api/v1/users/{userId}/following")
    suspend fun getFollowing(
        @Path("userId") userId: String
    ): List<UserProfileDto>
}"""

content = content.replace("}", new_endpoints, 1)

with open('app/src/main/java/com/example/network/ApiService.kt', 'w') as f:
    f.write(content)
