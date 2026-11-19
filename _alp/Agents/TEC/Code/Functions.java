Main main()
{/*ALCODESTART::1794956361278*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1794956361280*/
// provisório: um trem por vez até o encoste/descarga do TEC existirem
return _trem_chegando == null;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1794956361282*/
// provisório: TEC ainda não tem descarga, então não gera vazios nem pede alívio
return 0;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1794956361284*/
// provisório: definir quando o alívio do TEC for construído
return null;
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
// provisório: definir quando o alívio do TEC for construído
return new ArrayList<Faixa>();
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
// provisório: TEC ainda não tem alívio
Log.warn("TEC", "takeAlivio", String.format("trem=%d alivio ainda nao implementado", trem.getId()));
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
// provisório: TEC ainda não tem descarga
return 0;
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

