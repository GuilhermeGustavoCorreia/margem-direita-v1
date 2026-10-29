Main main()
{/*ALCODESTART::1792408546619*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1792408546621*/
// ALT-058: 2,0 só quando tudo foi descarregado (os 40 das linhas e os 24 da troca).
// Até lá fica 0. descarregou_tudo é ligado no fim da troca (ALT-059, verificar_descarregou_tudo).
if (descarregou_tudo) {
	return (float) 2.0;
}
return (float) 0.0;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1792408546623*/
// ALT-063: a loco do alívio sai de pos_locoAlivio_PMC e para em _suzano_alivio (7000_c)
return main()._suzano_alivio;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1792408546625*/
return 1;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1792408546627*/
// ALT-061: Suzano é do pátio PMC (celulose)
return "pmc";
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1792408546629*/
return true;
/*ALCODEEND*/}

ArrayList<Faixa>  get_faixas_primeiro_alivio()
{/*ALCODESTART::1792408546631*/
// ALT-063: faixas do caminho da loco: silosMuro_a (onde nasce) -> marginal_c -> linha6000_a
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1792408546633*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1792408546635*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1792408546637*/
// ALT-062
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1792408546639*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1792408546641*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1792408546643*/
// ALT-014: vazios reais nas linhas L1..L4
return _getVagoesVazios();
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1792408546645*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1792408546647*/
return 1;
/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1792408584803*/
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
{/*ALCODESTART::1792408584805*/
// ALT-057: vazios das linhas L1..L4
// ALT-059: e os 30 vazios deixados na L7000 na troca
int vazios_linha_L1 = main().getTamanhoDoTrem(loteVazios_L1);
int vazios_linha_L2 = main().getTamanhoDoTrem(loteVazios_L2);
int vazios_linha_L3 = main().getTamanhoDoTrem(loteVazios_L3);
int vazios_linha_L4 = main().getTamanhoDoTrem(loteVazios_L4);
int vazios_L7000    = main().getTamanhoDoTrem(loteVazios_L7000);
return (
	vazios_linha_L1 +
	vazios_linha_L2 +
	vazios_linha_L3 +
	vazios_linha_L4 +
	vazios_L7000
);
/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1792408584807*/
// ALT-057: carregados nas linhas L1..L4 e no lote que ficou na L7000
int carregados_linha_L1 = main().getTamanhoDoTrem(lote_carregados_L1);
int carregados_linha_L2 = main().getTamanhoDoTrem(lote_carregados_L2);
int carregados_linha_L3 = main().getTamanhoDoTrem(lote_carregados_L3);
int carregados_linha_L4 = main().getTamanhoDoTrem(lote_carregados_L4);
int carregados_L7000    = main().getTamanhoDoTrem(lote_L7000);
return (
	carregados_linha_L1 +
	carregados_linha_L2 +
	carregados_linha_L3 +
	carregados_linha_L4 +
	carregados_L7000
);
/*ALCODEEND*/}

double registrar_linha_descarregada()
{/*ALCODESTART::1792770000043*/
// ALT-059: conta as linhas da 1a rodada (40 vagões) que terminaram a descarga.
// Com as 4 descarregadas, bloqueia os holds para a 2a rodada e chama a loco FIPS da troca.
linhas_descarregadas++;
traceln(String.format("[%.1f][Suzano][registrar_linha_descarregada] %d de 4 linhas descarregadas", time(), linhas_descarregadas));
if (linhas_descarregadas >= 4) {
	linhas_descarregadas = 0;
	em_troca = true;
	aguarda_L4a.block();
	aguarda_L4b.block();
	aguarda_L4c.block();
	traceln(String.format("[%.1f][Suzano][registrar_linha_descarregada] chama loco FIPS para a troca dos 24", time()));
	loco_pcx_starts.inject();
}
/*ALCODEEND*/}

double verificar_descarregou_tudo()
{/*ALCODESTART::1792770000044*/
// ALT-059: fim da troca quando L1..L3 descarregaram e os vazios estão na L7000 (ALT-060: 24 vazios).
// Liga o overload (2,0) e deixa os holds bloqueados para o próximo trem.
if (loteVazios_L1 != null && loteVazios_L2 != null && loteVazios_L3 != null && loteVazios_L7000 != null) {
	descarregou_tudo = true;
	em_troca = false;
	aguarda_L4a.block();
	aguarda_L4b.block();
	aguarda_L4c.block();
	traceln(String.format("[%.1f][Suzano][verificar_descarregou_tudo] %d vazios, overload = 2,0", time(), _getVagoesVazios()));
}
/*ALCODEEND*/}

ResourcePool[] get_recursos_loco_alivio(Trem trem)
{/*ALCODESTART::1792795000007*/
// ALT-063: pools que a loco do alívio trouxe e ainda segura:
// linha 5000/6000 (ocupar_linha5000_6000) e as faixas do caminho (bloquear_faixas_pmc).
List<ResourcePool> pools = new ArrayList<ResourcePool>();
for (ResourcePool rp : main().gestorDeCirculacao.get_recursos_linha_5000_6000(trem)) pools.add(rp);
for (Faixa f : primeiro_alivio_faixas) {
	if (f != null && f.recurso != null && !pools.contains(f.recurso) && !trem.resourceUnitsOfPool(f.recurso).isEmpty()) pools.add(f.recurso);
}
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

