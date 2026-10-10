void pegarVagaoPraDescarga()
{/*ALCODESTART::1784895423253*/
if (waitLotesParaDescarga.size() > 0 && _loteEmDescarga == null && fazendoAlivio == false) {
	
    // Percorre a lista de prioridades, na ordem definida em _prioridadeDeLotesDescarga
    for (String tipo : _prioridadeDeLotesDescarga) {

            // Para cada prioridade, procura no Wait o primeiro trem com esse tipoDeLote
        for (int i = 0; i < waitLotesParaDescarga.size(); i++) {

			Agent ag = waitLotesParaDescarga.get(i);
					Trem trem = (Trem) ag;

      
            // Achou um trem com o tipo de lote da prioridade atual
            if (tipo.equals(trem.tipoDeLote)) {
                 waitLotesParaDescarga.free(ag); // libera esse trem, manda pro "out" do Wait
                return; // libera so um trem por chamada
            }
        }
    }

    traceln("nenhum trem na fila corresponde a alguma prioridade da lista");
}
/*ALCODEEND*/}

void verificaSePodeAliviar()
{/*ALCODESTART::1784919235864*/
_estaDisponivelAlivio();
/*ALCODEEND*/}

