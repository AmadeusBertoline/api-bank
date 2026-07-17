# API Bank — Sistema de Gerenciamento Financeiro

API REST para gerenciamento de contas bancárias e transações financeiras,
desenvolvida com Java 21 e Spring Boot 3. Projeto desenvolvido para fins de
portfólio, simulando funcionalidades reais de um sistema bancário.

## 🚀 Deploy

A API está em produção e pode ser acessada sem configuração local:

**Base URL:** `https://api-bank-production-7de4.up.railway.app`

**Swagger UI:** `https://api-bank-production-7de4.up.railway.app/swagger-ui/index.html`

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA + Hibernate
- MySQL 8
- Maven
- Lombok
- Bean Validation
- Springdoc OpenAPI (Swagger)
- JUnit 5 + Mockito
- Railway (deploy)

## Funcionalidades

- Autenticação e autorização com JWT (perfis ROLE_ADMIN e ROLE_CLIENTE)
- Cadastro e gerenciamento de contas bancárias (CORRENTE e POUPANÇA)
- Operações financeiras: depósito, saque e transferência com atomicidade transacional
- Extrato de transações por conta
- Validação de dados de entrada com mensagens descritivas
- Tratamento de erros centralizado com respostas padronizadas
- Documentação interativa via Swagger UI
- Testes unitários cobrindo regras de negócio críticas

## Arquitetura

O projeto segue arquitetura em camadas com separação clara de responsabilidades:

Controller → Service → Repository → Banco de Dados

- **Controller:** recebe requisições HTTP, delega ao Service, devolve resposta
- **Service:** contém regras de negócio e orquestra as operações
- **Repository:** responsável pela persistência via Spring Data JPA
- **DTOs:** desacoplam a API da entidade, controlando o que entra e o que sai
- **Security:** filtro JWT intercepta cada requisição e valida o token

## Como executar localmente

### Pré-requisitos
- Java 21+
- MySQL 8+
- Maven

### Configuração

```bash
git clone https://github.com/AmadeusBertoline/api-bank.git
cd api-bank
```

Crie o banco de dados:
```sql
CREATE DATABASE financas_db;
```

Configure as variáveis de ambiente ou edite `application.properties`:
```properties
DATABASE_URL=jdbc:mysql://localhost:3306/financas_db
DATABASE_USERNAME=root
DATABASE_PASSWORD=sua_senha
JWT_SECRET=sua-chave-secreta-minimo-32-caracteres
```

Execute:
```bash
./mvnw spring-boot:run
```

## Autenticação

Todos os endpoints (exceto `/auth/**` e `/swagger-ui/**`) exigem token JWT.

1. Registre um usuário: `POST /auth/registrar`
2. Faça login: `POST /auth/login` — copie o token retornado
3. Use o token no header: `Authorization: Bearer SEU_TOKEN`

No Swagger UI, clique em **Authorize** e cole o token.

### Perfis de acesso

| Role | Permissões |
|------|------------|
| `ROLE_ADMIN` | Acesso total, incluindo deletar contas |
| `ROLE_CLIENTE` | Criar, consultar e realizar transações |

## Endpoints

### Autenticação (público)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/registrar` | Registrar novo usuário |
| POST | `/auth/login` | Login — retorna JWT |

### Contas (autenticado)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/contas` | Criar nova conta |
| GET | `/contas` | Listar todas as contas |
| GET | `/contas/{id}` | Buscar conta por ID |
| PUT | `/contas/{id}` | Atualizar conta |
| DELETE | `/contas/{id}` | Deletar conta (ADMIN) |

### Transações (autenticado)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/transacoes` | Realizar depósito, saque ou transferência |
| GET | `/transacoes/conta/{contaId}` | Extrato da conta |

## Regras de Negócio

- Saldo não pode ficar negativo em saques
- Transferências são atômicas — ou debitam e creditam completamente, ou nenhuma operação acontece (`@Transactional`)
- Contas inativas não realizam nem recebem transações
- Tipos aceitos: `DEPOSITO`, `SAQUE`, `TRANSFERENCIA`
- `DELETE /contas` restrito a `ROLE_ADMIN`

## Testes

```bash
./mvnw test
```

Cobertura de testes unitários no `ContaService` e `TransacaoService`, incluindo
cenários de sucesso, erro de validação e violação das regras de negócio.