# API Bank — Sistema de Gerenciamento Financeiro

API REST para gerenciamento de contas bancárias e transações financeiras, 
desenvolvida com Java 21 e Spring Boot 3.

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Data JPA + Hibernate
- MySQL 8
- Maven
- Lombok
- Bean Validation
- Springdoc OpenAPI (Swagger)

## Funcionalidades

- Cadastro e gerenciamento de contas bancárias (CORRENTE e POUPANÇA)
- Operações financeiras: depósito, saque e transferência
- Extrato de transações por conta
- Validação de dados de entrada
- Tratamento de erros centralizado
- Documentação interativa via Swagger UI

## Arquitetura

O projeto segue arquitetura em camadas com separação clara de responsabilidades:

Controller  →  Service  →  Repository  →  Banco de Dados

- **Controller**: recebe requisições HTTP e delega para o Service
- **Service**: contém regras de negócio e orquestra as operações
- **Repository**: responsável pela persistência de dados
- **DTOs**: objetos de transferência que desacoplam a API da entidade

## Como executar

### Pré-requisitos
- Java 21+
- MySQL 8+
- Maven

### Configuração do banco
```sql
CREATE DATABASE financas_db;
```

### Executando a aplicação
```bash
git clone https://github.com/AmadeusBertoline/api-bank.git
cd api-bank
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## Documentação da API

Acesse a documentação interativa (Swagger UI):
http://localhost:8080/swagger-ui/index.html

## Endpoints

### Contas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/contas` | Criar nova conta |
| GET | `/contas` | Listar todas as contas |
| GET | `/contas/{id}` | Buscar conta por ID |
| PUT | `/contas/{id}` | Atualizar conta |
| DELETE | `/contas/{id}` | Deletar conta |

### Transações
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/transacoes` | Realizar depósito, saque ou transferência |
| GET | `/transacoes/conta/{contaId}` | Extrato da conta |

## Regras de Negócio

- Saldo não pode ficar negativo em saques
- Transferências são atômicas — ou debitam e creditam completamente, ou nenhuma operação acontece
- Contas inativas não realizam nem recebem transações
- Tipos de transação aceitos: `DEPOSITO`, `SAQUE`, `TRANSFERENCIA`