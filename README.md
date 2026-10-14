# MargemDireitaModel1

Detalhes no [`CHANGELOG.md`](CHANGELOG.md).

- **ALT-001**: removidos os agentes `solicitacao_alivio` e `solicitacao_faixas`, que não eram usados.
- **ALT-002**: correções do CoperXI
  - **002.1**: descarga pausa durante o alívio (`fazendoAlivio`). Isso corrige a colisão na linha terra_a.
  - **002.2**: o gestor de alívios só tira o terminal da fila se houver locomotiva disponível. Isso corrige o alívio que não acontecia no 2º trem.
  - **002.3**: o contador de locomotivas passa a usar o wait (`locomotivas*.size()`).
  - **002.4**: o helper da VLI é cortado na linha verde, antes dos encostes.
  - **002.5**: os encostes (`tira*vgs`) contam só vagões e não levam mais a locomotiva da cabeça.
- **ALT-003**: correções do GestorDeCirculacao
  - **003.1**: rota do CoperXXI (via3_intermar → via3_a → via2_c → track_entrada_pera), com nova faixa na entrada da pêra.
  - **003.2**: rota do T39 (… → via3_a → via3_b → via3_c → silosMuro a/b/c → encoste_linha1000), licenciando uma faixa à frente.
  - **003.3**: decisões como no documento: fim da via3_a "é coperXI ou pêra?", fim da via2_c "é pêra?".
  - **003.4**: rota do Tgrao (… → via3_c → silosMuro_a → marginal_c → comecar_encoste).
- **ALT-004** (em andamento): MoegaoMar + pendências da ALT-002 (CoperXI)
- **ALT-005**: Tgrao — loco volta ao GestorDeLocomotivas, lotes de carregados zerados após a descarga, overload conta os vazios
- **ALT-006**: gestor de alívios — tipo e quantidade de locos por terminal (Tgrao: 2 locos de manobra em `pos_locoManobra_PST`), seize da faixa de criação, ◇ `ehLocoManobra`
- **ALT-007**: Tgrao — alívio libera a loco B para acoplar nos vazios (`aguardaPrimeiraManobra.unblock()`)
- **ALT-008**: faixas 7000_a..d no GestorDeCirculacao; Tgrao ocupa/libera essas faixas no encoste e no alívio; `voltarPosInicial3` sai para o lado Valongo
- **ALT-009**: Tgrao — alívio ocupa a 7000_a..d e, na saída, 6000_a/marginal_c/silosMuro_a (faixa nova 6000_a); `voltarPosInicial3` vai até `pos_saida_tgrao`
- **ALT-010** (diagnóstico): traces `[diag_liberar]` em `liberar_faixas_ocupadas` para achar o pool liberado sem posse
- **ALT-011**: console espelhado em `logs/sim.log` (função `configurar_log_em_arquivo` no On startup do Main)
- **ALT-012** (temporária): trace "bom dia Bruno" ao entrar no Tgrao (`colocarLinhaMar`)
- **ALT-013**: alívio na linha 4000 — com 82 vagões o trem do PCX vai para as DVs pelo circuito do GestorDeCirculacao (intervalo4000_a → marginal c/b/a → via2 → val_via2)
- **ALT-014**: Bracell — chegada pela circulação (val_via2 → … → intervalo4000_c), loco volta ao GestorDeLocomotivas, alívio de celulose separado (sem linha 4000 e sem DVs), saída de ré pela intervalo4000 até as DVs
