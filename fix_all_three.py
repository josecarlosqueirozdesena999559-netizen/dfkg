import os

# 1. Create DeletePostUseCase.kt
with open('app/src/main/java/com/example/usecase/DeletePostUseCase.kt', 'w') as f:
    f.write("""package com.example.usecase

import com.example.repository.PostRepository

class DeletePostUseCase(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return repository.deletePost(postId)
    }
}
""")

# 2. Fix MockPostRepository.kt
# I'll just write it correctly.
with open('app/src/main/java/com/example/repository/MockPostRepository.kt', 'r') as f:
    content = f.read()

# I will find the deletePost and the corrupted methods
import re
# The corruption started when I replaced "}". I'll just restore the original file and apply the change properly.
