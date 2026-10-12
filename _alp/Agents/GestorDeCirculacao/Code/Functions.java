Main main()
{/*ALCODESTART::1787580935550*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

double inicializar()
{/*ALCODESTART::1787603378110*/

//coordenadas_ferroviarias.put(main().pos_locoAlivio_PMC,    9);
//coordenadas_ferroviarias.put(main().pos_circ_alivio_tgrao, 10); 

linhas_e_recursos.put(rp_linha5000, coll_trecho5000);

linhas_e_recursos.put(rp_linha_intervalo4000, 		coll_intervalo4k		);
linhas_e_recursos.put(rp_linha_marginalRetaVelha, 	coll_marginalRetaVelha	);
linhas_e_recursos.put(rp_linha_via2, 				coll_via2				);
linhas_e_recursos.put(rp_linha_via2_intermar, 		coll_via2_intermar		); 

traceln("startup -> linhas_e_recursos");
/*ALCODEEND*/}

double configurar_rota(Trem trem)
{/*ALCODESTART::1787667154475*/
destino = trem.posicao_destino;
/*ALCODEEND*/}

double atualizar_licenciamento(Trem trem)
{/*ALCODESTART::1788901087606*/
List<RailwayTrack> tracksOcupados = trem.getAllOccupiedTracks();

trem._linhas_para_liberar.clear();

for (Map.Entry<ResourcePool, Collection<RailwayTrack>> entry : linhas_e_recursos.entrySet()) {
    
    ResourcePool pool = entry.getKey();
    Collection<RailwayTrack> tracksDaLinha = entry.getValue();

    boolean temRecurso = !trem.resourceUnitsOfPool(pool).isEmpty();
    boolean saiuDaLinha = Collections.disjoint(tracksOcupados, tracksDaLinha);

    if (temRecurso && saiuDaLinha) {

        trem._linhas_para_liberar.add(pool);   // ← isso que faltava
    }
}
/*ALCODEEND*/}

ResourcePool[] liberar_faixas_ocupadas(Trem trem)
{/*ALCODESTART::1789317324799*/
List<ResourcePool> recursosLiberados = new ArrayList<>();

// ALT-010 [diag_liberar]: o que o trem realmente possui entre os pools das faixas
StringBuilder possui = new StringBuilder();
for (Faixa f : faixas) {
    if (!trem.resourceUnitsOfPool(f.recurso).isEmpty())
        possui.append(f.recurso.getName()).append(" ");
}
traceln("[diag_liberar] t=" + time() + " trem " + trem.getId()
        + " | possui: " + (possui.length() > 0 ? possui.toString() : "nenhum"));

for (Faixa faixa : faixas) {

    boolean faixaDoTrem = (faixa.idTremLicenciado == trem.getId());
    boolean faixaOcupada = (faixa.estado == Faixa.EstadoFaixa.OCUPADA);

    if (faixaDoTrem && faixaOcupada) {

        boolean faixaEstaVazia = faixa.faixaEstaVazia();
        boolean temUnidade     = !trem.resourceUnitsOfPool(faixa.recurso).isEmpty();

        // ALT-010 [diag_liberar]: cada faixa candidata a liberacao
        traceln("[diag_liberar]   " + faixa.recurso.getName()
                + " | vazia=" + faixaEstaVazia
                + " | temUnidade=" + temUnidade
                + ((faixaEstaVazia && !temUnidade) ? "  <-- VAI DAR ERRO" : ""));

        if (faixaEstaVazia) {
            recursosLiberados.add(faixa.recurso);
            faixa.liberar();
        }
    }
}

return recursosLiberados.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

double bloquear_faixas(Trem trem,List<Faixa> faixas)
{/*ALCODESTART::1790699074295*/
for (Faixa f : faixas) {
    f.bloquear(trem);
}
/*ALCODEEND*/}

