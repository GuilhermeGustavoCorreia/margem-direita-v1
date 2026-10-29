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

Controla as faixas de circulação e o licenciamento dos trens.

- **Faixas**: cada `Faixa` tem `estado` (LIVRE, OCUPADA, BLOQUEADA), `idTremLicenciado` e `recurso` (ResourcePool).
- **Entrada na circulação**: o trem chega pelo `chegada_carregados.take(agent)` e é licenciado até a entrada do terminal de destino.
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

## GestorDeAlivios

Decide qual terminal aliviar, ou seja, retirar os vazios.

- Só dispara quando o overload de vazios de um terminal passa de 1.
- Pega locomotivas da ferrovia em atendimento ou de manobra, conforme `get_tipo_loco_alivio()`.
- Só tira um terminal de `terminais_pst` se houver loco no wait da ferrovia.
  [verificar: regra completa do alívio]
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

## GestorDeDVs

Ainda não desenvolvido. Requisitos já definidos:

- Recebe o trem de alívio em `entrarNasDVs`.
- Precisa informar o espaço livre, em vagões, por ferrovia.
  O GestorDeAlivios usa esse valor antes de aliviar o Tgrao (precisa ser mais de 40).

## GestorDeChegadas

Ainda não desenvolvido.

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
