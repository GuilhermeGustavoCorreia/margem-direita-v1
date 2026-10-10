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
