Main main()
{/*ALCODESTART::1798330000044*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1798330000045*/
// provisório: um trem por vez até o encoste/descarga do TEC existirem
return _trem_chegando == null;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1798330000046*/
// descarregado e com a loco na cauda: pede o alívio
return descarga_concluida ? (float) 2.0 : 0;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1798330000047*/
// provisório: definir quando o alívio do TEC for construído
return null;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1798330000048*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1798330000049*/
return "pmc";
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_primeiro_alivio()
{/*ALCODESTART::1798330000050*/
// provisório: definir quando o alívio do TEC for construído
return new ArrayList<Faixa>();
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1798330000051*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1798330000052*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1798330000053*/
// chamado pelo GestorDeAlivios (tipo "proprio", trem = null): solta o trem que espera no aguardando_alivio
if (aguardando_alivio.size() > 0) {
	Log.info("TEC_ACUCAR", "takeAlivio", "inicio do alivio");
	aguardando_alivio.free(aguardando_alivio.get(0));
} else {
	Log.warn("TEC_ACUCAR", "takeAlivio", "nenhum trem no aguardando_alivio");
}
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1798330000054*/
// primeira faixa da saída do alívio
return new ArrayList<>(Arrays.asList(main().gestorDeCirculacao.faixa_via1_intermar_a));
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1798330000055*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1798330000056*/
return _get_vagoes_vazios();
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1798330000057*/
// "manobra": loco FIPS PST | "ferrovia": loco da ferrovia em atendimento | "proprio": loco do próprio trem (TEC)
return "proprio";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1798330000058*/
// igual ao T39
return 1;
/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1798330000066*/
// vagões carregados do trem no TEC
return _trem_chegando == null ? 0 : _trem_chegando.getNumeroDeVagoesCarregados();
/*ALCODEEND*/}

int _get_vagoes_vazios()
{/*ALCODESTART::1798330000067*/
// vagões vazios do trem no TEC (a loco não conta)
return _trem_chegando == null ? 0 : _trem_chegando.getNumeroDeVagoesVazios();
/*ALCODEEND*/}

double log_analise(String evento,Agent a,String msg)
{/*ALCODESTART::1798330000068*/
// log de análise: só escreve com log_de_analise = true
if (!log_de_analise) return;
String t = (a == null) ? "-" : String.valueOf(a.getId());
if (a instanceof Trem) {
	Trem tr = (Trem) a;
	t = String.format("trem=%d carros=%d vgs=%d locos=%d", tr.getId(), tr.size(), tr.getNumeroDeVagoes(), tr.getNumeroDeLocomotivas());
}
Log.info("TEC_ACUCAR", evento, t + " | " + msg);
/*ALCODEEND*/}

RailwayTrack get_track_cauda()
{/*ALCODESTART::1798330000104*/
// trilho da traseira do último vagão do trem parado no _tec_encoste
Agent ultimo = _trem_chegando.getLast();
try {
	return (RailwayTrack) ultimo.getClass().getMethod("getTrack", boolean.class).invoke(ultimo, false);
} catch (Exception e) {
	error("TEC get_track_cauda: " + e);
	return null;
}
/*ALCODEEND*/}

double get_offset_cauda()
{/*ALCODESTART::1798330000105*/
// cabeça da loco que entra na cauda (TrainEnter na orientação padrão: a loco fica entre offset - comprimento e offset).
// trem andando no sentido do trilho (traseira no menor offset): cabeça da loco na traseira do vagão.
// trem andando contra o sentido do trilho: cabeça da loco um comprimento de loco depois da traseira.
double[] info = get_ultimo_carro_info();
double frente = info[0], tras = info[1];
double comp_loco = 22.5; // locos de 22,5 m (formações de teste)
double offset = (frente > tras) ? tras : tras + comp_loco;
Log.info("TEC_ACUCAR", "loco_cauda", String.format("trilho=%s frente=%.1f tras=%.1f offset_loco=%.1f", get_track_cauda().getName(), frente, tras, offset));
return offset;
/*ALCODEEND*/}

double[] get_ultimo_carro_info()
{/*ALCODESTART::1798330000106*/
// {offset da frente, offset da traseira} do último vagão, no trilho da traseira
Agent ultimo = _trem_chegando.getLast();
try {
	java.lang.reflect.Method mo = ultimo.getClass().getMethod("getOffset", boolean.class);
	double frente = (double) mo.invoke(ultimo, true);
	double tras   = (double) mo.invoke(ultimo, false);
	return new double[] { frente, tras };
} catch (Exception e) {
	error("TEC get_ultimo_carro_info: " + e);
	return new double[] { 0, 0 };
}
/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1798330000070*/
for (int i = 0; i < trem.size(); i++) {
        Agent carro = trem.getCar(i); // confere o nome exato do método com Ctrl+Espaço

        if (carro instanceof Vagao) {
            Vagao vagao = (Vagao) carro;
            ferrovia_em_atendimento = vagao.ferrovia;
            segmento_em_atendimento = vagao.segmento;
            return; // achou o primeiro vagão, encerra
        }
    }

    Log.erro("ADM", "trem_sem_vagao", "trem não tem nenhum vagão");
/*ALCODEEND*/}

