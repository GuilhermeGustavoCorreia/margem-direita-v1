# MargemDireitaModel1 — Correções CoperXI

Detalhes de cada item no [`CHANGELOG.md`](CHANGELOG.md).

- **ALT-001**: removidos os agentes `solicitacao_alivio` e `solicitacao_faixas`, que não eram usados.
- **ALT-002**: descarga pausa durante o alívio (`fazendoAlivio`). Isso corrige a colisão na linha terra_a.
- **ALT-003**: o gestor de alívios só tira o terminal da fila se houver locomotiva disponível. Isso corrige o alívio que não acontecia no 2º trem.
- **ALT-004**: o contador de locomotivas passa a usar o wait (`locomotivas*.size()`).
- **ALT-005**: o helper da VLI é cortado na linha verde, antes dos encostes.
- **ALT-006**: os encostes (`tira*vgs`) contam só vagões e não levam mais a locomotiva da cabeça.
