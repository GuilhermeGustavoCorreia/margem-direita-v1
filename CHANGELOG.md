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
## ALT-083 — interface-leitura-arquivos
- **Data:** 2026-11-03
- **Status:** validada em 2026-11-05 (12 terminais e 1 combinação lidos sem erro)
- **Objetivo:** etapa 1 da interface HTML (`simulacao/interface_simulacao.html`). O modelo lê os arquivos gerados pela interface e mostra no log o que entendeu. Nada muda no comportamento da simulação.
- **Alterações (Main):**
  - Parâmetro `pasta_simulacao` (String, padrão `"simulacao"`, relativo à pasta do modelo).
  - Variáveis `duracao_h`, `warmup_h` (double) e `replicacoes` (int), preenchidas a partir de `config/simulacao.csv`. Ainda não são aplicadas.
  - Função `ler_csv(caminho)`: separador `;`, pula o cabeçalho e linhas vazias; `error()` se o arquivo não existir.
  - Função `carregar_configuracao()`, chamada no On startup depois de `configurar_log_em_arquivo()`. Lê e valida:
    - `config/simulacao.csv`: duração > 0, 0 ≤ warm-up < duração, replicações ≥ 1.
    - `config/terminais_moega.csv`: `main().<instancia>` existe (reflexão) e implementa `ITerminalMoega`; aviso se a classe difere de `agente`; segmento e ao menos uma ferrovia.
    - `input/tempo_entre_chegadas.csv`: terminal cadastrado, ferrovia operada pelo terminal, intervalo > 0.
  - Havendo erro, para com `error()` depois de listar todos no log.
- **Traces:** `[config]`.
- **Arquivos:** `_alp/Agents/Main/AOC.Main.xml`, `_alp/Agents/Main/Variables.xml`, `_alp/Agents/Main/Code/{Functions.java,Functions.xml}`
## ALT-084 — gestor-isn
- **Data:** 2026-11-05
- **Status:** aplicada, aguardando validação
- **Objetivo:** pátio ISN com estacionamento (linha 02) e circulação própria (linha 01 e porteira), no padrão de faixas do GestorDeCirculacao. Inclui a parte do GestorDeChegadas que decide o destino e coloca o trem no trilho.
- **Alterações (GestorDeISN):**
  - Faixas `faixa_isn_a..e` (trechos `isn_linha01_*`) e `faixa_isn_porteira` (`psn_linha_porteira`), pools `rp_isn_*`, polyline nova `pl_isn_porteira` no painel.
  - Fluxo `circular_isn_*` → `ir_isn_*` → Exit com `seguir_para_faixa()`; entrega ao GestorDeCirculacao na `faixa_val_via2` (próxima `faixa_via3_intermar`).
  - Linha 02: ◇ `e_estacionar` (`trem.estacionarISN`) no fim da faixa c → `ir_isn_linha02_e` (travessão `railwayTrack24`) → `ir_linha02` → `liberar_linha01` → `espera_linha02` → `sair_linha02` (porteira).
  - `registrar_faixas()` no On startup (faixas do ISN em `gestorDeCirculacao.faixas`); evento `ev_verificar_saida` (1 min) chama `verificar_saida()`.
  - Removidos `destino_isn` e `para_circulacao`; função `main()` adicionada.
- **Alterações (outros):**
  - Trem: variável `estacionarISN`.
  - GestorDeChegadas: `receber_trem` grava `estacionarISN`; `entrada_livre` exige `faixa_isn_a` LIVRE; `ev_relogio` não chama mais `verificar_saida`; função `main()` adicionada.
  - Terminais ADM, CoperXI, CoperXXI, MoegaoMar, MoegaoTerra, TES e Tgrao: Exit `paraGestorCirculacao_chegadaCarregados` chama `gestorDeChegadas.receber_trem`.
  - CLAUDE.md: exceção à regra dos pools de linha (pools do ISN no GestorDeISN).
- **Traces:** `[ISN]` e `[chegadas]`.
- **Riscos conhecidos:** `para_via3_ate_santa` e a exceção da Bracell ficam sem uso pelos terminais [revisar]; Tgrao tem a cabeça para o Valongo e ainda não foi testado no ISN; posição da `pl_isn_porteira` no painel é provisória.
- **Arquivos:** `_alp/Agents/GestorDeISN/*`, `_alp/Agents/GestorDeChegadas/*`, `_alp/Agents/Trem/Variables.xml`, `_alp/Agents/{ADM,CoperXI,CoperXXI,MoegaoMar,MoegaoTerra,TES,Tgrao}/EmbeddedObjects.xml`, `_alp/Classes/Class.{Pedido,FluxoChegada}.java`, `CLAUDE.md`, `docs/gestores.md`
## ALT-085 — prefixo-do-trem
- **Data:** 2026-11-05
- **Status:** aplicada, aguardando validação
- **Objetivo:** todo trem que entra no modelo recebe um prefixo (letra + 2 dígitos pares), atribuído pelo GestorDeChegadas na criação.
- **Regra (sorteio aleatório, número sempre par):**
  - Rumo: grão X/I/Y, açúcar R, celulose L; 02–98.
  - MRS: grão M 60–78; açúcar M 80–98; celulose U 30–48.
  - VLI: U 02–98 (qualquer segmento).
- **Alterações:**
  - Trem: variável `prefixo` (String), criada pelo usuário.
  - GestorDeChegadas: função `gerar_prefixo(trem)` (segmento e ferrovia lidos do 1º vagão); `receber_trem` grava `trem.prefixo` antes de tudo, inclusive no trem do botão manual.
  - Traces `[chegadas]` e `[ISN]`: `trem=<id>/<prefixo>`.
- **Riscos conhecidos:** sorteio simples, dois trens no modelo podem ter o mesmo prefixo; combinação ferrovia × segmento fora da regra para com `error()`; trens criados por TrainDecouple (lotes) não herdam o prefixo.
- **Arquivos:** `_alp/Agents/Trem/Variables.xml`, `_alp/Agents/GestorDeChegadas/Code/{Functions.java,Functions.xml}`, `_alp/Agents/GestorDeISN/{EmbeddedObjects.xml,Code/Functions.java}`

## ALT-086 — log-gestor-circulacao
- **Data:** 2026-11-05
- **Status:** aplicada, aguardando validação
- **Objetivo:** log padronizado do GestorDeCirculacao: `[ges-circulacao][tempo] - [prefixo: segmento destino] [status]`, ex.: `[ges-circulacao][12.40h] - [R42: acucar coperXI] [entrou faixa pst_via3_a]`.
- **Quando loga:**
  - Entrada no gestor (`chegada_carregados`, `circulacao`, `circulacao_vazios`, `tentar_usar_faixa`): `comecou circulacao`. Só na 1ª entrada: o `tentar_usar_faixa` recebe o trem a cada faixa, então o gestor guarda os ids em `trens_no_gestor`.
  - onEnter dos 45 TrainMoveTo de faixa (`ir_*`, `recuar_*`...): `entrou faixa <track da faixa_atual>`.
  - Saída para terminal (`para_*` de cada terminal, `continuar5`, `para_manobra_coperXI`): `saiu para terminal <nome>`; `exit` (takeAlivio): `saiu para terminal <nome> (alivio)`; `para_DVS`, `alivio_para_DVs`, `exit3`: `saiu para DV`. Tira o trem de `trens_no_gestor`.
- **Alterações (GestorDeCirculacao):** funções `log_circulacao(trem, status)`, `log_entrada(trem)`, `log_saida(trem, status)`; variável `trens_no_gestor` (Set<Integer>). Os traces antigos `trace_circulacao` ficaram como estavam.
- **Riscos conhecidos:** trens criados por TrainDecouple não têm prefixo (sai `null`); TrainMoveTo legados sem onEnter (`moverAtéOPonto`, `linha_5k_*`, `irAteCruzamento*`) não logam; se um trem sair do gestor por um caminho fora da lista acima, a próxima entrada dele não loga `comecou circulacao`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects.xml,Variables.xml,Code/Functions.xml,Code/Functions.java}`

## ALT-087 — log-padrao
- **Data:** 2026-11-05
- **Status:** aplicada, aguardando compilar e validar
- **Objetivo:** padronizar os logs de todos os agentes. Formato: `[   0.320h] INFO  Origem  evento  mensagem`, tempo sempre em horas, níveis DEBUG/INFO/WARN/ERRO, filtro por nível e por origem.
- **Alterações:**
  - Nova classe `Log` (`_alp/Classes/Class.Log.java`, registrada no `.alpx`) com `iniciar`, `debug`, `info`, `warn`, `erro`, `nivelMinimo`, `silenciados`. Saída por `System.out` (continua espelhada em `logs/sim.log`).
  - GestorDeCirculacao: 130 `traceln` convertidos (opção B: cabeçalho padronizado, texto da mensagem mantido). `Log.iniciar(this)` no `inicializar`. Evento = `enter_<bloco>`/`exit_<bloco>`; `exit_sem_rota_silosMuro_c` sai como WARN. O emoji 🟠➡️ foi removido.
  - GestorDeISN (14 `traceln`) e GestorDeChegadas (6): convertidos do mesmo jeito. Origens `ISN` e `Chegadas`; `Log.iniciar(this)` em `registrar_faixas` (ISN) e `iniciar` (Chegadas). No ISN o evento do `seguir_para_faixa` é o nome do bloco (`nascimento`, `fim_isn_linha01_a`...); a `Log` troca espaços por `_` no evento.
  - GestorDeAlivios (22 `traceln`, origem `GesAlivios`) e GestorDeLocomotivas (6, origem `GesLocomotivas`): convertidos. As tags antigas viraram evento (`diag_pst`, `qtd_alivio`, `alivio4000_compatibilidade`, `saida_4000_livre`, `retornar_alivio_pmc`...). Passam a WARN: `SEM LOCO`, `nenhuma faixa contem a posicao` e `sem faixas de alívio`. `erro: carro não é locomotiva` passa a ERRO. Os `diag_*` continuam em INFO. O `dkfnALIEFNÃPIN` (resto de depuração) virou DEBUG com o mesmo texto.
  - Terminais ADM (18), TES (30) e T39 (4): convertidos, origem = nome do terminal, evento = tag antiga (`decidir_moega`, `descarga_04e5`, `resto_3000`...). Marcos de alívio: `alivio_inicio` (ADM: loco chegou; TES: pega lote) e `alivio_fim` (TES). `AVISO` virou WARN, `ERRO` virou ERRO, e os prints soltos do ADM (`get_distancia_entrada_linha7`, `linha_entrada`, `vagoes_entrando_linha7`) e do T39 (`trem_na_faixa`) viraram DEBUG, que não aparece com `nivelMinimo = INFO`. Ainda não existem logs de `chegou`, `descarga_inicio`, `descarga_fim` e `saiu` nos terminais.
  - Referências `ALT-NNN` removidas dos comentários e das descrições de função dos 8 agentes convertidos (texto mantido). Regra do `CLAUDE.md` atualizada.
- **Pendente:** converter os demais agentes (Trem, terminais restantes, Main, MoverTremCompleto_Ponto). Eles ainda logam no formato antigo; `ALT-NNN` ainda aparece nos comentários deles. Os terminais MoegaoMar e MoegaoTerra logam com a tag `[CoperXII]` (cópia).
- **Backup:** `_backup_antes_alt087/` (`_alp` e `.alpx` antes da alteração).
- **Arquivos:** `_alp/Classes/Class.Log.java`, `MargemDireitaModel1.alpx`, `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects.xml,Variables.xml,Code/Functions.java}`, `_alp/Agents/GestorDeISN/**`, `_alp/Agents/GestorDeChegadas/**`, `_alp/Agents/GestorDeAlivios/**`, `_alp/Agents/GestorDeLocomotivas/**`, `_alp/Agents/{ADM,TES,T39}/**`, `CLAUDE.md`

## ALT-088 — coperxi-contagem-vagao-fracionado
- **Data:** 2026-11-06
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** o `pega1vagao` separa o último vagão do lote antes da descarga. O vagão separado (`train2`) esperava na fila do `mnbCarregados_ocupar1` sem estar em nenhuma variável, e só entrava em `_loteEmDescarga` no `descarregar`. Com isso, `_getVagosLinhaMoega()` e `get_vagoes_carregados()` contavam a menos: o rechego (`<= 27`) era chamado cedo, `pode_rechegar_…` achava espaço que não existia e `podeReceberNovoLote()` liberava novo trem com vagão ainda carregado. Como `atualiza_vaiDescarga()` só olhava `_loteEmDescarga`, um segundo release podia separar outro vagão enquanto o primeiro ainda esperava.
- **Alterações (agente CoperXI):**
  - Nova variável `_vagao_aguardando_descarga` (Trem).
  - `pega1vagao.onExitDecoupled`: `_vagao_aguardando_descarga = (Trem) train2;`
  - `descarregar.onEnter`: `_vagao_aguardando_descarga = null;`
  - `_getVagosLinhaMoega()`: soma `_vagao_aguardando_descarga` (vagão ainda está na linha).
  - `get_vagoes_carregados()`: soma `_loteEmDescarga` (vagão na moega ainda está carregado).
  - `atualiza_vaiDescarga()`: só libera novo lote se `_vagao_aguardando_descarga == null`.
  - Log de contagem: nova função `log_contagem(evento)` (classe `Log`, origem `CoperXI`) imprime `mar/meio/terra/bomba`, `aguardando`, `descarga`, `verde`, `linhaMoega` e `carregados`. Eventos: `fracionou` (`pega1vagao`), `entrou_descarga` (`descarregar`), `release` (`rp_manobrando_carregados.onRelease`) e `rechego` (injeção da loco em `atualiza_precisa_de_rechego()`, no lugar dos 3 `traceln`).
- **2ª etapa (após 1º teste, log de 2026-11-06):**
  - O evento cíclico `pegarVagaoPraDescarga` (1 s) também libera lotes e ignorava a trava: vários vagões eram fracionados e ficavam na fila do `mnbCarregados_ocupar1`, só o último aparecia. A condição do evento passa a exigir `_vagao_aguardando_descarga == null` e `mnbCarregados_ocupar3.size() == 0` (sem loco de rechego esperando).
  - Lote do rechego sumia da contagem entre o `trainCouple` (zera `_loteDeCarregados_linhaVerde`) e a entrega nas linhas. Nova variável `_lote_em_rechego` (Trem): recebe o trem no `trainCouple.onExit` e volta a null no `mnbCarregados_liberar2.onExit`. `get_vagoes_carregados()` soma `_lote_em_rechego.getNumeroDeVagoes()` (sem a loco). `log_contagem` mostra `rechego=`.
- **Observação:** `_loteEmDescarga` só volta a null nos `formarBloco*`, então `get_vagoes_carregados()` conta o vagão até ele formar bloco nas linhas de vazios (conservador).
- **Backup:** `_backup_antes_alt088/CoperXI/` (original) e `_backup_antes_alt088/CoperXI/etapa2/` (antes da 2ª etapa)
- **Arquivos:** `_alp/Agents/CoperXI/{Variables.xml,EmbeddedObjects.xml,Code/Functions.java,Code/Functions.xml,Code/Events.java}`

## ALT-089 — coperxi-um-vagao-por-vez-na-descarga
- **Data:** 2026-11-09
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-09 mostrou o evento `pegarVagaoPraDescarga` (20 s) fracionando um vagão a cada ciclo, inclusive durante o encoste: 26 `fracionou` para 13 `entrou_descarga` em 1,437 h. Os vagões ficavam na fila do `mnbCarregados_ocupar1` e só o último aparecia em `_vagao_aguardando_descarga`, então `_getVagosLinhaMoega()` contava ~12 a menos. A condição da 2ª etapa da ALT-088 no evento não estava no código. Além disso, o `atualiza_vaiDescarga()` do `onRelease` nunca liberava, porque `_loteEmDescarga` só volta a null no `formarBloco*`.
- **Regra:** só um vagão fracionado por vez; novo lote só vai para `descarga_moega`/`pega1vagao` sem vagão aguardando nem em descarga, sem loco de rechego esperando e fora do alívio.
- **Alterações (agente CoperXI):**
  - `atualiza_vaiDescarga()`: passa a exigir também `!fazendoAlivio` (regra única de liberação).
  - Evento `pegarVagaoPraDescarga`: corpo substituído por `atualiza_vaiDescarga();` (fica só como retomada, ex.: fim do alívio).
  - `formarBlocoMar_a/b` e `formarBlocoTerra_a/b` (`onExit` e `onExitHit`): chamam `atualiza_vaiDescarga()` no fim, depois de `_loteEmDescarga = null`.
- **Pendente:** `_lote_em_rechego` (2ª etapa da ALT-088) também não está no código.
- **Backup:** `_backup_antes_alt089/CoperXI/`
- **Arquivos:** `_alp/Agents/CoperXI/{EmbeddedObjects.xml,Code/Functions.java,Code/Events.java}`

## ALT-090 — log-circulacao-prefixo-id
- **Data:** 2026-11-09
- **Status:** aplicada, aguardando compilar e validar
- **Motivo:** rastrear o trem de alívio do PST (prefixo null) e conferir se o trem que volta do alívio tem o mesmo id da loco.
- **Alteração (GestorDeCirculacao):** `log_circulacao()` passa de `[prefixo: segmento destino] [status]` para `[(prefixo - id): segmento destino] [status]`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`

## ALT-091 — log-id-loco-alivio-pst
- **Data:** 2026-11-09
- **Status:** aplicada, aguardando compilar e validar
- **Motivo:** conferir se o trem que volta do alívio do PST tem o mesmo id da loco chamada.
- **Alteração (GestorDeAlivios):** `chamar_locomotiva_de_alivio_pst()` registra `loco do alivio (prefixo - id)` ao tirar a loco do wait da ferrovia.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`

## ALT-092 — alivio-pst-faixa-criacao-ocupada-e-prefixo-mnb
- **Data:** 2026-11-09
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** logs de 2026-11-09 mostraram `The car being added on the track 'via1_intermar_d' intersects with another car`. O `liberar_faixas_pst` (Release de tudo) solta o `rp_via1_intermar_d` com o trem de volta ainda na faixa; o `atualizar_status_alivio_terminal` devolve o terminal a `terminais_pst` e o próximo alívio cria a loco em cima dele. A ALT-091 confirmou que o trem de volta é o mesmo agente da loco (mesmo id).
- **Alterações (GestorDeAlivios):**
  - `seize_faixa_criacao_pst.onExit`: `ocupar_agente(9999)` vira `ocupar((Trem) agent)` (OCUPADA com o id da loco) e registra `loco do alivio (prefixo - id) ocupa <recurso>`.
  - `fazer_procedimento_de_alivio_pst()`: só segue se a faixa de criação estiver LIVRE; senão o terminal continua em `terminais_pst` (log a cada 60 s).
  - `retornar_alivio.onEnter`: se a faixa de criação não for a de entrada na circulação (Tgrao: `marginal_c`), libera ali.
  - A liberação da `via1_intermar_d` fica com o GestorDeCirculacao (`liberar_faixas_ocupadas`), sem mudança lá.
  - Prefixo `MNB` + número ímpar no trem de alívio: nova variável `contador_mnb` (int, inicial -1). Aplicado em `seize_faixa_criacao_pst` (PST), `fazer_procedimento_de_alivio_pmc()` e `loco_manobra_alivio_pmc` (PMC) e `loco_pcx_starts` (linha 4000).
- **Docs:** `docs/gestores.md` (GestorDeAlivios).
- **Backup:** `_backup_antes_alt092/GestorDeAlivios/`
- **Arquivos:** `_alp/Agents/GestorDeAlivios/{EmbeddedObjects.xml,Variables.xml,Code/Functions.java}`

## ALT-093 — trens-em-circulacao
- **Data:** 2026-11-09
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-09: em 2,783 h o GestorDeISN liberou o M98 da linha 02 e, no mesmo instante, o GestorDeChegadas mandou o R02 direto para a circulação, os dois para a CoperXI. `podeReceberNovoLote()` só olha os carregados no terminal, e o M98 ainda não tinha chegado. Em 3,172 h o R02 encostou nos vagões da linha verde (`ir_linha_verde.outHit` sem conexão).
- **Regra:** um trem a caminho de cada terminal por vez.
- **Alterações:**
  - GestorDeCirculacao: nova collection `trens_em_circulacao` (ArrayList<Trem>). `log_saida()` retira o trem quando ele sai para o terminal.
  - GestorDeChegadas: `decidir_destino()` só manda direto para a circulação se o terminal pode receber e não há trem em `trens_em_circulacao` com o mesmo `ITerminalMoega_destino`. `receber_trem()` adiciona o trem à lista quando o destino é a circulação (e o trem do botão manual, se tiver destino).
  - GestorDeISN: `verificar_saida()` só libera a linha 02 com a mesma checagem e também confere o `pedido_atual` da Chegadas; ao liberar, adiciona o trem à lista.
  - Trens de alívio não entram na lista.
- **Backup:** `_backup_antes_alt093/`
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{Variables.xml,Code/Functions.java}`, `_alp/Agents/GestorDeChegadas/Code/Functions.java`, `_alp/Agents/GestorDeISN/Code/Functions.java`

## ALT-094 — coperxi-trem-chegando-e-vez-da-linha02
- **Data:** 2026-11-09
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-09 (após a ALT-093): (1) o R96 saiu de `trens_em_circulacao` no `log_saida` (4,891 h), mas a CoperXI só conta os vagões a partir do `deixar27vgs`; nesse intervalo `podeReceberNovoLote()` ficou true e o P5 (M98) foi mandado direto (4,900 h), batendo na linha verde em 5,191 h. (2) O R02 ficou na linha 02 desde 3,006 h: a Chegadas mandou o P4 e o P5 na frente dele.
- **Alterações:**
  - CoperXI: variável `_trem_chegando` (Trem). Recebe o trem no `encostarLinhaVerde.onEnter`; volta a null no novo Delay `finalizar_manobra` (0 s), entre `mnbCarregados_liberar` e `devolverLocomotiva` (fim da manobra de chegada). `podeReceberNovoLote()` devolve false enquanto `_trem_chegando != null`.
  - GestorDeChegadas: `decidir_destino()` conta o trem da linha 02 (`lote_linha02`) com o mesmo destino como trem a caminho; o pedido novo fica na fila e o ISN libera o da linha 02 primeiro.
- **Conectores:** `connector41` passa a ligar `finalizar_manobra.out` → `devolverLocomotiva.port`; novo `connector_finalizar_manobra` liga `mnbCarregados_liberar.out` → `finalizar_manobra.in`. Posição do bloco a ajustar no editor.
- **Backup:** `_backup_antes_alt094/`
- **Arquivos:** `_alp/Agents/CoperXI/{Variables.xml,EmbeddedObjects.xml,Connectors.xml,Code/Functions.java}`, `_alp/Agents/GestorDeChegadas/Code/Functions.java`

## ALT-095 — gestordvs-quantos-vagoes-cabem
- **Data:** 2026-11-10
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** `quantos_vagoes_cabem` devolvia 82 fixo e o GestorDeAlivios usava 12 fixo (chamada comentada). Com DVs cheias, um MNB que não fecha 82 nem cabe ficaria preso na entrada.
- **Alterações:**
  - GestorDeDVs: `quantos_vagoes_cabem(terminal)` — falta = 82 − acumulado do tipo (ferrovia × segmento do terminal); vazios ≥ falta → falta (acopla e parte); vazios ≤ livre → vazios (estaciona); senão 0. O acumulado e o livre incluem o `lote_slot_via2`.
  - GestorDeDVs: funções novas `acumulado_do_tipo(ferrovia, segmento)`, `espaco_livre()` (`CAPACIDADE_X − main().getNumeroDeVagoes(lote_x)` nas 4 linhas + slot da via 2 = 138), `log_estado()`, `texto_linha(lote, capacidade)`; variáveis `trace_dvs` (true) e `TAMANHO_TREM_FORMADO` (82, constante).
  - GestorDeDVs: logs `consulta_cabem` e `estado` (origem `GesDVs`); com retorno 0, só a cada 60 s.
  - GestorDeAlivios: `colocar_terminal_em_alivio_pst` usa `quantos_vagoes_as_dvs_precisam(terminal)` no lugar do 12 fixo; `fazer_procedimento_de_alivio_pst` consulta uma vez só e passa o valor para `colocar_terminal_em_alivio_pst(terminal, cabem_na_dv)` (parâmetro novo); com retorno 0 não começa o alívio (terminal fica em `terminais_pst`).
- **Limitação:** os lotes das DVs ainda não são preenchidos fisicamente; até lá, acumulado = 0 e livre = 138.
- **Backup:** `_backup_antes_alt095/`
- **Arquivos:** `_alp/Agents/GestorDeDVs/{Variables.xml,Code/Functions.java,Code/Functions.xml}`, `_alp/Agents/GestorDeAlivios/Code/{Functions.java,Functions.xml}`

## ALT-096 — gestordvs-logs-manobra
- **Data:** 2026-11-10
- **Status:** aplicada, aguardando compilar e validar
- **O que:** logs da manobra nas DVs com os dados do trem.
- **Alterações (GestorDeDVs):**
  - `texto_trem(trem)`: `(prefixo - id) | N vgs + M loco | ferrovia/segmento` (tipo pelo `getPrimeiroVagao()`, que pula as locos).
  - `log_comecou_manobra(trem)` → trace `comecou_manobra` (com o livre); chamado no `onEnter` do `entrarNasDVs`.
  - `colocar_lote(linha, lote)`: grava `lote_direita/dv2/dv3/dv4/slot_via2` (linha `"DIR"`, `"DV2"`, `"DV3"`, `"DV4"`, `"SLOT"`), trace `colocou_lote` e `log_estado()`. Ainda sem chamada: entra com os MoveTos.
  - `log_terminou_manobra(trem)` → trace `terminou_manobra` + `log_estado()`; chamado no `onEnter` do `trainExit`.
- **Backup:** `_backup_antes_alt096/`
- **Arquivos:** `_alp/Agents/GestorDeDVs/{EmbeddedObjects.xml,Code/Functions.java,Code/Functions.xml}`

## ALT-097 — gestordvs-estacionar-formar
- **Data:** 2026-11-10
- **Status:** aplicada, aguardando compilar e validar
- **O que:** fluxo físico das DVs: estacionar lotes, formar o trem de 82 e sair por `circ_pos_fim_placa_2`.
- **Sentido:** carro 0 do lado da Ponta da Praia, loco no fim (lado do Valongo). Até o ponto e até a saída o trem anda recuando (Ponta da Praia → Valongo); do ponto para a linha, avançando.
- **Fluxo (GestorDeDVs):** `entrarNasDVs` (`iniciar_manobra`) → ◇ `sel_segue_direto` (82 vgs ou celulose)
  - sim → `ir_saida`
  - não → `ir_ponto` (`_dv_ponto_aux_mnb`, evitando as 4 linhas: passa pela `dv_5`) → `moverComposicao` → `liberar_faixas_ponto` → ◇ `sel_acao` (`escolher_linha`)
    - linha → `ir_linha` (ponta da Ponta da Praia da linha, 3 m; `out` = linha vazia, `outHit` = bateu no lote) → `deixar_lote` (decouple dos primeiros `min(capacidade, vagões)`) → `lote_estacionado` (`colocar_lote`); o resto volta ao `moverComposicao`
    - bateu no lote → `entrar_lote` + `acoplar_lote` (`limpar_lote`) → ◇ `sel_formando`: formando → volta ao ponto; senão → `deixar_lote`
    - sem linha (82 formado ou só loco) → `ir_saida` (`circ_pos_fim_placa_2`, log `saida_82` / `saida_loco` / `saida`) → `trainExit` (`finalizar_manobra`) → `liberar_faixas_circulacao` → ◇ `sel_so_loco`: só loco → `devolverLocomotiva`; senão `trainDecouple3` (corta as locos do fim: `size − vagões`)
- **Regras:** `formando` = acumulado do tipo > 0 e acumulado + vagões do trem == 82. Estacionar: 1º linha com lote do mesmo tipo e espaço (acopla e completa), 2º linha vazia. Slot da via 2 fora desta versão (livre = 108). Linha DV4 = `dv_4_b`. `condicao_pode_receber_alivio()` = `trem_em_manobra == null`.
- **Funções novas:** `segue_direto`, `iniciar_manobra`, `escolher_linha`, `mesmo_tipo`, `get_lote`, `get_capacidade`, `get_track_linha`, `get_offset_do_inicio`, `get_trilhos_evitar`, `entrar_lote_da_linha`, `limpar_lote`, `n_vagoes_deixar`, `log_saida`, `finalizar_manobra`.
- **Variáveis novas:** `trem_em_manobra`, `linha_destino`, `formando`.
- **Blocos novos:** `sel_segue_direto`, `ir_ponto`, `liberar_faixas_ponto`, `sel_acao`, `ir_linha`, `entrar_lote`, `acoplar_lote`, `sel_formando`, `deixar_lote`, `lote_estacionado`, `ir_saida`, `sel_so_loco` (posições a ajustar no editor). Os blocos antigos do esqueleto (`irDV3..5`, `trainCouple*`, `cabeDV*` etc.) ficaram desconectados.
- **Backup:** `_backup_antes_alt097/`
- **Arquivos:** `_alp/Agents/GestorDeDVs/{EmbeddedObjects.xml,Connectors.xml,Variables.xml,Code/Functions.java,Code/Functions.xml}`

## ALT-098 — movertremponto-residual-pela-traseira
- **Data:** 2026-11-11
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-10 (ALT-097): no `gestorDeDVs.moverComposicao`, a loco do MNB01 parou no `_dv_ponto_aux_mnb` atravessando o switch (traseira na `railwayTrack203`). O `_jaEstaNoDestino` olha frente e traseira e deu true; o `_distanciaResidual` só olhava a frente (`getTrack(true)`), não achou carro na track do ponto e chamou `trem.getDistanceToTarget()` com o trem parado → `Cannot obtain distance to target while the train is stopped`.
- **Alterações (`MoverTremCompleto_Ponto._distanciaResidual`):**
  - procura o carro também pela traseira (`getTrack(false)`), como o `_jaEstaNoDestino`; achado pela traseira, soma o comprimento do próprio carro.
  - sem carro na track do ponto: tenta `getDistanceToTarget()`; se o trem estiver parado, `Log.warn` (`MoverTremPonto distancia_residual`) e move o comprimento do trem.
- **Impacto:** agente compartilhado; só muda o caso que antes terminava em erro.
- **Backup:** `_backup_antes_alt098/`
- **Arquivos:** `_alp/Agents/MoverTremCompleto_Ponto/Code/Functions.java`

## ALT-099 — dvs-decisao-da-linha-uma-vez
- **Data:** 2026-11-11
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-11: `proxima_linha` saía duplicado. A condição do `sel_acao` chamava `escolher_linha`, que grava log e altera `linha_destino`; o SelectOutput avalia a condição duas vezes (quando o trem fica pronto no bloco anterior e na transmissão).
- **1ª tentativa (descartada):** decidir no `liberar_faixas_ponto.onExit` e condição `linha_destino != null` → erro `The choice for agent changed before the agent transmission` (a 1ª avaliação ocorre antes do `onExit` do bloco anterior).
- **Alterações (GestorDeDVs):**
  - nova função pura `calcular_linha(trem)` (String ou null; sem log e sem alterar estado).
  - `escolher_linha` grava `linha_destino = calcular_linha(trem)` e loga `proxima_linha` / `sem_linha`.
  - `sel_acao.condition` → `calcular_linha((Trem) agent) != null`; `sel_acao.onEnter` → `escolher_linha((Trem) agent);`.
  - `liberar_faixas_ponto.onExit` volta a ficar vazio.
- **Analisado sem alteração:** acumulado 81 / falta 1 — buscar 1 vagão é aceito; formação do 82 em várias linhas (DIR → DV2 → DV3) — lógica de `formando` já volta ao ponto a cada acoplamento.
- **Backup:** `_backup_antes_alt099/` (estado ALT-098) e `_backup_antes_alt099b/` (1ª tentativa)
- **Arquivos:** `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `Code/Functions.java`, `Code/Functions.xml`

## ALT-100 — coperxi-primeiro-vagao-da-linha-de-vazios
- **Data:** 2026-11-11
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-11: trem de 82 no CoperXI, alívios levaram 35 + 35 + 11 e 1 vagão ficou parado na linha. O primeiro vagão de uma linha de vazios vazia ia do `formarBloco*.out` direto para `acumularX.in1` (esperando o próximo vagão) e `_loteDeVazios_LinhaX` só era preenchido no acoplamento → `_getVagosVazios()` não contava esse vagão e o alívio não conseguia retirá-lo (não passa pelo `wait_bloco*`). Trava quando é o último vagão do trem.
- **Alterações (CoperXI), nas 4 linhas (Mar A/B, Terra A/B) — igual ao caminho do excedente (`mnbVazios_liberar17/19/21/23`):**
  - conectores `mnbVazios_liberar9/11/13/15.out` → `wait_blocoMar_a/Mar_b/Terra_a/Terra_b.in` (antes `acumular2/acumular1/acumular/acumular3.in1`) — refeitos pelo usuário no AnyLogic.
  - `onExit` desses Release: `_loteDeVazios_LinhaX = (Trem) agent;`.
- **Comportamento:** o vagão sozinho conta como vazio e fica no wait; o próximo vagão bate (`onExitHit`: `condicao_ficar = true` + `liberar_wait`) e os dois acoplam; num alívio sai pelo wait com `condicao_ficar = false`.
- **Fora:** CoperXXI e CostaLeite parecem ter a mesma estrutura (não alterados).
- **Backup:** `_backup_antes_alt100/`
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXI/Connectors.xml`

## ALT-101 — alivio-via2-entrega-as-dvs-no-via2-intermar-a
- **Data:** 2026-11-11
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-11 (Tgrao): o alívio pela via 2 andava duas faixas além de `via2_intermar_a` (`railwayTrack199 → val_via2`) e entrava nas DVs pelo lado do Valongo → `gestorDeDVs.ir_ponto`: `There is no straight route from val_via2, offset 10 to railwayTrack203`.
- **Alterações (GestorDeCirculacao), igual ao fim do caminho via1 (`para_via6` → `ir_via1_intermar_a` → `para_DVS`):**
  - `alivio_para_via2_intermar_a.onExit`: `proxima_faixa = null` (antes `faixa_railwayTrack199`).
  - conector `recuar_via2_intermar_a.out` → `alivio_para_DVs` (antes `alivio_para_railwayTrack199`).
  - cadeia `alivio_para_railwayTrack199 → railwayTrack199 → val_via2 → recuar_val_via2` fica desconectada para o alívio (não apagada).
- **Risco:** rota recuando de `via2_intermar_a` até `railwayTrack203` (evitando as 4 linhas) não confirmada pelo XML.
- **Backup:** `_backup_antes_alt101/`
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeCirculacao/Connectors.xml`

## ALT-102 — dvs-livre-por-tipo-e-reserva-do-lider
- **Data:** 2026-11-11
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-11 (Tgrao + CoperXI): (1) `espaco_livre()` somava vagas de todas as linhas, inclusive de linha com lote de outro tipo → `livre=34` para açúcar quando cabiam 33; sobrou 1 vagão (`sem_linha`) que saiu com a loco. (2) DIR 40 grão + 67 açúcar = 107: açúcar falta 15 (terminal com 14), grão falta 42 sem espaço → nenhum tipo fecha 82, `retorno=0` para sempre.
- **Regra nova (`quantos_vagoes_cabem`):**
  - fecha o trem (`vazios >= falta`) → `falta` (como antes).
  - senão, `vazios` precisa caber em `espaco_livre_tipo` (linhas vazias + linhas do mesmo tipo).
  - reserva do líder (tipo com mais vagões acumulados que este): simula estacionar os vazios (mesma ordem do `calcular_linha`, por linha) e exige que sobre `falta_lider` para o líder; senão 0.
  - cota fixa por tipo descartada: 108 vagas não comportam dois tipos reservando 82.
- **Funções novas:** `lote_do_tipo`, `espaco_livre_tipo`, `lote_do_lider`, `espaco_lider_apos`. Log `consulta_cabem` ganha `livre_tipo` e `lider=tipo falta sobra`.
- **Efeito colateral:** um tipo não-líder pode esperar o líder fechar o trem.
- **Backup:** `_backup_antes_alt102/`
- **Arquivos:** `_alp/Agents/GestorDeDVs/Code/Functions.java`, `Code/Functions.xml`

## ALT-103 — coperxi-costaleite-encoste-pega-via2e-e-marginais-juntas
- **Data:** 2026-11-11
- **Status:** aplicada, aguardando compilar e validar
- **Problema 1 (deadlock):** log de 2026-11-11 (208.18h): R22 (carregado CoperXI) licenciou a via2_e no início da via3_b e, no terminal, o `pst_marginal_bloquear` pedia marginal_a/b/c + intervalo4000_a/b. O vazio do Tgrao (MNB01) estava nas marginais esperando a via2_e. Um esperava o outro. A CostaLeite tem o mesmo `pst_marginal_bloquear` (mesmas 5 faixas e o mesmo switch73).
- **Problema 2 (CostaLeite nunca soltava as marginais):** nenhum Release apontava para o `pst_marginal_bloquear` dela e o `liberar_faixas_ocupadas` só libera faixas `OCUPADA` (as marginais ficam `OCUPADA_BLOQUEADA`). A loco saía pelo `mov_devolver_loco` com as 5 unidades; log de 2026-11-11 (215.48h): `saida_4000_livre aguardando rp_pmc_intervalo4000_b: OCUPADA_BLOQUEADA | trem 25853` para sempre, e o mesmo agente 25853 seguia como loco dos alívios (MNB01..07).
- **Alterações (GestorDeCirculacao):**
  - `para_via3_b`: coperXI/costaLeite → `proxima_faixa = null` (não licencia mais a via2_e antes).
  - Novo Seize `bloquear_encoste_marginal` entre `e_coperXI.outT` e `para_via2_e`: um conjunto `{rp_pst_via2_e, marginal_a/b/c, intervalo4000_a/b}`; onExit licencia a via2_e, `bloquear()` nas 5 faixas e `railwaySwitch73` marginal_a↔b. O `para_via2_e` encontra a via2_e já licenciada e não pede de novo.
- **Alterações (CoperXI):** `pst_marginal_bloquear` excluído do build e desligado (`encostarLinhaVerde` → `ir_linha_verde`); `pst_marginal_liberar` passa a `SPECIFIED_RESOURCES` com os 5 pools (antes `ALL_FROM_SEIZES {pst_marginal_bloquear}`), mantendo os `liberar()`.
- **Alterações (CostaLeite):** `pst_marginal_bloquear` excluído do build e desligado (`encostar` → `ir_marginal`); novo Release `liberar_marginal_encoste` (`SPECIFIED_RESOURCES`, 5 pools + `liberar()`) entre `desacoplar_10vgs` (loco) e `mov_devolver_loco`.
- **A validar:**
  - [ ] Seize policy do `bloquear_encoste_marginal` = "Seize whole set at once" (conferir na tela; se pegar uma a uma, o deadlock volta).
  - [ ] Caminho da loco da CostaLeite até o `guardar_loco` não passa pela marginal (se passar, o Release tem que ir depois).
  - [ ] Cenário do deadlock (vazio do Tgrao + carregado CoperXI) e saída da 4000 depois de um trem da CostaLeite.
- **Backup:** `_backup_antes_alt103/` (subpastas GestorDeCirculacao, CoperXI, CostaLeite). Cópia igual ficou por engano em `_backup_antes_alt100/<agente>/`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/CoperXI/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/CostaLeite/{EmbeddedObjects,Connectors}.xml`

## ALT-104 — costaleite-rota-loco-alivio
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-12 (1,339h): `The track via1_intermar_d is not directly connected to the next track in the route: psnSaidaCopexXXI_b1` no `ir_entrada_terminal_pst`. O `get_pos_alivio()` da CostaLeite passou a devolver `_costaLeite_inicio_alivio` (na `psnSaidaCopexXXI_b1`), mas `get_faixas_primeiro_alivio()` seguia vazia; `Alivio.getRotaLicenciada()` montava `[via1_intermar_d, psnSaidaCopexXXI_b1]`, sem as linhas do meio.
- **Alteração (CostaLeite):** `get_faixas_primeiro_alivio()` devolve `linhaPaqueta_a` e `linhaCostaLeita_a` como `Faixa` sem recurso (mesmo padrão do CoperXXI). Rota: `via1_intermar_d → linhaPaqueta_a → linhaCostaLeita_a → psnSaidaCopexXXI_b1` (conferida no grafo de AMVs do Main).
- **Observação:** o WARNING `bloquear_faixas_pst ... empty resource set` continua (as faixas não têm recurso) e é inofensivo.
- **A validar:**
  - [ ] Loco do alívio chega a `_costaLeite_inicio_alivio` e o alívio da CostaLeite termina.
- **Backup:** `_backup_antes_alt104/CostaLeite/Functions.java`
- **Arquivos:** `_alp/Agents/CostaLeite/Code/Functions.java`

## ALT-105 — gestor-alivios-um-alivio-pst-por-vez
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-12 (2,076h): `Alivio.getConjuntoRecursos_faixas(): terminal sem faixas de primeiro alivio`. Quando as DVs voltaram a aceitar (2,066h), Tgrao e CostaLeite passaram no mesmo `analisar_pst()`: o `rp_alivio_pst.idle()` só é testado antes do `for` e o laço não parava. O segundo `colocar_terminal_em_alivio_pst` sobrescreveu `alivio_em_andamento_pst`; a CostaLeite terminou e esvaziou a lista, e a loco seguinte entrou no `bloquear_faixas_pst` com o `Alivio` vazio.
- **Alteração (GestorDeAlivios):** em `analisar_pst()`, depois de `fazer_procedimento_de_alivio_pst(terminal)`, `return` se o terminal saiu de `terminais_pst` (alívio começou).
- **A verificar:** `analisar_pmc()` e `analisar_pcx()` têm o mesmo laço sem parada.
- **Backup:** `_backup_antes_alt105_106/GestorDeAlivios/`
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`

## ALT-106 — recurso-amv-switch36-no-encoste
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-12 (1,253h): `Train [274] and train [273] are trying to occupy the switch railwaySwitch36 simultaneously`. O AMV liga `pst_linha_verde`, `railwayTrack22` (lado da `pstLinhaDaBomba`, CoperXI) e `railwayTrack77` (lado da `linha_cantina`/`pst_linha_atendimento_costaLeite`). O encoste passa por ele e nenhum recurso o protegia.
- **Alterações:**
  - GestorDeCirculacao: novo ResourcePool `rp_pst_switch36` (cópia do `rp_pmc_marginal_a`), incluído no conjunto do `bloquear_encoste_marginal` (pego junto com via2_e, marginais e 4000).
  - CoperXI `pst_marginal_liberar` e CostaLeite `liberar_marginal_encoste`: liberam também o `rp_pst_switch36`.
- **A validar:**
  - [ ] Compila (pool novo acessível como `main().gestorDeCirculacao.rp_pst_switch36`).
  - [ ] Nenhum outro movimento (descarga do CoperXI, alívio) cruza o AMV fora do encoste; se cruzar, precisa pegar o mesmo recurso.
- **Backup:** `_backup_antes_alt105_106/` (GestorDeCirculacao, CoperXI, CostaLeite)
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/CoperXI/EmbeddedObjects.xml`, `_alp/Agents/CostaLeite/EmbeddedObjects.xml`

## ALT-107 — logs-movimentos-coperxi-costaleite
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Objetivo:** acompanhar de perto os movimentos do CoperXI e da CostaLeite (investigação da colisão no `railwaySwitch36`, ALT-106).
- **Alterações (CoperXI e CostaLeite):**
  - Nova função `log_mov(String evento, Agent a)`: `Log.info(<terminal>, evento, "(prefixo - id) | N vgs + N loco | linhas=<tracks ocupadas>")`.
  - Chamada no `onEnter` (`ini:<bloco>`) e no `onExit` (`fim:<bloco>`) de todos os TrainMoveTo, no `onEnter` de todos os Enter (`etapa:<bloco>`) e no `onExit` do `pst_marginal_liberar` / `liberar_marginal_encoste` (`liberou:<bloco>`). Código existente nos campos foi mantido (a chamada entra na primeira linha).
  - Total: 82 chamadas no CoperXI, 44 na CostaLeite.
- **Não cobertos:** blocos `MoverTremCompleto_Ponto`, `TrainDecouple`/`TrainCouple` e `DevolverLocomotiva`.
- **Para silenciar:** `Log.silenciados.add("CoperXI")` / `Log.silenciados.add("CostaLeite")`.
- **Backup:** `_backup_antes_alt107/` (CoperXI, CostaLeite)
- **Arquivos:** `_alp/Agents/{CoperXI,CostaLeite}/EmbeddedObjects.xml`, `_alp/Agents/{CoperXI,CostaLeite}/Code/Functions.{java,xml}`

## ALT-108 — coperxi-rota-loco-alivio-sem-linhas-da-costaleite
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-12 (4,710h): `The track linhaCostaLeita_a is not directly connected to the next track in the route: pstSaidaCoperXI`. A `get_faixas_primeiro_alivio()` do CoperXI estava com as linhas da CostaLeite (`linhaPaqueta_a`, `linhaCostaLeita_a`), código colado no agente errado durante a ALT-104. A `pstSaidaCoperXI` sai direto da `via1_intermar_d`.
- **Alteração (CoperXI):** `get_faixas_primeiro_alivio()` volta a devolver lista vazia (como no commit `46cec1d`). Rota: `via1_intermar_d → pstSaidaCoperXI`.
- **Backup:** `_backup_antes_alt108/CoperXI/Functions.java`
- **Arquivos:** `_alp/Agents/CoperXI/Code/Functions.java`

## ALT-109 — dvs-devolve-loco-direto-da-linha
- **Data:** 2026-11-12
- **Status:** validada (log de 2026-11-12: `colocou_lote` → `devolveu_loco` → `terminou_manobra`)
- **Nota:** registrada primeiro como ALT-103, número já usado por outra sessão; renumerada.
- **Pedido:** depois de colocar o lote, a loco sozinha não precisa ir ao `_dv_ponto_aux_mnb` nem ao `circ_pos_fim_placa_2`; é devolvida direto.
- **Alterações (GestorDeDVs):**
  - novo `sel_loco_sozinha` (SelectOutput) entre `deixar_lote.out` e `moverComposicao`; condição pura `main().getNumeroDeVagoes((Trem) agent) == 0`.
  - `outT` → `devolverLocomotiva.port`; `onExitTrue`: log `devolveu_loco ... na linha X`, `log_terminou_manobra`, `trem_em_manobra = null`, `formando = false` (libera o próximo alívio). `linha_destino` não é zerada ali porque o lote pode sair do `deixar_lote` depois da loco e o `colocar_lote` usa ela; o `iniciar_manobra` zera no próximo trem.
  - `outF` (sobraram vagões) → `moverComposicao`, como antes.
  - conector `con_dvs_15` passa a `deixar_lote.out → sel_loco_sozinha.in`; novos `con_dvs_21` e `con_dvs_22`.
- **Por que funciona:** `devolverLocomotiva` entrega ao `gestorDeLocomotivas.guardar_loco`, que começa com `sairDaTrack` (TrainExit); as faixas já foram liberadas no `liberar_faixas_ponto`.
- **Sem mudança:** `ir_saida → trainExit → sel_so_loco` segue para trem com vagões (`saida_82`, `sem_linha`).
- **Backup:** `_backup_antes_alt103/` (nome mantido)
- **Arquivos:** `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `_alp/Agents/GestorDeDVs/Connectors.xml`

## ALT-110 — costaleite-solta-vazio-do-wait-no-fim-do-alivio
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-12 (3,650h): com as 3 linhas de vazios cheias (8+8+8), o 25º vazio (`26107`) saiu da moega, `cabe_linhaTerra` = false → `mnbVazios_liberar15 → wait`. Nada chamava `wait.free()`: o vazio ficou preso, o `_loteEmDescarga` não voltou a null (só zera no `formarBloco*`) e o `atualiza_vaiDescarga` nunca liberou a próxima descarga, nem depois do alívio de 24 (4,857h).
- **Alteração (CostaLeite):** `deixarTerminal.onEnter` (fim do alívio) solta todos do `wait` antes do `atualiza_vaiDescarga()`: `while (wait.size() > 0) wait.free(wait.get(0));`. O vazio volta ao `mnbVazios_ocupar1 → cabe_linha*`, entra na linha vazia e o `formarBloco` zera o `_loteEmDescarga`.
- **Ajuste (2026-11-12):** o `while (wait.size() > 0) wait.free(wait.get(0));` era laço infinito — o `free()` só agenda a saída, o `wait.size()` não diminui dentro do evento e a simulação congelava no `deixarTerminal` (log ALT-111: `soltou_wait soltando 1` e nada depois). Trocado por `wait.freeAll();`. Backup do ajuste: `_backup_antes_alt110b/`.
- **Backup:** `_backup_antes_alt110/`
- **Arquivos:** `_alp/Agents/CostaLeite/EmbeddedObjects.xml`

## ALT-111 — costaleite-logs-recurso-de-manobra
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Objetivo:** investigar o travamento da descarga da CostaLeite depois do alívio (ALT-110): `wait` com 1 vazio, `rp_manobrando_carregados` em uso, `waitLotesParaDescarga` = 2, `_loteEmDescarga` preenchido. Os Seize/Release/Wait não tinham log.
- **Funções novas (CostaLeite):** `texto_trem(t)` → `(prefixo - id)`; `texto_estado()` → `rp=ocupado/capacidade | wait=N | waitLotes=N | emDescarga=(prefixo - id)`; `log_rp(evento, a)` → trem + estado.
- **Logs:**
  - Seize `mnbCarregados_ocupar`, `mnbVazios_ocupar1/2/4/5/6`: `fila:<bloco>` (onEnter) e `pegou:<bloco>` (onExit).
  - Release `mnbCarregados_liberar`, `mnbVazios_liberar9..16`: `soltou:<bloco>` (onExit).
  - `wait.onEnter`: `esperando:wait`.
  - `cabe_linhaTerra.onExitFalse`: `linhas_cheias` (Mar/Meio/Terra x/8 + estado).
  - `deixarTerminal`: `soltou_wait` antes do `wait.free`.
  - `atualiza_vaiDescarga()`: `vai_descarga` / `nao_descarrega` + estado.
- **Para silenciar:** `Log.silenciados.add("CostaLeite")`.
- **Backup:** `_backup_antes_alt111/`
- **Arquivos:** `_alp/Agents/CostaLeite/EmbeddedObjects.xml`, `Code/Functions.java`, `Code/Functions.xml`

## ALT-112 — costaleite-trem-chegando
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** (1) `podeReceberNovoLote()` era `_getVagoesCarregados() < 0` (sempre false, erro de sinal). (2) Mesma janela da ALT-094 no CoperXI: os carregados só contam a partir dos `fica_lote_*`; entre o `comecar_encoste` e o primeiro desacoplamento o terminal parecia vazio.
- **Alterações (CostaLeite):**
  - Variável `_trem_chegando` (Trem). Recebe o trem no `comecar_encoste.onEnter`; volta a null no `liberar_marginal_encoste.onExit` (último lote já deixado pelo `desacoplar_10vgs`).
  - `podeReceberNovoLote()`: false enquanto `_trem_chegando != null`; senão `_getVagoesCarregados() <= 0`.
- **Backup:** `_backup_antes_alt112/`
- **Arquivos:** `_alp/Agents/CostaLeite/{Variables.xml,EmbeddedObjects.xml,Code/Functions.java}`
## ALT-113 — botoes-passam-pelo-gestor-de-chegadas
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **Problema:** os botões dos terminais chamavam `tremTeste_*.inject()` (CostaLeite: `trem_rumo_grao.inject()`) direto, sem passar pelas condições do GestorDeChegadas. Cliques seguidos criavam um trem em cima do anterior na `isn_linha01_a` (`intersects with another car`); a criação falhava no meio e sobrava um `Trem` sem vagões (erro do prefixo no trem 25984 e trem vazio no `ir_via3_intermar`).
- **Alterações:**
  - GestorDeChegadas: `pedir_trem(terminal, ferrovia, fonte)` cria um pedido manual na fila e chama `despachar()`; o trem entra pelas mesmas regras do pedido programado.
  - 20 botões em ADM, CoperXI, CoperXXI, CostaLeite, MoegaoMar, MoegaoTerra, TES e Tgrao passam a chamar `main().gestorDeChegadas.pedir_trem(this, "<ferrovia>", <fonte>)`.
  - `despachar()` registra o motivo de espera (`aguardando`) só quando ele muda e a decisão (`decisao`) com o porquê. Funções novas: `motivo_entrada`, `trem_a_caminho`, `motivo_destino`, `registrar_motivo`. `entrada_livre` e `decidir_destino` usam essas funções (mesma regra).
  - Pedido: campos `manual` e `ultimo_motivo`.
  - `gerar_prefixo`: trem sem vagão válido vira `Log.warn` (`sem_prefixo`, com carros, locos e destino) e prefixo `???`, sem parar a simulação.
  - `receber_trem`: trem sem pedido passa a `Log.warn`.
  - CLAUDE.md: pasta do modelo em `margem-direita-desktop`.
- **Fora do escopo:** botões `tremTesteSource` e `trem_celulose_teste` (terminais de celulose) continuam com `inject()` direto.
- **Backup:** `_backup_antes_alt113/`
- **Arquivos:** `_alp/Agents/GestorDeChegadas/Code/{Functions.java,Functions.xml}`, `_alp/Classes/Class.Pedido.java`, `_alp/Agents/{ADM,CoperXI,CoperXXI,CostaLeite,MoegaoMar,MoegaoTerra,TES,Tgrao}/Levels/Level.level.xml`, `CLAUDE.md`
## ALT-114 — dvs-fila-na-entrada
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **Problema:** dois alívios chegaram às DVs ao mesmo tempo (MNB17 em manobra, MNB19 chegou). O `iniciar_manobra` só avisava (`manobra_simultanea`) e sobrescrevia `trem_em_manobra` e `linha_destino = null`; o MNB17 seguiu para o `deixar_lote` e o `get_capacidade(null)` estourou (`NullPointerException ... "linha" is null`).
- **Alterações (GestorDeDVs):**
  - Variável `fila_dvs` (`ArrayList<Trem>`).
  - `chegar_nas_dvs(trem)`: entra direto se não há manobra; senão vai para a fila e espera parado na entrada (log `aguardando_dvs`).
  - `liberar_proxima_manobra()`: o primeiro da fila entra (log `entra_da_fila`). Chamada no `finalizar_manobra` (saída) e no caso da loco sozinha: no `sel_loco_sozinha.onExitTrue` se o lote já foi colocado, senão no `lote_estacionado.onExit` depois do `colocar_lote`.
  - `get_capacidade(null)`: devolve 0 com `Log.warn` (`linha_nula`).
- **Alterações (GestorDeCirculacao):** Exits `para_DVS` e `alivio_para_DVs` chamam `gestorDeDVs.chegar_nas_dvs` em vez de `entrarNasDVs.take`.
- **Risco a validar:** o trem da fila fica parado no fim da circulação, na entrada das DVs; se a manobra em andamento passar por esse trecho, os trens podem se bloquear.
- **Backup:** `_backup_antes_alt114/`
- **Arquivos:** `_alp/Agents/GestorDeDVs/{Code/Functions.java,Code/Functions.xml,Variables.xml,EmbeddedObjects.xml}`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`

## ALT-115 — alivio-pst-saida-pela-via1-intermar-c
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando compilar e validar
- **Problema:** log de 2026-11-12 (5,717h): `There is no straight route from via1_intermar_c, offset 319 to via1_intermar_d, offset 0` no `ir_via1_intermar_d`. Alívio do CoperXI com 44 vazios (MNB19): no `avancoAlivio` (`MoverTremCompleto_Ponto` até `pos_aux_alivio_pst` com `verificarUltimo`) a ponta do trem passou da `via1_intermar_d` para a `via1_intermar_c`. O `configurar_para_circulacao` mandava sempre para a d (offset 0), que já tinha ficado atrás da ponta. Além disso, o trem entrou na c **sem licença** (só a d, faixa de criação, estava apreendida).
- **Alterações:**
  - `Alivio.get_faixas_saida()`: faixas de `get_primeiras_linhas_de_circulacao_vazios()` do terminal, sem a faixa de criação (CoperXI, T39, TES e ADM: `via1_intermar_c`; terminais que devolvem null: nenhuma).
  - `Alivio.getConjuntoRecursos_faixa_criacao()`: faixa de criação + faixas de saída no mesmo conjunto (um Seize só, evita o trem segurar a d esperando a c). `seize_faixa_criacao_pst.onExit` ocupa as faixas de saída com o id da loco.
  - `fazer_procedimento_de_alivio_pst()`: só começa o alívio com as faixas de saída LIVRES (mesmo padrão da faixa de criação).
  - `liberar_faixas_pst`: era Release no modo padrão (soltava tudo o que o trem tinha, inclusive as faixas debaixo dele, sem atualizar o estado). Passa a `ALL_FROM_SEIZES` com `bloquear_faixas_pst`, `usar_recurso_loco_pst` e `seize_alivio_pst`.
  - Release novo `liberar_faixas_fora_do_trem_pst` (depois do `liberar_faixas_pst`): `Alivio.liberar_faixas_fora_do_trem(trem, faixas)` solta as faixas que o trem ainda possui mas onde ele não está mais (log `retorno_alivio`). As faixas debaixo do trem ficam com ele e a circulação libera quando ele sai. Substitui a liberação da faixa de criação que ficava no `retornar_alivio.onEnter` (removida).
  - `configurar_para_circulacao`: se o trem está na `via1_intermar_c`, entra por c → b; senão d → c. Faixa atual/próxima que o trem já possui fica LICENCIADA (a circulação não tenta apreender de novo). Aviso se o trem chegou na `via1_intermar_b`. Log `retorno_alivio` com a faixa de entrada.
- **Efeito colateral esperado:** Tgrao — as faixas do `ocupar_saida_alivio` (6000_a, marginal_c, silosMuro_a, via3_c) deixam de ser soltas no retorno e passam a ser liberadas pelo laço da circulação, como a ALT-009 previa.
- **Riscos a validar:** (1) a `via1_intermar_c` fica apreendida durante todo o alívio do CoperXI/T39/TES; (2) o `liberar_faixas_pst` deixou de ser "solta tudo": recurso de terminal que o trem não tenha devolvido passa a ficar com ele.
- **Backup:** `_backup_antes_alt115/`
- **Arquivos:** `_alp/Classes/Class.Alivio.java`, `_alp/Agents/GestorDeAlivios/{EmbeddedObjects.xml,Connectors.xml,Code/Functions.java}`
## ALT-116 — coperxxi-pode-receber-novo-lote
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **O que:** `podeReceberNovoLote()` do CoperXXI passa a recusar trem enquanto um trem está entrando (`_trem_chegando`, do `comecar_encoste` ao `entrar_moega`) ou enquanto há carregados no terminal (`get_vagoes_carregados() == 0`, regra do Guilherme).
  - Corrige a batida no `_coperXII_encoste` (erro `ir_encoste.outHit` sem ligação): o ISN liberava o 2º trem com a segunda parte do 1º ainda no encoste.
  - Variável nova `_trem_chegando` (Trem).
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.java`
## ALT-117 — moegoes-pode-receber-novo-lote
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **O que:** MoegaoMar e MoegaoTerra recebem a mesma regra da ALT-116: `podeReceberNovoLote()` recusa trem enquanto um trem está entrando (`_trem_chegando`, do `comecar_encoste` ao `entrar_moega`) ou com carregados no terminal (`get_vagoes_carregados() == 0`).
  - Variável nova `_trem_chegando` (Trem) nos dois agentes.
- **Arquivos:** `_alp/Agents/{MoegaoMar,MoegaoTerra}/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/{MoegaoMar,MoegaoTerra}/Code/Functions.java`
## ALT-118 — alivio-pst-segura-faixa-de-criacao
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **O que:** no retorno do alívio PST, `Alivio.liberar_faixas_fora_do_trem` não solta mais a faixa de criação (`via1_intermar_d`) quando o trem ainda não passou por ela (exceto Tgrao, que sai pela marginal_c). A circulação libera a faixa quando o trem sair dela.
  - Corrige a colisão no `ir_entrada_terminal_pst`: o alívio do MoegaoMar devolvia a `via1_intermar_d` ainda no terminal, o alívio do MoegaoTerra começava e a loco nova encontrava o trem de volta no corredor não licenciado (`linhaPaqueta_a` → `psnSaidaCopexXXI_a` → `pstSaidaMoegao`).
- **Arquivos:** `_alp/Classes/Class.Alivio.java`
## ALT-119 — costaleite-linha-terra-sem-vazios
- **Data:** 2026-11-12
- **Status:** aplicada pelo Guilherme no AnyLogic, aguardando validação
- **O que:** `podeReceberNovoLote()` do CostaLeite também devolve false enquanto `_loteDeVazios_LinhaTerra != null`.
  - O encoste dos carregados recua até o `_costaLeite_linhaTerra` (`mov_recuar_linhaTerra`) e deixa 14 vagões na linha Terra, a mesma linha do lote de vazios. Com o alívio travado nas DVs, o trem novo entrava e batia nos vazios (`mov_recuar_linhaTerra.outHit`).
- **Arquivos:** `_alp/Agents/CostaLeite/Code/Functions.java`
## ALT-120 — coperxxi-alivio-respeita-quantidade
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **O que:** o alívio do CoperXXI passa a levar só a quantidade definida pelo GestorDeAlivios (`set_quantidade_alivio`), no padrão do CoperXI.
  - Variáveis novas `quantidade_alivio` (41) e `qt_vgs_inicio_alivio` (gravada no `iniciar_alivio`).
  - Depois de cada engate (lote_b → lote_a → moega), `rmv_excesso` (TrainDecouple, primeiros carros) corta `max(0, trazidos − quantidade_alivio)`; o excesso volta para o estoque da própria linha (`guardar_excesso_alivio`) e continua como `lote_vazios_x`.
  - `precisa_mais_vgs` decide entre engatar o próximo lote (`alivio_seguir`) ou voltar (`fim_alivio`).
  - Funções novas `get_vgs_trazidos_alivio`, `guardar_excesso_alivio`.
  - Corrige o impasse nas DVs: o gestor pedia 6 (faltavam 6 para 82) e o CoperXXI entregava 41; o acumulado passava de 82 e o trem nunca era formado.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{java,xml}`, `_alp/Agents/CoperXXI/Levels/Level.level.xml`
## ALT-121 — alivio-pst-chegada-por-batida
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **O que:** `ir_entrada_terminal_pst.outHit` do GestorDeAlivios passa a ter ligação (Exit `bateu_entrada_terminal_pst`). Se a loco encostou no lote na linha de destino antes do ponto de alívio, conta como chegada (`takeAlivio`) e registra `chegada_por_batida`; batida em outra linha continua parando a simulação com mensagem.
  - Corrige o erro do 2º alívio do MoegaoTerra: o ponto de alívio coincide com a cabeça do lote_b e a loco encostava antes de chegar a ele.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/{EmbeddedObjects,Connectors}.xml`
## ALT-123 — log-de-analise-coperxi-coperxxi
- **Data:** 2026-11-12
- **Status:** aplicada, aguardando validação
- **O que:** variável `log_de_analise` (boolean, inicial true) e função `log_analise(evento, agente, msg)` no CoperXI e no CoperXXI. Com a variável em true, cada bloco do fluxo escreve no log (classe `Log`, origem = nome do agente, evento = nome do bloco):
  - SelectOutput: resultado (true/false); TrainCouple: "acoplou"; TrainDecouple: "vai cortar", "parte que segue", "parte cortada";
  - TrainMoveTo: "inicio", "chegou", "bateu (outHit)"; Seize: "na fila", "pegou"; Release: "soltou"; Wait: "esperando", "liberado"; Hold: "passou"; Delay: "inicio", "fim"; TrainExit/TrainEnter: saiu/entrou na via.
  - Cada linha traz trem, carros, vagões e locos. A lógica não muda.
  - CoperXXI: 48 blocos (97 pontos). CoperXI: 121 blocos (237 pontos).
- **Arquivos:** `_alp/Agents/{CoperXI,CoperXXI}/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/{CoperXI,CoperXXI}/Code/Functions.{java,xml}`
## ALT-124 — coperxxi-alivio-parcial-sobra
- **Data:** 2026-11-13
- **Status:** aplicada, aguardando validação
- **O que:** alívio parcial do CoperXXI (pedido de 1 a 41) no padrão do Costa Leite: a sobra sai do trem, volta para a posição e fica no wait.
  - `estoque_b`, `estoque_a` e `estoque_moega` passam de Hold para Wait. `liberar_estoque(linha)` solta o lote no `acoplar_x` (no lugar de `unblock`).
  - O alívio engata sempre todos os lotes (b → a → moega), para a moega nunca ficar ocupada por vazios com b/a vazios. `precisa_mais_vgs` = `ainda_tem_lote_vazios()`; `rmv_excesso` só corta no último engate.
  - A sobra (rabo, lado da moega) vai para `entrar_sobra` → `linha_livre_sobra` (espera de 30 s em `espera_linha_sobra` até o trem do alívio sair da `psnSaidaCopexXXI_b`) → `ir_sobra` (até `_coperXXI_lote_b`) → `sobra_cabe` / `cortar_sobra` → `guardar_sobra`. Reparte em b (22) → a (13) → moega.
  - A descarga (lote 2) só volta depois que a sobra é guardada: `retornar_gestorDeAlivios` não libera se `trem_sobra != null`; `guardar_sobra(..., true)` libera.
  - Variáveis novas `trem_alivio` e `trem_sobra`. Funções novas `ainda_tem_lote_vazios`, `linha_livre_para_sobra`, `get_capacidade_linha`, `sobra_linha_alvo`, `registrar_sobra`, `guardar_sobra`, `liberar_estoque`.
  - A sobra anda sem loco (simplificação, como no Costa Leite).
  - Correção (teste com pedido 4): a sobra fica com a frente virada para o encoste; `ir_sobra` anda de ré (`forward = false`) e `cortar_sobra` corta os últimos carros (`decoupleFirstCars = false`), que são o lado do lote_b.
- **Validar:** sentido dos "primeiros carros" no `cortar_sobra` (parte cortada deve ser a do lado do lote_b); `getTrack(true/false)` do trem do alívio; posição dos pedaços em relação a `_coperXXI_lote_a`; pedidos 41, 38, 32, 20, 6 e 1.
- **Backup:** `_backup/ALT-124/` (agente antes da alteração e o script `alt124.py`).
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{java,xml}`
## ALT-126 — coperxxi-sobra-com-loco-pontos-aux
- **Data:** 2026-11-13
- **Status:** aplicada, aguardando validação
- **O que:** a sobra do alívio parcial do CoperXXI é deixada peça a peça pela própria loco, sem andar sozinha (substitui a sobra da ALT-124).
  - Pontos na `psnSaidaCopexXXI_b` acertados para vagão de 17,5 m: `_coperXXI_lote_b_aux_mnb` 370 → 399 (cauda do b, 14 + 22 × 17,5), `_coperXXI_lote_a` 389,239 → 399, `_coperXXI_lote_a_aux_mnb` 565 → 626,5 (cauda do a, 399 + 13 × 17,5), `_coperXII_moega` 594,239 → 626,5 (os 35 de b + a não cabiam antes da moega).
  - O alívio engata b → a → moega. No fim, `proxima_peca()` corta a sobra do rabo (lado da moega): moega (`s − 35`, no lugar), a (`mover_peca_a`: cauda no `a_aux`, até 13) e b (`mover_peca_b`: cauda no `b_aux`). `cortar_peca` (primeiros carros) e `guardar_peca()` guardam cada peça no estoque da linha. `cauda_no_ponto()` evita mover distância 0.
  - `engatar_moega` vai até `_coperXII_encoste` e acopla ao bater no lote da moega (antes parava no ponto da moega e travava se o lote não estivesse encostado).
  - Removidos da ALT-124: `entrar_sobra`, `linha_livre_sobra`, `espera_linha_sobra`, `ir_sobra`, `sobra_cabe`, `cortar_sobra`, `para_guardar_sobra`, `para_guardar_sobra_parte`, `rmv_excesso`, `para_guardar_excesso`; funções `guardar_excesso_alivio`, `linha_livre_para_sobra`, `sobra_linha_alvo`, `registrar_sobra`, `guardar_sobra`; variáveis `trem_alivio`, `trem_sobra`. `retornar_gestorDeAlivios` volta a liberar a descarga direto.
  - Variáveis novas `_peca_linha`, `_peca_vagoes`.
  - Correção (teste com pedido 4, travou no 4º alívio): a peça da moega era cortada no lugar, no rabo do trem, e o lote 2 completava do lado do encoste; o lote da moega andava +70 m por ciclo até passar do encoste e o `engatar_moega` não batia nele. Agora `mover_peca_moega` (TrainMoveTo de ré, `maxDistance = distancia_peca_moega()`) leva a cauda até `_coperXII_moega` + peça antes do corte: a peça começa no ponto da moega e a cauda já fica no `a_aux` e no `b_aux` para os cortes seguintes.
- **Validar:** sentido do `MoverTremCompleto_Ponto` (`vaiRecuar = false`, `sentidoPontaDaPraia = false`); a frente do trem entra até ~340 m na `linhaCostaLeita_a` / `linhaPaqueta_a` sem licença (pendência); pedidos 41, 38, 32, 20, 6, 4 e 1.
- **Arquivos:** `_alp/Agents/CoperXXI/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/CoperXXI/Code/Functions.{java,xml}`, `_alp/Agents/Main/Levels/Level.level.xml`
## ALT-127 — coperxxi-alivio-sem-carga
- **Data:** 2026-11-13
- **Status:** aplicada, aguardando validação
- **Problema:** com alívio de 6, o último grupo do lote deixou a moega com 5/6 e o terminal com 40 vazios (b=22, a=13, moega=5). O alívio só era pedido com o terminal cheio (41), então ninguém pedia o último e o terminal ficava parado. Um trem novo que chegasse ficaria preso no `aguarda_espaco_vazios` (deadlock).
- **O que:**
  - `verificar_alivio()`: além do terminal cheio, pede alívio quando não há carregados no terminal (`get_vagoes_carregados() == 0`) e há ao menos 1 vazio. Não pede de novo com `aguardando_alivio` ou `fazendoAlivio`. Log novo `CoperXXI pedir_alivio`.
  - `retornar_gestorDeAlivios.onExit`: chama `verificar_alivio()` no fim, para encadear os alívios até zerar os vazios.
  - `podeReceberNovoLote()`: recusa trem enquanto `fazendoAlivio` (loco do alívio manobrando no terminal).
  - O gatilho do GestorDeAlivios continua `getOverloadVazios() > 1` (vale 2 com `aguardando_alivio`).
- **Validar:** com qtd 6, após o último grupo, alívios encadeados até b, a e moega ficarem vazios; trem novo não entra durante a manobra do alívio.
- **Arquivos:** `_alp/Agents/CoperXXI/Code/Functions.java`, `_alp/Agents/CoperXXI/EmbeddedObjects.xml`
## ALT-128 — coperxxi-peca-corta-no-lugar
- **Data:** 2026-11-13
- **Status:** aplicada, aguardando validação
- **Problema:** com os alívios encadeados da ALT-127 (pedido 4), o terminal passou a ser esvaziado só pelo lote b. A cada alívio o `mover_peca_b` reposicionava o trem e o lote b foi escorregando. Com 14 vgs, o trem não cobria mais o `_coperXXI_lote_b_aux_mnb` e o `irParaPonto` do `MoverTremCompleto_Ponto` deu `There is no straight route from psnSaidaCopexXXI_b, offset 242 to ... offset 516`.
- **O que:** quando a linha da peça é a última que o alívio engatou, a cauda do trem é a cauda do próprio lote, que não se moveu: a peça é cortada no lugar, sem `mover_peca_*`.
  - Variável nova `_ultima_linha_engatada` (String), gravada no `para_fim_alivio.onExit` antes de zerar `etapa_alivio`.
  - `proxima_peca()`: ramos a e b cortam no lugar se `_ultima_linha_engatada` for a linha da peça (ou se a cauda já estiver no ponto aux, como antes). Ramo da moega sem mudança.
- **Validar:** pedido 4 até zerar o terminal só pelo lote b (22 → 18 → 14 → … → 0); lote a cortado no lugar quando o alívio não engata a moega.
- **Arquivos:** `_alp/Agents/CoperXXI/Code/Functions.java`, `_alp/Agents/CoperXXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXXI/Variables.xml`
## ALT-129 — coperxxi-descarga-espera-alivio
- **Data:** 2026-11-13
- **Status:** aplicada, aguardando validação
- **Problema:** com a ALT-127 o alívio é pedido com o terminal sem carga. Um trem novo entrou enquanto o alívio estava pedido (`aguardando_alivio`), o lote 1 começou a descarregar (havia 1 vaga no lote a) e a loco do alívio engatou ao mesmo tempo. O grupo da descarga acoplou no `acoplar_a` com `etapa_alivio = "a"` e foi tratado como trem do alívio (`proxima_peca`), dando erro de rota no `mover_peca_b` (`offset 868 to offset 516`).
- **O que:** descarga e alívio nunca ao mesmo tempo. Função nova `pode_descarregar_grupo()` = `get_espaco_vazios() > 0 && !aguardando_alivio && !fazendoAlivio`, usada na `condition` e no `onEnter` do `tem_espaco_vazios`. O lote espera no `aguarda_espaco_vazios` até o fim do alívio (`liberar_espaco_vazios()` no `retornar_gestorDeAlivios`).
- **Validar:** trem que chega com alívio pedido encosta e espera; a descarga começa só depois de `alivio terminou`.
- **Arquivos:** `_alp/Agents/CoperXXI/Code/Functions.{java,xml}`, `_alp/Agents/CoperXXI/EmbeddedObjects.xml`
## ALT-130 — coperxxi-recebe-so-vazio
- **Data:** 2026-11-13
- **Status:** aplicada, aguardando validação
- **Problema:** com alívio pedido pela ALT-127 (vazios MRS), o ISN liberou um trem VLI. Na chegada ele trocou a ferrovia em atendimento para VLI e o alívio passou a pedir loco VLI para os vazios MRS. Sem loco VLI livre (estavam nos lotes do trem novo), o lote esperava o alívio e o alívio esperava a loco: travou (`aguardando 1 loco(s) 'vli' para aliviar`).
- **O que:** terminal só recebe trem depois de esvaziar. `podeReceberNovoLote()` recusa enquanto `aguardando_alivio || fazendoAlivio` (antes só `fazendoAlivio`).
- **Motivo:** a ferrovia e o segmento do terminal são analisados na chegada do carregado; se houver vazio VLI e chegar MRS, o alívio pede a loco errada e o modelo trava.
- **Doc:** `docs/terminais/coperxxi.md` criado (regras de recebimento e alívio das ALT-127 a 130).
- **Validar:** trem seguinte fica no ISN até `pedir_alivio` parar e os vazios zerarem; alívios do trem anterior usam a loco da ferrovia dele.
- **Arquivos:** `_alp/Agents/CoperXXI/Code/Functions.java`, `docs/terminais/coperxxi.md`
## ALT-131 — coperxxi-grupo-bate-sem-lote
- **Data:** 2026-11-13
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e conferir os blocos ligados e a compilação)
- **Problema:** teste com alívio de 19: o alívio levou moega + lote a e deixou o lote a vazio. O grupo seguinte da descarga (`ir_lote_a`) encostou na cauda do lote b (mesmo ponto 399) e saiu pelo `outHit` para o `acoplar_a`, que ficou esperando um lote do `estoque_a` vazio. Terminal travado.
- **O que:** SelectOutput novos `tem_lote_a` e `tem_lote_b` entre o `outHit` do `ir_lote_*` e o `acoplar_*`. Condição `estoque_x.size() > 0`: true → `acoplar_x.in1`; false → `para_chegou_vazios*` (caminho do "chegou").
  - Removidos os conectores `ir_lote_a.outHit → acoplar_a.in1` e `ir_lote_b.outHit → acoplar_b.in1`; criados 6 conectores (connector1118 a 1123).
- **Doc:** `docs/terminais/coperxxi.md`, seção "Grupo que bate sem lote na linha".
- **Validar:** alívio de 19 até a 2ª parte descarregar no lote a vazio (`tem_lote_a | false` e depois `guardado (coperXII_vazios_a)`).
- **Arquivos:** `_alp/Agents/CoperXXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXXI/Connectors.xml`, `docs/terminais/coperxxi.md`

## ALT-132 — tgrao-pode-receber-novo-lote
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `podeReceberNovoLote()` do Tgrao retornava sempre `true`. No log, o Y42 saiu da circulação para o terminal às 2,996h e, 15 s depois, o ISN liberou o X34 da linha 02 (`liberado: tgrao pode receber`), com o Y42 ainda entrando.
- **O que:**
  - Variável nova `_trem_chegando` (Trem, null).
  - `comecar_encoste.onEnter`: `_trem_chegando = (Trem) agent;`
  - `tirarLote.onExitDecoupled`: `_trem_chegando = null;` (1º lote na linha mar)
  - `podeReceberNovoLote()`: recusa com `_trem_chegando != null` ou `_getVagoesCarregados() > 0`.
  - Removido o `traceln` de demonstração do `colocarLinhaMar.onEnter` ("bom dia Bruno").
- **Decisão:** um trem por vez no terminal (mesmo padrão de CoperXI, MoegaoMar e CostaLeite).
- **Doc:** `docs/terminais/tgrao.md`, seção "Recebimento de trem".
- **Validar:** X34 fica estacionado na linha 02 até o Y42 descarregar linha mar e moega; `liberado_linha02` só depois dos `lote_*` ficarem null.
- **Arquivos:** `_alp/Agents/Tgrao/Code/Functions.java`, `_alp/Agents/Tgrao/EmbeddedObjects.xml`, `_alp/Agents/Tgrao/Variables.xml`, `docs/terminais/tgrao.md`

## ALT-133 — tgrao-nao-recebe-com-vazios
- **Data:** 2026-11-16
- **Status:** aplicada no AnyLogic pelo Guilherme (código colado no editor), aguardando validação
- **Problema:** com as DVs cheias (108/108), o alívio do Tgrao foi recusado (`cabem=0`) e os 40 vazios do Y94 ficaram na linha mar. O `podeReceberNovoLote()` olhava só os carregados e liberou o Y92, que encostou nos vazios e saiu pelo `encostar_linhaMar.outHit` sem ligação: `Agent can't leave the port root.tgrao.encostar_linhaMar.outHit`.
- **O que:** `podeReceberNovoLote()` também recusa com `_getVagoesVazios() > 0`.
- **Doc:** `docs/terminais/tgrao.md`, seção "Recebimento de trem".
- **Fora do escopo (visto no mesmo log):** alívio do Tgrao saiu com 2 vagões definidos e 28 de espaço nas DVs (regra exige mais de 40) e o MNB levou 40; DVs com 108 rumo/grão não formam trem de 82 para sair.
- **Validar:** com DVs cheias, o trem da linha 02 fica estacionado até o alívio do Tgrao acontecer.
- **Arquivos:** `_alp/Agents/Tgrao/Code/Functions.java`, `docs/terminais/tgrao.md`

## ALT-134 — celulose-pode-receber-novo-lote
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `podeReceberNovoLote()` de Bracell, Eldorado e Suzano retornava sempre `true`.
- **Regra:** celulose recebe um trem por vez; o próximo só entra depois de descarregar e aliviar (o trem sai com todos os vagões).
- **O que:**
  - Variável nova `_trem_chegando` (Trem, null) nos três terminais.
  - **Bracell:** `encoste.onEnter` preenche; cada `loteVazios_L1..L4 = ...` zera. `podeReceberNovoLote()`: recusa com `_trem_chegando`, carregados ou vazios.
  - **Eldorado:** `comecar_encoste.onEnter` preenche; cada `lote_vazios_linha01..04 = ...` zera. `podeReceberNovoLote()`: recusa com `_trem_chegando` ou vazios (`_getVagoesCarregados()` retorna 0 fixo).
  - **Suzano:** `comecar_encoste.onEnter` preenche; `lote_L7000 = ...` e cada `lote_carregados_L1..L4 = ...` zeram. `podeReceberNovoLote()`: recusa com `_trem_chegando`, `em_troca`, carregados ou vazios.
- **Observação:** na Bracell os `lote_carregados_L*` nunca são atribuídos, então `_getVagoesCarregados()` também é sempre 0; a descarga fica coberta pelo `_trem_chegando` até o 1º lote de vazios.
- **Doc:** `docs/terminais/bracell.md`, seção "Recebimento de trem" (Eldorado e Suzano sem ficha .md).
- **Validar:** 2 pedidos seguidos para cada terminal; o 2º trem fica na linha 02 do ISN até o alívio do 1º sair.
- **Arquivos:** `_alp/Agents/{Bracell,Eldorado,Suzano}/{Code/Functions.java,EmbeddedObjects.xml,Variables.xml}`, `docs/terminais/bracell.md`

## ALT-135 — celulose-entra-pelo-gestor-de-chegadas
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** o trem de teste de Suzano, Eldorado e Bracell saía do TrainSource direto para `gestorDeCirculacao.chegada_carregados`, sem chamar `receber_trem()`. O `pedido_atual` do GestorDeChegadas ficava preso no 1º pedido e a fila parava (P2 a P16 sem decisão); o trem rodava sem prefixo (`null`) e sem passar pelo ISN.
- **O que:** `onExit` do Exit de chegada dos carregados (`paraGestorCirculacao_chegadaCarregados` na Suzano e na Eldorado; Exit equivalente na Bracell) passa a chamar `main().gestorDeChegadas.receber_trem((Trem) agent);`, igual ao Tgrao e ao CostaLeite.
- **Validar:** pedidos seguidos para os três terminais; cada trem recebe prefixo, passa pelo ISN e a fila anda (`decisao` / `aguardando`).
- **Arquivos:** `_alp/Agents/{Suzano,Eldorado,Bracell}/EmbeddedObjects.xml`

## ALT-136 — log-de-analise-tgrao-celulose
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Motivo:** o modelo travou sem erro com Tgrao, Bracell, Eldorado e Suzano atendendo ao mesmo tempo; falta rastro das manobras.
- **O que:** variável `log_de_analise` (boolean, true) e função `log_analise(evento, agente, msg)` no Tgrao, Bracell, Eldorado e Suzano, no padrão da ALT-123. Cada bloco da biblioteca escreve no log (classe `Log`, origem = nome do agente, evento = nome do bloco):
  - mesmos pontos da ALT-123, mais Enter ("entrou") e Exit ("saiu");
  - blocos próprios do modelo (MoverTremCompleto, DescarregaLote, DecoupleDelay etc.) não foram instrumentados;
  - Tgrao 74 blocos (107 pontos), Bracell 72 (114), Eldorado 80 (130), Suzano 107 (165). A lógica não muda.
- **Arquivos:** `_alp/Agents/{Tgrao,Bracell,Eldorado,Suzano}/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/{Tgrao,Bracell,Eldorado,Suzano}/Code/Functions.{java,xml}`

## ALT-137 — suzano-solta-linha6000-no-alivio
- **Data:** 2026-11-16
- **Status:** validada em 2026-11-16 (2 trens seguidos na Suzano; a troca do 2º passou pelo `ocupar_faixas_triangulacao`)
- **Problema:** no 2º trem da Suzano a loco FIPS da troca ficava para sempre em `ocupar_faixas_triangulacao | na fila`. O `ocupar_faixas_mnb_alivio` do alívio anterior apreende `rp_pmc_marginal_c` e `rp_linha6000`; a marginal_c volta pela circulação, mas a `rp_linha6000` não é faixa e nenhum Release a soltava. O trem do alívio saía da Margem Direita com a unidade.
- **O que:** Release novo `liberar_linha6000_alivio` (SPECIFIED_RESOURCES, `main().gestorDeCirculacao.get_recursos_linha_5000_6000((Trem) agent)`) entre o `pausar` e o `para_sair_alivio`.
  - Conector `connector52` (`pausar.port1`) passa a ir para `liberar_linha6000_alivio.in`; conector novo `conn_liberar_linha6000_alivio` (`liberar_linha6000_alivio.out` → `para_sair_alivio.in`).
- **A verificar:** a Eldorado tem `ocupar_linha5000_6000` com `rp_linha6000`; conferir se é solto no fim do alívio.
- **Validar:** 2 trens seguidos na Suzano; na 2ª troca aparece `ocupar_faixas_triangulacao | pegou`.
- **Arquivos:** `_alp/Agents/Suzano/EmbeddedObjects.xml`, `_alp/Agents/Suzano/Connectors.xml`

## ALT-138 — eldorado-solta-linha6000
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** o trem de carregados do Eldorado apreende a `rp_linha6000` (capacidade 1) no `ocupar_linha5000_6000` e nenhum Release a soltava. O `liberar_faixas_circulacao` só solta faixas (`liberar_faixas_ocupadas`). Depois dos decouples, a unidade fica com a loco, que saía pelo `devolverLocomotiva` com a linha 6000. Todo Seize seguinte da `rp_linha6000` esperava para sempre, sem erro: o 2º trem do Eldorado, o encoste da Bracell, o alívio da Suzano e o GestorDeCirculacao. É a pendência "A verificar" da ALT-137.
- **O que:** Release novo `liberar_linha5000_6000` (SPECIFIED_RESOURCES, `main().gestorDeCirculacao.get_recursos_linha_5000_6000((Trem) agent)`, `log_analise(..., "soltou")`) entre o `tira_10vgs3` e o `devolverLocomotiva`, igual ao da Bracell.
  - Conector `connector31` (`devolverLocomotiva.port`) passa a ligar no `liberar_linha5000_6000.out`. Conector novo `conn_liberar_linha5000_6000` (`tira_10vgs3.out` → `liberar_linha5000_6000.in`).
- **Validar:** 2 trens seguidos no Eldorado; no 2º aparece `ocupar_linha5000_6000 | pegou`. Depois, Eldorado junto com Bracell e Suzano.
- **Arquivos:** `_alp/Agents/Eldorado/EmbeddedObjects.xml`, `_alp/Agents/Eldorado/Connectors.xml`

## ALT-139 — prioridade-licenciamento-circulacao
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** deadlock com os 4 terminais de celulose atendendo. Às 9,169 h o L50 (Eldorado) licencia a `silosMuro_a`. Às 9,181 h a loco do 2º alívio da Suzano entra no `bloquear_faixas_pmc` pedindo `rp_silosMuro_a` + `rp_pmc_marginal_c` + `rp_pmc_linha6000_a` (apreender tudo de uma vez) e entra na fila dos três pools. O L50 pede a `marginal_c` (livre, 1/1) no `licenciar_proxima_faixa` e fica atrás da loco na fila do pool. A loco espera a `silosMuro_a` do L50 e o L50 espera a loco: nenhum anda. Confirmado no modelo pausado: 1 na fila do `bloquear_faixas_pmc`, 1 na fila do `licenciar_proxima_faixa`, `rp_pmc_marginal_c` 1/1 livre, `rp_silosMuro_a` 0/1.
- **O que:** prioridade da tarefa = 1 nos Seize `licenciar_esta_faixa` e `licenciar_proxima_faixa` do GestorDeCirculacao. Os Seize de várias faixas (`bloquear_faixas_pmc`, `ocupar_faixas_triangulacao`, `ocupar_faixas_mnb_alivio`, `ocupar_faixas_manobra`) continuam com 0.
- **Efeito colateral:** com trens seguidos na circulação, a loco do alívio pode esperar mais.
- **Validar:** mesmo cenário (Tgrao, Bracell, Eldorado e Suzano juntos); depois de `alivio_pmc suzano`, o trem do Eldorado entra na `pst_silosMuro_a` e vem `para_alivio_terminal_pmc ... -> suzano`.
- **Doc:** `docs/gestores.md`, seção GestorDeCirculacao.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-140 — loco-alivio-pmc-pede-linha6000
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** com a ALT-139 o deadlock sumiu e apareceu `ir_alivio_pmc.outHit: it has no connections`: a loco do alívio da Suzano (agente 279) bateu no trem do Eldorado (agente 213), que manobrava com a `rp_linha6000`. A loco do alívio PMC só pedia as faixas do terminal; o `ocupar_linha5000_6000` antes do `bloquear_faixas_pmc` (ALT-014/061/063) não está mais no fluxo do GestorDeAlivios (`loco_alivio_pmc → bloquear_faixas_pmc → trainEnter_pmc`), sem registro da remoção.
- **O que:** `get_conjunto_faixas_pmc()` inclui `main().gestorDeCirculacao.rp_linha6000` no mesmo conjunto quando o terminal é Suzano ou Eldorado. Com "apreender tudo de uma vez", a loco só nasce com as faixas e a linha 6000 livres. A Suzano (`liberar_recursos_loco_alivio`) e o Eldorado (`liberar_faixas_loco_alivio`) já soltam a 6000 pela `get_recursos_loco_alivio`.
- **Pendente:** Bracell fora: o alívio dela não tem Release da `rp_linha6000` (a loco levaria a unidade embora). Avaliar se a loco da Bracell também precisa da 6000.
- **Validar:** mesmo cenário dos 4 terminais; a loco da Suzano espera no `bloquear_faixas_pmc` até `Eldorado liberar_linha5000_6000 | soltou` e segue até `para_alivio_terminal_pmc ... -> suzano` sem batida.
- **Doc:** `docs/gestores.md`, seção GestorDeAlivios.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `docs/gestores.md`

## ALT-141 — eldorado-segura-marginal-c-no-encoste
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `eldorado.avancar3.outHit: it has no connections`. O `_eldorado_mnb_encoste` (destino dos `avancar`…`avancar3`) fica na `pst_marginal_c`: a cada lote o trem do Eldorado manobra pela `pmc_linha6000_a` até a marginal_c. O `liberar_faixas_circulacao` (0,945 h) soltava as duas faixas na entrada do terminal; às 1,105/1,110 h o GestorDeCirculacao licenciou marginal_c e linha6000_a para o L42 da Suzano e o `avancar3` bateu nele. A `rp_linha6000` do Eldorado não protege: a circulação usa `rp_pmc_marginal_c` e `rp_pmc_linha6000_a`.
- **O que (Eldorado):**
  - Função nova `get_faixas_circulacao_liberar(Trem)`: igual à `liberar_faixas_ocupadas` do GestorDeCirculacao, mas pula `faixa_marginal_c` e `faixa_linha6000_a`. Usada no `liberar_faixas_circulacao`.
  - Função nova `get_recursos_fim_encoste(Trem)`: linha 5000/6000 + `rp_pmc_marginal_c` + `rp_pmc_linha6000_a` (só os que o trem possui) e `liberar()` nas duas faixas. Usada no `liberar_linha5000_6000` (depois do `tira_10vgs3`), no lugar da `get_recursos_linha_5000_6000`.
- **Efeito:** a circulação para Suzano e Bracell espera o fim dos encostes do Eldorado (~0,3 h).
- **Validar:** Eldorado e Suzano juntos; o trem da Suzano espera na `pst_marginal_c`/`linha6000_a` até `Eldorado liberar_linha5000_6000 | soltou`, sem batida no `avancar*`.
- **Doc:** Eldorado ainda sem ficha em `docs/terminais/`.
- **Arquivos:** `_alp/Agents/Eldorado/EmbeddedObjects.xml`, `_alp/Agents/Eldorado/Code/Functions.{java,xml}`

## ALT-142 — recurso-mnb-pcx
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** travas por fila em ordem entre Seize de conjunto (alívio, triangulação) e trens que já seguravam faixas (ALT-139 e log das 15:38: troca da Suzano × L22 do Eldorado na `rp_linha6000`). Tgrao, Bracell, Eldorado e Suzano compartilham pontos no encoste, triangulação, manobra e alívio; cada operação pegava as peças numa ordem diferente.
- **Regra:** `rp_mnb_pcx` (capacidade 1) no GestorDeCirculacao. Encoste, alívio dos 4 terminais e triangulação da Suzano pegam o `rp_mnb_pcx` **antes** de faixas e linhas. Trens que só passam pela área não pegam (ALT-139 dá prioridade a eles).
- **O que:**
  - **GestorDeCirculacao:** pool `rp_mnb_pcx`; funções `eh_terminal_mnb_pcx`, `get_recursos_mnb_pcx`, `incluir_mnb_pcx`; `para_via3_d` desvia os 4 terminais para `entrar_mnb_pcx → ocupar_mnb_pcx → para_licenciar_apos_mnb` (depois `tentar_usar_faixa`).
  - **Fim do encoste:** Bracell `liberar_linha5000_6000` e Suzano `liberar_faixas_circulacao` passam por `incluir_mnb_pcx`; Eldorado `get_recursos_fim_encoste` inclui o mnb_pcx; Tgrao Release novo `liberar_mnb_pcx` entre `tirarLote2` e `devolverLocomotiva`.
  - **GestorDeAlivios PMC:** Seize `ocupar_mnb_pcx_pmc` antes do `bloquear_faixas_pmc` (das duas entradas); `retornar_alivio_pmc` passa por `soltar_mnb_pcx_pmc → liberar_mnb_pcx_pmc → para_circulacao_alivio_pmc`.
  - **GestorDeAlivios PST:** SelectOutput `eh_alivio_tgrao` e Seize `ocupar_mnb_pcx_pst` antes do `seize_faixa_criacao_pst`; `liberar_faixas_pst` inclui `ocupar_mnb_pcx_pst` nos seizeBlocks.
  - **Suzano triangulação:** Seize `ocupar_mnb_pcx_triangulacao` entre `usar_recurso_loco1` e `ocupar_faixas_triangulacao`; `liberar_faixas_triangulacao` inclui o bloco nos seizeBlocks.
- **Efeitos:** uma operação por vez nos 4 terminais; o trem que espera o mnb_pcx fica no fim da `via3_c` e segura quem vem atrás na via3, inclusive trens de outros terminais. Enquanto o alívio do Tgrao espera o mnb_pcx, os outros alívios PST esperam junto.
- **A conferir:** no Tgrao, o trem que chega ao `retornar_alivio` tem que ser o mesmo agente que pegou o mnb_pcx no `ocupar_mnb_pcx_pst` (2 locos de manobra, `separarLocomotivas`). Se não for, o mnb_pcx não é solto e todo encoste seguinte fica `na fila do mnb_pcx`. A ALT-142 de prioridade proposta antes foi descartada.
- **Validar:** os 4 terminais juntos; procurar `na fila do mnb_pcx` / `pegou mnb_pcx` e os `soltou` no fim de cada encoste e alívio; nenhuma operação deve ficar na fila para sempre.
- **Doc:** `docs/gestores.md`, `docs/terminais/bracell.md`, `docs/terminais/tgrao.md`.
- **Arquivos:** `_alp/Agents/{GestorDeCirculacao,GestorDeAlivios,Tgrao,Suzano,Bracell}/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/GestorDeCirculacao/Code/Functions.{java,xml}`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `_alp/Agents/Eldorado/Code/Functions.java`

## ALT-143 — dvs-saida-licencia-val-via2
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `Train [460] and train [147] are trying to occupy the switch railwaySwitch134 simultaneously` (~1,41 h). O `railwaySwitch134` liga `val_via2_a`, `psn_linha_porteira` (saída do ISN) e `railwayTrack42`. O MNB01 (alívio da Bracell) saiu das DVs pelo `ir_saida` (move-to até `circ_pos_fim_placa_2`, sem licença de faixa) e o L94 saiu do ISN com a `val_via2` licenciada; os dois chegaram juntos no AMV.
- **O que (GestorDeDVs):** Seize novo `ocupar_val_via2_saida` (`main().gestorDeCirculacao.faixa_val_via2.recurso`) entre `sel_segue_direto.outT` / `sel_acao.outF` e o `ir_saida`; no onExit, `faixa_val_via2.ocupar(trem)`. O `liberar_faixas_circulacao` (depois do `trainExit`) solta a faixa junto com as outras.
- **Validar:** os 4 terminais juntos; nos trens que saem das DVs aparece `ocupar_val_via2_saida ... pegou` antes do `saida`, e trem do ISN espera a `val_via2` quando um MNB está saindo.
- **Doc:** `docs/gestores.md`, seção GestorDeDVs.
- **Arquivos:** `_alp/Agents/GestorDeDVs/{EmbeddedObjects,Connectors}.xml`

## ALT-144 — suzano-lote-6000d-e-trava-eldorado
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `tgrao.colocarTrem_L7.colocarCaudaNoPonto.outHit` (~1,59 h). O lote de 24 da Suzano (carregados e depois vazios) fica horas em `_suzano_encoste` (`pmc_linha7000_e`, ~130 m); com ~420 m, a cauda entra na `7000_d`, linha do Tgrao (`ocupar_7000_abcd`). O Tgrao recuou na 7000 e bateu nos 24 vazios.
- **Decisão (Guilherme):** o lote vai para a `pmc_linha6000_d` (~563 m), a 1 m do fim da via (lado da 6000_e). A triangulação usa o travessão `railwayTrack82` (6000_c/d ↔ 7000_c) até as linhas (`railwayTrack85`). Como a 6000_d é caminho obrigatório do Eldorado (6000_c → d → e → f), Suzano e Eldorado não atendem ao mesmo tempo.
- **O que:**
  - **Suzano:** `mov_avnc__L7000`, `mov_avnc__L7000a`, `mov_avnc__L700b` e `avancar_alivio_L7000` passam de `TARGET_AT_POINT_ON_TRACK` (`_suzano_encoste`) para `TARGET_AT_TRACK_OFFSET` na `pmc_linha6000_d`, `offsetFromBeginningOfTrack = false`, `targetOffset = 1 m`; `tracksToAvoid = { pmc_linha6000_e }` (antes evitavam a 6000_d).
  - **Suzano e Eldorado:** função nova `terminal_ocupado()` com a regra antiga do `podeReceberNovoLote()`; o `podeReceberNovoLote()` passa a ser `!terminal_ocupado() && !main().<outro>.terminal_ocupado()`.
- **A conferir:** rotas (se algum movimento exigir inversão no meio, sai `no route`); orientação do corte no encoste (`rmv_40vgs`: os 24 da frente ficam na 6000_d e os 40 de trás vão para as linhas); a triangulação continua passando pela 7000_c, que o Tgrao também usa.
- **Validar:** os 4 terminais; Suzano encosta e deixa os 24 na 6000_d; Tgrao não bate; pedidos do Eldorado ficam `terminal não pode receber` enquanto a Suzano estiver ocupada (e vice-versa).
- **Arquivos:** `_alp/Agents/Suzano/EmbeddedObjects.xml`, `_alp/Agents/{Suzano,Eldorado}/Code/Functions.{java,xml}`

## ALT-145 — suzano-ziguezague-6000c
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** com o lote na 6000_d (ALT-144), `mvp_ir_entrada_terminal`: *no straight route from railwayTrack114 ... to pmc_linha7000_c*. De ré pela 6000 não há caminho até a 7000_c; o travessão `railwayTrack82` liga 6000_c → 7000_c no sentido Ponta da Praia, então lote ↔ linhas exige zigue-zague com inversão na 6000_c.
- **O que:**
  - **Main (Level):** RailStopline nova `_suzano_inversao_6000c` na `pmc_linha6000_c`, offset 31 px (~40 m do `railwaySwitch81`).
  - **Suzano:** `posicaoNaVia` de `mvp_ir_entrada_terminal` (encoste, 40 → linhas), `recuar3` (triangulação, saindo do lote com os 24 carregados), `recuar2` (triangulação, indo deixar os 24 vazios), `recuar4` (triangulação, saindo do lote depois dos vazios) e `recuar_alivio_L7000` (alívio, indo buscar os 24 vazios) passa de `_suzano_entrada_terminal` para `_suzano_inversao_6000c`. Os movimentos para frente seguintes não mudam.
- **A conferir:** orientação dos trens depois dos acoplamentos (pode dar *no route*); saída do alívio da 6000_d para a circulação.
- **Arquivos:** `_alp/Agents/Main/Levels/Level.level.xml`, `_alp/Agents/Suzano/EmbeddedObjects.xml`

## ALT-146 — terminal-ocupado-trem-a-caminho
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Contexto:** o zigue-zague da ALT-145 funcionou (3 ciclos completos da Suzano: encoste, triangulação, alívio e saída pelas DVs).
- **Problema:** `eldorado.entrarNoTerminal.outHit` (~603,63 h). O P5 (Suzano, L02) foi injetado às 602,767 h e o P6 (Eldorado, L96) às 602,983 h: o `terminal_ocupado()` da Suzano dava false porque o L02 ainda estava na circulação (`_trem_chegando` só é preenchido no `comecar_encoste`). O L96 pegou o `mnb_pcx` quando a Suzano terminou o encoste e bateu no lote de 24 na 6000_d.
- **O que:** `terminal_ocupado()` da Suzano e do Eldorado retorna true também se houver trem em `main().gestorDeCirculacao.trens_em_circulacao` com `ITerminalMoega_destino == this`.
- **Validar:** pedidos alternados Suzano/Eldorado; o 2º fica `terminal não pode receber` até o 1º terminar o alívio.
- **Arquivos:** `_alp/Agents/{Suzano,Eldorado}/Code/Functions.java`

## ALT-147 — mnb-pcx-antes-da-via3c
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** deadlock a partir de 1,852 h (sem erro). O alívio do Tgrao (loco 26846) tinha o `mnb_pcx` e esperava o `ocupar_saida_alivio` (`linha6000_a`, `marginal_c`, `silosMuro_a`, **`via3_c`**); o Y94 (Tgrao carregado) segurava `via3_c`/`via3_d` e esperava o `mnb_pcx` (pedido no `para_via3_d`, ALT-142). Em cascata: L42 (Suzano) parado na via3 com a cauda na `val_via2`, MNB01 (Eldorado) preso no `ocupar_val_via2_saida`, alívio da Bracell na fila do `mnb_pcx`.
- **Causa:** a ALT-142 pedia o `mnb_pcx` depois de licenciar a `via3_c`, que também é usada pela saída do alívio do Tgrao (regra "mnb_pcx antes das faixas" quebrada).
- **O que (GestorDeCirculacao):** o desvio `eh_terminal_mnb_pcx → entrar_mnb_pcx` sai do `para_via3_d` e vai para o `para_via3_b` (fim da `via3_a`, antes de licenciar a `via3_c`). `para_via3_d` volta a chamar só o `tentar_usar_faixa`.
- **Efeito:** quem espera o `mnb_pcx` segura só a `via3_a`/`via3_b`, que nenhuma operação da área comum usa.
- **Doc:** `docs/gestores.md`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-148 — alivio-solta-mnb-pcx-apos-marginal-c
- **Data:** 2026-11-16
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `Train [603] and train [766] are trying to occupy the switch railwaySwitch82` (4,533 h). O MNB17 (alívio do Tgrao) soltou o `mnb_pcx` no `retornar_alivio` (ao sair do terminal) ainda fisicamente na 7000; no mesmo instante a loco de manobra do alívio da Suzano pegou o `mnb_pcx` e foi para a Suzano por `railwayTrack120` → 7000_b → AMV 82 (trecho sem faixa). Os dois chegaram juntos ao AMV.
- **O que:**
  - **GestorDeCirculacao:** `liberar_faixas_ocupadas(Trem)` inclui o `rp_mnb_pcx` (se o trem tiver a unidade) quando `trem.eh_alivio` e a `faixa_marginal_c` já não está licenciada para ele; log `soltou mnb_pcx (saiu da marginal_c)`.
  - **GestorDeAlivios:** `liberar_mnb_pcx_pmc` passa a soltar nada (`new ResourcePool[0]`); `liberar_faixas_pst` deixa de soltar o `ocupar_mnb_pcx_pst`.
- **Efeito:** o próximo da fila do `mnb_pcx` espera o trem do alívio sair da área comum (todos saem por `marginal_c → marginal_b`).
- **Validar:** 4 terminais; depois de cada `retornar_alivio`, o `soltou mnb_pcx (saiu da marginal_c)` aparece quando o trem já está na `marginal_b`/`marginal_a`, e só então o próximo `pegou mnb_pcx`.
- **Doc:** `docs/gestores.md`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeAlivios/{EmbeddedObjects.xml,Code/Functions.java}`, `docs/gestores.md`

## ALT-149 — pode-receber-novo-lote-pcx
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `podeReceberNovoLote()` do TES, ADM e T39 retornava sempre `true`. O GestorDeChegadas mandava trem direto para a circulação com a linha de chegada ocupada (no TES o trem ficava parado na circulação esperando a `faixa_linha3000_a`). No T39, o `encosta_L1K1_proximo` gravava `lote_linha_1000_emDescarga` em vez de `lote_linha_1000_proximo`, sobrescrevendo o lote em descarga.
- **O que:**
  - Nova variável `Trem _trem_chegando` (null) nos três agentes. Recebe o trem no `onEnter` do `comecar_encoste` (TES, ADM) / `encoste_linha1000` (T39); volta a null quando o trem para na linha de chegada (`mov_linha3K_ponto_a/b`, `mov_linha2K_ponto_a/b`, `encosta_L1K1_proximo/fim`).
  - `podeReceberNovoLote()`: false com `_trem_chegando != null`. TES: `lote_linha3000_emDescarga == null && lote_linha3000_proximo == null` (a 3000 comporta um trem). ADM: `lote_linha2000_proximo == null`. T39: `lote_linha_1000_proximo == null`.
  - T39 `encosta_L1K1_proximo.onEnter`: grava `lote_linha_1000_proximo`.
- **A confirmar:** se a 2000 e a 1000 comportam fisicamente dois trens de 82 (`emDescarga` + `proximo`). Se não, ADM e T39 passam a usar a condição do TES.
- **Validar:** dois pedidos seguidos para o mesmo terminal; o 2º fica `terminal não pode receber` (ou vai ao ISN) até o 1º parar na linha de chegada (TES: até a 3000 esvaziar).
- **Arquivos:** `_alp/Agents/{TES,ADM,T39}/{Variables.xml,EmbeddedObjects.xml,Code/Functions.java}`

## ALT-150 — adm-t39-linha-chegada-vazia
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `root.adm.mov_linha2K_ponto_a: Agent can't leave the port outHit: it has no connections` (~5,10 h). O Y42 voltou para a 2000 com 27 vagões (`fim_manobra`); pela ALT-149 o ADM podia receber (`proximo` livre), o X34 foi para o `mov_linha2K_ponto_a` e bateu na cauda do Y42. O `outHit` do bloco de `proximo` está desconectado nos três terminais (`mov_linha2K_ponto_a`, `mov_linha3K_ponto_a`, `encosta_L1K1_proximo`).
- **O que:** `podeReceberNovoLote()` do ADM e do T39 passa a exigir a linha de chegada vazia, como o TES: `emDescarga == null && proximo == null` (além de `_trem_chegando == null`). O 2º trem espera no ISN.
- **Pendência:** usar o `proximo` de fato (ponto de parada atrás do lote ou ligação do `outHit`) nos três terminais.
- **Validar:** vários pedidos seguidos para o ADM; o 2º fica na linha 02 do ISN até a 2000 esvaziar.
- **Arquivos:** `_alp/Agents/{ADM,T39}/Code/Functions.java`

## ALT-151 — adm-t39-zerar-em-descarga
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** depois da ALT-150 o ADM nunca mais recebeu trem: às 552 h os pedidos P3..P20 ainda esperavam e o X34 continuava na linha 02 do ISN. O `lote_linha2000_emDescarga` (ADM) e o `lote_linha_1000_emDescarga` (T39) nunca voltavam a null.
- **O que:**
  - ADM `so_locos_manobra.onExitTrue` (só locos, 2000 sem vagões): `lote_linha2000_emDescarga = null;`
  - T39 `liberar_linha_1000_emDescarga.onExit` (saída do `sobrou_so_loco`): `lote_linha_1000_emDescarga = null;`
- **Validar:** pedidos seguidos para o ADM; o X34 sai da linha 02 logo depois do `fim_manobra ... so com N loco(s)` do Y42.
- **Arquivos:** `_alp/Agents/{ADM,T39}/EmbeddedObjects.xml`

## ALT-152 — tes-novo-lote-espera-linhas
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `root.tes.ir_linha03: Agent can't leave the port outHit` (~1,82 h). O resto do Y42 saiu da 3000 para o encoste (1,478 h) e zerou o `lote_linha3000_emDescarga`; o Y02 foi liberado do ISN e chegou à 3000. O 1º encoste dele (`sooRumo_41vgs` → `comecar_encoste_moegas` → `seize` → `ir_linha03`) não conferia as linhas e bateu no lote 27008 (6 carregados que sobraram do alívio) na linha 03.
- **O que (TES):**
  - Novos blocos `esperar_encoste_3000` (Enter) → `wait_encoste_3000` (Wait) → `para_encoste_3000` (Exit, `comecar_encoste_moegas.take(agent)`).
  - `para_comecar_encoste_moegas.onExit`: `esperar_encoste_3000.take(agent)` (antes ia direto para o `comecar_encoste_moegas`).
  - Nova função `tentar_encostar_novo_lote_3000(boolean)`: solta o lote do `wait_encoste_3000` só com `lote_linha_3`, `lote_linha_4`, `lote_linha_04_fundo` e `lote_linha_5` null e `trem_em_encoste == null`. Chamada no `onEnter` do `wait_encoste_3000` e no começo do `tentar_encostar_resto_3000` (mesmos gatilhos do resto). Log `novo_lote_3000`.
- **Efeito:** o TES continua recebendo assim que a 3000 esvazia; o trem novo espera na 3000 até as linhas 03/04/05 liberarem.
- **Validar:** dois pedidos seguidos para o TES; o 2º mostra `novo_lote_3000 espera: ...` e só sai quando as linhas ficam livres.
- **Arquivos:** `_alp/Agents/TES/{EmbeddedObjects.xml,Connectors.xml,Code/Functions.java,Code/Functions.xml}`

## ALT-153 — t39-botao-pelo-gestor-de-chegadas
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `The car being added on the track 'isn_linha01_a' intersects with another car` (0,56 h) e, em seguida, `gestorDeCirculacao.ir_via3_intermar: The train is empty`. O botão "Encostar" do T39 chamava `tremTesteSource.inject()` direto, sem passar pelo GestorDeChegadas; o trem nasceu em cima do X34 (TES) na `isn_linha01_a` e entrou vazio na circulação (`(null - 26785): t39`, sem prefixo).
- **O que (T39):**
  - Botão "Encostar": `main().gestorDeChegadas.pedir_trem(this, "rumo", tremTesteSource);`
  - `para_encoste_linha1000.onExit`: `main().gestorDeChegadas.receber_trem((Trem) agent);` (antes `gestorDeCirculacao.chegada_carregados.take`).
- **Validar:** clicar no T39 com outro trem na `isn_linha01`; o pedido fica `entrada ocupada` e o trem nasce com prefixo.
- **Arquivos:** `_alp/Agents/T39/{Levels/Level.level.xml,EmbeddedObjects.xml}`

## ALT-154 — adm-liberar-vaga-2000
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** modelo travado a partir de 3,255 h, sem erro. O Y92 (2º trem do ADM) entrou no terminal às 2,220 h e parou no `ocupar_lote_em_descarga` (Seize do `linha2000_lote_em_descarga`), que nenhum bloco do ADM devolvia: o 1º trem (26700) ficou com a unidade até sair só com as locos. Como as faixas só são liberadas depois do `mov_linha2K_ponto_b`, a cauda do Y92 segurou a via 3: o I30 (TES) parou na `via3_a` e o I16 (T39) na entrada da circulação.
- **O que (ADM):** `rls_fim_manobra` (saída "só locos" do `so_locos_manobra`) solta `{ mnb_carregados, linha2000_lote_em_descarga }` (antes só `mnb_carregados`).
- **Validar:** pedidos seguidos para o ADM; o 2º trem passa do `ocupar_lote_em_descarga` logo depois do `fim_manobra ... so com N loco(s)` do 1º.
- **Arquivos:** `_alp/Agents/ADM/EmbeddedObjects.xml`

## ALT-155 — adm-seize-loco-fips-no-trem
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** travamento por falta de locos FIPS do PCX (`pediu_2_locos ... livres: 0`, ~6,4 h). O ADM fazia o Seize do `rp_locoFips_pcx` (`usar_recurso_loco`, `qt_loco = 2`) com o trem da loco criado pelo `loco_pcx_starts`; no `acoplar_loco_mnb` esse trem entra pelo `in2` e é destruído, levando as unidades junto. Nenhum Release do ADM devolvia (o `devolverLocomotiva2` só guarda a loco no `locomotivasPCX`). Ciclo só do ADM confirmou: 2 unidades presas por trem.
- **O que (ADM):**
  - `loco_pcx_starts.out` → `trainEnter` (a loco nasce sem Seize).
  - `tem_loco_mnb.outF` → `usar_recurso_loco` → `acoplar_loco_mnb.in1`: o Seize passa a ser do trem do ADM (que sobrevive ao acoplamento). Bloco movido para perto do `acoplar_loco_mnb`.
  - `rls_moema1` (antes do `devolverLocomotiva2`): `seizeBlocks = { licenciar_moema, usar_recurso_loco }`.
- **Pendência:** as locos FIPS devolvidas pelo ADM acumulam no `locomotivasPCX` (o alívio PCX sempre cria locos novas); o T39 tira a loco do modelo no `trainExit2`.
- **Validar:** ciclo só do ADM; aparece `rp_locoFips_pcx ... devolveu` 2x antes do `devolverLocomotiva2` e o pool volta a 8 livres.
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects.xml,Connectors.xml}`

## ALT-156 — faixa-isn-linha02-e
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `Train [684] and train [780] are trying to occupy the switch railwaySwitch124 simultaneously` (3,117 h). O U66 (ADM) atravessava para a linha 02 do ISN (`isn_linha01_c` → `railwayTrack24` → `isn_linha02_e` → AMV 124 → `isn_linha02_f`) e o MNB05 saía das DVs para o `circ_pos_fim_placa_2` (na `isn_linha02_a`) por `railwayTrack14` → AMV 124 → `isn_linha02_e`. Nenhum dos dois licenciava a `isn_linha02_e`: a travessia ia com `proxima_faixa = null` e a saída das DVs só pegava a `val_via2`.
- **O que:**
  - GestorDeISN: novo `rp_isn_linha02_e` e nova `faixa_isn_linha02_e` (track `isn_linha02_e`, linha `l_isn_linha02_e`), registrada no `registrar_faixas`.
  - GestorDeISN `para_isn_c`: quem vai estacionar pede `faixa_isn_linha02_e` como próxima (antes `null`); `ir_isn_linha02_e.onEnter` ocupa a faixa. Solta no `liberar_linha01` (ou na próxima liberação) quando a `isn_linha02_e` esvazia.
  - GestorDeDVs `ocupar_val_via2_saida`: Seize de `{ faixa_val_via2.recurso, gestorDeISN.rp_isn_linha02_e }` e ocupa as duas faixas; o `liberar_faixas_circulacao` (depois do `trainExit`) solta.
- **Validar:** trem estacionando na linha 02 do ISN enquanto um trem sai das DVs; um espera o outro passar pela `isn_linha02_e`.
- **Arquivos:** `_alp/Agents/GestorDeISN/{EmbeddedObjects.xml,Variables.xml,Code/Functions.java}`, `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`

## ALT-157 — adm-segundo-lote
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Regra (definida pelo Guilherme):** um terminal não descarrega duas ferrovias/segmentos ao mesmo tempo. Com o lote em descarga na linha de chegada com até `_LOTE_DESCARGA_TAMANHO_RECEBIMENTO` (10) vagões, um trem de qualquer ferrovia pode parar no 2º lote. Ele vira `emDescarga` quando a vaga da linha é solta (mesma ferrovia e segmento) ou só com o terminal vazio, sem carregados e sem vazios (tipo diferente).
- **Problema que motivou:** impasse na linha 4000 (5,5 h): o M78 (MRS) entrou no ADM com os lotes do U96 (VLI) ainda descarregando; os vazios se misturaram na linha 7 e o trem da 4000 foi definido como VLI com 36 vagões. Depois só vieram vazios MRS, e ADM e TES esperaram a 4000 para sempre.
- **O que:**
  - Parâmetro `_LOTE_DESCARGA_TAMANHO_RECEBIMENTO = 10` no ADM, TES e T39 (por enquanto usado só no ADM).
  - ADM `podeReceberNovoLote()`: recebe com a 2000 vazia ou com `lote_linha2000_emDescarga` ≤ parâmetro, sem `proximo` e sem `_trem_chegando`.
  - Main: `_adm_segundo_lote` de 1.045 para 1.500 na `pmc_linha2000_a`. O resto volta para o fim da 2000 pelo `movtc_recuar_linha2000` (para quando a ponta entra na `pmc_linha2000_a`); com até 10 vagões + 2 locos (~220 m) ocupa ~1.520–1.743, e o trem de 82 parado com a frente em 1.500 fica com a cauda em ~20 (sem tocar no `railwayTrack78`).
  - ADM: novo Wait `wait_segundo_lote_2000` entre `mov_linha2K_ponto_a` (`out` e `outHit`) e `ocupar_lote_em_descarga`. Funções `tentar_promover_segundo_lote()`, `eh_mesmo_tipo_em_atendimento(Trem)`, `terminal_vazio_para_novo_tipo()`. Chamada no `onEnter` do Wait e a cada ciclo do `atualizar_analises` do GestorDeAlivios.
  - ADM `comecar_encoste`: só atualiza a ferrovia/segmento em atendimento se a 2000 estiver vazia; `mov_linha2K_ponto_b` atualiza ao promover.
- **Pendência:** T39 e TES. No T39 o resto fica perto do `t39_encoste` (1.230), e um trem de 82 no 2º lote ficaria com a cauda no `railwayTrack78`. No TES o `_tes_segundo_lote` está na `pmc_linha4000_c`.
- **Validar:** ADM com trem VLI seguido de MRS; o MRS para no 2º lote e só vira `emDescarga` depois de `segundo_lote ... promovido (mesmo_tipo=false)`, com a linha 7 vazia.
- **Arquivos:** `_alp/Agents/ADM/{EmbeddedObjects.xml,Connectors.xml,Variables.xml,Code/Functions.java,Code/Functions.xml}`, `_alp/Agents/{TES,T39}/Variables.xml`, `_alp/Agents/GestorDeAlivios/Code/Events.java`, `_alp/Agents/Main/Levels/Level.level.xml`

## ALT-158 — tes-t39-receber-mesmo-tipo
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** impasse na linha 4000 (~7 h). O M78 (MRS) entrou no TES às 6,970 h com lotes do Y02 (Rumo) ainda no terminal; o trem da 4000 ficou Rumo com 59 vagões e o TES, só com vazios MRS, esperou para sempre (`tes espera | terminal: mrs/grao | trem da 4000: rumo/grao`).
- **Regra:** TES e T39 só recebem trem de outra ferrovia ou segmento com o terminal vazio (sem carregados e sem vazios). O ADM resolve pelo 2º lote.
- **O que:**
  - TES e T39: nova função `terminal_vazio()`.
  - GestorDeChegadas: novas funções `pode_receber_tipo(terminal, ferrovia, segmento)` e `pode_receber_trem(terminal, trem)` (tipo do 1º vagão). `decidir_destino` usa `pode_receber_tipo` com o pedido; `motivo_destino` registra `terminal com outro tipo (fer/seg)`.
  - GestorDeISN `verificar_saida`: libera o trem da linha 02 com `pode_receber_trem`.
  - `Pedido.segmento` (null = segmento padrão do terminal; o `tempo_entre_chegadas.csv` ainda não traz segmento).
- **Validar:** TES com Rumo e depois MRS; o MRS vai para o ISN (`terminal com outro tipo (rumo/grao)`) e só sai com o TES vazio.
- **Arquivos:** `_alp/Agents/{TES,T39}/Code/{Functions.java,Functions.xml}`, `_alp/Agents/GestorDeChegadas/Code/{Functions.java,Functions.xml}`, `_alp/Agents/GestorDeISN/Code/Functions.java`, `_alp/Classes/Class.Pedido.java`

## ALT-159 — adm-segundo-lote-offset
- **Data:** 2026-11-17
- **Status:** aplicada
- **Problema:** `_adm_segundo_lote (PositionOnTrack) : Parameter 'offset=1500.0' should be in the range [0, 1267.725...]` na criação do modelo. O comprimento da `pmc_linha2000_a` medido pelos pontos do desenho (1.743) não é o comprimento real (1.267,7 m).
- **O que:** `_adm_segundo_lote` volta para 1.045 (o resto de até 10 vagões + 2 locos no fim da 2000 ocupa ~1.048–1.268).
- **Atenção:** com a frente em 1.045, um trem de 82 no 2º lote fica com a cauda ~435 m no `railwayTrack78` (entrada da 1000/2000/3000) e segura as faixas da circulação até o `mov_linha2K_ponto_b`.
- **Arquivos:** `_alp/Agents/Main/Levels/Level.level.xml`

## ALT-160 — adm-encoste-so-mesmo-tipo
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** impasse na 4000 (~7 h). O U40 (VLI) entrou no ADM às 6,270 h com vazios MRS na linha 7 e foi direto para o encoste (2000 vazia); os vazios se misturaram e o trem MRS da 4000 parou em 67.
- **O que (ADM):** `linha_2000_vazia` vai para o encoste só com a 2000 vazia **e** (`eh_mesmo_tipo_em_atendimento` ou `terminal_vazio_para_novo_tipo()`); senão o trem para no 2º lote e espera a promoção. O `comecar_encoste` só atualiza a ferrovia/segmento em atendimento na mesma condição.
- **TES e T39:** a mesma regra já vale pela entrada (`pode_receber_tipo`): o trem de outro tipo espera no ISN até o terminal ficar sem carregados e sem vazios.
- **Validar:** ADM com MRS e depois VLI; o VLI para no 2º lote e só vira `emDescarga` com `promovido (mesmo_tipo=false)` depois que a linha 7 esvazia; o trem MRS da 4000 fecha 82.
- **Arquivos:** `_alp/Agents/ADM/EmbeddedObjects.xml`

## ALT-161 — t39-alivio-licencia-1000-b
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `Train [786] and train [789] are trying to occupy the switch railwaySwitch104` (8,300 h). A loco do alívio do T39 ia para o `pos_t39_alivio` pelo `railwayTrack79` → AMV 104 → `pmc_linha1000_c` e o lote do T39 (corte de 15 do 27288) andava na `pmc_linha1000_b` para a linha 11. O AMV 104 liga `pmc_linha1000_b`, `pmc_linha1000_c` e `railwayTrack79`; o alívio licenciava só a `linha1000_c`.
- **O que (T39):** `primeiro_alivio_faixas` inclui a `faixa_linha1000_b`. O trem do T39 fica na `pmc_linha1000_a` e só usa a b e a c para encostar nas moegas, então o alívio espera no máximo um encoste.
- **Arquivos:** `_alp/Agents/T39/Variables.xml`

## ALT-162 — t39-moega-espera-espaco-vazios
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** `The car being added on the track 't39_linha9_fundo' intersects with another car` (2,283 h). O alívio do T39 esperava o trem MRS da 4000 fechar (correto pela regra de ferrovia) e a descarga continuou: linha 10 → linha 9 → fundo da 9 sem checar capacidade. As três comportam 14 + 24 + 23 = 61 vazios; o 62º nasceu em cima de outro.
- **O que (T39):**
  - Novo SelectOutput `tem_espaco_vazios` entre `aliviar_moega` e `disp_alivio1`: há lugar na 10, na 9 ou no fundo da 9 (`CAPACIDADE_LINHA_9_FUNDO`)?
  - Sem lugar: novo Wait `wait_espaco_vazios` (log `AVISO sem espaco nas linhas 9/10/fundo`); a moega para. Fica antes do `disp_alivio1`, para não segurar o `fazendo_alivio`.
  - Nova função `tentar_liberar_espaco_vazios()`, chamada no `onEnter` do Wait, no `onRelease` do `fazendo_alivio` e a cada ciclo do `atualizar_analises` do GestorDeAlivios.
- **Validar:** T39 com o alívio bloqueado; aparece o AVISO, a moega para, e depois do alívio `espaco liberado, vagao=... segue`.
- **Arquivos:** `_alp/Agents/T39/{EmbeddedObjects.xml,Connectors.xml,Code/Functions.java,Code/Functions.xml}`, `_alp/Agents/GestorDeAlivios/Code/Events.java`

## ALT-163 — liberar-faixas-na-espera-do-terminal
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** o U22 (26715, adm/vli) ficou no `wait_segundo_lote_2000` da ADM, que estava em RUMO descarregando. As faixas `via3_b` e `via3_c` continuaram OCUPADA com os tracks vazios, porque o `liberar_faixas_ocupadas` só rodava no `liberar_faixas_circulacao`, depois do `ponto_b`. O Y40 (27293, tes) travou na `via3_a` e a linha 02 do ISN travou atrás dele.
- **O que:** novo Release `liberar_faixas_espera` (mesmo código: `liberar_faixas_ocupadas`) logo antes do ponto de espera de cada terminal. Solta só as faixas já vazias; as que ainda têm a cauda seguem até o `liberar_faixas_circulacao`.
  - ADM: `mov_linha2K_ponto_a` (out e outHit) → `liberar_faixas_espera` → `wait_segundo_lote_2000`
  - TES: `mov_linha3K_ponto_a` → `liberar_faixas_espera` → `ocupar_lote_em_descarga`
  - T39: `encosta_L1K1_proximo` → `liberar_faixas_espera` → `ser_linha_1000_emDescarga`
- **Validar:** segundo lote da ADM de outra ferrovia esperando; a `via3_b`/`via3_c` ficam LIVRE e o próximo trem do TES passa.
- **Arquivos:** `_alp/Agents/{ADM,TES,T39}/{EmbeddedObjects.xml,Connectors.xml}`

## ALT-164 — overload-ignora-segundo-lote-de-outro-tipo
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** impasse na ADM. O M64 (26715, mrs, 82 carregados) era o segundo lote (`lote_linha2000_proximo`) e entrava no `_getVagoesCarregados()`. Com isso o `getOverloadVazios()` não via "últimos vazios", os 16 vazios rumo da linha 7 não pediam alívio, o trem da 4000 parou em 66 e o M64 não era promovido. Os trens atrás esperavam a via3.
- **O que (ADM, TES, T39):** no `getOverloadVazios()`, o lote `*_proximo` de outra ferrovia/segmento não conta como carregado. TES e T39 ganharam `eh_mesmo_tipo_em_atendimento(Trem)` (compara o 1º vagão com `getFerrovia()`/`getSegmento()`).
- **Validar:** segundo lote de outra ferrovia na ADM; os últimos vazios pedem alívio, a 4000 fecha 82 e o segundo lote é promovido.
- **Arquivos:** `_alp/Agents/ADM/Code/Functions.java`, `_alp/Agents/{TES,T39}/Code/{Functions.java,Functions.xml}`

## ALT-165 — rp-acesso-moema
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** travamento às 33,69 h. O alívio do T39 não voltava à 4000: o corte em descarga ficava com a cauda em cima da 1000_b/c quando as linhas 9/10/fundo enchiam, e o alívio precisava passar por ali para abrir espaço. Sem alívio, TES e ADM também pararam.
- **O que:**
  - GestorDeCirculacao: novo `rp_acesso_moema` (capacidade 1). Um movimento por vez na área da Moema; quem precisa dele pega antes das faixas e não espera segurando o acesso.
  - GestorDeAlivios: `bloquear_faixas_ate_terminal` pede `conjunto_alivio_pcx()` (acesso + faixas); solta na volta à 4000 (`liberar_faixas_ate_terminal`).
  - Chegada (TES, ADM, T39): Seize `acesso_moema_chegada` depois do `novo_lote`; Release `liberar_acesso_chegada` quando o trem para na linha X000 e `liberar_acesso_espera` no caminho do ponto_a/proximo.
  - ADM: `acesso_moema_manobra` antes do `sz_mnb_carregados`; `licenciar_moema` renomeado para `licenciar_linhas_2000`; `rls_moema` e `rls_moema1` soltam os dois. Sem 2º lote (`podeReceberNovoLote` exige `emDescarga` e `proximo` null) e `pode_receber_tipo` vale também para a ADM.
  - TES: `acesso_moema_encoste` depois do `comecar_encoste_moegas`; `rls_rp_mnb_linha04e05` e `rls_volta_3000` soltam o acesso.
  - T39: `esperar_espaco_corte` (Wait) + `acesso_moema_corte` antes do `aguarda_linha_13`. O lote só tira o corte com a linha 11 livre e lugar para o corte inteiro nas 9/10/fundo (`espaco_livre_vazios`, `tamanho_proximo_corte`, `tentar_liberar_espaco_corte`). `getOverloadVazios` força o alívio com lote esperando espaço. O `release` solta o acesso.
- **Fora:** vinda das locos FIPS até os trens dos terminais.
- **Validar:** T39 com as linhas de vazios enchendo; aparece `T39 corte lote=... segue` só com espaço, o alívio entra e volta à 4000. TES/ADM/T39 nunca dois movimentos ao mesmo tempo na Moema.
- **Arquivos:** `_alp/Agents/{GestorDeCirculacao,GestorDeAlivios,GestorDeChegadas,ADM,TES,T39}/...`

## ALT-166 — tes-carregados-ignora-wait-encoste
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** TES travado às 382,815 h. O X50 (28020) esperava no `wait_encoste_3000` as linhas 03/04/05 esvaziarem e entrava no `_getVagoesCarregados()`. Os 20 vazios restantes (6 na linha 03, 14 no fundo 05) não forçavam o alívio; o U80 ficou no ISN.
- **O que (TES):** `_getVagoesCarregados()` não conta o `lote_linha3000_emDescarga` quando há lote no `wait_encoste_3000` (igual ao `wait_resto_3000`).
- **Validar:** novo lote esperando o encoste com vazios nas linhas; aparece `qtd_alivio tes`, as linhas esvaziam e o lote encosta.
- **Arquivos:** `_alp/Agents/TES/Code/Functions.java`

## ALT-167 — logs-alivio-pcx-e-acesso-moema
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** travamento às 4,648 h sem informação no log: o alívio do T39 foi definido e não andou, e não dava para saber qual recurso ele esperava.
- **O que (só logs):**
  - GestorDeCirculacao: variável `dono_acesso_moema`; `rp_acesso_moema` loga `acesso_moema pegou/soltou <prefixo>/<id>`.
  - GestorDeAlivios: logs `alivio_pcx` em `comecar_alivio`, saída do `bloquear_faixas_ate_terminal`, chegada no terminal, volta à 4000, `wait_trem_4000` e `liberar_wait_trem_4000()` (WARN quando não há trem na 4000).
  - GestorDeAlivios: `diag_alivio_pcx()` no `atualizar_analises`. Com o alívio mais de 1 min no `bloquear_faixas_ate_terminal`, a cada 5 min loga `alivio_espera`: trem, destino, tempo, acesso (idle/dono), locos, cada faixa (estado, id, trens no track), estado do corte do T39 e overload de TES/ADM/T39. Variáveis `t_espera_alivio_pcx`, `t_ultimo_diag_alivio_pcx`, `trem_espera_alivio_pcx`.
  - T39: `esperar_espaco_corte` loga o motivo da espera (linha 11, corte, espaço livre).
- **Arquivos:** `_alp/Agents/{GestorDeCirculacao,GestorDeAlivios,T39}/...`

## ALT-168 — log-de-analise-adm-tes-t39
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Motivo:** travamento às 9,4 h: a 4000 (mrs/grao) parou com 79 de 82 e o ADM (vli/grao) ficou esperando. Faltavam 3 vazios do M60 (28704) no T39, que não pediu mais alívio. O log não mostrava o estado do T39.
- **O que (só logs):** variável `log_de_analise` (boolean, true) e funções `log_analise(evento, agente, msg)`, `estado_resumo()`, `lote_txt(Trem)` e `log_estado()` no ADM, TES e T39, no padrão da ALT-123/136.
  - Cada bloco da biblioteca escreve no log (evento = nome do bloco), com os mesmos pontos da ALT-136. Toda linha termina com `vz=… cg=… ov=… moega=…` (`_getVagoesVazios`, `_getVagoesCarregados`, `getOverloadVazios`, moegas livres/ocupadas).
  - ADM 117 blocos (187 pontos), TES 141 (224), T39 167 (265). Blocos próprios do modelo não foram instrumentados.
  - `log_estado()`: `estado`, `estado_lotes` (id:vagões de cada `lote_*`), `estado_pools` (idle/busy) e `estado_waits` (tamanho). Chamado a cada 5 min pelo `atualizar_analises` do GestorDeAlivios (variável `t_ultimo_log_estado_terminais`).
- **Arquivos:** `_alp/Agents/{ADM,TES,T39}/{EmbeddedObjects,Variables}.xml`, `_alp/Agents/{ADM,TES,T39}/Code/Functions.{java,xml}`, `_alp/Agents/GestorDeAlivios/Variables.xml`, `_alp/Agents/GestorDeAlivios/Code/Events.java`

## ALT-169 — t39-acesso-moema-ate-corte-sair-da-1000
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** colisão no `railwaySwitch104` às 2,212 h. O pai do corte (27534) soltava o `rp_acesso_moema` no `release`, logo depois do `tirar_lote`, com o pedaço cortado (27753) ainda parado na 1000_b/c esperando o `licenciar_linha_1000_b`. O alívio do T39, que estava na fila, pegou o acesso e as faixas, e a loco entrou pela `railwayTrack79` em cima do pedaço.
- **O que:**
  - `release` deixa de soltar o `acesso_moema_corte` (continua soltando b1, c1 e linha 13).
  - Blocos novos entre o `release` e o `para_verificar_fim_lote`: Wait `esperar_corte_sair_1000` (o pai espera segurando só o acesso) → Release `liberar_acesso_corte` (ALL_FROM_SEIZES `{ acesso_moema_corte }`).
  - `livar_linha_1000_c` (o pedaço saiu da 1000_c) libera o pai do `esperar_corte_sair_1000`.
- **Arquivos:** `_alp/Agents/T39/{EmbeddedObjects,Connectors}.xml`

## ALT-170 — t39-vazios-so-vagoes-e-exit8-volta-4000
- **Data:** 2026-11-17
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **Problema:** travamento às 3,654 h. Uma loco da ponta de um corte (27711) passou pela moega e ficou na linha 10. `_getVagoesVazios()` / `get_quantidade_vazios()` somavam `getTamanhoDoTrem` (carros) e contaram a loco: alívio pedido de 61 com 60 vagões. O alívio 27981 pegou os 60, `terminou_alivio` deu false, as linhas estavam vazias e o trem caiu no `exit8` (Exit sem destino), parado no T39 com o `rp_acesso_moema` e as faixas. O M64 do ADM ficou na fila do acesso.
- **O que:**
  - `_getVagoesVazios()` e `get_quantidade_vazios()` somam `main().getNumeroDeVagoes(...)` (só vagões).
  - `exit8`: `WARN T39 alivio_incompleto trem=… pegou=… pedido=…` e `main().gestorDeAlivios.linha_4000.take(agent)`, igual ao `para_linha_4000`.
- **Em aberto:** a loco da ponta do corte continua indo para a moega e para as linhas de vazios.
- **Arquivos:** `_alp/Agents/T39/EmbeddedObjects.xml`, `_alp/Agents/T39/Code/Functions.java`

## ALT-171 — tec-rota-de-chegada
- **Data:** 2026-11-19
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **O que:** rota de chegada do TEC e esqueleto do agente.
  - Rota: `isn_linha01_a … e` → `psn_linha_porteira` (GestorDeISN) → `val_via2_a` → `dv_entrada_c` → `via1_intermar_a` (de frente) → `tec.comecar_encoste`.
  - GestorDeISN, saída da porteira: trem do TEC segue para `faixa_val_via2_a` (próxima `faixa_dv_entrada_c`); os demais continuam em `faixa_val_via2` → `faixa_via3_intermar`.
  - GestorDeCirculacao: faixas novas `faixa_val_via2_a` e `faixa_dv_entrada_c` (pools `rp_val_via2_a`, `rp_dv_entrada_c`; TMDS `pl_pst_val_via2_a`, `pl_pst_dv_entrada_c`), cada uma com `circular_*` → `ir_*` (de frente, 10 m do fim) → Exit; entraram na lista `faixas`.
  - GestorDeCirculacao: função `eh_terminal_tec(Trem)`; SelectOutput `eh_tec` depois da `circular_via1_intermar_a` → `ir_via1_intermar_a_tec` (de frente) → `para_tec` (`main().tec.comecar_encoste`). Os outros trens seguem para `ir_via1_intermar_a` → `para_DVS`, como antes.
  - TEC: implementa `ITerminalMoega`; variáveis `_trem_chegando`, `ferrovia_em_atendimento`, `segmento_em_atendimento`; Enter `comecar_encoste` (registra o trem e loga) → Wait `aguardando_encoste`.
  - TEC: funções da interface sem referências ao ADM. Provisórias até o encoste/descarga/alívio existirem: `podeReceberNovoLote` = sem trem chegando; `getOverloadVazios` e `get_quantidade_vazios` = 0; `get_pos_alivio` = null; `get_faixas_primeiro_alivio` = lista vazia; `takeAlivio` só loga WARN.
- **Em aberto:** posição dos traços do TMDS (`pl_pst_val_via2_a`, `pl_pst_dv_entrada_c`) e `getPatio()` do TEC (hoje `"pmc"`). `_trem_chegando` só volta a null quando o encoste existir (até lá, um trem por vez).
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/GestorDeCirculacao/Levels/Level.level.xml`, `_alp/Agents/GestorDeCirculacao/Code/Functions.{java,xml}`, `_alp/Agents/GestorDeISN/EmbeddedObjects.xml`, `_alp/Agents/TEC/{AOC.TEC,EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TEC/Code/Functions.java`

## ALT-172 — tec-encoste-e-descarga
- **Data:** 2026-11-19
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **O que:** fluxo do TEC do encoste até o pedido de alívio (o alívio fica para depois; o TEC não usa locos do GestorDeLocomotivas).
  - `comecar_encoste` → `ir_tec_encoste` (TrainMoveTo de frente até `main()._tec_encoste`, na `railwayTrack3`) → `liberar_faixas_circulacao` (Release com `liberar_faixas_ocupadas`) → `descarregar` (DescarregaLote, 45 fixo no bloco) → `tirar_loco` (TrainDecouple, 1 carro da frente = loco do 1º carro).
  - Loco: `outDecoupled` → `loco_sair` (TrainExit) → `loco_entrar_cauda` (TrainEnter no trilho/offset da traseira do último vagão) → `acoplar_loco_cauda.in2`. Vagões: `out` → `acoplar_loco_cauda.in1`. Depois → Wait `aguardando_alivio`.
  - `acoplar_loco_cauda` (ao sair): `descarga_concluida = true`; `getOverloadVazios()` devolve 2.0 com a descarga concluída (antes 0).
  - Funções novas `get_track_cauda()`, `get_offset_cauda()` e `get_ultimo_carro_info()` (reflection em `getTrack`/`getOffset` do último vagão). A loco entra na orientação padrão, com a cabeça na traseira do vagão (trem no sentido do trilho) ou um comprimento de loco (22,5 m) depois dela (trem contra o sentido).
  - Removido o Wait provisório `aguardando_encoste`. `log_analise` do TEC passa a logar com origem `TEC` (estava `Tgrao`).
- **Riscos:** a `railwayTrack3` é ponta de linha (sem desvio para a loco contornar), por isso a loco sai e entra na cauda. Se a cauda parar perto da emenda de dois trilhos, a loco pode não caber no TrainEnter; nesse caso, criar um ponto fixo.
- **Arquivos:** `_alp/Agents/TEC/{EmbeddedObjects,Connectors,Variables}.xml`, `_alp/Agents/TEC/Code/Functions.{java,xml}`

## ALT-173 — tec-terminais-pst-e-contagem
- **Data:** 2026-11-19
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **O que:**
  - GestorDeAlivios: `main().tec` entra em `terminais_pst`. Novo tipo de loco de alívio `"proprio"` (loco do próprio trem): no início do `fazer_procedimento_de_alivio_pst`, o terminal sai de `terminais_pst`, loga `alivio_proprio` e recebe `takeAlivio(null)`. Não pede loco, não cria trem e não consulta as DVs.
  - TEC: `get_tipo_loco_alivio()` = `"proprio"`; `takeAlivio` só loga `pronto para alivio (a implementar)` (aceita null).
  - TEC: `_getVagoesCarregados()` e `_get_vagoes_vazios()` contam os vagões do `_trem_chegando` (`getNumeroDeVagoesCarregados` / `getNumeroDeVagoesVazios`); `get_quantidade_vazios()` = `_get_vagoes_vazios()`. O Wait `aguardando_alivio` (ao entrar) passa `_trem_chegando` para o trem que sai do `acoplar_loco_cauda`.
  - `ITerminalMoega`: comentário do `get_tipo_loco_alivio` inclui `"proprio"`.
- **Decidido:** `_trem_chegando`, `ferrovia_em_atendimento`, `segmento_em_atendimento` e `descarga_concluida` voltam a null/false quando o trem sair do TEC no alívio (a construir); o alívio devolve o TEC a `terminais_pst`. `tecAcucar` fica fora de `terminais_pst` por enquanto.
- **Em aberto:** o `"proprio"` ainda não consulta o espaço nas DVs (`quantos_vagoes_as_dvs_precisam`). O `analisar_pst` só chega ao TEC com `condicao_pode_receber_alivio()` das DVs e `rp_alivio_pst` livre.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Variables.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `_alp/Agents/TEC/EmbeddedObjects.xml`, `_alp/Agents/TEC/Code/Functions.java`, `_alp/Classes/Class.ITerminalMoega.java`

## ALT-174 — tec-alivio-saida
- **Data:** 2026-11-19
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **O que:** saída do alívio do TEC. O trem sai com os 82 vazios e a loco da cauda, entra na circulação pela `via1_intermar_a` e passa pela entrada das DVs, onde `segue_direto` (82) o manda direto para a saída.
  - TEC `takeAlivio`: solta o trem do Wait `aguardando_alivio`.
  - TEC, Exit novo `para_circulacao`: `eh_alivio = true`, `faixa_atual = faixa_via1_intermar_a`, `proxima_faixa = null`; zera `_trem_chegando`, `ferrovia_em_atendimento`, `segmento_em_atendimento` e `descarga_concluida`; devolve o TEC a `gestorDeAlivios.terminais_pst`; `gestorDeCirculacao.tentar_usar_faixa.take(trem)`.
  - TEC `get_primeiras_linhas_de_circulacao_vazios()`: `faixa_via1_intermar_a` (antes, faixas do ADM).
  - GestorDeCirculacao `eh_tec`: `eh_terminal_tec(trem) && !trem.eh_alivio`; o trem de alívio segue `ir_via1_intermar_a` (recuando) → `para_DVS` → `gestorDeDVs.chegar_nas_dvs`.
- **Riscos:** `railwayTrack111`, `railwayTrack162`, `railwayTrack10` e `_183` não têm faixa (hoje só o TEC usa).
- **Arquivos:** `_alp/Agents/TEC/{EmbeddedObjects,Connectors}.xml`, `_alp/Agents/TEC/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`

## ALT-175 — isn-proxima-val-via2-a
- **Data:** 2026-11-19
- **Status:** aplicada, aguardando validação
- **O que:** GestorDeISN, na entrada da porteira (fim da `isn_linha01_e` e saída da linha 02), a próxima faixa passa a ser `faixa_val_via2_a` (antes `faixa_val_via2`). A `val_via2_a` é a 1ª faixa do GestorDeCirculacao para todos os trens; o trem do TEC deixava de licenciar a `val_via2` indevidamente.
- **Arquivos:** `_alp/Agents/GestorDeISN/EmbeddedObjects.xml`

## ALT-176 — tec-acucar
- **Data:** 2026-11-19
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **O que:** TEC_ACUCAR com a mesma lógica do TEC.
  - Agente TEC_ACUCAR = cópia do TEC (IDs novos): implementa `ITerminalMoega`; mesmo fluxo (encoste → descarga → loco da cabeça para a cauda → `aguardando_alivio` → `para_circulacao`), mesmas funções, variáveis e trens de teste. Diferenças: encoste em `main()._tecAcucar_encoste` (`railwayTrack136`, ponta de linha), logs com origem `TEC_ACUCAR`, `para_circulacao` devolve `main().tecAcucar` a `terminais_pst`.
  - GestorDeCirculacao: `eh_terminal_tec` reconhece `"tec"` e `"tecAcucar"`; `_eh_tec` passa a usar `eh_terminal_tec`; `para_tec` chama o `comecar_encoste` do terminal do trem.
  - GestorDeAlivios: `main().tecAcucar` entra em `terminais_pst`.
- **Riscos:** herda a saída pelas DVs (`ocupar_val_via2_saida` licencia `val_via2` + `isn_linha02_e`, fora do caminho do trem). `railwayTrack111` e `railwayTrack130` sem faixa, compartilhadas por TEC e TEC_ACUCAR.
- **Arquivos:** `_alp/Agents/TEC_ACUCAR/**`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeAlivios/Variables.xml`

## ALT-177 — trem-origem
- **Data:** 2026-11-19
- **Status:** aplicada, aguardando validação (abrir no AnyLogic e compilar)
- **O que:** novo atributo `String origem` no `Trem`, preenchido na saída do MNB:
  - alívio PST (`atualizar_status_alivio_terminal`): `getName()` do terminal;
  - alívio PMC (`retornar_alivio_pmc`): `getName()` do terminal;
  - alívio PCX, volta para a 4000 (`linha_4000`): `getName()` do terminal (`alivio_em_andamento_pcx.get_moega_atual()`);
  - saída da 4000 com 82 (`para_alivio_na_linha_4000`): `"linha4k"`.
  - Rótulo do 1º carro (Vagao e Locomotiva): `prefixo - destino (de origem)`, só quando `origem != null`.
- **Arquivos:** `_alp/Agents/Trem/Variables.xml`, `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `_alp/Agents/Vagao/Levels/Level.level.xml`, `_alp/Agents/Locomotiva/Levels/Level.level.xml`

## ALT-178 — trem-4000-espera-dentro-da-linha
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** o MNB do alívio PCX esperava em `pos_linha_4000_criar_loco` (na `pmc_intervalo4000_b`), que é passagem da Bracell, sem segurar essa faixa. O alívio da Bracell (MNB07) recuou na `intervalo4000_b` e bateu no MNB05 parado (`recuar_intervalo4000_b.outHit` sem conexão).
- **O que:** `ir_linha_4000` passa de `TARGET_AT_POINT_ON_TRACK` (`pos_linha_4000_criar_loco`) para `TARGET_AT_TRACK_OFFSET` em `main().pmc_linha4000_a`, 10 m do início (lado da `intervalo4000_c`). O ponto de criação da loco não muda.
- **Limite conhecido:** a 4000 (a+b+c ≈ 1.132 m) comporta 2 locos + ~62 vagões (17,5 m). Acima disso o trem passa do fim da `linha4000_c` (lado do TES). Na saída com 82, o trem atravessa `intervalo4000_c` e `intervalo4000_b` sem licenciar essas faixas (`faixa_atual = intervalo4000_a`).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-179 — seize-saida-4000
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** com o trem da 4000 esperando dentro da linha, na saída com 82 ele atravessava `intervalo4000_c` e `intervalo4000_b` sem licenciar. `ocupar_faixas_saida_4000` deixava essas faixas LIVRE e a loco nova nasceu em `pos_linha_4000_criar_loco` em cima da cauda (`loco_pcx_starts`: car intersects on `pmc_intervalo4000_b`).
- **O que (GestorDeAlivios):**
  - Novo Seize `seize_saida_4000` entre `linha_4000_cheia` (outT) e `liberar_linha_4000`: pega `intervalo4000_c`, `b` e `a` num conjunto só. No onExit, c e b ficam OCUPADA pelo trem e a fica LICENCIADA (faixa_atual da entrega à circulação).
  - `ocupar_faixas_saida_4000`: `intervalo4000_c/b` que o trem possui ficam OCUPADA mesmo antes de ele chegar nelas; o GestorDeCirculacao solta o recurso quando a cauda sai.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `docs/gestores.md`

## ALT-180 — saida-4000-nao-soltar-antes-de-passar
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** o trem cheio da 4000 pega `intervalo4000_c/b` no `seize_saida_4000` antes de chegar nelas. Na primeira chamada de `liberar_faixas_ocupadas` as duas estavam vazias (o trem ainda dentro da 4000) e eram soltas na largada. A loco nova nasceu na `intervalo4000_b` em cima da cauda (car intersects, 1,990 h).
- **O que (GestorDeCirculacao.liberar_faixas_ocupadas):** `intervalo4000_c` não é solta enquanto o trem ocupar `linha4000_a/b/c`; `intervalo4000_b` não é solta enquanto ocupar `linha4000_*` ou `intervalo4000_c`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`

## ALT-181 — par-via1-intermar-a-dv-entrada-c
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** M68 (para o TEC_ACUCAR) licenciou a `dv_entrada_c` e esperava a `via1_intermar_a`; o MNB21 (para as DVs) licenciou a `via1_intermar_a` e entrou nas DVs pela `dv_entrada_c` sem licenciar. Colisão de frente em 4,158 h (`gestorDeDVs.ir_ponto.outHit`).
- **O que (GestorDeCirculacao):**
  - `licenciar_esta_faixa`: passa a usar conjunto de recursos; se `faixa_atual` é `via1_intermar_a` ou `dv_entrada_c`, pega as duas juntas e licencia as duas para o trem. Demais faixas como antes.
  - `para_DVS`, `alivio_para_DVs`, `exit3`: se a `dv_entrada_c` está licenciada para o trem, passa a OCUPADA.
  - `liberar_faixas_ocupadas`: `dv_entrada_c` não é solta enquanto o trem ocupar a `via1_intermar_a` (exceto trem chegando ao TEC, que percorre a `dv_entrada_c` pela circulação).
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `docs/gestores.md`

## ALT-182 — cruzamento-porteira
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** MNB07 saindo das DVs (pega `val_via2` + `isn_linha02_e`) e M66 saindo do ISN pela `val_via2_a` chegaram juntos ao AMV `railwaySwitch1` (1,351 h). Além disso, o par da ALT-181 só valia no `licenciar_esta_faixa`; a `via1_intermar_a`/`dv_entrada_c` também são licenciadas como `proxima_faixa` (`licenciar_proxima_faixa`), e um trem que já tinha a `dv_entrada_c` travaria pedindo o par de novo.
- **O que:**
  - GestorDeCirculacao: novo ResourcePool `rp_cruzamento_porteira` (capacidade 1).
  - Nova função `get_conjunto_licenca(trem, faixa)`: faixa + par (`via1_intermar_a`/`dv_entrada_c`) ou `rp_cruzamento_porteira` (para `val_via2_a`), sem os recursos que o trem já tem. Usada no `licenciar_esta_faixa` e no `licenciar_proxima_faixa` (os dois em modo conjunto).
  - onExit dos dois: licencia a outra metade do par só se ainda não for do trem.
  - GestorDeDVs `ocupar_val_via2_saida`: pega também `rp_cruzamento_porteira`.
  - `liberar_faixas_ocupadas`: solta o cruzamento junto com a `val_via2_a` ou a `val_via2`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/Code/Functions.xml`, `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-183 — liberar-faixas-antes-da-fila-das-dvs
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock em ≈3,98 h. MNB09 saiu para as DVs e ficou na fila (`fila_dvs`) segurando a `via2_c` (cauda já fora). R88 esperava a `via2_c` segurando `val_via2` + `rp_cruzamento_porteira`; MNB17, em manobra nas DVs, esperava esses dois para sair. `para_DVS`/`alivio_para_DVs` entregavam o trem às DVs sem soltar faixas, e a fila não solta nada.
- **O que (GestorDeCirculacao):** novos Release `liberar_antes_dvs` (antes de `para_DVS`) e `liberar_antes_dvs_alivio` (antes de `alivio_para_DVs`), com `resources = liberar_faixas_ocupadas((Trem) agent)`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeCirculacao/Connectors.xml`, `docs/gestores.md`

## ALT-184 — cruzamento-na-entrada-das-dvs
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock em ≈4,85 h. M78 (alívio do TEC_ACUCAR, 82 vgs) nas DVs com a cauda na `dv_entrada_c` pedia o cruzamento para sair (`ocupar_val_via2_saida`); I90 (chegando ao TEC) tinha `val_via2_a` + cruzamento e esperava o par `dv_entrada_c`/`via1_intermar_a`. A saída das DVs vai pela `dv_direita` e não passa pelo cruzamento nem pela `val_via2`.
- **O que:**
  - GestorDeDVs `ocupar_val_via2_saida`: pega só a `isn_linha02_e` (saem `val_via2` e `rp_cruzamento_porteira`). Novo Release `liberar_antes_saida` antes dele (de `sel_segue_direto` e `sel_acao`).
  - GestorDeCirculacao `get_conjunto_licenca`: cruzamento junto com `val_via2_a`, `val_via2` e o par `via1_intermar_a`/`dv_entrada_c`.
  - `liberar_faixas_ocupadas`: solta o cruzamento quando o trem não tem mais `val_via2_a`, `val_via2` nem `dv_entrada_c` e não está na `railwayTrack18`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `_alp/Agents/GestorDeDVs/Connectors.xml`, `docs/gestores.md`

## ALT-185 — saida-das-dvs-pela-porteira-e-rota-do-tec
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** com a ALT-184 a saída das DVs deixou de pegar `val_via2` + cruzamento. O MNB07 (segue direto, parado no fim da `via2_intermar_a`) sai pela `railwayTrack199` → `val_via2` → AMV da porteira e bateu no Y26 que saía do ISN pela `val_via2` (1,417 h, `ir_val_via2.outHit`).
- **O que:**
  - GestorDeCirculacao: nova função `get_conjunto_saida_dvs(trem)`: `val_via2` + `isn_linha02_e` + `rp_cruzamento_porteira`, sem o que o trem já tem.
  - GestorDeDVs `ocupar_val_via2_saida`: usa `get_conjunto_saida_dvs`; onExit volta a ocupar a `val_via2`. Vale para segue direto e para a saída depois da manobra.
  - `get_conjunto_licenca`: trem chegando ao TEC na `val_via2_a` pega `val_via2_a` + cruzamento + `dv_entrada_c` + `via1_intermar_a` de uma vez. onExit do `licenciar_esta_faixa` / `licenciar_proxima_faixa` licencia o par para ele.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/Code/Functions.xml`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-186 — cruzamento-so-ao-entrar-nas-dvs
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock em ≈2,08 h. U22 (alívio do TEC_ACUCAR) pegou o par `via1_intermar_a`/`dv_entrada_c` + cruzamento ao sair do TEC e ficou com o cruzamento na fila das DVs; MNB05 (segue direto, em manobra) esperava o cruzamento para sair.
- **O que:**
  - GestorDeCirculacao `get_conjunto_licenca`: o par `via1_intermar_a`/`dv_entrada_c` não leva mais o cruzamento (a chegada ao TEC pela `val_via2_a` continua com tudo junto).
  - Nova função `get_conjunto_entrada_dvs(trem)`: cruzamento, se o trem ainda não tiver.
  - GestorDeDVs: novo Seize `seize_cruzamento_entrada` entre `sel_segue_direto.outF` e `ir_ponto`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/Code/Functions.xml`, `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `_alp/Agents/GestorDeDVs/Connectors.xml`, `docs/gestores.md`

## ALT-187 — prioridade-no-cruzamento-das-dvs
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock em ≈4,57 h. Cruzamento livre desde 4,533 h, mas o MNB27 (nas DVs, com o par `via1_intermar_a`/`dv_entrada_c`) ficou na fila do `seize_cruzamento_entrada` atrás do Y62 (porteira, indo ao TEC), que pedia `val_via2_a` + cruzamento + par e não fechava o conjunto.
- **O que (GestorDeDVs):** `seize_cruzamento_entrada` e `ocupar_val_via2_saida` com `priority = 10` (antes 1, igual às licenças da circulação).
- **Arquivos:** `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-188 — um-alivio-pst-por-vez
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** `IllegalStateException` em 6,526 h (`bloquear_faixas_pst`, "terminal sem faixas de primeiro alivio"). O alívio do Tgrao começou às 6,330 h e a loco ficou esperando o `mnb_pcx`; como o `rp_alivio_pst` só é pego no fim do fluxo, o alívio do MoegaoMar começou às 6,411 h, sobrescreveu `alivio_em_andamento_pst` e terminou; a loco do Tgrao leu o objeto vazio.
- **O que (GestorDeAlivios, disparo do alívio PST):** além de `rp_alivio_pst.idle() > 0`, exige que não haja alívio PST em andamento (`alivio_em_andamento_pst == null || finalizou_moegas()`).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `docs/gestores.md`

## ALT-189 — chave-para-traces-repetitivos
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** traces periódicos ocupavam ≈ 66% do `sim.log` (`Chegadas aguardando` 22 mil linhas, `saida_4000_livre` 5 mil, `diag_pst` 1,3 mil em 17 h).
- **O que:**
  - GestorDeAlivios: nova variável `trace_repetitivo` (boolean, `false`); os 8 blocos `% 60 == 0` só logam com ela ligada.
  - GestorDeChegadas: nova variável `trace_aguardando` (boolean, `false`); `registrar_motivo` só loga com ela ligada.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Variables.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `_alp/Agents/GestorDeChegadas/Variables.xml`, `_alp/Agents/GestorDeChegadas/Code/Functions.java`, `docs/gestores.md`

## ALT-190 — hora-do-log-e-data-de-inicio
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** o tempo do log saía em horas decimais (`[  16.966h]`), difícil de ler.
- **O que:**
  - `Class.Log`: novo `data_inicio` (`LocalDate`, padrão 01/01 do ano atual); o tempo sai como `[hh:mm - dd/mm]`, contado de 00:00 de `data_inicio`.
  - Main `carregar_configuracao`: lê a 4ª coluna opcional `data_inicio` (dd/mm/aaaa) do `simulacao.csv` e mostra no `[config]`.
  - `simulacao/config/simulacao.csv`: coluna `data_inicio` (01/01/2026).
  - `interface_simulacao.html`: campo "Data de início" na aba Configurações (salva a coluna) e leitor de log entende o formato novo (continua lendo o antigo em horas).
  - GestorDeChegadas: `trace_aguardando` renomeada para `log_analise_detalhada`.
  - CLAUDE.md: formato do log atualizado.
- **Arquivos:** `_alp/Classes/Class.Log.java`, `_alp/Agents/Main/Code/Functions.java`, `_alp/Agents/GestorDeChegadas/Variables.xml`, `_alp/Agents/GestorDeChegadas/Code/Functions.java`, `simulacao/config/simulacao.csv`, `simulacao/interface_simulacao.html`, `CLAUDE.md`, `docs/gestores.md`

## ALT-191 — pedido-criado-na-analise-detalhada
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** com a análise detalhada desligada, o `pedido_criado` ainda era a maior parte do `sim.log`.
- **O que (GestorDeChegadas):** `pedido_criado` só sai com `log_analise_detalhada = true`.
- **Arquivos:** `_alp/Agents/GestorDeChegadas/Code/Functions.java`, `docs/gestores.md`

## ALT-192 — rota-inteira-da-pera
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock em ≈16,2 h. M86 (moegaoTerra) parado na `via3_intermar` com `val_via2` + cruzamento esperando a `via2_c`; MNB39 na `via2_c` esperando a `via2_intermar_c` (cauda do MNB21); MNB21 (segue direto nas DVs) esperando `val_via2` + cruzamento.
- **O que (GestorDeCirculacao):**
  - Nova função `eh_terminal_pera(trem)`: moegaoTerra, moegaoMar, coperXXI.
  - `get_conjunto_licenca`: na `val_via2_a`, o trem da pera pega também `val_via2`, `via3_intermar`, `via3_a`, `via2_c` e `entrada_pera`.
  - onExit do `licenciar_esta_faixa` / `licenciar_proxima_faixa`: essas faixas ficam LICENCIADAS para ele.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/Code/Functions.xml`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-193 — desfaz-rota-inteira-da-pera
- **Data:** 2026-11-20
- **Status:** aplicada
- **O que:** desfaz a ALT-192 (`eh_terminal_pera`, rota da pera em `get_conjunto_licenca` e licença das faixas no onExit dos Seize de licença), a pedido do Guilherme, para reanalisar o deadlock M86 × MNB39 × MNB21.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/Code/Functions.xml`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-194 — traces-de-licenca-e-cruzamento
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** o log não mostrava quem espera licença, quem é dono de cada faixa nem quem está com o cruzamento; os deadlocks eram deduzidos pela ausência de registros.
- **O que:**
  - GestorDeCirculacao: variáveis `dono_cruzamento` (Trem) e `esperando_licenca` (HashSet<Integer>); funções `texto_trem_id`, `log_aguarda_licenca`, `log_pegou_licenca`, `registrar_dono_cruzamento`.
  - `licenciar_esta_faixa` / `licenciar_proxima_faixa`: `aguarda_licenca` no onEnter e `pegou_licenca` no onExit.
  - GestorDeDVs `seize_cruzamento_entrada` / `ocupar_val_via2_saida`: os mesmos traces.
  - `liberar_faixas_ocupadas`: `soltou <faixa>` e limpa `dono_cruzamento` ao soltar o cruzamento.
  - Nenhuma mudança de comportamento.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/Code/Functions.xml`, `_alp/Agents/GestorDeCirculacao/Variables.xml`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-195 — release-antes-de-licenciar
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock em 16:10 de 01/01. M86 parado no fim da `via3_intermar` esperando a `via2_c` (MNB39) ainda segurava `val_via2` + cruzamento, com a cauda já fora deles; o MNB21 precisava dos dois para sair das DVs e a cauda dele prendia a `via2_intermar_c` que o MNB39 esperava. O Release das faixas de trás só rodava depois de conseguir a próxima licença.
- **O que (GestorDeCirculacao):**
  - Novo Release `liberar_antes_de_licenciar` entre `tentar_usar_faixa` e `esta_licenciado_nesta_faixa`, com `liberar_faixas_sem_mnb_pcx`.
  - Nova função `liberar_faixas_sem_mnb_pcx(trem)` e variável `pular_mnb_pcx`: mesma lógica do `liberar_faixas_ocupadas` sem soltar o `mnb_pcx` (que depende da `marginal_c` já licenciada).
  - `liberar_faixas_usadas` continua no fim do passo.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeCirculacao/Code/Functions.xml`, `_alp/Agents/GestorDeCirculacao/Variables.xml`, `_alp/Agents/GestorDeCirculacao/EmbeddedObjects.xml`, `_alp/Agents/GestorDeCirculacao/Connectors.xml`, `docs/gestores.md`

## ALT-196 — acesso-moema-antes-das-faixas-no-alivio-da-4000
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** travamento em 18:09 de 03/01 (68,2 h). U08 (TES, lote 45990) pegou o `acesso_moema` e ficou no `seize` do encoste da linha 03 (`rp_mnb_linha03` + `linha3000_b..e`, todos livres). O MNB15 (alívio da 4000) pedia `acesso_moema` + faixas no mesmo Seize e estava na frente da fila da `linha3000_c` sem ter o acesso, travando o U08.
- **O que (GestorDeAlivios):**
  - Novo Seize `pegar_acesso_moema_pcx` (só `rp_acesso_moema`) entre `comecar_alivio` e `bloquear_faixas_ate_terminal`; a espera do `diag_alivio_pcx` começa nele.
  - `conjunto_alivio_pcx()` não inclui mais o acesso: `bloquear_faixas_ate_terminal` pega só as faixas.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `docs/gestores.md`

## ALT-197 — cruzamento-antes-da-val-via2-na-saida-das-dvs
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** travamento em 11:53 de 04/01 (83,9 h). X18 (saindo do ISN) tinha `val_via2_a` + cruzamento e esperava a `val_via2` livre; o MNB85 (segue direto, prioridade 10) pedia `val_via2` + `isn_linha02_e` + cruzamento no mesmo Seize, ficava na frente da fila da `val_via2` sem o cruzamento e travava o X18.
- **O que:**
  - GestorDeDVs: novo Seize `pegar_cruzamento_saida` (só o cruzamento, prioridade 10) entre `liberar_antes_saida` e `ocupar_val_via2_saida`.
  - GestorDeCirculacao `get_conjunto_saida_dvs`: só `val_via2` + `isn_linha02_e`.
- **Arquivos:** `_alp/Agents/GestorDeCirculacao/Code/Functions.java`, `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `_alp/Agents/GestorDeDVs/Connectors.xml`, `docs/gestores.md`

## ALT-198 — encoste-do-tes-espera-o-alivio
- **Data:** 2026-11-20
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 16:25 de 06/01 (136,4 h): `tes.ir_linha03.outHit` sem conexão. O alívio da 4000 pegou o lote da linha 03 (`lote_linha_3` ficou null), o fim da descarga da moega 04 soltou o resto da 3000 para o encoste, e o alívio deixou 4 vazios na linha 03; o encoste (I06/64138) bateu neles.
- **O que (TES):** nova função `linhas_livres_para_encoste()`, usada pelo `tentar_encostar_resto_3000` e pelo `tentar_encostar_novo_lote_3000`: além dos lotes das linhas 03/04/05 e fundo 04 vazios, exige que não haja alívio em andamento (`rp_mnb_linha03` e `rp_mnb_linha04e5` livres, `wait_retomar_linha03` vazio) e que a track do `_tes_linha03_fim` esteja sem trem.
- **Arquivos:** `_alp/Agents/TES/Code/Functions.java`, `_alp/Agents/TES/Code/Functions.xml` (o repositório não tem ficha `docs/terminais/tes.md`)

## ALT-199 — desfaz-encoste-do-tes-espera-o-alivio
- **Data:** 2026-11-20
- **Status:** aplicada
- **O que:** desfaz a ALT-198 (`linhas_livres_para_encoste` no TES), a pedido do Guilherme. Com a checagem física da linha 03, o encoste testado no fim do alívio (trem do alívio ainda saindo da linha 03) ficava esperando e ninguém testava de novo: o TES parou às 04:41 de 01/01.
- **Arquivos:** `_alp/Agents/TES/Code/Functions.java`, `_alp/Agents/TES/Code/Functions.xml`

## ALT-200 — lote-da-linha-03-so-fica-livre-no-fim-do-corte
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 16:25 de 06/01 (136,4 h, `tes.ir_linha03.outHit`). O `alivio_linha03` zerava o `lote_linha_3` ao começar a pegar o lote; nessa janela o fim da descarga da moega 04 soltou o resto da 3000 para o encoste; o alívio levou 2 e devolveu 4 vazios para a linha 03, e o encoste bateu neles.
- **O que (TES):**
  - Nova variável `lote_alivio_03` (Trem).
  - `alivio_linha03` (onEnter): guarda o lote em `lote_alivio_03` e não zera mais o `lote_linha_3`.
  - `remover_excesso_03` (onExit, parte que segue, roda depois do `excesso_linha03`): se o `lote_linha_3` ainda é o lote aliviado (não sobrou nada), zera; limpa `lote_alivio_03`.
- **Arquivos:** `_alp/Agents/TES/Variables.xml`, `_alp/Agents/TES/EmbeddedObjects.xml`

## ALT-201 — excedente-da-4000-na-track178
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 13:38 de 08/01 (181,6 h): MNB03 (alívio da 4000, 82 vagões) voltou do ADM e entrou na 4000 pela ponta da `4000_c`; a 4000 comporta ≈ 80, e a cauda ficou no sw111/`track176`/sw110. Ele soltou o `acesso_moema` e o M60 (TES) passou pelo sw110 (`railwaySwitch110` ocupado por dois trens).
- **O que (GestorDeAlivios):**
  - Novo parâmetro `LIMITE_SEM_MANOBRA_4000 = 78`.
  - Entre `ir_linha_4000` e `liberar_faixas_ate_terminal`: `sel_excede_4000` (vagões > 78?) → `seize_intervalo_4000` (`intervalo4000_c/b`) → `avancar_criar_loco` (ré até `pos_linha_4000_criar_loco`) → `recuar_track178` (frente até `_linha4000_excedente`, na `track101`) → `liberar_intervalo_4000`. Com até 78 vagões segue direto como antes.
  - Ponto `_linha4000_excedente` criado pelo Guilherme na `railwayTrack101`.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `_alp/Agents/GestorDeAlivios/Variables.xml`, `docs/gestores.md`

## ALT-202 — corrige-onexit-do-recuar-track178
- **Data:** 2026-11-21
- **Status:** aplicada, em teste
- **Problema:** erro de compilação em 01:54 de 01/01 (`agent cannot be resolved` no onExit do `recuar_track178`): em TrainMoveTo a variável é `train`.
- **O que (GestorDeAlivios):** `recuar_track178` onExit usa `train.getId()`.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`

## ALT-203 — volta-do-excedente-antes-de-ir-ao-terminal
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 07:51 de 02/01: `ir_entrada_terminal` sem rota da `railwayTrack101` (sentido do fim da track) até a `linha2000_e`. Depois da manobra do excedente, as locos ficam na `track101`, viradas para o sw103.
- **O que (GestorDeAlivios):** entre `bloquear_faixas_ate_terminal` e `ir_entrada_terminal`: `sel_esta_no_excedente` (ocupa `track178` ou `track101`?) → `voltar_do_excedente` (TrainMoveTo de ré até 10 m da `linha4000_a`, como o `ir_linha_4000`). O trem já tem acesso e faixas nesse momento.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `docs/gestores.md`

## ALT-204 — faixas-da-loco-do-alivio-bracell
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 20:36 de 02/01: a loco do alívio da Bracell nasceu na `pst_silosMuro_a` em cima do U18. O `get_faixas_primeiro_alivio()` da Bracell devolvia lista vazia, então o `bloquear_faixas_pmc` passava sem pegar nenhuma faixa.
- **O que (Bracell):**
  - `get_faixas_primeiro_alivio()` devolve `silosMuro_a`, `marginal_c`, `intervalo4000_a`, `intervalo4000_b`, `intervalo4000_c` (caminho da loco até a `_bracell_alivio`).
  - Função nova `get_recursos_loco_alivio(trem)`: pools dessas faixas que a loco ainda segura.
  - Release novo `liberar_faixas_loco_alivio` entre `iniciar_alivio` e `irAteTerminal`; no onExit volta para LIVRE as faixas com `idTremLicenciado == 9999` (mesmo padrão da Suzano).
- **Efeito colateral aceito:** a `intervalo4000_b` fica com a loco durante o trajeto; nesse período a 4000 não sai nem faz a manobra do excedente.
- **Arquivos:** `_alp/Agents/Bracell/Code/Functions.java`, `_alp/Agents/Bracell/Code/Functions.xml`, `_alp/Agents/Bracell/EmbeddedObjects.xml`, `_alp/Agents/Bracell/Connectors.xml`, `docs/terminais/bracell.md`

## ALT-205 — chegada-sem-acesso-moema
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** travamento em 20:37 de 02/01 (45,3 h). U18 (ADM) parou no `acesso_moema_chegada` com a cauda na `silosMuro_a`; o acesso estava com o MNB89 (alívio do TES), que esperava `intervalo4000_c/b` para o excedente, na fila atrás da loco do alívio da Bracell, que esperava a `silosMuro_a`.
- **O que (ADM, TES, T39):** a chegada até o ponto de encoste não pede o `rp_acesso_moema` (já anda com as faixas da circulação). Removidos o Seize `acesso_moema_chegada` e os Releases `liberar_acesso_chegada` e `liberar_acesso_espera`; setas religadas (`novo_lote → linha_X000_vazia`, `mov/encosta ... → liberar_faixas_circulacao`, `liberar_faixas_espera → próximo bloco`). O acesso das manobras na moega (`acesso_moema_manobra`, `acesso_moema_encoste`, `acesso_moema_corte`) não muda.
- **Arquivos:** `_alp/Agents/ADM/EmbeddedObjects.xml`, `_alp/Agents/ADM/Connectors.xml`, `_alp/Agents/TES/EmbeddedObjects.xml`, `_alp/Agents/TES/Connectors.xml`, `_alp/Agents/T39/EmbeddedObjects.xml`, `_alp/Agents/T39/Connectors.xml`, `docs/gestores.md`

## ALT-206 — saida-da-4000-pega-mnb-pcx
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** travamento em 22:08 de 03/01 (70,1 h). MNB45 (trem cheio saindo da 4000) segurava `marginal_c/b` e esperava a `marginal_a`; a loco do alívio do Tgrao (MNB53) tinha pego o `mnb_pcx`, nasceu na `marginal_a` e esperava `marginal_b/c`. A saída da 4000 passava pela área comum sem o `mnb_pcx`.
- **O que (GestorDeAlivios):** Seize novo `ocupar_mnb_pcx_saida_4000` (só `rp_mnb_pcx`) entre `linha_4000_cheia` (T) e `seize_saida_4000`. A devolução é a regra existente do `liberar_faixas_ocupadas` (`eh_alivio`, cauda fora da `marginal_c`).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `docs/gestores.md`

## ALT-207 — volta-do-excedente-ate-criar-loco
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 19:15 de 02/01 (43,25 h): `ir_entrada_terminal` sem rota da `railwayTrack178` (offset 15) até a `pmc_linha2000_e`. Com 81 vagões, o MNB87 encostado a 10 m da `linha4000_a` ainda tinha a ponta na `track178`; da `track178` não há caminho até a `track176`.
- **O que (GestorDeAlivios):** no ramo T do `sel_esta_no_excedente`: `seize_intervalo_volta` (intervalo4000_c/b, priority 10) → `voltar_do_excedente` (agora até o `pos_linha_4000_criar_loco`, TARGET_AT_POINT_ON_TRACK) → `ir_entrada_terminal1` (cópia do `ir_entrada_terminal`) → `liberar_intervalo_volta` → `para_alivio_terminal_pcx2`. A intervalo4000_c/b fica com o trem até a chegada no terminal.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `docs/gestores.md`

## ALT-208 — rechego-coperxi-bloqueia-marginais
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 17:18 de 10/01 (233,3 h): a loco FIPS do rechego do CoperXI (trem 90012) entrou no `trainEnter` em `pos_locoManobra_PST` (`marginal_a`, offset 40) em cima da cauda do MNB25 (retorno do alívio do Eldorado, licenciado na `marginal_a`). O rechego não pedia nenhuma faixa da circulação. A mensagem cita `loco_pcx_starts`, mas o carro adicionado era o do CoperXI.
- **O que (CoperXI):**
  - Seize novo `bloquear_marginal_rechego` (conjunto único: `marginal_a/b/c`, `intervalo4000_a/b`, `pst_switch36`) entre `seize_locoFips_pst` e `mnbCarregados_ocupar3`; onExit faz `bloquear()` nas 5 faixas e liga o switch73 `marginal_a ↔ b`, como o `bloquear_encoste_marginal`. Fica antes do recurso de manobra para não esperar as marginais segurando o `rp_manobrando_carregados`.
  - `mnbCarregados_liberar2` (fim do rechego): `seizeBlocks = { mnbCarregados_ocupar3, bloquear_marginal_rechego }`; onExit volta para LIVRE as faixas com o id do trem.
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXI/Connectors.xml`

## ALT-209 — gestor-de-locomotivas-guarda-uma-loco-por-vez
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 13:47 de 19/01 (445,8 h): `bracell.colocar_L2b` sem rota de `_bracell_L1` (offset 19) até `_bracell_L2`. A loco do alívio (99649) era um trem com 2 locos tirado do `locomotivasRumo`; com 22,5 m a mais, o `recuar4` não tirava o trem da L1. O GestorDeLocomotivas guardava o trem como chegava, e trens de 2 locos ficavam juntos no wait.
- **O que (GestorDeLocomotivas):** entre `guardar_loco` e `sairDaTrack`: `tem_varias_locos` (SelectOutput, `size() > 1`) → `separar_loco` (TrainDecouple, `nCars = 1`); `outDecoupled` → `sairDaTrack`, `out` → volta ao `tem_varias_locos`; F → `sairDaTrack`. Log `separar_loco`.
- **Arquivos:** `_alp/Agents/GestorDeLocomotivas/EmbeddedObjects.xml`, `_alp/Agents/GestorDeLocomotivas/Connectors.xml`, `docs/gestores.md`

## ALT-210 — loco-da-4000-pega-faixas-depois-das-locos
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** exceção em 14:45 de 30/01 (710,75 h): `loco_pcx_starts` criou as locos na `intervalo4000_b` em cima do MNB59 (retorno do alívio da Bracell). A saída da 4000 foi conferida no `inject()`, antes das 14:40; o trem ficou esperando locos FIPS no `usar_recurso_loco_pcx` (0/8 livres) e, quando pegou, foi direto ao `trainEnter` sem nada segurando a `intervalo4000_b`.
- **O que (GestorDeAlivios):**
  - Ordem invertida: `loco_pcx_starts → usar_recurso_loco_pcx → bloquear_4000 → trainEnter`.
  - `bloquear_4000` pega também `intervalo4000_b/c` (mesmo conjunto da linha 4000).
  - Release novo `liberar_intervalo_criacao` entre `ir_entrada_terminal` e `para_alivio_terminal_pcx2`, com `get_recursos_intervalo_criacao(trem)` (só os pools que o trem possui).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `_alp/Agents/GestorDeAlivios/Code/Functions.xml`, `docs/gestores.md`

## ALT-211 — configuracao-da-simulacao-com-semente
- **Data:** 2026-11-21
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** o commit `dd361e7` (campos de semente na interface) partiu de uma cópia antiga do `interface_simulacao.html` e apagou o campo Data de início e o leitor do log `[hh:mm - dd/mm]` (`RX_DATA`, `horasDesdeInicio`). As colunas do CSV passaram a `duracao_h;warmup_h;replicacoes;semente_tipo;semente`, e o Main lia o `simulacao.csv` pela posição (a 4ª coluna seria lida como data).
- **O que:**
  - Interface: junta as duas versões (semente + data de início + leitor do log). Colunas: `duracao_h;warmup_h;replicacoes;data_inicio;semente_tipo;semente`.
  - Main `carregar_configuracao`: lê o `simulacao.csv` pelo nome da coluna; `data_inicio`, `semente_tipo` (padrão `fixa`) e `semente` (padrão 1) são opcionais. Variáveis novas `semente_tipo` (String) e `semente` (long), mostradas no log `[config]`. Ainda não mudam a rodada (fica para o experimento de replicações).
  - `simulacao/config/simulacao.csv` com as 6 colunas.
- **Arquivos:** `simulacao/interface_simulacao.html`, `simulacao/config/simulacao.csv`, `_alp/Agents/Main/Code/Functions.java`, `_alp/Agents/Main/Variables.xml`

## ALT-212 — replicacoes
- **Data:** 2026-11-21
- **Status:** parte do modelo aplicada; o experimento `Replicacoes` (Custom Experiment) é criado pelo Guilherme no editor, com o código passado no chat
- **O que:**
  - Main: variável nova `replicacao_atual` (int, 0 = rodada normal). `configurar_log_em_arquivo` grava em `logs/sim_rep<n>.log` quando `replicacao_atual > 0`; a rodada normal continua em `logs/sim.log`.
  - Experimento `Replicacoes`: lê `simulacao.csv` (replicações, semente_tipo, semente), roda em sequência e sem animação; semente `semente + r - 1` (fixa) ou sorteada (variável). A parada continua no On startup do Main (`duracao_h`).
  - `MaximumMemory` do `Simulation` já estava em 8192 MB (alterado pelo Guilherme).
- **Arquivos:** `_alp/Agents/Main/Code/Functions.java`, `_alp/Agents/Main/Variables.xml`

## ALT-213 — csv-trens-criados
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **O que (GestorDeChegadas):** primeiro indicador de saída para a interface. `simulacao/output/trens_criados.csv` (ou `trens_criados_rep<n>.csv`) com uma linha por trem criado: `pedido;prefixo;trem_id;terminal;ferrovia;segmento;data_pedido;data_entrada;warm_up`.
  - `iniciar_csv_trens()` chamada no `iniciar()`: cria a pasta e o cabeçalho.
  - `registrar_trem_csv(p, trem)` chamada no `receber_trem()`: grava em append na hora, para não perder dados se a rodada cair.
  - Variável nova `csv_trens` (caminho do arquivo).
  - Pedidos ainda na fila no fim da rodada ficam de fora; trens do warm-up entram com `warm_up = true`.
- **Arquivos:** `_alp/Agents/GestorDeChegadas/Code/Functions.java`, `_alp/Agents/GestorDeChegadas/Code/Functions.xml`, `_alp/Agents/GestorDeChegadas/Variables.xml`, `docs/gestores.md`

## ALT-214 — pasta-trens-criados
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **O que (GestorDeChegadas):** o CSV dos trens criados passa para a pasta própria `simulacao/output/trens_criados/` (`trens_criados.csv` ou `trens_criados_rep<n>.csv`). Só o caminho em `iniciar_csv_trens()`; a pasta é criada automaticamente.
- **Arquivos:** `_alp/Agents/GestorDeChegadas/Code/Functions.java`, `docs/gestores.md`

## ALT-213 — loco-fips-pcx-pega-silosmuro-c
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** replicação 5 (semente 5), 02:24 de 01/01 (2,4 h): as 2 locos FIPS do TES nasceram em `t39_criar_loco_rechego` (`pst_silosMuro_c`) em cima da cauda do Y48, que ainda tinha a faixa licenciada. O ponto é usado por TES, ADM e T39, e nenhum pedia a faixa antes de criar a loco. A mensagem cita `loco_pcx_starts`, mas o carro era do TES.
- **O que (TES, ADM, T39):** o lote pega a `silosMuro_c` antes de chamar a loco e solta depois do engate (a loco entra no `in2` e é absorvida; o lote segue).
  - Seize novo `ocupar_silosMuro_c_loco` entre `usar_recurso_loco` e o TrainCouple (`acoplar_loco_pcx` / `acoplar_loco_mnb` / `acoplar_loco`); onExit faz `bloquear` na faixa e o `loco_pcx_starts.inject()`.
  - O `inject()` saiu do onExit do `usar_recurso_loco` (TES, T39) e do `onEnter1` do `acoplar_loco_mnb` (ADM).
  - Release novo `liberar_silosMuro_c_loco` (`ALL_FROM_SEIZES { ocupar_silosMuro_c_loco }`) depois do engate; onExit volta a faixa para LIVRE se estiver com o id do lote.
- **Arquivos:** `_alp/Agents/TES|ADM|T39/EmbeddedObjects.xml`, `_alp/Agents/TES|ADM|T39/Connectors.xml`

## ALT-214 — avanco-t39-adm-pega-intervalo4000
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** 04:17 de 01/01 (4,3 h): a loco do alívio da Bracell (MNB33) bateu no trem da 4000 (MNB25, 78 vgs) na `intervalo4000_b`. No alívio do T39, o `paraFrente` leva o trem até `pos_linha_4000_criar_loco` (intervalo4000_b), limitado ao comprimento; acima de ~40 vagões a ponta entra na intervalo sem o recurso. O `sair_linha7` do ADM tem o mesmo alvo.
- **O que:**
  - GestorDeAlivios: parâmetro `LIMITE_VGS_INTERVALO_4000 = 40`; funções `pools_intervalo_faltando(trem)` (intervalo4000_c/b que o trem não segura) e `get_intervalo_para_avancar(trem)` (vazio até o limite). `seize_intervalo_4000` e `seize_intervalo_volta` passam a usar `pools_intervalo_faltando`.
  - T39: Seize `ocupar_intervalo_avanco` entre `trainMoveTo` e `paraFrente`.
  - ADM: Seize `ocupar_intervalo_avanco` antes do `sair_linha7` (as três entradas).
  - A soltura fica no `liberar_faixas_ate_terminal` (volta para a 4000).
- **Arquivos:** `_alp/Agents/GestorDeAlivios/` (Variables.xml, EmbeddedObjects.xml, Code/Functions.java, Code/Functions.xml), `_alp/Agents/T39/` e `_alp/Agents/ADM/` (EmbeddedObjects.xml, Connectors.xml), `docs/gestores.md`

## ALT-224 — pedidos-nao-atendidos
- **Data:** 2026-11-26
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** os pedidos que ainda estavam na fila no fim da rodada não iam para nenhum arquivo. Na rodada de 26/11 foram cerca de 20 mil pedidos para 1.632 trens criados.
- **O que (GestorDeChegadas):**
  - `iniciar_csv_pedidos()` (no `iniciar`): cria `simulacao/output/pedidos_nao_atendidos/pedidos_nao_atendidos.csv` (`pedidos_nao_atendidos_rep<n>.csv` nas replicações) só com o cabeçalho `pedido;terminal;ferrovia;manual;data_pedido;espera_ate_fim_h;ultimo_motivo;warm_up`.
  - `registrar_fila_csv()`: uma linha por pedido que não virou trem: o `pedido_atual` (motivo `injetado, trem não nasceu`) e os pedidos da `fila`, na ordem. `;` do motivo vira ` |`.
  - `ev_relogio`: chama `registrar_fila_csv()` no último tique antes de `duracao_h` (uma vez por rodada, variável `fila_registrada`). Funciona com e sem replicações.
  - Variáveis `csv_pedidos` (String) e `fila_registrada` (boolean, `false`).
- **Riscos conhecidos:** se a rodada cair ou for parada antes do fim, o arquivo fica só com o cabeçalho.
- **Arquivos:** `_alp/Agents/GestorDeChegadas/` (Variables.xml, Code/Functions.java, Code/Functions.xml, Code/Events.java), `docs/gestores.md`

## ALT-223 — lista-de-vagoes-ativos
- **Data:** 2026-11-26
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Objetivo:** seção 1 do registro dos vagões que não chegam ao `trainExit`: saber quais vagões ainda estão no modelo.
- **O que:**
  - GestorDeChegadas: collection `vagoes_ativos` (`LinkedHashSet<Vagao>`); `marcar_vagoes` adiciona o vagão na criação do trem e `registrar_vagoes_saida` remove no `trainExit`.
  - GestorDeDVs: o log `vagoes_exit4` mostra também `ativos=<n>`.
- **Próximas seções:** gravar os ativos no fim da rodada (coluna `situacao`), coluna `carregado`, coluna `track_final`.
- **Arquivos:** `_alp/Agents/GestorDeChegadas/` (Variables.xml, Code/Functions.java), `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-222 — vagoes-trem-criado-e-log-exit4
- **Data:** 2026-11-26
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **O que:**
  - Vagao: variáveis `trem_criado_id` e `prefixo_trem_criado`, preenchidas no `marcar_vagoes` (GestorDeChegadas).
  - `vagoes.csv`: colunas `trem_criado_id;prefixo_trem_criado` depois de `vagao_id`.
  - GestorDeDVs: contador `vagoes_no_exit4` e log `vagoes_exit4` (trem, vagões, acumulado) no `onExit` do `exit4`, para medir quantos vagões ficam vivos fora do fluxograma.
  - Vagões ainda no modelo no fim da rodada continuam fora do CSV (decisão).
- **Arquivos:** `_alp/Agents/Vagao/Variables.xml`, `_alp/Agents/GestorDeChegadas/Code/Functions.java`, `_alp/Agents/GestorDeDVs/` (Variables.xml, EmbeddedObjects.xml), `docs/gestores.md`

## ALT-221 — indicador-de-vagoes
- **Data:** 2026-11-26
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Objetivo:** registrar cada vagão do nascimento à saída da Margem Direita, com o terminal de origem, porque ao longo do processo o vagão é separado do trem original.
- **O que:**
  - Vagao: variáveis `hora_criacao_h` (On startup), `hora_descarga_h` (em `descarregar()`), `hora_descarte_h`, `posicao_absoluta` (índice do carro, locos incluídas, 0 = cabeça) e `posicao_relativa` (só vagões, a partir de 1).
  - GestorDeChegadas: `marcar_vagoes(trem)` no `receber_trem` copia `trem.ITerminalMoega_destino` para cada vagão (`setTerminal`) e grava as posições; `iniciar_csv_vagoes()`, `registrar_vagoes_saida(trem)`, `registrar_vagao_csv(v)` e variável `csv_vagoes`.
  - GestorDeDVs: o `onEnter` do `trainExit` (fim do circuito) chama `registrar_vagoes_saida` antes do `finalizar_manobra`.
  - Saída: `simulacao/output/wagons/vagoes.csv` (`vagoes_rep<n>.csv` nas replicações), uma linha por vagão na saída: `vagao_id;terminal;ferrovia;segmento;posicao_absoluta;posicao_relativa;data_criacao;data_descarga;data_descarte;tempo_ate_descarga_h;tempo_vazio_h;tempo_no_sistema_h;warm_up`. Tempos em horas com vírgula decimal.
- **Fora do escopo:** vagões ainda no modelo no fim da rodada não entram no CSV; o `exit4` continua Exit simples (vagões não são destruídos).
- **Arquivos:** `_alp/Agents/Vagao/` (Variables.xml, AOC.Vagao.xml, Code/Functions.java), `_alp/Agents/GestorDeChegadas/` (Variables.xml, Code/Functions.java, Code/Functions.xml), `_alp/Agents/GestorDeDVs/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-220 — tgrao-saida-alivio-sem-via3c
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** replicação 3 (semente 3) travou às 07:57 de 04/01 (≈80 h) sem erro: o `ocupar_saida_alivio` do Tgrao pedia `linha6000_a`, `marginal_c`, `silosMuro_a` e `via3_c` de uma vez; o Y72 (T39) estava na `via3_c` esperando a `silosMuro_a` livre, mas o pedido do Tgrao estava à frente na fila do `rp_silosMuro_a`. Em cascata: L68 (Suzano) no `cruzamento_porteira` esperando a `via3_intermar`, MNB15 (alívio Bracell) nas DVs esperando o cruzamento.
- **O que (Tgrao):** `ocupar_saida_alivio` passa a pedir só `rp_pmc_linha6000_a`, `rp_pmc_marginal_c` e `rp_silosMuro_a`; o onExit não ocupa mais a `faixa_via3_c`. O trem do alívio entra na circulação pela `marginal_c`.
- **Arquivos:** `_alp/Agents/Tgrao/EmbeddedObjects.xml`, `docs/terminais/tgrao.md`

## ALT-219 — coperxxi-alivio-termina-ao-sair-da-linha
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** replicação 1 (semente 1), 01:46 de 03/01 (49,8 h): o trace mostrou que o "vagão solo" era o alívio anterior (MNB29, loco + 2 vgs), parado na `psnSaidaCopexXXI_b` (470,5–528 m) esperando licença na `via1_intermar_c`. O CoperXXI já tinha liberado a descarga; o grupo 43488 bateu nele e o lote b se formou em 550–935 m, o lote a em 935–1058 m. No alívio seguinte a ponta passou do `_coperXII_moega` e o `engatar_a` ficou sem rota.
- **O que (CoperXXI):**
  - `retornar_gestorDeAlivios` (onExit): guarda o trem em `trem_alivio_na_linha` e não encerra o alívio.
  - Novo evento cíclico `verificar_saida_alivio` (30 s): com o trem fora da `psnSaidaCopexXXI_b`, chama `finalizar_alivio()`.
  - Nova função `finalizar_alivio()` com o código que estava no onExit.
- **Arquivos:** `_alp/Agents/CoperXXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXXI/Variables.xml`, `_alp/Agents/CoperXXI/AOC.CoperXXI.xml`, `_alp/Agents/CoperXXI/Code/Events.xml` (novo), `_alp/Agents/CoperXXI/Code/Events.java` (novo), `_alp/Agents/CoperXXI/Code/Functions.java`, `_alp/Agents/CoperXXI/Code/Functions.xml`, `docs/terminais/coperxxi.md`

## ALT-218 — coperxxi-log-trens-na-linha
- **Data:** 2026-11-23
- **Status:** aplicada (só log)
- **Problema:** replicação 1 (semente 1) parou às 01:46 de 03/01 (49,8 h) no `engatar_a` do CoperXXI (sem rota: a ponta do trem já estava depois do `_coperXII_moega`). Um vagão solo, fora dos estoques, estava na `psnSaidaCopexXXI_b`; às 00:48 o grupo 43488 bateu nele (`ir_lote_b` outHit com estoque b vazio) e o lote b se formou fora do lugar. O log não mostra de onde veio o vagão.
- **O que:** função `listar_trens_linha(String evento)` (log de análise, `trens_na_linha`), chamada no `onExitHit` de `engatar_b`, `engatar_a`, `ir_lote_b` e `ir_lote_a`.
- **Arquivos:** `_alp/Agents/CoperXXI/Code/Functions.java`, `_alp/Agents/CoperXXI/Code/Functions.xml`, `_alp/Agents/CoperXXI/EmbeddedObjects.xml`, `docs/terminais/coperxxi.md`

## ALT-217 — desliga-log-de-analise-t39
- **Data:** 2026-11-23
- **Status:** aplicada
- **O que:** `log_de_analise` do T39 volta para `false` (o do TES já estava `false`).
- **Arquivos:** `_alp/Agents/T39/Variables.xml`

## ALT-216 — avanco-do-alivio-da-4000-pega-mnb-pcx-antes
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock às 04:19 de 01/01 (semente 5): o trem da 4000 (29969, 78 vgs) pegou a `intervalo4000_c/b` no avanço do T39 sem o `mnb_pcx` e esperava o `mnb_pcx` para o excedente; a loco do alívio da Bracell (29075) tinha o `mnb_pcx` e esperava a `intervalo4000_b/c`. Ordem de recursos invertida.
- **Regra:** quem usa a `intervalo4000` pega o `mnb_pcx` antes.
- **O que:**
  - T39 e ADM: novo Seize `ocupar_mnb_pcx_avanco` antes do `ocupar_intervalo_avanco` (no ADM, as três entradas). Pede o `mnb_pcx` só acima de `LIMITE_VGS_INTERVALO_4000` e se o trem ainda não tem (`get_mnb_pcx_para_avancar`).
  - GestorDeAlivios: funções `mnb_pcx_faltando(Trem)` e `get_mnb_pcx_para_avancar(Trem)`; `ocupar_mnb_pcx_excedente` e `ocupar_mnb_pcx_volta` passam a pedir `mnb_pcx_faltando`.
  - Soltar: sem mudança (`liberar_intervalo_4000`/`_volta` ou `liberar_faixas_ate_terminal`).
- **Efeito colateral:** em alívio do T39/ADM com mais de 40 vagões, o trem da 4000 segura o `mnb_pcx` até voltar para a 4000; Bracell, Suzano, Eldorado e Tgrao esperam.
- **Arquivos:** `_alp/Agents/T39/EmbeddedObjects.xml`, `_alp/Agents/T39/Connectors.xml`, `_alp/Agents/ADM/EmbeddedObjects.xml`, `_alp/Agents/ADM/Connectors.xml`, `_alp/Agents/GestorDeAlivios/Code/Functions.java`, `_alp/Agents/GestorDeAlivios/Code/Functions.xml`, `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `docs/gestores.md`

## ALT-215 — excedente-da-4000-pega-mnb-pcx
- **Data:** 2026-11-23
- **Status:** aplicada, em teste (abrir no AnyLogic e compilar)
- **Problema:** deadlock em 18:46 de 05/01 (115,3 h): o L42 (carregado da Bracell, com o `mnb_pcx` desde 18:40) parou na `intervalo4000_a` atrás do MNB85 (trem da 4000, 80 vgs), que fez a manobra do excedente às 18:45 sem o `mnb_pcx` e ficou no caminho; o MNB85, mandado de novo ao TES, esperava a `intervalo4000_c/b` licenciada pelo L42. A cauda do L42 na `via3_d` travou a porteira.
- **O que (GestorDeAlivios):**
  - `ocupar_mnb_pcx_excedente` (só `rp_mnb_pcx`) entre `sel_excede_4000` (T) e `seize_intervalo_4000`.
  - `ocupar_mnb_pcx_volta` (só `rp_mnb_pcx`) entre `sel_esta_no_excedente` (T) e `seize_intervalo_volta`.
  - `liberar_intervalo_4000` e `liberar_intervalo_volta` soltam também o `rp_mnb_pcx`.
  - O encoste da Bracell já pega o `mnb_pcx` no `para_via3_b` (sem mudança).
- **A conferir:** se, depois do `recuar_track178`, a ponta do trem de 80+ vagões do lado do sw53 fica livre.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/EmbeddedObjects.xml`, `_alp/Agents/GestorDeAlivios/Connectors.xml`, `docs/gestores.md`
