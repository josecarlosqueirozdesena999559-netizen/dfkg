package com.example.network

import retrofit2.http.*

interface ApiService {

    @GET("api/v1/posts")
    suspend fun getFeed(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?
    ): List<PostDto>

    @POST("api/v1/posts")
    suspend fun createPost(
        @Body request: CreatePostRequest
    ): PostDto

    @POST("api/v1/posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: String
    ): Any

    @DELETE("api/v1/posts/{postId}/like")
    suspend fun unlikePost(
        @Path("postId") postId: String
    ): Any

    @POST("api/v1/polls/{pollId}/vote")
    suspend fun vote(
        @Path("pollId") pollId: String,
        @Body request: VoteRequest
    ): Any

    @GET("api/v1/me")
    suspend fun getMe(): MeProfileDto

    @GET("api/v1/users/{username}")
    suspend fun getUserProfile(
        @Path("username") username: String
    ): MeProfileDto

    @PUT("api/v1/me/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): UserProfileDto

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
}
