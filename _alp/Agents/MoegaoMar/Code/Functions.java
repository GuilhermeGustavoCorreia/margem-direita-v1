Main main()
{/*ALCODESTART::1785766512897*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

double _estaDisponivelMoega()
{/*ALCODESTART::1785778694182*/
boolean criteriosParaDescarga = (!movendoParaMoega && !fazendoAlivio && !fazendoRechego);


if(lote_descarga == null && criteriosParaDescarga){// && !fazendoRechego){
	aguardaDescarga.unblock();
}else{
	aguardaDescarga.block();
}
/*ALCODEEND*/}

boolean _terminouManobra_L3(Trem trem)
{/*ALCODESTART::1785853302370*/
int vgsDesacoplar = (main().getTamanhoDoTrem((Trem) trem) - _CAPACIDADE_ALIVIO_L3);

if (vgsDesacoplar > 0){
	return false;
}else{
	return true;
}




/*ALCODEEND*/}

boolean _terminouManobra_L4(Trem trem)
{/*ALCODESTART::1785857693405*/
int vgsDesacoplar = (main().getTamanhoDoTrem((Trem) trem) - _CAPACIDADE_ALIVIO_L4);

if (vgsDesacoplar > 0){
	return false;
}else{
	return true;
}




/*ALCODEEND*/}

boolean _terminouManobra_meio(Trem trem)
{/*ALCODESTART::1785858265243*/
int vgsDesacoplar = (main().getTamanhoDoTrem((Trem) trem) - _CAPACIDADE_ALIVIO_MEIO);

if (vgsDesacoplar > 0){
	return false;
}else{
	return true;
}




/*ALCODEEND*/}

double _atualizaLiberarAlivio()
{/*ALCODESTART::1785860546999*/
boolean criteriosParaAliviar = (!fazendoAlivioVazios && !movendoParaMoega);

int tamanhoLote_L3 	 = main().getTamanhoDoTrem(_loteVazios_L3);
int tamanhoLote_L4 	 = main().getTamanhoDoTrem(_loteVazios_L4);
int tamanhoLote_MEIO = main().getTamanhoDoTrem(_loteVazios_meio);

int capacidadeVazios = (_CAPACIDADE_ALIVIO_L3 + _CAPACIDADE_ALIVIO_L4 + _CAPACIDADE_ALIVIO_MEIO);

int totaisDeVazios   = (tamanhoLote_L3 + tamanhoLote_L4 + tamanhoLote_MEIO);

if (totaisDeVazios >= capacidadeVazios){

	liberarAlivio.block();

}else{

	if (criteriosParaAliviar == true){

		liberarAlivio.unblock();
		
	}else{

		liberarAlivio.block();
	}

}
/*ALCODEEND*/}

int _selecionarTamanhoDoLote(Trem trem)
{/*ALCODESTART::1785868637521*/
Agent primeiroVagao = trem.getFirst();

int tamanhoVazios   	= (main().getTamanhoDoTrem(_loteVazios_L3) + main().getTamanhoDoTrem(_loteVazios_L4) + main().getTamanhoDoTrem(_loteVazios_meio));
int capacidadeVazios 	= (_CAPACIDADE_ALIVIO_L3 + _CAPACIDADE_ALIVIO_L4 + _CAPACIDADE_ALIVIO_MEIO);
int tamanhoDesejado 	= (primeiroVagao instanceof Locomotiva) ? 7 : 6;
int tamanhoTrem 		= trem.size();

int vaziosNecessarios 	= (capacidadeVazios - tamanhoVazios);

if (vaziosNecessarios < Math.min(tamanhoDesejado, tamanhoTrem)){

	return vaziosNecessarios;

}else{

	return  Math.min(tamanhoDesejado, tamanhoTrem);

}
/*ALCODEEND*/}

double _espacoRechegoDisponivel()
{/*ALCODESTART::1785883803706*/
boolean criteriosParaRechego = (!movendoParaMoega && !fazendoAlivio && !fazendoAlivioVazios);


if(_tamanhoLotePera > 0){

	if (_tamanhoLotePera <= valorNecessarioParaRechego && criteriosParaRechego){
	
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
{/*ALCODESTART::1785883803708*/
if (_tamanhoLotePera <= valorNecessarioParaRechego ){

	return true;
	
}
else{

	return false;
}

/*ALCODEEND*/}

int _contarVagoes(Trem trem)
{/*ALCODESTART::1785893121641*/
int contador = 0;

for (int i = 0; i < trem.size(); i++) {

    Agent vagao = trem.getCar(i);

    if (!(vagao instanceof Locomotiva)) {
        contador++;
    }
}

return contador;
/*ALCODEEND*/}

double necessitaDeAlivio()
{/*ALCODESTART::1785893828681*/
boolean criteriosAlivioVazios = (!fazendoAlivio && !fazendoAlivioVazios);

if (criteriosAlivioVazios){
	
	if (formouLoteMinimoDeAlivio() || _acabouOsVagoesCarregados() ){
		
		fazendoAlivioVazios = true;
		locoMnb.inject();
		
	}
	
}
/*ALCODEEND*/}

boolean formouLoteMinimoDeAlivio()
{/*ALCODESTART::1785937194055*/
int vazios_Linha03 	 = main().getTamanhoDoTrem(_loteVazios_L3);
int vazios_Linha04 	 = main().getTamanhoDoTrem(_loteVazios_L4);
int vazios_LinhaMeio = main().getTamanhoDoTrem(_loteVazios_meio);

int totaisDeVazios = (vazios_Linha03 + vazios_Linha04 + vazios_LinhaMeio);

if (totaisDeVazios >= _LOTE_MINIMO_ALIVIO && fazendoAlivioVazios == false) {
	
	return true;
}
else {
	
	return false;
}
/*ALCODEEND*/}

boolean precisaAlivio(Trem trem)
{/*ALCODESTART::1785938897566*/
int totalVazios = (main().getTamanhoDoTrem(_loteVazios_L3) + main().getTamanhoDoTrem(_loteVazios_L4) + main().getTamanhoDoTrem(_loteVazios_meio));

int vazios_linhaMar_MEIO = main().getTamanhoDoTrem(_loteVazios_meio);
int vazios_linhaMar_L4 	 = main().getTamanhoDoTrem(_loteVazios_L4);
int vazios_linhaMar_L3 	 = main().getTamanhoDoTrem(_loteVazios_L3);

boolean lote_completo_L4   = ( vazios_linhaMar_MEIO >= _CAPACIDADE_ALIVIO_L4   );
boolean lote_completo_L3   = ( vazios_linhaMar_L4   >= _CAPACIDADE_ALIVIO_L4   );
boolean lote_completo_MEIO = ( vazios_linhaMar_L3   >= _CAPACIDADE_ALIVIO_MEIO );

//Começando um alivio novo
if (!fazendoAlivioVazios){
	
	//chegou no lote minimo
	if (totalVazios >= _LOTE_MINIMO_ALIVIO){
	
		return true;
	
	//chegou no ultimo lote	
	}else if(_éUltimoLote(trem)){
	
		return true;
	
	}else{
		
		return false;
	
	}
	
}else{
	//aqui é para o caso de CONTINUAR O ALIVIO QUE ESTA ACONTECENDO fazendoAlivioVazios = TRUE
	//SE NAO ELE PARA O ALIVIO NO MEIO
	if (lote_completo_L4 || lote_completo_L3 || lote_completo_MEIO ){
	
		return true;
		
	}else if(_éUltimoLote(trem) && totalVazios > 0){
	
		return true;
	
	}else{
		
		return false;
	
	}
}


/*ALCODEEND*/}

boolean verificarVgsLinha03()
{/*ALCODESTART::1785939159984*/
int vazios_linhaMar_L3 = main().getTamanhoDoTrem(_loteVazios_L3);
int vazios_linhaMar_L4 = main().getTamanhoDoTrem(_loteVazios_L4);

if (
	(vazios_linhaMar_L3 > vazios_linhaMar_L4) || 
	(vazios_linhaMar_L3 == _CAPACIDADE_ALIVIO_L3)
){

	return true;
	
}else{

	return false;
	
}

/*ALCODEEND*/}

boolean verificarVgsLinha04()
{/*ALCODESTART::1785945695510*/
int vazios_linhaMar_MEIO = main().getTamanhoDoTrem(_loteVazios_meio);
int vazios_linhaMar_L4 	 = main().getTamanhoDoTrem(_loteVazios_L4);

if (
	(vazios_linhaMar_MEIO < vazios_linhaMar_L4) || 
	(vazios_linhaMar_L4 == _CAPACIDADE_ALIVIO_L4)
){

	return true;
	
}else{

	return false;
	
}

/*ALCODEEND*/}

boolean _éUltimoLote(Trem trem)
{/*ALCODESTART::1785972407335*/
Boolean temVagoesNaMoega 		=  main().getTamanhoDoTrem(lote_descarga) > 0 ? true : false;
Boolean temCarregadosNaPera		= main().getTamanhoDoTrem(lote_pera) > 0 ? true : false;

if (temVagoesNaMoega || temCarregadosNaPera){

 	return false;
 	
}else{

	return true;
	
}
/*ALCODEEND*/}

boolean _acabouOsVagoesCarregados()
{/*ALCODESTART::1785973265350*/
int vazios_Linha03 	 = main().getTamanhoDoTrem(_loteVazios_L3);
int vazios_Linha04 	 = main().getTamanhoDoTrem(_loteVazios_L4);
int vazios_LinhaMeio = main().getTamanhoDoTrem(_loteVazios_meio);

int totaisDeVazios   = (vazios_Linha03 + vazios_Linha04 + vazios_LinhaMeio);

int carregadosMoega  = main().getTamanhoDoTrem(lote_descarga);
int carregadosPera   = _tamanhoLotePera;

int totaisCarregados = (carregadosMoega + carregadosPera);

if (totaisCarregados == 0 && totaisDeVazios > 0){

	return true;
}
else{

	return false;
}
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1787663122659*/
return "PST";
/*ALCODEEND*/}

List<Faixa>  getPrimeiroAlivioFaixas()
{/*ALCODESTART::1790637996004*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790791109466*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224441707*/
return 0;
/*ALCODEEND*/}

