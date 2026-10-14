# Terminal Bracell

> ALT-014. Alterações aplicadas, aguardando compilação e validação.

## Identificação

- Pátio: `pcx` (`getPatio()`). Segmento: celulose.
- Terminais de celulose do PCX: Bracell, Suzano e Eldorado (Suzano e Eldorado ainda vão ser criados).
- Ferrovia e segmento: vêm do primeiro vagão (`atualizarFerroviaEProdutoEmAtendimento`, chamado no `encoste`).
- Alívio: 1 locomotiva da ferrovia em atendimento (`get_tipo_loco_alivio()` = "ferrovia").
- Ponto de chegada da loco de alívio: `main()._bracell_alivio`, na linha 5000_b (`get_pos_alivio()`).
- Convenção dos documentos: carro de índice 0 aponta para a Ponta da Praia; o último, para o Valongo.

## Regras

- A Bracell não alivia pela linha 4000. A linha 4000 é só para T39, TES e ADMO.
- O trem sai com todos os vagões que chegaram. Na Bracell, sai com os 64 descarregados, como o Tgrao, que chega com 40 e sai com 40.
- O alívio não depende das DVs. O GestorDeAlivios só verifica se o overload passou de 1 (lógica separada: `analisar_celulose`).

## Linhas e recursos

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

Alívio (overload > 1 quando vazios ≥ 64):

1. GestorDeAlivios (`analisar_celulose`): pega 1 loco da ferrovia no wait do GestorDeLocomotivas →
   `loco_alivio_celulose` → `ocupar_linha5000_6000` → `trainEnter_celulose` (`circ_pos_inicio_L5K`) → `ir_alivio_celulose` (`_bracell_alivio`) → `takeAlivio`.
2. Bracell: `iniciar_alivio` → `irAteTerminal` → `pausar1` → `paraAlivio_L1`.
3. L1 a L4: `alivio_L*` → `colocar_L*` → `trainCouple*` → `recuar4..7`. Cada `recuar` zera o `loteVazios_L*` acoplado.
4. `para_circVzs`: marca `eh_alivio`, devolve a Bracell para `terminais_celulose` e entrega o trem ao GestorDeCirculacao na `intervalo4000_c`.

## Terminais testados

- Nenhum ainda. ALT-014 aguardando validação.

## Pendências

- [pendente] `pode_receber_novo_lote()`: hoje `podeReceberNovoLote()` sempre devolve true.
  Um 2º trem de carregados pode ser despachado durante o alívio e travar na intervalo4000 (ele espera a linha 5000, e o alívio espera a intervalo4000 para sair).
- [verificar] Sentido da loco no `trainEnter_celulose` / `ir_alivio_celulose`. Se der "no straight route", ajustar `forward`.
- [verificar] Se a cauda do trem de carregados ainda estiver na intervalo4000_c no `liberar_faixas_circulacao`, a faixa é solta no `liberar_faixas_circulacao1`.
- [verificar] `pausar1` está no caminho do alívio. Confirmar se ele pausa a simulação.
- Blocos antigos do GestorDeCirculacao (`alivio_bracell*`, `linha5K*`, `linha_5k_*`, `circulacaoVzs_PCX`) não são mais usados pela Bracell.
