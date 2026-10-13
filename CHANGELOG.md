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

