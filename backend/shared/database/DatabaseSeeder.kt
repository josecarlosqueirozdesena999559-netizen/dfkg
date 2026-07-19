package com.decisoes.shared.database

import org.jetbrains.exposed.sql.*
import java.util.UUID
import java.time.Instant
import org.slf4j.LoggerFactory

object DatabaseSeeder {
    private val logger = LoggerFactory.getLogger(DatabaseSeeder::class.java)

    suspend fun seedIfNeeded() {
        try {
            DatabaseConnector.dbQuery {
        val userCount = UsersTable.selectAll().count()
        if (userCount > 0) {
            logger.info("Database already seeded with {} users. Skipping seeder.", userCount)
            return@dbQuery
        }

        logger.info("Database is empty. Initiating data seeding...")

        // Define UUIDs
        val userIds = List(10) { UUID.randomUUID() }
        val usernames = listOf("anaclara", "gabriel", "lucas", "juliana", "bruno", "carlos", "mariana", "fernanda", "rodrigo", "camila")
        val names = listOf("Ana Clara", "Gabriel Ferreira", "Lucas Souza", "Juliana Costa", "Bruno Alves", "Carlos Eduardo", "Mariana Santos", "Fernanda Lima", "Rodrigo Silva", "Camila Souza")
        val bios = listOf(
            "Apaixonada por decisões inteligentes e debates produtivos.",
            "Desenvolvedor Android e fã de enquetes diárias.",
            "Filósofo nas horas vagas. Gosto de questionar tudo.",
            "Estudante de Direito. Decisões moldam nossa sociedade.",
            "Empreendedor. Decidir rápido é melhor do que não decidir.",
            "Engenheiro de Dados. Gosto de estatísticas e votações.",
            "Arquiteta de soluções. Decisões elegantes salvam projetos.",
            "Amo viajar e fazer enquetes sobre destinos incríveis.",
            "Fotógrafo amador. Posto fotos e perguntas visuais.",
            "Marketing digital. Decisões baseadas em comportamento."
        )

        // 1. Insert Users & Profiles
        userIds.forEachIndexed { idx, uuid ->
            UsersTable.insert {
                it[id] = uuid
                it[firebaseUid] = "firebase_uid_${usernames[idx]}"
                it[email] = "${usernames[idx]}@example.com"
                it[username] = usernames[idx]
                it[status] = "ACTIVE"
            }
            UserProfilesTable.insert {
                it[userId] = uuid
                it[displayName] = names[idx]
                it[bio] = bios[idx]
                it[profileImageUrl] = "https://images.unsplash.com/photo-${1500000000000 + idx}?auto=format&fit=crop&w=150&q=80"
                it[coverImageUrl] = "https://images.unsplash.com/photo-${1600000000000 + idx}?auto=format&fit=crop&w=600&q=80"
                it[verified] = (idx % 3 == 0) // Some verified
            }
        }

        // 2. Insert Follows (Ana Clara follows Gabriel, and vice versa, etc.)
        val followsPairs = listOf(
            0 to 1, 0 to 2, 0 to 3,
            1 to 0, 1 to 2,
            2 to 0, 2 to 1, 2 to 4,
            3 to 0, 4 to 0, 5 to 0,
            6 to 7, 7 to 6, 8 to 9, 9 to 8
        )
        followsPairs.forEach { (from, to) ->
            FollowsTable.insert {
                it[followerId] = userIds[from]
                it[followingId] = userIds[to]
            }
            // Update profile counters
            UserProfilesTable.update({ UserProfilesTable.userId eq userIds[from] }) {
                with(SqlExpressionBuilder) {
                    it[UserProfilesTable.followingCount] = UserProfilesTable.followingCount + 1
                }
            }
            UserProfilesTable.update({ UserProfilesTable.userId eq userIds[to] }) {
                with(SqlExpressionBuilder) {
                    it[UserProfilesTable.followersCount] = UserProfilesTable.followersCount + 1
                }
            }
        }

        // 3. Insert Posts & Polls
        val postsData = listOf(
            // TEXT & IMAGE posts
            Triple("TEXT", "Qual a sua opinião sobre o uso de IA no desenvolvimento de software de alta escala?", userIds[0]),
            Triple("TEXT", "A arquitetura de microserviços às vezes é superestimada. Quem concorda?", userIds[1]),
            Triple("IMAGE", "Olha essa foto que tirei hoje no parque! O que vocês preferem: montanha ou praia?", userIds[8]),
            Triple("QUESTION", "Como vocês lidam com prazos apertados em projetos críticos?", userIds[3]),
            Triple("TEXT", "Decidir é abrir mão de outras alternativas. Uma reflexão sobre foco.", userIds[4])
        )

        val postIds = mutableListOf<UUID>()
        postsData.forEach { (type, content, author) ->
            val pId = UUID.randomUUID()
            postIds.add(pId)
            PostsTable.insert {
                it[id] = pId
                it[authorId] = author
                it[PostsTable.type] = type
                it[PostsTable.content] = content
            }
            UserProfilesTable.update({ UserProfilesTable.userId eq author }) {
                with(SqlExpressionBuilder) {
                    it[UserProfilesTable.postsCount] = UserProfilesTable.postsCount + 1
                }
            }
        }

        // Add some POLL posts
        val pollPosts = listOf(
            "Qual o melhor framework backend para Kotlin?" to listOf("Ktor", "Spring Boot", "Micronaut", "Quarkus"),
            "Onde você prefere passar as férias coletivas?" to listOf("Montanhas Nevadas", "Praia Tropical", "Cidade Histórica"),
            "Com que frequência você estuda novas tecnologias?" to listOf("Diariamente", "Semanalmente", "Mensalmente", "Raramente"),
            "Qual banco de dados relacional você mais utiliza?" to listOf("PostgreSQL", "MySQL", "Oracle", "SQL Server"),
            "Qual padrão arquitetural você prefere no Android?" to listOf("MVVM", "MVI", "MVP", "Clean Architecture"),
            "Se você pudesse escolher apenas uma rede social:" to listOf("Decisões", "LinkedIn", "Instagram", "Threads"),
            "Quem deve vencer o próximo debate da comunidade?" to listOf("Grupo A", "Grupo B", "Abstenção"),
            "Qual sistema operacional você prefere para codificar?" to listOf("Linux", "macOS", "Windows"),
            "Prefere trabalhar de forma remota, híbrida ou presencial?" to listOf("Remoto", "Híbrido", "Presencial"),
            "Qual paradigma de programação você mais gosta?" to listOf("Funcional", "Orientado a Objetos", "Reativo", "Procedural")
        )

        pollPosts.forEachIndexed { i, (questionText, options) ->
            val pId = UUID.randomUUID()
            postIds.add(pId)
            val author = userIds[i % 10]
            
            PostsTable.insert {
                it[id] = pId
                it[authorId] = author
                it[type] = "POLL"
                it[content] = questionText
            }

            val createdPollId = UUID.randomUUID()
            PollsTable.insert {
                it[id] = createdPollId
                it[postId] = pId
                it[question] = questionText
                it[expiresAt] = Instant.now().plusSeconds(86400 * 30) // Expira em 30 dias
            }

            val optIds = options.mapIndexed { optIdx, optText ->
                val oId = UUID.randomUUID()
                PollOptionsTable.insert {
                    it[id] = oId
                    it[pollId] = createdPollId
                    it[text] = optText
                    it[displayOrder] = optIdx
                }
                oId
            }

            // Let some users vote on this poll
            val votersCount = (3..7).random()
            val shuffledVoters = userIds.shuffled().take(votersCount)
            shuffledVoters.forEach { voterId ->
                val chosenOptIdx = (0 until options.size).random()
                VotesTable.insert {
                    it[id] = UUID.randomUUID()
                    it[pollId] = createdPollId
                    it[optionId] = optIds[chosenOptIdx]
                    it[userId] = voterId
                }
                
                // Update votes counts
                PollOptionsTable.update({ PollOptionsTable.id eq optIds[chosenOptIdx] }) {
                    with(SqlExpressionBuilder) {
                        it[PollOptionsTable.votesCount] = PollOptionsTable.votesCount + 1
                    }
                }
                PollsTable.update({ PollsTable.id eq createdPollId }) {
                    with(SqlExpressionBuilder) {
                        it[PollsTable.totalVotes] = PollsTable.totalVotes + 1
                    }
                }
            }

            UserProfilesTable.update({ UserProfilesTable.userId eq author }) {
                with(SqlExpressionBuilder) {
                    it[UserProfilesTable.postsCount] = UserProfilesTable.postsCount + 1
                }
            }
        }

        // 4. Create Likes
        postIds.forEach { pId ->
            val likers = userIds.shuffled().take((0..5).random())
            likers.forEach { likerId ->
                PostLikesTable.insert {
                    it[postId] = pId
                    it[userId] = likerId
                }
                PostsTable.update({ PostsTable.id eq pId }) {
                    with(SqlExpressionBuilder) {
                        it[PostsTable.likesCount] = PostsTable.likesCount + 1
                    }
                }
            }
        }

        // 5. Create Comments
        postIds.take(5).forEach { pId ->
            val commentsList = listOf(
                "Excelente colocação!",
                "Não concordo muito com isso, mas respeito.",
                "Podemos marcar uma conversa sobre isso?",
                "Esse assunto é de extrema relevância ultimamente.",
                "Sensacional, concordo em gênero, número e grau."
            )
            commentsList.forEachIndexed { commentIdx, commentText ->
                val author = userIds[(commentIdx + 2) % 10]
                val commentId = UUID.randomUUID()
                CommentsTable.insert {
                    it[id] = commentId
                    it[postId] = pId
                    it[authorId] = author
                    it[content] = commentText
                }
                PostsTable.update({ PostsTable.id eq pId }) {
                    with(SqlExpressionBuilder) {
                        it[PostsTable.commentsCount] = PostsTable.commentsCount + 1
                    }
                }

                // Add a reply to some comments
                if (commentIdx == 0) {
                    CommentsTable.insert {
                        it[id] = UUID.randomUUID()
                        it[postId] = pId
                        it[authorId] = userIds[0]
                        it[parentCommentId] = commentId
                        it[content] = "Obrigada pelo feedback!"
                    }
                    PostsTable.update({ PostsTable.id eq pId }) {
                        with(SqlExpressionBuilder) {
                            it[PostsTable.commentsCount] = PostsTable.commentsCount + 1
                        }
                    }
                }
            }
        }

        logger.info("Database seeding completed successfully. 10 users, follow connections, posts, polls, votes, likes and comments are initialized.")
        }
    } catch (e: Exception) {
        logger.info("Database seeding skipped or aborted (likely due to concurrent initialization): \${e.message}")
    }
}}
