import os

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'r') as f:
    content = f.read()

repos = """    private val commentRepository: com.example.repository.CommentRepository by lazy {
        com.example.repository.ApiCommentRepository(apiService)
    }

    private val followRepository: com.example.repository.FollowRepository by lazy {
        com.example.repository.ApiFollowRepository(apiService)
    }

    private val getCommentsUseCase: com.example.usecase.GetCommentsUseCase by lazy {
        com.example.usecase.GetCommentsUseCase(commentRepository)
    }

    private val createCommentUseCase: com.example.usecase.CreateCommentUseCase by lazy {
        com.example.usecase.CreateCommentUseCase(commentRepository)
    }

    private val followUserUseCase: com.example.usecase.FollowUserUseCase by lazy {
        com.example.usecase.FollowUserUseCase(followRepository)
    }

    private val unfollowUserUseCase: com.example.usecase.UnfollowUserUseCase by lazy {
        com.example.usecase.UnfollowUserUseCase(followRepository)
    }

    private val getFollowersUseCase: com.example.usecase.GetFollowersUseCase by lazy {
        com.example.usecase.GetFollowersUseCase(followRepository)
    }

    private val getFollowingUseCase: com.example.usecase.GetFollowingUseCase by lazy {
        com.example.usecase.GetFollowingUseCase(followRepository)
    }
"""

content = content.replace("private val postRepository", repos + "\n    private val postRepository")

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'w') as f:
    f.write(content)

