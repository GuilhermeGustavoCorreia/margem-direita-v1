Main main()
{/*ALCODESTART::1788185597915*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1788185597917*/
int tamanhoL1 = main().getTamanhoDoTrem(loteVazios_L1);
int tamanhoL2 = main().getTamanhoDoTrem(loteVazios_L2);
int tamanhoL3 = main().getTamanhoDoTrem(loteVazios_L3);
int tamanhoL4 = main().getTamanhoDoTrem(loteVazios_L4);

int tamanhoVazios = (tamanhoL1 + tamanhoL2 + tamanhoL3 + tamanhoL4);

int capacidadeVazios = 64;

if (tamanhoVazios >= capacidadeVazios) {

    return (float) 1.1;
    
} else {

    return (float) 0.0;
    
}
/*ALCODEEND*/}

double takeAlivio(Agent agent)
{/*ALCODESTART::1788185597919*/
alivioVazios.take(agent);
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1788185597921*/
return main().pos_circ_alivio_tgrao;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1788185597923*/
return 1;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1788185597925*/
return "PMC";
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1788188469793*/
return true;
/*ALCODEEND*/}

PositionOnTrack  getPosicaoUltimoVagao(Trem trem,boolean offsetCrescenteParaAtras)
{/*ALCODESTART::1788196178875*/
Agent ultimoVagao = trem.getLast();
if (ultimoVagao == null) {
    error("Trem vazio.");
    return;
}

try {
    java.lang.reflect.Method mTrack = ultimoVagao.getClass().getMethod("getTrack", boolean.class);
    java.lang.reflect.Method mOffset = ultimoVagao.getClass().getMethod("getOffset", boolean.class);

    _trackAparecerLocoMnb = (RailwayTrack) mTrack.invoke(ultimoVagao, false);
    double offsetUltimoVagao = (double) mOffset.invoke(ultimoVagao, false);

    _offsetAparecerLocoMnb = offsetCrescenteParaAtras ? offsetUltimoVagao + 20 : offsetUltimoVagao - 20;
    _offsetAparecerLocoMnb = Math.max(0, Math.min(_offsetAparecerLocoMnb, _trackAparecerLocoMnb.length(METER)));

} catch (Exception e) {
    error("Erro ao calcular posição do último vagão: " + e.getMessage());
}
/*ALCODEEND*/}

ArrayList<Faixa>  get_faixas_primeiro_alivio()
{/*ALCODESTART::1790638028230*/
// a faixa de criação da loco (faixa_via1_intermar_d, onde fica pos_locoAlivio_PST)
// é apreendida no seize_faixa_criacao_pst do gestor; aqui só as faixas seguintes até o terminal
return new ArrayList<Faixa>();
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790777437387*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790777437390*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790791070721*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790791434821*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791222944595*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224579471*/
return 0;
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1791700000001*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1791700000002*/
return 1;
/*ALCODEEND*/}

