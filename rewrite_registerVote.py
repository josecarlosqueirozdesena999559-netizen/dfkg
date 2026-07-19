import os

with open('backend/modules/posts/repositories/PostRepository.kt', 'r') as f:
    content = f.read()

old_vote = """    suspend fun registerVote(
        voteId: UUID,
        pollId: UUID,
        optionId: UUID,
        userId: UUID
    ): Boolean = dbQuery {
        val alreadyVoted = VotesTable.selectAll()
            .where { (VotesTable.pollId eq pollId) and (VotesTable.userId eq userId) }
            .count() > 0
        if (alreadyVoted) return@dbQuery false

        VotesTable.insert {
            it[VotesTable.id] = voteId
            it[VotesTable.pollId] = pollId
            it[VotesTable.optionId] = optionId
            it[VotesTable.userId] = userId
        }"""

new_vote = """    suspend fun registerVote(
        voteId: UUID,
        pollId: UUID,
        optionId: UUID,
        userId: UUID
    ): Boolean = dbQuery {
        val insertedCount = VotesTable.insertIgnore {
            it[VotesTable.id] = voteId
            it[VotesTable.pollId] = pollId
            it[VotesTable.optionId] = optionId
            it[VotesTable.userId] = userId
        }.insertedCount

        if (insertedCount == 0) return@dbQuery false"""

content = content.replace(old_vote, new_vote)

with open('backend/modules/posts/repositories/PostRepository.kt', 'w') as f:
    f.write(content)
