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
- **ALT-015**: via3 nova (via3_c) entre via3_b e via3_d (antiga via3_c); CoperXI chega pela via3_b → via2_e
- **ALT-016**: rota da CostaLeite na circulação (via3_b → via2_e → comecar_encoste)
- **ALT-017**: CostaLeite — encostes da moega: lotes de 6/14/11/11/10 em moegaMeio, linhaTerra, lateralMoega, linhaMar_a e linhaMar_b (`lote_costaLeite_*`), loco devolvida; trem de teste com 1 loco
- **ALT-018**: CostaLeite — `tipoDeLote` em cada lote e `_prioridadeDeLotesDescarga` = linha-terra, moega-meio, lateral-moega, linha-mar-a, linha-mar-b
- **ALT-019**: CostaLeite — `desacoplar_Nvgs_*`, movimentos `mov_*` com Exit/Enter entre os passos; descarga começa ao devolver a loco (`descarga_moega` → `descarregar_lote` → `linhaMar`); nomes do CoperXI corrigidos para compilar
- **ALT-020**: CostaLeite — descarga vagão a vagão como no CoperXI (some na moega, reaparece vazio na linha de vazios); gatilho no `desacoplar_10vgs`
- **ALT-021**: CostaLeite — vazios vão para a linha mar, meio ou terra (a primeira que couber), com os pontos `_costaLeite_vazios_*`; linha terra adaptada da cópia da meio
- **ALT-022**: CostaLeite — alívio das linhas mar → meio → terra (bloco sai do `wait_bloco*` para o acoplar), excedente volta para a linha, `deixarTerminal` devolve ao GestorDeAlivios; contagens de vazios/carregados
- **ALT-023**: Suzano — base ITerminalMoega (cópia da ADM), trem de teste e rota na circulação (… → silosMuro_a → marginal_c → linha6000_a → comecar_encoste)
- **ALT-024**: ADM — descarga das moegas X e V vagão a vagão revezando o `mnb_vazios`, formando o `lote_linha_7` (máx. `CAPACIDADE_LINHA_7` = 30)
- **ALT-025**: ADM — alívio da linha 7 pela linha 4000 como no T39 (dispara com 30, linha 7 com 35), descarga e manobra param durante o alívio; GestorDeAlivios: ADM em `terminais_pcx`, um alívio da 4000 por vez
- **ALT-027**: ADM — recurso `mnb_alivio`: vagão entrando na linha 7 e alívio não acontecem juntos; trem do alívio sai da linha 7 inteiro (`afastar_linha7`) antes de liberar
- **ALT-028**: ADM — manobra termina quando sobram só as locos (devolve as locos e começa a descarga); traces `[ADM][decidir_moega]` e `[ADM][fim_manobra]`
- **ALT-029**: ADM — trem do alívio sai da linha 7 em passos de ré até não ter carro na linha 7 (corrige o 2º alívio com o trem da 4000)
- **ALT-031**: ADM — excesso do alívio desconta os vagões que o trem já traz (como no T39); capacidade da linha 7 conta vagões a caminho e trava durante o alívio
- **ALT-032**: ADM — correções da descarga nova com `rp_moegaX/V` (condição, moega V, soltar o wait, último vagão, capacidade da linha 7)
- **ALT-033**: ADM — `vagoes_entrando_linha7` gravado na saída do `sz_mnb_alivio_l7` (o excesso na fila tinha o valor sobrescrito)
- **ALT-034**: GestorDeAlivios — `aux_esta_ocorrendo_alivio_pcx = false` quando o trem cheio sai da 4000 (alívios da 4000 paravam depois do 1º trem)
- **ALT-035**: TES — Rumo fraciona 41 vagões na 3000 e distribui 24 na linha 03, 12 na 04 e o resto na 05, recuando até `_tes_trocar_via` entre as linhas; licenciamento da 3000 corrigido
- **ALT-036**: TES — descarga da linha 03: lote vai à `_tes_moega_linha03`, descarrega 2 e avança 2 vagões até não caber 1 antes de `_tes_linha03_fim` (`talhou_linha03 = true`)
- **ALT-037**: TES — descarga vagão a vagão das linhas 05 e 04 na moega 04; vazios vão para o fundo da 05 e depois para o fundo da 04 (com a linha 04 vazia)
- **ALT-038**: TES — descarga 04/05 com o lote parado no wait da linha (só o vagão vai à moega), `rp_mnb_linha04e5` na manobra da 04/05, correções e limpeza de blocos/funções/variáveis sem uso
- **ALT-039**: TES — descarga 04/05 também começa quando o lote entra no wait (a loco liberava o recurso antes do lote parar)
- **ALT-040**: TES — `iniciar_descarga_moegas()` no fim da manobra (delay de 0,1 s, como na ADM) solta a descarga 04/05; lotes esperam nos wait
- **ALT-041**: TES — próximo vagão da moega 04 não espera mais o pool aparecer livre no mesmo instante do release (só o 1º vagão descarregava)
- **ALT-042**: TES — alívio no padrão do T39 (linha 03 → fundo 05 → fundo 04), `getOverloadVazios` com `QUANTIDADE_PARA_ALIVIAR` = 20, TES nos `terminais_pcx`
- **ALT-043**: TES — fundos soltos com `condicao_ficar_fundo0X` + `liberar_wait_lote_fundo0X()`, como no T39
- **ALT-044**: TES — linha 03 (e moega 04) voltam a descarregar quando o alívio termina (`retomar_descargas`)
- **ALT-045**: TES — 2 locos FIPS PCX na cauda do lote que ficou na 3000, depois do encoste da linha 05 (padrão ADM)
- **ALT-046**: TES — resto da 3000 faz o mesmo encoste quando as linhas esvaziam; locos FIPS devolvidas depois da linha 05
- **ALT-047**: TES — manobra ocupa as faixas 3000_b..e até o encoste da linha 05 (colisão no railwaySwitch103 com o trem de alívio); carregados em manobra entram no overload
- **ALT-050**: TES — fundo 05 só entra no alívio com a linha 05 vazia (`lote_linha_5 == null`)
- **ALT-051**: TES — `lote_linha_5/4` só ficam null depois que o último vagão descarrega; ALT-049 removida
- **ALT-057**: Suzano — 40 vagões em L1..L4 (10 cada, recuando até `_suzano_entrada_terminal` entre as linhas), 24 vagões ficam na L7000, loco da ferrovia devolvida, descarga das 4 linhas juntas como na Bracell; trem de teste com 64 vagões
- **ALT-058**: Suzano — `getOverloadVazios()` = 2,0 quando `descarregou_tudo` (fim da troca dos 24); até lá 0
- **ALT-059**: Suzano — troca dos 24: loco FIPS chamada quando as 4 linhas descarregam, recolhe os vazios de L1..L3, deixa 30 na L7000, distribui 10/10/4 em L1..L3; descarga junta liga o overload; loco FIPS devolvida
- **ALT-060**: Suzano — troca deixa só 24 vazios na L7000 (os 30 bloqueavam o railwaySwitch55); L1 recebe 6 vazios + 4 carregados, L2 e L3 10 carregados
- **ALT-061**: alívio do PMC (Bracell; Suzano e Eldorado depois) — `analisar_pmc`, trava `terminal_alivio_pmc`, saída centralizada em `retornar_alivio_pmc`
- **ALT-062**: Suzano — alívio PMC: loco em `pos_locoAlivio_PMC` engata L1..L4 e L7000 e sai pela linha6000_a → marginal_c (`retornar_alivio_pmc`); Suzano em `terminais_pmc`
- **ALT-063**: alívio PMC — loco nasce em `pos_locoAlivio_PMC` e licencia as faixas do terminal (`bloquear_faixas_pmc`); Suzano para em `_suzano_alivio` com silosMuro_a/marginal_c/linha6000_a e solta tudo no `liberar_recursos_loco_alivio`
- **ALT-064**: Suzano — `liberar_faixas_circulacao` volta para depois do `mov_avnc__L7000`; loco espera os 40 saírem da linha6000_a e solta a faixa antes de devolver; uma loco FIPS só
- **ALT-065**: CoperXII — corta o trem antes da 2ª loco; cada lote passa na moega de 6 em 6 e os grupos vão para `_coperXXI_lote_b` (21) e depois `_coperXXI_lote_a` (20); loco devolvida ao chegar
- **ALT-066**: CoperXII — corrige StackOverflow com linhas cheias; resto do lote descarrega na moega e fica como vazio aguardando alívio
- **ALT-067**: CoperXII — quebra o ciclo SelectOutput ↔ Hold da espera por espaço (StackOverflow)
- **ALT-068**: CoperXII — rota da loco do alívio passa pela linhaPaqueta_a e linhaCostaLeita_a (sem licenciar); loco para em `_coperXXI_lote_b`
- **ALT-069**: CoperXII — loco do alívio engata lote_b, lote_a e moega pelos couples dos vazios e volta ao GestorDeAlivios; próximo lote começa quando ela sai da linha
- **ALT-070**: CoperXII — lote 2 guardado em `segundo_lote`; começa ao fim do lote 1 (se sobrou espaço) ou no `retornar_gestorDeAlivios`
- **ALT-071**: CoperXII — corrige o lote 2 que não começava; alívio no fim de todo lote
- **ALT-072**: CoperXII — remove blocos, funções, eventos, variáveis e parâmetros do fluxo antigo
- **ALT-073**: CoperXII — lote só anda com loco: pega a da segunda parte ou uma FIPS PST; `lote_em_descarga` / `lote_segunda_parte`
- **ALT-074**: CoperXII — rota da loco FIPS PST por lista de linhas até a cauda do lote; licencia e solta as faixas do caminho
- **ALT-075**: CoperXII — loco FIPS nasce em `pos_loco_pera`; rota começa na via2_c e solta só as faixas que bloqueou
- **ALT-076**: CoperXII — todo alívio com 41 (22 + 13 + 6 na moega); MRS e VLI completam o 1º com parte do 2º lote
- **ALT-077**: CoperXII — contador de vazios da tela conta direto lote_b + lote_a (mostrava -6)

- **ALT-078**: CoperXII — fluxograma reorganizado em 5 colunas com pares Exit/Enter e painel de variáveis/funções (sem mudar a lógica)

- **ALT-079**: MoegaoMar — lógica do CoperXII com posições, rota FIPS e alívio próprios; pera decide entre CoperXXI e MoegaoMar; entra em terminais_pst

- **ALT-080**: MoegaoTerra — lógica de terminal da pera (cópia do MoegaoMar) com posições, rota FIPS e alívio próprios; pera decide também pelo MoegaoTerra

- **ALT-081**: GestorDeAlivios — trem cheio da linha 4000 ocupa as faixas da saída até a cauda sair; nova loco PCX só nasce com a intervalo4000_b livre

- **ALT-082**: GestorDeAlivios — trem da linha 4000 só junta vazios de terminais da mesma ferrovia e segmento (variáveis gravadas no 1º terminal; equals)

- **ALT-083**: Main — lê os arquivos da interface HTML (`simulacao/config` e `simulacao/input`) no startup e valida terminais e tempos entre chegadas; só log, sem efeito na simulação

- **ALT-084**: GestorDeISN — pátio ISN com faixas próprias (linha 01 a..e e porteira), estacionamento na linha 02 com `estacionarISN` e entrega à circulação na val_via2; GestorDeChegadas decide o destino; terminais entram por `receber_trem`

- **ALT-085**: GestorDeChegadas — prefixo do trem (letra + 2 dígitos pares) sorteado por ferrovia × segmento na criação; traces `[chegadas]` e `[ISN]` mostram `id/prefixo`

- **ALT-086**: GestorDeCirculacao — log `[ges-circulacao][tempo] - [prefixo: segmento destino] [status]` na entrada do gestor, em cada TrainMoveTo de faixa e na saída para terminal/DV

- **ALT-087**: classe `Log` padrão (`[tempo em h] NÍVEL Origem evento mensagem`); gestores (Circulacao, ISN, Chegadas, Alivios, Locomotivas) e terminais ADM, TES e T39 convertidos (demais pendentes); referências ALT removidas dos comentários desses oito

- **ALT-088**: CoperXI — vagão separado pelo `pega1vagao` passa a ser contado (`_vagao_aguardando_descarga`) em `_getVagosLinhaMoega()`; `get_vagoes_carregados()` soma o vagão em descarga; só um vagão fracionado por vez
- **ALT-090**: GestorDeCirculacao — log da circulação mostra `(prefixo - id)`
- **ALT-091**: GestorDeAlivios — log do id da loco do alívio PST `(prefixo - id)`
- **ALT-092**: GestorDeAlivios — faixa de criação da loco PST OCUPADA com o id da loco e liberada pela circulação; prefixo MNB ímpar nos trens de alívio
- **ALT-093**: GestorDeCirculacao/Chegadas/ISN — collection `trens_em_circulacao`: um trem a caminho de cada terminal por vez
- **ALT-094**: CoperXI — `_trem_chegando` + Delay `finalizar_manobra` (não recebe outro trem durante a manobra de chegada); Chegadas respeita o trem da linha 02
- **ALT-095**: GestorDeDVs — `quantos_vagoes_cabem` pela regra falta/vazios/livre (0 quando não pode); GestorDeAlivios não alivia com 0
- **ALT-096**: GestorDeDVs — logs `comecou_manobra`, `colocou_lote`, `terminou_manobra` com dados do trem; função `colocar_lote(linha, lote)`
- **ALT-097**: GestorDeDVs — estacionar lotes, formar o trem de 82 e sair por `circ_pos_fim_placa_2` (log `saida_82`); slot da via 2 fora (livre 108)
- **ALT-098**: MoverTremCompleto_Ponto — `_distanciaResidual` acha o carro também pela traseira (switch); trem parado sem alvo não dá mais erro
- **ALT-099**: GestorDeDVs — condição do `sel_acao` usa a função pura `calcular_linha`; `escolher_linha` roda uma vez no `onEnter` do `sel_acao` (fim do `proxima_linha` duplicado)
- **ALT-100**: CoperXI — primeiro vagão de linha de vazios vazia passa pelo `wait_bloco*` e já conta em `_loteDeVazios_*` (último vagão do trem não fica mais parado)
- **ALT-101**: GestorDeCirculacao — alívio pela via 2 entrega às DVs em `via2_intermar_a` (sem `railwayTrack199`/`val_via2`)
- **ALT-102**: GestorDeDVs — livre por tipo (por linha) e reserva para o tipo líder fechar os 82 (fim do travamento por mistura)
- **ALT-103**: CoperXI/CostaLeite — via2_e e marginais pegas juntas no GestorDeCirculacao (`bloquear_encoste_marginal`); CostaLeite passa a soltar as marginais antes de devolver a loco
- **ALT-104**: CostaLeite — rota da loco de alívio inclui `linhaPaqueta_a` e `linhaCostaLeita_a` até `_costaLeite_inicio_alivio`
- **ALT-105**: GestorDeAlivios — um alívio PST por vez (`analisar_pst` para após iniciar um alívio)
- **ALT-106**: GestorDeCirculacao — `rp_pst_switch36` (AMV da linha verde) pego no `bloquear_encoste_marginal` e liberado no fim do encoste (CoperXI/CostaLeite)
- **ALT-107**: CoperXI/CostaLeite — `log_mov` em todos os TrainMoveTo (ini/fim), Enter (etapa) e nos Release do encoste
- **ALT-108**: CoperXI — rota da loco de alívio volta a `via1_intermar_d → pstSaidaCoperXI` (removidas as linhas da CostaLeite)
- **ALT-109**: GestorDeDVs — loco sozinha é devolvida direto da linha depois de colocar o lote (`sel_loco_sozinha`)
- **ALT-110**: CostaLeite — fim do alívio solta os vazios presos no `wait` (descarga não trava com as linhas de vazios cheias)
- **ALT-111**: CostaLeite — logs dos Seize/Release/Wait do recurso de manobra, `linhas_cheias`, `soltou_wait` e `vai_descarga`/`nao_descarrega` com `_loteEmDescarga`
- **ALT-112**: CostaLeite — `_trem_chegando` bloqueia novo lote durante a manobra de chegada; corrigido sinal do `podeReceberNovoLote()`

- **ALT-113**: GestorDeChegadas — botões dos terminais viram pedidos na fila (`pedir_trem`), com as mesmas condições de entrada; logs `aguardando`/`decisao`; prefixo sem vagão vira aviso

- **ALT-114**: GestorDeDVs — fila na entrada (`chegar_nas_dvs`, `fila_dvs`): um trem manobra por vez; `get_capacidade(null)` vira aviso
- **ALT-115**: GestorDeAlivios — alívio PST licencia a `via1_intermar_c` junto com a faixa de criação; retorno entra pela faixa onde o trem está; `liberar_faixas_pst` não solta mais as faixas debaixo do trem
- **ALT-116**: CoperXXI — podeReceberNovoLote recusa trem entrando (_trem_chegando) ou com carregados no terminal
- **ALT-117**: MoegaoMar e MoegaoTerra — podeReceberNovoLote recusa trem entrando (_trem_chegando) ou com carregados no terminal
- **ALT-118**: Alívio PST — a faixa de criação (via1_intermar_d) fica com o trem até ele passar por ela na volta; evita outro alívio entrar no corredor
- **ALT-119**: CostaLeite — não recebe trem enquanto houver vazios na linha Terra (_loteDeVazios_LinhaTerra)
- **ALT-120**: CoperXXI — alívio leva só a quantidade pedida pelo GestorDeAlivios; o excesso fica na linha (rmv_excesso / precisa_mais_vgs)
- **ALT-121**: GestorDeAlivios — loco do alívio PST que encosta no lote na linha de destino conta como chegada (outHit ligado)
- **ALT-123**: CoperXI e CoperXXI — log de análise por bloco (variável `log_de_analise`)
- **ALT-124**: CoperXXI — alívio parcial: estoques viram Wait, alívio engata tudo e a sobra volta para b → a → moega
- **ALT-126**: CoperXXI — sobra do alívio deixada pela loco nos pontos auxiliares (moega → a → b); pontos acertados para vagão de 17,5 m
- **ALT-127**: CoperXXI — alívio também é pedido sem carregados no terminal (≥ 1 vazio), encadeado no fim de cada alívio; não recebe trem durante o alívio
- **ALT-128**: CoperXXI — peça da sobra na última linha engatada é cortada no lugar (fim do erro de rota no `mover_peca_b` com lote b curto)
- **ALT-129**: CoperXXI — descarga não começa grupo com alívio pedido ou em andamento (`pode_descarregar_grupo`)
- **ALT-130**: CoperXXI — terminal só recebe trem depois de esvaziar (ferrovia é lida na chegada do carregado); ficha `docs/terminais/coperxxi.md`
- **ALT-131**: CoperXXI — grupo que bate sem lote na linha segue como "chegou" (`tem_lote_a`/`tem_lote_b`)
- **ALT-132**: Tgrao — podeReceberNovoLote recusa trem entrando (_trem_chegando) ou com carregados no terminal; removido trace "bom dia Bruno"
- **ALT-133**: Tgrao — não recebe trem com vazios no terminal (esperando ou em alívio)
- **ALT-134**: Bracell, Eldorado e Suzano — podeReceberNovoLote recusa trem entrando, carregados ou vazios no terminal (Suzano também na troca)
- **ALT-135**: Suzano, Eldorado e Bracell — trem de teste entra pelo GestorDeChegadas (`receber_trem`), não direto na circulação
- **ALT-136**: Tgrao, Bracell, Eldorado e Suzano — log de análise por bloco (variável `log_de_analise`)
- **ALT-137**: Suzano — alívio solta a `rp_linha6000` antes de sair (`liberar_linha6000_alivio`); destrava a troca do 2º trem
- **ALT-138**: Eldorado — a loco solta a `rp_linha6000` depois do último lote (`liberar_linha5000_6000`); destrava o 2º trem e a linha 6000 para Bracell e Suzano
