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

### ALT-2.1 — coperxi-pausar-descarga-no-alivio
- **Data:** 2026-10-10
- **Status:** aplicada e validada (2º trem descarregou sem colisão)
- **Problema:** erro `The car being added on the track 'pstSaidaCoperXI_terra_a' intersects with another car` na descarga do 2º trem. O excedente do alívio (10 vgs) voltava para a linha terra_a depois que a descarga já tinha colocado 3 vazios novos nela (10 + 3 > capacidade).
- **Causa raiz:** `fazendoAlivio` nunca era setado no CoperXI, então `pegarVagaoPraDescarga` e `aguardaAlivio` não pausavam a descarga durante o alívio. Agravante: o excedente voltava ao `wait_bloco*` sem atualizar `_loteDeVazios_*`.
- **Alterações (CoperXI):**
  - `takeAlivio()`: `fazendoAlivio = true; aguardaAlivio.block();`
  - `deixarTerminal.onEnter`: `fazendoAlivio = false;`
  - `mnbVazios_liberar19/17/21/23.onExit`: `_loteDeVazios_LinhaMar_a / Mar_b / Terra_a / Terra_b = (Trem) agent;`
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXI/Code/Functions.java`

### ALT-2.2 — gestor-alivio-aguardar-loco
- **Nos comentários do código:** `ALT-003`
- **Data:** 2026-10-10
- **Status:** aplicada (reaplicada após o AnyLogic sobrescrever), aguardando validação
- **Problema:** no 2º trem o CoperXI chegava em overload e o alívio nunca acontecia.
- **Causa raiz:** em `fazer_procedimento_de_alivio_pst`, o terminal era removido de `terminais_pst` (`colocar_terminal_em_alivio_pst`) antes de verificar se havia locomotiva. No fim do 1º trem, a regra de "últimos vazios" pediu alívio dos 7 vazios restantes 16 s depois do alívio anterior sair com a única loco rumo → `locos no wait=0` → alívio não iniciou e o terminal nunca voltou para a lista.
- **Alteração (GestorDeAlivios):** `fazer_procedimento_de_alivio_pst` verifica o wait `locomotivas<Ferrovia>` antes de colocar o terminal em alívio; sem loco, o terminal permanece na análise e o alívio dispara quando a loco voltar.
- **Arquivos:** `_alp/Agents/GestorDeAlivios/Code/Functions.java`
- **Observação de modelagem:** há 1 loco rumo para alívio; os últimos vazios esperam ela ir às DVs e voltar.

### ALT-2.3 — contador-locos-pelo-wait
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

### ALT-2.4 — cortar-helper-antes-dos-encostes
- **Nos comentários do código:** `ALT-005`
- **Data:** 2026-10-10
- **Status:** validada (VLI cortou o helper e reacoplou)
- **Problema:** trem VLI chega com loco na cabeça e helper na posição 36; com os desacoplamentos de quantidade fixa, o helper terminava dentro do `_loteDeCarregados_MoegaMeio` e ia para a descarga.
- **Solução:** cortar o helper na linha verde, logo após `liberar_faixas_circulacao` e antes do `deixar27vgs`:
  `semHelper` (SelectOutput) → `para_cortarHelper` → `cortarHelper` → `separarTraseira_helper` (deixa os carros atrás do helper parados) → `tirarHelper` (1 carro) → helper vai para `devolverHelper` (guardar_loco) → `reencostarTraseira` (ré até o início da `pst_silosMuro_a`, máx. 40 m — corrigido: o destino `pos_PontaDaPraia` dava "no straight route") → `reacoplarTraseira` (TrainCouple) → `deixar27vgs`.
- **Nova função:** `Trem.getCarrosAtrasDoHelper()` — nº de carros atrás da 1ª loco fora do bloco da cabeça; -1 se não houver.
- **Limitações:** trata 1 helper; helper como último carro (0 carros atrás) não é cortado aqui.
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`, `_alp/Agents/CoperXI/Connectors.xml`, `_alp/Agents/Trem/Code/Functions.xml`, `_alp/Agents/Trem/Code/Functions.java`

### ALT-2.5 — encostes-contam-vagoes
- **Nos comentários do código:** `ALT-006`
- **Data:** 2026-10-10
- **Status:** aplicada, aguardando validação
- **Problema:** após a ALT-005 o VLI (1 loco + 81 vgs) não devolvia a loco da cabeça e a descarga não começava. A sequência fixa 27+4+10+4+10+10+10+4+3 tira 82 carros; com 81 vagões o `tira3vgs` tentava levar a loco junto, o trem não chegava ao `mnbCarregados_liberar` e o `rp_manobrando_carregados` ficava preso.
- **Alteração (CoperXI):** `nCars` dos 12 blocos `tira*vgs` (encoste e rechego) passa de N fixo para `Math.min(N, ((Trem) train).getNumeroDeVagoes())`, com comentário dos casos Rumo/MRS (2 locos na cabeça + 81 vgs), VLI (1 loco + 81 vgs) e original (1 loco + 82 vgs).
- **Efeito:** com menos de 82 vagões o último lote (linha bomba) sai menor; as locos da cabeça sempre sobram para o `devolverLocomotiva`.
- **Arquivos:** `_alp/Agents/CoperXI/EmbeddedObjects.xml`

## Traces de diagnóstico ativos (remover após validar)
- CoperXI: `AouB1.onExitTrue/onExitFalse`, `retornar_linhaTerra_a1.onEnter`, `retornar_linhaTerra_b.onEnter`
- CoperXI (ALT-005): `semHelper.onExitFalse`, `reacoplarTraseira.onExit`
- GestorDeAlivios: `[diag_pst]` em `analisar_pst`, `fazer_procedimento_de_alivio_pst`, `chamar_locomotiva_de_alivio_pst`, `atualizar_status_alivio_terminal.onEnter`
