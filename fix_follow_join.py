import os

with open('backend/modules/follows/repositories/FollowRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("(FollowsTable innerJoin UserProfilesTable on (FollowsTable.followerId eq UserProfilesTable.userId))", "FollowsTable.crossJoin(UserProfilesTable)")
content = content.replace(".where { FollowsTable.followingId eq userId }", ".where { (FollowsTable.followerId eq UserProfilesTable.userId) and (FollowsTable.followingId eq userId) }")

content = content.replace("(FollowsTable innerJoin UserProfilesTable on (FollowsTable.followingId eq UserProfilesTable.userId))", "FollowsTable.crossJoin(UserProfilesTable)")
content = content.replace(".where { FollowsTable.followerId eq userId }", ".where { (FollowsTable.followingId eq UserProfilesTable.userId) and (FollowsTable.followerId eq userId) }")

with open('backend/modules/follows/repositories/FollowRepository.kt', 'w') as f:
    f.write(content)
