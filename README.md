# 📌 TODO List & User Manager (CLI)

Uma aplicação interativa de linha de comando (CLI), desenvolvida em **Java puro**, para o gerenciamento de **usuários, tarefas e categorias**.

O sistema foi projetado seguindo princípios de **Clean Architecture**, com separação de responsabilidades em camadas como **Controller, Service e Repository**. A persistência dos dados é realizada localmente utilizando a API moderna **Java NIO**.

## 🚀 Tecnologias e Conceitos Utilizados

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

### 2. Executando via Terminal com Gradle Wrapper

O projeto já possui o Gradle Wrapper, portanto não é necessário instalar o Gradle manualmente.

Estando dentro da pasta do projeto, execute:

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

## 🖥️ Executando a Aplicação

Após executar o comando run, a aplicação será iniciada diretamente no terminal e apresentará seu menu interativo para gerenciamento dos dados.

A aplicação permite trabalhar com:

- 👤 Usuários
- 📋 Tarefas
- 🏷️ Categorias

Os dados são persistidos localmente em arquivos, permitindo que as informações sejam mantidas entre diferentes execuções da aplicação.
