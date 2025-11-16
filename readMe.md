# Painel de Investimentos - Desafio Técnico

Este projeto é uma API RESTful completa desenvolvida em Java 21 e Spring Boot 3, que simula uma plataforma de investimentos. O sistema analisa o perfil de risco do cliente com base em seu comportamento e recomenda produtos de investimento adequados, além de fornecer endpoints para simulação, histórico e telemetria.

A aplicação foi projetada com foco em **alta performance**, **código limpo** e **segurança**, seguindo os princípios de DDD (Domain-Driven Design), SOLID e Arquitetura Limpa.

## 🎯 Objetivo

O objetivo principal, conforme o desafio proposto, é:

> Criar uma aplicação web que analisa o comportamento financeiro do cliente e ajusta automaticamente seu perfil de risco, sugerindo produtos de investimento como CDBs, LCIS, LCAs, Tesouro Direto, Fundos, etc. 

## ✨ Funcionalidades Principais

* **Autenticação e Autorização:** Sistema de segurança completo usando **JWT (Bearer Token)**.
* **Motor de Simulação:** Endpoint `POST /simular` otimizado para alta velocidade, usando persistência assíncrona.
* **Motor de Perfil de Risco:** Endpoint `GET /perfil-risco` que calcula o perfil (Conservador, Moderado, Agressivo) de um cliente com base no histórico de simulações.
* **Motor de Recomendação:** Endpoint `GET /produtos-recomendados` que sugere produtos com base no perfil de risco.
* **Endpoints de Histórico:** Consultas otimizadas (JPQL) para histórico de simulações e histórico de investimentos por cliente.
* **Telemetria:** Endpoint `GET /telemetria` que usa o Spring Boot Actuator para relatar métricas da API.
* **Documentação:** API totalmente documentada com **Swagger (OpenAPI)**.
* **Alta Otimização:** O sistema utiliza caching em múltiplos níveis (In-Memory e Spring Cache), projeções JPQL e índices de banco de dados para garantir respostas de alta velocidade.

## 💻 Stack de Tecnologias

* **Java 21**
* **Spring Boot 3**
* **Spring Security 6** (Autenticação JWT)
* **Spring Data JPA** (Hibernate)
* **SQL Server** 
* **Springdoc OpenAPI (Swagger)** (Documentação)
* **JWT** (Biblioteca para JSON Web Token)
* **Lombok**
* **JUnit 5, Mockito & AssertJ** (Testes)
* **Maven**

## 🚀 Como Executar (Localmente)

**Pré-requisitos:**

* **Java 21 (JDK)**
* **Apache Maven** 3.9+

**Passos:**

1.  Clone o repositório:
    ```bash
    git clone https://github.com/ziggy-data/investimento.git
    cd investimento
    ```
2.  Compile e construa o projeto (isso irá baixar todas as dependências):
    ```bash
    mvn clean install
    ```
3.  Execute a aplicação:
    ```bash
    mvn spring-boot:run
    ```
4.  A API estará disponível em `http://localhost:8080`.
    * **Swagger UI (Documentação):** `http://localhost:8080/swagger-ui/index.html`


## 🐳 Como Executar (Docker)

Certifique-se de ter o **Docker** e o **Docker Compose** instalados em sua máquina.

```bash

1. Build da Imagem e subir a aplicação
    docker-compose up --build

2. Parar a aplicação
    docker-compose down
```

Quando aparecer no terminal `investimento-api  | Sistema de investimento iniciou!`, a API estará disponível em `http://localhost:8080`.

## 📊 Cobertura de Testes

Os testes foram feitos tanto em nível unitário (JUnit 5 + Mockito) quanto em nível de integração (`@SpringBootTest` + `@DataJpaTest`).

O projeto atingiu uma cobertura de **95% das classes** e **98% dos métodos**, garantindo que todas as regras de negócio, validações, otimizações e casos de falha (400, 401, 404, 500) estão cobertos.

É recomendado o uso do `application-test.properties` para rodar os testes com um banco de dados em memória H2.

## 📐 Arquitetura do Sistema

Para atender aos princípios de **SOLID** e **DDD (Domain-Driven Design)**, o projeto não utiliza a arquitetura monolítica "Package-by-Layer" (ex: `controller`, `service`, `repository`).

Em vez disso, adotamos uma arquitetura **"Package-by-Context"** (ou "Package-by-Feature"). O código é organizado em "Bounded Contexts" de negócio, onde cada contexto é autônomo e focado em uma única responsabilidade de negócio:

* **`auth`**: Cuida apenas da autenticação (Login, geração de JWT).
* **`config`**: Configurações globais (Segurança, Cache, Async, Swagger).
* **`simulacao`**: O contexto principal. Cuida da lógica de `POST /simular`, dos históricos (`GET /simulacoes`), e da persistência.
* **`recomendacao`**: O "Motor de Recomendação". Cuida da lógica de `GET /perfil-risco` e `GET /produtos-recomendados`.
* **`telemetria`**: Cuida da coleta e exposição de métricas (`GET /telemetria`).
* **`user`**: Define o domínio do usuário para a segurança.
* **`exception`**: O `GlobalExceptionHandler` que padroniza todas as respostas de erro.

Esta arquitetura torna o sistema mais fácil de manter, testar e escalar.

## 🛡️ Autenticação (Como Usar a API)

Todos os endpoints, exceto `/api/v1/auth/login` e `/swagger-ui/`, são protegidos por **JWT**.

### 1\. Obter o Token (Login)

Primeiro, você deve se autenticar. A aplicação é iniciada (via `UserSeeder`) com um usuário padrão:

* **Username:** `admin`
* **Password:** `password123` (Este valor pode ser alterado no `application.properties`)

**Requisição: `POST /api/v1/auth/login`**

```json
{
    "username": "admin",
    "password": "password123"
}
```

**Resposta (200 OK):**

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MzQ0... (token longo)"
}
```

### 2\. Acessar Endpoints Protegidos

Para chamar qualquer outro endpoint (ex: `GET /api/v1/investimentos/simulacoes`), você deve copiar o token recebido e enviá-lo como um **Header HTTP** `Authorization`.

**Exemplo (no Postman ou Insomnia):**

* **Header:** `Authorization`
* **Value:** `Bearer <seu-token-aqui>` (Não se esqueça do "Bearer " no início)

Se o token estiver ausente ou inválido, a API retornará um erro **401 Unauthorized**.

## 📋 API Endpoints (Relação com o Desafio)

A API implementa todos os 7 endpoints do desafio, além do endpoint de autenticação.

| Req. PDF | Verbo | Endpoint (API) | Protegido? | Descrição |
|:---------| :--- | :--- | :--- | :--- |
| N/A      | `POST` | `/api/v1/auth/login` | **Não** | Autentica e obtém um token JWT. |
| `1`      | `POST` | `/api/v1/investimentos/simular` | **Sim** | Calcula e salva uma simulação de investimento. |
| `2`      | `GET` | `/api/v1/investimentos/simulacoes` | **Sim** | Retorna o histórico de todas as simulações. |
| `2`      | `GET` | `/api/v1/investimentos/simulacoes/por-produto-dia` | **Sim** | Retorna dados agregados (contagem, média) por dia e produto. |
| `4`      | `GET` | `/api/v1/investimentos/telemetria` | **Sim** | Retorna métricas de telemetria da API. |
| `5`      | `GET` | `/api/v1/investimentos/perfil-risco/{clienteld}` | **Sim** | Calcula o perfil de risco (Conservador, etc.) de um cliente. |
| `6`      | `GET` | `/api/v1/investimentos/produtos-recomendados/{perfil}` | **Sim** | Retorna produtos adequados para um perfil de risco. |
| `7`      | `GET` | `/api/v1/investimentos/{clienteld}` | **Sim** | Retorna o histórico de investimentos (baseado em simulações) de um cliente. |

## 🚀 Guia Rápido (Happy Path em 5 Minutos)

Siga este guia para ver a API em ação e validar os requisitos do PDF.

**1. Execute a Aplicação**
(Veja a seção "Como Executar" abaixo).

**2. Acesse a Documentação (Swagger)**
Abra seu navegador em: `http://localhost:8080/swagger-ui/index.html`

**3. Obtenha seu Token de Acesso**

* Vá até a seção `Autenticação` e abra o `POST /api/v1/auth/login`.
* Clique em "Try it out".
* Use o usuário padrão (criado pelo `UserSeeder`) no corpo da requisição:
  ```json
  {
    "username": "admin",
    "password": "password123"
  }
  ```
* Clique em "Execute" e copie o `token` da resposta.

**4. Autorize-se no Swagger**

* No topo da página, clique no botão verde **"Authorize"**.
* Na caixa de texto, cole o seu token, precedido de ` Bearer  ` (com espaço).
    * **Exemplo:** `Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG...`
* Clique em "Authorize" e feche o pop-up. Agora você está autenticado.

**5. Valide o PDF: `POST /simular`**

* Vá até a seção `Investimentos` e abra o `POST /api/v1/investimentos/simular`.
* Clique em "Try it out".
* Use o **JSON exato** fornecido no desafio :
  ```json
  {
    "clienteId": 123,
    "valor": 10000.00,
    "prazoMeses": 12,
    "tipoProduto": "CDB"
  }
  ```
* Clique em "Execute". Você verá a **resposta 200 OK** exatamente como especificada no PDF, provando que o motor de validação e cálculo funciona.

**6. Valide o PDF: `GET /perfil-risco`**

* Vá até o `GET /api/v1/investimentos/perfil-risco/{clienteld}`.
* Clique em "Try it out".
* Digite `123` (o `clienteId` que acabamos de usar) no campo `clienteld`.
* Clique em "Execute". Você verá o perfil de risco ("Conservador") calculado para este cliente com base na simulação que acabamos de fazer.

-----

## ⚡ Destaques de Performance e Otimização

Para garantir que a API seja "crucialmente rápida", implementamos várias otimizações avançadas:

* **Escrita Assíncrona (Async):** O `POST /simular` é instantâneo. Ele valida o usuário, calcula em memória e, em seguida, envia o salvamento no banco de dados (`simulacaoRepository.save()`) para uma *thread separada* (`@Async`). O usuário recebe a resposta "200 OK" imediatamente, sem esperar pelo I/O do banco.
* **Cache de Leitura (In-Memory):** O `data.sql` (com os produtos) não muda. Em vez de ir ao banco a cada simulação, o `ProdutoValidationServiceImpl` carrega **todos** os produtos para uma `List` na memória no momento da inicialização (`@PostConstruct`). As validações (filtros e `max()`) são feitas em nanossegundos usando Java Streams, eliminando o "delay" da primeira chamada.
* **Cache de Métodos (`@Cacheable`):** Endpoints de leitura pesada, como `GET /produtos-recomendados` e `GET /perfil-risco`, são cacheados. A primeira chamada pode levar \~200ms (para a query de agregação rodar), mas todas as chamadas subsequentes para o mesmo perfil ou cliente são retornadas da memória em \< 50ms.
* **Projeções JPQL:** Nossos repositórios não retornam Entidades (`Simulacao`, `Produto`) em endpoints de leitura. Eles usam `new ...DTO()` direto no `@Query` (Projeção de Construtor) para que o banco de dados retorne apenas os dados necessários, reduzindo o tráfego de I/O.
* **Índices de Banco:** A coluna `clienteId` na tabela `simulacoes` é indexada (`@Index`), tornando as queries de agregação para o perfil de risco (`GROUP BY clienteId`) drasticamente mais rápidas.

-----

## ✅ Critérios de Aceitação (Do PDF)

O projeto buscou atender aos 4 critérios de avaliação definidos:

1.  **Estrutura da API e documentação:** 
    * *Status:* **Concluído**. A API segue os padrões RESTful, está versionada, e totalmente documentada via Swagger/OpenAPI.
2.  **Qualidade do motor de recomendação:** 
    * *Status:* **Concluído**. O motor (`MotorRecomendacaoService`) calcula o perfil com base no volume, frequência e risco, e o `Enum PerfilRisco` mapeia as regras de negócio.
3.  **Segurança e tratamento de erros:** 
    * *Status:* **Concluído**. A API implementa JWT (Autenticação) e um `GlobalExceptionHandler` que padroniza todas as respostas de erro (400, 401, 404, 500).
4.  **Testes unitários e integração:** 
    * *Status:* **Concluído**. O projeto possui uma suíte de testes (JUnit 5 + Mockito) e testes de integração (`@SpringBootTest` + `@DataJpaTest`) com 95% de cobertura de código (JaCoCo).
