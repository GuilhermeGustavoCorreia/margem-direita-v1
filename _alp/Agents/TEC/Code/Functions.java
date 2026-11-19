Main main()
{/*ALCODESTART::1794956361278*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1794956361280*/
// trem entrando: do comecar_encoste até parar na linha 2000
if (_trem_chegando != null) return false;
// sem 2º lote: a cauda de 82 vagões ficaria na garganta da Moema e travaria o alívio
return lote_linha2000_emDescarga == null && lote_linha2000_proximo == null;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1794956361282*/
int vazios = get_quantidade_vazios();
float overload = (float) vazios / QUANTIDADE_PARA_ALIVIAR;

boolean moegas_ocupadas = (rp_moegaV.idle() == 0 || rp_moegaX.idle() == 0);	
// segundo lote de outra ferrovia/segmento não conta: ele só entra depois do alívio destes vazios
int carregados = _getVagoesCarregados();
if (lote_linha2000_proximo != null && !eh_mesmo_tipo_em_atendimento(lote_linha2000_proximo))
    carregados -= main().getNumeroDeVagoes(lote_linha2000_proximo);
boolean ultimos_vazios_no_terminal = (carregados <= 0 && _getVagoesVazios() > 0);

//ultimos vagoes
if (ultimos_vazios_no_terminal && !moegas_ocupadas) {
    return (float) 2.0;
}

if (QUANTIDADE_PARA_ALIVIAR <= 0) return 0;
return (float) _getVagoesVazios() / QUANTIDADE_PARA_ALIVIAR;


/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1794956361284*/
return main()._adm_alivio_comeco;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1794956361286*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1794956361288*/
return "pmc";
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_primeiro_alivio()
{/*ALCODESTART::1794956361290*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1794956361293*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1794956361295*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1794956361297*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1794956361299*/
return new ArrayList<>(Arrays.asList(
	main().gestorDeCirculacao.faixa_via1_intermar_d, 
	main().gestorDeCirculacao.faixa_via1_intermar_c
));
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1794956361301*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1794956361303*/
return _get_vagoes_vazios();
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1794956361305*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1794956361307*/
// igual ao T39
return 1;
/*ALCODEEND*/}

