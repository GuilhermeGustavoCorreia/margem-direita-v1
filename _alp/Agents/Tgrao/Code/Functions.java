Main main()
{/*ALCODESTART::1786651264885*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1787231640652*/
// ALT-005: conta os lotes vazios (antes contava os carregados)
int tamanhoVazios = _getVagoesVazios();

int capacidadeVazios = (
	CAPACIDADE_LINHA_MAR +
	CAPACIDADE_LINHA_TERRA
);

if (tamanhoVazios >= capacidadeVazios) {
	return (float) 1.1;
} else {
	return (float) 0.0;
}
/*ALCODEEND*/}

double takeAlivio(Agent agent)
{/*ALCODESTART::1787242578330*/
alivioVazios.take(agent);
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1787242590653*/
return true;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1787599296674*/
return main().pos_circ_alivio_tgrao;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1787599350404*/
return 2;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1787663062252*/
return "pst";
/*ALCODEEND*/}

ArrayList<Faixa>  get_faixas_primeiro_alivio()
{/*ALCODESTART::1790637932726*/
// faixas do caminho da loco de manobra até o Tgrao
// (a faixa de criação, marginal_a, é apreendida à parte pelo gestor)
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790777501574*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790777501576*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790791230610*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790791430567*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791223058967*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224533452*/
return _getVagoesVazios();
/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1791662803557*/
for (int i = 0; i < trem.size(); i++) {
        Agent carro = trem.getCar(i); // confere o nome exato do método com Ctrl+Espaço

        if (carro instanceof Vagao) {
            Vagao vagao = (Vagao) carro;
            ferrovia_em_atendimento = vagao.ferrovia;
            segmento_em_atendimento = vagao.segmento;
            return; // achou o primeiro vagão, encerra
        }
    }

    traceln("erro: trem não tem nenhum vagão");
/*ALCODEEND*/}

int _getVagoesVazios()
{/*ALCODESTART::1791662924046*/
int vagoes_linhaMar 	= main().getTamanhoDoTrem(loteVazios_linhaMar);
int vagoes_linhaMoega	= main().getTamanhoDoTrem(loteVazios_linhaMoega);



return (vagoes_linhaMar + vagoes_linhaMoega);

/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1791663125070*/
int vagoes_linhaTerra = main().getNumeroDeVagoes(lote_linhaTerra);
int vagoes_linhaMar   = main().getNumeroDeVagoes(lote_linhaMar);

return (vagoes_linhaTerra + vagoes_linhaMar);
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1791700000013*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "manobra";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1791700000014*/
return 2;
/*ALCODEEND*/}

