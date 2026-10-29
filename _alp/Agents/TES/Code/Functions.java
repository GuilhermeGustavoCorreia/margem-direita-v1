double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1792518752491*/
for (int i = 0; i < trem.size(); i++) {
        Agent carro = trem.getCar(i); // confere o nome exato do método com Ctrl+Espaço

        if (carro instanceof Vagao) {
            Vagao vagao = (Vagao) carro;
            ferrovia_em_atendimento = vagao.ferrovia;
            segmento_em_atendimento = vagao.segmento;
            return; // achou o primeiro vagão, encerra
        }
    }

    traceln("erro: trem não tem nenhum vagão");
/*ALCODEEND*/}

int _getVagoesVazios()
{/*ALCODESTART::1792518896288*/


int vazios_linha_03	= lote_linha_3 != null ? lote_linha_3.getNumeroDeVagoesVazios() : 0;
int vazios_linha_04	= main().getNumeroDeVagoes(lote_linha_04_fundo);
int vazios_linha_05	= main().getNumeroDeVagoes(lote_linha_05_fundo);

return (

	vazios_linha_03 +
	vazios_linha_04 +
	vazios_linha_05
	
);
/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1792518896291*/
// ALT-053: o lote parado no wait_resto_3000 nao conta (espera as linhas liberarem);
// senao o alivio dos ultimos vazios nunca e forcado (deadlock na VLI)
boolean resto_esperando = wait_resto_3000.size() > 0 && wait_resto_3000.get(0) == lote_linha3000_emDescarga;
int vagoes_linha_3000_emDescarga = resto_esperando ? 0 : main().getNumeroDeVagoes(lote_linha3000_emDescarga);
int vagoes_linha_3000_proximo 	 = main().getNumeroDeVagoes(lote_linha3000_proximo);

int vagoes_linha_03				 = lote_linha_3 != null? lote_linha_3.getNumeroDeVagoesCarregados() : 0;
int vagoes_linha_04				 = main().getNumeroDeVagoes(lote_linha_4);
int vagoes_linha_05				 = main().getNumeroDeVagoes(lote_linha_5); 

return (
	vagoes_linha_3000_emDescarga + 
	vagoes_linha_3000_proximo + 
	vagoes_linha_03 +
	vagoes_linha_04 +
	vagoes_linha_05 +
	main().getNumeroDeVagoes(trem_em_encoste)   // ALT-047: carregados em manobra
);
/*ALCODEEND*/}

Main main()
{/*ALCODESTART::1792519031768*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1792519031770*/
return true;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1792519031772*/
// ALT-042: padrao do T39
boolean moegas_livres  = rp_moega03.idle() > 0 && rp_moega04.idle() > 0;
boolean ultimos_vazios = (_getVagoesCarregados() == 0 && _getVagoesVazios() > 0);

// ultimos vazios do terminal: forca o alivio
if (moegas_livres && ultimos_vazios) return (float) 2.0;

if (QUANTIDADE_PARA_ALIVIAR <= 0) return 0;
return (float) _getVagoesVazios() / QUANTIDADE_PARA_ALIVIAR;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1792519031774*/
// ALT-042
return main()._tes_inicio_alivio;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1792519031776*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1792519031778*/
return "pmc";
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_primeiro_alivio()
{/*ALCODESTART::1792519031780*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1792519031782*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1792519031784*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1792519031786*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1792519031788*/
return new ArrayList<>(Arrays.asList(
	main().gestorDeCirculacao.faixa_via1_intermar_d, 
	main().gestorDeCirculacao.faixa_via1_intermar_c
));
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1792519031790*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1792519031792*/
return _getVagoesVazios();
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1792519031794*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1792519031796*/
// ALT-025: igual ao T39
return 1;
/*ALCODEEND*/}

int atualizar_licenciamento_linha3000(Trem trem)
{/*ALCODESTART::1792519768494*/
// ALT-035: a 3000 fica ocupada enquanto houver lote nela ou a caminho.
// Antes olhava se havia trem fisicamente na track: no onEnter o trem ainda nao chegou e a faixa era liberada.
Faixa faixa_linha_3000_a = main().gestorDeCirculacao.faixa_linha3000_a;

boolean tem_lote_linha_3000 = (lote_linha3000_emDescarga != null || lote_linha3000_proximo != null);

if (tem_lote_linha_3000) {
    faixa_linha_3000_a.ocupar(trem);
} else {
    faixa_linha_3000_a.liberar();
}
/*ALCODEEND*/}

boolean eh_trem_rumo(Trem trem)
{/*ALCODESTART::1792521700042*/
// ALT-035: true se o primeiro vagao do trem for da Rumo
for (int i = 0; i < trem.size(); i++) {
    Agent carro = trem.getCar(i);
    if (carro instanceof Vagao) {
        return "rumo".equals(((Vagao) carro).ferrovia);
    }
}
return false;
/*ALCODEEND*/}

double distancia_ate_fim_linha03(Trem trem)
{/*ALCODESTART::1792526700004*/
// ALT-036: distancia (m) entre o carro do trem mais perto de _tes_linha03_fim e o ponto.
// _tes_moega_linha03 e _tes_linha03_fim estao na mesma track; o trem vem da moega para o fim.
// Se nenhum carro estiver na track do fim, retorna 0 (nao avanca).
PositionOnTrack fim   = main()._tes_linha03_fim;
PositionOnTrack moega = main()._tes_moega_linha03;
RailwayTrack track_fim = fim.getTrack();
double offset_fim = fim.getOffset(METER);
double sinal = (moega.getOffset(METER) >= offset_fim) ? 1 : -1; // sentido moega -> fim

double menor = Double.MAX_VALUE;
try {
    for (int i = 0; i < trem.size(); i++) {
        Agent carro = trem.getCar(i);
        java.lang.reflect.Method m_track  = carro.getClass().getMethod("getTrack", boolean.class);
        java.lang.reflect.Method m_offset = carro.getClass().getMethod("getOffset", boolean.class);
        for (boolean frente : new boolean[]{true, false}) {
            RailwayTrack track = (RailwayTrack) m_track.invoke(carro, frente);
            if (track == track_fim) {
                double offset = (double) m_offset.invoke(carro, frente);
                menor = Math.min(menor, sinal * (offset - offset_fim));
            }
        }
    }
} catch (Exception e) {
    error("distancia_ate_fim_linha03: " + e.getMessage());
    return 0;
}

if (menor == Double.MAX_VALUE) {
    traceln(String.format("[%.1f][TES][distancia_ate_fim_linha03] trem=%d sem carro na track do fim", time(), trem.getId()));
    return 0;
}
return Math.max(0, menor);
/*ALCODEEND*/}

int get_fundo_destino(Trem lote_carregado)
{/*ALCODESTART::1792671400066*/
// ALT-037: qual lote de fundo recebe o proximo vazio: 5 = linha 05, 4 = linha 04, 0 = nenhum
if (cabe_no_fundo(lote_linha_05_fundo, CAPACIDADE_LINHA_05_FUNDO, main()._tes_linha05_fundo_fim, main()._tes_linha05_fundo_comeco))
    return 5;

// o fundo da 04 ocupa o mesmo espaco do lote_linha_4: so recebe com a linha 04 vazia
// (ou quando o vagao que vai sair agora e o ultimo do lote_linha_4)
boolean linha4_livre = (lote_linha_4 == null) || (lote_carregado == lote_linha_4 && lote_linha_4.size() <= 1);
if (linha4_livre && cabe_no_fundo(lote_linha_04_fundo, CAPACIDADE_LINHA_04_FUNDO, main()._tes_linha04_fim, main()._tes_linha04_comeco))
    return 4;

return 0;
/*ALCODEEND*/}

boolean cabe_no_fundo(Trem lote,int capacidade,PositionOnTrack fim,PositionOnTrack comeco)
{/*ALCODESTART::1792671400067*/
// ALT-037: o lote de fundo cresce a partir do comeco; o vazio entra ocupando 1 vagao a partir do fim.
// Cabe se nao passar da capacidade e se (vagoes do lote + 1) vagoes couberem entre fim e comeco.
int n = (lote == null) ? 0 : lote.size();
if (n >= capacidade) return false;
double espaco = Math.abs(comeco.getOffset(METER) - fim.getOffset(METER));
return (n + 1) * main().TAMANHO_VAGAO_GRAO <= espaco;
/*ALCODEEND*/}

PositionOnTrack get_pos_fim_fundo()
{/*ALCODESTART::1792671400068*/
// ALT-037
return (fundo_destino_04e5 == 5) ? main()._tes_linha05_fundo_fim : main()._tes_linha04_fim;
/*ALCODEEND*/}

PositionOnTrack get_pos_comeco_fundo()
{/*ALCODESTART::1792671400069*/
// ALT-037
return (fundo_destino_04e5 == 5) ? main()._tes_linha05_fundo_comeco : main()._tes_linha04_comeco;
/*ALCODEEND*/}

double get_offset_entrada_fundo()
{/*ALCODESTART::1792671400070*/
// ALT-037: cabeca do vazio 1 vagao para dentro a partir do fim (o vagao fica entre fim e fim + 1 vagao),
// como no trainEnter1 da ADM. Depois ele anda ate o comeco ou ate bater no lote de fundo.
PositionOnTrack fim    = get_pos_fim_fundo();
PositionOnTrack comeco = get_pos_comeco_fundo();
double sinal = (comeco.getOffset(METER) >= fim.getOffset(METER)) ? 1 : -1;
return fim.getOffset(METER) + sinal * main().TAMANHO_VAGAO_GRAO;
/*ALCODEEND*/}

double liberar_wait_lote_fundo05()
{/*ALCODESTART::1792671400071*/
// ALT-043: solta o lote do fundo 05 (como liberar_wait_lote_linha_09 do T39)
if (wait_fundo_05.size() > 0) {
    wait_fundo_05.free(wait_fundo_05.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_fundo04()
{/*ALCODESTART::1792681100002*/
// ALT-043: solta o lote do fundo 04 (como liberar_wait_lote_linha_09 do T39)
if (wait_fundo_04.size() > 0) {
    wait_fundo_04.free(wait_fundo_04.get(0));
}
/*ALCODEEND*/}

double fim_ciclo_04e5()
{/*ALCODESTART::1792671400072*/
// ALT-038: vazio ja esta no lote de fundo (recursos liberados no rls_vazio_04e5a/b) -> proximo vagao
ciclo_04e5_ativo = false;
proximo_vagao_04e5();
// ALT-046: linhas 04/05 podem ter acabado -> resto da 3000
tentar_encostar_resto_3000(false);
/*ALCODEEND*/}

double proximo_vagao_04e5()
{/*ALCODESTART::1792674800016*/
// ALT-038: solta o proximo vagao para a moega 04 (linha 05 primeiro, depois 04).
// Chamada pelo iniciar_descarga_moegas (fim da manobra) e no fim de cada ciclo (fim_ciclo_04e5).
if (ciclo_04e5_ativo) return;                                          // ciclo em andamento
// ALT-041: sem checar rp_*.idle() aqui. No fim do ciclo o vagao acabou de liberar os recursos
// (rls_vazio_04e5a/b) e o pool ainda aparece ocupado no mesmo instante. Se a manobra estiver
// com o rp_mnb_linha04e5, o vagao espera na fila do sz_descarga_04e5.

boolean da_linha05 = (lote_linha_5 != null);
Trem lote = da_linha05 ? lote_linha_5 : lote_linha_4;
if (lote == null) return;                                               // nada para descarregar

int no_wait = da_linha05 ? wait_lote_linha05.size() : wait_lote_linha04.size();
if (no_wait == 0) {                                                     // lote ainda nao parou na linha
    traceln(String.format("[%.1f][TES][proximo_vagao_04e5] espera: lote=%d ainda nao esta no wait da linha %s", time(), lote.getId(), da_linha05 ? "05" : "04"));
    return;
}

fundo_destino_04e5 = get_fundo_destino(lote);
if (fundo_destino_04e5 == 0) {
    traceln(String.format("[%.1f][TES][descarga_04e5] AVISO sem espaco nos fundos 05/04, lote=%d parado com %d vagoes",
        time(), lote.getId(), lote.getNumeroDeVagoes()));
    return;
}

ciclo_04e5_ativo = true;
if (da_linha05) wait_lote_linha05.free(wait_lote_linha05.get(0));
else            wait_lote_linha04.free(wait_lote_linha04.get(0));
/*ALCODEEND*/}

double iniciar_descarga_moegas()
{/*ALCODESTART::1792675800006*/
// ALT-040: chamada no fim da manobra de encoste (delay_iniciar_descarga), como na ADM.
// Os lotes das linhas 05 e 04 ficam parados nos wait_lote_linha05/04 ate aqui.
// Solta o primeiro vagao para a moega 04; os proximos saem no fim de cada ciclo (fim_ciclo_04e5).
traceln(String.format("[%.1f][TES][iniciar_descarga_moegas] lote_linha_5=%s lote_linha_4=%s", time(),
    lote_linha_5 == null ? "null" : String.valueOf(lote_linha_5.getNumeroDeVagoes()),
    lote_linha_4 == null ? "null" : String.valueOf(lote_linha_4.getNumeroDeVagoes())));
proximo_vagao_04e5();
/*ALCODEEND*/}

boolean pode_aliviar_linha03()
{/*ALCODESTART::1792680500038*/
// ALT-042: a linha 03 so entra no alivio depois que a descarga talhou (o lote esta parado)
return lote_linha_3 != null && talhou_linha03 && lote_linha_3.getNumeroDeVagoesVazios() > 0;
/*ALCODEEND*/}

int get_excesso_alivio(Trem trem)
{/*ALCODESTART::1792680500039*/
// ALT-042: quantos carros do lote que acabou de acoplar ficam na linha (os do fundo).
// Leva so os vazios que faltam para quantidade_alivio; na linha 03 o resto (vazios + carregados) fica.
int pegos_antes = main().getNumeroDeVagoes(trem) - vgs_lote_alivio - qt_vgs_inicio_alivio;
int falta       = Math.max(0, quantidade_alivio - pegos_antes);
int levar       = Math.min(vazios_lote_alivio, falta);
int excesso     = vgs_lote_alivio - levar;
traceln(String.format("[%.1f][TES][alivio] falta=%d leva=%d deixa=%d", time(), falta, levar, excesso));
return excesso;
/*ALCODEEND*/}

double retomar_descargas()
{/*ALCODESTART::1792685300008*/
// ALT-044: chamada no fim do alivio (rls_alivio).
// Linha 03: o que sobrou do alivio volta para o loop da descarga pelo tem_espaco
// (se nao houver carregados, o tem_espaco manda direto para para_fim_descarga_linha03).
if (wait_retomar_linha03.size() > 0) {
    Trem lote = (Trem) wait_retomar_linha03.get(0);
    int carregados = lote.getNumeroDeVagoesCarregados();
    if (carregados > 0) talhou_linha03 = false;
    traceln(String.format("[%.1f][TES][retomar_descargas] linha 03 lote=%d com %d carregados", time(), lote.getId(), carregados));
    wait_retomar_linha03.free(lote);
}
// Moega 04: se a descarga 04/05 tinha parado sem espaco nos fundos, volta agora
proximo_vagao_04e5();
// ALT-046: o alivio pode ter esvaziado a linha 03 e o fundo 04 -> resto da 3000
tentar_encostar_resto_3000(true);
/*ALCODEEND*/}

double tentar_encostar_resto_3000(boolean mostrar_trace)
{/*ALCODESTART::1792698800016*/
// ALT-046: solta o resto da 3000 (com as locos FIPS na cauda) para o encoste das linhas 03/04/05
// quando os lotes do primeiro encoste ja sairam: linha 03 aliviada, linhas 04 e 05 descarregadas
// e o fundo 04 (mesmo espaco da linha 04) aliviado. O fundo 05 fica em outra track.
if (wait_resto_3000.size() == 0) return;

boolean linhas_livres = lote_linha_3 == null && lote_linha_4 == null
                     && lote_linha_04_fundo == null && lote_linha_5 == null;
if (!linhas_livres) {
    if (mostrar_trace)
        traceln(String.format("[%.1f][TES][resto_3000] espera: linha03=%d linha04=%d fundo04=%d linha05=%d", time(),
            main().getTamanhoDoTrem(lote_linha_3), main().getTamanhoDoTrem(lote_linha_4),
            main().getTamanhoDoTrem(lote_linha_04_fundo), main().getTamanhoDoTrem(lote_linha_5)));
    return;
}

Trem resto = (Trem) wait_resto_3000.get(0);
traceln(String.format("[%.1f][TES][resto_3000] lote=%d sai da 3000 para o encoste", time(), resto.getId()));
// ALT-054: a MRS (82 vagoes) nao cabe numa manobra (24+12+11) e volta para a 3000 -> a 3000 continua dela
if (resto.getNumeroDeVagoes() <= 47) lote_linha3000_emDescarga = null;
atualizar_licenciamento_linha3000(resto);
wait_resto_3000.free(resto);
/*ALCODEEND*/}

