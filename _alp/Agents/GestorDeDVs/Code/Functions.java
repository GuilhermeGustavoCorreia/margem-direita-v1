Main main()
{/*ALCODESTART::1787941043056*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

int quantos_vagoes_cabem(ITerminalMoega terminal)
{/*ALCODESTART::1790621198562*/
// quantos vagoes o MNB do terminal pode trazer para as DVs
// - fecha o trem (vazios >= falta): traz exatamente a falta, acopla nos lotes e parte
// - nao fecha: traz os vazios se couberem nas linhas do tipo (linhas vazias + linhas do mesmo tipo)
//   e se, depois de estacionar, ainda sobrar espaco para o tipo lider fechar os 82 dele
// - senao 0: o terminal espera e o GestorDeAlivios tenta de novo depois
String ferrovia = terminal.getFerrovia();
String segmento = terminal.getSegmento();

int acumulado = acumulado_do_tipo(ferrovia, segmento);
int falta     = TAMANHO_TREM_FORMADO - acumulado;
int vazios    = terminal.get_quantidade_vazios();
int livre     = espaco_livre_tipo(ferrovia, segmento);

// reserva: tipo com mais vagoes acumulados que este (null = sem lider)
Trem lider = lote_do_lider(ferrovia, segmento, acumulado);
int falta_lider = 0, sobra_lider = 0;
if (lider != null) {
    falta_lider = TAMANHO_TREM_FORMADO - acumulado_do_tipo(lider.getFerrovia(), lider.getSegmento());
    sobra_lider = espaco_lider_apos(ferrovia, segmento, vazios, lider.getFerrovia(), lider.getSegmento());
}

int retorno;
if (falta <= 0)                                   retorno = 0;
else if (vazios >= falta)                         retorno = falta;
else if (vazios > livre)                          retorno = 0;
else if (lider != null && sobra_lider < falta_lider) retorno = 0;
else                                              retorno = vazios;

if (trace_dvs && (retorno > 0 || ((int) time()) % 60 == 0)) {
    String txt_lider = (lider == null) ? "-" : String.format("%s/%s falta=%d sobra=%d", lider.getFerrovia(), lider.getSegmento(), falta_lider, sobra_lider);
    Log.info("GesDVs", "consulta_cabem", String.format("%s | tipo=%s/%s | acumulado=%d | falta=%d | vazios=%d | livre=%d | livre_tipo=%d | lider=%s | retorno=%d",
        ((Agent) terminal).getName(), ferrovia, segmento, acumulado, falta, vazios, espaco_livre(), livre, txt_lider, retorno));
    log_estado();
}

return retorno;
/*ALCODEEND*/}

boolean condicao_pode_receber_alivio()
{/*ALCODESTART::1791478926284*/
// nao recebe novo alivio enquanto um trem manobra nas DVs
return trem_em_manobra == null;
/*ALCODEEND*/}

int acumulado_do_tipo(String ferrovia,String segmento)
{/*ALCODESTART::1797310000001*/
// vagoes ja estacionados nas DVs com a mesma ferrovia e segmento (cada lote tem um tipo so)
int total = 0;
Trem[] lotes = { lote_dv2, lote_dv3, lote_dv4, lote_direita };

for (Trem lote : lotes) {
    if (lote == null) continue;
    if (lote.getFerrovia().equalsIgnoreCase(ferrovia) && lote.getSegmento().equalsIgnoreCase(segmento)) {
        total += main().getNumeroDeVagoes(lote);
    }
}
return total;
/*ALCODEEND*/}

int espaco_livre()
{/*ALCODESTART::1797310000002*/
// vagoes livres nas 4 linhas das DVs (o slot da via 2 fica fora desta versao: 108)
return (CAPACIDADE_DV2     - main().getNumeroDeVagoes(lote_dv2))
     + (CAPACIDADE_DV3     - main().getNumeroDeVagoes(lote_dv3))
     + (CAPACIDADE_DV4     - main().getNumeroDeVagoes(lote_dv4))
     + (CAPACIDADE_DIREITA - main().getNumeroDeVagoes(lote_direita));
/*ALCODEEND*/}

double log_estado()
{/*ALCODESTART::1797310000003*/
// resumo das linhas: ocupacao/capacidade e tipo de cada lote
Log.info("GesDVs", "estado", String.format("DIR %s | DV2 %s | DV3 %s | DV4 %s | livre=%d",
    texto_linha(lote_direita, CAPACIDADE_DIREITA),
    texto_linha(lote_dv2, CAPACIDADE_DV2),
    texto_linha(lote_dv3, CAPACIDADE_DV3),
    texto_linha(lote_dv4, CAPACIDADE_DV4),
    espaco_livre()));
/*ALCODEEND*/}

String texto_linha(Trem lote,int capacidade)
{/*ALCODESTART::1797310000004*/
if (lote == null) return "0/" + capacidade;
return main().getNumeroDeVagoes(lote) + "/" + capacidade + " " + lote.getFerrovia() + "/" + lote.getSegmento();
/*ALCODEEND*/}

String texto_trem(Trem trem)
{/*ALCODESTART::1797320000001*/
// (prefixo - id) | vagoes + locos | ferrovia/segmento do 1o vagao (pula as locos)
if (trem == null) return "-";

int vagoes = main().getNumeroDeVagoes(trem);
int locos  = trem.size() - vagoes;
Vagao v    = trem.getPrimeiroVagao();
String tipo = (v == null) ? "-" : v.ferrovia + "/" + v.segmento;

return String.format("(%s - %d) | %d vgs + %d loco | %s", trem.prefixo, trem.getId(), vagoes, locos, tipo);
/*ALCODEEND*/}

double log_comecou_manobra(Trem trem)
{/*ALCODESTART::1797320000002*/
if (!trace_dvs) return;
Log.info("GesDVs", "comecou_manobra", texto_trem(trem) + " | livre=" + espaco_livre());
/*ALCODEEND*/}

double colocar_lote(String linha,Trem lote)
{/*ALCODESTART::1797320000003*/
// grava o lote na linha e registra no log; linha: "DIR", "DV2", "DV3", "DV4" ou "SLOT"
int capacidade;
switch (linha) {
    case "DIR":  lote_direita   = lote; capacidade = CAPACIDADE_DIREITA;   break;
    case "DV2":  lote_dv2       = lote; capacidade = CAPACIDADE_DV2;       break;
    case "DV3":  lote_dv3       = lote; capacidade = CAPACIDADE_DV3;       break;
    case "DV4":  lote_dv4       = lote; capacidade = CAPACIDADE_DV4;       break;
    case "SLOT": lote_slot_via2 = lote; capacidade = CAPACIDADE_SLOT_VIA2; break;
    default:
        error("GestorDeDVs.colocar_lote: linha desconhecida '" + linha + "'");
        return;
}
if (lote != null && lote.prefixo == null) lote.prefixo = "LOTE_" + linha;

if (trace_dvs) {
    Log.info("GesDVs", "colocou_lote", String.format("%s %d/%d | %s",
        linha, main().getNumeroDeVagoes(lote), capacidade, texto_trem(lote)));
    log_estado();
}
/*ALCODEEND*/}

double log_terminou_manobra(Trem trem)
{/*ALCODESTART::1797320000004*/
if (!trace_dvs) return;
Log.info("GesDVs", "terminou_manobra", texto_trem(trem));
log_estado();
/*ALCODEEND*/}

boolean segue_direto(Trem trem)
{/*ALCODESTART::1797340000001*/
// segue direto para a saida: trem completo (82) ou celulose
Vagao v = trem.getPrimeiroVagao();
boolean celulose = (v != null && "celulose".equalsIgnoreCase(v.segmento));
return main().getNumeroDeVagoes(trem) >= TAMANHO_TREM_FORMADO || celulose;
/*ALCODEEND*/}

double iniciar_manobra(Trem trem)
{/*ALCODESTART::1797340000002*/
// trem chegou nas DVs: decide se forma o trem de 82 ou estaciona
if (trem_em_manobra != null) {
    Log.warn("GesDVs", "manobra_simultanea", "ja existe trem em manobra: " + texto_trem(trem_em_manobra) + " | chegou: " + texto_trem(trem));
}
trem_em_manobra = trem;
linha_destino   = null;

Vagao v = trem.getPrimeiroVagao();
int acumulado = (v == null) ? 0 : acumulado_do_tipo(v.ferrovia, v.segmento);
formando = !segue_direto(trem) && acumulado > 0
        && acumulado + main().getNumeroDeVagoes(trem) == TAMANHO_TREM_FORMADO;

if (trace_dvs) {
    Log.info("GesDVs", "comecou_manobra", texto_trem(trem) + " | livre=" + espaco_livre()
        + " | acumulado do tipo=" + acumulado
        + " | " + (segue_direto(trem) ? "segue direto" : (formando ? "forma o trem de 82" : "estaciona")));
}
/*ALCODEEND*/}

boolean escolher_linha(Trem trem)
{/*ALCODESTART::1797340000003*/
// grava linha_destino e loga a decisao; chamada uma vez no onEnter do sel_acao
linha_destino = calcular_linha(trem);

if (linha_destino == null && !formando && main().getNumeroDeVagoes(trem) > 0) {
    Log.warn("GesDVs", "sem_linha", "nenhuma linha disponivel para " + texto_trem(trem) + " -> saida com os vagoes");
}
if (trace_dvs) {
    Log.info("GesDVs", "proxima_linha", texto_trem(trem) + " -> " + (linha_destino == null ? "saida" : linha_destino));
}
return linha_destino != null;
/*ALCODEEND*/}

String calcular_linha(Trem trem)
{/*ALCODESTART::1797340000015*/
// proxima linha do trem no ponto; null = vai para a saida
// sem efeito colateral: usada na condicao do sel_acao, que o AnyLogic avalia mais de uma vez
String[] linhas = { "DIR", "DV2", "DV3", "DV4" };
Vagao v = trem.getPrimeiroVagao();
int vagoes = main().getNumeroDeVagoes(trem);

if (formando) {
    // proxima linha com lote do mesmo tipo
    if (vagoes < TAMANHO_TREM_FORMADO) {
        for (String l : linhas) {
            if (mesmo_tipo(get_lote(l), v)) return l;
        }
    }
    return null;
}
if (vagoes == 0) return null;

// 1o: linha com lote do mesmo tipo e espaco; 2o: linha vazia
for (String l : linhas) {
    Trem lote = get_lote(l);
    if (mesmo_tipo(lote, v) && main().getNumeroDeVagoes(lote) < get_capacidade(l)) return l;
}
for (String l : linhas) {
    if (get_lote(l) == null) return l;
}
return null;
/*ALCODEEND*/}

boolean mesmo_tipo(Trem lote,Vagao v)
{/*ALCODESTART::1797340000004*/
if (lote == null || v == null) return false;
return lote.getFerrovia().equalsIgnoreCase(v.ferrovia) && lote.getSegmento().equalsIgnoreCase(v.segmento);
/*ALCODEEND*/}

Trem get_lote(String linha)
{/*ALCODESTART::1797340000005*/
switch (linha) {
    case "DIR": return lote_direita;
    case "DV2": return lote_dv2;
    case "DV3": return lote_dv3;
    case "DV4": return lote_dv4;
    default:    return null;
}
/*ALCODEEND*/}

int get_capacidade(String linha)
{/*ALCODESTART::1797340000006*/
if (linha == null) {
    Log.warn("GesDVs", "linha_nula", "get_capacidade chamado sem linha_destino | trem em manobra: " + texto_trem(trem_em_manobra));
    return 0;
}
switch (linha) {
    case "DIR": return CAPACIDADE_DIREITA;
    case "DV2": return CAPACIDADE_DV2;
    case "DV3": return CAPACIDADE_DV3;
    case "DV4": return CAPACIDADE_DV4;
    default:    return 0;
}
/*ALCODEEND*/}

RailwayTrack get_track_linha(String linha)
{/*ALCODESTART::1797340000007*/
// trilho de cada linha das DVs
switch (linha) {
    case "DIR": return main().dv_direita;
    case "DV2": return main().dv_2;
    case "DV3": return main().dv_3;
    case "DV4": return main().dv_4_b;
    default:
        error("GestorDeDVs.get_track_linha: linha desconhecida '" + linha + "'");
        return null;
}
/*ALCODEEND*/}

boolean get_offset_do_inicio(String linha)
{/*ALCODESTART::1797340000008*/
// a ponta da Ponta da Praia e o inicio da track em dv_direita, dv_2 e dv_4_b; na dv_3 e o fim
return !"DV3".equals(linha);
/*ALCODEEND*/}

RailwayTrack[] get_trilhos_evitar(String linha)
{/*ALCODESTART::1797340000009*/
// linhas das DVs que a rota nao pode usar (todas, menos a de destino)
List<RailwayTrack> evitar = new ArrayList<RailwayTrack>();
for (String l : new String[] { "DIR", "DV2", "DV3", "DV4" }) {
    if (!l.equals(linha)) evitar.add(get_track_linha(l));
}
return evitar.toArray(new RailwayTrack[0]);
/*ALCODEEND*/}

double entrar_lote_da_linha()
{/*ALCODESTART::1797340000010*/
// o trem bateu no lote da linha: o lote entra no acoplar_lote
Trem lote = get_lote(linha_destino);
if (lote == null) {
    error("GestorDeDVs: trem bateu na linha " + linha_destino + " sem lote registrado");
    return;
}
entrar_lote.take(lote);
/*ALCODEEND*/}

double limpar_lote(String linha,Trem trem)
{/*ALCODESTART::1797340000011*/
// lote acoplado no trem: a linha fica vazia ate o decouple deixar vagoes de novo
switch (linha) {
    case "DIR": lote_direita = null; break;
    case "DV2": lote_dv2     = null; break;
    case "DV3": lote_dv3     = null; break;
    case "DV4": lote_dv4     = null; break;
}
if (trace_dvs) {
    Log.info("GesDVs", "acoplou_lote", linha + " | trem agora: " + texto_trem(trem));
}
/*ALCODEEND*/}

int n_vagoes_deixar(Trem trem)
{/*ALCODESTART::1797340000012*/
// primeiros vagoes (lado da Ponta da Praia) que ficam na linha
return Math.min(get_capacidade(linha_destino), main().getNumeroDeVagoes(trem));
/*ALCODEEND*/}

double log_saida(Trem trem)
{/*ALCODESTART::1797340000013*/
int vagoes = main().getNumeroDeVagoes(trem);
String evento = vagoes >= TAMANHO_TREM_FORMADO ? "saida_82" : (vagoes == 0 ? "saida_loco" : "saida");
Log.info("GesDVs", evento, texto_trem(trem) + " -> circ_pos_fim_placa_2");
/*ALCODEEND*/}

double finalizar_manobra(Trem trem)
{/*ALCODESTART::1797340000014*/
log_terminou_manobra(trem);
trem_em_manobra = null;
linha_destino   = null;
formando        = false;
liberar_proxima_manobra();
/*ALCODEEND*/}

boolean lote_do_tipo(Trem lote,String ferrovia,String segmento)
{/*ALCODESTART::1797340000016*/
if (lote == null) return false;
return lote.getFerrovia().equalsIgnoreCase(ferrovia) && lote.getSegmento().equalsIgnoreCase(segmento);
/*ALCODEEND*/}

int espaco_livre_tipo(String ferrovia,String segmento)
{/*ALCODESTART::1797340000017*/
// vagas que este tipo pode usar: linhas vazias + espaco nas linhas com lote do mesmo tipo
String[] linhas = { "DIR", "DV2", "DV3", "DV4" };
int total = 0;
for (String l : linhas) {
    Trem lote = get_lote(l);
    if (lote == null)                               total += get_capacidade(l);
    else if (lote_do_tipo(lote, ferrovia, segmento)) total += get_capacidade(l) - main().getNumeroDeVagoes(lote);
}
return total;
/*ALCODEEND*/}

Trem lote_do_lider(String ferrovia,String segmento,int acumulado)
{/*ALCODESTART::1797340000018*/
// lote de outro tipo com mais vagoes acumulados que este; null se nenhum passa deste acumulado
String[] linhas = { "DIR", "DV2", "DV3", "DV4" };
Trem lider = null;
int maior = acumulado;
for (String l : linhas) {
    Trem lote = get_lote(l);
    if (lote == null || lote_do_tipo(lote, ferrovia, segmento)) continue;
    int acum = acumulado_do_tipo(lote.getFerrovia(), lote.getSegmento());
    if (acum > maior) { maior = acum; lider = lote; }
}
return lider;
/*ALCODEEND*/}

int espaco_lider_apos(String ferrovia,String segmento,int vagoes,String ferrovia_lider,String segmento_lider)
{/*ALCODESTART::1797340000019*/
// simula estacionar 'vagoes' do tipo na mesma ordem do calcular_linha
// (1o linhas do tipo com espaco, 2o linhas vazias) e devolve o espaco que sobra para o lider
String[] linhas = { "DIR", "DV2", "DV3", "DV4" };
boolean[] usada = new boolean[linhas.length];
int resto = vagoes;

for (int i = 0; i < linhas.length && resto > 0; i++) {
    Trem lote = get_lote(linhas[i]);
    if (!lote_do_tipo(lote, ferrovia, segmento)) continue;
    int esp = get_capacidade(linhas[i]) - main().getNumeroDeVagoes(lote);
    resto -= Math.min(esp, resto);
}
for (int i = 0; i < linhas.length && resto > 0; i++) {
    if (get_lote(linhas[i]) != null) continue;
    usada[i] = true;   // linha passa a ter este tipo: o lider perde a linha inteira
    resto -= Math.min(get_capacidade(linhas[i]), resto);
}

int espaco = 0;
for (int i = 0; i < linhas.length; i++) {
    Trem lote = get_lote(linhas[i]);
    if (lote_do_tipo(lote, ferrovia_lider, segmento_lider)) espaco += get_capacidade(linhas[i]) - main().getNumeroDeVagoes(lote);
    else if (lote == null && !usada[i])                    espaco += get_capacidade(linhas[i]);
}
return espaco;
/*ALCODEEND*/}

double chegar_nas_dvs(Trem trem)
{/*ALCODESTART::1797400000501*/
// entrada das DVs: um trem manobra por vez; quem chega com manobra em andamento espera parado na entrada
if (trem_em_manobra == null && fila_dvs.isEmpty()) {
    entrarNasDVs.take(trem);
    return;
}
fila_dvs.add(trem);
Log.info("GesDVs", "aguardando_dvs", texto_trem(trem) + " | em manobra: " + texto_trem(trem_em_manobra) + " | fila=" + fila_dvs.size());
/*ALCODEEND*/}

double liberar_proxima_manobra()
{/*ALCODESTART::1797400000502*/
// manobra terminou: o primeiro da fila entra nas DVs
if (trem_em_manobra != null || fila_dvs.isEmpty()) return;
Trem t = fila_dvs.remove(0);
Log.info("GesDVs", "entra_da_fila", texto_trem(t) + " | fila=" + fila_dvs.size());
entrarNasDVs.take(t);
/*ALCODEEND*/}

