# Terminal Bracell

> ALT-014. Alterações aplicadas, aguardando compilação e validação.

## Identificação

- Pátio: `pcx` (`getPatio()`). Segmento: celulose.
- Terminais de celulose do PCX: Bracell, Suzano e Eldorado (Suzano e Eldorado ainda vão ser criados).
- Ferrovia e segmento: vêm do primeiro vagão (`atualizarFerroviaEProdutoEmAtendimento`, chamado no `encoste`).
- Alívio: 1 locomotiva da ferrovia em atendimento (`get_tipo_loco_alivio()` = "ferrovia").
- Ponto de chegada da loco de alívio: `main()._bracell_alivio`, na linha 5000_b (`get_pos_alivio()`).
- Convenção dos documentos: carro de índice 0 aponta para a Ponta da Praia; o último, para o Valongo.

## Recebimento de trem

`podeReceberNovoLote()` recusa o trem enquanto:
- há um trem entrando (`_trem_chegando`): do `encoste` até o 1º lote de vazios (`loteVazios_L*`);
- há carregados no terminal (`_getVagoesCarregados()` > 0);
- há vazios no terminal (`_getVagoesVazios()` > 0), esperando ou em alívio.

O terminal recebe um trem por vez: o próximo só entra depois do alívio.
Os `lote_carregados_L*` nunca são atribuídos no fluxograma; quem cobre a descarga é o `_trem_chegando`.

## Regras

- A Bracell não alivia pela linha 4000. A linha 4000 é só para T39, TES e ADMO.
- O trem sai com todos os vagões que chegaram. Na Bracell, sai com os 64 descarregados, como o Tgrao, que chega com 40 e sai com 40.
- O alívio não depende das DVs. O GestorDeAlivios só verifica se o overload passou de 1 (lógica separada: `analisar_celulose`).

## Linhas e recursos

- `rp_mnb_pcx` (ALT-142): o trem pega na circulação antes da `silosMuro_a` e a loco solta no `liberar_linha5000_6000`; a loco do alívio pega no GestorDeAlivios e solta ao sair do terminal.
- 4 linhas de descarga: L1, L2, L3, L4. Cada uma recebe um lote de 16 vagões (`deixarLote*`, nCars = 16).
- A Bracell fica depois da linha 5000: `intervalo4000_c → linha5000_a → linha5000_b → … → linha 6000/7000_h → _bracell_L12 / _bracell_L34`.
- Recurso de linha: `rp_linha5000` e `rp_linha6000` (GestorDeCirculacao).
  - Trem de carregados: ocupa no `encoste` (`ocupar_linha5000_6000`) e libera depois do `recuar3` (`liberar_linha5000_6000`).
  - Loco de alívio: ocupa no GestorDeAlivios, antes de entrar na linha 5000, e libera no GestorDeCirculacao, no `recuar_intervalo4000_b`.
- Lotes: `lote_carregados_L1..L4` e `loteVazios_L1..L4`.

## Percurso na circulação

Chegada (licenciada faixa a faixa pelo GestorDeCirculacao):

`val_via2 → via3_intermar → via3_a → via3_b → via3_c → pst_silosMuro_a → pst_marginal_c → pmc_intervalo4000_a → pmc_intervalo4000_b → pmc_intervalo4000_c → bracell.encoste`

Saída do alívio (de ré, `eh_alivio = true`):

`pmc_intervalo4000_c → b → a → pst_marginal_c → b → a → via2_f … via2_a → via2_intermar_c … a → railwayTrack199 → val_via2 → gestorDeDVs.entrarNasDVs`

## Ciclo no terminal

Trem de carregados (formação de teste `trem_celulose_teste`: 1 loco Rumo + 64 vagões):

1. `encoste` → `ocupar_linha5000_6000` → `entrarNoTerminal` (`_bracell_encoste`) → `liberar_faixas_circulacao`.
2. Loco para a cauda: `colocarLocoNaCauda` → `removerLoco` → `trainExit` → `delay` → `trainEnter` → `trainMoveTo` → `acoplarLoco`.
3. Encostes, um lote de 16 por linha:
   - L1: `encostar_L1` → `colocar_L1` → `liberar_faixas_circulacao1` → `deixarLote` → `recuar`
   - L2: `encostar_L2` → `colocar_L2` → `deixarLote1` → `recuar1`
   - L3: `encostar_L3` → `colocar_L3` → `deixarLote2` → `recuar2`
   - L4: `encostar_L4` → `colocar_L4` → `deixarLote3` → `recuar3` → `liberar_linha5000_6000` → `para_guardar_loco` (GestorDeLocomotivas)
4. Descarga: L1 a L3 esperam nos Holds `aguarda_L4a/b/c`. `descarregar_L4` desbloqueia os três e as 4 linhas descarregam juntas.
5. Cada lote vira `loteVazios_L*` e espera em `aguardarAliviar_L*`.

Alívio (overload > 1 quando vazios ≥ 64), pelo caminho do PMC no GestorDeAlivios:

1. GestorDeAlivios: `alivio_pmc` → `ocupar_mnb_pcx_pmc` → `bloquear_faixas_pmc` (pega as faixas de `get_faixas_primeiro_alivio()`) →
   `trainEnter_pmc` (`pos_locoAlivio_PMC`, na `silosMuro_a`) → `ir_alivio_pmc` (`_bracell_alivio`) → `para_alivio_terminal_pmc`.
   - Faixas do caminho da loco: `silosMuro_a → marginal_c → intervalo4000_a → intervalo4000_b → intervalo4000_c`.
     Ficam com a loco (id 9999) durante o trajeto, então a 4000 não sai nem faz a manobra do excedente nesse período.
2. Bracell: `iniciar_alivio` → `liberar_faixas_loco_alivio` (solta os pools dessas faixas e volta as faixas para LIVRE) → `irAteTerminal` → `pausar1` → `paraAlivio_L1`.
3. L1 a L4: `alivio_L*` → `colocar_L*` → `trainCouple*` → `recuar4..7`. Cada `recuar` zera o `loteVazios_L*` acoplado.
4. `para_circVzs`: marca `eh_alivio`, devolve a Bracell para `terminais_celulose` e entrega o trem ao GestorDeCirculacao na `intervalo4000_c`.

## Terminais testados

- Nenhum ainda. ALT-014 aguardando validação.

## Pendências

- [pendente] `pode_receber_novo_lote()`: hoje `podeReceberNovoLote()` sempre devolve true.
  Um 2º trem de carregados pode ser despachado durante o alívio e travar na intervalo4000 (ele espera a linha 5000, e o alívio espera a intervalo4000 para sair).
- [verificar] Se a cauda do trem de carregados ainda estiver na intervalo4000_c no `liberar_faixas_circulacao`, a faixa é solta no `liberar_faixas_circulacao1`.
- [verificar] `pausar1` está no caminho do alívio. Confirmar se ele pausa a simulação.
- Blocos antigos do GestorDeCirculacao (`alivio_bracell*`, `linha5K*`, `linha_5k_*`, `circulacaoVzs_PCX`) não são mais usados pela Bracell.
