# Terminal CoperXXI

> ALT-127 a ALT-130. Ficha iniciada com as regras de recebimento e alívio; o restante do terminal ainda não foi documentado aqui.

## Identificação

- Pátio: `pst` (`getPatio()`). Segmento: açúcar. Ferrovias: rumo, mrs, vli.
- Ferrovia e segmento em atendimento: lidos do primeiro vagão do trem carregado, na chegada (`comecar_encoste.onEnter` → `ferrovia_em_atendimento`, `produto_em_atendimento`).
- Alívio: 1 locomotiva da ferrovia em atendimento (`get_tipo_loco_alivio()` = "ferrovia").
- Linhas de vazios: lote b (22), lote a (13) e lote da moega (6). Total 41 (`QTD_ALIVIO`).
- Os traces antigos do terminal saem com o prefixo `[CoperXII]`.

## Regras

### Terminal só recebe trem depois de esvaziar

- `podeReceberNovoLote()` recusa trem enquanto houver alívio pedido ou em andamento (`aguardando_alivio || fazendoAlivio`), além de trem entrando (`_trem_chegando`) ou carregados no terminal.
- **Motivo:** a ferrovia e o segmento do terminal são analisados na chegada do carregado. Se o terminal ainda tem vazios de uma ferrovia (ex.: VLI) e chega um trem de outra (ex.: MRS), o terminal passa a atender a nova ferrovia e o alívio pede loco dela para levar os vazios da anterior. O modelo trava: o lote novo espera o alívio e o alívio espera uma loco que está presa nos lotes do trem novo.
- Efeito: o trem seguinte espera no ISN até os vazios do anterior saírem com a loco da ferrovia certa.

### Quando o alívio é pedido (`verificar_alivio`)

- Terminal cheio: b + a + moega = 41 vazios.
- Terminal sem carga (`get_vagoes_carregados() == 0`) e com pelo menos 1 vazio.
- No fim de cada alívio (`retornar_gestorDeAlivios`), a condição é reavaliada, encadeando alívios até zerar os vazios.
- O gatilho do GestorDeAlivios continua `getOverloadVazios() > 1` (vale 2 com `aguardando_alivio`).

### Descarga e alívio nunca ao mesmo tempo

- `tem_espaco_vazios` usa `pode_descarregar_grupo()`: só descarrega grupo se houver espaço e não houver alívio pedido nem em andamento.
- Motivo: os blocos `acoplar_a`, `acoplar_b` e `acoplar_moega` servem à descarga e ao alívio e escolhem o caminho por `etapa_alivio`. Com os dois ao mesmo tempo, um grupo da descarga foi tratado como trem do alívio.

### Sobra do alívio

- A loco engata b → a → moega e deixa a sobra peça a peça (moega → a → b).
- A peça da última linha engatada (`_ultima_linha_engatada`) é cortada no lugar: a cauda do trem é a cauda do próprio lote, que não se moveu.
