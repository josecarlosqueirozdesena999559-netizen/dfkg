import os

with open('backend/modules/follows/repositories/FollowRepository.kt', 'r') as f:
    content = f.read()

new_methods = """
    suspend fun getFollowers(userId: UUID): List<com.decisoes.modules.users.models.UserProfileDto> = dbQuery {
        (FollowsTable innerJoin UserProfilesTable on (FollowsTable.followerId eq UserProfilesTable.userId))
            .selectAll()
            .where { FollowsTable.followingId eq userId }
            .orderBy(FollowsTable.createdAt to SortOrder.DESC)
            .map { row ->
                com.decisoes.modules.users.models.UserProfileDto(
                    userId = row[UserProfilesTable.userId].toString(),
                    displayName = row[UserProfilesTable.displayName],
                    bio = row[UserProfilesTable.bio],
                    profileImageUrl = row[UserProfilesTable.profileImageUrl],
                    coverImageUrl = row[UserProfilesTable.coverImageUrl],
                    followersCount = row[UserProfilesTable.followersCount],
                    followingCount = row[UserProfilesTable.followingCount],
                    postsCount = row[UserProfilesTable.postsCount],
                    verified = row[UserProfilesTable.verified],
                    createdAt = row[UserProfilesTable.createdAt].toString(),
                    updatedAt = row[UserProfilesTable.updatedAt].toString()
                )
            }
    }

    suspend fun getFollowing(userId: UUID): List<com.decisoes.modules.users.models.UserProfileDto> = dbQuery {
        (FollowsTable innerJoin UserProfilesTable on (FollowsTable.followingId eq UserProfilesTable.userId))
            .selectAll()
            .where { FollowsTable.followerId eq userId }
            .orderBy(FollowsTable.createdAt to SortOrder.DESC)
            .map { row ->
                com.decisoes.modules.users.models.UserProfileDto(
                    userId = row[UserProfilesTable.userId].toString(),
                    displayName = row[UserProfilesTable.displayName],
                    bio = row[UserProfilesTable.bio],
                    profileImageUrl = row[UserProfilesTable.profileImageUrl],
                    coverImageUrl = row[UserProfilesTable.coverImageUrl],
                    followersCount = row[UserProfilesTable.followersCount],
                    followingCount = row[UserProfilesTable.followingCount],
                    postsCount = row[UserProfilesTable.postsCount],
                    verified = row[UserProfilesTable.verified],
                    createdAt = row[UserProfilesTable.createdAt].toString(),
                    updatedAt = row[UserProfilesTable.updatedAt].toString()
                )
            }
    }
}
"""

content = content.replace("}\n", new_methods, 1)

# Ensure only one closing brace at the very end
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + "}\n"
else:
    content += "\n}\n"

with open('backend/modules/follows/repositories/FollowRepository.kt', 'w') as f:
    f.write(content)
