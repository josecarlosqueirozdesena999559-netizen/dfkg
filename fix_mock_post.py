content = """package com.example.repository

import com.example.model.Comment
import com.example.model.FeedPost
import com.example.model.PollOption
import com.example.model.UserProfile
import kotlinx.coroutines.delay
import java.util.UUID

class MockPostRepository : PostRepository {
    private val mockUserAna = UserProfile("", "Ana Clara", "@anaclara", "ana_clara", "Entusiasta de políticas públicas e sustentabilidade urbana.", 45, "1.2K", 280, true, false)
    private val mockUserGabriel = UserProfile("", "Gabriel Ferreira", "@gabrielf", "gabriel", "Arquiteto e urbanista focado em mobilidade sustentável.", 89, "3.1K", 420, true, false)
    private val mockUserJuliana = UserProfile("", "Juliana Costa", "@julianacosta", "juliana", "Pesquisadora de comportamento humano.", 34, "950", 180, false, false)
    private val mockUserLucas = UserProfile("", "Lucas Martins", "@lucasmartins", "lucas", "Ciclista urbano, fotógrafo amador.", 56, "1.5K", 310, false, false)
    private val mockUserBeatriz = UserProfile("", "Beatriz Santos", "@beatriz", "beatriz", "Amante da natureza, esportes.", 27, "780", 220, false, false)

    private val inMemoryPosts = mutableListOf(
        FeedPost(
            id = "post-1",
            authorName = "Marina Souza",
            authorUsername = "@marinasouza",
            authorAvatar = "marina",
            timeAgo = "12m",
            tag = "Enquete",
            content = "Como a nova lei de zoneamento afetará o acesso aos parques no centro da cidade? Vocês acreditam que teremos mais ou menos áreas verdes protegidas?",
            isPoll = true,
            pollOptions = listOf(
                PollOption("opt-1a", "Mais áreas protegidas", votes = 1245),
                PollOption("opt-1b", "Menos áreas protegidas", votes = 632),
                PollOption("opt-1c", "Não fará diferença", votes = 210)
            ),
            totalVotes = 2087,
            likes = 432,
            comments = 3,
            shares = 54,
            hasLiked = true,
            postComments = listOf(
                Comment("c1", "Ana Clara", "@anaclara", "ana_clara", "Com certeza teremos menos, os interesses imobiliários sempre falam mais alto na região central.", "10m", 12, true),
                Comment("c2", "Gabriel Ferreira", "@gabrielf", "gabriel", "Acho que a lei tem brechas, mas prevê compensação ambiental. Vai depender da fiscalização.", "5m", 5, false),
                Comment("c3", "Juliana Costa", "@julianacosta", "juliana", "Eu perdi as últimas discussões, onde posso ver o texto do projeto?", "2m", 1, false)
            ),
            likedByUsers = listOf(mockUserAna, mockUserGabriel, mockUserJuliana),
            category = "Política"
        ),
        FeedPost(
            id = "post-2",
            authorName = "Gabriel Ferreira",
            authorUsername = "@gabrielf",
            authorAvatar = "gabriel",
            timeAgo = "1h",
            tag = "Pensamento",
            content = "As decisões coletivas que tomamos hoje moldam as cidades do amanhã. Menos muros, mais pontes de diálogo ativo.",
            isPoll = false,
            likes = 512,
            comments = 2,
            shares = 32,
            postComments = listOf(
                Comment("c3", "Ana Clara", "@anaclara", "ana_clara", "Super concordo, Gabriel!", "45m", 8, false),
                Comment("c4", "Lucas Martins", "@lucasmartins", "lucas", "Precisamos de mais espaços públicos.", "30m", 14, false)
            ),
            likedByUsers = listOf(mockUserAna, mockUserLucas, mockUserBeatriz),
            category = "Vida"
        ),
        FeedPost(
            id = "post-3",
            authorName = "Juliana Costa",
            authorUsername = "@julianacosta",
            authorAvatar = "juliana",
            timeAgo = "2h",
            tag = "Enquete",
            content = "Deveríamos adotar o horário flexível definitivo em todas as esferas de trabalho público e privado?",
            isPoll = true,
            pollOptions = listOf(
                PollOption("opt-3a", "Sim, traz equilíbrio", votes = 836),
                PollOption("opt-3b", "Não, prejudica sincronia", votes = 394)
            ),
            totalVotes = 1230,
            likes = 256,
            comments = 1,
            shares = 23,
            postComments = listOf(
                Comment("c5", "Beatriz Santos", "@beatriz", "beatriz", "Trabalho flexível melhora 100% a saúde mental.", "1h", 20, false)
            ),
            likedByUsers = listOf(mockUserBeatriz, mockUserGabriel),
            category = "Tecnologia"
        ),
        FeedPost(
            id = "post-4",
            authorName = "Lucas Martins",
            authorUsername = "@lucasmartins",
            authorAvatar = "lucas",
            timeAgo = "3h",
            tag = "Pensamento",
            content = "Incrível ver como a mobilidade sustentável e ciclovias bem planejadas transformam a energia das nossas cidades.",
            isPoll = false,
            likes = 189,
            comments = 1,
            shares = 15,
            imageUrl = "https://images.unsplash.com/photo-1541614101331-1a5a3a194e92?w=600&auto=format&fit=crop",
            postComments = listOf(
                Comment("c6", "Juliana Costa", "@julianacosta", "juliana", "Incrível ver essa ciclovia!", "2h", 5, false)
            ),
            likedByUsers = listOf(mockUserJuliana, mockUserAna, mockUserLucas),
            category = "Meio Ambiente"
        )
    )

    override suspend fun getFeed(limit: Int, cursor: String?): List<FeedPost> {
        delay(300) // Simulate network delay
        if (cursor == null) {
            return inMemoryPosts.take(limit)
        }
        val index = inMemoryPosts.indexOfFirst { it.id == cursor }
        if (index == -1 || index == inMemoryPosts.size - 1) return emptyList()
        return inMemoryPosts.drop(index + 1).take(limit)
    }

    override suspend fun createPost(
        content: String,
        isPoll: Boolean,
        options: List<PollOption>,
        category: String,
        imageUrl: String?
    ): FeedPost {
        delay(200)
        val newPost = FeedPost(
            id = "user-post-${UUID.randomUUID()}",
            authorName = "Marina Souza",
            authorUsername = "@marinasouza",
            authorAvatar = "marina",
            timeAgo = "Agora",
            tag = if (isPoll) "Enquete" else "Pensamento",
            content = content,
            isPoll = isPoll,
            pollOptions = options,
            totalVotes = options.sumOf { it.votes },
            likes = 0,
            comments = 0,
            shares = 0,
            category = category,
            imageUrl = imageUrl
        )
        inMemoryPosts.add(0, newPost)
        return newPost
    }

    override suspend fun likePost(postId: String): Boolean {
        delay(100)
        val index = inMemoryPosts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = inMemoryPosts[index]
            val newHasLiked = !post.hasLiked
            val newLikes = if (newHasLiked) post.likes + 1 else post.likes - 1
            inMemoryPosts[index] = post.copy(hasLiked = newHasLiked, likes = newLikes)
            return true
        }
        return false
    }

    override suspend fun unlikePost(postId: String): Boolean {
        return likePost(postId) // Toggle behavior is identical for mock
    }

    override suspend fun vote(pollId: String, optionId: String): Boolean {
        delay(100)
        // Note: pollId in mock is actually postId or similar context
        for (i in inMemoryPosts.indices) {
            val post = inMemoryPosts[i]
            if (post.isPoll) {
                val optIndex = post.pollOptions.indexOfFirst { it.id == optionId }
                if (optIndex != -1 && post.userSelectedOptionId == null) {
                    val updatedOpts = post.pollOptions.map { opt ->
                        if (opt.id == optionId) opt.copy(votes = opt.votes + 1) else opt
                    }
                    inMemoryPosts[i] = post.copy(
                        pollOptions = updatedOpts,
                        totalVotes = post.totalVotes + 1,
                        userSelectedOptionId = optionId
                    )
                    return true
                }
            }
        }
        return false
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        inMemoryPosts.removeAll { it.id == postId }
        return Result.success(Unit)
    }
}
"""
with open('app/src/main/java/com/example/repository/MockPostRepository.kt', 'w') as f:
    f.write(content)
