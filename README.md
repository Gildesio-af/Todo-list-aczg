# 📌 TODO List & TaskFlow

Este projeto é composto por duas aplicações de gerenciamento de tarefas:
1. **Back-end (CLI)**: Uma aplicação interativa de linha de comando desenvolvida em **Java puro**. O sistema foi projetado seguindo princípios de **Clean Architecture**, utilizando a API moderna **Java NIO** para persistência em arquivos locais.
2. **Front-end (TaskFlow)**: Uma aplicação Web interativa desenvolvida com HTML, CSS e JavaScript para gerenciamento visual de tarefas.

## 📂 Estrutura do Projeto

O repositório foi organizado nas seguintes pastas:
- `/back-end`: Contém o código fonte da aplicação CLI em Java.
- `/front-end`: Contém a interface web do gerenciador de tarefas (TaskFlow).

## 🚀 Tecnologias e Conceitos Utilizados

### 🖥️ Back-end (CLI em Java)
- **Java 17+**
    - Records
    - Stream API
    - Métodos modernos de coleções, como `.toList()`

- **Gradle**
    - Gerenciamento de dependências
    - Build e execução da aplicação

- **Lombok**
    - Redução de código boilerplate
    - Getters, Setters e Construtores

- **Java NIO**
    - Leitura e escrita de arquivos
    - Manipulação moderna de arquivos
    - Uso de `try-with-resources`

- **Clean Architecture**
    - Separação de responsabilidades
    - Camadas de Controller, Service e Repository
### 🌐 Front-end (Web App)
- **HTML5 & CSS3**: Estruturação semântica e estilização moderna, com foco em uma interface limpa utilizando web fonts.
- **Vanilla JavaScript (ES6 Modules)**: Componentização e separação da lógica de negócio em módulos (`main.js`, `storage.js`, `ui.js`, `constants.js`).
- **Local Storage API**: Persistência de dados diretamente no navegador. As tarefas são criadas, editadas, filtradas e removidas utilizando apenas os recursos locais do browser, sem dependência de um servidor externo.

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado em sua máquina:

- **Git**
- **Java JDK 21** ou superior
- Uma IDE de sua preferência, como:
    - IntelliJ IDEA
    - Eclipse
    - VS Code

> **Importante:** caso utilize Lombok na IDE, certifique-se de que o plugin do Lombok esteja instalado e que a opção **Enable Annotation Processing** esteja ativada.

## 🔧 Como Clonar e Rodar o Projeto

### 1. Clonar o repositório

Abra o terminal e execute:

```bash
git clone https://github.com/Gildesio-af/Todo-list-aczg.git
cd Todo-list-aczg
```

### 2. Rodando o Front-end (Web App)

O funcionamento do front-end baseia-se no armazenamento local do navegador.
1. Navegue até a pasta do front-end: `cd front-end`
2. Abra o arquivo `index.html` em seu navegador web.
3. *(Opcional)* Utilize uma extensão como o **Live Server** no VS Code para executar a aplicação com recarregamento automático.

### 3. Executando o Back-end (Terminal com Gradle Wrapper)

O projeto Java já possui o Gradle Wrapper. Estando na raiz do repositório, acesse a pasta do back-end:

```bash
cd back-end
```

Em seguida, execute os comandos:

Linux / macOS

```bash
./gradlew build
./gradlew run --console=plain -q
```

Windows
```bash
gradlew.bat build
gradlew.bat run --console=plain -q
```

## 🖥️ Utilizando a Aplicação CLI (Back-end)

Após executar o comando run, a aplicação CLI será iniciada diretamente no terminal e apresentará seu menu interativo para gerenciamento dos dados.

A aplicação permite trabalhar com:

- 👤 Usuários
- 📋 Tarefas
- 🏷️ Categorias

Os dados são persistidos localmente em arquivos, permitindo que as informações sejam mantidas entre diferentes execuções da aplicação.
