# Gestores

Cada gestor cuida de um recurso compartilhado. Terminais pedem ao gestor; o gestor não conhece a lógica interna do terminal.

> RASCUNHO. GestorDeAlivios e GestorDeChegadas ainda precisam de conferência no código.

Convenção dos documentos: carro de índice 0 aponta para a Ponta da Praia; o último, para o Valongo.

## GestorDeLocomotivas

Controla as locomotivas por pátio e por ferrovia.

- **Pools por pátio**: `pst_locomotivas_disponiveis`, `pmc_locomotivas_disponiveis`, `pcx_locomotivas_disponiveis`.
- **Pools por ferrovia**: `rp_locoRumo` (capacidade fixa 10), `rp_locoMRS`, `rp_locoVLI`. Escolhido por `selecionar_rp_locomotiva_trecho(ferrovia)`.
- **Criar loco de alívio**: `pst_colocarLocomotivas_alivioTerminal(moega)` (e pmc/pcx), que desconta do pátio e injeta a loco em `PST_POSICAO_CRIAR_LOCOMOTIVA`.
- **Receber loco de volta**: blocos `guardar_loco`, `guardar_locoRumo/MRS/VLI/FIPS` (Enter com `take(agent)`).
  Cada um leva a loco ao seu destino.
- **Devolver saldo**: `devolver_saldoLocomotiva(Trem)` soma 1 ao pool do pátio de cada loco do trem.
  Hoje só é chamado pelos blocos TrainDispose (`trainDispose`, `locoFips_pmc_dispose`).
- **Contadores de tela**: `locomotivas{Rumo,MRS,VLI}.size()`.

Regra: loco que sobra volta ao GestorDeLocomotivas. Nunca vai para TrainDispose.

## GestorDeCirculacao

- **Trens a caminho do terminal** (ALT-093): collection `trens_em_circulacao`. A Chegadas (`receber_trem`) e o ISN (`verificar_saida`) adicionam o trem quando o mandam para a circulação; o `log_saida` retira quando ele sai para o terminal.
  Os dois só mandam trem para um terminal que pode receber e sem outro trem da lista com o mesmo destino.

Controla as faixas de circulação e o licenciamento dos trens.

- **Prioridade da circulação** (ALT-139): `licenciar_esta_faixa` e `licenciar_proxima_faixa` têm prioridade da tarefa 1. Os Seize de várias faixas (alívio PMC e manobras dos terminais) ficam com 0, então o trem em circulação passa na frente deles na fila dos pools.

- **Faixas**: cada `Faixa` tem `estado` (LIVRE, OCUPADA, BLOQUEADA), `idTremLicenciado` e `recurso` (ResourcePool).
- **Entrada na circulação**: desde a ALT-084 o trem carregado chega pelas faixas do GestorDeISN e é entregue já com
  `faixa_atual = faixa_val_via2` e `proxima_faixa = faixa_via3_intermar` em `tentar_usar_faixa`. Daí é licenciado até a entrada do terminal de destino.
  O `chegada_carregados` / `para_via3_ate_santa` não recebe mais trens dos terminais [revisar: exceção da Bracell e botões manuais].
- **Regra de licenciamento**: o trem licencia só a faixa à frente (`faixa_atual` / `proxima_faixa`).
  Cada faixa tem `circular_*` → `ir_*` (TrainMoveTo) → Exit, que define o próximo par.
  A decisão de destino fica no fim da última faixa da rota.
- **Percurso do trem da Bracell** (ALT-014): `val_via2 → via3_intermar → via3_a → via3_b → via3_c → pst_silosMuro_a → pst_marginal_c → pmc_intervalo4000_a → pmc_intervalo4000_b → pmc_intervalo4000_c → bracell.encoste`.
  Decisões: `para_via3_ate_santa` (bracell começa na val_via2), ◇ `definir_sentido_val_via2`, `para_silosMuro_a` / ◇ `e_tgrao` (tgrao ou bracell → marginal_c),
  ◇ `e_bracell` no fim da marginal_c, `continuar3/4/5` na intervalo4000 (o `continuar5` entrega à Bracell).
- **Saída da Margem Direita**: o alívio devolve o trem ao GestorDeCirculacao, que licencia as faixas de saída
  (linha 6000 a, marginal c, silos muro a). Essas faixas são ocupadas com `ocupar()`, não bloqueadas,
  para que `liberar_faixas_ocupadas` as solte conforme o trem anda.
- **Circuito de saída do alívio até as DVs** (trem com `eh_alivio = true`, de ré): `marginal_c → marginal_b → marginal_a → via2_f … via2_a → via2_intermar_c … a → railwayTrack199 → val_via2 → gestorDeDVs.entrarNasDVs`.
  Os ◇ `definir_sentido_*` (`!eh_alivio`) separam o trem de alívio da circulação normal. Usado pelo Tgrao e pelo alívio na linha 4000 (entra pela `intervalo4000_a`, ALT-013).
- **Saída do alívio de celulose** (ALT-014, de ré): `intervalo4000_c → b → a` e daí o circuito de saída acima.
  ◇ `definir_sentido_intervalo4000_c/_b` separam o alívio. No `recuar_intervalo4000_b`, `liberar_linha5000_6000` solta a linha 5000/6000 (`get_recursos_linha_5000_6000`).
- Os blocos antigos `alivio_bracell*`, `linha5K*`, `linha_5k_*` e `circulacaoVzs_PCX` não são mais usados pela Bracell.
- **Liberar**: `liberar_faixas_ocupadas(Trem)` solta as faixas do trem que estão OCUPADA e vazias.
  Devolve os ResourcePools liberados para o Release do terminal.
- **Bloquear**: `bloquear_faixas(Trem, List<Faixa>)`.
- **Linhas e recursos**: `inicializar()` mapeia cada ResourcePool de linha à sua lista de trilhos.
- **Pendência — dois trens diretos para o mesmo terminal** (em aberto, 05/11/2026):
  o GestorDeChegadas manda um trem direto para a circulação quando `terminal.podeReceberNovoLote()` é true.
  O 1º trem entra e, enquanto ainda está a caminho, o terminal continua true; um 2º pedido para o mesmo terminal também entraria direto.
  Decidir se o terminal passa a false assim que recebe um trem destinado, ou se o GestorDeChegadas reserva o terminal até o `comecar_encoste`.

## GestorDeAlivios

Decide qual terminal aliviar, ou seja, retirar os vazios.

- Só dispara quando o overload de vazios de um terminal passa de 1.
- Pega locomotivas da ferrovia em atendimento ou de manobra, conforme `get_tipo_loco_alivio()`.
- Só tira um terminal de `terminais_pst` se houver loco no wait da ferrovia.
  [verificar: regra completa do alívio]
- **Faixa de criação da loco PST** (ALT-092): no `seize_faixa_criacao_pst` a faixa (`via1_intermar_d` para `pos_locoAlivio_PST`) fica OCUPADA com o id da loco.
  Novo alívio do PST só começa com essa faixa LIVRE. Quem libera é o GestorDeCirculacao (`liberar_faixas_ocupadas`) quando o trem de volta,
  que é o mesmo agente da loco, sai dela. Se a faixa de criação não for a de entrada na circulação (Tgrao: `marginal_c`), o `retornar_alivio` libera.
- **Prefixo do trem de alívio** (ALT-092): `MNB` + número ímpar (MNB01, MNB03 … MNB99, depois volta a MNB01), contador `contador_mnb`.
  Vale para PST, PMC e linha 4000. Os prefixos de chegada são pares (`gerar_prefixo`).
- **Celulose PMC** (ALT-014, ALT-061, `analisar_pmc`): Bracell, Suzano e Eldorado, lista `terminais_pmc`.
  Hoje a lista só tem a Bracell: a Suzano entra quando tiver o fluxo de coleta dos vazios; o Eldorado, quando o agente existir.
  Lógica separada: não usa a linha 4000 e não depende das DVs. Só verifica overload > 1 e se há loco da ferrovia no wait.
  Um alívio do PMC por vez (`terminal_alivio_pmc`). Ida: `loco_alivio_pmc → ocupar_linha5000_6000 → trainEnter_pmc` (`circ_pos_inicio_L5K`) `→ ir_alivio_pmc` (`get_pos_alivio()`) `→ para_alivio_terminal_pmc` (`takeAlivio`).
  O trem sai com todos os vazios do terminal. Volta: o terminal define `faixa_atual`/`proxima_faixa` e chama `retornar_alivio_pmc(trem)`,
  que solta a trava, devolve o terminal para `terminais_pmc`, marca `eh_alivio` e entrega o trem ao GestorDeCirculacao.
- **Linha 4000** (`terminais_pcx`): T39 e ADM (ALT-025); TES ainda vai ser criado.
  Um alívio da 4000 por vez: `analisar_pcx` para o laço quando `aux_esta_ocorrendo_alivio_pcx` fica true.
  ADM: alivia com 30 vazios exatos na linha 7 (`QUANTIDADE_PARA_ALIVIAR`); a loco chega em `_adm_alivio_comeco` (linha2000_e).
- **Tgrao**: o alívio é sempre de 40 vagões, nunca menos.
  Só alivia se o GestorDeDVs tiver mais de 40 vagões de espaço livre para a ferrovia do Tgrao. [pendente no código]
- **Alívio na linha 4000** (circuito PCX, ALT-013): quando o trem volta do alívio com `CAPACIDADE_LINHA_4000` (82) vagões,
  ◇ `linha_4000_cheia` → `liberar_linha_4000` (solta `linha4000_a/b/c`) → `para_alivio_na_linha_4000`, que marca `eh_alivio`,
  zera `lote_linha_4000` e entrega o trem ao GestorDeCirculacao com `faixa_atual = intervalo4000_a`, `proxima_faixa = marginal_c`.
  Com menos de 82, o trem espera em `wait_trem_4000`.

## GestorDeISN

Pátio ISN (ALT-084): estacionamento e circulação dos trens carregados antes da Margem Direita.
O trem nasce na `circ_pos_inicio_placa_2`, dentro da `isn_linha01_a`. Documento completo: artefato "GestorDeISN".

- **Faixas**: `faixa_isn_a..e` (um trecho `isn_linha01_*` cada) e `faixa_isn_porteira` (`psn_linha_porteira`).
  Pools `rp_isn_*` (capacidade 1) no próprio GestorDeISN; `linhaTMDS` são as polylines do painel do ISN.
  Licenciam como as da circulação: usam o mecanismo `tentar_usar_faixa` do GestorDeCirculacao.
  `registrar_faixas()` (On startup) as adiciona em `gestorDeCirculacao.faixas`, para `liberar_faixas_ocupadas` liberá-las.
- **Fluxo**: `chegada` → cada faixa `circular_isn_*` → `ir_isn_*` → Exit, que chama `seguir_para_faixa(trem, atual, proxima, bloco)`.
  Pares: nascimento a/b · fim a: b/c · fim b: c/(d ou null) · fim c: ◇ `e_estacionar` · d/e · e/porteira · porteira/val_via2.
  No fim da porteira, `para_val_via2` entrega: `faixa_val_via2` / `faixa_via3_intermar`.
- **Estacionamento (linha 02)**: trem com `estacionarISN = true` (gravado pelo GestorDeChegadas, que também reserva `lote_linha02`).
  No fim da faixa c: `ir_isn_linha02_e` (atravessa pelo `railwayTrack24`) → `ir_linha02` (até `_isn_estacionar_linha02`, na `isn_linha02_i`)
  → `liberar_linha01` (Release das faixas a..c) → `espera_linha02` (Wait).
- **Saída da linha 02**: `ev_verificar_saida` (1 min) → `verificar_saida()`: com `podeReceberNovoLote()` true dá `free`.
  `sair_linha02` zera `estacionarISN` e segue para a faixa da porteira (próxima `faixa_val_via2`), sem voltar à linha 01.
  A vaga (`lote_linha02 = null`) só é solta quando o trem sai de todos os trechos `isn_linha02_*` (`esta_na_linha02`).
- **Linha 04**: fora do escopo (`lote_linha04` sem uso).
- **Traces**: prefixo `[ISN]`, tempo em horas e id do trem.

## GestorDeDVs

Ainda não desenvolvido. Requisitos já definidos:

- Recebe o trem de alívio em `entrarNasDVs`.
- Precisa informar o espaço livre, em vagões, por ferrovia.
  O GestorDeAlivios usa esse valor antes de aliviar o Tgrao (precisa ser mais de 40).

## GestorDeChegadas

Em desenvolvimento (ALT-084). Parte ligada ao ISN:

- `decidir_destino`: terminal pode receber → circulação; senão `gestorDeISN.pode_estacionar()` → ISN; senão espera na fila.
- `entrada_livre`: só coloca trem com `faixa_isn_a` LIVRE e `isn_linha01_a` vazia.
- `receber_trem`: liga trem e pedido; destino ISN grava `gestorDeISN.lote_linha02` e `trem.estacionarISN = true`; entrega em `gestorDeISN.chegada`.
- Os Exits `paraGestorCirculacao_chegadaCarregados` dos terminais (ADM, CoperXI, CoperXXI, MoegaoMar, MoegaoTerra, TES, Tgrao) chamam `receber_trem`.

## Como os gestores conversam com os terminais

| De → Para | Chamada | Quando |
|---|---|---|
| Terminal → GestorDeLocomotivas | `selecionar_rp_locomotiva_trecho`, `guardar_loco.take` | Locomotiva sobra ou precisa ser devolvida |
| Terminal → GestorDeCirculacao | `liberar_faixas_ocupadas((Trem) agent)` | Trem sai de uma faixa |
| Terminal PMC → GestorDeAlivios | `retornar_alivio_pmc(trem)` (Bracell: faixa_atual = intervalo4000_c) | Fim do alívio de celulose |
| GestorDeAlivios → Terminal PMC | `takeAlivio(Trem)` via `analisar_pmc` | Overload > 1 |
| GestorDeAlivios → Terminal | `getOverloadVazios()`, `takeAlivio(Trem)`, `get_pos_alivio()` | Alívio precisa começar |
| GestorDeAlivios → GestorDeLocomotivas | `pst_colocarLocomotivas_alivioTerminal(moega)` | Criar loco de manobra para o alívio |
| GestorDeAlivios → GestorDeDVs | espaço livre por ferrovia [a criar] | Antes de aliviar o Tgrao |
| GestorDeAlivios → GestorDeCirculacao | `tentar_usar_faixa.take(agent)` | Alívio na linha 4000: trem cheio vai para as DVs |
| Terminal → GestorDeChegadas | `receber_trem((Trem) agent)` | Trem carregado nasce na placa 2 |
| GestorDeChegadas → GestorDeISN | `pode_estacionar()`, `lote_linha02`, `chegada.take(trem)` | Decidir destino e entregar o trem |
| GestorDeISN → GestorDeCirculacao | `tentar_usar_faixa.take(trem)`, `faixas.add`, `liberar_faixas_ocupadas` | Licença das faixas do ISN e entrega na val_via2 |
