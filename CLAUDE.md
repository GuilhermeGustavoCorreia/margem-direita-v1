# MargemDireitaModel1: instruções para o Claude

Modelo AnyLogic (Rail Library) das operações ferroviárias da Margem Direita do Porto de Santos.

## Antes de qualquer alteração

1. Perguntar: "O AnyLogic está fechado?" Só editar com resposta sim.
   O AnyLogic salva por cima de edições externas.
2. Explicar o problema e a solução proposta, e esperar confirmação.
3. Leitura é livre. Edição, não.
4. Antes de diagnosticar, reler o arquivo do agente ou pedir o texto atual do campo no AnyLogic.
   O arquivo no disco pode estar diferente do que está no editor.

## Onde estão as coisas

- Pasta do modelo: `C:\Users\cs404937\Desktop\projetos-digitais\anylogic\margem-direita\MargemDireitaModel1`
- Código-fonte: `_alp\Agents\<Agente>\`
  - `EmbeddedObjects.xml`: blocos do fluxograma e seus parâmetros
  - `Connectors.xml`: ligações entre blocos
  - `Variables.xml`: variáveis, parâmetros, ResourcePools
  - `Code\Functions.java` e `Code\Events.java`: código Java (ler estes antes dos XML)
  - `Levels\`: desenho e trilhos. Só ler para tarefas de layout.
- Ler só os arquivos do agente envolvido. Nunca carregar a pasta `_alp` inteira.

### Agentes

- **Main**: todos os trilhos e as linhas internas dos terminais.
- **Terminais**: CoperXI, CoperXXI, CostaLeite, MoegaoTerra, MoegaoMar, Bracell, Tgrao.
- **Gestores**: GestorDeLocomotivas, GestorDeCirculacao, GestorDeAlivios.
  GestorDeChegadas e GestorDeDVs ainda não estão desenvolvidos.
- **Auxiliares**: Trem, Locomotiva, Vagao, MoverTremCompleto, DevolverLocomotiva,
  ChamarLocomotiva, DescarregaLote, DescargaContinua, DecoupleDelay.

### Recursos de linha

- Os ResourcePools de linha ficam no GestorDeCirculacao: `main().gestorDeCirculacao.rp_...`.
- Não existe método `pst_*()` no Main para recurso de linha. Conferir o nome em `docs/gestores.md` antes de usar.

## Logs da simulação

- O console do AnyLogic é espelhado em `logs\sim.log` (ALT-011, função `configurar_log_em_arquivo` no On startup do Main).
  Entram `traceln`, `System.out` e `System.err`, inclusive exceções com stack trace.
- O arquivo é sobrescrito a cada execução. Ele reflete só a última rodada.
- Ler o log antes de pedir print ou texto do console. Pode ler com o AnyLogic aberto.
- Conferir a data na 1ª linha (`[log] <data/hora> gravando em ...`) para saber se é da rodada atual.
- O erro fica no fim do arquivo. O stack trace é da biblioteca (`Unknown Source`).
  Trem e recurso envolvidos aparecem nos traces logo antes do erro.
- Log grande: procurar pelo prefixo do trace (ex.: `[diag_liberar]`) ou pelo ID do trem, sem ler o arquivo inteiro.
- Erros na criação dos agentes, antes do On startup do Main, não entram no log.
- Traces novos: prefixo `[tag]`, tempo e ID do trem.
  Ex.: `traceln(String.format("[%.1f][Tgrao][bloco] trem=%d ...", time(), agent.getId()));`
- `logs/sim.log` é versionado no GitHub, para ser lido pelo Claude Code na web e no celular.
  Depois de rodar, Guilherme faz commit e push só do log, com a mensagem `log <descrição curta>`.
  Commit de log não é ALT e não entra no CHANGELOG.
- No Claude Code na web, o log é o do último push. Conferir a data na 1ª linha antes de diagnosticar.

## Documentação de apoio

Consultar quando a tarefa envolver o assunto. O código é a fonte da verdade.
Se um doc divergir do código, avisar e não alterar o modelo com base só no doc.

- Termos do domínio: `docs/glossario.md`
- Fluxo do trem de ponta a ponta: `docs/ciclo-do-trem.md`
- Papel de cada gestor e quem chama quem: `docs/gestores.md`
- Ficha de cada terminal: `docs/terminais/<nome>.md`
  - Ao tocar em um terminal, ler a ficha dele. Não ler as outras.

## Como editar

- Arquivos XML usam CRLF. Preservar ao editar.
- Nomes de blocos em `Connectors.xml` têm que bater exatamente com o `<Name>` em `EmbeddedObjects.xml`.
- Funções novas em snake_case (ex.: `get_tipo_loco_alivio`), não camelCase.
- Comentários no código citam a ALT (ex.: `// ALT-009: ...`).

## Regras de domínio que não podem ser quebradas

- Locomotiva que sobra volta ao GestorDeLocomotivas. Nunca vai para TrainDispose.
- Trem é licenciado pelo GestorDeCirculacao desde a chegada até a entrada do terminal.
- Na saída do alívio, o trem volta ao GestorDeCirculacao para sair da Margem Direita.
- Lotes `lote_*` ficam null após a descarga. `loteVazios_*` ficam null após o alívio.
- Alívio dispara com `getOverloadVazios() > 1`. Esse comportamento é intencional.
  [a confirmar: regra do overload]

## Versionamento

- Cada alteração é uma ALT-NNN.
- Um commit por ALT, com o número da ALT na mensagem.
- Registrar a ALT no `CHANGELOG.md`.
- O `README.md` tem só a lista de ALTs.
- Atualizar o doc correspondente (glossário, ciclo, gestor ou ficha do terminal) na mesma ALT
  que mudar o comportamento.

## Documentação HTML (quando for gerar ou editar)

- Fluxogramas horizontais.
- Referências ALT só no cabeçalho do documento.
- Todo documento de gestor tem a seção "Terminais testados".
- Ilustrações de trem seguem as cores do documento do CoperXI.

## Pendências

Ficam em `docs/pendencias.md`, não aqui. [ainda não criado]
