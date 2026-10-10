Main main()
{/*ALCODESTART::1785421553067*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

int _selecionarTamanhoDoLote(Trem trem)
{/*ALCODESTART::1785424602465*/
Agent primeiroVagao = trem.getFirst();

int tamanhoVazios   	= (main().getTamanhoDoTrem(_loteVazios_a) + main().getTamanhoDoTrem(_loteVazios_b));
int capacidadeVazios 	= (CAPACIDADE_ALIVIO_A + CAPACIDADE_ALIVIO_B);
int tamanhoDesejado 	= (primeiroVagao instanceof Locomotiva) ? 7 : 6;
int tamanhoTrem 		= trem.size();

int vaziosNecessarios 	= (capacidadeVazios - tamanhoVazios);

if (vaziosNecessarios < Math.min(tamanhoDesejado, tamanhoTrem)){
	
	_tamanhoDoLote = vaziosNecessarios;
	
}else{

	_tamanhoDoLote = Math.min(tamanhoDesejado, tamanhoTrem);
	
}
/*ALCODEEND*/}

double _estaDisponivelAlivio()
{/*ALCODESTART::1785434221122*/

/*ALCODEEND*/}

double _estaDisponivelMoega()
{/*ALCODESTART::1785439461472*/
if(lote_descarga == null && !fazendoRechego){
	aguardaDescarga.unblock();
}else{
	aguardaDescarga.block();
}
/*ALCODEEND*/}

double necessitaDeAlivio()
{/*ALCODESTART::1785467478528*/
if (formouLoteMinimoDeAlivio() || ultimosVagoes()){
	
	fazendoAlivio = true;
	locoMnb.inject();
	
}

/*ALCODEEND*/}

int _contarVagoes(Trem trem)
{/*ALCODESTART::1785500525996*/
int contador = 0;

for (int i = 0; i < trem.size(); i++) {

    Agent vagao = trem.getCar(i);

    if (!(vagao instanceof Locomotiva)) {
        contador++;
    }
}

return contador;
/*ALCODEEND*/}

double _espacoRechegoDisponivel()
{/*ALCODESTART::1785520830479*/
if(_tamanhoLotePera > 0){

	if (_tamanhoLotePera <= valorNecessarioParaRechego && !colocandoLoteNaMoega){
	
		fazendoRechego = true;
		aguardaRechego.unblock();
	}
	else{
	
		fazendoRechego = false;
		aguardaRechego.block();
	}

}



/*ALCODEEND*/}

boolean _precisaDeRechego()
{/*ALCODESTART::1785524039803*/
if (_tamanhoLotePera <= valorNecessarioParaRechego ){

	return true;
	
}
else{

	return false;
}

/*ALCODEEND*/}

boolean formouLoteMinimoDeAlivio()
{/*ALCODESTART::1785526472325*/
int quantidadeVazios = (main().getTamanhoDoTrem(_loteVazios_a) + main().getTamanhoDoTrem(_loteVazios_b)); 
int capacidadeVazios = (CAPACIDADE_ALIVIO_A + CAPACIDADE_ALIVIO_B);

if (quantidadeVazios == capacidadeVazios){
	

	return true;
}
else{
	return false;
}
/*ALCODEEND*/}

boolean ultimosVagoes()
{/*ALCODESTART::1785526854908*/
boolean temVazios = (_loteVazios_a != null || _loteVazios_b != null);

if (lote_descarga  == null && loteNaPera  == null && temVazios){
	
	return true;
}
else{

	return false;
}
/*ALCODEEND*/}

boolean _esteÉoUltimoLote()
{/*ALCODESTART::1785527429663*/

boolean temVagoesNaMoega =  main().getTamanhoDoTrem(lote_descarga) > 0 ? true : false;
boolean temVagoesNaPera	 =  main().getTamanhoDoTrem(loteNaPera) > 0 ? true : false;


if (temVagoesNaMoega || temVagoesNaPera){
	traceln("é o ultimo lote: !false");
 	return false;
 	
}else{

	traceln("é o ultimo lote: !true");
	return true;
}

/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1791650000004*/
// ALT-003: CoperXXI ainda não usa esse critério; só não recebe durante alívio
return !fazendoAlivio;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1791650000005*/
// ALT-003: vazios / capacidade das linhas de alívio (a + b)
int capacidade = CAPACIDADE_ALIVIO_A + CAPACIDADE_ALIVIO_B;
if (capacidade <= 0) return 0;
return (float) _getVagosVazios() / capacidade;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1791650000006*/
return "pst";
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1791650000007*/
return produto_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1791650000008*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1791650000009*/
// ALT-003: o CoperXXI ainda faz alívio próprio (locoMnb) e não está em terminais_pst do GestorDeAlivios
error("CoperXXI.takeAlivio chamado, mas o CoperXXI ainda não está integrado ao GestorDeAlivios");
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1791650000010*/
return 2;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1791650000011*/
// ALT-003: provisório — revisar na integração com o GestorDeAlivios
return main().posAlivio_coperXII_a;
/*ALCODEEND*/}

ArrayList<Faixa> getPrimeiroAlivioFaixas()
{/*ALCODESTART::1791650000012*/
// ALT-003: provisório — sem faixas até integrar com o GestorDeAlivios
return new ArrayList<Faixa>();
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1791650000013*/
// ALT-003: provisório — sem faixas até integrar com o GestorDeAlivios
return new ArrayList<Faixa>();
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791650000014*/
// ALT-003: provisório — alívio do CoperXXI ainda é próprio (leva tudo das linhas a e b)
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791650000015*/
return _getVagosVazios();
/*ALCODEEND*/}

int _getVagosVazios()
{/*ALCODESTART::1791650000016*/
return main().getTamanhoDoTrem(_loteVazios_a) + main().getTamanhoDoTrem(_loteVazios_b);
/*ALCODEEND*/}

