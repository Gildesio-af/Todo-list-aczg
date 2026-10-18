# Orientações do projeto

## 1. Visão geral

O repositório contém uma aplicação de gerenciamento de tarefas. O back-end é
uma aplicação CLI em Java, com menus interativos, arquitetura em camadas e
persistência local em arquivos CSV. O diretório `front-end` contém uma
interface web independente; estas orientações descrevem principalmente o
back-end em `back-end`.

As funcionalidades já implementadas no código do back-end são:

- Inicialização da aplicação CLI pelo menu principal, com acesso aos módulos
  de usuários e tarefas e opção de encerramento.
- Cadastro, consulta, atualização e exclusão do perfil de um usuário.
- Validação de nome, e-mail e idade no cadastro e na atualização do usuário.
- Cadastro, listagem e exclusão de categorias, com prevenção de categorias
  duplicadas sem diferenciar maiúsculas de minúsculas.
- Cadastro de tarefas com nome, descrição, prioridade de 1 a 5, status
  (`TODO`, `DOING` ou `DONE`), datas inicial e final e categoria.
- Atualização parcial de tarefas, preservando os valores para os quais o
  usuário não informa alteração.
- Exclusão de tarefas por nome.
- Listagem de todas as tarefas e ordenação por prioridade.
- Filtragem de tarefas por categoria ou por status.
- Dashboard com a contagem de tarefas nos estados `TODO`, `DOING` e `DONE`.
- Validação de campos obrigatórios, prioridades, status e intervalo entre
  datas de tarefas.
- Leitura e gravação local de usuários, tarefas e categorias em arquivos CSV,
  preservando os dados entre execuções.
- Mensagens de sucesso, entrada inválida, item não encontrado e erro nos
  menus da aplicação.

## 2. Stack e estrutura

- **Java:** Java 17 ou superior; o README recomenda JDK 21 ou superior. O
  código utiliza recursos modernos como records, Stream API e métodos modernos
  de coleções.
- **Gradle:** Gradle Wrapper 9.7.0, configurado no arquivo
  `back-end/gradle/wrapper/gradle-wrapper.properties`.
- **Plugins Gradle:** `java`, `groovy` e `application`; a classe principal é
  `zg.acelera.Main`.
- **Lombok:** versão 1.18.32, como `compileOnly` e `annotationProcessor`, para
  reduzir boilerplate sem substituir contratos tipados.
- **Java NIO:** API `java.nio.file` para criar, ler e escrever os arquivos CSV
  de persistência.
- **Testes:** Groovy 4.0.21, Spock 2.3 para Groovy 4, Byte Buddy 1.14.13 e
  JUnit Platform Launcher. O Gradle executa os testes com
  `useJUnitPlatform()`.

A árvore de pacotes da aplicação, sob `back-end/src/main/java/zg/acelera`, é:

```text
app/
├── MainMenu
├── UserMenu
├── TaskMenu
└── CategoryMenu
controller/
├── UserController
├── TaskController
└── CategoryController
service/
├── UserService
├── TaskService
└── CategoryService
repository/
├── contratos de persistência
└── implementações em arquivos CSV
domain/
├── User
├── Task
├── Category
└── enums/Status
dto/
├── UserDTO
├── UserUpdateDTO
├── TaskDTO
└── TaskUpdateDTO
util/
├── exception/
└── interface_user/
```

Responsabilidades:

- `app`: menus interativos, fluxo da aplicação e leitura das opções do
  usuário.
- `controller`: coordenação da entrada da aplicação, chamada dos casos de uso
  e apresentação de mensagens na interface.
- `service`: regras de negócio, transformações entre DTOs e domínio e
  coordenação das operações dos repositórios.
- `repository`: contratos e implementações de persistência; as implementações
  usam Java NIO e arquivos CSV.
- `domain`: entidades, estado e enumerações do domínio.
- `dto`: contratos de entrada e atualização, incluindo validações de borda.
- `util`: interfaces de interação com o usuário e exceções específicas, como
  `TaskInfoWrongException` e `UserInfoWrongException`.

Os testes ficam em `back-end/src/test/groovy`, organizados nos pacotes
`controller`, `service` e `repository`.

## 3. Como executar e testar

Execute os comandos a partir do diretório `back-end`:

```bash
./gradlew build
./gradlew run --console=plain -q
./gradlew test
```

O primeiro comando compila e executa a verificação de build, o segundo inicia
o menu interativo da aplicação via CLI e o terceiro executa toda a suíte
automatizada. No Windows, use `gradlew.bat` no lugar de `./gradlew`:

```bat
gradlew.bat build
gradlew.bat run --console=plain -q
gradlew.bat test
```

## 4. Convenções

- Preserve a arquitetura em camadas e as fronteiras dos pacotes `app`,
  `controller`, `service`, `repository`, `domain`, `dto` e `util`. Coloque uma
  nova responsabilidade na camada existente mais específica; não crie uma
  arquitetura paralela sem alteração documentada da constituição.
- Valide toda entrada nas bordas: menus, controllers, métodos públicos de
  services e DTOs. A validação deve cobrir obrigatoriedade, formato, limites e
  invariantes entre campos antes da lógica de negócio ou da persistência.
  Cubra valores válidos, inválidos e de limite nos testes.
- Use o fluxo de erro padronizado já estabelecido: exceções específicas de
  domínio quando aplicável, causa preservada ao converter erros de I/O,
  mensagem acionável na interface e nenhum detalhe interno exposto ao
  usuário. Não use mensagens ad hoc, falhas silenciosas, retornos de sucesso
  falsos ou capturas amplas sem tratamento explícito.
- Mantenha tipagem estrita: use tipos explícitos para domínio, DTOs,
  repositórios, services e controllers; não use coleções raw, conversões
  unchecked ou casts desnecessários. Transformações entre DTO e domínio devem
  ser explícitas.
- Pacotes usam nomes minúsculos sob `zg.acelera`; classes e records usam
  `PascalCase`; métodos, parâmetros e variáveis usam `camelCase`; constantes
  usam `UPPER_SNAKE_CASE`.
- Interfaces representam contratos e implementações deixam seu papel explícito
  no nome, seguindo o padrão atual `NomeRepository`/`NomeRepositoryImpl` e
  `NomeInterface`/`NomeInterfaceImpl`.
- Mantenha DTOs separados das entidades de domínio. Use records para contratos
  de dados quando adequado e Lombok apenas quando mantiver o contrato claro.
- Prefira métodos pequenos, valores imutáveis quando possível e as abstrações
  existentes. Não duplique helpers nem mova classes entre camadas sem
  necessidade.
- Testes Spock devem usar nomes descritivos de especificação e permanecer no
  pacote correspondente (`controller`, `service` ou `repository`) dentro de
  `src/test/groovy`.

## 5. Restrições e Fluxo TDD

Toda mudança deve seguir **Test-First Delivery**:

1. escrever ou atualizar o teste que expressa o comportamento esperado;
2. executar o teste e confirmar que ele falha pelo motivo correto;
3. implementar a menor solução suficiente;
4. refatorar com os testes verdes, preservando contratos e legibilidade.

Os testes unitários devem cobrir o comportamento de services e repositories, e
testes de controller ou de integração devem cobrir cada borda alterada. Os
Quality Gates da constituição são obrigatórios:

- o plano de testes e os testes correspondentes devem existir antes da
  implementação;
- o build deve compilar com sucesso;
- a tarefa Gradle de testes relevante deve passar;
- validações, caminhos de erro e valores de fronteira devem estar cobertos;
- as fronteiras de pacotes e a arquitetura existente devem ser preservadas;
- a revisão deve verificar conformidade com estas regras e qualquer desvio
  deve estar documentado;
- `./gradlew build` é a verificação obrigatória de qualidade de release.

O agente **NUNCA deve alterar funcionalidades existentes fora do escopo da
feature atual nem adicionar dependências sem pedir permissão antes**. Também
não deve alterar formatos de persistência, comportamento visível da CLI,
fronteiras de pacotes ou o front-end fora do escopo solicitado. Upgrades de
dependências, migrações, mudanças destrutivas e reorganizações arquiteturais
exigem autorização explícita. Alterações não relacionadas já presentes no
worktree devem permanecer intocadas.
