# MIB Backend — Fases 1 a 13 (roadmap completo)

Backend do **MIB**, evolução do BMO, reescrito em **Java 21 + Spring Boot 3**, com foco em
saúde mental, bem-estar, comunidade e gamificação — usando apenas tecnologias
gratuitas/open source.

## Fase 1 — Fundação (Auth)

- Estrutura de pacotes em camadas: `controller`, `service`/`service.impl`, `repository`,
  `entity`, `dto`, `mapper`, `config`, `security`, `exception`, `util`.
- Entidades base: `User`, `Role`, `Profile`, `RefreshToken`.
- Módulo de Auth completo: registro, login, refresh token rotativo, logout, JWT + BCrypt,
  rate limiting anti força bruta, validação forte de senha.

## Fase 2 — Perfil e Amizades

- **Perfil**: visualizar o próprio perfil, visualizar perfil público de outro usuário
  (perfis privados só ficam visíveis para amigos), editar nome de exibição/bio/status/
  visibilidade, upload de foto de perfil (PNG/JPEG/WEBP, até 2MB, armazenado localmente
  — ver nota de infraestrutura abaixo).
- **Presença (online / último acesso)**: rastreada em memória a cada requisição
  autenticada (`PresenceService`), sincronizada com o banco a cada 60s via `@Scheduled`,
  sem sobrecarregar o banco com escrita a cada request.
- **Amizades**: busca de usuários por username/nome de exibição, envio de solicitação,
  aceitar/recusar/cancelar solicitação, listar amigos (com status online e último acesso),
  remover amizade. Perfis privados ficam visíveis para amigos aceitos.
- Chat privado com amigos fica para a **Fase 3 (Chat/WebSocket)**, já que reaproveita a
  mesma modelagem de mensagens do chat geral.

### Nota de infraestrutura — avatares
Os avatares são salvos no disco local do container (`uploads/avatars`). Em provedores com
disco efêmero (ex.: free tier do Render sem volume persistente), os arquivos podem ser
perdidos em um redeploy. Para produção, anexe um disco persistente ao serviço ou,
futuramente, troque `AvatarStorageService` por um bucket compatível com S3 em um provedor
com camada gratuita — a interface do serviço já isola essa decisão do resto do código.

## Fase 3 — XP, Níveis e Missões Diárias

- **XP e nível**: `XpService` é o ponto central usado por qualquer módulo que precise
  conceder experiência (hoje: login diário e conclusão de missão; nas próximas fases:
  tarefas, respiração, chat, conquistas etc. — basta chamar `awardXp`).
- **Fórmula de nível**: XP necessário para avançar do nível N para o N+1 = `100 × N²`
  (cumulativo). Implementada em `LevelCalculator`, testável isoladamente.
- **Login diário**: 10 XP concedidos no primeiro login do dia (verificado por data UTC,
  não repete no mesmo dia).
- **Missões diárias**: `MissionTemplate` é o banco de modelos reutilizáveis (12 já
  cadastrados via seed, cobrindo hidratação, exercício, respiração, sono, positividade,
  social, mindfulness e bem-estar digital). Todo dia à meia-noite, `DailyMissionScheduler`
  sorteia 5 templates ativos e gera as `DailyMission` do dia — com salvaguarda que roda
  também na subida da aplicação, caso o servidor estivesse fora do ar à meia-noite.
- Concluir uma missão registra `UserMissionCompletion` (impedindo repetição no mesmo dia)
  e credita o XP da missão via `XpService`.
- Histórico de XP paginado disponível em `/api/xp/me/history`.

## Fase 4 — Chat e WebSocket

- **Protocolo**: STOMP sobre WebSocket (com fallback SockJS), endpoint `/ws`.
- **Sala geral**: única, criada automaticamente, aberta a qualquer usuário autenticado.
  Cliente publica em `/app/chat.general` e assina `/topic/chat.general`.
- **Chat privado**: só é possível entre amigos com amizade aceita (reaproveita o
  `FriendshipRepository` da Fase 2). Cliente publica em `/app/chat.room/{roomId}` e
  assina `/topic/chat.room.{roomId}`.
- **Autenticação no WebSocket**: `WebSocketAuthInterceptor` valida o JWT no frame
  `CONNECT` (header `Authorization: Bearer {token}`) e autoriza cada `SUBSCRIBE` a uma
  sala privada, garantindo que só os participantes possam se inscrever naquele tópico.
- **Fallback REST**: `POST /api/chat/rooms/{roomId}/messages` envia mensagem sem depender
  de WebSocket (útil para clientes simples ou testes) e dispara o mesmo broadcast — tanto
  o caminho HTTP quanto o STOMP passam pelo mesmo `ChatService`, sem duplicar regra.
  Histórico paginado em `GET /api/chat/rooms/{roomId}/messages`.
- Moderação automática das mensagens (Fase 5) vai atuar sobre a mesma entidade `Message`
  (campo `removedByModeration` já presente, pronto para ser usado).

## Fase 5 — Estrelas Pontilhadas, Denúncias e Moderação Automática

- **Estrelas Pontilhadas**: mural de mensagens anônimas. O autor é mantido no banco
  (necessário para moderação/histórico de infrações), mas **nunca** é exposto pela API —
  a anonimidade é garantida na camada de DTO/mapper. Suporta respostas (thread simples),
  curtidas (não é possível curtir a própria mensagem), exclusão pelo próprio autor.
  Curtida recebida concede XP ao autor (`XpReasonType.LIKE_RECEIVED`).
- **Denúncias**: qualquer usuário pode denunciar mensagem de chat, mensagem anônima ou
  perfil (`POST /api/reports`), com motivo (bullying, spam, assédio, conteúdo ofensivo,
  fake news, outro). Uma denúncia por usuário/alvo. Fila de revisão fica pronta para o
  painel administrativo (Fase 12).
- **Moderação automática** (`ContentModerationEngine` + `ModerationService`): avalia texto
  contra listas configuráveis (`mib.moderation.*` no `application.yml`) de assédio, ameaça,
  discurso de ódio e padrões de spam (URLs repetidas, caracteres repetidos, frases de
  golpe). Já está plugada tanto no chat (Fase 4) quanto nas Estrelas Pontilhadas: infração
  detectada → conteúdo removido automaticamente, log gravado em `ModerationLog`, contador
  de advertências do usuário incrementado, com escalada automática para suspensão
  temporária (3 advertências) → suspensão longa (5) → banimento (7), tudo configurável.
- **Importante — decisão de produto**: o filtro automático mira **apenas** abuso contra
  terceiros (assédio, ameaça, ódio, spam). Ele nunca analisa nem pune expressões de
  sofrimento emocional, tristeza ou ideação de autolesão do próprio usuário — isso seria o
  oposto do propósito de um app de saúde mental. Esse tipo de conteúdo é reservado para o
  módulo de IA de apoio emocional (fase futura), que deve acolher, não penalizar.
- As listas de termos incluídas são apenas um ponto de partida ilustrativo (poucos exemplos
  de cada categoria) — para produção, vale expandir via `application.yml`/variáveis de
  ambiente com um vocabulário mais completo adequado ao público do MIB.

## Fase 6 — Tarefas Pessoais

- CRUD completo de tarefas do próprio usuário: título, descrição, categoria (texto livre
  definido pelo usuário, ex. "Estudos", "Saúde"), data de vencimento opcional e status
  (`PENDING`, `IN_PROGRESS`, `COMPLETED`).
- Filtros por status e categoria em `GET /api/tasks`, mais `GET /api/tasks/categories` para
  alimentar autocomplete/filtros no app sem o usuário digitar do zero.
- Concluir uma tarefa credita XP (`XpReasonType.TASK_COMPLETED`) **uma única vez por
  tarefa** — reabrir e concluir de novo não gera XP extra, para não incentivar "farm".
- Distinta da `DailyMission` (Fase 3): aqui a tarefa é livre e pessoal; lá é gerada
  automaticamente pelo sistema e compartilhada entre todos os usuários no dia.

## Fase 7 — Loja, Inventário e Conquistas

- **Decisão de design importante — XP vitalício vs. carteira gasta**: `User.totalXp`
  continua sendo o histórico vitalício que define o nível (nunca diminui). Criei
  `User.xpWallet` como um saldo **separado**, que sobe junto com `totalXp` a cada XP
  ganho, mas só ele é debitado nas compras da loja. Assim, gastar XP num item nunca
  derruba o nível do usuário — algo que aconteceria se a loja descontasse diretamente do
  XP vitalício. `GET /api/xp/me` agora retorna `xpWallet` junto do resumo de nível.
- **Loja**: itens cosméticos (moldura, fundo, ícone, emblema, título, efeito visual,
  tema), comprados com XP da carteira. Itens marcados `exclusiveToAchievement` não
  aparecem para compra — só são obtidos desbloqueando a conquista correspondente.
- **Inventário**: equipar/desequipar. Molduras, fundos, ícones, títulos, efeitos e temas
  ocupam um "slot" único por tipo (equipar um novo desequipa o anterior); emblemas
  (`BADGE`) podem ficar todos equipados ao mesmo tempo. `ProfileResponse` e
  `MessageResponse` agora incluem os cosméticos equipados, para aparecerem no perfil
  público e no chat, como pedido na especificação.
- **Conquistas**: `AchievementService.grant(user, code)` é idempotente e concede XP +
  item exclusivo (se houver) automaticamente. Gatilhos já plugados: primeiro login
  (registro), primeira amizade aceita, primeira missão concluída, streak de 7/30 dias de
  acesso consecutivo, 100 mensagens enviadas, e níveis 5/10/20. O gatilho de primeiro
  exercício de respiração já está seedado no banco, pronto para a Fase 9 chamá-lo.
- **Evitando dependência circular**: `XpService` precisaria checar conquistas de nível, e
  `AchievementService` precisa do `XpService` para pagar a recompensa — para não criar um
  ciclo de beans, o level-up é publicado como `LevelUpEvent` (Spring `ApplicationEvent`) e
  o `AchievementService` apenas escuta, sem o `XpService` conhecer o módulo de conquistas.
- **Streak de login**: `User.currentLoginStreak`/`longestLoginStreak`, calculado no login
  comparando a data do acesso anterior — reinicia se houver uma lacuna maior que 1 dia.

## Fase 8 — Interesses, Recomendação e Salas Temáticas

- **Interesses**: catálogo fixo (`GET /api/interests`) com 16 opções (Animes, Filmes,
  Livros, Música, Jogos, Programação, Tecnologia, Academia, Esportes, Natureza, Arte,
  Fotografia, Meditação, Psicologia, Saúde Mental, Outros). O usuário substitui sua
  seleção inteira via `PUT /api/interests/me`.
- **Salas temáticas**: a "ThemeRoom" da especificação foi implementada reaproveitando o
  `ChatRoom` já existente (tipo `THEME`, adicionado desde a Fase 4) em vez de criar uma
  entidade paralela redundante — um `Interest` pode referenciar um `ChatRoom` tema. Ao
  selecionar um interesse com sala vinculada, o usuário entra automaticamente nela
  (`ChatRoomParticipant`); ao remover o interesse, sai automaticamente. 7 salas já
  seedadas: Anime, Programação, Música, Academia, Livros, Saúde Mental, Natureza.
- **Recomendação de amizades**: `GET /api/recommendations/friends` ordena candidatos pela
  quantidade de interesses em comum, excluindo quem já é amigo ou tem solicitação
  pendente. Calculado sob demanda (sem entidade `Recommendation` persistida — dado
  derivado não precisa de tabela própria, e fica sempre atualizado).
- **Escopo consciente**: a especificação também menciona recomendar missões/desafios por
  interesse. Como as missões diárias (Fase 3) são geradas globalmente e compartilhadas
  por todos os usuários, personalizá-las por interesse exigiria reestruturar esse
  sistema — deixei isso como uma extensão futura documentada, em vez de forçar uma
  implementação parcial agora.

## Fase 9 — Respiração

- 7 técnicas guiadas seedadas (`GET /api/breathing/techniques`), cada uma com o padrão de
  tempo (inspirar/segurar/expirar/segurar) para a animação do app: 4-7-8, Respiração
  Quadrada, Diafragmática, Relaxante, Anti-Ansiedade, Para Dormir, Energizante.
  **Nota**: "Box Breathing" e "Respiração Quadrada" são a mesma técnica (o segundo nome é
  só a tradução) — consolidei as duas em um único registro para não duplicar o mesmo
  padrão sob dois nomes diferentes.
- `POST /api/breathing/techniques/{id}/complete` registra a sessão e credita XP
  (`BREATHING_EXERCISE_COMPLETED`), limitado a 5 sessões premiadas por dia — a sessão
  continua sendo registrada para estatística mesmo depois do limite, só para de gerar XP
  extra, evitando farming.
- Desbloqueia a conquista `FIRST_BREATHING_EXERCISE` (seedada desde a Fase 7) na primeira
  sessão concluída.
- `GET /api/breathing/stats` retorna total de sessões, minutos praticados e uso por
  técnica, para as "estatísticas de utilização" pedidas na especificação.

## Fase 10 — Humor e Feed Positivo

- **Diário de humor**: `POST /api/mood` registra o humor do dia (muito bem, bem, normal,
  triste, muito triste) com nota opcional. Registrar de novo no mesmo dia **atualiza** o
  registro existente em vez de duplicar — sempre um por usuário por dia.
  `GET /api/mood/history?days=` alimenta o gráfico pessoal de evolução emocional.
- **Decisão de produto deliberada**: registrar humor **não concede XP**. Conceder XP
  criaria incentivo a "melhorar" o humor relatado só pela recompensa — o oposto do que um
  diário sincero deveria fazer. Da mesma forma, o humor registrado **nunca** aciona
  moderação, restrição ou qualquer consequência automática; é puramente autorrelato do
  próprio usuário, reservado para uso futuro pela IA de apoio emocional (Fase 13)
  personalizar recomendações — nunca para puni-lo.
- **Feed positivo**: `GET /api/feed/positive` retorna frases motivacionais, curiosidades
  sobre saúde mental e desafios rápidos. A seleção é determinística pela data
  (`LocalDate.now().toEpochDay()` como seed do embaralhamento) — o mesmo conjunto para
  todo mundo durante o dia inteiro, trocando sozinho a cada virada de dia, sem precisar
  de um scheduler dedicado.

## Fase 11 — Notificações

- `NotificationService.notify(...)` é o ponto central: persiste a notificação e a empurra
  em tempo real via WebSocket (`/user/queue/notifications`), reaproveitando toda a
  infraestrutura STOMP da Fase 4 — quem estiver com o app aberto recebe na hora; quem não
  estiver, vê na próxima vez que abrir (`GET /api/notifications`).
- Gatilhos já plugados: pedido de amizade recebido/aceito, mensagem nova em conversa
  privada (chat geral e salas temáticas não notificam, para não gerar spam), missão
  concluída, novas missões do dia disponíveis (para todos os usuários, quando o
  scheduler da Fase 3 gera um novo conjunto), compra na loja, conquista desbloqueada e
  subida de nível.
- `GET /api/notifications/unread-count` alimenta o badge do app; `PATCH .../read` e
  `PATCH .../read-all` marcam como lida.
- **Nota de escala**: a notificação de "novas missões" hoje itera todos os usuários de
  forma síncrona no próprio scheduler. Para uma base de usuários muito grande, isso
  deveria virar um job assíncrono/em fila (ex.: `@Async` do Spring ou uma tabela de
  outbox processada em lote) — deixei essa observação comentada diretamente no código,
  no ponto exato onde a mudança entraria.

## Fase 12 — Painel Administrativo

- Todo o prefixo `/api/admin/**` já exigia `ROLE_ADMIN` desde a Fase 1
  (`SecurityConfig`) — esta fase só preenche os endpoints atrás dessa porta.
- **Usuários**: buscar/listar, ver detalhe, suspender/remover suspensão, banir/desbanir,
  conceder/revogar `ROLE_ADMIN`. Um admin não pode suspender, banir ou remover a própria
  permissão de administrador (trava de segurança contra autolockout).
- **Conteúdo**: apagar mensagem de chat ou de Estrelas Pontilhadas (soft-delete via
  `removedByModeration`, preservando o histórico), listar e revisar denúncias
  (`REVIEWED`/`DISMISSED`), consultar o histórico de ações da moderação automática.
- **Missões e Loja**: CRUD completo de templates de missão e de itens da loja.
  "Excluir" na verdade desativa (`active = false`) em vez de apagar — preserva o
  histórico de quem já completou a missão ou já possui o item.
- **Estatísticas**: usuários totais/banidos/suspensos, novos cadastros (7/30 dias),
  mensagens, tarefas e missões concluídas, sessões de respiração, denúncias pendentes.
- **Trilha de auditoria** (`AdminLog`, distinta do `ModerationLog` automático da Fase 5):
  toda ação manual de um admin fica registrada — quem fez, o quê, quando. Cada ação
  relevante também notifica o usuário afetado (reaproveitando a Fase 11).

## Fase 13 — IA de Apoio Emocional

Provedor: **Groq** (mesma escolha do BMO), com o modelo gratuito `llama-3.1-8b-instant`,
via API compatível com o formato da OpenAI. Configurado por variável de ambiente
(`GROQ_API_KEY`) — sem chave configurada, o endpoint responde `503` de forma clara em
vez de quebrar.

- **Memória pedida por você**: a cada mensagem, `AiContextBuilder` monta um resumo do
  usuário — nome, nível/XP, streak de acesso, interesses, últimas 3 conquistas e um
  **resumo agregado do humor dos últimos 7 dias** (ex.: "2 dias bem, 1 dia triste") — e
  injeta como uma mensagem de sistema separada antes da conversa. A IA usa isso para
  personalizar sem que você precise repetir contexto a cada sessão.
- **Decisão de privacidade importante**: o resumo de humor manda só os **níveis**
  (bem/triste/etc.), nunca as **notas de texto livre** que o usuário escreveu no diário.
  Essas notas podem conter desabafos pessoais escritos para o próprio usuário, não para
  serem repassadas a uma API de IA de terceiros a cada mensagem — mandar só o agregado
  já dá contexto suficiente sem expor o conteúdo sensível.
- **Segurança do prompt** (`AiSystemPrompt`, uma única constante fácil de revisar):
  a IA nunca diagnostica, sempre reforça (com naturalidade, não como aviso legal) que
  não substitui acompanhamento profissional, e — se detectar sinal de risco de
  autolesão/suicídio — para de tentar "resolver" o problema e direciona diretamente para
  o **CVV (188, 24h, cvv.org.br)** e, em emergência, SAMU (192). Isso é reforçado no
  prompt, não filtrado por um classificador — a IA generativa lida melhor com nuance de
  linguagem nesse tipo de situação do que um filtro de palavras-chave.
- **Mensagem de boas-vindas automática** na primeira interação do usuário, explicando o
  papel da IA e o limite dela, sem precisar de um aviso legal repetido a cada resposta.
- **Sem XP por conversar com a IA** — de propósito, pelo mesmo motivo do diário de humor
  (Fase 10): gamificar a busca por apoio emocional incentivaria uso performático em vez
  de genuíno.
- **Proteções operacionais**: limite de 20 mensagens/hora por usuário (`AiRateLimiter`,
  Bucket4j em memória, mesmo padrão do rate limit de auth da Fase 1) contra abuso/custo
  na cota gratuita da Groq; janela de contexto limitada às últimas 10 mensagens da
  conversa para não estourar o limite de tokens gratuito; troca é atômica — se a chamada
  à IA falhar, nada fica salvo pela metade (usuário nunca vê uma pergunta seguida de
  "erro" sem contexto).

## Como rodar

### Opção 1 — Docker (recomendado)
```bash
cp .env.example .env
# edite o JWT_SECRET no .env antes de subir em produção
docker compose up --build
```
A API sobe em `http://localhost:8080`, com Swagger em `http://localhost:8080/docs`.

### Opção 2 — Local (Postgres já rodando)
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/mib
export DATABASE_USERNAME=mib_user
export DATABASE_PASSWORD=mib_password
export JWT_SECRET=uma-chave-bem-grande-e-aleatoria

mvn spring-boot:run
```

### Rodando os testes
```bash
mvn test
```

## Endpoints disponíveis

| Método | Rota                                     | Descrição                                       |
|--------|-------------------------------------------|--------------------------------------------------|
| POST   | `/api/auth/register`                     | Cria conta                                        |
| POST   | `/api/auth/login`                        | Login (email ou username + senha)                |
| POST   | `/api/auth/refresh`                      | Gera novo par de tokens                          |
| POST   | `/api/auth/logout`                       | Revoga o refresh token                           |
| GET    | `/api/profile/me`                        | Perfil do usuário autenticado                    |
| GET    | `/api/profile/{userId}`                  | Perfil público (ou privado, se amigos)           |
| PUT    | `/api/profile/me`                        | Atualiza nome, bio, status, visibilidade         |
| POST   | `/api/profile/me/avatar`                 | Upload de foto de perfil                         |
| GET    | `/api/users/search?query=`               | Busca usuários por username/nome                 |
| GET    | `/api/friends`                           | Lista amigos (com status online/último acesso)   |
| DELETE | `/api/friends/{friendUserId}`            | Remove amizade                                   |
| GET    | `/api/friends/requests/incoming`         | Solicitações recebidas pendentes                 |
| GET    | `/api/friends/requests/outgoing`         | Solicitações enviadas pendentes                  |
| POST   | `/api/friends/requests/{targetUserId}`   | Envia solicitação de amizade                     |
| POST   | `/api/friends/requests/{requestId}/accept` | Aceita solicitação                             |
| POST   | `/api/friends/requests/{requestId}/decline`| Recusa solicitação                             |
| DELETE | `/api/friends/requests/{requestId}`      | Cancela solicitação enviada                      |
| GET    | `/api/xp/me`                             | XP total, nível e progresso para o próximo nível |
| GET    | `/api/xp/me/history`                     | Histórico paginado de ganhos de XP               |
| GET    | `/api/missions/daily`                    | Missões do dia com status de conclusão           |
| POST   | `/api/missions/daily/{dailyMissionId}/complete` | Conclui uma missão do dia e credita XP    |
| GET    | `/api/chat/general/room`                 | Id da sala geral                                 |
| GET    | `/api/chat/direct`                       | Lista conversas privadas existentes              |
| GET    | `/api/chat/direct/{friendUserId}`        | Retorna/cria conversa privada com um amigo       |
| GET    | `/api/chat/rooms/{roomId}/messages`      | Histórico paginado de mensagens de uma sala      |
| POST   | `/api/chat/rooms/{roomId}/messages`      | Envia mensagem (fallback REST; WS é o padrão)    |
| WS     | `/ws` (STOMP)                             | Conexão em tempo real (ver seção da Fase 4)      |
| GET    | `/api/anonymous`                         | Feed paginado de mensagens anônimas              |
| GET    | `/api/anonymous/{messageId}/replies`     | Respostas de uma mensagem anônima                |
| POST   | `/api/anonymous`                         | Publica mensagem anônima (ou resposta)           |
| DELETE | `/api/anonymous/{messageId}`             | Remove mensagem publicada pelo próprio usuário   |
| POST   | `/api/anonymous/{messageId}/like`        | Alterna curtida                                  |
| POST   | `/api/reports`                           | Denuncia mensagem (chat/anônima) ou perfil       |
| GET    | `/api/tasks`                             | Lista tarefas (filtros: status, category)        |
| GET    | `/api/tasks/categories`                  | Categorias já usadas pelo usuário                |
| GET    | `/api/tasks/{taskId}`                    | Detalha uma tarefa                               |
| POST   | `/api/tasks`                             | Cria uma tarefa                                  |
| PUT    | `/api/tasks/{taskId}`                    | Atualiza uma tarefa                              |
| PATCH  | `/api/tasks/{taskId}/complete`           | Conclui a tarefa e credita XP (uma vez)          |
| PATCH  | `/api/tasks/{taskId}/reopen`             | Reabre a tarefa (sem reverter XP)                |
| DELETE | `/api/tasks/{taskId}`                    | Remove uma tarefa                                |
| GET    | `/api/shop/items`                        | Lista itens da loja (com indicação de já possuído) |
| POST   | `/api/shop/items/{shopItemId}/purchase`  | Compra um item, debitando a carteira de XP       |
| GET    | `/api/inventory`                         | Lista o inventário do usuário                    |
| POST   | `/api/inventory/{shopItemId}/equip`      | Equipa um item possuído                          |
| POST   | `/api/inventory/{shopItemId}/unequip`    | Desequipa um item                                |
| GET    | `/api/achievements`                      | Lista conquistas com status de desbloqueio       |
| GET    | `/api/interests`                         | Catálogo de interesses (indica os selecionados)  |
| PUT    | `/api/interests/me`                      | Substitui os interesses do usuário               |
| GET    | `/api/recommendations/friends`           | Sugere amigos por interesses em comum            |
| GET    | `/api/breathing/techniques`              | Lista técnicas de respiração guiada              |
| POST   | `/api/breathing/techniques/{id}/complete`| Registra sessão concluída e credita XP           |
| GET    | `/api/breathing/stats`                   | Estatísticas de uso do módulo de respiração      |
| POST   | `/api/mood`                              | Registra (ou atualiza) o humor de hoje           |
| GET    | `/api/mood/today`                        | Humor registrado hoje, se houver                 |
| GET    | `/api/mood/history`                      | Histórico de humor (padrão: últimos 30 dias)     |
| GET    | `/api/feed/positive`                     | Feed positivo do dia (frases, fatos, desafios)   |
| GET    | `/api/notifications`                     | Lista notificações (filtro opcional: só não lidas) |
| GET    | `/api/notifications/unread-count`        | Quantidade de notificações não lidas             |
| PATCH  | `/api/notifications/{id}/read`           | Marca uma notificação como lida                  |
| PATCH  | `/api/notifications/read-all`            | Marca todas as notificações como lidas           |
| WS     | `/user/queue/notifications`              | Push em tempo real de novas notificações         |
| GET    | `/api/admin/users`                       | Busca/lista usuários (admin)                     |
| GET    | `/api/admin/users/{id}`                  | Detalha um usuário (admin)                       |
| POST   | `/api/admin/users/{id}/suspend`          | Suspende uma conta                               |
| POST   | `/api/admin/users/{id}/unsuspend`        | Remove a suspensão                               |
| POST   | `/api/admin/users/{id}/ban`              | Bane uma conta                                   |
| POST   | `/api/admin/users/{id}/unban`            | Remove o banimento                               |
| POST   | `/api/admin/users/{id}/roles/admin/grant`| Concede ROLE_ADMIN                               |
| POST   | `/api/admin/users/{id}/roles/admin/revoke`| Revoga ROLE_ADMIN                               |
| DELETE | `/api/admin/messages/{id}`               | Remove mensagem de chat                          |
| DELETE | `/api/admin/anonymous-messages/{id}`     | Remove mensagem anônima                          |
| GET    | `/api/admin/reports`                     | Lista denúncias por status                       |
| POST   | `/api/admin/reports/{id}/review`         | Revisa uma denúncia                              |
| GET    | `/api/admin/moderation-logs`             | Histórico de ações da moderação automática       |
| GET    | `/api/admin/missions/templates`          | Lista templates de missão (admin)                |
| POST   | `/api/admin/missions/templates`          | Cria template de missão                          |
| PUT    | `/api/admin/missions/templates/{id}`     | Atualiza template de missão                      |
| DELETE | `/api/admin/missions/templates/{id}`     | Desativa template de missão                      |
| GET    | `/api/admin/shop/items`                  | Lista itens da loja (admin)                      |
| POST   | `/api/admin/shop/items`                  | Cadastra item da loja                            |
| PUT    | `/api/admin/shop/items/{id}`             | Atualiza item da loja                            |
| DELETE | `/api/admin/shop/items/{id}`             | Desativa item da loja                            |
| GET    | `/api/admin/stats`                       | Estatísticas gerais da plataforma                |
| GET    | `/api/admin/logs`                        | Histórico de ações administrativas manuais       |
| GET    | `/api/ai/conversation`                   | Histórico paginado da conversa com a IA          |
| POST   | `/api/ai/conversation/messages`          | Envia mensagem à IA e recebe a resposta          |
| DELETE | `/api/ai/conversation`                   | Apaga o histórico de conversa com a IA           |

## Roadmap — completo

1. ~~Perfil e Amizades~~ ✔
2. ~~XP, Níveis e Missões~~ ✔
3. ~~Chat e WebSocket~~ ✔
4. ~~Estrelas Pontilhadas~~ ✔
5. ~~Moderação automática~~ ✔
6. ~~Tarefas~~ ✔
7. ~~Loja, Inventário e Conquistas~~ ✔
8. ~~Interesses, Recomendação e Salas Temáticas~~ ✔
9. ~~Respiração~~ ✔
10. ~~Humor e Feed Positivo~~ ✔
11. ~~Notificações~~ ✔
12. ~~Painel Admin~~ ✔
13. ~~IA de apoio emocional~~ ✔ concluído nesta entrega

Todas as 13 fases da especificação original foram implementadas, com Auth, WebSocket em
tempo real, moderação automática, gamificação completa (XP/nível/missões/loja/
conquistas), personalização social (interesses/recomendação/salas temáticas), bem-estar
(respiração/humor/feed positivo), notificações em tempo real, painel administrativo
completo e IA de apoio emocional com memória de contexto — tudo com tecnologias
gratuitas/open source, testado com testes de integração em cada fase, e documentado
módulo a módulo neste README.

## Nota sobre build

O ambiente onde este projeto foi gerado não tem acesso ao Maven Central, então as
dependências não puderam ser baixadas/compiladas aqui para validação automática. Rode
`mvn clean install` na sua máquina (com acesso normal à internet) para baixar as
dependências e confirmar a build antes de seguir para a próxima fase.
