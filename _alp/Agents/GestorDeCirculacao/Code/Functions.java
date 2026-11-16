Main main()
{/*ALCODESTART::1787580935550*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

double inicializar()
{/*ALCODESTART::1787603378110*/
Log.iniciar(this);

//coordenadas_ferroviarias.put(main().pos_locoAlivio_PMC,    9);
//coordenadas_ferroviarias.put(main().pos_circ_alivio_tgrao, 10); 

linhas_e_recursos.put(rp_linha5000, coll_trecho5000);

linhas_e_recursos.put(rp_linha_intervalo4000, 		coll_intervalo4k		);
linhas_e_recursos.put(rp_linha_marginalRetaVelha, 	coll_marginalRetaVelha	);
linhas_e_recursos.put(rp_linha_via2, 				coll_via2				);
linhas_e_recursos.put(rp_linha_via2_intermar, 		coll_via2_intermar		); 

Log.info("GesCirculacao", "startup", "linhas_e_recursos");
/*ALCODEEND*/}

double configurar_rota(Trem trem)
{/*ALCODESTART::1787667154475*/
destino = trem.posicao_destino;
/*ALCODEEND*/}

ResourcePool[] liberar_faixas_ocupadas(Trem trem)
{/*ALCODESTART::1789317324799*/
List<ResourcePool> recursosLiberados = new ArrayList<>();

// [diag_liberar]: o que o trem realmente possui entre os pools das faixas
StringBuilder possui = new StringBuilder();

for (Faixa f : faixas) {
    
    if (!trem.resourceUnitsOfPool(f.recurso).isEmpty())
       
        possui.append(f.recurso.getName()).append(" ");
}

for (Faixa faixa : faixas) {

    boolean faixaDoTrem = (faixa.idTremLicenciado == trem.getId());
    boolean faixaOcupada = (faixa.estado == Faixa.EstadoFaixa.OCUPADA);

    if (faixaDoTrem && faixaOcupada) {

        boolean faixaEstaVazia = faixa.faixaEstaVazia();
        boolean temUnidade     = !trem.resourceUnitsOfPool(faixa.recurso).isEmpty();

        // [diag_liberar]: cada faixa candidata a liberacao
        // a faixa sempre sai de OCUPADA quando o trem já saiu dela.
        // Só entra na lista de Release o recurso que o trem realmente possui
        // (Release em SPECIFIED_RESOURCES dá erro se o agente não tiver a unidade).
   
        if (faixaEstaVazia) {
            
            faixa.liberar();
            if (temUnidade) {
                recursosLiberados.add(faixa.recurso);
            }
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

ResourcePool[] get_recursos_linha_5000_6000(Trem trem)
{/*ALCODESTART::1792014000500*/
// pools da linha 5000/6000 que o trem realmente possui
// (Release em SPECIFIED_RESOURCES dá erro se o agente não tiver a unidade)
List<ResourcePool> pools = new ArrayList<>();
for (ResourcePool rp : java.util.List.of(rp_linha5000, rp_linha6000)) {
    if (!trem.resourceUnitsOfPool(rp).isEmpty()) pools.add(rp);
}
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

double log_circulacao(Agent trem,String status)
{/*ALCODESTART::1793910000002*/
// Log padrao: origem GesCirculacao, texto [(prefixo - id): segmento destino] [status]
Trem t = (Trem) trem;
String destino = t.ITerminalMoega_destino == null ? "sem destino" : t.ITerminalMoega_destino.getName();
Log.info("GesCirculacao", "circulacao", String.format("[(%s - %d): %s %s] [%s]", t.prefixo, t.getId(), t.getSegmento(), destino, status));
/*ALCODEEND*/}

double log_entrada(Agent trem)
{/*ALCODESTART::1793910000003*/
// loga só a primeira entrada do trem no gestor (tentar_usar_faixa recebe o trem a cada faixa)
if (trens_no_gestor.add(trem.getId())) log_circulacao(trem, "comecou circulacao");
/*ALCODEEND*/}

double log_saida(Agent trem,String status)
{/*ALCODESTART::1793910000004*/
// trem deixa o gestor (terminal ou DV)
trens_no_gestor.remove(trem.getId());
// chegou ao terminal: deixa de ser trem a caminho dele (Chegadas e ISN consultam esta lista)
trens_em_circulacao.remove((Trem) trem);
log_circulacao(trem, status);
/*ALCODEEND*/}

boolean eh_terminal_mnb_pcx(Trem trem)
{/*ALCODESTART::1798140000901*/
// terminais que usam o recurso mnb_pcx (encoste, alívio e triangulação na área comum)
String t = trem.get_terminal_name();
return "tgrao".equals(t) || "bracell".equals(t) || "eldorado".equals(t) || "suzano".equals(t);
/*ALCODEEND*/}

ResourcePool[] get_recursos_mnb_pcx(Trem trem)
{/*ALCODESTART::1798140000902*/
// rp_mnb_pcx, se o trem tiver a unidade (Release em SPECIFIED_RESOURCES dá erro se não tiver)
if (trem != null && !trem.resourceUnitsOfPool(rp_mnb_pcx).isEmpty()) return new ResourcePool[] { rp_mnb_pcx };
return new ResourcePool[0];
/*ALCODEEND*/}

ResourcePool[] incluir_mnb_pcx(ResourcePool[] recursos,Trem trem)
{/*ALCODESTART::1798140000903*/
// junta o rp_mnb_pcx (se o trem tiver a unidade) aos recursos de um Release
List<ResourcePool> pools = new ArrayList<ResourcePool>();
if (recursos != null) for (ResourcePool rp : recursos) if (!pools.contains(rp)) pools.add(rp);
for (ResourcePool rp : get_recursos_mnb_pcx(trem)) if (!pools.contains(rp)) pools.add(rp);
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}
