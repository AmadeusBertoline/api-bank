# API Bank — Sistema Bancário com Pix

![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-black?logo=jsonwebtokens)
![Tests](https://img.shields.io/badge/tests-58%20passing-success)

API REST que simula um banco digital com transferências via **Pix**, desenvolvida em Java 21 e Spring Boot 3. O projeto reproduz, em escala reduzida, problemas reais de sistemas financeiros: concorrência em transações simultâneas, controle de limites, cache de leitura e autenticação stateless — não apenas o CRUD básico de conta/transação.

## 🚀 Deploy

A API está em produção e pode ser acessada sem configuração local:

**Base URL:** `https://api-bank-production-7de4.up.railway.app`

**Swagger UI:** `https://api-bank-production-7de4.up.railway.app/swagger-ui/index.html`

## Sobre o projeto

Cada usuário, ao se registrar, recebe automaticamente uma conta de pagamento e pode cadastrar chaves Pix (e-mail, CPF, telefone ou aleatória) para receber transferências. As transferências debitam e creditam duas contas na mesma transação, respeitando saldo, limite diário e status da conta — com proteção contra condições de corrida quando duas transferências acontecem ao mesmo tempo envolvendo a mesma conta.

## Funcionalidades

- Cadastro de usuário com endereço vinculado e criação automática da conta bancária
- Autenticação via JWT, com perfis `ROLE_USUARIO` e `ROLE_ADMIN`
- Cadastro de chaves Pix com **detecção automática do tipo** (e-mail, CPF, CNPJ, telefone ou chave aleatória) por regex
- Transferências Pix atômicas, com trava pessimista e ordenação de locks para evitar deadlock em transações concorrentes
- Limite diário de Pix configurável por conta, validado somando o total já transferido no dia
- Extrato de transações paginado, com cache em Redis (TTL de 10 min) e invalidação automática a cada novo Pix
- Encerramento de conta pelo próprio usuário — definitivo, só permitido com saldo zerado
- Bloqueio/desbloqueio de conta por um admin — para investigação, sem apagar o histórico nem impedir a consulta
- Painel administrativo: listagem paginada de todas as contas e gestão de status
- Validação de entrada com anotações customizadas (CPF com dígito verificador, idade mínima, força de senha, formato de endereço, etc.)
- Tratamento de erros centralizado com respostas padronizadas por tipo de exceção
- Documentação interativa via Swagger UI
- 45 testes unitários (JUnit 5 + Mockito) cobrindo services e regras de negócio

## Tecnologias

- Java 21
- Spring Boot 3.5
- Spring Web, Spring Data JPA + Hibernate
- Spring Security + JWT (`jjwt` 0.12.6)
- Spring Data Redis (cache)
- Bean Validation (Jakarta) com validadores customizados
- MySQL 8
- Lombok
- springdoc-openapi / Swagger UI
- JUnit 5 + Mockito
- Docker e Docker Compose
- Maven

## Arquitetura

Arquitetura em camadas, com responsabilidades bem separadas:

```
Controller → Service → Repository → Banco de Dados
```

- **Controller** — recebe a requisição HTTP e delega ao Service; não tem lógica de negócio
- **Service** — concentra as regras de negócio, orquestra repositórios e aplica cache
- **Repository** — persistência via Spring Data JPA, incluindo queries customizadas e locks pessimistas
- **DTOs (records)** — desacoplam a API do modelo de persistência, controlando exatamente o que entra e o que sai
- **Validation** — anotações customizadas (`@CpfValido`, `@SenhaValida`, `@ChavePixValida`, etc.) reaproveitáveis entre DTOs
- **Security** — filtro JWT (`OncePerRequestFilter`) intercepta cada requisição e popula o contexto de segurança
- **Exception Handling** — `@RestControllerAdvice` centraliza a tradução de exceções em respostas HTTP consistentes

### Pontos técnicos que valem destaque

- **Concorrência em transferências**: ao processar um Pix, as duas contas envolvidas são bloqueadas com `PESSIMISTIC_WRITE`, sempre na mesma ordem (menor ID primeiro), prevenindo deadlock quando duas transferências entre as mesmas contas ocorrem em paralelo.
- **Cache com Redis**: o extrato e os dados do usuário são cacheados por 10 minutos; qualquer novo Pix invalida o cache de transações do usuário automaticamente (`@CacheEvict`).
- **Geração de número de conta**: a conta é salva primeiro para obter um ID e só então o número/dígito verificador é calculado a partir dele.

## Como executar localmente

Nenhum segredo fica no código-fonte — tudo é lido de variáveis de ambiente. Veja a seção [Variáveis de ambiente](#variáveis-de-ambiente) para a lista completa.

### Opção 1 — Docker Compose (recomendado)

Sobe API, MySQL e Redis já conectados entre si, sem precisar instalar nada além do Docker.

```bash
git clone https://github.com/AmadeusBertoline/api-bank.git
cd api-bank
cp .env.example .env   # preencha com seus próprios valores
docker-compose up --build
```

A API sobe em `http://localhost:8080`.

### Opção 2 — Ambiente local

**Pré-requisitos:** Java 21+, Maven (ou use o `./mvnw` incluso), MySQL 8+ e Redis rodando localmente.

```bash
git clone https://github.com/AmadeusBertoline/api-bank.git
cd api-bank
export JWT_SECRET=$(openssl rand -hex 32)
./mvnw spring-boot:run
```

O perfil `local` é ativado por padrão e já aponta para `localhost` (banco e schema são criados automaticamente na primeira execução). Não é necessário criar o banco manualmente — só a variável `JWT_SECRET` precisa existir no ambiente antes de rodar.

### Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

## Variáveis de ambiente

Nenhuma delas tem valor padrão versionado no código. Use o `.env.example` como referência para criar seu `.env` local.

| Variável | Usada em | Descrição |
|---|---|---|
| `JWT_SECRET` | Local, Docker, Produção | Chave usada para assinar e validar os tokens JWT |
| `MYSQL_ROOT_PASSWORD` | Docker Compose | Senha do usuário root do MySQL do container |
| `DATABASE_URL` | Produção (Railway) | URL JDBC completa do banco |
| `DATABASE_USERNAME` | Produção (Railway) | Usuário do banco |
| `DATABASE_PASSWORD` | Produção (Railway) | Senha do banco |

## Autenticação

Todos os endpoints exigem token JWT, exceto `/auth/login`, `/auth/registrar` e o Swagger.

1. Registre um usuário: `POST /auth/registrar`
2. Faça login: `POST /auth/login` e copie o token retornado
3. Envie o token no header: `Authorization: Bearer SEU_TOKEN`

No Swagger UI, clique em **Authorize** e cole o token.

| Perfil | Acesso |
|---|---|
| `ROLE_USUARIO` | Gerencia a própria conta, chaves Pix e transações |
| `ROLE_ADMIN` | Acesso a `/admin/**`: bloqueia/desbloqueia qualquer conta e lista todas as contas do sistema |

### Criando o primeiro admin

Só um admin pode cadastrar outro admin (`POST /admin/registrar` exige `ROLE_ADMIN`), então não existe rota pública para criar o primeiro. Ele precisa ser inserido direto no banco.

1. Gere um hash BCrypt para a senha escolhida (qualquer gerador BCrypt padrão funciona — o `BCryptPasswordEncoder` do Spring Security lê hashes `$2a$`/`$2b$`/`$2y$` de forma compatível).
2. Insira o usuário manualmente:

```sql
INSERT INTO usuarios (nome, email, senha, cpf, data_nascimento, role, data_criacao)
VALUES (
  'Admin Teste',
  'admin@apibank.com',
  '$2b$10$xa2KNEch4vrcmQvydNJLvujHvmqcb1UPjOY1mUxNvKrgte/bdkgvy', -- senha: AdminSenha123
  '52998224725',
  '1995-01-01',
  'ROLE_ADMIN',
  NOW()
);
```

CPF e data de nascimento precisam ser valores realmente válidos (CPF com dígito verificador correto, idade ≥ 16), não só qualquer texto — mesmo inserido direto no banco, esse registro passa a valer para regras que dependem desses campos, como o cadastro de chave Pix tipo CPF, que confere se a chave bate com o CPF do dono. Não é preciso criar `conta` nem `endereco` para esse usuário: nenhuma rota de `/admin/**` depende disso (se ele tentar acessar `/contas/me`, recebe 404 — esperado, admin não tem conta pessoal).

3. Faça login normalmente:

```json
POST /auth/login
{ "email": "admin@apibank.com", "senha": "AdminSenha123" }
```

O token retornado já vem com `role: ROLE_ADMIN`. A partir daí, use `POST /admin/registrar` para criar os próximos admins pela própria API — só esse primeiro precisa entrar via banco.

> ⚠️ O hash acima é só para ambiente local/Docker Compose de teste. Para criar um admin em produção (Railway), gere um hash com uma senha privada seguindo o mesmo passo a passo — não reaproveite este exemplo, ele fica público neste README.

### Exemplo — registro e login

```json
POST /auth/registrar
{
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "senha": "SenhaForte123",
  "confirmarSenha": "SenhaForte123",
  "cpf": "52998224725",
  "dataNascimento": "2000-05-10",
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "123",
    "complemento": "Apto 45",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "uf": "SP",
    "cep": "01001-000"
  }
}
```

```json
POST /auth/login
{ "email": "maria@email.com", "senha": "SenhaForte123" }

// resposta
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "id": 1,
  "nome": "Maria Silva",
  "role": "ROLE_USUARIO"
}
```

## Endpoints

### Autenticação — `/auth` (público)
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/registrar` | Registra um novo usuário e cria conta + endereço automaticamente |
| POST | `/auth/login` | Autentica e retorna o JWT |

### Administração — `/admin` (ROLE_ADMIN)
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/admin/registrar` | Um admin registra outro admin |
| PATCH | `/admin/bloquear-conta/{id}` | Bloqueia a conta de qualquer usuário (nome da rota mantido por compatibilidade; a ação é bloquear) |
| PATCH | `/admin/desbloquear-conta/{id}` | Desbloqueia a conta de qualquer usuário |
| GET | `/admin/listar-contas` | Lista todas as contas do sistema (paginado) |

### Usuários — `/usuarios` (autenticado)
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/usuarios/me` | Dados do usuário logado, incluindo endereço |
| PATCH | `/usuarios/email/atualizar` | Atualiza o e-mail (login) |
| PATCH | `/usuarios/endereco/atualizar` | Atualiza o endereço |

### Contas — `/contas` (autenticado)
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/contas/me` | Dados da conta do usuário logado |
| PATCH | `/contas/encerrar` | Encerra a própria conta — permanente, exige saldo zerado |
| PATCH | `/contas/limite` | Ajusta o limite diário de Pix |

### Chaves Pix — `/chaves` (autenticado)
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/chaves` | Cadastra chave Pix (tipo detectado automaticamente) |
| GET | `/chaves` | Lista as chaves Pix do usuário logado |
| DELETE | `/chaves/{id}` | Remove uma chave Pix (somente o dono) |

### Transações — `/transacoes` (autenticado)
| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/transacoes` | Realiza uma transferência Pix |
| GET | `/transacoes/extrato` | Extrato paginado da conta (cache de 10 min) |

## Status da conta

Toda conta tem um dos três status abaixo (`StatusConta`):

- **`ATIVA`** — status padrão, criado junto com a conta. Acesso completo.
- **`BLOQUEADA`** — definida por um admin (`PATCH /admin/desativar-conta/{id}`), tipicamente para investigação. A conta continua podendo ser **consultada** normalmente, mas nenhuma **alteração** é permitida: Pix (enviar ou receber), cadastro/exclusão de chave Pix, troca de e-mail ou endereço, ajuste de limite diário e até o encerramento pelo próprio usuário ficam bloqueados. Só um admin reverte, desbloqueando a conta (`PATCH /admin/ativar-conta/{id}`).
- **`ENCERRADA`** — definida pelo próprio usuário (`PATCH /contas/desativar`), só permitida com o saldo zerado (o saldo não é zerado automaticamente — é preciso transferir tudo antes de encerrar). É permanente: não existe rota para reabrir uma conta encerrada, e ela não pode mais enviar nem receber Pix.

## Regras de negócio

- Conta de origem e destino não podem ser a mesma
- Contas bloqueadas ou encerradas não enviam nem recebem Pix
- Transferência bloqueada se ultrapassar o limite diário (soma do que já foi enviado no dia)
- Transferência bloqueada se o saldo for insuficiente
- Débito e crédito ocorrem na mesma transação (`@Transactional`) — ou os dois acontecem, ou nenhum
- Cada usuário só pode ter uma conta
- Chave Pix não pode ser duplicada; uma chave do tipo CPF só pode ser cadastrada pelo próprio dono do documento
- Apenas o dono de uma chave Pix pode excluí-la

## Testes

```bash
./mvnw test
```

58 testes unitários com JUnit 5 + Mockito, cobrindo `AuthService`, `ContaService`, `ChavePixService`, `TransacaoService`, `EnderecoService` e `UsuarioService` — incluindo cenários de sucesso, violação de regra de negócio e erro de validação.

## Autor

**Amadeus Bertoline**
[GitHub](https://github.com/AmadeusBertoline) · amadeusbertoline123@gmail.com