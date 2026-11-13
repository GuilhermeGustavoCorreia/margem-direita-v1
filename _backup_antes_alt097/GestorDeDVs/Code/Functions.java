Main main()
{/*ALCODESTART::1787941043056*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

int quantos_vagoes_cabem(ITerminalMoega terminal)
{/*ALCODESTART::1790621198562*/
// quantos vagoes o MNB do terminal pode trazer para as DVs
// - fecha o trem (vazios >= falta): traz exatamente a falta, acopla no lote e parte
// - nao fecha: traz os vazios se couberem no espaco livre (estaciona)
// - senao 0: o terminal espera e o GestorDeAlivios tenta de novo depois
String ferrovia = terminal.getFerrovia();
String segmento = terminal.getSegmento();

int acumulado = acumulado_do_tipo(ferrovia, segmento);
int falta     = TAMANHO_TREM_FORMADO - acumulado;
int vazios    = terminal.get_quantidade_vazios();
int livre     = espaco_livre();

int retorno;
if (falta <= 0)           retorno = 0;
else if (vazios >= falta) retorno = falta;
else if (vazios <= livre) retorno = vazios;
else                      retorno = 0;

if (trace_dvs && (retorno > 0 || ((int) time()) % 60 == 0)) {
    Log.info("GesDVs", "consulta_cabem", String.format("%s | tipo=%s/%s | acumulado=%d | falta=%d | vazios=%d | livre=%d | retorno=%d",
        ((Agent) terminal).getName(), ferrovia, segmento, acumulado, falta, vazios, livre, retorno));
    log_estado();
}

return retorno;
/*ALCODEEND*/}

boolean condicao_pode_receber_alivio()
{/*ALCODESTART::1791478926284*/
//DV´s estão cheias?
return true;
/*ALCODEEND*/}

int acumulado_do_tipo(String ferrovia,String segmento)
{/*ALCODESTART::1797310000001*/
// vagoes ja estacionados nas DVs com a mesma ferrovia e segmento (cada lote tem um tipo so)
int total = 0;
Trem[] lotes = { lote_dv2, lote_dv3, lote_dv4, lote_direita, lote_slot_via2 };

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
// vagoes livres nas DVs: 4 linhas + slot da via 2 (total 138)
return (CAPACIDADE_DV2       - main().getNumeroDeVagoes(lote_dv2))
     + (CAPACIDADE_DV3       - main().getNumeroDeVagoes(lote_dv3))
     + (CAPACIDADE_DV4       - main().getNumeroDeVagoes(lote_dv4))
     + (CAPACIDADE_DIREITA   - main().getNumeroDeVagoes(lote_direita))
     + (CAPACIDADE_SLOT_VIA2 - main().getNumeroDeVagoes(lote_slot_via2));
/*ALCODEEND*/}

double log_estado()
{/*ALCODESTART::1797310000003*/
// resumo das linhas: ocupacao/capacidade e tipo de cada lote
Log.info("GesDVs", "estado", String.format("DIR %s | DV2 %s | DV3 %s | DV4 %s | SLOT %s | livre=%d",
    texto_linha(lote_direita, CAPACIDADE_DIREITA),
    texto_linha(lote_dv2, CAPACIDADE_DV2),
    texto_linha(lote_dv3, CAPACIDADE_DV3),
    texto_linha(lote_dv4, CAPACIDADE_DV4),
    texto_linha(lote_slot_via2, CAPACIDADE_SLOT_VIA2),
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

