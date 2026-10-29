Main main()
{/*ALCODESTART::1792958885228*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1792958885230*/
int tamanhoL1 = main().getTamanhoDoTrem(lote_vazios_linha01);
int tamanhoL2 = main().getTamanhoDoTrem(lote_vazios_linha02);
int tamanhoL3 = main().getTamanhoDoTrem(lote_vazios_linha03);
int tamanhoL4 = main().getTamanhoDoTrem(lote_vazios_linha04);

int tamanhoVazios = (tamanhoL1 + tamanhoL2 + tamanhoL3 + tamanhoL4);

int capacidadeVazios = 64;

if (tamanhoVazios >= capacidadeVazios) {

    return (float) 1.1;
    
} else {

    return (float) 0.0;
    
}
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1792958885232*/
// ALT-063: a loco do alívio sai de pos_locoAlivio_PMC e para em _suzano_alivio (7000_c)
return main()._eldorado_alivio;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1792958885234*/
return 1;
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1792958885236*/
// ALT-061: Suzano é do pátio PMC (celulose)
return "pmc";
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1792958885238*/
return true;
/*ALCODEEND*/}

ArrayList<Faixa>  get_faixas_primeiro_alivio()
{/*ALCODESTART::1792958885240*/
// ALT-063: faixas do caminho da loco: silosMuro_a (onde nasce) -> marginal_c -> linha6000_a
return primeiro_alivio_faixas;
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1792958885242*/
return segmento_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1792958885244*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1792958885246*/
// ALT-062
iniciar_alivio.take(trem);
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1792958885248*/
return null;
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1792958885250*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1792958885252*/
// ALT-014: vazios reais nas linhas L1..L4
return _getVagoesVazios();
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1792958885254*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1792958885256*/
return 1;
/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1792959035948*/
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
{/*ALCODESTART::1792959120766*/
int vazios_linha_L1 = main().getTamanhoDoTrem(lote_vazios_linha01);
int vazios_linha_L2 = main().getTamanhoDoTrem(lote_vazios_linha02);
int vazios_linha_L3 = main().getTamanhoDoTrem(lote_vazios_linha03);
int vazios_linha_L4 = main().getTamanhoDoTrem(lote_vazios_linha04);

return (
	vazios_linha_L1 + 
	vazios_linha_L2 + 
	vazios_linha_L3 +
	vazios_linha_L4
);

/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1792959120768*/
return 0;
/*ALCODEEND*/}

ResourcePool[] get_recursos_loco_alivio(Trem trem)
{/*ALCODESTART::1793103253588*/
// ALT-063: pools que a loco do alívio trouxe e ainda segura:
// linha 5000/6000 (ocupar_linha5000_6000) e as faixas do caminho (bloquear_faixas_pmc).
List<ResourcePool> pools = new ArrayList<ResourcePool>();
for (ResourcePool rp : main().gestorDeCirculacao.get_recursos_linha_5000_6000(trem)) pools.add(rp);
for (Faixa f : primeiro_alivio_faixas) {
	if (f != null && f.recurso != null && !pools.contains(f.recurso) && !trem.resourceUnitsOfPool(f.recurso).isEmpty()) pools.add(f.recurso);
}
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

