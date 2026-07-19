import os

with open('app/src/main/java/com/example/repository/PostRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("}", "    suspend fun deletePost(postId: String): Result<Unit>\n}")

with open('app/src/main/java/com/example/repository/PostRepository.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/repository/ApiPostRepository.kt', 'r') as f:
    content = f.read()

impl = """    override suspend fun deletePost(postId: String): Result<Unit> {
        return dataSource.deletePost(postId)
    }
}"""
content = content.replace("}", impl)

with open('app/src/main/java/com/example/repository/ApiPostRepository.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/repository/MockPostRepository.kt', 'r') as f:
    content = f.read()

mock_impl = """    override suspend fun deletePost(postId: String): Result<Unit> {
        inMemoryPosts.removeAll { it.id == postId }
        return Result.success(Unit)
    }
}"""
content = content.replace("}", mock_impl)

with open('app/src/main/java/com/example/repository/MockPostRepository.kt', 'w') as f:
    f.write(content)

