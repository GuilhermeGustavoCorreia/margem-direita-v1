# Terminal Tgrao

> RASCUNHO. Ferrovias e segmento ainda precisam de confirmação.

## Identificação

- Pátio: `pst` (`getPatio()`).
- Ferrovia e segmento: vêm do primeiro vagão do trem em atendimento (`getFerrovia()`, `getSegmento()`).
- Função de alívio: usa 2 locomotivas de manobra (`getNumeroLocomotivasAlivio()`), criadas em `pos_locoManobra_PST`.
  Não são locomotivas da ferrovia.
- Convenção dos documentos: carro de índice 0 aponta para a Ponta da Praia; o último, para o Valongo.

## Regras do alívio

- O alívio do Tgrao é sempre de 40 vagões. Nunca menos.
- Antes de aliviar, o GestorDeDVs precisa ter mais de 40 vagões de espaço livre para a ferrovia do Tgrao.
  Sem esse espaço, o alívio não começa.
- [pendente] As duas regras ainda não estão no código. O GestorDeDVs ainda não foi desenvolvido.

## Recursos

- Linhas 7000 a, b, c, d: `rp_linha7000` com Seize (`ocupar_7000_abcd`, `ocupar_7000_abcd_alivio`, `ocupar_7000_abcd_locoB`) e Release (`liberar_7000_*`, `passar_7000_abcd_vazios`).
- Saída do alívio: `ocupar_saida_alivio` e `liberar_7000_abcd_alivio`.
- Faixas de circulação: a faixa de criação, marginal_a, é apreendida pelo GestorDeCirculacao.
  As faixas do caminho da loco de manobra até o Tgrao são `primeiro_alivio_faixas`.

## Ciclo no terminal

Trem de carregados (sequência a partir do GestorDeCirculacao):

1. `colocarLinhaMar` (Enter) → `liberar_faixas_circulacao` (Release) → `encostar_linhaMar` (TrainMoveTo).
2. `descarrega_linhaMar` (Enter) → `espera_loteMoega` (Hold) → `descarrega_loteMar` (DescarregaLote).
3. `tirarLote` (TrainDecouple) → `liberar_7000_d` → `encostar_linhaMar`, para o próximo lote.
4. Moega: `encostar_Moega` → `colocarNaEntradaMoega` → `descarregar_linhaMoega` → `descarrega_loteMoega`.
5. `acabouLotes` (SelectOutput) decide se o terminal encerrou. Se não, `tirarExcesso2` → `manobrarLote`.

Alívio de vazios (locomotivas de manobra, sempre 40 vagões):

1. `paraAlivioMar` ou `paraAlivioMoega` → `temLoteVzs_linhaMar` / `temLoteVzs_linhaMoega` (SelectOutput).
2. `alivioMar` / `alivioMoega` (Enter) → `engatarLoteMar` / `engatarLoteMoega` (TrainMoveTo) → `acoplar` / `acoplar1` (TrainCouple).
3. `voltarPosInicial2` (MoverTremCompleto_Ponto) → `separarLocomotivas` (TrainDecouple) → `entrarNaLinhaDoTerminal` (TrainMoveTo) → `alivioVazios` (Enter) → `iniciar_alivio` (Enter).
4. Saída: `paraSairDoTerminal` → `liberar_7000_abcd_alivio` → `voltarPosInicial3` → `ocupar_saida_alivio` → `trainCouple` → `trainMoveTo` → `ocupar_7000_abcd_locoB` → `aguardaLote7K`.
   O `ocupar_saida_alivio` ocupa as faixas linha 6000 a, marginal c e silos muro a no GestorDeCirculacao.
   Com isso o trem volta à circulação para sair da Margem Direita.

Locomotivas no Tgrao:

- `retirarLocomotiva_a` e `linha7k`: a loco de manobra é retirada para a linha 7000.
- `devolverLocomotiva` e `devolverLocomotiva1` (blocos do agente DevolverLocomotiva):
  devolvem a loco ao GestorDeLocomotivas, com `tirarLote2` / `trainDecouple`.
  [pendência: ver abaixo]

## Pendências

- [pendente] Alívio de exatamente 40 vagões e checagem de espaço no GestorDeDVs (> 40 vagões para a ferrovia do Tgrao).
- [pendente] Após `entrarNaLinhaDoTerminal`, as locomotivas devem liberar as faixas de circulação.
  Hoje o fluxo vai direto para `alivioVazios`, sem nenhum Release de faixa nesse caminho.
  O único `liberar_faixas_circulacao` do Tgrao fica no ramo da linha mar.
- [pendente] `devolverLocomotiva1` não devolve a loco ao GestorDeLocomotivas. O saldo do pátio não é incrementado.
  Ver `docs/gestores.md`, seção GestorDeLocomotivas.
- [pendente] Confirmar se a loco separada mantém o id do trem original.
  Isso decide se `liberar_faixas_ocupadas` encontra as faixas dela.
