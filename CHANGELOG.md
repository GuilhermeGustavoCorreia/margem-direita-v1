# CHANGELOG — MargemDireitaModel1

Convenção: cada alteração recebe um código `ALT-NNN-descricao`, vira um commit com o mesmo nome no `main` e é registrada aqui.

---

## ALT-000 — baseline
- **Data:** 2026-10-10
- **O que:** versão inicial do modelo versionada no GitHub (`margem-direita-v1`), antes de qualquer alteração.

## ALT-001 — remover-solicitacoes
- **Data:** 2026-10-10
- **Status:** concluída (excluídos no AnyLogic, compilado e commitado)
- **O que:** remover os tipos de agente `solicitacao_alivio` e `solicitacao_faixas` (pasta `agentes-auxiliares/gestor-de-alivios`).
- **Análise de dependências:** nenhuma referência aos dois agentes, nem por nome nem por Id, nos outros 180 arquivos do modelo (Main, gestores, terminais, classes e `.alpx`). Remoção segura.
- **Arquivos removidos:**
  - `_alp/Agents/solicitacao_alivio/` (AOC, Variables)
  - `_alp/Agents/solicitacao_faixas/` (AOC, Variables, Levels)
- **Commit:** `ALT-001 remover agentes solicitacao_alivio e solicitacao_faixas`

## ALT-002 — correcoes-coperxi
- **Data:** 2026-10-10
- **O que:** correções do terminal CoperXI (descarga x alívio, alívio do gestor, contador de locos, helper VLI e encostes). Um único commit, com os subitens abaixo.
- **Commit:** `ALT-002 correcoes CoperXI`

### ALT-002.1 — coperxi-pausar-descarga-no-alivio
- **Nos comentários do código:** `ALT-002`
- **Data:** 2026-10-10
- **Status:** aplicada e validada (2º trem descarregou sem colisão)
- **Problema:** erro `The car being added on the track 'pstSaidaCoperXI_terra_a' intersects with another car` na descarga do 2º trem. O excedente do alívio (10 vgs) voltava para a linha terra_a depois que a descarga já tinha colocado 3 vazios novos nela (10 + 3 > capacidade).
- **Causa raiz:** `fazendoAlivio` nunca era setado no CoperXI, então `pegarVagaoPraDescarga` e `aguardaAlivio` não pausavam a descarga durante o alívio. Agravante: o excedente voltava ao `wait_bloco*` sem atualizar `_loteDeVazios_*`.
- **Alterações (CoperXI):**
  - `takeAlivio()`: `fazendoAlivio = true; aguardaAlivio.block();`
  - `deixarTerminal.onEnter`: `fazendoAlivio = false;`
  - `mnbVazios_liberar19/17/21/23.onExit`: `_loteDeVazios_LinhaMar_a / Mar_b / Terra_a / Terra_b = (Trem) agent;`
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXI/Code/Functions.java`

### ALT-002.2 — gestor-alivio-aguardar-loco
- **Nos comentários do código:** `ALT-003`
- **Data:** 2026-10-10
- **Status:** aplicada (reaplicada após o AnyLogic sobrescrever), aguardando validação
- **Problema:** no 2º trem o CoperXI chegava em overload e o alívio nunca acontecia.
- **Causa raiz:** em `fazer_procedimento_de_alivio_pst`, o terminal era removido de `terminais_pst` (`colocar_terminal_em_alivio_pst`) antes de verificar se havia locomotiva. No fim do 1º trem, a regra de "últimos vazios" pediu alívio dos 7 vazios restantes 16 s depois do alívio anterior sair com a única loco rumo → `locos no wait=0` → alívio não iniciou e o terminal nunca voltou para a lista.
- **Alteração (GestorDeAlivios):** `fazer_procedimento_de_alivio_pst` verifica o wait `locomotivas<Ferrovia>` antes de colocar o terminal em alívio; sem loco, o terminal permanece na análise e o alívio dispara quando a loco voltar.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`
- **Observação de modelagem:** há 1 loco rumo para alívio; os últimos vazios esperam ela ir às DVs e voltar.

### ALT-002.3 — contador-locos-pelo-wait
- **Nos comentários do código:** `ALT-004`
- **Data:** 2026-10-10
- **Status:** aplicada, aguardando validação
- **Problema:** o contador de locos na tela (`rp_locoRumo.capacity - rp_locoRumo.busy()`) não caía quando a loco saía para alívio.
- **Causa raiz:** `locomotivasRumo.onEnter` fazia `rp_locoRumo.set_capacity(size()+1)` a cada retorno da loco (capacidade só crescia) e o `liberar_faixas_pst` devolvia a unidade quando o trem saía do terminal, com a loco ainda viajando.
- **Alterações (GestorDeLocomotivas):**
  - Textos da tela passam a mostrar `locomotivasRumo.size()`, `locomotivasMRS.size()`, `locomotivasVLI.size()`.
  - `locomotivasRumo.onEnter`: `set_capacity` comentado.
  - `rp_locoRumo` capacidade fixa 10 (igual MRS/VLI), para o `usar_recurso_loco_pst` do GestorDeAlivios nunca bloquear; o pool deixa de ser usado como contador.
- **Arquivos:** `_alp/Agents/GestorDeLocomotivas/EmbeddedObjects.xml`, `_alp/Agents/GestorDeLocomotivas/Levels/Level.level.xml`

### ALT-002.4 — cortar-helper-antes-dos-encostes
- **Nos comentários do código:** `ALT-005`
- **Data:** 2026-10-10
- **Status:** validada (VLI cortou o helper e reacoplou)
- **Problema:** trem VLI chega com loco na cabeça e helper na posição 36; com os desacoplamentos de quantidade fixa, o helper terminava dentro do `_loteDeCarregados_MoegaMeio` e ia para a descarga.
- **Solução:** cortar o helper na linha verde, logo após `liberar_faixas_circulacao` e antes do `deixar27vgs`:
  `semHelper` (SelectOutput) → `para_cortarHelper` → `cortarHelper` → `separarTraseira_helper` (deixa os carros atrás do helper parados) → `tirarHelper` (1 carro) → helper vai para `devolverHelper` (guardar_loco) → `reencostarTraseira` (ré até o início da `pst_silosMuro_a`, máx. 40 m — corrigido: o destino `pos_PontaDaPraia` dava "no straight route") → `reacoplarTraseira` (TrainCouple) → `deixar27vgs`.
- **Nova função:** `Trem.getCarrosAtrasDoHelper()` — nº de carros atrás da 1ª loco fora do bloco da cabeça; -1 se não houver.
- **Limitações:** trata 1 helper; helper como último carro (0 carros atrás) não é cortado aqui.
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXI/Connectors.xml`, `_alp/Agents/Trem/Code/Functions.xml`, `_alp/Agents/Trem/Code/Functions.java`

### ALT-002.5 — encostes-contam-vagoes
- **Nos comentários do código:** `ALT-006`
- **Data:** 2026-10-10
- **Status:** validada (Rumo/MRS com 2 locos na cabeça e VLI)
- **Problema:** após a ALT-005 o VLI (1 loco + 81 vgs) não devolvia a loco da cabeça e a descarga não começava. A sequência fixa 27+4+10+4+10+10+10+4+3 tira 82 carros; com 81 vagões o `tira3vgs` tentava levar a loco junto, o trem não chegava ao `mnbCarregados_liberar` e o `rp_manobrando_carregados` ficava preso.
- **Alteração (CoperXI):** `nCars` dos 12 blocos `tira*vgs` (encoste e rechego) passa de N fixo para `Math.min(N, ((Trem) train).getNumeroDeVagoes())`, com comentário dos casos Rumo/MRS (2 locos na cabeça + 81 vgs), VLI (1 loco + 81 vgs) e original (1 loco + 82 vgs).
- **Efeito:** com menos de 82 vagões o último lote (linha bomba) sai menor; as locos da cabeça sempre sobram para o `devolverLocomotiva`.
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`

## ALT-003 — correcoes-gestor-de-circulacao
- **Data:** 2026-10-10
- **O que:** rotas dos carregados no GestorDeCirculacao (CoperXXI, T39 e Tgrao) e decisões reorganizadas como no diagrama "Circulação dos carregados" do documento `GestorDeCirculacao.html`. Um único commit, com os subitens abaixo.
- **Commit:** `ALT-003 correcoes GestorDeCirculacao`

### ALT-003.1 — circulacao-coperxxi
- **Nos comentários do código:** `ALT-003.1`
- **Data:** 2026-10-10
- **Status:** validada (CoperXXI testado pela circulação)
- **O que:** levar o trem do CoperXXI pelo GestorDeCirculacao até a pêra: `via3_intermar` → `via3_a` → `via2_c` → `track_entrada_pera` → `coperXXI.comecar_encoste`.
- **Problema:** a circulação não conhecia o coperXXI (o trem caía no `continuar_via3`, um Exit sem código, e ficava parado) e tratava o costaLeite como terminal da pêra.
- **Alterações (GestorDeCirculacao):**
  - `acessar_via_2`: inclui `coperXXI`. `acessar_pera` e `acessar_coperXI`: pêra = `coperXXI`, `moegaoMar`, `moegaoTerra` (sai o `costaLeite`). As condições passam a usar `.equals()`.
  - Nova faixa `faixa_entrada_pera` (`rp_entrada_pera`, track `main().track_entrada_pera`, Enter `circular_entrada_pera`, polilinha `pl_entrada_pera`), incluída na coleção `faixas`.
  - `para_terminal_na_pera` (antes vazio): atual = via2_c, próxima = entrada_pera.
  - `para_CoperXI` (saída "pêra" do `acessar_coperXI`): atual = entrada_pera, próxima = null.
  - Novos blocos `circular_entrada_pera` → `ir_entrada_pera` (track_entrada_pera, offset 10) → `para_coperXXI` (`main().coperXXI.comecar_encoste.take(agent)`).
- **Alteração (CoperXXI):** novo Release `liberar_faixas_circulacao` (`liberar_faixas_ocupadas`) entre `analisaRechego` e `precisaRechego`. A cada lote, libera as faixas de circulação que o trem já desocupou.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/GestorDeCirculacao/Levels/Level.level.xml`, `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [ ] Trem Rumo, MRS e VLI chegam ao `_coper_xii_encoste` sem "no straight route".
  - [ ] A via2_c e a entrada da pêra ficam brancas no painel depois que o trem sai delas.
  - [ ] Posição da `pl_entrada_pera` no painel (desenhada provisoriamente no fim da via2_c).
- **Fora do escopo:** o costaLeite continua no `acessar_via_2`, mas sem rota definida (cai no laço do `continuar_via2_e`).

### ALT-003.2 — circulacao-t39
- **Nos comentários do código:** `ALT-003.2`
- **Data:** 2026-10-10
- **Status:** validada (trem de teste do T39 chegou ao encoste pela circulação)
- **O que:** levar o trem do T39 pelo GestorDeCirculacao: `via3_intermar` → `via3_a` → `via3_b` → `via3_c` → `pst_silosMuro_a` → `pst_silosMuro_b` → `pst_silosMuro_c` → `t39.encoste_linha1000`.
- **Regra:** igual às outras rotas, o trem licencia só uma faixa à frente (`faixa_atual` / `proxima_faixa`). Cada faixa tem seu `circular_*` → `ir_*` → Exit, e o Exit define o próximo par. No fim da `pst_silosMuro_c` um SelectOutput pergunta se o destino é o t39.
  - Correção da 1ª versão: a rota inteira era licenciada de uma vez num Seize (`licenciar_rota_t39`), e o trem ia em `ROUTE_AUTO` direto ao encoste do T39. O AnyLogic escolheu o caminho pela via2_c. O Seize foi removido.
- **Alterações (GestorDeCirculacao):**
  - `continuar_via3` (antes vazio): atual = via3_a, próxima = null.
  - `acessar_t39` (SelectOutput, entre `acessar_pera.outF` e `continuar_via2`) → `para_via3_b`: atual = via3_b, próxima = via3_c.
  - `ir_via3_b` → `para_via3_c` (antes ia para o `destino_pera_ou_coperXI1`, que ficou sem entrada): atual = via3_c, próxima = silosMuro_a.
  - Novos `circular_via3_c` / `ir_via3_c` → `para_silosMuro_a`; `circular_silosMuro_a` / `ir_silosMuro_a` → `para_silosMuro_b`; `circular_silosMuro_b` / `ir_silosMuro_b` → `para_silosMuro_c` (próxima = null); `circular_silosMuro_c` / `ir_silosMuro_c` → `destino_t39`.
  - `destino_t39`: sim → `para_t39` (`main().t39.encoste_linha1000`); não → `sem_rota_silosMuro_c` (`error(...)`, para não parar o trem em silêncio).
  - Novas faixas `faixa_via3_c` e `faixa_silosMuro_a/b/c` (recursos `rp_*`, Enter `circular_*` e polilinhas `pl_*` provisórias no painel), incluídas em `faixas`.
- **Alterações (T39):**
  - `tremTesteSource`: o destino era `coperXI` e passa a ser `main().t39`. A posição inicial muda de `t39_teste` (na linha 1000) para `circ_pos_inicio_placa_2`, porque nascendo dentro do T39 dava "no straight route" até a via3_intermar. `para_encoste_linha1000` passa a entrar pelo `gestorDeCirculacao.chegada_carregados`.
  - Novo Release `liberar_faixas_circulacao` entre `encosta_L1K1_fim` e `delay1`. Libera as faixas que a cauda ainda ocupava na entrega, e repete a cada lote.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/GestorDeCirculacao/Levels/Level.level.xml`, `_alp/Agents/T39/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [x] Trem de teste do T39 percorre via3_b → via3_c → silosMuro a/b/c e chega ao `t39_encoste`.
  - [ ] As faixas ficam brancas depois que a cauda passa.
  - [ ] Posição das polilinhas novas no painel.
- **Pendência:** a `pst_silosMuro_a` é usada pelo CoperXI no corte do helper VLI (ALT-002.4) sem licenciamento. Um trem do T39 e uma manobra VLI do CoperXI ao mesmo tempo podem colidir.

### ALT-003.3 — decisoes-da-circulacao-como-no-documento
- **Nos comentários do código:** `ALT-003.3`
- **Data:** 2026-10-10
- **Status:** validada (CoperXI, CoperXXI, T39 e Tgrao rodaram com as novas decisões)
- **O que:** reorganizar as decisões do GestorDeCirculacao para seguir o diagrama "Circulação dos carregados" do documento:
  - fim da `via3_intermar`: sem pergunta, vai para a via3_a;
  - fim da `via3_a`: ◇ é coperXI ou pêra? sim → via2_c, não → via3_b;
  - fim da `via2_c`: ◇ é pêra? sim → track_entrada_pera, não → via2_d;
  - fim da `pst_silosMuro_c`: ◇ é t39? (já existia).
- **Licenciamento:** continua a regra da faixa atual + a seguinte. Como a `proxima_faixa` precisa ser conhecida ao entrar na faixa, os Exits escolhem a seguinte pelo destino:
  - `para_via3_a` (fim da via3_intermar): atual = via3_a, próxima = via2_c se coperXI/pêra, senão via3_b;
  - `para_via2_c` (fim da via3_a, sim): atual = via2_c, próxima = entrada_pera se pêra, senão via2_d. O trem do CoperXI reserva via2_c e via2_d no fim da via3_a;
  - `para_via3_b` (fim da via3_a, não): atual = via3_b, próxima = via3_c;
  - `para_entrada_pera` / `continuar_via2_d` (fim da via2_c): atual = entrada_pera / via2_d, próxima = null.
- **Blocos (GestorDeCirculacao):**
  - Removidos: `acessar_via_2`, `para_via2`, `acessar_t39`, `continuar_via2`.
  - Renomeados: `continuar_via3` → `para_via3_a`; `acessar_pera` → `e_coperXI_ou_pera`; `para_terminal_na_pera` → `para_via2_c`; `acessar_coperXI` → `e_pera`; `para_CoperXI` → `para_entrada_pera`.
- **Efeito colateral:** o costaLeite, sem rota, cai na via3_b e para com `error` no fim da pst_silosMuro_c (antes ficava preso no laço da via2).
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [x] CoperXI, CoperXXI e T39 chegam ao encoste.
  - [ ] No fim da via3_a, o CoperXI fica com via2_c e via2_d licenciadas (verdes).

### ALT-003.4 — circulacao-tgrao
- **Nos comentários do código:** `ALT-003.4`
- **Data:** 2026-10-10
- **Status:** validada (trem de teste do Tgrao chegou ao encoste pela circulação)
- **O que:** levar o trem do Tgrao pelo GestorDeCirculacao: `via3_intermar` → `via3_a` → `via3_b` → `via3_c` → `pst_silosMuro_a` → (railwayTrack114) → `pst_marginal_c` → `tgrao.comecar_encoste`.
- **Alterações (GestorDeCirculacao):**
  - `para_silosMuro_a` (fim da via3_c): atual = silosMuro_a, próxima = marginal_c se tgrao, senão silosMuro_b.
  - Novo ◇ `e_tgrao` no fim da `pst_silosMuro_a`: sim → `para_marginal_c` (atual = marginal_c, próxima = null); não → `para_silosMuro_b` (como antes).
  - `ir_marginal_c` (já existia) passa a sair para o novo `para_tgrao` → `main().tgrao.comecar_encoste`. Antes ia para o `continuar2`, que ficou sem entrada.
  - `faixa_marginal_c` incluída na coleção `faixas`, para ser liberada pelo laço. Não interfere no bloqueio da marginal pelo CoperXI (`pst_marginal_bloquear`), porque a liberação só mexe em faixa OCUPADA do próprio trem. Com a marginal bloqueada, o Tgrao espera no fim da silosMuro_a.
- **Alterações (Tgrao):**
  - Enter `encoste` renomeado para `comecar_encoste` (e `paraEncoste`).
  - `tremTesteSource`: nasce em `circ_pos_inicio_placa_2` (antes `ponto_teste_tgrao`), destino `main().tgrao`, e entra pelo novo `paraGestorCirculacao_chegadaCarregados` → `gestorDeCirculacao.chegada_carregados`. O `irAtéTerminal` ficou sem entrada.
  - Novo Release `liberar_faixas_circulacao` entre `colocarLinhaMar` e `encostar_linhaMar`. Roda a cada lote e libera as faixas de circulação que o trem já desocupou.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/Tgrao/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [x] Trem de teste do Tgrao chega ao encoste pela circulação.
  - [ ] As faixas da rota ficam brancas depois que a cauda passa.
- **Pendência:** a `railwayTrack114` (travessão silosMuro_a ↔ marginal) não tem faixa.

## ALT-004 — moegaomar + pendencias-coperxi
- **Data:** 2026-10-10
- **Status:** em andamento
- **Escopo:** correções do MoegaoMar + pendências herdadas da ALT-002 (CoperXI).

### Pendências herdadas da ALT-002 (CoperXI)
- [ ] **1º vagão invisível:** com a linha de vazios vazia, o 1º vagão vai para `acumular*.in1` (não para `wait_bloco*`); alívio e contagem só o enxergam quando chega o 2º.
- [ ] **Alívio sempre começa pela linha "a":** `pega_linhaMar_a` / `pega_linhaTerra_a` não checam se a linha tem lote; porta `out` delas desconectada.
- [ ] **Validar 002.2** (gestor aguarda loco) **e 002.3** (contador de locos).
- [ ] **Remover traces de diagnóstico:** CoperXI (`AouB1.onExitTrue/onExitFalse`, `retornar_linhaTerra_a1/b.onEnter`, `semHelper.onExitFalse`, `reacoplarTraseira.onExit`) e GestorDeAlivios (`[diag_pst]`).
- [ ] **Comentários no código** com números antigos (`ALT-003` a `ALT-006`) → renomear para `ALT-002.x`.

### MoegaoMar
- [ ] (a definir)

## ALT-005 — tgrao-lotes-e-loco
- **Nos comentários do código:** `ALT-005`
- **Data:** 2026-10-10
- **Status:** aplicada, aguardando validação
- **O que:** três correções no Tgrao.
  - **Loco ao gestor:** depois do 2º lote, a loco do trem carregado ia para o `trainDispose1` e sumia. O bloco virou o Exit `paraDevolverLocomotiva` → `main().gestorDeLocomotivas.devolverLocomotiva`.
  - **Lotes de carregados:** depois da descarga, os vagões saem do lote de carregados. `paraAcoplarAlivioMar` zera `lote_linhaMar`; `parAacoplarAlivioMoega` zera `lote_linhaTerra`.
  - **Lotes de vazios:** zerados após o alívio, quando o lote é engatado. `paraProximaEtapa` já zerava `loteVazios_linhaMar`; `paraProximaEtapa1` zerava `lote_linhaTerra` e passa a zerar `loteVazios_linhaMoega`. Isso evita que o ◇ `temLoteVzs_linhaMoega` mande o alívio de volta para a moega.
  - **Overload:** `getOverloadVazios()` passa a contar os vazios (`_getVagoesVazios()` = `loteVazios_linhaMar` + `loteVazios_linhaMoega`). Antes contava os carregados; com os lotes zerados, nunca dispararia o alívio.
- **Arquivos:** `_alp/Agents/Tgrao/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/Tgrao/Code/Functions.java`
- **A validar:**
  - [ ] A loco do trem de teste chega ao GestorDeLocomotivas (contador de locos sobe).
  - [ ] Com os dois lotes descarregados (40 vazios), o overload do Tgrao vai a 1.1.
- **Efeito a observar:** o ◇ `aindaCabeNa_linhaMar` olha só `lote_linhaMar`. Com o lote zerado após a descarga, um 2º trem enxerga a linha mar livre mesmo com vazios nela.
- **Fora do escopo:** `takeAlivio(Trem)` → `iniciar_alivio` sem saída; `paraSairDoTerminal` comentado.

## ALT-006 — gestor-alivio-loco-por-terminal
- **Nos comentários do código:** sem marcação (nomes novos falam por si)
- **Data:** 2026-10-11
- **Status:** aplicada, aguardando compilação e validação
- **O que:** o tipo e a quantidade de locos do alívio PST passam a vir do terminal. Tgrao usa 2 locos de manobra (FIPS PST) criadas em `pos_locoManobra_PST`; os demais seguem com 1 loco da ferrovia em atendimento.
  - **Interface `ITerminalMoega`:** `getPrimeiroAlivioFaixas()` → `get_faixas_primeiro_alivio()`; novos `get_tipo_loco_alivio()` (`"manobra"` | `"ferrovia"`) e `get_quantidade_loco_alivio()`. Implementados em Bracell, CoperXI, CoperXXI, CostaLeite, MoegaoTerra, T39 (ferrovia, 1) e Tgrao (manobra, 2).
  - **Faixa de criação fora de `get_faixas_primeiro_alivio()`:** a lista passa a ter só as faixas seguintes até o terminal. CoperXI, Bracell, CostaLeite e MoegaoTerra (criação em `pos_locoAlivio_PST`, faixa `via1_intermar_d`) retornam lista vazia. Tgrao monta `{ via1_intermar_d }` na hora da chamada — a variável `primeiro_alivio_faixas` era inicializada antes do `gestorDeCirculacao` existir e ficava com `null` (NullPointerException em `Alivio.getConjuntoRecursos_faixas`).
  - **`Alivio`:** novos `tipo_loco`, `faixa_criacao`, `ehLocoManobra()` e `getConjuntoRecursos_faixa_criacao()`; `getConjuntoRecursos_faixas()` ignora a faixa de criação (já apreendida) e acusa faixa `null` com mensagem clara.
  - **GestorDeAlivios — funções:** `fazer_procedimento_de_alivio_pst` checa `rp_locoFips_pst.idle() >= qtd` (manobra) ou o wait da ferrovia; `colocar_terminal_em_alivio_pst` define tipo, quantidade, `pos_criar_locomotiva` (`pos_locoManobra_PST` ou `pos_locoAlivio_PST`) e `faixa_criacao`; `chamar_locomotiva_de_alivio_pst` injeta `loco_manobra_alivio_pst` quando é manobra; `get_recurso_locomotiva_pst` devolve `rp_locoFips_pst` para manobra; nova `get_faixa_da_posicao(pos)`.
  - **GestorDeAlivios — fluxograma:** `loco_alivio_pst` (loco da ferrovia, vinda do wait) / `loco_manobra_alivio_pst` (TrainSource novo, `putInRailYard = false`: cria as locos como agente lógico) → `seize_faixa_criacao_pst` (novo) → `bloquear_faixas_pst` → `usar_recurso_loco_pst` (quantidade = `get_quantidade_loco_alivio`) → `seize_alivio_pst` → `trainEnter1` (coloca na via em `pos_criar_locomotiva`, para os dois tipos) → `para_comecar_alivio_pst`. Licencia antes de entrar na via.
  - **Pool da loco FIPS pelo pátio:** nova `get_rp_loco_fips(patio)` (`"pst"`/`"pmc"`/`"pcx"`, minúsculo), usada em `get_recurso_locomotiva_pst` e na checagem de `fazer_procedimento_de_alivio_pst`; o TrainSource grava `patio = alivio_em_andamento_pst.patio`. Tgrao: `getPatio()` `"PMC"` → `"pst"`.
  - **GestorDeCirculacao:** a coleção `faixas` passa a ter todas as 36 faixas do gestor (faltavam marginal_a/b, linha1000_a–c, linha2000_a–e, linha3000_a–e, linha4000_a–c). Necessário para `get_faixa_da_posicao` achar `faixa_marginal_a` (`pos_locoManobra_PST`). Efeito colateral: `liberar_faixas_ocupadas` passa a considerar essas faixas.
  - **Rota licenciada:** `ir_entrada_terminal_pst` passa de `ROUTE_AUTO` para `ROUTE_LIST_OF_TRACKS` ("A given list of tracks") com `alivio_em_andamento_pst.getRotaLicenciada()` (novo em `Alivio`): faixa de criação + `get_faixas_primeiro_alivio()` + track do destino. A loco segue só o caminho licenciado e vira os AMVs pela lista (antes pegava outro ramo no AMV). `get_faixas_primeiro_alivio()` deve estar em ordem de percurso, do Valongo no sentido PCX.
  - **Tgrao — caminho da loco de manobra:** `primeiro_alivio_faixas = { faixa_marginal_b, faixa_marginal_c }`; `pos_circ_alivio_tgrao` movida para a `pst_marginal_c` (antes na `pst_silosMuro_a`, linha paralela, sem ligação direta). Rota: `marginal_a → marginal_b → marginal_c`.
  - **Tgrao:** `get_quantidade_vazios()` retornava 0; passa a retornar `_getVagoesVazios()`.
- **Arquivos:** `_alp/Classes/Class.{ITerminalMoega,Alivio}.java`, `_alp/Agents/GestorDeAlivios/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.{java,xml}`, `_alp/Agents/GestorDeCirculacao/Variables.xml`, `_alp/Agents/{Bracell,CoperXI,CoperXXI,CostaLeite,MoegaoTerra,T39,Tgrao}/Code/Functions.{java,xml}`
- **A validar:**
  - [ ] Compila (F7).
  - [ ] T39 segue OK (linha1000_b/c usam `ocupar(trem)` e agora entram em `liberar_faixas_ocupadas`).
  - [ ] CoperXI segue aliviando com loco Rumo (fluxo ferrovia).
  - [ ] Tgrao: 2 locos de manobra aparecem em `pos_locoManobra_PST`, a faixa de criação fica ocupada e o alívio segue para o terminal.
- **Pendente:** Bracell `get_pos_alivio()` também retorna `pos_circ_alivio_tgrao` (cópia do Tgrao); no Tgrao uma das 2 locos de manobra é devolvida (a analisar); volta das locos de manobra após o alívio; `takeAlivio(Trem)` → `iniciar_alivio` do Tgrao sem saída (ALT-005).

## ALT-007 — tgrao-liberar-loco-b
- **Nos comentários do código:** `ALT-007`
- **Data:** 2026-10-11
- **Status:** aplicada, aguardando validação
- **Problema:** no alívio do Tgrao, a loco B ficava presa no Hold `aguardaPrimeiraManobra` (inicia bloqueado) e nunca ia acoplar nos vazios.
- **Causa:** o `aguardaPrimeiraManobra.unblock()` ficava no Exit `paraDevolverLocomotiva_a`, que foi substituído pelo bloco `devolverLocomotiva1` (DevolverLocomotiva). Nenhum bloco desbloqueava mais o Hold.
- **Alteração:** `paraAcoplarLoco_b.onExit` passa a chamar `aguardaPrimeiraManobra.unblock()` antes de `acoplarLoco_b.take(agent)`. O `trainMoveTo.onEnter` continua bloqueando o Hold de novo.
- **Arquivos:** `_alp/Agents/Tgrao/EmbeddedObjects.xml`
- **A validar:**
  - [ ] Depois do corte da loco A, a loco B vai até `_tgrao_mnb_loco_b` e acopla nos 40 vazios.
- **Fora do escopo:** `paraSairDoTerminal` com código comentado (o trem do alívio para depois do `trainCouple`).

## ALT-008 — tgrao-faixas-7000-e-saida-do-alivio
- **Nos comentários do código:** `ALT-008`
- **Data:** 2026-10-11
- **Status:** aplicada, aguardando validação
- **Faixas novas (GestorDeCirculacao):** `faixa_linha7000_a..d`, com pools `rp_pmc_linha7000_a..d` (cópias do `rp_pmc_linha4000_a`) e linhas `pl_pmc_linha7000_a..d` no painel, numa linha nova em y = 40 (**posição provisória**, ajustar). Sem `enter`. Incluídas na coleção `faixas`.
- **Encoste (Tgrao):**
  - Seize `ocupar_7000_abcd` antes do `colocarTrem_L7`: ocupa e bloqueia 7000_a, b, c, d.
  - Release `liberar_7000_d` depois do `encostar_linhaMar`.
  - Release `liberar_7000_abc` depois do `encostar_Moega`.
- **Alívio (Tgrao):**
  - Seize `ocupar_7000_d_alivio` antes do `voltarPosInicial1`.
  - Release `passar_7000_d_vazios` depois do `trainDecouple` (vazios) e Seize `ocupar_7000_d_locoB` antes do `trainMoveTo`: a 7000_d passa do trem dos vazios para a loco B. Motivo: no `trainCouple` o trem da entrada 2 (vazios) é removido e os recursos dele ficariam presos.
  - Release `liberar_7000_d_alivio` depois do `voltarPosInicial3`, antes do `paraSairDoTerminal` (take para `gestorDeAlivios.retornar_alivio`).
- **Erro corrigido:** `voltarPosInicial3.colocarCaudaNoPonto: There is no straight route from pst_marginal_c, offset 97 to railwayTrack197, offset 11`. O trem anda para a silosMuro_a (lado Valongo), mas a cauda era empurrada para a Ponta da Praia. `voltarPosInicial3.sentidoPontaDaPraia = false`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/GestorDeCirculacao/Levels/Level.level.xml`, `_alp/Agents/Tgrao/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [ ] Painel: 7000_a..d magenta na chegada; d branca após o encoste na linha mar; a, b, c brancas após o encoste na moega.
  - [ ] Alívio: 7000_d magenta do `voltarPosInicial1` até a saída; branca antes do retorno ao gestor.
  - [ ] `voltarPosInicial3` sem "no straight route".
  - [ ] Posição das linhas `pl_pmc_linha7000_*` no painel.

## ALT-009 — tgrao-faixas-do-alivio
- **Nos comentários do código:** `ALT-009`
- **Data:** 2026-10-11
- **Status:** aplicada, aguardando validação
- **Alívio usa a 7000_a..d inteira** (antes só a 7000_d). Blocos renomeados:
  - `ocupar_7000_abcd_alivio` (antes `ocupar_7000_d_alivio`), antes do `voltarPosInicial1`;
  - `passar_7000_abcd_vazios` / `ocupar_7000_abcd_locoB` (passagem dos vazios para a loco B);
  - `liberar_7000_abcd_alivio`, antes do `paraSairDoTerminal`.
- **Saída do alívio:** novo Seize `ocupar_saida_alivio` antes do `voltarPosInicial3`: ocupa `pmc_linha6000_a`, `pst_marginal_c` e `pst_silosMuro_a` com `ocupar()` (estado OCUPADA). Elas são soltas pelo laço do GestorDeCirculacao (`liberar_faixas_ocupadas`) conforme o trem circula para o Valongo.
- **Faixa nova (GestorDeCirculacao):** `faixa_linha6000_a`, pool `rp_pmc_linha6000_a`, linha `pl_pmc_linha6000_a` no painel (**posição provisória**, perto da marginal). Incluída na coleção `faixas`.
- **`voltarPosInicial3`:** destino `main().pos_saida_tgrao` (antes `pos_locoAlivio_PMC`).
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/GestorDeCirculacao/Levels/Level.level.xml`, `_alp/Agents/Tgrao/{EmbeddedObjects,Connectors}.xml`
- **Atenção:** o `liberar_faixas_pst` do GestorDeAlivios (logo no `retornar_alivio`) está com o modo padrão do Release (todos os recursos). Ele devolve os pools da 6000_a, marginal_c e silosMuro_a assim que o trem chega ao gestor; as faixas continuam vermelhas no painel até o laço liberá-las, mas o pool fica livre antes de o trem sair das tracks.
- **A validar:**
  - [ ] Painel: 7000_a..d magenta do `voltarPosInicial1` até a saída.
  - [ ] 6000_a, marginal_c e silosMuro_a vermelhas na saída e brancas conforme o trem anda para o Valongo.
  - [ ] Posição da `pl_pmc_linha6000_a` no painel.

## ALT-010 — diag-liberar-faixas-usadas
- **Data:** 2026-10-11
- **Status:** diagnóstico temporário (remover os traces `[diag_liberar]` depois de achar a causa)
- **Problema:** `root.gestorDeCirculacao.liberar_faixas_usadas: The agent does not possess the resource(s) it wants to release`. A função `liberar_faixas_ocupadas` escolhe o que liberar pelo estado da `Faixa` (`OCUPADA` + `idTremLicenciado`), sem conferir se o trem possui a unidade do pool.
- **Hipóteses:** (1) faixas bloqueadas pelo `pst_marginal_bloquear` do CoperXI viram `OCUPADA` no `ocupar()` e são liberadas por dois caminhos; (2) IDs mágicos `9999`/`999999` (`ocupar_agente`, `bloquear(null)`) coincidindo com o ID de um Trem; (3) unidade já liberada num Release de terminal (CoperXI/Tgrao) sem o estado da faixa acompanhar.
- **O que:** traces em `GestorDeCirculacao.liberar_faixas_ocupadas` — a cada chamada, lista os pools que o trem possui e, para cada faixa candidata, `vazia` e `temUnidade`; marca `<-- VAI DAR ERRO` quando vai liberar sem possuir. Comportamento inalterado.
- **Arquivo:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`

## ALT-011 — log-em-arquivo
- **Data:** 2026-10-12
- **Status:** aplicada, aguardando validação
- **Problema:** para diagnosticar, o Claude dependia de prints ou de colar o console do AnyLogic.
- **O que:** o console (`traceln`, `System.out` e `System.err`, inclusive stack traces de exceções) passa a ser espelhado em `logs/sim.log`, sobrescrito a cada execução. O console do AnyLogic continua igual.
- **Alterações (Main):**
  - Função nova `configurar_log_em_arquivo()` (void).
  - `On startup` do Main: `configurar_log_em_arquivo();`
  - Os streams originais ficam em `System.getProperties()` para não encadear o espelho ao reiniciar a simulação.
- **Versionamento do log:** `logs/sim.log` vai para o GitHub (não está no `.gitignore`), para o Claude Code na web/celular ler. Commit e push só do log, com a mensagem `log <descrição curta>`, fora da numeração de ALTs.
- **Limitação:** erros antes do `On startup` do Main (na criação dos agentes) não entram no log.
- **Arquivos:** `_alp/Agents/Main/AOC.Main.xml`, `_alp/Agents/Main/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Compila e roda; primeira linha do console é `[log] ... gravando em ...\logs\sim.log`.
  - [ ] Exceção de runtime aparece no `sim.log`.

## ALT-012 — tgrao-trace-bom-dia
- **Data:** 2026-10-13
- **Status:** temporária (demonstração; remover depois)
- **O que:** `On enter` do `colocarLinhaMar` (Tgrao) imprime `[t][Tgrao][colocarLinhaMar] trem=<id> bom dia Bruno`. Trens de alívio não passam por esse bloco.
- **Arquivo:** `_alp/Agents/Tgrao/EmbeddedObjects.xml`

## ALT-013 — alivio-na-linha-4000
- **Data:** 2026-10-13
- **Status:** aplicada, aguardando compilação e validação
- **O que:** alívio na linha 4000 — quando o trem do circuito PCX volta do alívio com 82 vagões (`CAPACIDADE_LINHA_4000`), ele sai da linha 4000 e vai para as DVs pelo circuito de saída do GestorDeCirculacao: `pmc_intervalo4000_a → pst_marginal_c → pst_marginal_b → pst_marginal_a → via2_f → via2_e → via2_d → via2_c → via2_b → via2_a → via2_intermar_c → via2_intermar_b → via2_intermar_a → railwayTrack199 → val_via2 → gestorDeDVs.entrarNasDVs`.
  - **GestorDeAlivios:** depois do `liberar_faixas_ate_terminal`, ◇ `linha_4000_cheia` (`main().getNumeroDeVagoes((Trem) agent) >= CAPACIDADE_LINHA_4000`). Não → `wait_trem_4000` (como antes). Sim → `liberar_linha_4000` (Release das faixas `linha4000_a/b/c` que o trem possui, via nova `get_recursos_linha_4000(Trem)`, + `liberarFaixas`) → `para_alivio_na_linha_4000` (Exit: `eh_alivio = true`, `faixa_atual = faixa_intervalo4000_a`, `proxima_faixa = faixa_marginal_c`, zera `lote_linha_4000`/`ferrovia_na_linha_4000`/`segmento_na_linha_4000`, `gestorDeCirculacao.tentar_usar_faixa.take`).
  - **GestorDeCirculacao:** `circular_intervalo4000_a` → ◇ `definir_sentido_intervalo4000_a` (`!eh_alivio`): sim → `ir_intervalo_intervalo4000_a` (como antes); não → `recuar_intervalo4000_a` (TrainMoveTo de ré até `pmc_intervalo4000_a`) → `alivio_para_marginal_c` (Exit: `faixa_atual = faixa_marginal_c`, `proxima_faixa = faixa_marginal_b`). Daí segue pelo circuito de saída do alívio que já existia.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.{java,xml}`, `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [ ] Compila (F7).
  - [ ] Com 82 vagões na linha 4000, aparece `[Alivio4000][para_alivio_na_linha_4000]` e o trem sai pela intervalo4000_a até as DVs.
  - [ ] Sentido do `recuar_intervalo4000_a` (`forward = false`, de ré como o resto do circuito); se o trem precisar andar para frente, trocar.
  - [ ] Novo ciclo PCX cria loco nova (lote_linha_4000 = null).
- **Efeito a observar:** `intervalo4000_b/c` continuam marcadas por `ocupar_agente(9999)` do `loco_pcx_starts` (sem recurso apreendido) até o próximo ciclo.

## ALT-014 — bracell-circulacao-e-alivio-celulose
- **Data:** 2026-10-13
- **Status:** aplicada, aguardando compilação e validação
- **Regras (Guilherme):** linha 4000 só para T39, TES e ADMO. Celulose PCX (Bracell, Suzano, Eldorado) não alivia pela 4000: o trem sai com todos os vagões que chegaram (Bracell: 64) e o alívio não depende das DVs, só de overload > 1.
- **Bracell:**
  - `getPatio()` = "pcx"; `get_pos_alivio()` = `_bracell_alivio`; `get_quantidade_vazios()` = vazios reais.
  - `encoste` chama `atualizarFerroviaEProdutoEmAtendimento`.
  - Carregados: novo Seize `ocupar_linha5000_6000` (5000 e 6000 juntas) antes do `entrarNoTerminal`; Releases `liberar_faixas_circulacao` (depois do `entrarNoTerminal`) e `liberar_faixas_circulacao1` (depois do `colocar_L1`).
  - `recuar3` → `liberar_linha5000_6000` → `para_guardar_loco` (`gestorDeLocomotivas.guardar_loco`). Removido o `trainDispose`.
  - `iniciar_alivio` ligado ao `irAteTerminal`. Removido o Seize `linha5K` do alívio (ele pegava 5000 OU 6000; a loco agora ocupa no GestorDeAlivios).
  - `recuar4..7` zeram `loteVazios_L1..L4`.
  - `para_circVzs`: `eh_alivio = true`, `faixa_atual = intervalo4000_c`, `proxima_faixa = intervalo4000_b`, devolve a Bracell para `terminais_celulose` → `gestorDeCirculacao.tentar_usar_faixa`.
  - Trem de teste: locos e vagões com ferrovia "rumo" (o `guardar_loco` precisa da ferrovia da loco).
- **GestorDeCirculacao:**
  - Chegada da Bracell: `val_via2 → via3_intermar → via3_a → via3_b → via3_c → silosMuro_a → marginal_c → intervalo4000_a → b → c → bracell.encoste`.
    Novos: ◇ `definir_sentido_val_via2`, `ir_val_via2`, `para_via3_intermar`, ◇ `e_bracell`, `para_intervalo4000_a`. Ajustados: `para_via3_ate_santa`, `para_silosMuro_a`, `e_tgrao`, `para_marginal_c`, `continuar3/4/5`.
  - Corrigido: `ir_intervalo_intervalo4000_b/_c` apontavam para a `pst_marginal_c`.
  - Saída do alívio de celulose, de ré: ◇ `definir_sentido_intervalo4000_c/_b`, `recuar_intervalo4000_c/_b`, `alivio_para_intervalo4000_b/_a`, `liberar_linha5000_6000` (função nova `get_recursos_linha_5000_6000`). Daí segue o circuito de saída da ALT-013 até as DVs.
- **GestorDeAlivios:** lógica separada `analisar_celulose` / `fazer_procedimento_de_alivio_celulose` / `get_wait_locos_ferrovia`, chamada no evento `atualizar_analises`. Variáveis `terminais_celulose` ({bracell}) e `terminal_alivio_celulose`. Fluxo: `loco_alivio_celulose` → `ocupar_linha5000_6000` → `trainEnter_celulose` (`circ_pos_inicio_L5K`) → `ir_alivio_celulose` → `para_alivio_terminal_celulose` (`takeAlivio`).
- **Arquivos:** `_alp/Agents/Bracell/{EmbeddedObjects,Connectors}.xml`, `Bracell/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors}.xml`, `GestorDeCirculacao/Code/Functions.{java,xml}`, `_alp/Agents/GestorDeAlivios/{EmbeddedObjects,Connectors,Variables}.xml`, `GestorDeAlivios/Code/{Functions.java,Functions.xml,Events.java}`, docs.
- **A validar:**
  - [ ] Compila (F7).
  - [ ] Trem de teste licencia val_via2 → … → intervalo4000_c e entra na Bracell.
  - [ ] Faixas da intervalo4000 ficam brancas depois do encoste.
  - [ ] Loco do carregados vai para o `locomotivasRumo` e a linha 5000/6000 fica livre.
  - [ ] Com 64 vazios aparece `[AlivioCelulose]`, a loco entra em `circ_pos_inicio_L5K` e chega em `_bracell_alivio` (se der "no straight route", ajustar o sentido).
  - [ ] Trem de alívio sai de ré pela intervalo4000_c/b/a até a val_via2 e as DVs; `loteVazios_L*` = null.
- **Pendente:** `pode_receber_novo_lote()` para não despachar outro trem para a Bracell durante o alívio (risco de travar na intervalo4000).

## ALT-015 — via3-nova-e-coperxi-pela-via2_e
- **Data:** 2026-10-15
- **Status:** aplicada, aguardando validação
- **Contexto (Main):** trecho novo entre a via3_b e a antiga via3_c. O trecho novo passou a se chamar `via3_c` (401 m) e o antigo, `via3_d`. A via3_b se liga à `via2_e` (nova, 210 m) pela `railwayTrack81`, e a via2_e segue para a `travessao_linha_verde` (encoste do CoperXI).
- **Rota do CoperXI:** `via3_intermar` → `via3_a` → `via3_b` → `via2_e` → `encoste_coperXI` (antes ia pela via2_c → via2_d).
- **Alterações (GestorDeCirculacao):**
  - `para_via3_a`: próxima = via2_c se pêra, senão via3_b.
  - `e_coperXI_ou_pera` → renomeado `e_pera_via3_a` (só pêra). `para_via2_c`: atual = via2_c, próxima = entrada_pera.
  - `para_via3_b`: próxima = via2_e se coperXI, senão via3_c.
  - Novo ◇ `e_coperXI` no fim da via3_b: sim → `para_via2_e` (atual = via2_e, próxima = null); não → `para_via3_c` (atual = via3_c, próxima = via3_d).
  - `circular_via2_e` → novo ◇ `definir_sentido_via2_e` (`!eh_alivio`): sim → novo `ir_via2_e` → `para_encostar_coperXI`; não → `recurar_via2_e` (saída do alívio, como antes).
  - Nova faixa `faixa_via3_d` (`rp_via3_d`, `main().via3_d`, `circular_via3_d`, `pl_via3_d` provisória no painel), na coleção `faixas`. A `faixa_via3_c` passa a ser o trecho novo.
  - Novos `circular_via3_d` → `ir_via3_d` → `para_silosMuro_a`; `ir_via3_c` → novo `para_via3_d` (atual = via3_d, próxima = silosMuro_a).
- **Fora de uso:** o ramo via2_c → via2_d → `encostar_coperXI1` ficou sem trem (só a pêra usa a via2_c).
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/GestorDeCirculacao/Levels/Level.level.xml`
- **A validar:**
  - [x] CoperXI chega ao encoste pela via2_e.
  - [ ] T39, Tgrao e Bracell passam pela via3_c nova e pela via3_d.
  - [ ] CoperXXI continua chegando pela via2_c → entrada_pera.
  - [ ] Saída do alívio pela via2_e (de ré) continua funcionando.
  - [ ] Posição da `pl_via3_d` no painel.

## ALT-016 — circulacao-costaleite
- **Data:** 2026-10-15
- **Status:** validada (trem de teste da CostaLeite chegou pela via2_e)
- **O que:** levar o trem da CostaLeite pelo GestorDeCirculacao: `via3_intermar` → `via3_a` → `via3_b` → `via2_e` → `costaLeite.comecar_encoste` (mesmo caminho do CoperXI até a via2_e).
- **Alterações (GestorDeCirculacao):**
  - `para_via3_b`: próxima = via2_e se coperXI **ou costaLeite**.
  - `e_coperXI` (fim da via3_b): condição passa a ser coperXI ou costaLeite.
  - Novo ◇ `e_costaLeite` depois do `ir_via2_e`: sim → novo `para_costaLeite` (`main().costaLeite.comecar_encoste`); não → `para_encostar_coperXI` (como antes).
- **Alterações (CostaLeite):**
  - Enter `atendimento` renomeado para `comecar_encoste`.
  - `tremTeste_rumo`: destino e terminal dos vagões eram `coperXI`; passam a ser `main().costaLeite`.
  - Novo Release `liberar_faixas_circulacao` entre `delay` e `validacao` (roda a cada lote de 4 vagões).
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/CostaLeite/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [x] Trem de teste da CostaLeite chega ao terminal pela via2_e.
  - [ ] Faixas da rota ficam brancas depois que a cauda passa.
- **Atenção:** logo na entrada, o `vaiMarginal1` da CostaLeite leva o trem inteiro até a `pst_marginal_a` (passando pela via2_f) antes de encostar na balança. A via2_f e a marginal_a são faixas, mas esse movimento não as licencia.

## ALT-017 — costaleite-encostes-moega
- **Data:** 2026-10-15
- **Status:** aplicada, aguardando validação
- **O que:** depois do `para_encostes_moega`, o trem da CostaLeite deixa os lotes nas linhas do terminal e devolve a loco. Cada lote fica parado na linha e guardado numa variável `lote_costaLeite_*` (Trem).
- **Sequência (CostaLeite):** `para_encostes_moega` → novo Enter `encostes_moega` →
  1. `recuar_moegaMeio` (TrainMoveTo de ré até `_costaLeite_moegaMeio`) → `deixar_lote_moegaMeio` (6 vagões → `lote_costaLeite_moegaMeio`)
  2. `avancar_linhaC1` (MoverTremCompleto, cauda entra na `linhaCostaLeita_c1`)
  3. `recuar_linhaTerra` (TrainMoveTo de ré até `_costaLeite_linhaTerra`) → `deixar_lote_linhaTerra` (14 → `lote_costaLeite_linhaTerra`)
  4. `avancar_lateralMoega` (MoverTremCompleto_Ponto, cauda passa `_costaLeite_lateralMoega`) → `deixar_lote_lateralMoega` (11 → `lote_costaLeite_lateralMoega`)
  5. `avancar_linhaMar_a` (MoverTremCompleto_Ponto, `_costaLeite_linhaMar_a`) → `deixar_lote_linhaMar_a` (11 → `lote_costaLeite_linhaMar_a`)
  6. `avancar_linhaMar_b` (MoverTremCompleto_Ponto, `_costaLeite_linhaMar_b`) → `deixar_lote_linhaMar_b` (10 → `lote_costaLeite_linhaMar_b`)
  7. Sobra a loco → `devolver_loco` (DevolverLocomotiva → `gestorDeLocomotivas.guardar_loco`).
- **Detalhes:**
  - Linha (`linhaCostaLeita_c1`) usa MoverTremCompleto; pontos (`_costaLeite_*`) usam MoverTremCompleto_Ponto. Avanços com `vaiRecuar`/`vaiDeRé` = true (avançar) e sentido Ponta da Praia.
  - Cortes pela cauda (`decoupleFirstCars = false`) com `Math.min(N, getNumeroDeVagoes())`: nunca levam a loco.
  - O `outDecoupled` de cada corte vai para um Exit sem ação (`fica_lote_*`): o lote sai do fluxograma e fica parado na linha até a descarga ser modelada.
  - `trem_rumo_grao`: 1 loco + 52 vagões (53 carros; antes eram 2 locos + 51 vagões).
  - O fluxo antigo (`executarDescarga`, `pega4vagoes`, linha de vazios, alívio local) continua no agente, mas não recebe mais trem.
- **Arquivos:** `_alp/Agents/CostaLeite/{EmbeddedObjects,Connectors,Variables}.xml`
- **A validar:**
  - [ ] Sentido Ponta da Praia nos avanços (se der "no route", trocar `sentidoPontaDaPraia` para false).
  - [ ] Cada lote fica com 6/14/11/11/10 vagões e a loco volta ao GestorDeLocomotivas.
  - [ ] Faixas: o `pst_marginal_bloquear` segura marginal a/b/c e intervalo4000 a/b, e o `liberar_faixas_circulacao` ficou no fluxo antigo; nenhum dos dois é liberado no fluxo novo.

## ALT-018 — costaleite-tipos-de-lote
- **Data:** 2026-10-15
- **Status:** aplicada, aguardando validação
- **O que:** cada lote deixado pela ALT-017 tem um `tipoDeLote`, e a `_prioridadeDeLotesDescarga` usa esses tipos. A ordem decide qual lote o `pegar_um_lote_para_descarga()` libera primeiro do `waitLotesParaDescarga`.
- **Tipos (onExitDecoupled dos `deixar_lote_*`):** moegaMeio → `moega-meio`, linhaTerra → `linha-terra`, lateralMoega → `lateral-moega` (estava `mlateral-moega`), linhaMar_a → `linha-mar-a`, linhaMar_b → `linha-mar-b`.
- **`_prioridadeDeLotesDescarga`:** `linha-terra`, `moega-meio`, `lateral-moega`, `linha-mar-a`, `linha-mar-b`. Saem os tipos antigos (linha-bomba, hidrovias, moega-mar, barracao-terra), que não existem neste terminal.
- **Arquivos:** `_alp/Agents/CostaLeite/{EmbeddedObjects,Variables}.xml`
- **A validar:**
  - [ ] Os lotes ainda vão para os Exits `fica_lote_*`; para a prioridade valer, precisam entrar no `descarga` → `waitLotesParaDescarga`.

## ALT-019 — costaleite-passos-e-descarga
- **Data:** 2026-10-15
- **Status:** aplicada, aguardando validação
- **Nomes (CostaLeite):**
  - Decouples: `desacoplar_6vgs_moegaMeio`, `desacoplar_14vgs_linhaTerra`, `desacoplar_11vgs_lateralMoega`, `desacoplar_11vgs_linhaMar_a`, `desacoplar_10vgs_linhaMar_b` (eram `deixar_lote_*`).
  - Blocos de movimento com prefixo `mov_`: `mov_recuar_moegaMeio`, `mov_avancar_lateralMoega1`, `mov_recuar_linhaTerra`, `mov_avancar_lateralMoega`, `mov_avancar_linhaMar_a`, `mov_avancar_linhaMar_b`, `mov_devolver_loco`.
- **Passos separados:** depois de cada `desacoplar_*`, o resto do trem passa por Exit `para_<passo>` → Enter `<passo>` → `mov_<passo>` (`avancar_lateralMoega1`, `avancar_lateralMoega`, `avancar_linhaMar_a`, `avancar_linhaMar_b`, `devolver_loco`).
- **Descarga:**
  - Os Exits `fica_lote_*` mandam cada lote para `descarga` → `waitLotesParaDescarga`.
  - O Enter `devolver_loco` chama `atualiza_vaiDescarga()`: a descarga começa quando a loco é devolvida, pela ordem da `_prioridadeDeLotesDescarga`.
  - Novo `descarga_moega` (grava `_loteEmDescarga`, zera `condicao_vaiDescarga`) → `descarregar_lote` (DescarregaLote) → `para_linhaMar` → `linhaMar`.
  - `formarBlocoMar` chama `atualiza_vaiDescarga()` ao terminar, para puxar o próximo lote.
  - Nova variável `_loteEmDescarga` (Trem).
- **Nomes que vieram do CoperXI (para compilar):**
  - `condicao_ficar_linhaMar_a` → `condicao_ficar_linhaMar`; `_loteDeVazios_LinhaMar_a` → `_loteDeVazios_LinhaMar`; `liberar_wait_lote_linhaMar_a()` → `liberar_wait_lote_linhaMar()`.
  - `mnbVazios_liberar9/10`: `ALL_FROM_SEIZES` de `mnbVazios_ocupar` (não existe) → `SPECIFIED_RESOURCES` com `liberar_faixas_ocupadas`.
  - `atualiza_vaiDescarga()`: `aguardandoRechego = false` (era `mnbCarregados_ocupar3.size() > 0`).
  - Comentados: `loteVz_linhaMar_a.take(agent)` no `paraAlivio_linhaMar_a` e `atualiza_precisa_de_rechego()` no `rp_manobrando_carregados`.
- **Arquivos:** `_alp/Agents/CostaLeite/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CostaLeite/Code/Functions.java`
- **A validar:**
  - [ ] `formarBlocoMar` ainda vai para `posSaidaCoperXI_mar_a` (ponto do CoperXI). Trocar pelo ponto da linha mar da CostaLeite.
  - [ ] `descarregar_lote` descarrega o lote onde ele está; não há movimento até a moega.
  - [ ] Lotes saem na ordem linha-terra, moega-meio, lateral-moega, linha-mar-a, linha-mar-b.

## ALT-020 — costaleite-descarga-vagao-a-vagao
- **Data:** 2026-10-15
- **Status:** aplicada, aguardando validação
- **O que:** descarga como no CoperXI. O lote libera um vagão por vez: o vagão some da linha (TrainExit), é descarregado e reaparece vazio na linha de vazios (TrainEnter), onde o `formarBlocoMar` junta no bloco.
- **Fluxo (CostaLeite):** `descarga_moega` → `pega1vagao` (1 vagão da cauda) →
  - resto do lote → `para_descarga` → volta para `descarga` / `waitLotesParaDescarga`;
  - vagão → `mnbCarregados_ocupar` (rp_manobrando_carregados) → `descarregar` (TrainExit, grava `_loteEmDescarga`) → `descarregar_lote` → `mnbCarregados_liberar` → `mnbVazios_ocupar` (rp_manobrando_vazios) → `enterLinhaMar` (TrainEnter) → `para_linhaMar` → `linhaMar` → `formarBlocoMar`.
- **Gatilho:** `desacoplar_10vgs` (onExit) chama `atualiza_vaiDescarga()`: a descarga começa quando a loco segue para `mov_devolver_loco`. O Enter `devolver_loco` da ALT-019 tinha sido removido no editor.
- **Revertido da ALT-019:** `mnbVazios_liberar9/10` voltam a `ALL_FROM_SEIZES { mnbVazios_ocupar }`, agora que o Seize existe.
- **Corrigido da ALT-019:** comentário no fim da condição do `ficar_linhaMar` (campo de expressão) removido.
- **Arquivos:** `_alp/Agents/CostaLeite/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [ ] `enterLinhaMar` (`posCoperXI_alivio_mar_a`) e `formarBlocoMar` (`posSaidaCoperXI_mar_a`) usam os pontos do CoperXI, como referência. Trocar pelos da linha de vazios da CostaLeite.
  - [ ] Último vagão de cada lote: o `pega1vagao` corta 1 de 1 (mesmo comportamento do CoperXI).

## ALT-021 — costaleite-vazios-mar-meio-terra
- **Data:** 2026-10-15
- **Status:** aplicada, aguardando validação
- **O que:** o vagão descarregado vai para a linha de vazios que tiver espaço: mar, depois meio, depois terra. Cada linha usa os pontos da CostaLeite (`_costaLeite_vazios_*`).
- **Fluxo:** `descarregar_lote` → `mnbCarregados_liberar` → novo `para_formacaoVzs` → `formacaoVzs` → `mnbVazios_ocupar1` →
  - `cabe_linhaMar` (`getTamanhoDoTrem(_loteDeVazios_LinhaMar) < _CAPACIDADE_LINHA_MAR`) → `para_linhaMar` → `linhaMar` → `enterLinhaMar_a` (`positionOnTrack_costaLeite_vazios_mar_comeco`) → `formarBlocoMar` (`_costaLeite_vazios_mar_fim`);
  - senão `cabe_linhaMeio` (`... _LinhaMeio < _CAPACIDADE_LINHA_MEIO`) → `para_linhaMeio` → `linhaMeio` → novo `enterLinhaMeio` (`_costaLeite_vazios_meio_comeco`) → `formarBlocoMar1` (`_costaLeite_vazios_meio_fim`);
  - senão `para_linhaTerra` → `linhaTerra` → novo `enterLinhaTerra` (`_costaLeite_vazios_terra_comeco`) → `formarBlocoTerra` (`_costaLeite_vazios_terra_fim`).
- **Linha terra (cópia da meio adaptada):** blocos `linhaMeio1`, `formarBlocoMar2`, `acumular4`, `wait_blocoMeio1`, `ficar_linhaMeio1`, `paraAlivio_linhaMeio1` → `linhaTerra`, `formarBlocoTerra`, `acumularTerra`, `wait_blocoTerra`, `ficar_linhaTerra`, `paraAlivio_linhaTerra`. Variáveis `_loteDeVazios_LinhaMeio1`, `condicao_ficar_linhaMeio1`, `_CAPACIDADE_LINHA_MEIO1` → `..._LinhaTerra`, `..._linhaTerra`, `_CAPACIDADE_LINHA_TERRA`. Função `liberar_wait_lote_linhaMeio1()` → `liberar_wait_lote_linhaTerra()`.
- **Linha meio:** `formarBlocoMar1`, `acumular3` e `ficar_linhaMeio` usavam as variáveis da linha mar; passam a usar `condicao_ficar_linhaMeio`, `liberar_wait_lote_linhaMeio()` e `_loteDeVazios_LinhaMeio`.
- **Correções:** `liberar_wait_lote_linhaMeio()` liberava pelo `wait_blocoMar` um agente do `wait_blocoMeio`; agora usa o wait da própria linha (idem terra). `cabe_linhaMeio` perdeu os traces do CoperXI (`_loteDeVazios_LinhaTerra_a/_b`, `pstSaidaCoperXI_terra_a`). `mnbVazios_liberar9..14` liberam o `mnbVazios_ocupar1`.
- **Removidos (caminho da ALT-020):** `mnbVazios_ocupar`, `enterLinhaMar`, `para_linhaMar` antigo.
- **Arquivos:** `_alp/Agents/CostaLeite/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CostaLeite/Code/Functions.{java,xml}`
- **A validar:**
  - [ ] Vazios formam bloco nas três linhas, nesta ordem.
  - [ ] `paraAlivio_linha*` ainda não levam a lugar nenhum (Exit sem ação).

## ALT-022 — costaleite-alivio-mar-meio-terra
- **Data:** 2026-10-16
- **Status:** aplicada, aguardando validação
- **O que:** alívio da CostaLeite adaptado do CoperXI. Os lotes de vazios ficam em paralelo (mar, meio, terra), então não há `precisa_mais_vgs` na mesma linha: a cada passada o trem pega o bloco de uma linha, devolve o excedente e o `precisaDeAlívio` decide se vai a outra linha ou sai.
- **Fluxo:** `iniciar_alivio` → `analisar_composicao` (guarda `qt_vgs_inicio_alivio`) → `selecionarLinha` → `mnbVazios_ocupar2` →
  - `ir_linhaMar` (mar tem vazios?) → `alivio_mar`; senão `ir_LinhaMeio` (meio tem vazios?) → `alivio_meio`; senão `alivioTerra`. Ordem fixa mar → meio → terra.
  - `pega_linha*` vai até o `_costaLeite_vazios_*_comeco` (eram pontos do CoperXI). No onEnter: `condicao_ficar_linha* = false` e `liberar_wait_lote_linha*()`. O bloco sai do `wait_bloco*` → `ficar_linha*` (falso) → Exit → Enter (`loteVz_linhaMar`, `alivio_linhaMeio`, `alivio_linhaTerra`) → `in2` do acoplar.
  - `acoplar1/2/acoplar` zeram `_loteDeVazios_Linha*`. `rmv_excesso*` corta o que passa de `quantidade_alivio`.
  - Excedente: `trainExit*` → `mnbVazios_ocupar4/5/6` → `para_retornar_linha*` → novos `retornar_linha*` → `enterRetorno_linha*` (TrainEnter no `_comeco`) → `formarRetorno_linha*` (ré até o `_fim`) → `liberarRetorno_linha*` (grava `_loteDeVazios_Linha*`, `condicao_ficar_linha* = true`) → `wait_bloco*`.
  - `avancoAlivio` → `moverTremCompleto_Ponto` (`pos_aux_alivio_pst`) → `mnbVazios_liberar4` (agora libera o `mnbVazios_ocupar2`; antes liberava o `mnbVazios_ocupar1`) → `precisaDeAlívio`: volta para `selecionarLinha` se ainda faltar e houver vazios; senão novo `deixarTerminal` (`fazendoAlivio = false`, chama `atualiza_vaiDescarga()`) → novo `retornar_gestorDeAlivios` (`main().gestorDeAlivios.retornar_alivio`).
- **Funções:** `_getVagoesVazios()` soma mar+meio+terra; `_getVagoesCarregados()` soma os `lote_costaLeite_*`; `get_quantidade_vazios()` usa `_getVagoesVazios()`; `precisaAlivio()` e `verificarVgsLinhaMar()` sem as linhas `_a/_b` do CoperXI.
- **Arquivos:** `_alp/Agents/CostaLeite/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/CostaLeite/Code/Functions.java`
- **A validar:**
  - [ ] Loco de alívio chega pelo lado do `_fim` e bate no bloco antes do `_comeco` (se chegar pelo outro lado, sai pelo `out` do `pega_linha*`, que não está ligado).
  - [ ] `_loteDeVazios_Linha*` só existe depois do 2º vazio acoplar; uma linha com 1 vazio solto não entra na escolha.
  - [ ] O `wait` do `cabe_linhaTerra` ainda não é liberado por ninguém.

## ALT-023 — suzano-base-e-circulacao
- **Data:** 2026-10-16
- **Status:** aplicada, aguardando validação
- **O que:** levar o trem da Suzano pelo GestorDeCirculacao: `via3_intermar` → `via3_a` → `via3_b` → `via3_c` → `via3_d` → `pst_silosMuro_a` → `pst_marginal_c` → `pmc_linha6000_a` → `suzano.comecar_encoste`.
- **Alterações (Suzano, base copiada da ADM):**
  - `AOC.Suzano.xml`: `Implements ITerminalMoega`.
  - `Code/Functions.xml|java` novos: `main()`, os métodos da interface e `atualizarFerroviaEProdutoEmAtendimento` (usada no `comecar_encoste`). Pátio `pcx`, alívio com 1 loco da ferrovia.
  - `Variables.xml` novo: `quantidade_alivio`, `ferrovia_em_atendimento`, `segmento_em_atendimento`.
  - Novo trem de teste `tremTeste_rumo` (cópia do da ADM, segmento celulose, destino `main().suzano`) → `paraGestorCirculacao_chegadaCarregados`.
  - Novo Release `liberar_faixas_circulacao` entre `mov_avnc__L7000` e `para_atendimento_terminal`.
  - Alívio ainda não implementado: `getOverloadVazios()` = 0, `get_pos_alivio()` = null e `takeAlivio()` dá `error`.
- **Alterações (GestorDeCirculacao):**
  - `para_silosMuro_a`: suzano também reserva a marginal_c.
  - `e_tgrao_ou_celulose`: condição = tgrao || bracell || suzano (a versão salva terminava com `||` sobrando).
  - `para_marginal_c`: próxima = linha6000_a se suzano.
  - Novo ◇ `e_suzano` na saída "não" do `eh_trgao`: sim → `para_linha6000_a` (atual = linha6000_a, próxima = null); não → `para_intervalo4000_a` (Bracell, como antes).
  - Novos `circular_linha6000_a` → `ir_linha6000_a` → `para_suzano` (`main().suzano.comecar_encoste`). A `faixa_linha6000_a` passa a ter `enter = circular_linha6000_a`.
- **Arquivos:** `_alp/Agents/Suzano/{AOC.Suzano,EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/Suzano/Code/Functions.{xml,java}`, `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors,Variables}.xml`
- **A validar:**
  - [ ] Modelo compila com a Suzano implementando `ITerminalMoega`.
  - [ ] Trem de teste da Suzano chega ao `_suzano_encoste`.
  - [ ] A linha6000_a continua funcionando nas manobras do Tgrao.

## ALT-024 — adm-descarga-moegas-linha7
- **Data:** 2026-10-19
- **Status:** aplicada, aguardando validação
- **O que:** descarga das moegas X e V da ADM vagão a vagão, revezando o `mnb_vazios`: 1 vagão da X → linha 7, 1 vagão da V → linha 7, e assim por diante, formando o `lote_linha_7`.
- **Gatilho:** fim da manobra (`rls_mnb_carregados.onExit` → `iniciar_descarga_moegas()`): solta o lote parado em `wait_linha_moegaX/V` (com `condicao_ficar_linha_moega* = true`) para `para_novo_lote1/3` → `descarregar_moegaX/V`.
- **Fluxo por moega (X e V iguais):** `descarregar_moega*` → `sz_mnb_vazios_*` (Seize `mnb_vazios`) → ◇ `linha7_tem_espaco_*` (não → `wait_linha7_cheia_*`) → ◇ `ultimo_vagao_*` →
  - sim (sobra lote): `tira_1vg(_V)` → vagão vai para `trainExit`; resto do lote → `wait_vagao_linha7_*` → `rls_mnb_vazios_*` → `mov_moega*1` → volta ao seize;
  - não (último vagão): o próprio lote vai para `trainExit`; `finalizar_descarga_moega()` zera `lote_moega*`, `descarregando_moega* = false` e libera o `wait_lote_linha2000`.
- **Linha 7:** `trainExit` → `descarregaLote` → `linha_07` → `trainEnter1` → `trainMoveTo1`; saída normal → `rls_mnb_vazios_l7a` → `wait_lote_linha_07`; batida → `liberar_wait_lote_linha_07()` + `rls_mnb_vazios_l7b` → `trainCouple2.in2`. O `wait_lote_linha_07.onEnter` grava `lote_linha_7` e chama `liberar_descarga_moega()`, que solta o lote da moega para devolver o `mnb_vazios`. Assim a outra moega só pega o recurso depois que o vagão entrou no lote da linha 7.
- **Capacidade:** `CAPACIDADE_LINHA_7` = 30 (era 24). Com 30 vagões a moega que tem o recurso para em `wait_linha7_cheia_*`; `liberar_espaco_linha7()` solta (chamar quando o lote da linha 7 sair no alívio).
- **Outras alterações:** `sz_mnb_carregados1` renomeado para `sz_mnb_vazios_X` (agora pega `mnb_vazios`); `para_novo_lote3` mandava para `novo_lote`, agora manda para `descarregar_moegaV`; `ir_moegaX/V` não mandam lote para moega em descarga (`descarregando_moegaX/V`).
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/ADM/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Modelo compila.
  - [ ] Revezamento X/V no `mnb_vazios` (trace `[ADM][linha7]`).
  - [ ] `rls_mnb_vazios_l7a/b` não dá erro quando o vagão não segura o recurso (vagão normal).
  - [ ] Linha 7 para em 30 vagões.
  - [ ] Ligações novas do fluxograma podem aparecer tortas: só visual.
- **Pendências:** alívio da linha 7 ainda não existe (`liberar_espaco_linha7()` e `lote_linha_7 = null` ficam para ele); trem que fica só com locos no meio da manobra não tem saída.

## ALT-025 — adm-alivio-linha7
- **Data:** 2026-10-19
- **Status:** aplicada, aguardando validação
- **O que:** alívio dos vazios da linha 7 da ADM pela linha 4000, igual ao T39 (2 locos FIPS PCX do `loco_pcx_starts`, ou o trem parado na 4000). `CAPACIDADE_LINHA_7` = 35; alívio dispara com 30 vazios exatos (`QUANTIDADE_PARA_ALIVIAR` = 30).
- **Overload (ADM):** `getOverloadVazios()` = vazios / 30, e devolve 1,01 quando há 30 exatos (o gestor só alivia com > 1). `_get_vagoes_vazios()` = tamanho do `lote_linha_7`. `get_quantidade_loco_alivio()` = 1, como no T39.
- **Fluxo do alívio (ADM):** `iniciar_alivio` (trace `[ADM][alivio]`) → `sz_mnb_alivio` (Seize `mnb_vazios` + `mnb_carregados`, prioridade 10: descarga e manobra param; solta o `wait_lote_linha_07` com `condicao_ficar_linha_07 = false`) → `buscar_lote_l7` (até `_adm_linha7_comeco`) → `acoplar_lote_l7` (lote vem por `ficar_linha_09` → `para_novo_lote2` → `alivio_linha_07`) → ◇ `tem_excesso_l7` (zera `lote_linha_7`) →
  - sim: `remover_excesso_l7` (deixa `quantidade_alivio` vagões) → excesso sai do trilho (`trainExit_excesso_l7`) e espera em `wait_excesso_l7`;
  - → `sair_linha7` (de ré até `get_pos_alivio()` = `_adm_alivio_comeco`, na linha2000_e) → `rls_mnb_alivio` (`liberar_excesso_linha7()`: excesso volta pela entrada normal da linha 7; `liberar_espaco_linha7()`) → `para_linha_4000_adm` (`gestorDeAlivios.linha_4000.take`).
- **Correção da ALT-024:** a moega que acha a linha 7 cheia agora devolve o `mnb_vazios` (`rls_linha7_cheia_X/V`) antes de esperar, e volta para o seize ao ser liberada. Antes ela esperava segurando o recurso e o alívio travaria.
- **Linha 7:** `trainMoveTo1.onExitHit` volta `condicao_ficar_linha_07 = true` antes de soltar o lote.
- **GestorDeAlivios:** `terminais_pcx` = { t39, adm }; `analisar_pcx` para o laço quando um alívio da 4000 começa (com T39 e ADM em overload no mesmo ciclo, saíam 2 trens).
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/ADM/Code/Functions.{xml,java}`, `_alp/Agents/GestorDeAlivios/Variables.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`
- **A validar:**
  - [ ] Loco chega ao `_adm_alivio_comeco` e o `buscar_lote_l7` entra na linha 7 no sentido certo (`forward` padrão, como no `buscar_lote` do T39).
  - [ ] `sair_linha7` (de ré) volta para a linha2000_e.
  - [ ] Excesso: lado cortado pelo `remover_excesso_l7` e reentrada pela `_adm_linha7_fim`.
  - [ ] Descarga volta depois do alívio.
- **Riscos conhecidos:** a loco chega pela linha2000_e antes de a ADM apreender `mnb_carregados`: se a manobra estiver recuando para a linha2000_e nesse momento, pode haver colisão. Compatibilidade da 4000: só junta vazios de mesma ferrovia e segmento.

## ALT-026 — adm-entrada-linha7-por-tamanho
- **Data:** 2026-10-19
- **Status:** aplicada, aguardando validação
- **Problema:** `The car being created cannot be put over the occupied switch 'railwaySwitch102'` (o nome `moegaoMar.entraNaMoega.trainExit` na mensagem é do RailSettings compartilhado, não do bloco real). O excesso do alívio (2 vagões) reentrava pelo `trainEnter1` no `_adm_linha7_fim`, a poucos metros do `railwaySwitch102`, e a cauda caía em cima do AMV.
- **Correção:** `trainEnter1` passa a usar trilho + offset (`railwayTrack164`, `get_offset_entrada_linha7()`). A railwayTrack164 começa no `_adm_linha7_fim` e vai para o `_adm_linha7_comeco`; a entrada = offset do `_adm_linha7_fim` + (vagões − 1) × `TAMANHO_VAGAO_GRAO`. Com 1 vagão nada muda. `linha_07.onEnter` grava `vagoes_entrando_linha7`.
- **Linha 7 com vários trilhos:** a linha é railwayTrack164 → 189 → 193 → 195, no mesmo sentido (`get_trilhos_linha7()`). `get_track_entrada_linha7()` / `get_offset_entrada_linha7()` passam para o trilho seguinte quando a distância passa do fim de um trilho (como o `getTrack4000` do T39).
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/ADM/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Orientação do lote criado igual à de antes (o `orientation` do `trainEnter1` ficou no padrão).
  - [ ] Excesso de 2+ vagões entra sem erro e acopla no lote.

## ALT-027 — adm-mnb-alivio-linha7
- **Data:** 2026-10-19
- **Status:** aplicada, aguardando validação
- **Problema:** `The car being added on the track 'railwayTrack164' intersects with another car`. O excesso do alívio (2 vagões) reentrou na linha 7 enquanto a cauda do trem do alívio ainda estava na railwayTrack164: o `rls_mnb_alivio` soltava tudo quando a loco chegava ao `_adm_alivio_comeco`, com o resto do trem ainda na linha.
- **Regra:** enquanto acontece o alívio, nenhum vagão entra na linha 7; enquanto um vagão está entrando na linha 7 (até acoplar no lote), o alívio não começa. Os dois esperam em Seize.
- **Alterações (ADM):**
  - ResourcePool novo `mnb_alivio` (capacidade 1).
  - Seize novo `sz_mnb_alivio_l7` entre `linha_07` e `trainEnter1`; `rls_mnb_vazios_l7a/b` passam a soltar `{ mnb_vazios, mnb_alivio }` quando o vagão entra no lote.
  - `sz_mnb_alivio` / `rls_mnb_alivio` passam a usar `{ mnb_alivio, mnb_carregados }` (sai o `mnb_vazios`: as moegas continuam descarregando e o vagão espera no `sz_mnb_alivio_l7`).
  - TrainMoveTo novo `afastar_linha7` (de ré, rumo à `pos_linha_4000_criar_loco`, distância = comprimento do trem, como o `paraFrente` do T39) entre `sair_linha7` e `rls_mnb_alivio`: o trem inteiro sai da linha 7 antes de liberar.
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [ ] `afastar_linha7` tem espaço na linha 2000 para andar o comprimento do trem.
  - [ ] Excesso reentra sem colisão depois do `afastar_linha7`.

## ALT-028 — adm-fim-manobra-so-locos
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **Problema:** no rechego do `wait_lote_linha2000` (resto do 1º trem: 27 vagões + 2 locos), os 27 couberam inteiros na moega V e voltou para o `decidir_moega` um trem só com as 2 locos. A manobra nunca passava pelo `retornar_linha2000` → `rls_mnb_carregados`, então `iniciar_descarga_moegas()` não era chamado e o lote ficava parado no `wait_linha_moegaV` (e o `mnb_carregados` preso).
- **Correção (ADM):** ◇ novo `so_locos_manobra` entre `decidir_moega` e `tem_loco_cabeca1` (`getNumeroDeVagoes() == 0`):
  - sim → `rls_fim_manobra` (solta `mnb_carregados`, chama `iniciar_descarga_moegas()`) → `para_guardar_locos_mnb` (`gestorDeLocomotivas.guardar_loco.take`);
  - não → `tem_loco_cabeca1`, como antes.
- **Traces:** `[ADM][decidir_moega]` (carros, vagões e tamanho dos lotes X/V a cada passagem), `[ADM][tem_loco_cabeca1]`, `[ADM][fim_manobra]` (nos dois finais da manobra).
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects,Connectors}.xml`
- **A validar:**
  - [ ] Trace `[ADM][decidir_moega] ... vagoes=0` seguido de `[ADM][fim_manobra] ... so com 2 loco(s)`.
  - [ ] Descarga da moega V recomeça depois do rechego.
  - [ ] Locos de manobra chegam ao `guardar_locoFIPS` e devolvem o `rp_locoFips_pcx`.

## ALT-030 — adm-linha7-trilho-unico
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **O que:** no Main, a linha 7 virou um trilho só (`railwayTrack164`, com `_adm_linha7_fim` no offset 17 e `_adm_linha7_comeco` no 536). 189, 193 e 195 deixaram de existir e o modelo não compilava (`railwayTrack189 cannot be resolved`).
- **Correção (ADM):** `get_trilhos_linha7()` devolve só `{ railwayTrack164 }`. `get_track_entrada_linha7()`, `get_offset_entrada_linha7()` e `trem_na_linha7()` continuam iguais, porque já percorrem essa lista.
- **Arquivos:** `_alp/Agents/ADM/Code/Functions.java`

## ALT-031 — adm-excesso-alivio
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **Problema:** a partir do 2º alívio o trem da 4000 já chegava com vazios. O `remover_excesso_l7` cortava `vagões do trem − quantidade_alivio`, sem descontar os que o trem trazia: o excesso devolvido à linha 7 incluía vagões da 4000 (ex.: 38 + 36 − 35 = 39), o trem saía sem levar nada da ADM e o alívio se repetia. O excesso volta sem checar capacidade, a linha 7 passou de 35 e o vagão seguinte bateu na cauda do lote (`intersects with another car`).
- **Correções (ADM):**
  - `qt_vgs_inicio_alivio` gravado no `iniciar_alivio` (vagões que o trem traz). `tem_excesso_l7` / `remover_excesso_l7`: excesso = `vagões − qt_vgs_inicio_alivio − quantidade_alivio` (igual ao T39). Trace `[ADM][alivio] ... excesso=` e `ERRO` se o excesso passar de `CAPACIDADE_LINHA_7`.
  - `vagoes_pendentes_linha7`: soma no `linha_07` e desconta nos `rls_mnb_vazios_l7a/b` (vagões a caminho do lote, inclusive o excesso).
  - `alivio_linha7_em_andamento`: true no `sz_mnb_alivio`, false no `rls_mnb_alivio`.
  - `linha7_tem_espaco_X/V`: `!alivio_linha7_em_andamento && lote_linha_7 + vagoes_pendentes_linha7 < CAPACIDADE_LINHA_7`. Durante o alívio a moega espera em `wait_linha7_cheia_*` e é liberada pelo `liberar_espaco_linha7()` no fim do alívio.
  - `get_distancia_entrada_linha7()`: aviso `[ADM][entrada_l7] AVISO` quando a cabeça do lote passaria do `_adm_linha7_comeco`.
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/ADM/Code/Functions.java`
- **A validar:**
  - [ ] 2º alívio em diante: trace `chegou com 30 vagoes` e excesso pequeno (lote − quantidade_alivio).
  - [ ] Linha 7 nunca passa de 35.
  - [ ] `cabem na 4000` diminui a cada alívio.

## ALT-032 — adm-descarga-rp-moega
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **Contexto:** Guilherme trocou a descarga: cada moega tem o seu recurso (`rp_moegaX`, `rp_moegaV`), X e V descarregam em paralelo (`tira_1vg1/2` → `executar_descarga_moega*` → `sz_rp_moega*` → `trainExit1/2` → `descarregaLote1/2` → `linha_07`); o fluxo antigo com `mnb_vazios` ficou excluído do build.
- **Correções (ADM):**
  - `esta_descarregando/1`: condição quebrada (`!(rp_moegaX).idle() > 0)`) → `rp_moega*.idle() == 0 || !linha7_tem_espaco()`; trace `[ADM][descarga_X/V] ... espera`.
  - `para_executar_descarga_moegaV` mandava para `executar_descarga_moegaX` → agora `executar_descarga_moegaV`.
  - `wait_rp_moegaX/V` nunca eram soltos: `liberar_wait_rp_moegaX/V()` no `onExit` dos `rls_mnb_vazios_l7a/b` (quando o vagão entra no lote da linha 7 e devolve o recurso) e no `liberar_espaco_linha7()` (fim do alívio). A saída do wait volta para o `esta_descarregando` (testa de novo).
  - Último vagão: ◇ `ultimo_vagao_X2/V2` antes do `tira_1vg1/2`; com 1 vagão o próprio lote vai para a descarga e `finalizar_descarga_moega()` zera o lote, `descarregando_moega*` e solta o `wait_lote_linha2000`.
  - Capacidade da linha 7: `linha7_tem_espaco()` = sem alívio e `lote_linha_7 + vagoes_pendentes_linha7 + vagoes_em_descarga < CAPACIDADE_LINHA_7`. `vagoes_em_descarga` soma no `executar_descarga_moega*` e desconta no `para_linha_07a/b`.
  - Funções que usavam blocos excluídos: `liberar_descarga_moega()` ficou vazia; `liberar_espaco_linha7()` agora solta os `wait_rp_moega*`.
  - Delay `iniciar_descarga_moegas` renomeado para `delay_iniciar_descarga` (mesmo nome da função).
  - Removidos 2 conectores com uma ponta vazia (`connector41`, `connector_alt024_909`).
- **Não alterado:** `getOverloadVazios()` (versão do Guilherme, como no T39): com 30 exatos dá 1,0 e o gestor só alivia com mais de 1.
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/ADM/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Compila (fluxo antigo excluído do build).
  - [ ] X e V descarregam em paralelo; um lote espera só a própria moega.
  - [ ] Último vagão: trace `[ADM][descarga] moega X/V vazia` e rechego do `wait_lote_linha2000`.
  - [ ] Linha 7 não passa de 35.

## ALT-033 — adm-vagoes-entrando-linha7
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **Problema:** `The car being created cannot be put over the occupied switch 'railwaySwitch102'`. O `vagoes_entrando_linha7` era gravado no `linha_07.onEnter`, mas o `trainEnter1` só usa depois do `sz_mnb_alivio_l7`. Com a descarga em paralelo, o excesso do alívio (4 vagões) esperava no Seize e um vagão avulso que chegou depois sobrescreveu o valor para 1: o excesso entrou com a cabeça em 22 m e a cauda caiu no AMV.
- **Correção (ADM):** `vagoes_entrando_linha7` (e o trace) passa para o `sz_mnb_alivio_l7.onExit`, logo antes do `trainEnter1`. O `linha_07.onEnter` fica só com `vagoes_pendentes_linha7 += ...`.
- **Arquivos:** `_alp/Agents/ADM/EmbeddedObjects.xml`

## ALT-034 — gestor-alivios-libera-4000-cheia
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **Problema:** depois do 1º trem de 82 sair da 4000 para as DVs, nenhum terminal da 4000 (T39, ADM) era aliviado de novo. `aux_esta_ocorrendo_alivio_pcx` vira true no `fazer_procedimento_de_alivio_pcx` e só voltava a false no `wait_trem_4000` (trem volta com menos de 82). No caminho do trem cheio (`linha_4000_cheia` → `liberar_linha_4000` → `para_alivio_na_linha_4000`) ela ficava true e `condicao_4000_pode_aliviar()` dava false para sempre.
- **Correção (GestorDeAlivios):** `para_alivio_na_linha_4000.onExit` faz `aux_esta_ocorrendo_alivio_pcx = false` junto com `lote_linha_4000 = null`. O próximo alívio cria loco nova (`condicao_solicitar_locoFips_pcx`).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`
- **Pendente (visto no log):** o excesso do alívio volta para a linha 7 sem passar pelo teste de capacidade; com excesso de 20 a linha 7 chegou a 36.

## ALT-035 — tes-manobra-rumo-linhas-03-04-05
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **O que:** a Rumo chega na 3000 e fraciona os 41 vagões da frente, que são distribuídos nas linhas 03 (24), 04 (12) e 05 (o resto). Entre uma linha e outra a loco recua até `_tes_trocar_via` (sentido Valongo) com o `MoverTremCompleto_Ponto`. MRS e VLI seguem como antes.
- **Fluxo (TES):** `liberar_faixas_circulacao` → ◇ `eh_rumo` (função `eh_trem_rumo`)
  - não: `para_ajuste_loco` (como antes)
  - sim: `sooRumo_41vgs` (42 carros da frente = loco 0 + 41 vagões, `newTrain = Trem`). O resto (loco 42 + 41 vagões) fica na 3000 e vai para `para_ajuste_loco`.
  - parte da frente: `comecar_encoste_moegas` → `ir_linha03` → `tirar_24vgs` → `recuar_trocar_via_1` → `ir_linha04` → `tirar_12vgs` → `recuar_trocar_via_2` → `ir_linha05` → `tirar_vgs_linha05` → `recuar_trocar_via_3` → `devolverLocomotiva`
  - Lotes deixados: `para_descarga_linha03/04/05` gravam `lote_linha_3/4/5` e param ali (antes faziam `novo_lote.take` e voltariam para a 3000).
- **Orientação:** os vagões que ficam são os mais longe da loco (lado Ponta da Praia), então os cortes usam `decoupleFirstCars = false`. A entrada nas linhas é para longe da loco (`forward = false`). O recuo é para o lado da loco: `vaiRecuar = true`, `sentidoPontaDaPraia = false`.
- **Renomeados:** `trainMoveTo` → `ir_linha03`, `trainMoveTo1` → `ir_linha04` (apontava para a linha 03), `tirar_24vgs1` → `tirar_12vgs` (era 24).
- **Removidos:** `comecar_encoste1`, `para_novo_lote1`, `comecar_encoste_moegas1`, `para_novo_lote3`, `para_novo_lote4` (sem entrada).
- **Novos:** `eh_rumo`, `recuar_trocar_via_1/2/3`, `ir_linha05`, `tirar_vgs_linha05`, `para_descarga_linha05`, `devolverLocomotiva`, variável `lote_linha_5`, função `eh_trem_rumo`.
- **Correção:** `atualizar_licenciamento_linha3000` estava invertida (no `onEnter` o trem ainda não estava na track e a faixa era liberada). Agora a faixa fica ocupada enquanto `lote_linha3000_emDescarga` ou `lote_linha3000_proximo` não forem null.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Compila (ver pendências do TES abaixo).
  - [ ] Rumo: 41 vagões + loco 42 ficam na 3000; 24 na linha 03, 12 na 04, 5 na 05; loco devolvida.
  - [ ] Sentido dos movimentos (`forward = false` nas idas, recuo para o Valongo).
- **Pendências do TES (não mexidas, em construção):** `ir_moegaX/V` e os Exit das moegas usam `descarregando_moega*`, `lote_moega*`, `CAPACIDADE_LINHA_MOEGA_*`, `encoste_moega*`, `retornar_linha2000`, que não existem; `liberar_wait_lote_linha_03/04` usam `wait_lote_linha_09`, que não existe.

## ALT-036 — tes-descarga-linha-03
- **Data:** 2026-10-20
- **Status:** aplicada, aguardando validação
- **O que:** o lote de 24 deixado na linha 03 vai até `_tes_moega_linha03`, descarrega 2 vagões (`Trem.descarregar_vagoes(true, 2)`) e avança 2 vagões em direção a `_tes_linha03_fim`, repetindo. Para quando não dá mais para avançar 1 vagão até o fim; aí `talhou_linha03 = true`.
- **Fluxo (TES):** `para_descarga_linha03` (grava `lote_linha_3` e faz `exec_descraga_linha03.take`) → `mv_entrar_moega` (cabeça na moega) → `service` (`rp_moega03`, descarrega no `onExit`) → ◇ `tem_espaco` (`distancia_ate_fim_linha03 >= TAMANHO_VAGAO_GRAO`)
  - sim: `mv_tirar_vagoes_moega` (avança `qt_vgs_descarga_linha03` vagões, 2 ou 1 se só couber 1) → volta para o `service`
  - não: `para_fim_descarga_linha03` (era `para_novo_lote1`, que mandava o lote para a 3000) → `talhou_linha03 = true`
- **Geometria:** `_tes_moega_linha03` (offset 226) e `_tes_linha03_fim` (offset 8) estão na `railwayTrack155`. A cabeça do lote (carro 0, lado Valongo) aponta para o fim, então os dois movimentos usam `forward = true` e a descarga começa pela frente.
- **Novos:** variáveis `talhou_linha03` (false) e `qt_vgs_descarga_linha03` (2); função `distancia_ate_fim_linha03(Trem)` (menor distância entre uma ponta de carro na track do fim e o ponto).
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] A cabeça do lote chega na moega andando para frente (se o lote já cobre a moega quando é deixado, o `mv_entrar_moega` falha).
  - [ ] Descarrega 2 por vez e para com menos de 1 vagão até o fim; trace `[TES][descarga_linha03] ... talhou`.

## ALT-037 — tes-descarga-linhas-04-05
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** descarga vagão a vagão dos lotes das linhas 05 e 04 na moega 04, com os vazios indo para os lotes de fundo. Ordem: `lote_linha_5` e depois `lote_linha_4`. Vazios: `lote_linha_05_fundo` até `CAPACIDADE_LINHA_05_FUNDO`, depois `lote_linha_04_fundo` (só com `lote_linha_4 == null`, porque ocupa o mesmo espaço).
- **Fluxo (TES):** `para_descarga_linha05` → `exec_descarga_04e5` → ◇ `tem_fundo_livre` → `sz_descarga_04e5` (`rp_mnb_linha04e5` + `rp_moega04`, seguro o ciclo inteiro) → ◇ `eh_ultimo_vg_04e5`
  - não: `cortar_1vg_04e5` (1 vagão); o resto do lote espera em `aguarda_vg_04e5` → `rls_lote_04e5` → volta para o `tem_fundo_livre`
  - sim: o próprio lote é o vagão; `lote_linha_5/4 = null`
  - vagão: `trainExit_04e5` → `descarga_moega04` (50 s, `descarregar_vagoes`) → `trainEnter_fundo` (1 vagão para dentro a partir do fim do fundo) → `mv_vazio_fundo` até o começo do fundo
    - chegou ao começo (fundo vazio): `rls_vazio_04e5a` → vira o lote de fundo
    - bateu no lote de fundo: `rls_vazio_04e5b` → `acoplar_fundo` (lote sai do `wait_fundo_0X` pelo `liberar_lote_fundo`)
  - `wait_fundo_05/04` grava `lote_linha_0X_fundo` e chama `fim_ciclo_04e5` (solta o próximo vagão ou, no fim da linha 05, puxa o `lote_linha_4`)
- **Mesmo padrão da ADM** (`trainEnter1` → `trainMoveTo1` com `outHit` → `trainCouple2` → `wait_lote_linha_07`).
- **Fundos:** 05 = `_tes_linha05_fundo_fim` (offset 10) → `_tes_linha05_fundo_comeco` (335) na `railwayTrack186`; 04 = `_tes_linha04_fim` (10) → `_tes_linha04_comeco` (320) na `railwayTrack184`.
- **Capacidade física:** `cabe_no_fundo` também exige `(n + 1) × TAMANHO_VAGAO_GRAO ≤ espaço entre os pontos` (o vazio precisa de 1 vagão livre perto do fim para entrar). Com 17,5 m, o fundo 05 (325 m) leva no máximo 18 vagões, mesmo com `CAPACIDADE_LINHA_05_FUNDO = 21`.
- **Removidos:** rascunhos `comecar_encoste1/2`, `service1/2`, `trainExit`, `para_novo_lote1`.
- **Novos:** variáveis `fundo_destino_04e5`, `lote_em_descarga_04e5`; funções `get_fundo_destino`, `cabe_no_fundo`, `get_pos_fim_fundo`, `get_pos_comeco_fundo`, `get_offset_entrada_fundo`, `liberar_lote_fundo`, `fim_ciclo_04e5`.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Sentido: o vazio entra perto do fim e anda para o começo (`forward` padrão, como na ADM).
  - [ ] 5 vazios no fundo 05, depois 12 no fundo 05 também (17 ≤ 18); `lote_linha_5` e `lote_linha_4` ficam null.
- **Pendente:** se o fundo 05 encher com o `lote_linha_4` ainda carregado, a descarga para (`para_sem_espaco_fundo`, trace `AVISO`). Fim da descarga das 04/05 (item 5) fica para depois.

## ALT-038 — tes-descarga-04-05-lote-no-wait
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** fecha a reorganização do Guilherme (manobra em etapas com Exit/Enter e lote carregado parado num Wait da própria linha, só o vagão cortado vai para a moega 04). Corrige o que estava quebrado e remove o que ficou sem uso.
- **Manobra:** `encoste_linha_04` → `sz_rp_mnb_linha04e05` → `ir_linha04` (o `rp_mnb_linha04e5` fica preso da linha 04 até o `rls_rp_mnb_linha04e05`, no `finalizar_encoste`). No fim da manobra, `rls_rp_mnb_linha04e05.onExit` chama `proximo_vagao_04e5()`.
- **Descarga:** `wait_lote_linha05/04` → ◇ `eh_ultimo_vg_05/04`
  - não: `cortar_1vg_05/04` (o resto volta para o wait) → `para_vagao_moega_05/04`
  - sim: o próprio lote vai para a moega e `lote_linha_5/4 = null`
  - `exec_descarga_04e5` → `sz_descarga_04e5` (`rp_mnb_linha04e5` + `rp_moega04`, pelo vagão) → `trainExit_04e5` → moega → fundo (como na ALT-037); `rls_vazio_04e5a/b` liberam.
  - `fim_ciclo_04e5` (no `wait_fundo_05/04`) → `proximo_vagao_04e5()`: se não há ciclo ativo e os dois recursos estão livres, escolhe a linha 05 (ou a 04 quando `lote_linha_5 == null`), confere o fundo (`get_fundo_destino`) e solta o wait da linha.
- **Corrigido:**
  - `.take(agent);.take(agent);` no `para_descarga_linha05` e `para_finalizar_encoste` (não compilava).
  - `lote_linha_5 = agent` copiado em `para_encoste_linha_04/05`, `para_finalizar_encoste`, `para_descarga_linha06/07`, e `lote_linha_3 = agent` em `para_encoste_linha03` (gravavam o trem da loco ou o vagão cortado). Agora esses Exit só passam o agente.
  - `get_fundo_destino` e `sz_descarga_04e5` estavam excluídos do build; voltaram.
- **Renomeados:** `cortar_1vg_04e6/04e7` → `cortar_1vg_05/04`; `para_descarga_linha07/06` → `para_vagao_moega_05/04` (o 06 atendia a linha 04 e o 07 a 05).
- **Removidos (sem uso):** blocos `comecar_encoste1`, `para_novo_lote1`, `tem_fundo_livre`, `para_sem_espaco_fundo`, `eh_ultimo_vg_04e5`, `cortar_1vg_04e5`, `aguarda_vg_04e5`, `rls_lote_04e5` e 16 conectores (incluindo os de ponta solta); funções `liberar_wait_lote_linha_03/04` (usavam `wait_lote_linha_09`, que não existe); variáveis `condicao_ficar_linha_03/04`, `CAPACIDADE_LINHA_3000/3/4`. `lote_em_descarga_04e5` virou `ciclo_04e5_ativo`.
- **Mantidos de propósito:** `lote_linha_03_fundo` e `CAPACIDADE_LINHA_03_FUNDO` (fundo da linha 03, ainda a fazer); `podeReceberNovoLote`, `get_faixas_primeiro_alivio`, `get_primeiras_linhas_de_circulacao_vazios` e `iniciar_alivio` (exigidos pela `ITerminalMoega` / alívio).
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Compila.
  - [ ] Descarga começa quando a loco sai (`rls_rp_mnb_linha04e05`); 5 vagões da 05, depois 12 da 04; traces `linha 05 descarregada` e `linha 04 descarregada`.

## ALT-039 — tes-descarga-04-05-nao-comecava
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **Problema:** depois do `finalizar_encoste` a descarga das linhas 04/05 não começava (log: `[linha05] lote=... com 5 vagoes` e nada depois). O `tirar_vgs_linha05` solta o lote (→ `ficar_linha05` → `wait_lote_linha05`) e a loco (→ `finalizar_encoste` → `rls_rp_mnb_linha04e05`) no mesmo instante; os `Enter` passam o agente adiante de forma assíncrona e a loco chegava ao release antes do lote entrar no wait. O `proximo_vagao_04e5()` via `wait_lote_linha05.size() == 0`, saía, e ninguém chamava de novo.
- **Correção (TES):** `wait_lote_linha05/04.onEnter` também chamam `proximo_vagao_04e5()` — funciona nas duas ordens (durante um ciclo, `ciclo_04e5_ativo` barra a volta do resto do lote). Traces `[proximo_vagao_04e5] espera: ...` quando ele sai sem soltar. `finalizar_encoste.onEnter` sem o `atualizarFerroviaEProdutoEmAtendimento` (só a loco passa ali e imprimia `erro: trem não tem nenhum vagão`).
- **Arquivos:** `_alp/Agents/TES/EmbeddedObjects.xml`, `_alp/Agents/TES/Code/Functions.java`

## ALT-040 — tes-iniciar-descarga-moegas
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** a descarga das linhas 04/05 passa a começar como na ADM. Os lotes ficam parados nos `wait_lote_linha05/04`; no fim da manobra de encoste, `rls_rp_mnb_linha04e05` → `delay_iniciar_descarga` (0,1 s, cópia do da ADM) → `devolverLocomotiva`, e o delay chama `iniciar_descarga_moegas()`, que solta o primeiro vagão (`proximo_vagao_04e5`). Os próximos continuam saindo no fim de cada ciclo (`fim_ciclo_04e5`).
- **Desfeito da ALT-039:** os `wait_lote_linha05/04.onEnter` não chamam mais a descarga; `rls_rp_mnb_linha04e05.onExit` ficou vazio (o delay de 0,1 s garante que os lotes já estão nos wait). Os traces `[proximo_vagao_04e5] espera` ficaram.
- **Linha 03:** continua começando sozinha no `para_descarga_linha03` (decisão do Guilherme).
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`

## ALT-041 — tes-proximo-vagao-sem-idle
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **Problema:** só o 1º vagão das linhas 04/05 descarregava. Log: `[fundo_05] lote=... com 1 vazios` e logo depois `[proximo_vagao_04e5] espera: rp_mnb_linha04e5 livre=0, rp_moega04 livre=0`. O `fim_ciclo_04e5` roda no `wait_fundo_05.onEnter`, no mesmo instante em que o vagão acabou de liberar os recursos no `rls_vazio_04e5a/b`; nesse instante o pool ainda aparece ocupado, o `proximo_vagao_04e5` saía e ninguém chamava de novo.
- **Correção (TES):** `proximo_vagao_04e5` não confere mais `rp_*.idle()`. Quem garante um vagão por vez é o `ciclo_04e5_ativo`; se a manobra de um próximo trem estiver com o `rp_mnb_linha04e5`, o vagão espera na fila do `sz_descarga_04e5`. A capacidade dos fundos continua sendo conferida antes de soltar cada vagão (`get_fundo_destino`).
- **Arquivos:** `_alp/Agents/TES/Code/Functions.java`

## ALT-042 — tes-alivio
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** completa o alívio do TES que o Guilherme desenhou, no padrão do T39. Ordem dos lotes: vazios da linha 03 → fundo 05 → fundo 04.
- **Fluxo (TES):** `iniciar_alivio` → `analisar_composicao` (`qt_vgs_inicio_alivio`) → `sz_alivio` (`rp_mnb_linha03` + `rp_mnb_linha04e5`: descarga e manobras esperam) → `analisar_alivio` → ◇ `precisa_aliviar` (vagões pegos < `quantidade_alivio`)
  - ◇ `tem_vagoes_linha03` (`pode_aliviar_linha03`: descarga talhou e há vazios) → ◇ `tem_vagoes_linha05` (fundo 05 no wait) → ◇ `tem_vagoes_linha04` (fundo 04 no wait)
  - por linha: `ir_alivio_linha0X` → `mov_ir_linha0X` (desce até bater no lote; no `onEnter` solta o lote para `alivio_linha0X`) → `pegar_vazios*` → `remover_excesso_0X` (`get_excesso_alivio`) → `recuar_trocar_via_3/5/4` → `analisar_alivio`
  - sem mais nada a pegar: `rls_alivio` → `para_fim_alivio` → `gestorDeAlivios.linha_4000`
- **Excesso:** fica onde está. Linha 03: os vazios que sobram e os carregados voltam a ser o `lote_linha_3`. Fundos 05/04: os carros que ficam são os do lado do começo, onde o lote de fundo já mora; voltam para o `wait_fundo_0X` por `voltar_fundo0X` (sem `TrainEnter`), com `voltando_do_alivio` para não fechar ciclo de descarga.
- **`get_excesso_alivio`:** `levar = min(vazios do lote, quantidade_alivio - já pegos)`; `excesso = carros do lote - levar`.
- **Fundos 05/04 no padrão do T39:** `wait_fundo_0X` → ◇ `ficar_fundo0X` (`condicao_ficar_fundo0X`) → `acoplar_fundo1/2.in1` (descarga) ou `para_alivio_fundo0X` → `alivio_linha0X` (alívio). `liberar_lote_fundo` põe `condicao_ficar = true`; `mov_ir_linha05/04` põe `false`.
- **Interface:** `get_pos_alivio()` → `_tes_inicio_alivio`; `getOverloadVazios()` como no T39 (2.0 com `rp_moega03` e `rp_moega04` livres e só vazios no terminal; senão vazios / `QUANTIDADE_PARA_ALIVIAR`, novo parâmetro = 20). GestorDeAlivios: `main().tes` em `terminais_pcx`.
- **Corrigido:**
  - `eh_ultimo_vg_05/04`: com a condição invertida, o `lote = null` estava no caminho do corte; foi para o "Ao sair (false)".
  - `trainEnter_fundo` ia para `para_fim_descarga_linha04` (código da linha 03); agora `para_vazio_fundo` manda para `entrar_fundo05` ou `entrar_fundo04`.
  - `wait_fundo_06` voltou a ser `wait_fundo_05` (o `liberar_lote_fundo` usa esse nome); conector solto no `wait_fundo_04`.
  - `remover_excesso_*` com `newTrain = Trem`.
- **Renomeados:** `enter/enter1/enter2` → `ir_alivio_linha04/03/05`; `enter3/4` → `entrar_fundo05/04`; `exit7/12/8` → `para_alivio_linha03/05/04`; `exit10` → `para_fim_alivio`; `exit6/11` → `para_alivio_fundo05/04`; `exit3/5/4` → `excesso_linha03/05/04`; `trainDecouple/2/1` → `remover_excesso_03/05/04`.
- **Removidos:** `analisar_alivio1`, `exit9`, `exit13` e 13 conectores.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`, `_alp/Agents/GestorDeAlivios/Variables.xml`
- **A validar:**
  - [ ] Compila.
  - [ ] Sentido: `mov_ir_linha0X` com `forward` padrão (o trem chega de frente pela `_tes_inicio_alivio`), recuo para o Valongo.
  - [ ] `remover_excesso` com 0 carros (lote todo levado) — o T39 usa o mesmo `Math.max(0, ...)`.
  - [ ] `primeiro_alivio_faixas` do TES termina com vírgula (`faixa_linha2000_d,`) — conferir se compila e se essas faixas são o caminho da 4000 até `_tes_inicio_alivio`.

## ALT-043 — tes-liberar-wait-fundo-tradicional
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** soltar os lotes de fundo do jeito do T39 (`condicao_ficar_linha_09 = true; liberar_wait_lote_linha_09();`), uma função por fundo, no lugar da `liberar_lote_fundo()` que escolhia pelo `fundo_destino_04e5`.
  - `mv_vazio_fundo05/04.onExitHit`: `condicao_ficar_fundo0X = true; liberar_wait_lote_fundo0X();`
  - `mov_ir_linha05/04.onEnter` (alívio): `condicao_ficar_fundo0X = false; liberar_wait_lote_fundo0X();`
  - `liberar_lote_fundo()` virou `liberar_wait_lote_fundo05()`; nova `liberar_wait_lote_fundo04()`.
- **Arquivos:** `_alp/Agents/TES/EmbeddedObjects.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`

## ALT-044 — tes-retomar-descarga-apos-alivio
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** depois do alívio a descarga continua (padrão do `iniciar_descarga_moegas` da ADM).
  - `excesso_linha03` → `voltar_linha03` → `wait_retomar_linha03`: o que sobrou da linha 03 espera o trem de alívio sair.
  - `rls_alivio.onExit` → `retomar_descargas()`: solta o lote da linha 03 no `tem_espaco` (volta ao loop avança → `service`; `talhou_linha03 = false` se houver carregados) e chama `proximo_vagao_04e5()` (moega 04 volta se tinha parado por falta de espaço nos fundos).
  - `tem_espaco` também exige carregados no lote; sem carregados vai para `para_fim_descarga_linha03` (`talhou_linha03 = true` = "descarga da linha 03 parada, pronta para o alívio").
  - Trace `[alivio] analisar: pegos=.. linha03=.. fundo05=.. (wait ..) fundo04=..` em cada decisão. Na rodada de 13:05 o alívio levou 18 da linha 03 e terminou com `falta=3` sem passar no fundo 05 (5 vazios); o trace mostra por quê na próxima rodada.
- **Overload:** `getOverloadVazios()` já tem a regra do 2.0 igual à ADM/T39 (moegas 03 e 04 livres e `_getVagoesCarregados() == 0`). Não dispara porque `_getVagoesCarregados()` conta o resto do Rumo na 3000 (41 vagões que ficam no `para_ajuste_loco`) e os carregados da linha 03.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`

## ALT-045 — tes-locos-fips-lote-3000
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** depois do `encoste_linha_05`, 2 locos FIPS PCX vão para a cauda dos vagões que ficaram na 3000 (padrão do ADM: `loco_pcx_starts` → `usar_recurso_loco` → `trainEnter` → `trainMoveTo` → `acoplar_loco_mnb`).
- **Fluxo (TES):** `encoste_linha_05.onEnter` → `rechego_3000.take(lote_linha3000_emDescarga)` → `acoplar_loco_pcx.in1` (`onEnter1`: `loco_pcx_starts.inject()`)
  - `loco_pcx_starts` (TrainSource, `qt_loco` locos FIPS, pátio pcx) → `usar_recurso_loco` (seize `qt_loco` de `gestorDeLocomotivas.rp_locoFips_pcx`) → `entrar_loco_pcx` (`t39_criar_loco_rechego`) → `mv_loco_pcx_3000` (até `_tes_encoste`) → bate no lote (`outHit`) → `acoplar_loco_pcx.in2`
  - `acoplar_loco_pcx` → `para_lote_3000_com_loco` (próxima etapa a definir; só trace)
- **Novos:** variável `qt_loco` = 2; blocos `rechego_3000`, `acoplar_loco_pcx`, `loco_pcx_starts`, `usar_recurso_loco`, `entrar_loco_pcx`, `mv_loco_pcx_3000`, `para_lote_3000_com_loco`.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`
- **A validar:** as locos batem na cauda do lote (lado oposto à loco 42 da Rumo); `rp_locoFips_pcx` fica com 2 locos presas até a próxima etapa devolver.

## ALT-046 — tes-encoste-resto-3000
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** fecha o ciclo do Rumo. O resto da 3000 (41 vagões + 2 locos FIPS na cauda) espera as linhas esvaziarem e faz o mesmo encoste do primeiro lote (24 na linha 03, 12 na 04, o resto na 05); depois da linha 05 as locos FIPS são devolvidas.
- **Fluxo (TES):** `acoplar_loco_pcx` → `mv_loco_pcx_3001` → `para_lote_3000_com_loco` → `esperar_resto_3000` → `wait_resto_3000` → `rls_linha3000` (solta `linha3000_lote_em_descarga`) → `para_encoste_resto_3000` → `comecar_encoste_moegas` (mesmo encoste; sem loco na cabeça, vai direto para `para_encoste_linha03`).
- **`tentar_encostar_resto_3000(mostrar_trace)`:** solta o resto quando `lote_linha_3`, `lote_linha_4`, `lote_linha_04_fundo` e `lote_linha_5` estão null (o fundo 05 fica em outra track). Zera `lote_linha3000_emDescarga` e atualiza a faixa da 3000. Chamada no `wait_resto_3000.onEnter`, no `fim_ciclo_04e5` e no `retomar_descargas` (fim do alívio). Trace `[resto_3000] espera: linha03=.. linha04=.. fundo04=.. linha05=..`.
- **Locos FIPS:** quem segura as 2 unidades de `rp_locoFips_pcx` agora é o lote (`rechego_3000` → `usar_recurso_loco` → `acoplar_loco_pcx.in1`; `usar_recurso_loco.onExit` chama `loco_pcx_starts.inject()`). Antes eram as locos, que somem no acoplamento e levariam as unidades presas. No fim do encoste o trem que sobra (as locos) passa por `rls_rp_mnb_linha04e05`, que agora solta `rp_mnb_linha04e5` e `rp_locoFips_pcx`, e segue para `devolverLocomotiva` (`guardar_loco` → FIPS → PCX).
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/TES/Code/Functions.{xml,java}`
- **A validar:**
  - [ ] Os cortes (`tirar_24vgs`, `tirar_12vgs`, `tirar_vgs_linha05`, `decoupleFirstCars` padrão) pegam vagões e não as locos FIPS (locos no fim da lista, como a loco 42 no primeiro lote).
  - [ ] `rp_locoFips_pcx` volta a 2 livres a mais depois do `rls_rp_mnb_linha04e05`.

## ALT-047 — tes-manobra-faixas-3000
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **Problema:** `Train ... and train ... are trying to occupy the switch railwaySwitch103 simultaneously` (o erro aparece como `moegaoMar...`, mas é a RailSettings). O AMV 103 liga `pmc_linha3000_e`, `railwayTrack101` e `railwayTrack148` (entrada do TES). Em 4325 o alívio liberou `rp_mnb_linha03/04e5` e, no mesmo instante, o resto da 3000 saiu para a linha 03 e o trem de alívio voltou de ré para a 4000 pelo mesmo AMV.
- **Correção 1 (TES):** a manobra ocupa as faixas da 3000 do caminho até o terminal.
  - `seize` (antes do `ir_linha03`): `{ rp_mnb_linha03, rp_pmc_linha3000_b, _c, _d, _e }` (`main().gestorDeCirculacao`).
  - `rls_rp_mnb_linha04e05` (depois do encoste na linha 05): libera também as 4 faixas.
  - Vale para o 1º encoste e para o resto da 3000. O alívio do TES ocupa a `faixa_linha3000_c` pelo GestorDeAlivios (`primeiro_alivio_faixas`) até voltar à 4000, então os dois não entram juntos.
- **Problema 2:** em 4403 o gestor pediu alívio de 2 vazios: o overload deu 2.0 porque os 41 carregados do resto, em manobra, não estavam em nenhum lote contado.
- **Correção 2 (TES):** variável `trem_em_encoste` (gravada no `comecar_encoste_moegas`, null no `finalizar_encoste`); `_getVagoesCarregados()` soma os vagões dele.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/TES/Code/Functions.java`

## ALT-050 — tes-alivio-fundo05-linha05-vazia
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **Problema:** o trem de alívio (234) bateu no 252, vagão carregado entre ele e o lote do fundo 05 (249). Era o próprio `lote_linha_5` do 2º trem (último vagão, ainda no `wait_lote_linha05` na `railwayTrack163`). O caminho até o fundo 05 passa pela 163.
- **Correção (TES):** `tem_vagoes_linha05` = `lote_linha_05_fundo != null && wait_fundo_05.size() > 0 && lote_linha_5 == null`. Com a ALT-049 (último vagão fora da via antes do alívio entrar), o fundo 05 só é aliviado com a linha 05 vazia.
- **Arquivos:** `_alp/Agents/TES/EmbeddedObjects.xml`
- **Atenção:** o caminho até o fundo 04 pode passar pela 155/182 (lote da linha 03); ainda não testado.

## ALT-051 — tes-lote-null-depois-de-descarregar
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** o último vagão da linha 05/04 é o próprio lote; `lote_linha_5` / `lote_linha_4` só ficam null no `descarga_moega04.onExit`, depois de descarregar (antes ficavam null no `eh_ultimo_vg_05/04`, com o vagão ainda na via). Com o `tem_vagoes_linha05` exigindo `lote_linha_5 == null` (ALT-050), o alívio não vai ao fundo 05 enquanto houver vagão da linha 05 fora da moega.
- **Removida a ALT-049** (`aguarda_via_livre_alivio`, `vagao_na_via_04e5`, `alivio_no_tes` e as linhas no `sz_alivio`, `rls_alivio`, `descarga_moega04` e `proximo_vagao_04e5`); `analisar_composicao` volta a ligar direto no `sz_alivio`.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TES/Code/Functions.java`

## ALT-052 — tes-encoste-vli
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** VLI faz o mesmo encoste da Rumo. Corte na 3000: Rumo 43 carros, VLI 37 (loco 0 sai no `tem_loco_cabeca`, a loco 36 na cauda faz a manobra). Linha 03 recebe 24; linha 04 recebe 12 ou todos se sobrarem 12 ou menos (VLI: 11) e, sem vagões, a loco vai direto para o `finalizar_encoste`.
- **Acoplar locomotivas** (antigo "rechego"): bloco `rechego_3000` renomeado para `acoplar_locos_3000`. O gatilho saiu do `encoste_linha_05.onEnter` e foi para o `finalizar_encoste.onEnter` (fim da manobra, vale para Rumo e VLI). A flag `resto_com_fips` (ligada no `para_encoste_resto_3000`) evita acoplar de novo no fim do encoste do resto.
- **Blocos:** `eh_rumo` (Rumo ou VLI), `sooRumo_41vgs.nCars`, `tirar_12vgs.nCars`, novo `tem_vagoes_para_linha05` entre `tirar_12vgs` e `recuar_trocar_via_2`.
- **Pendente:** MRS (locos na cabeça) continua indo para o `para_ajuste_loco`; a manobra ainda não foi definida.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors,Variables}.xml`

## ALT-053 — tes-alivio-resto-esperando
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** deadlock na VLI: o resto da 3000 esperava as linhas 03/04/05 esvaziarem, mas o alívio dos últimos vazios nunca era forçado porque `_getVagoesCarregados()` contava o próprio resto (`lote_linha3000_emDescarga`). Agora o lote parado no `wait_resto_3000` não conta como carregado.
- **Arquivos:** `_alp/Agents/TES/Code/Functions.java`

## ALT-054 — tes-encoste-mrs
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** MRS (locos na cabeça) fazia só a chegada na 3000 (`para_ajuste_loco` sem código). Agora: `tirar_locos_mrs` solta as locos da cabeça (`devolverLocomotiva1`) e os 82 vagões vão para o `acoplar_locos_3000` (2 locos FIPS PCX na cauda). 1ª manobra: 24/12/11 nas linhas 03/04/05; os 35 que sobram voltam para o `_tes_encoste` (`voltar_3000`, `rls_volta_3000` libera manobra 04/05 e faixas da 3000, mantém as locos FIPS) e esperam no `wait_resto_3000`. 2ª manobra: 24/11.
- **3000:** com mais de 47 vagões o lote não libera a 3000 (`libera_linha3000` antes do `rls_linha3000`; `tentar_encostar_resto_3000` mantém o `lote_linha3000_emDescarga`).
- **Blocos:** novos `tirar_locos_mrs`, `para_acoplar_locos_mrs`, `libera_linha3000`, `sobrou_vagoes_linha05`, `voltar_3000`, `rls_volta_3000`, `para_esperar_resto_3000`; `tirar_vgs_linha05.nCars` = mín(11, vagões).
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/TES/Code/Functions.java`

## ALT-055 — tes-alivio-fundo05-condicao
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** o SelectOutput `tem_vagoes_linha05` (alívio do fundo 05) estava com "Use" no padrão (probabilidade), então a condição `lote_linha_5 == null` era ignorada e o alívio ia ao fundo 05 com a linha 05 ainda ocupada (colisão no 2º encoste da VLI). Agora `conditionIsProbabilistic = false`.
- **Arquivos:** `_alp/Agents/TES/EmbeddedObjects.xml`

## ALT-056 — alivio-4000-conta-vagoes
- **Data:** 2026-10-22
- **Status:** aplicada, aguardando validação
- **O que:** `condicao_4000_pode_aliviar()` usava `getTamanhoDoTrem(lote_linha_4000)` (conta as locos do trem de alívio parado na 4000). Com 80 vagões + 2 locos dava 82 = capacidade, e o terminal em overload (TES com 2 vazios, overload 2,0) nunca era aliviado, enquanto a 4000 esperava 82 vagões para sair. Agora conta só vagões (`getNumeroDeVagoes`), como `quantos_vagoes_pegar_do_terminal` e `linha_4000_cheia`. Vale para T39, ADM e TES.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`

## ALT-057 — suzano-encoste-descarga
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando validação
- **O que:** encoste e descarga da Suzano. O trem (1 loco + 64 vagões) avança até `_suzano_encoste` (7000_d) e o `rmv_40vgs` (antigo `rmv_60vgs`) tira os 40 de trás. A loco solta os 24 que ficaram com ela (`deixar_24vgs_L7000`, `lote_L7000`) e vai para o `devolver_loco_ferrovia`. Os 40 vão para L1..L4, 10 em cada (`colocar_L*` → `deixar_lote_L*`), recuando até `_suzano_entrada_terminal` entre uma linha e outra (`recuar_L*`, as linhas são paralelas). A L4 recebe os 10 que sobram, sem decouple. Descarga como na Bracell: L1..L3 esperam nos `aguarda_L4a/b/c`, o `descarregar_L4` solta os três e as 4 linhas descarregam juntas; cada lote vira `loteVazios_L*` e `lote_carregados_L*` fica null.
- **Trem de teste:** 65 carros (era 64) e `setTerminal(main().suzano)` (era costaLeite).
- **Funções:** `_getVagoesVazios` (L1..L4) e `_getVagoesCarregados` (L1..L4 + `lote_L7000`) reativadas. `getOverloadVazios` continua 0: o alívio com a loco FIPS fica para a ALT-058.
- **Variáveis:** `lote_carregados_L1..L4`, `loteVazios_L1..L4`, `lote_L7000`.
- **Pendente:** o lote de 40 anda sem loco e não licencia a 7000 nem a 6000_a ao recuar (o Tgrao usa as faixas 7000_a..d); os `aguarda_L4*` não voltam a bloquear depois do 1º trem (igual à Bracell); `para_novo_lote1`, `para_novo_lote2`, `comecar_encoste1`, `trainMoveTo` e `para_atendimento_terminal` ficaram soltos.
- **Próxima (ALT-058):** a loco FIPS troca os 24 da L7000 com os vazios de L1, L2 e L3 (10 + 10 + 4), espera descarregar e sai com os 64 até as DVs.
- **Arquivos:** `_alp/Agents/Suzano/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/Suzano/Code/Functions.java`

## ALT-058 — suzano-overload
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando validação
- **O que:** `getOverloadVazios()` da Suzano devolve 2,0 quando `descarregou_tudo` = true e 0 antes disso. A variável nova `descarregou_tudo` (boolean, false) vai ser ligada no fim da troca dos 24 (próxima ALT). Hoje o overload fica sempre 0. O gestor que chama a loco do alívio fica com o Guilherme.
- **Arquivos:** `_alp/Agents/Suzano/Variables.xml`, `_alp/Agents/Suzano/Code/Functions.java`

## ALT-059 — suzano-troca
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando validação
- **O que:** troca dos 24 carregados da L7000, completando o fluxo que o Guilherme montou (`loco_pcx_starts` → … → `deixar_lote_L6`).
- **Chamada da loco:** cada linha que termina a 1a descarga (40) chama `registrar_linha_descarregada()`. Na 4a, liga `em_troca`, bloqueia os `aguarda_L4a/b/c` e faz `loco_pcx_starts.inject()`.
- **Fim da descarga das linhas 1..3:** novo Exit `fim_descarga_L*`. Na 1a rodada, os vazios vão para `aguardar_troca_L*` → `trainCouple*.in2`. Na 2a, viram `loteVazios_L*`. `lote_carregados_L*` fica null nas duas.
- **24 da L7000:** voltam a passar pelo `para_lote_L7000` (grava `lote_L7000`) → `aguardar_L7000` → `trainCouple.in2`; o `trainCouple` zera `lote_L7000`.
- **Linha 3:** `colocar_L5` sai pelo `outHit` (como linhas 1 e 2); `deixar_lote_L6` deixa 4 vagões (eram 10).
- **30 vazios:** o Exit `exit` virou `para_vazios_L7000` e grava `loteVazios_L7000` (entra em `_getVagoesVazios`).
- **2a descarga:** `para_descarregar_L7` (4 na L3, último) solta os holds e as 3 linhas descarregam juntas. `verificar_descarregou_tudo()` liga `descarregou_tudo` (overload 2,0), desliga `em_troca` e bloqueia os holds de novo.
- **Loco FIPS:** no fim, `liberar_loco_fips` (solta o `rp_locoFips_pcx`) → `devolver_loco_fips`.
- **Variáveis:** `em_troca`, `linhas_descarregadas`, `loteVazios_L7000`.
- **Pendente:** zerar `descarregou_tudo` e os `loteVazios_*` no alívio (gestor do Guilherme); a loco FIPS anda sem licença de faixa (`trainEnter` em `pos_locoAlivio_PMC`).
- **Arquivos:** `_alp/Agents/Suzano/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/Suzano/Code/Functions.{java,xml}`

## ALT-060 — suzano-troca-24-vazios
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando validação
- **O que:** na troca, os 30 vazios deixados no `_suzano_encoste` não cabiam antes do `railwaySwitch55` (rodada de 23/10 parou no `moverTremCompleto_Ponto1` com "no straight route"). Agora a L7000 recebe só 24 vazios (`deixar_30vgs_L7001` → `deixar_24vazios_L7000`, nCars = 24). O trem volta ao terminal com 30 vagões (6 vazios + 24 carregados) e distribui 10/10/10: L1 fica com 6 vazios + 4 carregados, L2 e L3 com 10 carregados (`deixar_lote_L6` volta para 10). No fim: 24 vazios na L7000 + 10 em cada linha = 64.
- **Obs.:** durante a 2a descarga, `_getVagoesCarregados` conta os 6 vazios da L1 como carregados (conta carros do lote).
- **Arquivos:** `_alp/Agents/Suzano/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/Suzano/Code/Functions.java`

## ALT-061 — alivio-pmc
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando compilação e validação
- **O que:** o alívio de celulose (ALT-014) vira o alívio do pátio PMC: Bracell, Suzano e Eldorado. Continua com a loco da ferrovia em atendimento, sem depender das DVs, saindo com todos os vazios do terminal.
- **Continuação da renomeação feita no AnyLogic** (`analisar_pmc`, `fazer_procedimento_de_alivio_pmc`, `terminais_pmc`), que tinha deixado chamadas para `fazer_procedimento_de_alivio_celulose` e `terminal_alivio_celulose` (variável apagada).
- **GestorDeAlivios:**
  - Variável nova `terminal_alivio_pmc` (trava: um alívio do PMC por vez). `analisar_pmc` para o laço quando a trava está ocupada.
  - Blocos renomeados: `loco_alivio_celulose` → `loco_alivio_pmc`, `trainEnter_celulose` → `trainEnter_pmc`, `ir_alivio_celulose` → `ir_alivio_pmc`, `para_alivio_terminal_celulose` → `para_alivio_terminal_pmc`. `ocupar_linha5000_6000` mantido.
  - Função nova `retornar_alivio_pmc(Trem)`: solta a trava, devolve o terminal para `terminais_pmc`, marca `eh_alivio` e faz `gestorDeCirculacao.tentar_usar_faixa.take`. O terminal só define `faixa_atual`/`proxima_faixa` antes de chamar.
  - Traces `[AlivioCelulose]` → `[AlivioPMC]`.
- **Bracell:** `para_circVzs` define a saída (`intervalo4000_c → b`) e chama `retornar_alivio_pmc`. `getPatio()` = "pmc".
- **Suzano:** `getPatio()` = "pmc". Ainda **fora** de `terminais_pmc`: `takeAlivio` está comentado e `get_pos_alivio()` devolve `_bracell_alivio`. Se entrar agora, a loco some no `takeAlivio`, a trava nunca solta e a Bracell para de aliviar.
- **Pendente:**
  - Suzano: posição de alívio própria, fluxo de coleta (L1..L4 + L7000), saída chamando `retornar_alivio_pmc`, zerar `descarregou_tudo`, desbloquear `aguarda_L4*`; depois entrar em `terminais_pmc`.
  - Eldorado: criar o agente com a mesma interface e entrar em `terminais_pmc`.
  - `ocupar_linha5000_6000` é da Bracell; Suzano e Eldorado podem precisar de outras linhas.
  - Deadlock na `intervalo4000_c` entre o alívio e um 2º trem carregado (`podeReceberNovoLote()` da Bracell sempre true).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.{java,xml}`, `_alp/Agents/Bracell/EmbeddedObjects.xml`, `_alp/Agents/Bracell/Code/Functions.java`, `_alp/Agents/Suzano/Code/Functions.java`, `docs/gestores.md`, `CLAUDE.md`

## ALT-062 — suzano-alivio
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando validação
- **O que:** alívio da Suzano no alívio PMC (ALT-061). A loco da ferrovia chega em `pos_locoAlivio_PMC` (`get_pos_alivio()`), `takeAlivio` → `iniciar_alivio`, que põe `loteVazios_L1..L4` e `loteVazios_L7000` nos `alivio_vz_*` (in2 dos acoplamentos).
- **Fluxo:** `liberar_linha5000_6000_alivio` (a loco chega com a 5000/6000 do `ocupar_linha5000_6000`) → para L1, L2, L3, L4 e L7000: `recuar_alivio_*` (até `_suzano_entrada_terminal`) → `avancar_alivio_*` (`_suzano_linha0*` / `_suzano_encoste`) → `outHit` → `acoplar_alivio_*` (zera o `loteVazios_*`). No fim, `para_sair_alivio`: `descarregou_tudo = false`, `faixa_atual = linha6000_a`, `proxima_faixa = marginal_c`, `gestorDeAlivios.retornar_alivio_pmc(trem)`.
- **GestorDeAlivios:** `main().suzano` entra em `terminais_pmc`.
- **A conferir:** caminho da loco de `circ_pos_inicio_L5K` até `pos_locoAlivio_PMC` (na `pst_silosMuro_a`) e daí até a Suzano, sem licença de faixa; trem de 64 vazios + loco (≈ 1.140 m) ao recuar e ao sair pela `linha6000_a`.
- **Arquivos:** `_alp/Agents/Suzano/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/Suzano/Code/Functions.java`, `_alp/Agents/GestorDeAlivios/Variables.xml`

## ALT-063 — alivio-pmc-faixas
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando validação
- **O que:** a loco do alívio PMC (Suzano, Bracell e Eldorado) nasce em `pos_locoAlivio_PMC` (`trainEnter_pmc`; antes nascia no próprio `get_pos_alivio()`) e licencia as faixas do terminal até o ponto de alívio.
- **GestorDeAlivios:** Seize novo `bloquear_faixas_pmc` entre `ocupar_linha5000_6000` e `trainEnter_pmc`, com `get_conjunto_faixas_pmc()` (faixas de `get_faixas_primeiro_alivio()` do terminal) e `ocuparFaixas` na saída. Lista vazia só gera trace `[AlivioPMC][bloquear_faixas_pmc]` (caso da Bracell hoje).
- **Suzano:** `get_pos_alivio()` = `_suzano_alivio` (7000_c); `get_faixas_primeiro_alivio()` devolve `primeiro_alivio_faixas` = `faixa_silosMuro_a`, `faixa_marginal_c`, `faixa_linha6000_a` (o inicializador usava `pst_marginal_c`/`pmc_linha6000_a`, que não são faixas do gestor). `liberar_linha5000_6000_alivio` → `liberar_recursos_loco_alivio`: solta a 5000/6000 e as faixas (`get_recursos_loco_alivio`) e faz `liberar()` nelas.
- **A conferir:** Seize com conjunto vazio na Bracell; a loco da Bracell agora anda de `pos_locoAlivio_PMC` até `_bracell_alivio` sem faixas.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.{java,xml}`, `_alp/Agents/Suzano/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/Suzano/Code/Functions.{java,xml}`

## ALT-064 — suzano-liberar-faixas
- **Data:** 2026-10-23
- **Status:** aplicada, aguardando validação
- **O que:** a Suzano não soltava `via3_c`, `silosMuro_a`, `marginal_c` e `linha6000_a`. O `liberar_faixas_circulacao` estava depois do `colocar_L4`, onde o agente é o lote de 10 (trem novo dos decouples): as faixas têm o id do trem original, que também é o dono das unidades, então o Release voltava vazio. Na posição antiga (depois do `mov_avnc__L7000`), a `linha6000_a` não saía porque a cauda dos 40 (≈ 50 m) ainda estava nela, e a loco levava a unidade para o GestorDeLocomotivas.
- **Correção:** `liberar_faixas_circulacao` volta para `mov_avnc__L7000` → `rmv_40vgs` (solta via3_c, silosMuro_a e marginal_c). A loco, depois do `deixar_24vgs_L7000`, espera no Hold `aguardar_lote_sair_6000_a` (solto no `colocar_L1.onEnter`, com o lote já na entrada) e passa pelo Release `liberar_faixas_loco` (solta a linha6000_a e rebloqueia o Hold) antes do `devolver_loco_ferrovia`.
- **Loco FIPS duplicada:** o `fazer_inversao.onExit` também fazia `loco_pcx_starts.inject()`, além do `registrar_linha_descarregada()` (saíam 2 locos). Ficou só no `registrar_linha_descarregada()`.
- **Arquivos:** `_alp/Agents/Suzano/{EmbeddedObjects,Connectors}.xml`
## ALT-065 — coperxii-moega-6-em-6
- **Data:** 2026-10-27
- **Status:** aplicada, aguardando compilação e validação
- **O que:** nova lógica de descarga do CoperXII (agente `CoperXXI`), montada nas linhas Enter/Exit que o Guilherme deixou no diagrama.
  - Chegada: `comecar_encoste` → `ir_encoste` (`_coperXII_encoste`) → `liberar_faixas_xii` → `cortar_lotes` corta antes da 2ª loco. Lote 1 (loco + x) segue; lote 2 (loco + x) espera em `aguarda_lote_anterior`.
  - Ciclo da moega (um lote por vez, `seize_moega` em `rp_moega`): `tem_espaco_vazios` → `ir_moega` (`_coperXII_moega`, `definir_grupo`) → `descarga_grupo` (45 s, descarrega os vagões da frente) → `cortar_grupo` (loco + 6 no 1º grupo do lote, 6 nos seguintes). O resto espera em `aguarda_grupo` até o grupo chegar na linha de vazios.
  - Vazios: `_coperXXI_lote_b` (21) primeiro, depois `_coperXXI_lote_a` (20). No fim da linha o grupo é menor (só o que cabe). O grupo acopla no estoque da linha (`estoque_b/a`, Hold bloqueado) por `acoplar_b/a`. Loco na ponta é cortada e vai para `devolver_loco`.
  - O último grupo do lote é o trem que segurou `rp_moega`: ao chegar, `liberar_moega` solta a moega e o `aguarda_lote_anterior`.
  - Linhas cheias: o lote espera em `aguarda_espaco_vazios`; `liberar_espaco_vazios()` deve ser chamada pelo alívio (GestorDeAlivios, a integrar).
  - Trens de teste: 84 carros com loco no meio — Rumo 1+41+1+41, MRS 1+26+1+56, VLI 1+38+1+44.
  - Novas variáveis: `lote_vazios_a/b`, `_grupo_linha`, `_grupo_vagoes`, `_grupo_carros`, `CAPACIDADE_LOTE_A` (20), `CAPACIDADE_LOTE_B` (21), `QTD_VAGOES_GRUPO` (6). `_getVagosVazios`/`getOverloadVazios` passam a usar os novos lotes.
  - Removidas as linhas Enter/Exit de rascunho não usadas (`comecar_encoste5..8`, `para_encostar_moega4..7`, `descarregaLote1/2`). O fluxo antigo (atendimento/rechego/AouB) ficou no diagrama, sem entrada.
- **Pendências:** quando o 1º grupo de um lote (com loco na frente) entra numa linha que já tem vazios, a loco fica no meio do lote (trace `⚠️`). Acontece com MRS e VLI no lote 2. Alívio do CoperXII ainda não integrado.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{xml,java}`
## ALT-066 — coperxii-stackoverflow-e-resto-na-moega
- **Data:** 2026-10-27
- **Status:** aplicada, aguardando compilação e validação
- **StackOverflow:** com `lote_a` e `lote_b` cheios, o trem entrava em loop `tem_espaco_vazios` ↔ `aguarda_espaco_vazios`. O `block()` estava no `onExitFalse` do SelectOutput, que roda depois do agente já ter passado pelo Hold. Agora o Hold é fechado no `onEnter` do SelectOutput `moega_livre`, antes do trem chegar nele.
- **Resto na moega:** com as linhas cheias, o resto do lote vai à moega (`ir_moega_resto`), descarrega tudo até a próxima loco (`descarga_resto`), solta `rp_moega` e o lote seguinte (`liberar_moega_resto`) e fica no Hold `estoque_moega` (`lote_vazios_moega`). O lote seguinte espera em `aguarda_espaco_vazios` até o alívio.
- **Alívio (preparação):** `getOverloadVazios()` passa a 2 quando as linhas estão cheias e há lote na moega; `get_quantidade_vazios()` soma lote_b + lote_a + moega. O CoperXXI ainda não está em `terminais_pst` (falta definir rota da loco e saída).
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.java`
## ALT-067 — coperxii-stackoverflow-ciclo
- **Data:** 2026-10-27
- **Status:** aplicada, aguardando validação
- **O que:** o StackOverflow continuava quando o lote 2 encontrava as linhas e a moega cheias. A causa real é o ciclo direto de portas `tem_espaco_vazios` → `moega_livre` → `aguarda_espaco_vazios` → `tem_espaco_vazios`: o AnyLogic fica propagando "agente pronto" pelo ciclo sem fim (`forwardReadyEntityNotification`). A volta do Hold agora passa pelo Exit `para_reavaliar_espaco` → `proximo_grupo.take(agent)`, que quebra o ciclo de portas.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors}.xml`
## ALT-068 — coperxii-rota-loco-alivio
- **Data:** 2026-10-27
- **Status:** aplicada, aguardando validação
- **O que:** a loco do alívio dava `The track via1_intermar_d is not directly connected to the next track in the route: psnSaidaCopexXXI_b`. A rota do `ir_entrada_terminal_pst` (lista de linhas) é faixa de criação + `get_faixas_primeiro_alivio()` + linha do `get_pos_alivio()`, e o CoperXXI devolvia lista vazia.
  - `get_faixas_primeiro_alivio()` devolve faixas só de rota para `linhaPaqueta_a` e `linhaCostaLeita_a` (caminho pelos AMVs: via1_intermar_d → linhaPaqueta_a → linhaCostaLeita_a → psnSaidaCopexXXI_b). Sem recurso (não licenciam nada) e com `linhaTMDS` invisível.
  - `get_pos_alivio()` passa a `_coperXXI_lote_b`: a loco para antes da cabeça do lote_b (antes era `_coperXXI_lote_a`, atrás do lote_b, e a loco bateria nele).
- **Risco:** a loco passa pela Paquetá e pela Costa Leite sem licenciar essas linhas.
- **Arquivos:** `_alp/Agents/CoperXXI/Code/Functions.java`
## ALT-069 — coperxii-engate-alivio
- **Data:** 2026-10-27
- **Status:** aplicada, aguardando validação
- **O que:** a loco do alívio engata os três lotes de vazios usando os couples que já existem.
  - `iniciar_alivio` → `alivio_seguir(trem)`: engata `lote_b` → `lote_a` → moega (do lado da saída para dentro).
  - `engatar_b`/`engatar_a` (antigos `primeiroLote`/`segundoLote`) e `engatar_moega` seguem em frente mirando `_coperXII_moega`; `out` e `outHit` vão para o `in1` do `acoplar_b`/`acoplar_a`/`acoplar_moega` (novo). O `onEnter1` solta o Hold do estoque, que entra pelo `in2`. Sai do couple o trem do `in1` (a loco do alívio, com os recursos do gestor).
  - `para_chegou_vazios` desvia para `alivio_seguir` quando `etapa_alivio` é "b" ou "a" e zera o lote engatado; `para_moega_engatada` zera `lote_vazios_moega`.
  - `fim_alivio` → `retornar_gestorDeAlivios`. O próximo lote só é solto quando o trem do alívio sai da `psnSaidaCopexXXI_b` (`necessitaDeAlivio`, evento `verificaSePrecisaDeAlívio`).
  - Removidos `trainCouple`, `trainCouple1`, `alivio_a`, `alivio_b`. Novas variáveis `etapa_alivio`, `trem_alivio_saindo`.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{xml,java}`
## ALT-070 — coperxii-segundo-lote
- **Data:** 2026-10-27
- **Status:** aplicada, aguardando validação
- **O que:** o lote 2 fica guardado em `segundo_lote` (onEnter do `aguarda_lote_anterior`, no encoste) e começa pela função `iniciar_segundo_lote()` (se tiver vagões carregados, zera a variável e abre o Hold). Sem evento.
  - Lote 1 terminou com espaço nas linhas: `liberar_moega` chama `iniciar_segundo_lote()`.
  - Linhas cheias (resto na moega): `liberar_moega_resto` não solta mais o lote 2.
  - Alívio: `retornar_gestorDeAlivios` desliga `fazendoAlivio` e chama `iniciar_segundo_lote()`.
  - Removida a checagem pelo evento `verificaSePrecisaDeAlívio` (ALT-069).
- **Risco:** o lote 2 sai do encoste com o trem do alívio ainda na `psnSaidaCopexXXI_b`; se o alívio demorar a sair, o lote 2 pode alcançá-lo no `ir_moega` (sem saída de batida).
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{xml,java}`
## ALT-071 — coperxii-alivio-todo-lote
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **Bug (ALT-070):** o lote 2 nunca começava. O `cortar_lotes` bloqueia o Hold `aguarda_lote_anterior` antes do lote 2 sair; Hold bloqueado não deixa o agente entrar, então o `onEnter` que gravava `segundo_lote` não rodava e `iniciar_segundo_lote()` achava `null`. Agora `segundo_lote = train` é gravado no `onEnter` do `cortar_lotes` (o trem original sai pelo `out` e é o lote 2).
- **Alívio no fim de todo lote:** nova variável `aguardando_alivio`, ligada no `liberar_moega` (último grupo chegou) e no `liberar_moega_resto` (linhas cheias). Com ela, `getOverloadVazios()` devolve 2. O `liberar_moega` não solta mais o lote 2; só o `retornar_gestorDeAlivios` (que desliga `aguardando_alivio`). Resolve a MRS (lote 1 de 26 não enchia as linhas e o lote 2 entrava com a loco numa linha ocupada).
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.java`
## ALT-072 — coperxii-limpeza-fluxo-antigo
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando compilação
- **O que:** remove do agente `CoperXXI` tudo que não é usado pelo fluxo novo (conferido por ligações e código).
  - **Blocos (39):** atendimento, aguardaDescarga, tiraLote, paraDescarga, descarga, entraNaMoega, descarregaLote, paraSelecionarLinhaAlivio, selecionarLinhaAlivio, AouB, paraAcumular_a, paraAcumular_b, acumularLote_a, irParaLote_a, acumualrLote_a, tirarExcesso, paraNaoBugar, acabouDeAcumular, esperarAlivio_a, recuar, paraAcumularLote_b1, acumularLote_b, acumularLote_b1, irParaLote_b, acumualrLote_b, paraNaoBugar1, acabouDeAcumular1, esperarAlivio_b, paraAnalisaRechego, analisaRechego, liberar_faixas_circulacao (o antigo), precisaRechego, paraAtendimento, paraAtendimento1, paraFazerRechego, fazRechego, aguardaRechego, fazerRechego, rp_mnb_vazios — e as conexões entre eles.
  - **Funções (10):** _selecionarTamanhoDoLote, _estaDisponivelAlivio, _estaDisponivelMoega, necessitaDeAlivio, _contarVagoes, _espacoRechegoDisponivel, _precisaDeRechego, formouLoteMinimoDeAlivio, ultimosVagoes, _esteÉoUltimoLote.
  - **Eventos (3):** _verificaSePodeDescarregar, verificaSePrecisaDeAlívio, _verificaSePodeFazerRechego (`Events.xml`/`Events.java` ficam vazios).
  - **Variáveis (12) e parâmetros (3):** lote_descarga, _tamanhoDoLote, _loteVazios_a, _loteVazios_b, contadorParaRechego, fazendoRechego, colocandoLoteNaMoega, _tamanhoLotePera, valorNecessarioParaRechego, loteNaPera, lote_carregados, trem_alivio_saindo; CAPACIDADE_ALIVIO_A, CAPACIDADE_ALIVIO_B, _CAPACIDADE_LOTE_MOEGA.
  - **Tela (`text56`):** mostrava `loteNaPera` + `lote_descarga`; passa a mostrar os carregados do `segundo_lote`.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/{Functions,Events}.{xml,java}`, `_alp/Agents/CoperXXI/Levels/Level.level.xml`
## ALT-073 — coperxii-loco-empurra-lote
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **O que:** o lote só anda até a moega com loco.
  - `segundo_lote` → `lote_segunda_parte`; nova `lote_em_descarga` (gravada no `entrar_moega`). No fim de cada lote (`liberar_moega`/`liberar_moega_resto`), `trocar_lote_em_descarga()`: `lote_em_descarga = lote_segunda_parte`, `lote_segunda_parte = null`. O `retornar_gestorDeAlivios` chama `iniciar_proximo_lote()` (antiga `iniciar_segundo_lote`).
  - A segunda parte espera no Wait `espera_segunda_parte` (substitui o Hold `aguarda_lote_anterior`), que tem `free()`. `sair_espera` manda para `entrar_tirar_loco` ou `entrar_moega` conforme `destino_segunda_parte`.
  - Antes de toda ida à moega (`tem_loco_lote`, depois do `seize_moega`/`proximo_grupo`): sem loco, se a segunda parte tem loco na cabeça, `pedir_loco_segunda_parte()` corta essa loco (`tirar_loco_segunda`), que vai até a cauda do lote (`levar_loco`) e acopla (`acoplar_loco`, lote no `in1`). Sem segunda parte, o lote pega uma unidade de `rp_locoFips_pst` (`seize_loco_fips`) e o `loco_fips` cria a loco em `pos_locoManobra_PST` (`levar_loco_fips`).
  - `definir_grupo()`: quando o grupo leva todos os carregados, leva o lote inteiro (inclui a loco da cauda), que é devolvida na linha de vazios.
  - Resto na moega: `tem_loco_resto`/`tirar_loco_resto` tiram a loco da cauda antes de virar lote de vazios.
  - `liberar_moega`/`liberar_moega_resto` soltam `recursos_do_lote()` (rp_moega + unidade FIPS, se houver).
  - `text56` passa a mostrar `lote_segunda_parte`.
- **Riscos:** rota da loco FIPS (ROUTE_AUTO) e da loco da segunda parte sem licenciar faixas; sentido da loco FIPS criada não verificado.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{xml,java}`, `_alp/Agents/CoperXXI/Levels/Level.level.xml`
## ALT-074 — coperxii-rota-loco-fips
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **O que:** a loco FIPS PST dava `There is no straight route from pst_marginal_a ... to psnSaidaCopexXXI_b` (nascia em `pos_locoManobra_PST` e saía para o fim da pst_marginal_a). Sobre a edição do Guilherme (loco nasce em `pos_locoAlivio_PST`, `caminho_pera_bloquear` → `trainEnter`):
  - `levar_loco_fips` anda por lista de linhas: via1_intermar_d → linhaPaqueta_a → psnSaidaCopexXXI_a → pstSaidaMoegao → railwayTrack66 → via2_b → via2_c → track_entrada_pera → railwayTrack29 → pstL3 → psnSaidaCopexXXI_b (encosta na cauda do lote no encoste).
  - `caminho_pera_bloquear` licencia as faixas do caminho que existem no GestorDeCirculacao (via1_intermar_d, via2_b, via2_c, entrada_pera) e faz `bloquear()`.
  - `liberar_caminho_pera` (novo, ALL_FROM_SEIZES) solta as faixas e faz `liberar()` ao encostar, antes do `acoplar_loco` (a loco some no acoplamento).
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors}.xml`
## ALT-075 — coperxii-loco-fips-pos-pera
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **O que:** o Guilherme passou a criar a loco FIPS em `pos_loco_pera` (via2_c, offset 50) e a licenciar só via2_c e entrada_pera. A rota do `levar_loco_fips` ainda começava na via1_intermar_d (`The first track in the route must be the current track`).
  - Rota: via2_c → track_entrada_pera → railwayTrack29 → pstL3 → psnSaidaCopexXXI_b.
  - `liberar_caminho_pera` solta só via2_c e entrada_pera (antes liberava também via1_intermar_d e via2_b, que podiam estar com outro trem).
  - Trace do `loco_fips` atualizado.
- **Arquivos:** `_alp/Agents/CoperXXI/EmbeddedObjects.xml`
## ALT-076 — coperxii-alivio-41
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **O que:** todo alívio leva `QTD_ALIVIO` = 41 vagões: lote_b (22) + lote_a (13) + lote da moega (`get_capacidade_moega()` = 41 − 22 − 13 = 6). MRS e VLI completam o 1º alívio com parte do 2º lote.
  - A moega vira o 3º destino do grupo (`definir_grupo`: b → a → "moega"); `get_espaco_vazios()` soma b + a + moega.
  - Grupo da moega (`para_vazios` → `grupo_na_moega`): tira a loco da cauda se houver (`tem_loco_resto`/`tirar_loco_resto`), acopla no lote da moega se já existe (`moega_tem_lote` → `acoplar_moega`; `para_moega_engatada` separa alívio de grupo) e é guardado (`moega_tem_rp` → `liberar_moega_resto` se for o último grupo → `para_estoque_moega`).
  - `ir_moega.outHit` também conta como chegada (o lote encosta no lote da moega).
  - Alívio pedido por `verificar_alivio()` quando os 41 estão prontos, e não mais no fim do lote. No fim do lote, a segunda parte começa logo se ainda faltam vazios; senão espera o alívio. O resto carregado espera onde está (`aguarda_espaco_vazios`).
  - Removidos `moega_livre`, `ir_moega_resto`, `liberar_faixas_circulacao3`, `descarga_resto`.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{xml,java}`
## ALT-077 — coperxii-contador-vazios
- **Data:** 2026-10-29
- **Status:** aplicada
- **O que:** a tela mostrava -6 vazios no início. `_getVagosVazios()` calculava `(22 + 13) - get_espaco_vazios()`, e na ALT-076 o espaço passou a incluir os 6 do lote da moega. Agora conta direto os vagões de `lote_vazios_b` + `lote_vazios_a`.
- **Arquivos:** `_alp/Agents/CoperXXI/Code/Functions.java`
## ALT-078 — coperxii-layout
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **O que:** reorganização visual do fluxograma do CoperXXI, sem mudar a lógica.
  - 5 colunas com título: 1. Chegada, 2. Loco do lote, 3. Moega, 4. Vazios, 5. Alívio. Cada etapa tem subtítulo e linha própria.
  - Ligações longas entre etapas trocadas por pares Exit/Enter (`para_dividir_lotes`/`dividir_lotes`, `para_espera_segunda_parte`, `para_buscar_loco`/`entrar_espera_loco`, `para_lote_com_loco`/`lote_com_loco`, `para_acoplar_loco_segunda`/`para_acoplar_loco_fips`/`chegou_loco`, `para_aguardar_alivio`/`entrar_aguarda_alivio`, `para_acoplar_b/a/moega`/`entrar_acoplar_b/a/moega`).
  - `devolver_loco_moega` (novo DevolverLocomotiva) para a loco tirada do grupo da moega.
  - Conectores refeitos em ângulo reto. Rótulos antigos fora da tela removidos.
  - Painel de variáveis e funções agrupadas abaixo do fluxograma (y ≈ 1450).
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.xml`, `_alp/Agents/CoperXXI/Levels/Level.level.xml`
## ALT-079 — moegaomar-logica-coperxii
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **O que:** o MoegaoMar recebeu a lógica do CoperXXI (cópia do fluxograma). Ajustes para ele funcionar como terminal próprio:
  - `AOC.MoegaoMar.xml`: passa a implementar `ITerminalMoega`.
  - Destinos: `ir_encoste` → `_moegaoMar_encoste` (railwayTrack40); `ir_moega`, `levar_loco`, `levar_loco_fips`, `engatar_b/a/moega` → `_moegaoMar_moega`; `ir_lote_b` → `_moegaoMar_lote_b` (pstLinha04); `ir_lote_a` → `_moegaoMar_lote_a` (pstMoegao_LinhaMeio).
  - Rota da loco FIPS: via2_c → track_entrada_pera → railwayTrack29 → pstL5 → railwayTrack40 → pstMoegao_LinhaMeio.
  - Alívio: `get_pos_alivio()` = `_moegaoMar_lote_b`; `get_faixas_primeiro_alivio()` = linhaPaqueta_a, psnSaidaCopexXXI_a, pstSaidaMoegao.
  - Trens de teste do agente apontam para `main().moegaoMar`.
  - GestorDeCirculacao `para_coperXXI`: a pera manda para `moegaoMar.comecar_encoste` quando `get_terminal_name()` é "moegaoMar"; senão, CoperXXI.
  - GestorDeAlivios: `main().moegaoMar` incluído em `terminais_pst`.
- **Arquivos:** `_alp/Agents/MoegaoMar/{AOC.MoegaoMar,EmbeddedObjects}.xml`, `_alp/Agents/MoegaoMar/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Variables.xml`
## ALT-080 — moegaoterra-logica-pera
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **O que:** o MoegaoTerra recebeu a lógica de terminal da pera (cópia do MoegaoMar). Ajustes:
  - Destinos: `_moegaoMar_*` → `_moegaoTerra_*` (encoste na pstL7; moega e lote_a na railwayTrack36; lote_b na railwayTrack15).
  - Trens de teste do agente apontam para `main().moegaoTerra`.
  - Rota da loco FIPS: via2_c → track_entrada_pera → pstL7 → railwayTrack36.
  - Alívio: `get_pos_alivio()` = `_moegaoTerra_lote_b`; faixas do 1º alívio iguais às do MoegaoMar (linhaPaqueta_a, psnSaidaCopexXXI_a, pstSaidaMoegao).
  - Removida a função duplicada `main2()`; traces com prefixo `[MoegaoTerra]`.
  - GestorDeCirculacao `para_coperXXI`: a pera manda para `moegaoTerra.comecar_encoste` quando `get_terminal_name()` é "moegaoTerra".
- **Arquivos:** `_alp/Agents/MoegaoTerra/EmbeddedObjects.xml`, `_alp/Agents/MoegaoTerra/Code/{Functions.java,Functions.xml}`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`
## ALT-081 — linha4000-saida-trem-cheio
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **Problema:** `The car being added on the track 'pmc_intervalo4000_b' intersects with another car` no `loco_pcx_starts`. O trem cheio (82 vagões) saiu às 7339 s; às 7408 s o ADM entrou em overload e o gestor criou as 2 locos FIPS em `pos_linha_4000_criar_loco` (intervalo4000_b) com a cauda do trem ainda lá.
- **Causa raiz:** o trem sai com `faixa_atual = intervalo4000_a`; a intervalo4000_b/c e a linha 4000 ficavam sem licença. O `liberar_linha_4000` liberava as faixas da 4000 e o `para_alivio_na_linha_4000` zerava `lote_linha_4000`, então o `analisar_pcx` via a 4000 livre. O `bloquear_4000` só pega a linha 4000, não a intervalo4000_b.
- **Alterações (GestorDeAlivios):**
  - Novas funções `get_faixas_saida_4000()` (intervalo4000_b/c + linha4000_a/b/c), `saida_4000_livre()` e `ocupar_faixas_saida_4000(Trem)`.
  - `para_alivio_na_linha_4000.onExit`: chama `ocupar_faixas_saida_4000(trem)`; as faixas em que o trem está ficam `OCUPADA` com o id dele, as outras `LIVRE`. O `liberar_faixas_ocupadas` do GestorDeCirculacao solta cada uma quando a track esvazia.
  - `liberar_linha_4000.onExit`: deixa de liberar as faixas da linha 4000 (o Release dos recursos continua).
  - `condicao_4000_pode_aliviar()`: sem trem na 4000 (`lote_linha_4000 == null`), só alivia se `saida_4000_livre()`.
- **Traces:** `[Alivio4000][ocupar_faixas_saida_4000]` e `[Alivio4000][saida_4000_livre]` (a cada 60 s enquanto espera).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Code/{Functions.java,Functions.xml}`
## ALT-082 — linha4000-compatibilidade-ferrovia
- **Data:** 2026-10-29
- **Status:** aplicada, aguardando validação
- **Regra:** o trem da linha 4000 junta vazios de TES, ADM e T39 desde que sejam da mesma ferrovia e do mesmo segmento.
- **Problema:** `condicao_4000_eh_compativel_com_terminal` existia, mas `ferrovia_na_linha_4000` e `segmento_na_linha_4000` nunca recebiam valor (só eram zeradas). A condição devolvia sempre `true`. Além disso, as Strings eram comparadas com `==`.
- **Alterações (GestorDeAlivios):**
  - `colocar_terminal_em_alivio`: se `ferrovia_na_linha_4000 == null`, grava `getFerrovia()` e `getSegmento()` do 1º terminal do trem.
  - `condicao_4000_eh_compativel_com_terminal`: `java.util.Objects.equals` no lugar de `==`.
  - Terminal incompatível continua em `terminais_pcx` e espera o trem atual sair cheio (o `para_alivio_na_linha_4000` zera as variáveis).
- **Traces:** `[Alivio4000][compatibilidade]` quando o trem é definido e a cada 60 s para terminal em espera.
- **Riscos conhecidos:** trem incompleto fica na 4000 até um terminal da mesma ferrovia entrar em overload; `getFerrovia()` dos terminais devolve a ferrovia em atendimento, não necessariamente a dos vazios.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`
