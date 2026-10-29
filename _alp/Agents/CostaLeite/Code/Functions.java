boolean podeReceberNovoLote()
{/*ALCODESTART::1783947241534*/
return true;
/*ALCODEEND*/}

Main main()
{/*ALCODESTART::1783960203072*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

GestorDeAlivios formacaoVazios()
{/*ALCODESTART::1784059840311*/
return main().gestorDeAlivios;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1786130046535*/
boolean moega_esta_desocupada     = rp_manobrando_carregados.idle() > 0;
boolean ultimos_vazios_no_terminal = (_getVagoesCarregados() == 0 && get_quantidade_vazios() > 0);

//ultimos vagoes
if (moega_esta_desocupada && ultimos_vazios_no_terminal) {
    return (float) 2.0;
}

if (QUANTIDADE_PARA_ALIVIAR <= 0) return 0;
return (float) _getVagoesVazios() / QUANTIDADE_PARA_ALIVIAR;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1787599492983*/
return 1;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1787663104148*/
return "pst";
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1787666908403*/
return main().pos_locoAlivio_PST;
/*ALCODEEND*/}

ArrayList<Faixa>  get_faixas_primeiro_alivio()
{/*ALCODESTART::1790637952305*/
// a faixa de criação da loco (faixa_via1_intermar_d, onde fica pos_locoAlivio_PST)
// é apreendida no seize_faixa_criacao_pst do gestor; aqui só as faixas seguintes até o terminal
return new ArrayList<Faixa>();
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790777481009*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790777481011*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790791087456*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790791485013*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791223008004*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224497483*/
// ALT-022
return _getVagoesVazios();
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1791700000007*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1791700000008*/
return 1;
/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1792083865464*/
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
{/*ALCODESTART::1792083865466*/
// ALT-022: vazios das linhas mar, meio e terra
return (
	main().getTamanhoDoTrem(_loteDeVazios_LinhaMar) +
	main().getTamanhoDoTrem(_loteDeVazios_LinhaMeio) +
	main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra)
);
/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1792083865468*/
// ALT-022: carregados que ainda estão nos lotes deixados nos encostes
return (
	main().getTamanhoDoTrem(lote_costaLeite_moegaMeio) +
	main().getTamanhoDoTrem(lote_costaLeite_linhaTerra) +
	main().getTamanhoDoTrem(lote_costaLeite_lateralMoega) +
	main().getTamanhoDoTrem(lote_costaLeite_linhaMar_a) +
	main().getTamanhoDoTrem(lote_costaLeite_linhaMar_b)
);
/*ALCODEEND*/}

double atualiza_vaiDescarga()
{/*ALCODESTART::1792088756450*/
boolean aguardandoRechego = false; // ALT-019: CostaLeite ainda não tem rechego (era mnbCarregados_ocupar3.size() > 0)
boolean podeDescarregar   = (waitLotesParaDescarga.size() > 0 && _loteEmDescarga == null);

if (!aguardandoRechego && podeDescarregar) {

	condicao_vaiDescarga = true;
	pegar_um_lote_para_descarga();
	
}


/*ALCODEEND*/}

double pegar_um_lote_para_descarga()
{/*ALCODESTART::1792088756452*/
for (String tipo : _prioridadeDeLotesDescarga) {

    for (int i = 0; i < waitLotesParaDescarga.size(); i++) {

		Agent ag  = waitLotesParaDescarga.get(i);
		Trem trem = (Trem) ag;

        if (tipo.equals(trem.tipoDeLote)) {

            waitLotesParaDescarga.free(ag); 
            return; 
        }
    }
}

/*ALCODEEND*/}

double liberar_wait_lote_linhaMar()
{/*ALCODESTART::1792091621467*/
// libera o primeiro que entrou
if (wait_blocoMar.size() > 0) {
    wait_blocoMar.free(wait_blocoMar.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_linhaMeio()
{/*ALCODESTART::1792094004077*/
// libera o primeiro que entrou
// ALT-021: era wait_blocoMar.free(...)
if (wait_blocoMeio.size() > 0) {
    wait_blocoMeio.free(wait_blocoMeio.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_linhaTerra()
{/*ALCODESTART::1792094493941*/
// libera o primeiro que entrou
// ALT-021: cópia da linha meio adaptada
if (wait_blocoTerra.size() > 0) {
    wait_blocoTerra.free(wait_blocoTerra.get(0));
}
/*ALCODEEND*/}

boolean __existeTremComDestino(String moegaDestino)
{/*ALCODESTART::1792151887908*/
if (main().wait.size() > 0 ) {

	for (int i = 0; i < main().wait.size(); i++) {
		
	    Trem train = (Trem) main().wait.get(i);
	    String moegaDestinoDoTrem = main().getDestinoDoTrem(train);
	
	    if (moegaDestino.equals(moegaDestinoDoTrem)) {
	        traceln("return true");
	        return true;
	    }
	}
}

return false;

/*ALCODEEND*/}

boolean precisaAlivio()
{/*ALCODESTART::1792152033194*/
// ALT-022: linhas mar, meio e terra da CostaLeite
return _getVagoesVazios() >= _LOTE_MINIMO_ALIVIO;
/*ALCODEEND*/}

boolean verificarVgsLinhaMar()
{/*ALCODESTART::1792152191803*/
// ALT-022: true quando a linha mar tem vazios (ordem fixa mar -> meio -> terra)
return main().getTamanhoDoTrem(_loteDeVazios_LinhaMar) > 0;
/*ALCODEEND*/}

