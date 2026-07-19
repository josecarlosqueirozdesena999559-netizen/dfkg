import os

with open('backend/modules/posts/repositories/PostRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("val postIds = postRows.map", "val postIds: List<UUID> = postRows.map")
content = content.replace("val pollPostIds = postRows.filter", "val pollPostIds: List<UUID> = postRows.filter")
content = content.replace("val pollIds = polls.map", "val pollIds: List<UUID> = polls.map")

with open('backend/modules/posts/repositories/PostRepository.kt', 'w') as f:
    f.write(content)
