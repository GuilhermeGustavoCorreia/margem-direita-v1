Main main()
{/*ALCODESTART::1786651264885*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1787231640652*/
int tamanhoVazios = (
	main.getTamanhoDoTrem(lote_linhaMar) +
	main.getTamanhoDoTrem(lote_linhaTerra)
);

int capacidadeVazios = (
	CAPACIDADE_LINHA_MAR +
	CAPACIDADE_LINHA_TERRA
);

if (tamanhoVazios >= capacidadeVazios) {
	return (float) 1.1;
} else {
	return (float) 0.0;
}
/*ALCODEEND*/}

double takeAlivio(Agent agent)
{/*ALCODESTART::1787242578330*/
alivioVazios.take(agent);
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1787242590653*/
return true;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1787599296674*/
return main().pos_circ_alivio_tgrao;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1787599350404*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1787663062252*/
return "PMC";
/*ALCODEEND*/}

ArrayList<Faixa>  getPrimeiroAlivioFaixas()
{/*ALCODESTART::1790637932726*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790777501574*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790777501576*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790791230610*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790791430567*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791223058967*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224533452*/
return 0;
/*ALCODEEND*/}

