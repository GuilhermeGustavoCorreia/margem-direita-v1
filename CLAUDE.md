# MargemDireitaModel1: instruções para o Claude

Modelo AnyLogic (Rail Library) das operações ferroviárias da Margem Direita do Porto de Santos.

## Antes de qualquer alteração

1. Antes de aplicar, mostrar a alteração em dois blocos:
   - **As is:** como está hoje (código ou comportamento atual).
   - **To be:** como vai ficar.
   Depois perguntar: "Faz sentido essa lógica?". Se a resposta for sim, aplicar.
   O AnyLogic salva por cima de edições externas.
2. Se houver decisão em aberto, perguntar junto, antes do "Confirmar alteração?".
3. Leitura é livre. Edição, não.
4. Antes de diagnosticar, reler o arquivo do agente ou pedir o texto atual do campo no AnyLogic.
   O arquivo no disco pode estar diferente do que está no editor.

## Onde estão as coisas

- Pasta do modelo: `C:\Users\cs404937\Desktop\projetos-digitais\anylogic\margem-direita\margem-direita-desktop`
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
- **Gestores**: GestorDeLocomotivas, GestorDeCirculacao, GestorDeAlivios, GestorDeISN, GestorDeChegadas.
  GestorDeChegadas e GestorDeDVs ainda não estão desenvolvidos.
- **Auxiliares**: Trem, Locomotiva, Vagao, MoverTremCompleto, DevolverLocomotiva,
  ChamarLocomotiva, DescarregaLote, DescargaContinua, DecoupleDelay.

### Recursos de linha

- Os ResourcePools de linha ficam no GestorDeCirculacao: `main().gestorDeCirculacao.rp_...`.
  Exceção (ALT-084): as faixas do pátio ISN e seus pools ficam no GestorDeISN (`main().gestorDeISN.rp_isn_*`).
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
- Traces novos usam a classe `Log` (`_alp/Classes/Class.Log.java`), nunca `traceln` direto:
  `Log.info("Origem", "evento", String.format("trem=%d ...", ...));` (também `debug`, `warn`, `erro`).
  Saída: `[00:19 - 01/01] INFO  GesCirculacao   exit_para_via3_a   trem=...`. Tempo em `hh:mm - dd/mm`, contado de 00:00 da `data_inicio` (`simulacao/config/simulacao.csv`); origem = nome do agente.
  Para o % de 720 h: horas = (dias desde `data_inicio`) × 24 + hh + mm/60.
  Cada replicação chama `Log.iniciar(this)` (hoje no startup do GestorDeCirculacao, GestorDeISN e GestorDeChegadas).
  `Log.nivelMinimo` e `Log.silenciados` filtram por nível e por origem. Convertidos até agora: GestorDeCirculacao, GestorDeISN, GestorDeChegadas, GestorDeAlivios, GestorDeLocomotivas, ADM, TES, T39.
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
- Comentários no código não citam ALT (ALT-087). O histórico fica no `CHANGELOG.md`.

## Regras de domínio que não podem ser quebradas

- Locomotiva que sobra volta ao GestorDeLocomotivas. Nunca vai para TrainDispose.
- Trem é licenciado pelo GestorDeCirculacao desde a chegada até a entrada do terminal.
- Na saída do alívio, o trem volta ao GestorDeCirculacao para sair da Margem Direita.
- Lotes `lote_*` ficam null após a descarga. `loteVazios_*` ficam null após o alívio.
- Tgrao: alívio sempre de 40 vagões, nunca menos. Só alivia se o GestorDeDVs tiver mais de 40 vagões de espaço para a ferrovia do Tgrao.
- Linha 4000: só T39, TES e ADMO. Celulose do PMC (Bracell, Suzano, Eldorado) não alivia pela linha 4000:
  sai com todos os vagões que chegaram e não depende das DVs.
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
- Em qualquer documento, o carro de índice 0 aponta para a Ponta da Praia e o último para o Valongo.

## Pendências

Ficam em `docs/pendencias.md`, não aqui. [ainda não criado]
