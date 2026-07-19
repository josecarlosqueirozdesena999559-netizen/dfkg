import java.io.File

fun main() {
    val file = File("backend/modules/posts/repositories/PostRepository.kt")
    var content = file.readText()

    val findFeedStart = "    suspend fun findFeed(limit: Int, cursor: Instant?, viewerId: UUID? = null): List<PostDto> = dbQuery {"
    val findFeedEnd = "    suspend fun findById("

    val beforeFindFeed = content.substringBefore(findFeedStart)
    val afterFindFeed = findFeedEnd + content.substringAfter(findFeedEnd)

    val newFindFeed = """
    suspend fun findFeed(limit: Int, cursor: Instant?, viewerId: UUID? = null): List<PostDto> = dbQuery {
        var condition: Op<Boolean> = PostsTable.status eq "ACTIVE"
        if (cursor != null) {
            condition = condition and (PostsTable.createdAt less cursor)
        }
        
        val postRows = (PostsTable innerJoin UsersTable innerJoin UserProfilesTable)
            .selectAll()
            .where(condition)
            .orderBy(PostsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .toList()

        if (postRows.isEmpty()) return@dbQuery emptyList()

        val postIds = postRows.map { it[PostsTable.id] }
        
        val likedPostIds = if (viewerId != null) {
            PostLikesTable.selectAll()
                .where { (PostLikesTable.postId inList postIds) and (PostLikesTable.userId eq viewerId) }
                .map { it[PostLikesTable.postId] }
                .toSet()
        } else emptySet()

        val pollPostIds = postRows.filter { it[PostsTable.type] == "POLL" }.map { it[PostsTable.id] }
        
        val pollsByPostId = if (pollPostIds.isNotEmpty()) {
            val polls = PollsTable.selectAll()
                .where { PollsTable.postId inList pollPostIds }
                .toList()
                
            val pollIds = polls.map { it[PollsTable.id] }
            val optionsByPollId = if (pollIds.isNotEmpty()) {
                PollOptionsTable.selectAll()
                    .where { PollOptionsTable.pollId inList pollIds }
                    .orderBy(PollOptionsTable.displayOrder to SortOrder.ASC)
                    .groupBy { it[PollOptionsTable.pollId] }
            } else emptyMap()

            polls.associateBy({ it[PollsTable.postId] }) { pollRow ->
                val pId = pollRow[PollsTable.id]
                val options = optionsByPollId[pId]?.map { optRow ->
                    PollOptionDto(
                        id = optRow[PollOptionsTable.id].toString(),
                        pollId = pId.toString(),
                        text = optRow[PollOptionsTable.text],
                        imageUrl = optRow[PollOptionsTable.imageUrl],
                        votesCount = optRow[PollOptionsTable.votesCount],
                        displayOrder = optRow[PollOptionsTable.displayOrder]
                    )
                } ?: emptyList()
                
                PollDto(
                    id = pId.toString(),
                    postId = pollRow[PollsTable.postId].toString(),
                    question = pollRow[PollsTable.question],
                    totalVotes = pollRow[PollsTable.totalVotes],
                    expiresAt = pollRow[PollsTable.expiresAt]?.toString(),
                    options = options
                )
            }
        } else emptyMap()

        postRows.map { postRow ->
            val postId = postRow[PostsTable.id]
            PostDto(
                id = postId.toString(),
                authorId = postRow[PostsTable.authorId].toString(),
                authorName = postRow[UserProfilesTable.displayName],
                authorUsername = "@" + postRow[UsersTable.username],
                authorAvatarUrl = postRow[UserProfilesTable.profileImageUrl],
                type = postRow[PostsTable.type],
                content = postRow[PostsTable.content],
                visibility = postRow[PostsTable.visibility],
                status = postRow[PostsTable.status],
                likesCount = postRow[PostsTable.likesCount],
                commentsCount = postRow[PostsTable.commentsCount],
                sharesCount = postRow[PostsTable.sharesCount],
                viewsCount = postRow[PostsTable.viewsCount],
                hasLiked = likedPostIds.contains(postId),
                poll = pollsByPostId[postId],
                createdAt = postRow[PostsTable.createdAt].toString(),
                updatedAt = postRow[PostsTable.updatedAt].toString()
            )
        }
    }

"""
    file.writeText(beforeFindFeed + newFindFeed + afterFindFeed)
}
