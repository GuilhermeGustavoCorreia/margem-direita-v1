Main main()
{/*ALCODESTART::1787663544473*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean verificar_ferrovia_da_locomotiva(Trem trem,String ferrovia)
{/*ALCODESTART::1789654352597*/
Agent primeiroCarro = trem.getFirst();

if (primeiroCarro instanceof Locomotiva) {
    
    Locomotiva loco = (Locomotiva) primeiroCarro;
    return loco.ferrovia.equalsIgnoreCase(ferrovia);
    
} else {

    Log.erro("GesLocomotivas", "carro_nao_e_locomotiva", "carro não é locomotiva");
    return false;
    
}
/*ALCODEEND*/}

boolean solicitar_locomotivas_manobra(String patio,String tipo)
{/*ALCODESTART::1789664284000*/
Log.info("GesLocomotivas", "solicitar_loco_manobra", "chamando locomotiva de manobra");

switch (patio) {

    case "pst":
        if (tipo.equals("manobra-coperXI-rechego")) {
            
            Log.debug("GesLocomotivas", "debug_temporario", "dkfnALIEFNÃPIN");
            
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

    Log.erro("GesLocomotivas", "carro_nao_e_locomotiva", "carro não é locomotiva");
    return false;
    
}
/*ALCODEEND*/}

ResourcePool selecionar_rp_locomotiva_trecho(String ferrovia)
{/*ALCODESTART::1790779134818*/
Log.info("GesLocomotivas", "solicitar_loco_trecho", "chamando locomotiva de trecho");

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

