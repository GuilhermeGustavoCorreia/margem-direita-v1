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

//linhas_e_recursos.put(rp_linha_intervalo4000, 		coll_intervalo4k		);
//linhas_e_recursos.put(rp_linha_marginalRetaVelha, 	coll_marginalRetaVelha	);
//linhas_e_recursos.put(rp_linha_via2, 				coll_via2				);
//linhas_e_recursos.put(rp_linha_via2_intermar, 		coll_via2_intermar		); 

Log.info("GesCirculacao", "startup", "linhas_e_recursos");
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

// saída da 4000: o trem pega intervalo4000_c e b antes de chegar nelas (seize_saida_4000).
// Vazias porque ele ainda não chegou; só soltam depois que ele passou por elas.
List<RailwayTrack> occ = trem.getAllOccupiedTracks();
boolean antes_de_c = occ.contains(main().pmc_linha4000_a) || occ.contains(main().pmc_linha4000_b) || occ.contains(main().pmc_linha4000_c);
boolean antes_de_b = antes_de_c || occ.contains(main().pmc_intervalo4000_c);

for (Faixa faixa : faixas) {

    if (faixa == faixa_intervalo4000_c && antes_de_c) continue;
    if (faixa == faixa_intervalo4000_b && antes_de_b) continue;

    // ida para as DVs: dv_entrada_c foi licenciada junto com a via1_intermar_a; só solta depois que o trem sai da via1_intermar_a
    if (faixa == faixa_dv_entrada_c && occ.contains(main().via1_intermar_a)
        && !(eh_terminal_tec(trem) && !trem.eh_alivio)) continue;

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
                log_circulacao(trem, "soltou " + faixa.track.getName());
            }
        }
    }
}

// trem de alívio da área comum (Tgrao, Bracell, Eldorado, Suzano): solta o mnb_pcx
// só quando a cauda já passou da marginal_c (último trecho da área comum na saída)
if (!pular_mnb_pcx && trem.eh_alivio && faixa_marginal_c.idTremLicenciado != trem.getId()) {
    for (ResourcePool rp : get_recursos_mnb_pcx(trem)) {
        if (!recursosLiberados.contains(rp)) {
            recursosLiberados.add(rp);
            log_circulacao(trem, "soltou mnb_pcx (saiu da marginal_c)");
        }
    }
}

// cruzamento da porteira (AMVs railwaySwitch1 e railwaySwitch27, entrada das DVs): solta quando a cauda
// sai do cruzamento, isto é, o trem não tem mais val_via2_a, val_via2 nem dv_entrada_c
// (licenciada ou ocupada) e não está na railwayTrack18
boolean no_cruzamento = faixa_val_via2_a.idTremLicenciado == trem.getId()
                     || faixa_val_via2.idTremLicenciado   == trem.getId()
                     || faixa_dv_entrada_c.idTremLicenciado == trem.getId()
                     || trem.getAllOccupiedTracks().contains(main().railwayTrack18);
if (!no_cruzamento
    && !trem.resourceUnitsOfPool(rp_cruzamento_porteira).isEmpty()
    && !recursosLiberados.contains(rp_cruzamento_porteira)) {
    recursosLiberados.add(rp_cruzamento_porteira);
    dono_cruzamento = null;
    log_circulacao(trem, "soltou cruzamento_porteira");
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

ResourcePool[][] get_conjunto_licenca(Trem trem,Faixa faixa)
{/*ALCODESTART::1799300000010*/
// recursos que o trem pega ao licenciar a faixa (licenciar_esta_faixa / licenciar_proxima_faixa)
// - via1_intermar_a + dv_entrada_c: trecho único, sempre juntas
// - val_via2_a e val_via2 passam pelo cruzamento da porteira (AMV railwaySwitch1): o cruzamento vai junto.
//   O par não leva o cruzamento: quem vai às DVs pega no seize_cruzamento_entrada, na hora de entrar.
List<ResourcePool> pools = new ArrayList<ResourcePool>();
pools.add(faixa.recurso);
if      (faixa == faixa_via1_intermar_a) pools.add(faixa_dv_entrada_c.recurso);
else if (faixa == faixa_dv_entrada_c)    pools.add(faixa_via1_intermar_a.recurso);
else if (faixa == faixa_val_via2_a || faixa == faixa_val_via2) pools.add(rp_cruzamento_porteira);

// trem chegando ao TEC pela porteira: rota inteira de uma vez (val_via2_a + cruzamento + par),
// para não esperar o par segurando o cruzamento
if (faixa == faixa_val_via2_a && eh_terminal_tec(trem) && !trem.eh_alivio) {
    pools.add(faixa_dv_entrada_c.recurso);
    pools.add(faixa_via1_intermar_a.recurso);
}

// o que o trem já tem não entra: Seize de pool de capacidade 1 que o próprio trem segura nunca sai
pools.removeIf(rp -> !trem.resourceUnitsOfPool(rp).isEmpty());

return new ResourcePool[][] { pools.toArray(new ResourcePool[0]) };
/*ALCODEEND*/}

ResourcePool[][] get_conjunto_saida_dvs(Trem trem)
{/*ALCODESTART::1799600000001*/
// saída das DVs (ocupar_val_via2_saida): val_via2 + isn_linha02_e.
// O cruzamento da porteira é a "senha" da área: é pego antes, sozinho, no pegar_cruzamento_saida.
// o que o trem já tem não entra
List<ResourcePool> pools = new ArrayList<ResourcePool>();
pools.add(faixa_val_via2.recurso);
pools.add(main().gestorDeISN.rp_isn_linha02_e);
pools.removeIf(rp -> !trem.resourceUnitsOfPool(rp).isEmpty());
return new ResourcePool[][] { pools.toArray(new ResourcePool[0]) };
/*ALCODEEND*/}

ResourcePool[][] get_conjunto_entrada_dvs(Trem trem)
{/*ALCODESTART::1799700000020*/
// entrada nas linhas das DVs (seize_cruzamento_entrada): cruzamento da porteira, se o trem ainda não tiver
List<ResourcePool> pools = new ArrayList<ResourcePool>();
pools.add(rp_cruzamento_porteira);
pools.removeIf(rp -> !trem.resourceUnitsOfPool(rp).isEmpty());
return new ResourcePool[][] { pools.toArray(new ResourcePool[0]) };
/*ALCODEEND*/}

ResourcePool[] incluir_mnb_pcx(ResourcePool[] recursos,Trem trem)
{/*ALCODESTART::1798140000903*/
// junta o rp_mnb_pcx (se o trem tiver a unidade) aos recursos de um Release
List<ResourcePool> pools = new ArrayList<ResourcePool>();
if (recursos != null) for (ResourcePool rp : recursos) if (!pools.contains(rp)) pools.add(rp);
for (ResourcePool rp : get_recursos_mnb_pcx(trem)) if (!pools.contains(rp)) pools.add(rp);
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

boolean eh_terminal_tec(Trem trem)
{/*ALCODESTART::1798300000035*/
// trens do TEC e do TEC_ACUCAR: ISN -> val_via2_a -> dv_entrada_c -> via1_intermar_a (de frente) -> comecar_encoste do terminal
if (trem == null) return false;
String t = trem.get_terminal_name();
return "tec".equals(t) || "tecAcucar".equals(t);
/*ALCODEEND*/}

String texto_trem_id(int id)
{/*ALCODESTART::1800000000001*/
// "(prefixo - id)" de um trem em circulação; fora dela, só o id
if (id == 0) return "ninguem";
for (Trem t : trens_em_circulacao) if (t.getId() == id) return "(" + t.prefixo + " - " + id + ")";
return "(trem " + id + ")";
/*ALCODEEND*/}

double log_aguarda_licenca(Trem trem,ResourcePool[][] conjunto)
{/*ALCODESTART::1800000000002*/
// trace de espera: só sai se algum recurso do conjunto estiver sem unidade livre.
// Mostra cada recurso ocupado e o dono (faixa: idTremLicenciado e estado; cruzamento: dono_cruzamento)
if (trem == null || conjunto == null || conjunto.length == 0) return;
List<String> ocupados = new ArrayList<String>();
for (ResourcePool rp : conjunto[0]) {
    if (rp == null || rp.idle() > 0) continue;
    if (rp == rp_cruzamento_porteira) {
        ocupados.add("cruzamento_porteira com=" + (dono_cruzamento == null ? "?" : texto_trem_id(dono_cruzamento.getId())));
        continue;
    }
    String txt = rp.getName();
    for (Faixa f : faixas) {
        if (f.recurso == rp) { txt = f.track.getName() + " dono=" + texto_trem_id(f.idTremLicenciado) + " " + f.estado; break; }
    }
    ocupados.add(txt);
}
if (ocupados.isEmpty()) return;
esperando_licenca.add(trem.getId());
Log.info("GesCirculacao", "aguarda_licenca", "(" + trem.prefixo + " - " + trem.getId() + ") " + String.join(" | ", ocupados));
/*ALCODEEND*/}

double log_pegou_licenca(Trem trem,String descricao)
{/*ALCODESTART::1800000000003*/
// trace de fim de espera (só para quem logou aguarda_licenca) e registro do dono do cruzamento
if (trem == null) return;
if (esperando_licenca.remove(trem.getId()))
    Log.info("GesCirculacao", "pegou_licenca", "(" + trem.prefixo + " - " + trem.getId() + ") " + descricao);
registrar_dono_cruzamento(trem);
/*ALCODEEND*/}

double registrar_dono_cruzamento(Trem trem)
{/*ALCODESTART::1800000000004*/
// se o trem acabou de pegar o cruzamento da porteira, guarda o dono e loga
if (trem == null || trem.resourceUnitsOfPool(rp_cruzamento_porteira).isEmpty()) return;
if (dono_cruzamento == trem) return;
dono_cruzamento = trem;
log_circulacao(trem, "pegou cruzamento_porteira");
/*ALCODEEND*/}

ResourcePool[] liberar_faixas_sem_mnb_pcx(Trem trem)
{/*ALCODESTART::1800100000001*/
// Release do início do passo (antes de esperar a próxima licença): solta faixas OCUPADAS vazias e o cruzamento.
// O mnb_pcx fica para o liberar_faixas_usadas (fim do passo), que roda depois de licenciar a marginal_c.
pular_mnb_pcx = true;
try {
    return liberar_faixas_ocupadas(trem);
} finally {
    pular_mnb_pcx = false;
}
/*ALCODEEND*/}

