# Relatório de execução

## Escopo

Este relatório registra o processo usado para criar a skill genérica
`skills/feature-com-spec/SKILL.md`, baseada no fluxo da feature de alarmes:

```text
contexto → spec → plano → tasks → implementação → validação
```

O objetivo da skill é orientar o agente a trabalhar de forma incremental,
preservar a arquitetura existente, escrever testes e validar individualmente
os critérios de aceite.

## Consumo aproximado por etapa

Os valores abaixo são estimativas de consumo de tokens/créditos. A sessão não
disponibilizou uma métrica confiável de cobrança por ferramenta, portanto os
valores representam faixas aproximadas de contexto analisado, instruções
geradas e operações realizadas, e não uma fatura exata.

| Etapa | Atividades | Tokens aproximados | Créditos relativos |
|---|---|---:|---:|
| Exploração do contexto | Leitura do `AGENTS.md`, documentos em `docs/`, skills existentes e estrutura do projeto | 4.000–6.000 | Baixo–médio |
| Análise do fluxo da feature | Comparação entre `spec`, `plan`, `tasks`, contratos e quickstart da feature de alarmes | 3.000–5.000 | Médio |
| Desenho da skill | Organização das fases, regras de TDD, rastreabilidade e validação de regressão | 2.000–3.000 | Baixo–médio |
| Escrita do arquivo | Criação de `skills/feature-com-spec/SKILL.md` com frontmatter e instruções operacionais | 2.000–3.000 | Baixo |
| Verificação | Leitura do arquivo criado, `git diff --check` e conferência do estado do worktree | 800–1.500 | Baixo |
| Commit | Staging seletivo, commit semântico e verificação do hash/arquivos incluídos | 300–700 | Muito baixo |
| **Total estimado** |  | **12.100–19.200** | **Baixo–médio** |

O commit gerado para a skill foi:

```text
8b2d102 feat(skills): add feature development workflow
```

Alterações preexistentes em `categories.csv`, `tasks.csv` e `.idea/.name`
foram mantidas fora desse commit.

## Divergência observada e correção

### Execução de todas as tasks de uma vez

Uma situação de divergência do processo ocorreu quando o agente tratou o
arquivo `tasks.md` como uma lista para execução contínua e implementou todas
as tarefas de uma vez, em vez de respeitar o desenvolvimento incremental
previsto no plano. Com isso, setup, fundação, testes, implementação,
integração e validação foram realizados no mesmo ciclo, sem os checkpoints
necessários entre as fases.

Essa abordagem divergiu da spec e do plano porque:

- não preservou a ordem de dependências entre as tarefas;
- reduziu a visibilidade sobre qual requisito estava sendo implementado;
- dificultou confirmar a falha esperada antes de cada implementação;
- misturou mudanças de infraestrutura, domínio e interface;
- adiou a validação dos critérios de aceite e das regressões existentes.

### Como o problema foi corrigido

Foi necessário refazer o plano de `tasks` para separar explicitamente:

1. tarefas de setup e fundação;
2. testes da capacidade antes do código de produção;
3. implementação mínima por história/entrega;
4. integração com controllers, menus e persistência;
5. validação dos critérios de aceite;
6. testes de regressão e build final.

O novo plano passou a exigir identificadores, caminhos de arquivos,
dependências e resultado verificável por tarefa. A execução foi retomada de
forma sequencial: primeiro o teste, depois a confirmação da falha, em seguida
a implementação mínima e, por fim, a refatoração com os testes verdes.

Essa correção foi incorporada à skill em duas partes:

- a seção **Tarefas executáveis (`tasks`)** exige tarefas ordenadas,
  dependências explícitas e critérios independentes;
- a seção **Implementação orientada a testes** impede avançar sem uma
  verificação correspondente e reforça o fluxo TDD.

## Aprendizados incorporados

- O plano de tasks deve ser tratado como um grafo de dependências, não apenas
  como uma lista de ações.
- Cada história deve ser implementável e testável de forma independente.
- Critérios de aceite precisam de rastreabilidade individual até um teste ou
  uma verificação manual.
- A validação manual deve cobrir as funcionalidades existentes afetadas, como
  listagem, filtros, mudança de status e alarmes.
- O commit deve conter somente os arquivos relacionados à entrega atual.
