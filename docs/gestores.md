# Gestores

Cada gestor cuida de um recurso compartilhado. Terminais pedem ao gestor; o gestor não conhece a lógica interna do terminal.

> RASCUNHO. GestorDeAlivios e GestorDeChegadas ainda precisam de conferência no código.

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

Controla as faixas de circulação e o licenciamento dos trens.

- **Faixas**: cada `Faixa` tem `estado` (LIVRE, OCUPADA, BLOQUEADA), `idTremLicenciado` e `recurso` (ResourcePool).
- **Entrada na circulação**: o trem chega pelo `chegada_carregados.take(agent)` e é licenciado até a entrada do terminal de destino.
- **Regra de licenciamento**: o trem licencia só a faixa à frente (`faixa_atual` / `proxima_faixa`).
  Cada faixa tem `circular_*` → `ir_*` (TrainMoveTo) → Exit, que define o próximo par.
  A decisão de destino fica no fim da última faixa da rota.
- **Saída da Margem Direita**: o alívio devolve o trem ao GestorDeCirculacao, que licencia as faixas de saída
  (linha 6000 a, marginal c, silos muro a). Essas faixas são ocupadas com `ocupar()`, não bloqueadas,
  para que `liberar_faixas_ocupadas` as solte conforme o trem anda.
- **Liberar**: `liberar_faixas_ocupadas(Trem)` solta as faixas do trem que estão OCUPADA e vazias.
  Devolve os ResourcePools liberados para o Release do terminal.
- **Bloquear**: `bloquear_faixas(Trem, List<Faixa>)`.
- **Linhas e recursos**: `inicializar()` mapeia cada ResourcePool de linha à sua lista de trilhos.

## GestorDeAlivios

Decide qual terminal aliviar, ou seja, retirar os vazios.

- Só dispara quando o overload de vazios de um terminal passa de 1.
- Pega locomotivas da ferrovia em atendimento ou de manobra, conforme `get_tipo_loco_alivio()`.
- Só tira um terminal de `terminais_pst` se houver loco no wait da ferrovia.
  [verificar: regra completa do alívio]

## GestorDeChegadas e GestorDeDVs

Ainda não desenvolvidos.

## Como os gestores conversam com os terminais

| De → Para | Chamada | Quando |
|---|---|---|
| Terminal → GestorDeLocomotivas | `selecionar_rp_locomotiva_trecho`, `guardar_loco.take` | Locomotiva sobra ou precisa ser devolvida |
| Terminal → GestorDeCirculacao | `liberar_faixas_ocupadas((Trem) agent)` | Trem sai de uma faixa |
| GestorDeAlivios → Terminal | `getOverloadVazios()`, `takeAlivio(Trem)`, `get_pos_alivio()` | Alívio precisa começar |
| GestorDeAlivios → GestorDeLocomotivas | `pst_colocarLocomotivas_alivioTerminal(moega)` | Criar loco de manobra para o alívio |
