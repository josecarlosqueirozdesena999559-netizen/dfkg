# Decisões - Backend (Fase 1)

Este é o projeto de fundação do backend e banco de dados para a rede social **Decisões**.

Desenvolvido em **Kotlin** utilizando o framework **Ktor**, o banco relacional **PostgreSQL** e migrações de esquema automáticas via **Flyway**.

---

## Arquitetura e Estrutura

O projeto segue uma arquitetura modular limpa e organizada:

```
backend/
├── app/                  # Ponto de entrada do Ktor
├── modules/              # Módulos funcionais encapsulados (Routes, Services, Repositories)
│   ├── auth/             # Segurança e autenticação
│   ├── users/            # Gerenciamento de usuários e perfis
│   ├── posts/            # CRUD de publicações (Texto, imagem, enquetes)
│   ├── polls/            # Estrutura lógica de enquetes
│   ├── comments/         # Comentários e respostas encadeadas
│   └── follows/          # Relacionamentos de seguidores (Seguir/Deixar de seguir)
├── shared/               # Recursos e utilitários compartilhados
│   ├── database/         # Conectores, Schemas Exposed e Seeder de dados
│   ├── security/         # Middleware de validação Firebase Token
│   └── errors/           # Interceptador e formatador global de erros
├── src/main/resources/   # Recursos estáticos (Logback e Flyway migrations)
├── Dockerfile            # Configuração de build Docker em estágios
├── docker-compose.yml    # Orquestração do backend e banco Postgres local
└── .env.example          # Modelo de configuração de ambiente
```

---

## Tecnologias Utilizadas

- **Kotlin** (v2.2.10)
- **Ktor** (v3.1.0)
- **PostgreSQL** (v15)
- **Exposed ORM** (v0.58.0)
- **HikariCP** (v6.2.1)
- **Flyway** (v10.22.0)
- **Firebase Admin SDK** (v9.4.3)
- **Logback Classic** (v1.5.16)
- **Docker & Docker Compose**

---

## Como Executar Localmente (Docker)

A forma recomendada para executar o projeto com todas as dependências locais configuradas é via Docker Compose.

### Passo 1: Configurar Variáveis de Ambiente
Copie o arquivo `.env.example` para `.env`:
```bash
cp .env.example .env
```

### Passo 2: Inicializar os Serviços
Execute o Docker Compose para baixar a imagem do PostgreSQL, construir a imagem do backend e iniciar a aplicação:
```bash
docker compose up --build
```

O servidor estará disponível em: `http://localhost:8080`

---

## Dados de Seed Integrados (Desenvolvimento)
Ao iniciar o backend pela primeira vez com o banco limpo, o seeder automático (`DatabaseSeeder`) irá popular o PostgreSQL com:
- **10 Perfis de Usuários** realistas (com biografia e avatares Unsplash).
- **Conexões de Seguidores** bidirecionais e assimétricas.
- **20+ Publicações** variadas (texto simples, imagens, perguntas, etc).
- **10 Enquetes completas** de múltipla escolha com votos simulados distribuídos.
- **Comentários de posts** e respostas encadeadas (Nested replies).
- **Curtidas** distribuídas aleatoriamente.

---

## Fluxo de Autenticação com Firebase

1. O aplicativo Android realiza o login diretamente no Firebase Authentication.
2. O app obtém o **ID Token** gerado pelo Firebase.
3. Toda requisição para rotas protegidas deve conter o cabeçalho:
   ```http
   Authorization: Bearer <FIREBASE_ID_TOKEN>
   ```
4. O backend Ktor intercepta a requisição, valida o token via Firebase Admin SDK, obtém o Firebase UID e vincula-o ao usuário no PostgreSQL.
5. Se for o primeiro acesso daquele usuário, a conta e o perfil do banco Postgres serão criados de forma transparente e automática.

### Modo Bypass para Desenvolvimento (Sem Token Real)
Para facilitar o desenvolvimento do aplicativo e testes de endpoints (sem depender do Firebase ID Token), você pode ativar a flag `DEV_BYPASS_AUTH=true` nas variáveis de ambiente.

Dessa forma, você pode fazer chamadas enviando um token de mentira com o prefixo `dev_token_` seguido do nome de usuário:
```http
Authorization: Bearer dev_token_anaclara
```
O backend decodificará essa requisição como se fosse o usuário `@anaclara`, buscando ou criando seu perfil de forma imediata!

---

## Endpoints Disponíveis

### Verificação de Saúde
- `GET /health` - Retorna se o servidor e o banco PostgreSQL estão operacionais.

### Perfis de Usuários (Users)
- `GET /api/v1/me` *(Protegido)* - Retorna os dados do usuário ativo autenticado e seu perfil.
- `GET /api/v1/users/{username}` *(Público/Autenticado)* - Detalhes do perfil público de qualquer usuário cadastrado.
- `PUT /api/v1/me/profile` *(Protegido)* - Atualiza os dados de exibição (`display_name`, `bio`, `profile_image_url`, `cover_image_url`).

### Publicações (Posts)
- `GET /api/v1/posts/{postId}` *(Protegido)* - Detalhes completos da publicação (e enquetes/opções associadas).
- `POST /api/v1/posts` *(Protegido)* - Cria uma nova publicação. Suporta tipos `TEXT`, `IMAGE`, `QUESTION` e `POLL`.
- `DELETE /api/v1/posts/{postId}` *(Protegido)* - Remove uma publicação ativa pertencente ao usuário atual.

### Curtidas (Likes)
- `POST /api/v1/posts/{postId}/like` *(Protegido)* - Adiciona curtida.
- `DELETE /api/v1/posts/{postId}/like` *(Protegido)* - Remove curtida.

### Enquetes (Polls)
- `POST /api/v1/polls/{pollId}/vote` *(Protegido)* - Registra voto em uma opção da enquete. Impede votos duplicados e votos em enquetes expiradas.

### Comentários (Comments)
- `GET /api/v1/posts/{postId}/comments` *(Protegido)* - Retorna a lista de comentários associados à publicação.
- `POST /api/v1/posts/{postId}/comments` *(Protegido)* - Adiciona um comentário (suporta threaded replies com `parent_comment_id`).

### Seguidores (Follows)
- `POST /api/v1/users/{userId}/follow` *(Protegido)* - Segue um usuário. Impede auto-seguir.
- `DELETE /api/v1/users/{userId}/follow` *(Protegido)* - Deixa de seguir um usuário.
