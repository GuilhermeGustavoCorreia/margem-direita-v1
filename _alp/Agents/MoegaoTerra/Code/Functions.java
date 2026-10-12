Main main()
{/*ALCODESTART::1786037742464*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

int _getTamanhoDoLote(Trem trem)
{/*ALCODESTART::1786042418713*/
Agent primeiroVagao = trem.getFirst();

int tamanhoVazios   	= (main().getTamanhoDoTrem(lote_vazios_L1) + main().getTamanhoDoTrem(lote_vazios_L2) + main().getTamanhoDoTrem(lote_vazios_terra));
int capacidadeVazios 	= (CAPACIDADE_ALIVIO_L1 + CAPACIDADE_ALIVIO_L2 + CAPACIDADE_ALIVIO_TERRA);
int tamanhoDesejado 	= (primeiroVagao instanceof Locomotiva) ? (1 + _CAPACIDADE_LOTE_MOEGA) : _CAPACIDADE_LOTE_MOEGA;
int tamanhoTrem 		= trem.size();

int vaziosNecessarios 	= (capacidadeVazios - tamanhoVazios);

if (vaziosNecessarios < Math.min(tamanhoDesejado, tamanhoTrem)){

	return vaziosNecessarios;

}else{

	return  Math.min(tamanhoDesejado, tamanhoTrem);

}
/*ALCODEEND*/}

double atualizar_aguardaRechego()
{/*ALCODESTART::1786046375231*/

/*ALCODEEND*/}

double atualizar_aguardaMoega()
{/*ALCODESTART::1786046404095*/
boolean critetiosMoega = (!executando_descarga && !executando_alivioMoega);

if (critetiosMoega){
	
	aguardaMoega.unblock();
	
}else{

	aguardaMoega.block();

}

/*ALCODEEND*/}

double atualizar_aguardaAlivioMoega()
{/*ALCODESTART::1786046421421*/
boolean critetiosAlivioMoega = (!executando_alivioMoega && !executando_alivioVazios);

if (critetiosAlivioMoega){

	
	aguardaAlivioMoega.unblock();
	
}else{

	aguardaAlivioMoega.block();

}
/*ALCODEEND*/}

double atualizar_aguardaAlivioVazios()
{/*ALCODESTART::1786046434662*/
boolean critetiosAlivioVazios = (!executando_alivioMoega);

if (critetiosAlivioVazios){
	
	aguardaAlivioVazios.unblock();
	
}else{

	aguardaAlivioVazios.block();

}
/*ALCODEEND*/}

boolean _precisaAcumularMaisVagoes(int capacidade_linha,Trem lote_naLinha)
{/*ALCODESTART::1786106237483*/

int lote_atual 					= main().getTamanhoDoTrem(lote_naLinha);
Boolean temVagoesNaMoega 		= main().getTamanhoDoTrem(lote_moega) > 0 ? true : false;
Boolean temCarregadosNaPera		= main().getTamanhoDoTrem(lote_rechego) > 0 ? true : false;


//se nao couber mais vagoes ele nao precisa mais acumular
if (lote_atual >= capacidade_linha){
	return false;
}

//se acabou a descarga, pode nao precisa mais acumular também
else if (!temVagoesNaMoega && !temCarregadosNaPera){
	return false;
}

else{
	return true;
}


/*ALCODEEND*/}

int getTotalDeVazios()
{/*ALCODESTART::1786123767687*/
int tamanhoVazios = (main().getTamanhoDoTrem(lote_vazios_L1) + main().getTamanhoDoTrem(lote_vazios_L2) + main().getTamanhoDoTrem(lote_vazios_terra));
return tamanhoVazios;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1786384110372*/
int tamanhoVazios = (
	main().getTamanhoDoTrem(lote_vazios_L1) + 
	main().getTamanhoDoTrem(lote_vazios_L2) + 
	main().getTamanhoDoTrem(lote_vazios_terra)
);

return ( (float) tamanhoVazios / RECOMENDACAO_ALIVIO_VAZIOS );
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1786384321896*/
return true;
/*ALCODEEND*/}

String getAssociadaVazios()
{/*ALCODESTART::1786392550905*/
if (lote_vazios_terra != null) {

    return lote_vazios_terra.getAssociada();
    
} else if (lote_vazios_L2 != null) {

    return lote_vazios_L2.getAssociada();
    
} else if (lote_vazios_L1 != null) {

    return lote_vazios_L1.getAssociada();
    
} else {

    return null;

}
/*ALCODEEND*/}

String getSegmentoVazios()
{/*ALCODESTART::1786392899580*/
if (lote_vazios_terra != null) {

    return lote_vazios_terra.getSegmento();
    
} else if (lote_vazios_L2 != null) {

    return lote_vazios_L2.getSegmento();
    
} else if (lote_vazios_L1 != null) {

    return lote_vazios_L1.getSegmento();
    
} else {

    return null;

}
/*ALCODEEND*/}

double takeAlivio(Agent agent)
{/*ALCODESTART::1786469051530*/
aliviarMoega.take(agent);
/*ALCODEEND*/}

boolean _podeAguardaAlivio()
{/*ALCODESTART::1786632752330*/
if ( getOverloadVazios() >= 1){
	return true;
}else{
	return false;
	}
/*ALCODEEND*/}

double _setAcumular_L1()
{/*ALCODESTART::1786634743574*/
_vaiAliviar_L1 = false;

if(esperar_L1.size() > 0) esperar_L1.free(esperar_L1.get(0));
/*ALCODEEND*/}

double _setAliviar_L1()
{/*ALCODESTART::1786635407992*/
_vaiAliviar_L1 = true;

if(esperar_L1.size() > 0) esperar_L1.free(esperar_L1.get(0));
/*ALCODEEND*/}

double _setAcumular_L2()
{/*ALCODESTART::1786635943248*/
_vaiAliviar_L2 = false;

if(esperar_L2.size() > 0) esperar_L2.free(esperar_L2.get(0));
/*ALCODEEND*/}

double _setAliviar_L2()
{/*ALCODESTART::1786635943250*/
_vaiAliviar_L2 = true;

if(esperar_L2.size() > 0) esperar_L2.free(esperar_L2.get(0));
/*ALCODEEND*/}

double _setAcumular_terra()
{/*ALCODESTART::1786636364190*/
_vaiAliviar_meio = false;

if(esperar_meio.size() > 0) esperar_meio.free(esperar_meio.get(0));
/*ALCODEEND*/}

double _setAliviar_terra()
{/*ALCODESTART::1786636364192*/
_vaiAliviar_meio = true;

if(esperar_meio.size() > 0) esperar_meio.free(esperar_meio.get(0));
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1787599595927*/
return main().pos_locoAlivio_PMC;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1787599595931*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1787663149667*/
return "PST";
/*ALCODEEND*/}

ArrayList<Faixa>  get_faixas_primeiro_alivio()
{/*ALCODESTART::1790638014122*/
// a faixa de criação da loco (faixa_via1_intermar_d, onde fica pos_locoAlivio_PST)
// é apreendida no seize_faixa_criacao_pst do gestor; aqui só as faixas seguintes até o terminal
return new ArrayList<Faixa>();
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790777524068*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790777524070*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790791116856*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790791459146*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791223029307*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224520511*/
return 0;
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1791700000009*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1791700000010*/
return 1;
/*ALCODEEND*/}

