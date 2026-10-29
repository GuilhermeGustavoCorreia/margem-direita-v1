double descarregar()
{/*ALCODESTART::1781892511343*/
switch (ferrovia) {
    
    case "rumo": 
	    
	    vagao_carregado_rumo.setVisible(false); 
	    vagao_vazio_rumo.setVisible(true);
	    break;
	    
    case "mrs":  
	    
	    vagao_carregado_mrs.setVisible(false);
	    vagao_vazio_mrs.setVisible(true);  
	    break;
	    
    case "vli":  
	    
	    vagao_carregado_vli.setVisible(false);  
	    vagao_vazio_mrs.setVisible(true);
	    break;
    
    default:
    
        error("Ferrovia desconhecida: '" + ferrovia + "'");
        return;
}

carregado = false;
/*ALCODEEND*/}

double setSegmento(String seg)
{/*ALCODESTART::1791556207683*/
segmento = seg;

switch (segmento) {
    
    case "acucar": 	
    
    	retangulo_acucar.setVisible(true); 
    	break;
    
    case "grao":  	
    	
    	retangulo_grao.setVisible(true);  
    	break;
  
	case "celulose":  	
    	
    	retangulo_grao.setVisible(true);  
    	break;
    
    default:
        error("Segmento desconhecido: '" + segmento + "'");
        return;
}

/*ALCODEEND*/}

double setFerrovia(String ferr)
{/*ALCODESTART::1791556216106*/
ferrovia = ferr;

switch (ferrovia) {
    
    case "rumo": vagao_carregado_rumo.setVisible(true); break;
    case "mrs":  vagao_carregado_mrs.setVisible(true);  break;
    case "vli":  vagao_carregado_vli.setVisible(true);  break;
    
    default:
        error("Ferrovia desconhecida: '" + ferrovia + "'");
        return;
}

/*ALCODEEND*/}

double setTerminal(ITerminalMoega term)
{/*ALCODESTART::1791556912046*/
terminal = term;
/*ALCODEEND*/}

