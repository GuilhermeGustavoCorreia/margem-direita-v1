boolean podeReceberNovoLote()
{/*ALCODESTART::1783947241534*/
if (main().getTotalCarrosNasTracks_v2(linhas_atendimento) > 0) {
    return false;
} else {
    return true;
}
/*ALCODEEND*/}

Main main()
{/*ALCODESTART::1783960203072*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

GestorDeAlivios formacaoVazios()
{/*ALCODESTART::1784059840311*/
return main().gestorDeAlivios;
/*ALCODEEND*/}

double sistema_semaforo(String sinal)
{/*ALCODESTART::1784557361378*/
switch (sinal) {

    case "descarregar":
        _aguardaDescarregar = true;
        _aguardaColocar     = false;
        _aguardaAliviar     = false;
        break;

    case "colocar":
        _aguardaDescarregar = false;
        _aguardaColocar     = true;
        _aguardaAliviar     = false;
        break;

    case "aliviar":
        _aguardaDescarregar = false;
        _aguardaColocar     = false;
        _aguardaAliviar     = true;
        break;

    case "livre":
        _aguardaDescarregar = false;
        _aguardaColocar     = false;
        _aguardaAliviar     = false;
        break;
        
    case "all-blocj":
        _aguardaDescarregar = true;
        _aguardaColocar     = true;
        _aguardaAliviar     = true;
        break;
    

    default:
        error("Sinal desconhecido no sistema_semaforo: " + sinal);
        break;
}

aguardaDescarregar.recalculateConditions();
aguardaColocar.recalculateConditions();
aguardaAliviar.recalculateConditions();
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1786130046535*/
return 0;
/*ALCODEEND*/}

double takeAlivio(Agent agent)
{/*ALCODESTART::1786477151977*/
//aliviarMoega.take(agent);
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1787599492983*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1787663104148*/
return "PST";
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1787666908403*/
return main().pos_locoAlivio_PMC;
/*ALCODEEND*/}

ArrayList<Faixa>  getPrimeiroAlivioFaixas()
{/*ALCODESTART::1790637952305*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790777481009*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790777481011*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790791087456*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790791485013*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791223008004*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224497483*/
return 0;
/*ALCODEEND*/}

