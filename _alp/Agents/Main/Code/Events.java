void verificar_disponibilidadeDeAtendimento()
{/*ALCODESTART::1783080519428*/
if(wait.size() > 0){

	for (int i = 0; i < wait.size(); i++){

		String 	moegaDestino 		= wait.get(i).getPrimeiroVagao().moegaDestino;
		Boolean	atendimentoLivre 	= getVagoesLinhaAtendimento() > 0 ? false : true; 

		ITerminalMoega agenteMoega	= getTerminalAgent(moegaDestino);

		Boolean linhaEncosteLivre 	= agenteMoega.podeReceberNovoLote();

		if (linhaEncosteLivre && atendimentoLivre) wait.free(wait.get(i));

	}

}
/*ALCODEEND*/}

