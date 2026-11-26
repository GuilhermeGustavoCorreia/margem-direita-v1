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
  No `guardar_loco`, antes do `sairDaTrack`: `tem_varias_locos` (`size() > 1`) → `separar_loco` (TrainDecouple, 1 carro) em laço. Cada loco entra sozinha no wait da ferrovia, então o `get(0)` dos alívios sempre pega 1 loco.
- **Devolver saldo**: `devolver_saldoLocomotiva(Trem)` soma 1 ao pool do pátio de cada loco do trem.
  Hoje só é chamado pelos blocos TrainDispose (`trainDispose`, `locoFips_pmc_dispose`).
- **Contadores de tela**: `locomotivas{Rumo,MRS,VLI}.size()`.

Regra: loco que sobra volta ao GestorDeLocomotivas. Nunca vai para TrainDispose.

## GestorDeCirculacao

- **Trens a caminho do terminal** (ALT-093): collection `trens_em_circulacao`. A Chegadas (`receber_trem`) e o ISN (`verificar_saida`) adicionam o trem quando o mandam para a circulação; o `log_saida` retira quando ele sai para o terminal.
  Os dois só mandam trem para um terminal que pode receber e sem outro trem da lista com o mesmo destino.

Controla as faixas de circulação e o licenciamento dos trens.

- **Prioridade da circulação** (ALT-139): `licenciar_esta_faixa` e `licenciar_proxima_faixa` têm prioridade da tarefa 1. Os Seize de várias faixas (alívio PMC e manobras dos terminais) ficam com 0, então o trem em circulação passa na frente deles na fila dos pools.
- **Recurso `rp_mnb_pcx`** (ALT-142, capacidade 1): uma operação por vez na área comum de Tgrao, Bracell, Eldorado e Suzano (`silosMuro_a`, `marginal_c`, `linha6000_a`, linhas 5000/6000/7000, `intervalo4000`). Regra: pega primeiro o `rp_mnb_pcx`, depois faixas e linhas. Quem usa a `intervalo4000` pega o `mnb_pcx` antes.
  - Encoste: no `para_via3_b` (ALT-147; antes no `para_via3_d`), se `eh_terminal_mnb_pcx(trem)`, o trem passa por `entrar_mnb_pcx → ocupar_mnb_pcx → para_licenciar_apos_mnb` antes de licenciar a `via3_c` (a saída do alívio do Tgrao usa a `via3_c`). O terminal solta no fim do encoste (Bracell `liberar_linha5000_6000`, Eldorado `get_recursos_fim_encoste`, Suzano `liberar_faixas_circulacao`, Tgrao `liberar_mnb_pcx`).
  - Alívio e triangulação: ver GestorDeAlivios e Suzano.
  - Loco nova da 4000: `loco_pcx_starts` → `usar_recurso_loco_pcx` (2 locos FIPS) → `bloquear_4000` (`linha4000_a/b/c` + `intervalo4000_b/c`, logo antes do `trainEnter`, porque a loco nasce na `intervalo4000_b`). A `intervalo4000_b/c` é solta no `liberar_intervalo_criacao`, depois do `ir_entrada_terminal`.
  - Avanço no T39 e no ADM: o `paraFrente` (T39) e o `sair_linha7` (ADM) levam o trem até `pos_linha_4000_criar_loco`; acima de `LIMITE_VGS_INTERVALO_4000` (40) vagões ele entra na `intervalo4000_b`. Antes do avanço, `ocupar_mnb_pcx_avanco` pega o `mnb_pcx` sozinho (`get_mnb_pcx_para_avancar`: acima do limite e se o trem ainda não tem) e depois `ocupar_intervalo_avanco` pega `intervalo4000_c/b` (`get_intervalo_para_avancar`, só o que falta); solta no `liberar_faixas_ate_terminal`. Os Seizes do excedente pedem só o que o trem ainda não tem (`pools_intervalo_faltando`).
  - Excedente da 4000 (manobra e volta): pega o `mnb_pcx` sozinho antes da `intervalo4000_c/b` (`ocupar_mnb_pcx_excedente`, `ocupar_mnb_pcx_volta`), só se o trem ainda não tem (`mnb_pcx_faltando`; quem vem do avanço do T39/ADM já tem); `liberar_intervalo_4000` e `liberar_intervalo_volta` soltam as faixas e o `mnb_pcx` juntos.
  - Saída do trem cheio da 4000: `linha_4000_cheia` → `ocupar_mnb_pcx_saida_4000` (só o `mnb_pcx`) → `seize_saida_4000` (intervalo4000_c/b/a). O trem passa pela `marginal_c` e solta pela regra abaixo (é `eh_alivio`).
  - Saída do alívio (ALT-148): o trem leva o `mnb_pcx` para a circulação; a `liberar_faixas_ocupadas` inclui o `rp_mnb_pcx` quando o trem é `eh_alivio`, tem a unidade e a `marginal_c` já não está com ele (cauda fora da área comum).
  - Funções: `get_recursos_mnb_pcx(Trem)` (só se o trem tiver a unidade) e `incluir_mnb_pcx(ResourcePool[], Trem)`.

- **Faixas**: cada `Faixa` tem `estado` (LIVRE, OCUPADA, BLOQUEADA), `idTremLicenciado` e `recurso` (ResourcePool).
- **Entrada na circulação**: desde a ALT-084 o trem carregado chega pelas faixas do GestorDeISN e é entregue já com
  `faixa_atual = faixa_val_via2` e `proxima_faixa = faixa_via3_intermar` em `tentar_usar_faixa`. Daí é licenciado até a entrada do terminal de destino.
  O `chegada_carregados` / `para_via3_ate_santa` não recebe mais trens dos terminais [revisar: exceção da Bracell e botões manuais].
- **Regra de licenciamento**: o trem licencia só a faixa à frente (`faixa_atual` / `proxima_faixa`).
- **Par `via1_intermar_a` + `dv_entrada_c`**: são um trecho só. O `licenciar_esta_faixa` pega as duas juntas (conjunto único) quando a `faixa_atual` é uma delas, em qualquer sentido: chegada ao TEC/TEC_ACUCAR, saída do TEC (alívio) e ida para as DVs. Na ida para as DVs, `para_DVS`/`alivio_para_DVs`/`exit3` marcam a `dv_entrada_c` OCUPADA; o `liberar_faixas_ocupadas` só a solta depois que o trem sai da `via1_intermar_a` (o GestorDeDVs chama em `liberar_faixas_circulacao`/`liberar_faixas_ponto`).
- **Saída para as DVs**: antes de `para_DVS` e `alivio_para_DVs` o trem passa por um Release (`liberar_antes_dvs` / `liberar_antes_dvs_alivio`) que solta as faixas que a cauda já deixou. Sem isso, o trem na fila das DVs (`fila_dvs`) segurava faixas atrás dele (ex.: `via2_c`) até entrar nas DVs.
- **`get_conjunto_licenca(trem, faixa)`**: conjunto que `licenciar_esta_faixa` e `licenciar_proxima_faixa` pegam. Par `via1_intermar_a` + `dv_entrada_c` (sem o cruzamento); `val_via2_a` ou `val_via2` + `rp_cruzamento_porteira`; trem chegando ao TEC na `val_via2_a` pega a rota inteira (`val_via2_a` + cruzamento + par). O que o trem já tem fica de fora.
- **Cruzamento da porteira** (`rp_cruzamento_porteira`, capacidade 1): AMVs `railwaySwitch1` (`val_via2_a`/`val_via2`/`railwayTrack18`) e `railwaySwitch27` (`dv_entrada_b`/`dv_entrada_c`/`railwayTrack18`). Quem pega: (1) quem licencia `val_via2_a` ou `val_via2`; (2) o trem que entra nas linhas das DVs, no `seize_cruzamento_entrada` (GestorDeDVs, entre `sel_segue_direto.outF` e `ir_ponto`, via `get_conjunto_entrada_dvs`), e não na fila; (3) a saída das DVs (`ocupar_val_via2_saida`, via `get_conjunto_saida_dvs`: `val_via2` + `isn_linha02_e` + cruzamento). O trem que segue direto sai pela `railwayTrack199` → `val_via2` → AMV da porteira. Antes do Seize da saída, o Release `liberar_antes_saida` solta o que a cauda já deixou. Solta no `liberar_faixas_ocupadas` quando o trem não tem mais `val_via2_a`, `val_via2` nem `dv_entrada_c` e não está na `railwayTrack18`.
- **Cruzamento como senha na saída das DVs**: `liberar_antes_saida` → `pegar_cruzamento_saida` (só o cruzamento, `get_conjunto_entrada_dvs`, prioridade 10) → `ocupar_val_via2_saida` (`val_via2` + `isn_linha02_e`, `get_conjunto_saida_dvs`). Pedir cruzamento + faixas no mesmo Seize punha o trem na fila da `val_via2` sem ter o cruzamento e travava quem tinha (ex.: X18 saindo do ISN). Pendente: trem que chega à `val_via2` pelo lado da `via3_intermar` ainda pede `val_via2` + cruzamento juntos.
- **Release no início do passo** (`liberar_antes_de_licenciar`, entre `tentar_usar_faixa` e `esta_licenciado_nesta_faixa`): antes de esperar a próxima licença, o trem solta as faixas OCUPADAS vazias e o cruzamento (`liberar_faixas_sem_mnb_pcx`, que roda o `liberar_faixas_ocupadas` com `pular_mnb_pcx = true`). O `mnb_pcx` continua no `liberar_faixas_usadas`, no fim do passo, depois de licenciar a `marginal_c`. Sem isso, um trem parado esperando licença segurava o que a cauda já tinha deixado (M86 com `val_via2` + cruzamento na `via3_intermar`).
- **Traces de licença** (sempre ligados):
  - `aguarda_licenca`: no `onEnter` do `licenciar_esta_faixa` / `licenciar_proxima_faixa` e dos Seize do cruzamento nas DVs, só se algum recurso do conjunto estiver ocupado. Mostra cada recurso ocupado e o dono (faixa: trem e estado; cruzamento: `dono_cruzamento`).
  - `pegou_licenca`: no `onExit`, só para quem logou `aguarda_licenca` (conjunto `esperando_licenca`).
  - `pegou cruzamento_porteira` / `soltou cruzamento_porteira`: dono do cruzamento em `dono_cruzamento`.
  - `soltou <faixa>`: cada faixa solta no `liberar_faixas_ocupadas`.
- **Prioridade no cruzamento**: `seize_cruzamento_entrada` e `ocupar_val_via2_saida` (GestorDeDVs) têm `priority = 10`; as licenças da circulação ficam com 1. Quem já está nas DVs ou com o par passa na frente na fila do cruzamento (a fila do pool é por ordem de chegada e um pedido de conjunto inteiro que não fecha travava quem vinha atrás).
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
- **Traces periódicos** (`trace_repetitivo`, boolean, padrão `false`): liga os logs que saem a cada 60 s (`diag_pst` de espera, `alivio4000_compatibilidade ... espera`, `alivio_pmc ... aguardando loco`, `saida_4000_livre`). Os logs de evento saem sempre.
- **Excedente da 4000** (`LIMITE_SEM_MANOBRA_4000 = 78`; a 4000 comporta ≈ 80 vagões e a ponta da `4000_c` sai pelo sw111 → `track176` → sw110): ao voltar de um terminal com mais de 78 vagões, depois do `ir_linha_4000`, o alívio pega `intervalo4000_c/b` (`seize_intervalo_4000`), avança de ré até o `pos_linha_4000_criar_loco` (`avancar_criar_loco`), recua para a frente até o `_linha4000_excedente` na `track101` (`recuar_track178`, a sobra fica na `track178`/`track101`, que não são usadas) e solta `intervalo4000_c/b` (`liberar_intervalo_4000`). Só então o `liberar_faixas_ate_terminal` solta o `acesso_moema`. Na próxima saída para um terminal, depois de pegar acesso e faixas, `sel_esta_no_excedente` (ocupa `track178`/`track101`?) pega `intervalo4000_c/b` (`seize_intervalo_volta`, prioridade 10, só quem já tem o acesso chega aqui), volta de ré até o `pos_linha_4000_criar_loco` (`voltar_do_excedente`), para a ponta sair da `track178` (da `track178` não há caminho até a `track176`), segue no `ir_entrada_terminal1` e só solta `intervalo4000_c/b` ao chegar no terminal (`liberar_intervalo_volta`).
- **Acesso Moema no alívio da 4000** (`rp_acesso_moema`, "senha" da área PMC): só quem tem o acesso pede as linhas da área (3000, 2000, linha 03 do TES...). TES, ADM e T39 pegam o acesso sozinho, só nos movimentos que colocam lote na moega (`acesso_moema_manobra`, `acesso_moema_encoste`, `acesso_moema_corte`); a chegada até o ponto de encoste não pede o acesso, anda só com as faixas da circulação. O alívio da 4000 também pega o acesso sozinho: `comecar_alivio` → `pegar_acesso_moema_pcx` (só o acesso) → `bloquear_faixas_ate_terminal` (só as faixas, `conjunto_alivio_pcx()`). Pedir acesso + faixas no mesmo Seize punha o alívio na fila das linhas sem ter o acesso e travava quem tinha (ex.: TES no `seize` do encoste da linha 03).
- **Um alívio PST por vez**: o disparo exige `rp_alivio_pst` livre **e** nenhum alívio em andamento (`alivio_em_andamento_pst` sem moegas pendentes, `finalizou_moegas()`).
  O `rp_alivio_pst` só é pego no `seize_alivio_pst`, no fim do fluxo da loco; sem a segunda condição, outro alívio sobrescrevia o `alivio_em_andamento_pst` enquanto a loco do Tgrao esperava o `mnb_pcx`.
- **Prefixo do trem de alívio** (ALT-092): `MNB` + número ímpar (MNB01, MNB03 … MNB99, depois volta a MNB01), contador `contador_mnb`.
  Vale para PST, PMC e linha 4000. Os prefixos de chegada são pares (`gerar_prefixo`).
- **Celulose PMC** (ALT-014, ALT-061, `analisar_pmc`): Bracell, Suzano e Eldorado, lista `terminais_pmc`.
  Hoje a lista só tem a Bracell: a Suzano entra quando tiver o fluxo de coleta dos vazios; o Eldorado, quando o agente existir.
  Lógica separada: não usa a linha 4000 e não depende das DVs. Só verifica overload > 1 e se há loco da ferrovia no wait.
  No `bloquear_faixas_pmc` (ALT-140), a loco da Suzano e do Eldorado pede, no mesmo conjunto, as faixas do terminal e a `rp_linha6000`; o terminal solta tudo no início do alívio (`get_recursos_loco_alivio`). A Bracell não pede a 6000 (sem Release no alívio).
  Antes do `bloquear_faixas_pmc`, a loco pega o `rp_mnb_pcx` (`ocupar_mnb_pcx_pmc`, ALT-142); `retornar_alivio_pmc` solta no `soltar_mnb_pcx_pmc → liberar_mnb_pcx_pmc → para_circulacao_alivio_pmc`.
  No PST, o alívio do Tgrao passa por `eh_alivio_tgrao → ocupar_mnb_pcx_pst` antes do `seize_faixa_criacao_pst`; o `liberar_faixas_pst` solta.
  Um alívio do PMC por vez (`terminal_alivio_pmc`). Ida: `loco_alivio_pmc → ocupar_linha5000_6000 → trainEnter_pmc` (`circ_pos_inicio_L5K`) `→ ir_alivio_pmc` (`get_pos_alivio()`) `→ para_alivio_terminal_pmc` (`takeAlivio`).
  O trem sai com todos os vazios do terminal. Volta: o terminal define `faixa_atual`/`proxima_faixa` e chama `retornar_alivio_pmc(trem)`,
  que solta a trava, devolve o terminal para `terminais_pmc`, marca `eh_alivio` e entrega o trem ao GestorDeCirculacao.
- **Linha 4000** (`terminais_pcx`): T39 e ADM (ALT-025); TES ainda vai ser criado.
  Um alívio da 4000 por vez: `analisar_pcx` para o laço quando `aux_esta_ocorrendo_alivio_pcx` fica true.
  ADM: alivia com 30 vazios exatos na linha 7 (`QUANTIDADE_PARA_ALIVIAR`); a loco chega em `_adm_alivio_comeco` (linha2000_e).
- **Tgrao**: o alívio é sempre de 40 vagões, nunca menos.
  Só alivia se o GestorDeDVs tiver mais de 40 vagões de espaço livre para a ferrovia do Tgrao. [pendente no código]
- **Alívio na linha 4000** (circuito PCX, ALT-013): quando o trem volta do alívio com `CAPACIDADE_LINHA_4000` (82) vagões,
  ◇ `linha_4000_cheia` → `seize_saida_4000` (pega `intervalo4000_c/b/a` de uma vez; c e b ficam OCUPADA, a LICENCIADA) → `liberar_linha_4000` (solta `linha4000_a/b/c`) → `para_alivio_na_linha_4000`, que marca `eh_alivio`,
  zera `lote_linha_4000` e entrega o trem ao GestorDeCirculacao com `faixa_atual = intervalo4000_a`, `proxima_faixa = marginal_c`.
  Com menos de 82, o trem espera em `wait_trem_4000`.
  Na volta do alívio, o `ir_linha_4000` para o trem a 10 m do início da `pmc_linha4000_a` (antes `pos_linha_4000_criar_loco`, na `intervalo4000_b`),
  para não ocupar a passagem da Bracell. A 4000 (a+b+c ≈ 1.132 m) comporta 2 locos + ~62 vagões; acima disso o trem passa do fim da `linha4000_c`.

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
- Saída das DVs (ALT-143): antes do `ir_saida` (move-to até `circ_pos_fim_placa_2`), o Seize `ocupar_val_via2_saida` licencia a `val_via2`, porque o caminho passa pelo AMV da saída do ISN (`railwaySwitch134`). O `liberar_faixas_circulacao`, depois do `trainExit`, solta a faixa.
- Precisa informar o espaço livre, em vagões, por ferrovia.
  O GestorDeAlivios usa esse valor antes de aliviar o Tgrao (precisa ser mais de 40).

## GestorDeChegadas

- **`log_analise_detalhada`** (boolean, padrão `false`): liga os logs `pedido_criado` e `aguardando` (`registrar_motivo`) de cada pedido. `decisao`, `inject` e `entrou_trilho` saem sempre.

Em desenvolvimento (ALT-084). Parte ligada ao ISN:

- `decidir_destino`: terminal pode receber → circulação; senão `gestorDeISN.pode_estacionar()` → ISN; senão espera na fila.
- `entrada_livre`: só coloca trem com `faixa_isn_a` LIVRE e `isn_linha01_a` vazia.
- `receber_trem`: liga trem e pedido; destino ISN grava `gestorDeISN.lote_linha02` e `trem.estacionarISN = true`; entrega em `gestorDeISN.chegada`.
- Os Exits `paraGestorCirculacao_chegadaCarregados` dos terminais (ADM, CoperXI, CoperXXI, MoegaoMar, MoegaoTerra, TES, Tgrao) chamam `receber_trem`.

Saída (ALT-213, pasta na ALT-214): `simulacao/output/trens_criados/trens_criados.csv` (`trens_criados_rep<n>.csv` nas replicações).

Vagões (ALT-221): `marcar_vagoes` (no `receber_trem`) copia o terminal, o id e o prefixo do trem para cada vagão e grava `posicao_absoluta`/`posicao_relativa`. No `trainExit` do GestorDeDVs (fim do circuito), `registrar_vagoes_saida` marca `hora_descarte_h` e grava uma linha por vagão em `simulacao/output/wagons/vagoes.csv` (`vagoes_rep<n>.csv` nas replicações). No `exit4` do GestorDeDVs, o log `vagoes_exit4` mostra quantos vagões saíram e o acumulado (`vagoes_no_exit4`): eles não são destruídos. A collection `vagoes_ativos` (GestorDeChegadas) guarda os vagões vivos: entra no `marcar_vagoes`, sai no `registrar_vagoes_saida`; o log mostra `ativos=<n>`.

- `iniciar_csv_trens` (no `iniciar`): sobrescreve o arquivo só com o cabeçalho `pedido;prefixo;trem_id;terminal;ferrovia;segmento;data_pedido;data_entrada;warm_up`.
- `registrar_trem_csv` (no `receber_trem`): uma linha por trem que nasceu com pedido, gravada na hora (append). `data_pedido` = horário programado do pedido; `data_entrada` = trem no trilho; datas `dd/MM/yyyy HH:mm` a partir de `data_inicio`; `segmento` do 1º vagão; `warm_up` = entrada antes de `warmup_h`.
- Pedidos ainda na fila no fim da rodada não entram no `trens_criados.csv`: vão para o arquivo de pedidos não atendidos (abaixo).

Pedidos não atendidos (ALT-224): `simulacao/output/pedidos_nao_atendidos/pedidos_nao_atendidos.csv` (`pedidos_nao_atendidos_rep<n>.csv` nas replicações).

- `iniciar_csv_pedidos` (no `iniciar`): sobrescreve o arquivo só com o cabeçalho `pedido;terminal;ferrovia;manual;data_pedido;espera_ate_fim_h;ultimo_motivo;warm_up`.
- `registrar_fila_csv` (no último tique do `ev_relogio` antes de `duracao_h`, uma vez por rodada com `fila_registrada`): uma linha por pedido que não virou trem, o `pedido_atual` (motivo `injetado, trem não nasceu`) e depois a `fila`. `espera_ate_fim_h` = fim da rodada − hora do pedido; `;` do `ultimo_motivo` vira ` |`; `warm_up` = pedido antes de `warmup_h`.
- Rodada que cai ou é parada antes do fim deixa o arquivo só com o cabeçalho.

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
