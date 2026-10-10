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

