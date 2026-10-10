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
