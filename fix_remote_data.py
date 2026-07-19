import os

with open('app/src/main/java/com/example/network/PostRemoteDataSource.kt', 'r') as f:
    content = f.read()

impl = """    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deletePost(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}"""
content = content.replace("}", impl)

with open('app/src/main/java/com/example/network/PostRemoteDataSource.kt', 'w') as f:
    f.write(content)
