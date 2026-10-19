# Especificação da feature de Alarmes

## 1. Contexto e objetivo

O TODO List permite cadastrar tarefas com data/hora inicial e final,
prioridade e status. Esta feature adiciona alarmes vinculados às tarefas para
avisar o usuário quando a tarefa estiver próxima do término.

O usuário poderá cadastrar até três alarmes por tarefa, informando a
antecedência em horas e minutos em relação à data/hora final da tarefa. Os
alarmes serão persistidos em um arquivo CSV separado e monitorados enquanto a
aplicação estiver aberta. O aviso será emitido no console.

Cada alarme pode disparar uma vez no seu instante configurado. O alarme com
menor antecedência em relação ao fim também inicia um ciclo de repetição, com
intervalo igual à sua própria antecedência, até a tarefa ser concluída. A
prioridade da tarefa não altera a antecedência informada.

## 2. Regras de negócio (incluindo casos de borda)

### Cadastro e validação

- Um alarme pertence a exatamente uma tarefa.
- A tarefa deve possuir data/hora final válida para receber alarmes.
- A antecedência é informada em horas e minutos inteiros e deve ser de, no
  mínimo, um minuto.
- O instante calculado do alarme é a data/hora final menos a antecedência.
- O instante calculado não pode estar no passado no momento da criação ou
  edição do alarme.
- A antecedência deve ser menor que o intervalo entre a data/hora inicial e a
  data/hora final da tarefa. Antecedência maior ou igual à duração da tarefa é
  rejeitada.
- Uma tarefa pode possuir no máximo três alarmes.
- A prioridade da tarefa, de 1 a 5, não modifica a antecedência nem o
  instante calculado.
- Os status `TODO` e `DOING` permitem o processamento de alarmes. Tarefas
  `DONE` não disparam alarmes.
- Alterações na tarefa não removem automaticamente os alarmes existentes. A
  edição deve preservar os alarmes, mas qualquer novo instante calculado deve
  ser validado.
- Se uma edição gerar um novo instante futuro para um alarme que já disparou,
  o alarme poderá ser reativado conforme o novo instante. O estado anterior
  não deve causar repetição sem que exista um novo instante válido.
- Ao excluir uma tarefa, seus alarmes também devem ser excluídos.

### Seleção e disparo

- Os três alarmes podem ser cadastrados e disparar uma vez nos seus
  respectivos instantes.
- O alarme com menor antecedência, portanto mais próximo da data/hora final,
  é o único que entra em repetição.
- A repetição do alarme principal ocorre a cada intervalo igual à
  antecedência configurada para ele e continua até o status da tarefa mudar
  para `DONE`.
- Os alarmes que não são o principal não entram em repetição depois do
  disparo único.
- Cada disparo deve ser marcado no arquivo de alarmes para que uma execução
  posterior não repita indefinidamente o mesmo evento.
- Se a aplicação estiver fechada no instante do alarme, a primeira verificação
  após a reabertura deve emitir o aviso e iniciar o ciclo aplicável, desde que
  a tarefa ainda não esteja `DONE`.
- Se a aplicação estiver aberta e a thread verificar até 59 segundos depois do
  instante calculado, o aviso ainda é válido. A thread verifica os alarmes a
  cada um minuto.
- O monitoramento começa quando o primeiro alarme é criado e deve terminar
  quando não houver mais alarmes para monitorar.
- A thread deve ser encerrada de forma controlada quando a aplicação for
  finalizada.
- Um alarme disparado não deve ser repetido pelo mesmo instante em duas
  verificações consecutivas.

### Persistência e erros

- Os alarmes devem ser salvos em um arquivo CSV separado do arquivo de tarefas,
  usando Java NIO através da camada de repository.
- O registro persistido deve manter, no mínimo, a identificação da tarefa, a
  antecedência, o instante calculado, a indicação de ser o alarme principal e
  o estado necessário para controlar disparos e repetições.
- Falhas de leitura ou gravação devem preservar a causa na exceção de
  persistência e chegar à interface pela mensagem de erro padronizada.
- Não é permitido ignorar registros inválidos, mascarar falhas de I/O ou
  recriar silenciosamente o arquivo.

## 3. Critérios de aceite (no formato Dado / Quando / Então, prontos para virarem testes unitários no TDD)

1. **Dado** uma tarefa `TODO` com data/hora final futura, **quando** o usuário
   cadastrar um alarme de 2 horas e 30 minutos antes do fim, **então** o
   alarme deve ser aceito e persistido com o instante final menos 2 horas e 30
   minutos.

2. **Dado** uma tarefa com data/hora final válida, **quando** o usuário
   informar antecedência zero ou negativa, **então** o cadastro deve ser
   rejeitado pela validação de entrada.

3. **Dado** uma tarefa com intervalo de uma hora entre início e fim, **quando**
   o usuário informar antecedência de uma hora ou mais, **então** o cadastro
   deve ser rejeitado por inconsistência entre duração e antecedência.

4. **Dado** uma tarefa com data/hora final futura, **quando** o instante
   calculado do alarme estiver no passado, **então** a criação ou edição deve
   ser rejeitada e nenhum alarme inválido deve ser persistido.

5. **Dado** uma tarefa que já possui três alarmes, **quando** o usuário tentar
   cadastrar um quarto alarme, **então** a operação deve ser rejeitada com a
   mensagem de limite atingido.

6. **Dado** uma tarefa com prioridade 1 e outra com prioridade 5, ambas com a
   mesma data/hora final e antecedência configurada, **quando** os alarmes
   forem calculados, **então** os instantes devem ser iguais.

7. **Dado** uma tarefa `DONE` com alarmes pendentes, **quando** o monitoramento
   verificar a tarefa, **então** nenhum aviso deve ser emitido e nenhum ciclo
   de repetição deve ser iniciado.

8. **Dado** uma tarefa `TODO` com três alarmes válidos, **quando** cada instante
   configurado for alcançado, **então** cada alarme deve emitir seu aviso
   único, e somente o alarme mais próximo do fim deve ser elegível para
   repetição.

9. **Dado** o alarme principal com antecedência de 2 horas, **quando** ele
   disparar e a tarefa continuar `TODO` ou `DOING`, **então** o próximo aviso
   do ciclo deve ocorrer após 2 horas.

10. **Dado** um alarme que já foi marcado como disparado no CSV, **quando** a
    aplicação for reiniciada e fizer a primeira verificação, **então** o mesmo
    disparo não deve ser emitido novamente pelo mesmo instante.

11. **Dado** um alarme cujo instante passou enquanto a aplicação estava
    fechada, **quando** a aplicação for reaberta antes de a tarefa ser
    concluída, **então** a primeira verificação deve emitir o aviso e atualizar
    o estado persistido.

12. **Dado** a aplicação aberta com pelo menos um alarme monitorável, **quando**
    a thread executar sua verificação periódica, **então** ela deve verificar os
    alarmes a cada um minuto e aceitar atraso de até 59 segundos no aviso.

13. **Dado** que o último alarme monitorável seja removido ou sua tarefa seja
    excluída, **quando** o monitoramento for atualizado, **então** a thread
    deve ser encerrada de forma controlada.

14. **Dado** uma tarefa com alarmes cadastrados, **quando** a tarefa for
    editada sem alterar os alarmes, **então** os alarmes devem permanecer
    associados à tarefa.

15. **Dado** um alarme já disparado, **quando** uma edição criar um novo
    instante futuro válido para ele, **então** o alarme deve ser reativado para
    o novo instante e persistido sem reutilizar o disparo antigo.

16. **Dado** uma falha de leitura ou gravação no CSV de alarmes, **quando** a
    operação de persistência for executada, **então** o repository deve lançar
    a exceção de persistência padronizada, preservando a causa, e o controller
    deve exibir a mensagem de erro prevista.

17. **Dado** uma tarefa com status `TODO` ou `DOING`, **quando** o usuário
    utilizar o menu de tarefas, **então** deve ser possível criar, editar,
    listar e remover alarmes sem alterar os filtros, a ordenação ou o dashboard
    existentes.

## 4. Impacto no que já existe (filtros, status, persistência em arquivo, menu CLI)

- **Domínio e DTOs:** devem representar a configuração e o estado dos alarmes
  sem misturar DTOs com entidades de domínio. As validações de obrigatoriedade,
  limites, antecedência e consistência temporal devem ocorrer nas fronteiras
  de entrada e nos métodos públicos de serviço.
- **Services e repositories:** deve ser criado o contrato de persistência de
  alarmes e sua implementação CSV usando Java NIO. O service deve coordenar
  cadastro, edição, remoção, cálculo do alarme principal, disparos e
  atualização do estado persistido.
- **Thread de monitoramento:** deve pertencer à camada apropriada do back-end,
  depender de contratos do service/repository e ter encerramento controlado.
  A implementação não deve acessar diretamente arquivos a partir do menu.
- **Status e filtros:** `TODO` e `DOING` são os únicos status que permitem
  disparos; `DONE` silencia alarmes. Os filtros por categoria e status, a
  listagem, a ordenação por prioridade e o dashboard devem continuar
  funcionando sem mudança de semântica.
- **Persistência:** os arquivos atuais de usuários, tarefas e categorias não
  devem ser alterados fora do necessário para a associação lógica. Alarmes
  devem usar um CSV separado, com leitura e gravação persistentes e tratamento
  explícito de falhas.
- **Menu CLI:** o menu de tarefas deve oferecer criação, edição, listagem e
  remoção de alarmes. As mensagens devem seguir o fluxo existente para
  sucesso, entrada inválida, item não encontrado e erro.
- **TDD:** antes da implementação, devem ser criados ou atualizados testes
  Spock de service e repository para regras e persistência, além de testes de
  controller ou integração para o menu/entrada alterados. O teste deve falhar
  primeiro, seguido pela implementação mínima e refatoração com a suíte verde.

## 5. Fora do escopo

- Alarmes para tarefas `DONE`.
- Alarmes baseados em prioridade ou que alterem automaticamente a antecedência.
- Mais de três alarmes por tarefa.
- Canais de notificação fora do console da aplicação, como e-mail, SMS,
  push, desktop notification ou integração com calendário.
- Execução do monitoramento quando a aplicação estiver encerrada, como um
  serviço do sistema operacional ou processo agendado externo.
- Alteração dos formatos ou da semântica dos arquivos existentes de usuários,
  tarefas e categorias, além da criação do CSV separado de alarmes.
- Alteração do front-end.
- Reorganização dos pacotes `app`, `controller`, `service`, `repository`,
  `domain`, `dto` e `util`.
- Mudanças no comportamento de filtros, ordenação, dashboard ou demais
  funcionalidades de tarefas que não sejam necessárias para integrar alarmes.
- Antecedência configurada como data/hora absoluta, valor global da aplicação
  ou unidade diferente de horas e minutos inteiros.
