Main main()
{/*ALCODESTART::1784662852326*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1784663408134*/
if (__existeTremManobrando() || __getVagoes27Carregados() > 0) return false;
else return true;
/*ALCODEEND*/}

double _distanciaDoPontoAoTrem(PositionOnTrack  ponto,Trem  trem,boolean  medirAteFrente)
{/*ALCODESTART::1784731358420*/
Agent vagaoReferencia = medirAteFrente ? trem.getFirst() : trem.getLast();

if (vagaoReferencia == null) {
    error("Trem vazio ao calcular distância.");
    return 0;
}

try {
    java.lang.reflect.Method metodoGetTrack = vagaoReferencia.getClass().getMethod("getTrack", boolean.class);
    java.lang.reflect.Method metodoGetOffset = vagaoReferencia.getClass().getMethod("getOffset", boolean.class);

    RailwayTrack trackVagao = (RailwayTrack) metodoGetTrack.invoke(vagaoReferencia, medirAteFrente);
    double offsetVagao = (double) metodoGetOffset.invoke(vagaoReferencia, medirAteFrente);

    RailwayTrack trackPonto = ponto.getTrack();
    double offsetPonto = ponto.getOffset(METER);

    if (trackVagao != trackPonto) {
        error("Ponto e vagão de referência não estão na mesma track.");
        return 0;
    }

    return Math.abs(offsetVagao - offsetPonto);

} catch (Exception e) {
    error("Erro ao calcular distância do ponto ao trem: " + e.getMessage());
    return 0;
}
/*ALCODEEND*/}

List<RailwayTrack> getLinhasMoega()
{/*ALCODESTART::1784838211554*/
return linhasMoega;
/*ALCODEEND*/}

double _estaDisponivelAlivio()
{/*ALCODESTART::1785153298380*/
int vaziosMar_a 	= main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_a);
int vaziosMar_b 	= main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_b);

int vaziosTerra_a 	= main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_a);
int vaziosTerra_b 	= main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_b);

int totaisVazios 	= (vaziosMar_a + vaziosMar_b + vaziosTerra_a + vaziosTerra_b);

int capacidadeTotal = (_CAPACIDADE_LINHA_MAR_A + _CAPACIDADE_LINHA_MAR_B + _CAPACIDADE_LINHA_TERRA_A + _CAPACIDADE_LINHA_TERRA_B);

if (capacidadeTotal > totaisVazios && !fazendoAlivio) 	aguardaAlivio.unblock();
else													aguardaAlivio.block();

/*ALCODEEND*/}

boolean precisaAlivio()
{/*ALCODESTART::1785169606451*/
if ((
	
	main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_a) + 
	main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_b) + 
	main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_a) + 
	main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_b) 
	
) >= _LOTE_MINIMO_ALIVIO) 	return true;
else						return false;
/*ALCODEEND*/}

boolean verificarVgsLinhaMar()
{/*ALCODESTART::1785170115641*/
int vazios_linhaMar_a = main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_a);
int vazios_linhaMar_b = main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_b);

int vazios_linhaTerra_a = main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_a);
int vazios_linhaTerra_b = main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_b);


int vazios_linhaMar 	= ( vazios_linhaMar_a 	+ vazios_linhaMar_b   );
int vazios_linhaTerra 	= ( vazios_linhaTerra_a + vazios_linhaTerra_b );


//true  aliviar mar
//false aliviar terra

//casos para quando for linha mar
if (vazios_linhaMar > vazios_linhaTerra){
	return true;
}else{
	return false;
}

/*ALCODEEND*/}

boolean __existeTremManobrando()
{/*ALCODESTART::1785179147096*/
// Percorre cada track que faz parte das linhas da moega deste terminal (CoperXI)
for (RailwayTrack track : linhasMoega) {

    // Pega todos os trens (ou partes de trem) que estao fisicamente sobre essa track
    for (Agent trem : track.getTrains()) {

        // Le o status atual do trem (String: pode ser "manobrando" ou outros valores)
        String status = ((Trem) trem).status;

        // Se esse trem estiver com status "manobrando", o terminal
        
        // NAO pode receber novo lote agora - nao precisa checar o resto       
        
        if ("manobrando".equals(status)) {
           // traceln("resultado: false (existe trem manobrando)");
            return true;
        }
             
    }
}

return false;
/*ALCODEEND*/}

int __getVagoes27Carregados()
{/*ALCODESTART::1785179173136*/





return (

main().getTamanhoDoTrem(_loteDeCarregados_HidroviasMar) +
main().getTamanhoDoTrem(_loteDeCarregados_HidroviasTerra) +
main().getTamanhoDoTrem(_loteDeCarregados_HidroviasMeio) +

main().getTamanhoDoTrem(_loteDeCarregados_linhaBomba1) +
main().getTamanhoDoTrem(_loteDeCarregados_linhaBomba)

);

/*ALCODEEND*/}

boolean _linhaTerra_temMaisVagoes()
{/*ALCODESTART::1785181445327*/
//false -> terra
//true  -> mar

int totalMar   = main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_a)
               + main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_b);

int totalTerra = main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_a)
               + main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_b);

int capacidadeTerra = _CAPACIDADE_LINHA_TERRA_A + _CAPACIDADE_LINHA_TERRA_B;
int capacidadeMar   = _CAPACIDADE_LINHA_MAR_A   + _CAPACIDADE_LINHA_MAR_B;

if      (totalTerra > 0 && totalTerra < capacidadeTerra)	return false;
else if (totalMar != 0  && totalMar   >= capacidadeMar)		return false;
else                                                        return true;


/*ALCODEEND*/}

boolean __existeTremComDestino(String moegaDestino)
{/*ALCODESTART::1785246228648*/
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

int _getVagosLinhaMoega()
{/*ALCODESTART::1785247647217*/
int vagoes_MoegaMar 			= main().getTamanhoDoTrem(_loteDeCarregados_MoegaMar);
int vagoes_HidroviasMar 		= main().getTamanhoDoTrem(_loteDeCarregados_HidroviasMar);

int vagoes_MoegaMeio 			= main().getTamanhoDoTrem(_loteDeCarregados_MoegaMeio);
int vagoes_MoegaHidroviasMeio 	= main().getTamanhoDoTrem(_loteDeCarregados_HidroviasMeio);

int vagoes_BarracaoTerra 		= main().getTamanhoDoTrem(_loteDeCarregados_BarracaoTerra);
int vagoes_HidroviasTerra		= main().getTamanhoDoTrem(_loteDeCarregados_HidroviasTerra);

int vagoes_linhaBomba 			= main().getTamanhoDoTrem(_loteDeCarregados_linhaBomba);
int vagoes_linhaBomba1			= main().getTamanhoDoTrem(_loteDeCarregados_linhaBomba1);



int vagoesMar 	= (vagoes_MoegaMar 		+ vagoes_HidroviasMar);
int vagoesMeio 	= (vagoes_MoegaMeio 	+ vagoes_MoegaHidroviasMeio);
int vagoesTerra = (vagoes_BarracaoTerra + vagoes_HidroviasTerra);
int vagoesBomba = (vagoes_linhaBomba 	+ vagoes_linhaBomba1);

int total = vagoesMar + vagoesMeio + vagoesTerra + vagoesBomba;


return total;
/*ALCODEEND*/}

boolean _esteÉoUltimoLote()
{/*ALCODESTART::1785331773710*/
//nao tem mais carregados na linha de circulação
//nao tem mais carregados na linha da moega 
//nao tem mais descarregando.

Boolean temVagoesNaMoega 				=  main().getTamanhoDoTrem(_loteEmDescarga) > 0 ? true : false;
Boolean temCarregadosLinhaCirculacao	= __existeTremComDestino("coper-XI");
Boolean temCarregadosLinhaMoega			= _getVagosLinhaMoega() > 0 ? true : false;

if (temVagoesNaMoega || temCarregadosLinhaCirculacao || temCarregadosLinhaMoega){

 	return false;
 	
}else{

	return true;
	
}

/*ALCODEEND*/}

double _atualizarFazendoAlivio()
{/*ALCODESTART::1785348747193*/
int vazios_linhaMar_a = main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_a);
int vazios_linhaMar_b = main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_b);

int vazios_linhaTerra_a = main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_a);
int vazios_linhaTerra_b = main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_b);


int vazios_linhaMar 	= ( vazios_linhaMar_a 	+ vazios_linhaMar_b   );
int vazios_linhaTerra 	= ( vazios_linhaTerra_a + vazios_linhaTerra_b );

if (vazios_linhaMar + vazios_linhaTerra == 0) {
	fazendoUltimoAlivio = false;
}
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1786130034850*/
boolean moega_esta_desocupada     = rp_manobrando_carregados.idle() > 0;
boolean ultimos_vazios_no_terminal = (_getVagosLinhaMoega() == 0 && get_quantidade_vazios() > 0);

//ultimos vagoes
if (moega_esta_desocupada && ultimos_vazios_no_terminal) {
    return (float) 2.0;
}

if (QUANTIDADE_PARA_ALIVIAR <= 0) return 0;
return (float) _getVagosVazios() / QUANTIDADE_PARA_ALIVIAR;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1787599536529*/
return main().pos_alivio_coperXI;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1787599536533*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1787663087525*/
return "pst";
/*ALCODEEND*/}

boolean pode_rechegar_loteDeCarregados_linhaVerde()
{/*ALCODESTART::1789176244746*/
int vagoes_linha_moega = _getVagosLinhaMoega();
int vagoes_linha_verde = main().getTamanhoDoTrem(_loteDeCarregados_linhaVerde);

int todos_os_vagoes_carregados = ( vagoes_linha_moega + vagoes_linha_verde );

boolean tem_vagoes_na_linha_verde = vagoes_linha_verde > 0; 

if (tem_vagoes_na_linha_verde){

	if (todos_os_vagoes_carregados <= capacidade_linhas_moega){

		return true;	
	
	}

}
return false;

/*ALCODEEND*/}

int _getVagosVazios()
{/*ALCODESTART::1789656075382*/
int vagoes_linhaMar_a 	= main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_a);
int vagoes_linhaMar_b 	= main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_b);

int vagoes_linhaTerra_a = main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_a);
int vagoes_linhaTerra_b = main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_b);



return (vagoes_linhaMar_a + vagoes_linhaMar_b + vagoes_linhaTerra_a + vagoes_linhaTerra_b);

/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1789656660028*/
for (int i = 0; i < trem.size(); i++) {
        Agent carro = trem.getCar(i); // confere o nome exato do método com Ctrl+Espaço

        if (carro instanceof Vagao) {
            Vagao vagao = (Vagao) carro;
            ferrovia_em_atendimento = vagao.ferrovia;
            produto_em_atendimento = vagao.segmento;
            return; // achou o primeiro vagão, encerra
        }
    }

    traceln("erro: trem não tem nenhum vagão");
/*ALCODEEND*/}

String getResumoLotes()
{/*ALCODESTART::1789756973437*/
StringBuilder sb = new StringBuilder();

sb.append("BarracaoTerra: "  + main().getTamanhoDoTrem(_loteDeCarregados_BarracaoTerra)  + "\n");
sb.append("HidroviasMar: "   + main().getTamanhoDoTrem(_loteDeCarregados_HidroviasMar)   + "\n");
sb.append("HidroviasMeio: "  + main().getTamanhoDoTrem(_loteDeCarregados_HidroviasMeio)  + "\n");
sb.append("HidroviasTerra: " + main().getTamanhoDoTrem(_loteDeCarregados_HidroviasTerra) + "\n");
sb.append("\n");
sb.append("linhaBomba: "     + main().getTamanhoDoTrem(_loteDeCarregados_linhaBomba)     + "\n");
sb.append("linhaBomba1: "    + main().getTamanhoDoTrem(_loteDeCarregados_linhaBomba1)    + "\n");
sb.append("\n");
sb.append("MoegaMar: "       + main().getTamanhoDoTrem(_loteDeCarregados_MoegaMar)       + "\n");
sb.append("MoegaMeio: "      + main().getTamanhoDoTrem(_loteDeCarregados_MoegaMeio)      + "\n");
sb.append("\n");
sb.append("linhaVerde: "     + main().getTamanhoDoTrem(_loteDeCarregados_linhaVerde)     + "\n");
sb.append("\n");
sb.append("\n");
sb.append("VaziosMar_a: "    + main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_a)         + "\n");
sb.append("VaziosMar_b: "    + main().getTamanhoDoTrem(_loteDeVazios_LinhaMar_b)         + "\n");
sb.append("VaziosTerra_a: "  + main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_a)       + "\n");
sb.append("VaziosTerra_b: "  + main().getTamanhoDoTrem(_loteDeVazios_LinhaTerra_b));
sb.append("\n");
//sb.append(String.format("%-16s%s",   "UltimoAlivio:",   bolinha(fazendoUltimoAlivio)));

return sb.toString();
/*ALCODEEND*/}

double atualiza_vaiDescarga()
{/*ALCODESTART::1789838899548*/
boolean aguardandoRechego = mnbCarregados_ocupar3.size() > 0;
boolean podeDescarregar   = (waitLotesParaDescarga.size() > 0 && _loteEmDescarga == null);

if (!aguardandoRechego && podeDescarregar) {

	condicao_vaiDescarga = true;
	pegar_um_lote_para_descarga();
	
}


/*ALCODEEND*/}

double pegar_um_lote_para_descarga()
{/*ALCODESTART::1789839646017*/
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

boolean atualiza_precisa_de_rechego()
{/*ALCODESTART::1790001945784*/
boolean precisa_de_rechego = (_getVagosLinhaMoega() <= 27);
boolean tem_lote_rechego   = (_loteDeCarregados_linhaVerde != null);

if (precisa_de_rechego && tem_lote_rechego) {
	
	traceln("vamos fazer rechego");
	locoFips_pst_mnb_coperXI.inject();
	
}

/*ALCODEEND*/}

boolean chamar_loco_manobra()
{/*ALCODESTART::1790002002350*/
return _getVagosLinhaMoega() <= 28;

/*ALCODEEND*/}

String getHoldContents()
{/*ALCODESTART::1790103990599*/
String result = "";
for (int i = 0; i < waitLotesParaDescarga.size(); i++) {
    Trem t = (Trem) waitLotesParaDescarga.get(i);
    result += t.getName() + " - " + t.tipoDeLote + "\n";
}
return result.isEmpty() ? "Vazio" : result;
/*ALCODEEND*/}

ArrayList<Faixa> getPrimeiroAlivioFaixas()
{/*ALCODESTART::1790637769491*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790777298555*/
return produto_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790777299300*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790782150407*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790788494526*/
return new ArrayList<>(Arrays.asList(
	main().gestorDeCirculacao.faixa_via1_intermar_d, 
	main().gestorDeCirculacao.faixa_via1_intermar_c
));
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791222968477*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224461055*/
return _getVagosVazios();
/*ALCODEEND*/}

double liberar_wait_lote_linhaMar_b()
{/*ALCODESTART::1791463833995*/
// libera o primeiro que entrou
if (wait_blocoMar_b.size() > 0) {
    wait_blocoMar_b.free(wait_blocoMar_b.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_linhaMar_a()
{/*ALCODESTART::1791464045586*/
// libera o primeiro que entrou
if (wait_blocoMar_a.size() > 0) {
    wait_blocoMar_a.free(wait_blocoMar_a.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_linhaTerra_a()
{/*ALCODESTART::1791464076171*/
// libera o primeiro que entrou
if (wait_blocoTerra_a.size() > 0) {
    wait_blocoTerra_a.free(wait_blocoTerra_a.get(0));
}
/*ALCODEEND*/}

double liberar_wait_lote_linhaTerra_b()
{/*ALCODESTART::1791464079013*/
// libera o primeiro que entrou
if (wait_blocoTerra_b.size() > 0) {
    wait_blocoTerra_b.free(wait_blocoTerra_b.get(0));
}
/*ALCODEEND*/}

