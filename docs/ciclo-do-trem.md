# Ciclo do trem

Visão geral de por onde passa um trem de carregados no modelo.
Detalhes de cada terminal: `docs/terminais/<nome>.md`. Papel de cada gestor: `docs/gestores.md`.

> RASCUNHO. Etapas marcadas com [verificar] ainda não foram conferidas no código.

Convenção dos documentos: carro de índice 0 aponta para a Ponta da Praia; o último, para o Valongo.

## Etapas

1. **Chegada**: o trem entra na Margem Direita e é entregue ao GestorDeCirculacao
   (`chegada_carregados.take(agent)`, no Exit `paraGestorCirculacao_chegadaCarregados`).
   O GestorDeCirculacao licencia o trem faixa a faixa até a entrada do terminal de destino.
   [verificar: papel do GestorDeChegadas, se existir alguma parte dele nesta etapa]
2. **Escolha do terminal**: o trem vai para o terminal de destino, conforme a ferrovia e o produto.
   A partir da entrada do terminal, o licenciamento pelo GestorDeCirculacao termina.
   Bracell: `val_via2 → via3_intermar → via3_a → via3_b → via3_c → pst_silosMuro_a → pst_marginal_c → pmc_intervalo4000_a/b/c → linha 5000`.
3. **Encoste**: o trem é posicionado na linha de descarga (linha mar ou entrada da moega).
   Uma locomotiva pode ser separada aqui, se sobrar (regra: volta ao GestorDeLocomotivas).
4. **Descarga**: os vagões carregados são descarregados. O lote de carregados fica null ao final.
5. **Vazios**: os vagões já descarregados formam lotes de vazios nas linhas de alívio.
6. **Alívio**: quando o overload de vazios passa de 1, o GestorDeAlivios escolhe um terminal e
   envia uma locomotiva de manobra para retirar os vazios. O lote de vazios fica null ao final.
   No Tgrao, o alívio é sempre de 40 vagões e só começa se o GestorDeDVs tiver mais de 40 vagões de espaço para a ferrovia.
   Celulose PCX (Bracell, Suzano, Eldorado): alívio separado, sem linha 4000 e sem depender das DVs; o trem sai com todos os vazios (Bracell: 64).
   A linha 4000 acumula só T39, TES e ADMO; com 82 vagões vai para as DVs pelo circuito do GestorDeCirculacao.
7. **Saída**: o trem volta para o GestorDeCirculacao para sair da Margem Direita.
   No Tgrao, a saída ocupa as faixas linha 6000 a, marginal c e silos muro a
   (`ocupar_saida_alivio`), que são liberadas conforme o trem anda.
   A locomotiva de alívio volta ao GestorDeLocomotivas.

## Pontos de decisão

- `getOverloadVazios()`: 1.1 quando vazios >= capacidade (mar + terra). Acima de 1 dispara alívio.
- `temLocomotiva` / `precisaRemoverLoco`: decide se a locomotiva precisa sair do trem.
- `aindaCabeNa_linhaMar`: decide se o trem cabe na linha mar ou vai para a moega.
- `acabouLotes`: encerra o terminal quando não há mais lotes a descarregar.

## Recursos compartilhados

- Locomotivas: GestorDeLocomotivas (pools por pátio: pst, pmc, pcx; e por ferrovia: rp_locoRumo, rp_locoMRS, rp_locoVLI).
- Faixas de circulação: GestorDeCirculacao (cada faixa tem estado e o trem que a licencia).
- Linhas 7000 (a, b, c, d): ResourcePool rp_linha7000, usado com Seize/Release nos terminais.
- Vazios e alívio: GestorDeAlivios.
- Espaço nas DVs: GestorDeDVs (a desenvolver).

## Pendências conhecidas

- [pendente, sem ALT ainda] Após `entrarNaLinhaDoTerminal`, as locomotivas não liberam as faixas de circulação (Tgrao).
- [pendente, sem ALT ainda] A locomotiva devolvida pelo DevolverLocomotiva não incrementa o saldo do pátio.
- [pendente, sem ALT ainda] Tgrao: alívio de 40 vagões e checagem de espaço no GestorDeDVs.
- [pendente] Bracell: falta `pode_receber_novo_lote()`. Ver `docs/terminais/bracell.md`.
