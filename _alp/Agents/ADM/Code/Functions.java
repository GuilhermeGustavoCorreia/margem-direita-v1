Main main()
{/*ALCODESTART::1792172934668*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1792172934670*/
return true;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1792172934672*/
int vazios = get_quantidade_vazios();
float overload = (float) vazios / QUANTIDADE_PARA_ALIVIAR;

boolean moegas_ocupadas = (rp_moegaV.idle() == 0 || rp_moegaX.idle() == 0);	
boolean ultimos_vazios_no_terminal = (_getVagoesCarregados() == 0 && _getVagoesVazios() > 0);

//ultimos vagoes
if (ultimos_vazios_no_terminal && !moegas_ocupadas) {
    return (float) 2.0;
}

if (QUANTIDADE_PARA_ALIVIAR <= 0) return 0;
return (float) _getVagoesVazios() / QUANTIDADE_PARA_ALIVIAR;


/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1792172934674*/
return main()._adm_alivio_comeco;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1792172934676*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1792172934678*/
return "pmc";
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_primeiro_alivio()
{/*ALCODESTART::1792172934680*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1792172934682*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1792172934684*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1792172934686*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1792172934688*/
return new ArrayList<>(Arrays.asList(
	main().gestorDeCirculacao.faixa_via1_intermar_d, 
	main().gestorDeCirculacao.faixa_via1_intermar_c
));
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1792172934690*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1792172934692*/
return _get_vagoes_vazios();
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1792172934694*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1792172934696*/
// ALT-025: igual ao T39
return 1;
/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1792173002070*/
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

int _get_vagoes_vazios()
{/*ALCODESTART::1792173781676*/
// ALT-025: vazios = lote da linha 7
return main().getTamanhoDoTrem(lote_linha_7);
/*ALCODEEND*/}

int atualizar_licenciamento_linha2000(Trem trem)
{/*ALCODESTART::1792175913261*/
Faixa faixa_linha_2000_a = main().gestorDeCirculacao.faixa_linha2000_a;

boolean temTrem_linha_2000_a = !faixa_linha_2000_a.faixaEstaVazia();

if(temTrem_linha_2000_a){
    faixa_linha_2000_a.ocupar(trem);
}else{

    faixa_linha_2000_a.liberar();
}
/*ALCODEEND*/}

double liberar_wait_lote_linha_07()
{/*ALCODESTART::1792433322433*/
// libera o primeiro que entrou
if (wait_lote_linha_07.size() > 0) {
    wait_lote_linha_07.free(wait_lote_linha_07.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_linha_moegaX()
{/*ALCODESTART::1792435539028*/
// libera o primeiro que entrou
if (wait_linha_moegaX.size() > 0) {
    wait_linha_moegaX.free(wait_linha_moegaX.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_linha_moegaV()
{/*ALCODESTART::1792436398613*/
// libera o primeiro que entrou
if (wait_linha_moegaV.size() > 0) {
    wait_linha_moegaV.free(wait_linha_moegaV.get(0));
}
/*ALCODEEND*/}

double iniciar_descarga_moegas()
{/*ALCODESTART::1792500000062*/
// ALT-024: chamada no fim da manobra (rls_mnb_carregados).
// Solta para a descarga o lote que estiver parado em cada moega.
if (!descarregando_moegaX && wait_linha_moegaX.size() > 0) {
    descarregando_moegaX = true;
    condicao_ficar_linha_moegaX = true;   // vai_descarregar -> descarregar_moegaX
    liberar_wait_lote_linha_moegaX();
}
if (!descarregando_moegaV && wait_linha_moegaV.size() > 0) {
    descarregando_moegaV = true;
    condicao_ficar_linha_moegaV = true;   // vai_descarregar1 -> descarregar_moegaV
    liberar_wait_lote_linha_moegaV();
}
/*ALCODEEND*/}

double liberar_descarga_moega()
{/*ALCODESTART::1792500000063*/
// ALT-032: fluxo antigo (wait_vagao_linha7_X/V) excluído do build; o rp_moegaX/V é solto no rls_mnb_vazios_l7a/b
/*ALCODEEND*/}

double finalizar_descarga_moega(String moega)
{/*ALCODESTART::1792500000064*/
// ALT-024: último vagão da moega saiu para a descarga
if ("X".equals(moega)) {
    lote_moegaX = null;
    descarregando_moegaX = false;
} else {
    lote_moegaV = null;
    descarregando_moegaV = false;
}
traceln(String.format("[%.1f][ADM][descarga] moega %s vazia", time(), moega));
liberar_wait_lote_linha2000();
/*ALCODEEND*/}

double liberar_wait_lote_linha2000()
{/*ALCODESTART::1792500000065*/
// ALT-024: libera o primeiro trem que espera moega vazia
if (wait_lote_linha2000.size() > 0) {
    wait_lote_linha2000.free(wait_lote_linha2000.get(0));
}
/*ALCODEEND*/}

double liberar_espaco_linha7()
{/*ALCODESTART::1792500000066*/
// ALT-024: chamar quando o lote da linha 7 sair (alívio).
// ALT-032: libera os lotes das moegas que esperavam espaço na linha 7 (wait_rp_moegaX/V)
liberar_wait_rp_moegaX();
liberar_wait_rp_moegaV();
/*ALCODEEND*/}

double liberar_excesso_linha7()
{/*ALCODESTART::1792510000053*/
// ALT-025: a loco do alívio saiu da linha 7 -> o excesso volta pela entrada normal da linha 7
if (wait_excesso_l7.size() > 0) {
    wait_excesso_l7.free(wait_excesso_l7.get(0));
}
/*ALCODEEND*/}

double get_offset_entrada_linha7()
{/*ALCODESTART::1792520000002*/
// ALT-026/030: a linha 7 é a railwayTrack164 (get_trilhos_linha7), no sentido:
// começa no _adm_linha7_fim (railwayTrack164, lado do railwaySwitch102) e vai até o _adm_linha7_comeco.
// 1 vagão entra no ponto de sempre; cada vagão a mais empurra a entrada 1 vagão para dentro da linha,
// para a cauda do lote não cair em cima do railwaySwitch102. Se passar do fim de um trilho, segue no próximo.
RailwayTrack[] linhas = get_trilhos_linha7();
double alvo = get_distancia_entrada_linha7();
double acum = 0;
for (RailwayTrack l : linhas) {
    double comp = l.length(METER);
    if (alvo < acum + comp) return alvo - acum;
    acum += comp;
}
RailwayTrack ultima = linhas[linhas.length - 1];
traceln(String.format("[%.1f][ADM][entrada_l7] lote de %d vagoes passa do fim da linha 7", time(), vagoes_entrando_linha7));
return ultima.length(METER);
/*ALCODEEND*/}

RailwayTrack[] get_trilhos_linha7()
{/*ALCODESTART::1792520000101*/
// ALT-026: trilhos da linha 7, do _adm_linha7_fim ao _adm_linha7_comeco
// ALT-030: a linha 7 virou um trilho só (railwayTrack164); 189, 193 e 195 não existem mais
return new RailwayTrack[] {
    main().railwayTrack164
};
/*ALCODEEND*/}

double get_distancia_entrada_linha7()
{/*ALCODESTART::1792520000102*/
// ALT-026: distância desde o início da railwayTrack164 até a cabeça do lote que vai entrar
double base  = main()._adm_linha7_fim.getOffset(METER);
double extra = Math.max(0, vagoes_entrando_linha7 - 1) * main().TAMANHO_VAGAO_GRAO;

traceln("get_distancia_entrada_linha7: " + (base + extra));
// ALT-031: avisa se a cabeça do lote passa do _adm_linha7_comeco
if (base + extra > main()._adm_linha7_comeco.getOffset(METER)) {
    traceln(String.format("[%.1f][ADM][entrada_l7] AVISO lote de %d vagoes entra com a cabeca alem do _adm_linha7_comeco (%.1f > %.1f)",
        time(), vagoes_entrando_linha7, base + extra, main()._adm_linha7_comeco.getOffset(METER)));
}
return base + extra;
/*ALCODEEND*/}

RailwayTrack get_track_entrada_linha7()
{/*ALCODESTART::1792520000103*/
// ALT-026: trilho da linha 7 onde cai a cabeça do lote que vai entrar (par do get_offset_entrada_linha7)
RailwayTrack[] linhas = get_trilhos_linha7();
double alvo = get_distancia_entrada_linha7();
double acum = 0;
for (RailwayTrack l : linhas) {
    double comp = l.length(METER);
    if (alvo < acum + comp) return l;
    acum += comp;
}

traceln("linha entrada: " + linhas[linhas.length - 1].getName());

return linhas[linhas.length - 1];
/*ALCODEEND*/}

boolean trem_na_linha7(Trem trem)
{/*ALCODESTART::1792550000006*/
// ALT-029: true se algum carro do trem ainda está em um dos trilhos da linha 7
for (RailwayTrack l : get_trilhos_linha7()) {
    if (trem.estaNaLinha(l)) return true;
}
return false;
/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1792502238886*/
int vagoes_linha_2000_emDescarga = main().getNumeroDeVagoes(lote_linha2000_emDescarga);
int vagoes_linha_2000_proximo 	 = main().getNumeroDeVagoes(lote_linha2000_proximo);
int vagoes_linha_moegaX		     = main().getNumeroDeVagoes(lote_moegaX);
int vagoes_linha_moegaV			 = main().getNumeroDeVagoes(lote_moegaV);

return (
	vagoes_linha_2000_emDescarga + 
	vagoes_linha_2000_proximo + 
	vagoes_linha_moegaX + 
	vagoes_linha_moegaV
);
/*ALCODEEND*/}

int _getVagoesVazios()
{/*ALCODESTART::1792502397682*/
int vagoes_linha_07 	 = main().getTamanhoDoTrem(lote_linha_7);

return (vagoes_linha_07);
/*ALCODEEND*/}

double liberar_wait_rp_moegaX()
{/*ALCODESTART::1792570000014*/
// ALT-032: solta o lote da moega X que espera a moega ficar livre (ou espaço na linha 7)
if (wait_rp_moegaX.size() > 0) {
    wait_rp_moegaX.free(wait_rp_moegaX.get(0));
}
/*ALCODEEND*/}

double liberar_wait_rp_moegaV()
{/*ALCODESTART::1792570000015*/
// ALT-032: solta o lote da moega V que espera a moega ficar livre (ou espaço na linha 7)
if (wait_rp_moegaV.size() > 0) {
    wait_rp_moegaV.free(wait_rp_moegaV.get(0));
}
/*ALCODEEND*/}

boolean linha7_tem_espaco()
{/*ALCODESTART::1792570000016*/
// ALT-032: cabe mais 1 vagão na linha 7? Conta o lote, os vagões a caminho e os que estão em descarga.
// Durante o alívio não tira vagão da moega (ALT-031).
if (alivio_linha7_em_andamento) return false;
int total = main().getTamanhoDoTrem(lote_linha_7) + vagoes_pendentes_linha7 + vagoes_em_descarga;
return total < CAPACIDADE_LINHA_7;
/*ALCODEEND*/}

