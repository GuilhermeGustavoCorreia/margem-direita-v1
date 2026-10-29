boolean pmc_colocarLocomotivas_alivioTerminal(ITerminalMoega moega)
{/*ALCODESTART::1787661399275*/
int locomotivas_disponiveis = (pmc_locomotivas_disponiveis);

boolean podeCriarLocomotiva = (
	locomotivas_disponiveis >= moega.getNumeroLocomotivasAlivio()
);

if (podeCriarLocomotiva){

	ITerminalMoega_destino		= moega;
	destinoLocomotivas 			= moega.get_pos_alivio();
	posicao_criar_locomotivas 	= PMC_POSICAO_CRIAR_LOCOMOTIVA;
	nr_locomotivas				= moega.getNumeroLocomotivasAlivio();
	
	locomotivaSource.inject();
	pmc_locomotivas_disponiveis = (locomotivas_disponiveis - nr_locomotivas);
	
	return true;

}else{
	
	return false;

}
/*ALCODEEND*/}

boolean pst_colocarLocomotivas_alivioTerminal(ITerminalMoega moega)
{/*ALCODESTART::1787661705146*/
int locomotivas_disponiveis = (pst_locomotivas_disponiveis);

boolean podeCriarLocomotiva = (
	locomotivas_disponiveis >= moega.getNumeroLocomotivasAlivio()
);

if (podeCriarLocomotiva){

	ITerminalMoega_destino		= moega;
	destinoLocomotivas 			= moega.get_pos_alivio();
	posicao_criar_locomotivas 	= PST_POSICAO_CRIAR_LOCOMOTIVA;
	nr_locomotivas				= moega.getNumeroLocomotivasAlivio();
		
	locomotivaSource.inject();
	pst_locomotivas_disponiveis = (locomotivas_disponiveis - nr_locomotivas);
	
	return true;

}else{
	
	return false;

}
/*ALCODEEND*/}

boolean pcx_colocarLocomotivas_alivioTerminal(ITerminalMoega moega)
{/*ALCODESTART::1787661723856*/
int locomotivas_disponiveis = (pcx_locomotivas_disponiveis);

boolean podeCriarLocomotiva = (
	locomotivas_disponiveis >= moega.getNumeroLocomotivasAlivio()
);

if (podeCriarLocomotiva){

	ITerminalMoega_destino		= moega;
	destinoLocomotivas 			= moega.get_pos_alivio();
	posicao_criar_locomotivas 	= PCX_POSICAO_CRIAR_LOCOMOTIVA;
	nr_locomotivas				= moega.getNumeroLocomotivasAlivio();
		
	locomotivaSource.inject();
	pcx_locomotivas_disponiveis = (locomotivas_disponiveis - nr_locomotivas);
	
	return true;

}else{
	
	return false;

}
/*ALCODEEND*/}

Main main()
{/*ALCODESTART::1787663544473*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean devolver_saldoLocomotiva(Trem trem)
{/*ALCODESTART::1787855172665*/
for (int i = 0; i < trem.size(); i++) {
    Locomotiva locomotiva = (Locomotiva) trem.getCar(i);
    String patio = locomotiva.patio;

    switch (patio) {

        case "pst":
            pst_locomotivas_disponiveis = (pst_locomotivas_disponiveis + 1);
            break;

        case "pmc":
            pmc_locomotivas_disponiveis = (pmc_locomotivas_disponiveis + 1);
            break;

        case "pcx":
            pcx_locomotivas_disponiveis = (pcx_locomotivas_disponiveis + 1);
            break;

        default:
            error("PATIO DESCONHECIDO: " + patio);
            break;
    }
}
/*ALCODEEND*/}

boolean solicitar_locomotiva(String patio,String destino)
{/*ALCODESTART::1789181593937*/
Train trem = null;

switch (patio) {
    case "PSN":
        trem = trainSource1.inject();
        break;
    case "PMC":
        trem = trainSource2.inject();
        break;
    case 2"PCX":
        trem = trainSource3.inject();
        break;
    default:
        error("Valor não reconhecido para injeção de trem: " + valor);
        break;
}

if (trem != null) {
    trem.desino = valorDoAtributo;
}
/*ALCODEEND*/}

boolean verificar_ferrovia_da_locomotiva(Trem trem,String ferrovia)
{/*ALCODESTART::1789654352597*/
Agent primeiroCarro = trem.getFirst();

if (primeiroCarro instanceof Locomotiva) {
    
    Locomotiva loco = (Locomotiva) primeiroCarro;
    return loco.ferrovia.equalsIgnoreCase(ferrovia);
    
} else {

    traceln("erro: carro não é locomotiva");
    return false;
    
}
/*ALCODEEND*/}

boolean solicitar_locomotivas_manobra(String patio,String tipo)
{/*ALCODESTART::1789664284000*/
traceln("chamando locomotiva de manobra");

switch (patio) {

    case "pst":
        if (tipo.equals("manobra-coperXI-rechego")) {
            
            traceln("dkfnALIEFNÃPIN");
            
        } else {
            error("Tipo de manobra desconhecido no PST: [" + tipo + "]");
        }
        break;

    default:
        error("PATIO DESCONHECIDO: [" + patio + "] len=" + patio.length());
        break;
}
/*ALCODEEND*/}

boolean verificar_patio_da_locomotiva(Trem trem,String patio)
{/*ALCODESTART::1790089120221*/
Agent primeiroCarro = trem.getFirst();

if (primeiroCarro instanceof Locomotiva) {
    
    Locomotiva loco = (Locomotiva) primeiroCarro;
    return loco.patio.equalsIgnoreCase(patio);
    
} else {

    traceln("erro: carro não é locomotiva");
    return false;
    
}
/*ALCODEEND*/}

ResourcePool selecionar_rp_locomotiva_trecho(String ferrovia)
{/*ALCODESTART::1790779134818*/
traceln("chamando locomotiva de trecho");

if (ferrovia == null)
    error("FERROVIA NULA");

switch (ferrovia.trim().toLowerCase()) {

    case "rumo":
        return rp_locoRumo;

    case "mrs":
        return rp_locoMRS;

    case "vli":
        return rp_locoVLI;

    default:
        error("FERROVIA DESCONHECIDA: [" + ferrovia + "] len=" + ferrovia.length());
        return null;
}
/*ALCODEEND*/}

double inicializar()
{/*ALCODESTART::1790877818204*/

/*ALCODEEND*/}

