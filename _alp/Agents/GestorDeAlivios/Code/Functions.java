double analisar_lotesVazios()
{/*ALCODESTART::1786122418756*/
for (ITerminalMoega  moega : colecaoMoegas) {
    
    //int vagoes_vazios = moega.getTotalDeVazios();
    int capacidade_vazios = 0;//moega.RECOMENDACAO_ALIVIO_VAZIOS;
    
   	// if (vagoes_vazios >= capacidade_vazios) {
   	//     traceln( moega + " tem verificar = true");
   	// }
    
}
/*ALCODEEND*/}

Main main()
{/*ALCODESTART::1786122452461*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

double atualiza_terminaisEmOverload()
{/*ALCODESTART::1786368889158*/
terminais_emOverload.clear();

for (ITerminalMoega terminal : colecaoMoegas) {
    
    float overload = terminal.getOverloadVazios();
    
    if (overload > 1) {
        terminais_emOverload.add(new TerminalOverload(terminal, overload));
    }
}

Collections.sort(terminais_emOverload, (a, b) ->
    Float.compare(b.overload, a.overload)
);

/*ALCODEEND*/}

boolean precisa_criarLocomotiva()
{/*ALCODESTART::1786391441265*/
//critérios para criar locomotiva

return true;
/*ALCODEEND*/}

double _buscar_tremQueFaraAlivio()
{/*ALCODESTART::1786454727406*/
/*if (precisa_criarLocomotiva()) {
	
	fazendo_alivio = main().gestorDeLocomotivas.solicitar_locomotivas_alivioTerminal(
		moega_alivio 	
	);

}*/
/*ALCODEEND*/}

double fazerUmAlivio()
{/*ALCODESTART::1786459009212*/
if(!fazendo_alivio && !terminais_emOverload.isEmpty()){
	
	moega_alivio = terminais_emOverload.get(0).terminal;
	
	_buscar_tremQueFaraAlivio();
}
/*ALCODEEND*/}

double fazer_procedimento_de_alivio_pcx(ITerminalMoega terminal)
{/*ALCODESTART::1790951682923*/
if (condicao_4000_eh_compativel_com_terminal(terminal)) {

    colocar_terminal_em_alivio(terminal);
	
	//criar novo trem
    if (condicao_solicitar_locoFips_pcx()) {
    	
    	aux_esta_ocorrendo_alivio_pcx = true;  
        loco_pcx_starts.inject();
    
    } 
    //chamar trem na 400
    else {
       	
       	aux_esta_ocorrendo_alivio_pcx = true;
        liberar_wait_trem_4000();
        
    }
} 
/*ALCODEEND*/}

int quantos_vagoes_pegar_do_terminal()
{/*ALCODESTART::1790952225174*/
return (CAPACIDADE_LINHA_4000 - main().getNumeroDeVagoes(lote_linha_4000));

/*ALCODEEND*/}

boolean condicao_solicitar_locoFips_pcx()
{/*ALCODESTART::1790952274499*/
if(lote_linha_4000 == null){
	return true;
}

return false;
/*ALCODEEND*/}

double analisar_pcx()
{/*ALCODESTART::1790952526150*/
if (condicao_4000_pode_aliviar()) {

    if (terminais_pcx.isEmpty()) {

        return;
    }

    for (ITerminalMoega terminal : new ArrayList<>(terminais_pcx)) {

        // um alívio por vez na linha 4000 (T39 e ADM no mesmo ciclo criavam 2 locos)
        if (aux_esta_ocorrendo_alivio_pcx) break;

        float overload = terminal.getOverloadVazios();
        boolean precisa_aliviar = (overload > 1);

        if (precisa_aliviar) {
       
            fazer_procedimento_de_alivio_pcx(terminal);
        }
    }
} 
/*ALCODEEND*/}

double colocar_terminal_em_alivio(ITerminalMoega terminal)
{/*ALCODESTART::1790972027064*/
terminais_pcx.remove(terminal);

alivio_em_andamento_pcx = new Alivio();
alivio_em_andamento_pcx.quantidade_locomotivas = 1;
alivio_em_andamento_pcx.iTermalMoegas_aliviar.add(terminal);

alivio_em_andamento_pcx.pos_criar_locomotiva 	= main().pos_locoAlivio_PST;
alivio_em_andamento_pcx.patio 					= terminal.getPatio();
alivio_em_andamento_pcx.ferrovia 				= terminal.getFerrovia();
alivio_em_andamento_pcx.segmento 				= terminal.getSegmento();

// o 1º terminal do trem da 4000 define a ferrovia e o segmento; os seguintes têm que bater
// (zeradas no para_alivio_na_linha_4000 quando o trem cheio sai)
if (ferrovia_na_linha_4000 == null) {
    ferrovia_na_linha_4000 = terminal.getFerrovia();
    segmento_na_linha_4000 = terminal.getSegmento();
    Log.info("GesAlivios", "alivio4000_compatibilidade", String.format("trem da 4000 definido por %s | ferrovia=%s segmento=%s",
        ((Agent) terminal).getName(), ferrovia_na_linha_4000, segmento_na_linha_4000));
}

int vazios_no_terminal 				= terminal.get_quantidade_vazios();
int quantos_vagoes_cabem_na_4000 	= quantos_vagoes_pegar_do_terminal();

int quantidade = Math.min(vazios_no_terminal, quantos_vagoes_cabem_na_4000);

Log.info("GesAlivios", "qtd_alivio", "" + ((Agent) terminal).getName()
    + " | vazios no terminal: " + vazios_no_terminal
    + " | cabem na 4000: " + quantos_vagoes_cabem_na_4000
    + " | definido: " + quantidade
    + (quantidade == quantos_vagoes_cabem_na_4000 && quantos_vagoes_cabem_na_4000 < vazios_no_terminal
        ? " (limitado pela 4000)" : ""));

terminal.set_quantidade_alivio(quantidade);
/*ALCODEEND*/}

String get_nome_recursos()
{/*ALCODESTART::1791210390554*/
if (alivio_em_andamento_pcx == null || alivio_em_andamento_pcx.iTermalMoegas_aliviar.isEmpty())
    return "-";

ResourcePool[][] conj = alivio_em_andamento_pcx.getConjuntoRecursos_faixas();

StringBuilder sb = new StringBuilder();

if (conj != null) {
    for (int i = 0; i < conj.length; i++) {
        if (conj[i] == null) continue;
        for (int j = 0; j < conj[i].length; j++) {
            ResourcePool rp = conj[i][j];
            if (rp == null) continue;
            if (sb.length() > 0) sb.append("\n ");
            sb.append(rp.getName());
        }
    }
}
return sb.toString();
/*ALCODEEND*/}

ResourcePool[][] todosNumConjunto(ResourcePool[][] conj)
{/*ALCODESTART::1791211558240*/
if (conj == null) return new ResourcePool[0][];
ArrayList<ResourcePool> todos = new ArrayList<>();
for (ResourcePool[] linha : conj) {
    if (linha == null) continue;
    for (ResourcePool rp : linha) {
        if (rp != null) todos.add(rp);
    }
}
return new ResourcePool[][] { todos.toArray(new ResourcePool[0]) };
/*ALCODEEND*/}

double liberar_wait_trem_4000()
{/*ALCODESTART::1791232984589*/
// libera o primeiro que entrou
if (wait_trem_4000.size() > 0) {
    Agent t = wait_trem_4000.get(0);
    Log.info("GesAlivios", "alivio_pcx", String.format("trem=%d liberado da 4000", t.getId()));
    wait_trem_4000.free(t);
} else {
    Log.warn("GesAlivios", "alivio_pcx", "nenhum trem na 4000 para liberar");
}
/*ALCODEEND*/}

double ocuparFaixas(ArrayList<Faixa> faixas)
{/*ALCODESTART::1791309726871*/
if (faixas == null) {
    return;
}

for (Faixa f : faixas) {
    f.ocupar_agente(9999);
}
/*ALCODEEND*/}

double liberarFaixas(ArrayList<Faixa> faixas)
{/*ALCODESTART::1791310118687*/
if (faixas == null) {
    return;
}

for (Faixa f : faixas) {
    f.liberar();
}
/*ALCODEEND*/}

double analisar_pst()
{/*ALCODESTART::1791466610125*/
boolean podeReceber = main().gestorDeDVs.condicao_pode_receber_alivio();

// DEBUG (remover depois): a cada 60s, mostra por que um terminal em overload não está sendo aliviado
if (trace_repetitivo && ((int) time()) % 60 == 0) {
    for (ITerminalMoega t : colecaoMoegas) {
        float ov = t.getOverloadVazios();
        if (ov > 1) {
            Log.info("GesAlivios", "diag_pst", "" + ((Agent) t).getName()
                + " | overload=" + String.format("%.2f", ov)
                + " | em terminais_pst=" + terminais_pst.contains(t)
                + " | podeReceber(DVs)=" + podeReceber
                + " | rp_alivio_pst livre=" + rp_alivio_pst.idle() + "/" + rp_alivio_pst.size()
                + " | ferrovia=" + t.getFerrovia());
        }
    }
}

// um alívio PST por vez: o anterior só acaba quando terminar todas as moegas
// (o rp_alivio_pst só é pego no fim do fluxo da loco; até lá o alívio já está em andamento)
boolean alivio_pst_em_andamento = alivio_em_andamento_pst != null && !alivio_em_andamento_pst.finalizou_moegas();
if (podeReceber && rp_alivio_pst.idle() > 0 && !alivio_pst_em_andamento) {

    if (terminais_pst.isEmpty()) {
        
        return;
    }

    for (ITerminalMoega terminal : new ArrayList<>(terminais_pst)) {

        float overload = terminal.getOverloadVazios();
        boolean precisa_aliviar = (overload > 1);


        if (precisa_aliviar) {
            
            fazer_procedimento_de_alivio_pst(terminal);
            
            // um alívio PST por vez: o terminal saiu da lista, o alívio começou
            if (!terminais_pst.contains(terminal)) return;
            
        }
    }
}
/*ALCODEEND*/}

double fazer_procedimento_de_alivio_pst(ITerminalMoega terminal)
{/*ALCODESTART::1791469616777*/
String nomeTerminal = ((Agent) terminal).getName();

// "proprio": o terminal alivia com a loco que já está no trem (TEC). Não pede loco nem cria trem.
// Sai de terminais_pst para não ser analisado de novo; o alívio do terminal o devolve à lista quando terminar.
if ("proprio".equals(terminal.get_tipo_loco_alivio())) {
    terminais_pst.remove(terminal);
    Log.info("GesAlivios", "alivio_proprio", nomeTerminal + " | alivio com a loco do proprio terminal");
    terminal.takeAlivio(null);
    return;
}

boolean condicao_DVs_eh_compativel_com_terminal = main().gestorDeDVs.condicao_pode_receber_alivio();

if (condicao_DVs_eh_compativel_com_terminal) {

    // só tira o terminal da análise se houver locomotiva para o alívio.
    // manobra: unidades livres em rp_locoFips_pst | ferrovia: loco no wait da ferrovia em atendimento.
    // Sem loco, o terminal continua em terminais_pst e o alívio dispara quando ela estiver disponível.
    String tipoLoco = terminal.get_tipo_loco_alivio();
    int    qtdLoco  = terminal.get_quantidade_loco_alivio();
    boolean temLoco;

    if ("manobra".equals(tipoLoco)) {
    
        temLoco = get_rp_loco_fips(terminal.getPatio()).idle() >= qtdLoco;
        
    } else {
    
        String ferroviaTerminal = terminal.getFerrovia();
        Wait<Agent> waitLocos = null;
        if      ("rumo".equals(ferroviaTerminal)) waitLocos = main().gestorDeLocomotivas.locomotivasRumo;
        else if ("mrs".equals(ferroviaTerminal))  waitLocos = main().gestorDeLocomotivas.locomotivasMRS;
        else if ("vli".equals(ferroviaTerminal))  waitLocos = main().gestorDeLocomotivas.locomotivasVLI;
        temLoco = (waitLocos != null && waitLocos.size() > 0);
        
    }

    if (!temLoco) {
    
        if (trace_repetitivo && ((int) time()) % 60 == 0) {
            Log.info("GesAlivios", "diag_pst", "" + nomeTerminal + " | aguardando " + qtdLoco + " loco(s) '"
                + ("manobra".equals(tipoLoco) ? "manobra" : terminal.getFerrovia()) + "' para aliviar");
        }
        
        return;
        
    }

    // a loco nasce na faixa de criação: só começa outro alívio quando ela estiver LIVRE.
    // A faixa fica OCUPADA com o id da loco desde a criação e é liberada pela circulação
    // (liberar_faixas_ocupadas) quando o trem de volta sai dela.
    PositionOnTrack pos_criacao = "manobra".equals(tipoLoco) ? main().pos_locoManobra_PST : main().pos_locoAlivio_PST;
    Faixa faixa_criacao = get_faixa_da_posicao(pos_criacao);

    if (faixa_criacao != null && faixa_criacao.estado != Faixa.EstadoFaixa.LIVRE) {

        if (trace_repetitivo && ((int) time()) % 60 == 0) {
            Log.info("GesAlivios", "diag_pst", "" + nomeTerminal + " | aguardando faixa de criacao: " + faixa_criacao.getStatusTexto());
        }

        return;

    }

    // faixas de circulação que o trem pode ocupar durante o alívio: também precisam estar LIVRES
    ArrayList<Faixa> faixas_saida = terminal.get_primeiras_linhas_de_circulacao_vazios();
    if (faixas_saida != null) {
        for (Faixa f : faixas_saida) {
            if (f == null || f == faixa_criacao || f.recurso == null) continue;
            if (f.estado != Faixa.EstadoFaixa.LIVRE) {
                if (trace_repetitivo && ((int) time()) % 60 == 0) {
                    Log.info("GesAlivios", "diag_pst", "" + nomeTerminal + " | aguardando faixa de saida do alivio: " + f.getStatusTexto());
                }
                return;
            }
        }
    }

    // DVs: com 0 o terminal fica em terminais_pst e tenta de novo na proxima analise
    // consulta uma vez so: o mesmo valor testado aqui define a quantidade do alivio
    int cabem_na_dv = quantos_vagoes_as_dvs_precisam(terminal);
    if (cabem_na_dv <= 0) {

        if (trace_repetitivo && ((int) time()) % 60 == 0) {
            Log.info("GesAlivios", "diag_pst", "" + nomeTerminal + " | DVs nao aceitam vagoes deste terminal agora (cabem=0)");
        }

        return;

    }

    colocar_terminal_em_alivio_pst(terminal, cabem_na_dv);
    
    //criar novo trem
    boolean podeSolicitarLoco = condicao_solicitar_locoFips_pst();
   
    if (podeSolicitarLoco) {

        aux_esta_ocorrendo_alivio_pst = true;
        chamar_locomotiva_de_alivio_pst();
		
    } else {
        Log.info("GesAlivios", "diag_pst", "" + nomeTerminal + " | NAO solicitou loco: lote_alivio_pst != null");
    }

}
/*ALCODEEND*/}

boolean condicao_4000_pode_aliviar()
{/*ALCODESTART::1791470655489*/
// conta so vagoes (o trem na 4000 tem as locos do alivio); igual ao quantos_vagoes_pegar_do_terminal
int quantos_vagoes_faltam_linha_4000 = (CAPACIDADE_LINHA_4000 - main().getNumeroDeVagoes(lote_linha_4000));
boolean trem_esta_parado_na_4000 	 = !aux_esta_ocorrendo_alivio_pcx;
boolean precisa_de_vagoes_na_4000	 = quantos_vagoes_faltam_linha_4000 > 0;

// sem trem na 4000, a loco nova nasce na intervalo4000_b. Espera o trem cheio anterior sair dela.
boolean saida_4000_livre 			 = (lote_linha_4000 != null) || saida_4000_livre();

if (trem_esta_parado_na_4000 && precisa_de_vagoes_na_4000 && saida_4000_livre){
	
	return true;
	
}else{

	return false;
	
}

/*ALCODEEND*/}

boolean condicao_4000_eh_compativel_com_terminal(ITerminalMoega terminal)
{/*ALCODESTART::1791470834261*/
String segmento_terminal = terminal.getSegmento();
String ferrovia_terminal = terminal.getFerrovia();

// String comparada com equals (== compara referência)
boolean mesma_ferrovia = java.util.Objects.equals(ferrovia_terminal, ferrovia_na_linha_4000);
boolean mesmo_segmento = java.util.Objects.equals(segmento_terminal, segmento_na_linha_4000);

boolean existe_tem_na_4000 		      = ferrovia_na_linha_4000 != null;
boolean produto_e_ferrovia_incompativel = !(mesma_ferrovia && mesmo_segmento);

if (existe_tem_na_4000 && produto_e_ferrovia_incompativel){

    // terminal continua em terminais_pcx e espera o trem atual sair cheio
    if (trace_repetitivo && ((int) time()) % 60 == 0) {
        Log.info("GesAlivios", "alivio4000_compatibilidade", String.format("%s espera | terminal: %s/%s | trem da 4000: %s/%s",
            ((Agent) terminal).getName(), ferrovia_terminal, segmento_terminal,
            ferrovia_na_linha_4000, segmento_na_linha_4000));
    }
    return false;
    	
}

return true;
/*ALCODEEND*/}

boolean condicao_solicitar_locoFips_pst()
{/*ALCODESTART::1791479660737*/
if(lote_alivio_pst == null){

	return true;
	
}

return false;
/*ALCODEEND*/}

double colocar_terminal_em_alivio_pst(ITerminalMoega terminal,int cabem_na_dv)
{/*ALCODESTART::1791479727624*/
//retira ele da próxima análise
terminais_pst.remove(terminal);

//cria um novo alívio
alivio_em_andamento_pst = new Alivio();
alivio_em_andamento_pst.iTermalMoegas_aliviar.add(terminal);

// tipo e quantidade de locos vêm do terminal
alivio_em_andamento_pst.tipo_loco 				= terminal.get_tipo_loco_alivio();
alivio_em_andamento_pst.quantidade_locomotivas 	= terminal.get_quantidade_loco_alivio();
alivio_em_andamento_pst.pos_criar_locomotiva 	= alivio_em_andamento_pst.ehLocoManobra()
													? main().pos_locoManobra_PST
													: main().pos_locoAlivio_PST;
alivio_em_andamento_pst.faixa_criacao 			= get_faixa_da_posicao(alivio_em_andamento_pst.pos_criar_locomotiva);
alivio_em_andamento_pst.patio 					= terminal.getPatio();
alivio_em_andamento_pst.ferrovia 				= terminal.getFerrovia();
alivio_em_andamento_pst.segmento 				= terminal.getSegmento();

//define quantos vagões pegar do terminal
int vazios_no_terminal = terminal.get_quantidade_vazios();
int quantidade = Math.min(vazios_no_terminal, cabem_na_dv);

Log.info("GesAlivios", "qtd_alivio", "" + ((Agent) terminal).getName()
    + " | vazios no terminal: " + vazios_no_terminal
    + " | cabem_na_dv0: " + cabem_na_dv
    + " | definido: " + quantidade
    + (cabem_na_dv < vazios_no_terminal ? " (limitado pelas DVs)" : ""));

terminal.set_quantidade_alivio(quantidade);
/*ALCODEEND*/}

int quantos_vagoes_as_dvs_precisam(ITerminalMoega terminal)
{/*ALCODESTART::1791479898277*/
int quantos_vagoes_cabem = main().gestorDeDVs.quantos_vagoes_cabem(terminal);

return 4; //quantos_vagoes_cabem;
/*ALCODEEND*/}

ResourcePool get_recurso_locomotiva_pst()
{/*ALCODESTART::1791480425887*/
// loco FIPS: pool do pátio do terminal (getPatio)
if (alivio_em_andamento_pst.ehLocoManobra()) {
    return get_rp_loco_fips(alivio_em_andamento_pst.patio);
}

String ferrovia = alivio_em_andamento_pst.ferrovia;

switch (ferrovia) {
    
    case "rumo":
        
        return main().gestorDeLocomotivas.rp_locoRumo;
    
    case "mrs":
        
        return main().gestorDeLocomotivas.rp_locoMRS;
    
    case "vli":
        
        return main().gestorDeLocomotivas.rp_locoVLI;
    
    default:
        
        error("Ferrovia desconhecida: '" + ferrovia + "'");
        return null;
        
}
/*ALCODEEND*/}

String terminais_pcx_getName()
{/*ALCODESTART::1791482627202*/
StringBuilder sb = new StringBuilder();
for (ITerminalMoega t : terminais_pcx) {
    sb.append(((Agent) t).getName())
      .append(": ")
      .append(String.format("%.2f", t.getOverloadVazios()))
      .append("\n");
}
return sb.toString();
/*ALCODEEND*/}

String terminais_pst_getName()
{/*ALCODESTART::1791482690090*/
StringBuilder sb = new StringBuilder();
for (ITerminalMoega t : terminais_pst) {
    sb.append(((Agent) t).getName())
      .append(": ")
      .append(String.format("%.2f", t.getOverloadVazios()))
      .append("\n");
}
return sb.toString();
/*ALCODEEND*/}

double chamar_locomotiva_de_alivio_pst()
{/*ALCODESTART::1791545494501*/
// loco de manobra: cria as locos FIPS PST em pos_locoManobra_PST (entram direto nos seizes)
if (alivio_em_andamento_pst.ehLocoManobra()) {
    Log.info("GesAlivios", "diag_pst", "criar " + alivio_em_andamento_pst.quantidade_locomotivas + " loco(s) de manobra em "
        + alivio_em_andamento_pst.pos_criar_locomotiva.getName());
    loco_manobra_alivio_pst.inject();
    return;
}

String ferrovia = alivio_em_andamento_pst.ferrovia;
Wait<Agent> wait;

switch (ferrovia) {
    case "rumo": wait = main().gestorDeLocomotivas.locomotivasRumo; break;
    case "mrs":  wait = main().gestorDeLocomotivas.locomotivasMRS;  break;
    case "vli":  wait = main().gestorDeLocomotivas.locomotivasVLI;  break;
    default:
        error("Ferrovia desconhecida: '" + ferrovia + "'");
        return;
}

// DEBUG
Log.info("GesAlivios", "diag_pst", "chamar loco | ferrovia=" + ferrovia + " | locos no wait=" + wait.size());

if (wait.size() > 0) {
    Agent loco = wait.remove(wait.get(0));
    Log.info("GesAlivios", "diag_pst", String.format("loco do alivio (%s - %d)", ((Trem) loco).prefixo, loco.getId()));
    loco_alivio_pst.take(loco);
} else {
    // DEBUG
    Log.warn("GesAlivios", "diag_pst", "SEM LOCO '" + ferrovia + "' -> alivio NAO iniciado e terminal ficou FORA de terminais_pst");
}
/*ALCODEEND*/}

Faixa get_faixa_da_posicao(PositionOnTrack pos)
{/*ALCODESTART::1791700000015*/
// faixa do GestorDeCirculacao cuja track contém a posição
if (pos == null) return null;

for (Faixa f : main().gestorDeCirculacao.faixas) {
    if (f != null && f.track == pos.getTrack()) return f;
}

Log.warn("GesAlivios", "diag_pst", "nenhuma faixa contem a posicao " + pos.getName());
return null;
/*ALCODEEND*/}

ResourcePool get_rp_loco_fips(String patio)
{/*ALCODESTART::1791700000040*/
// pool de locos FIPS do pátio do terminal (padrão minúsculo)
switch (patio) {
    case "pst": return main().gestorDeLocomotivas.rp_locoFips_pst;
    case "pmc": return main().gestorDeLocomotivas.rp_locoFips_pmc;
    case "pcx": return main().gestorDeLocomotivas.rp_locoFips_pcx;
    default:
        error("Patio desconhecido para loco FIPS: '" + patio + "'");
        return null;
}
/*ALCODEEND*/}

ResourcePool[] get_recursos_linha_4000(Trem trem)
{/*ALCODESTART::1791950000012*/
// alívio na linha 4000: pools da linha 4000 que o trem realmente possui
// (Release em SPECIFIED_RESOURCES dá erro se o agente não tiver a unidade)
List<ResourcePool> pools = new ArrayList<>();
for (Faixa f : java.util.List.of(
        main().gestorDeCirculacao.faixa_linha4000_a,
        main().gestorDeCirculacao.faixa_linha4000_b,
        main().gestorDeCirculacao.faixa_linha4000_c)) {
    if (!trem.resourceUnitsOfPool(f.recurso).isEmpty()) pools.add(f.recurso);
}
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

double analisar_pmc()
{/*ALCODESTART::1792014000316*/
if (terminais_pmc.isEmpty()) {
    return;
}

for (ITerminalMoega terminal : new ArrayList<>(terminais_pmc)) {

    if (terminal_alivio_pmc != null) break;

    if (terminal.getOverloadVazios() > 1) {

        fazer_procedimento_de_alivio_pmc(terminal);
    }
}
/*ALCODEEND*/}

double fazer_procedimento_de_alivio_pmc(ITerminalMoega terminal)
{/*ALCODESTART::1792014000317*/
// um alívio do PMC por vez
if (terminal_alivio_pmc != null) {
    return;
}

String ferrovia = terminal.getFerrovia();
Wait<Agent> waitLocos = get_wait_locos_ferrovia(ferrovia);

if (waitLocos == null || waitLocos.size() == 0) {
    if (trace_repetitivo && ((int) time()) % 60 == 0) {
        Log.info("GesAlivios", "alivio_pmc", "" + ((Agent) terminal).getName()
            + " | aguardando loco '" + ferrovia + "' para aliviar");
    }
    return;
}

terminais_pmc.remove(terminal);
terminal_alivio_pmc = terminal;
terminal.set_quantidade_alivio(terminal.get_quantidade_vazios());

Agent loco = waitLocos.remove(waitLocos.get(0));
((Trem) loco).set_terminal(terminal);
contador_mnb = (contador_mnb >= 99) ? 1 : contador_mnb + 2;
((Trem) loco).prefixo = String.format("MNB%02d", contador_mnb);

Log.info("GesAlivios", "alivio_pmc", String.format("%s | vazios=%d | loco=%d (%s)",
    ((Agent) terminal).getName(), terminal.get_quantidade_vazios(), loco.getId(), ferrovia));

loco_alivio_pmc.take(loco);
/*ALCODEEND*/}

Wait<Agent> get_wait_locos_ferrovia(String ferrovia)
{/*ALCODESTART::1792014000318*/
// wait de locos da ferrovia no GestorDeLocomotivas
if (ferrovia == null) return null;
switch (ferrovia) {
    case "rumo": return main().gestorDeLocomotivas.locomotivasRumo;
    case "mrs":  return main().gestorDeLocomotivas.locomotivasMRS;
    case "vli":  return main().gestorDeLocomotivas.locomotivasVLI;
    default:     return null;
}
/*ALCODEEND*/}

double retornar_alivio_pmc(Trem trem)
{/*ALCODESTART::1792780000101*/
// fim do alívio de um terminal do PMC. Chamado pelo terminal quando o trem sai com os vazios.
// O terminal define trem.faixa_atual / trem.proxima_faixa antes de chamar (cada um tem sua saída).
// Solta a trava, devolve o terminal à análise e entrega o trem ao GestorDeCirculacao.
ITerminalMoega terminal = terminal_alivio_pmc;
terminal_alivio_pmc = null;

if (terminal != null && !terminais_pmc.contains(terminal)) {
    terminais_pmc.add(terminal);
}

trem.eh_alivio = true;

// terminal de onde o MNB saiu
if (terminal != null) trem.origem = ((Agent) terminal).getName();

Log.info("GesAlivios", "retornar_alivio_pmc", String.format("trem=%d vagoes=%d | %s volta para terminais_pmc",
    trem.getId(), main().getNumeroDeVagoes(trem),
    terminal == null ? "-" : ((Agent) terminal).getName()));

// entrega o trem ao GestorDeCirculacao (soltar_mnb_pcx_pmc -> para_circulacao_alivio_pmc);
// o mnb_pcx vai junto e é solto quando a cauda passa da marginal_c
soltar_mnb_pcx_pmc.take(trem);
/*ALCODEEND*/}

ResourcePool[][] get_conjunto_faixas_pmc()
{/*ALCODESTART::1792795000006*/
// recursos das faixas que a loco do alívio PMC licencia até o terminal.
// Lista vazia: só avisa no log (o Seize passa sem recurso).
ArrayList<Faixa> faixas = terminal_alivio_pmc.get_faixas_primeiro_alivio();
List<ResourcePool> recursos = new ArrayList<ResourcePool>();
if (faixas != null) {
	for (Faixa f : faixas) {
		if (f != null && f.recurso != null && !recursos.contains(f.recurso)) recursos.add(f.recurso);
	}
}
// Suzano e Eldorado: a loco também pede a linha 6000 (no mesmo conjunto),
// para não entrar enquanto um trem de carregados manobra nela.
// A Suzano e o Eldorado soltam a linha 6000 da loco em get_recursos_loco_alivio.
// A Bracell fica de fora: o alívio dela não tem Release da linha 6000.
if (terminal_alivio_pmc == main().suzano || terminal_alivio_pmc == main().eldorado) {
	ResourcePool rp6000 = main().gestorDeCirculacao.rp_linha6000;
	if (!recursos.contains(rp6000)) recursos.add(rp6000);
}
if (recursos.isEmpty()) {
	Log.warn("GesAlivios", "bloquear_faixas_pmc", String.format("%s sem faixas de alívio (get_faixas_primeiro_alivio vazio)",
		((Agent) terminal_alivio_pmc).getName()));
}
return new ResourcePool[][] { recursos.toArray(new ResourcePool[0]) };
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_saida_4000()
{/*ALCODESTART::1793310000001*/
// faixas onde fica o trem da linha 4000 (pos_linha_4000_criar_loco fica na intervalo4000_b)
GestorDeCirculacao gc = main().gestorDeCirculacao;

return new ArrayList<Faixa>(java.util.List.of(
    gc.faixa_intervalo4000_b,
    gc.faixa_intervalo4000_c,
    gc.faixa_linha4000_a,
    gc.faixa_linha4000_b,
    gc.faixa_linha4000_c
));
/*ALCODEEND*/}

boolean saida_4000_livre()
{/*ALCODESTART::1793310000002*/
// true só se todas as faixas da saída da 4000 estiverem LIVRE.
// O GestorDeCirculacao solta cada uma (liberar_faixas_ocupadas) quando a cauda do trem cheio sai da track.
for (Faixa f : get_faixas_saida_4000()) {

    if (f.estado != Faixa.EstadoFaixa.LIVRE) {

        if (trace_repetitivo && ((int) time()) % 60 == 0) {
            Log.info("GesAlivios", "saida_4000_livre", String.format("aguardando %s",
                f.getStatusTexto()));
        }
        return false;
    }
}

return true;
/*ALCODEEND*/}

double ocupar_faixas_saida_4000(Trem trem)
{/*ALCODESTART::1793310000003*/
// na saída do trem cheio, as faixas da 4000 onde ele ainda está ficam OCUPADA com o id dele.
// As outras ficam LIVRE (as da linha 4000 estavam com 9999 desde o bloquear_4000).
List<RailwayTrack> tracksDoTrem = trem.getAllOccupiedTracks();
GestorDeCirculacao gc = main().gestorDeCirculacao;

for (Faixa f : get_faixas_saida_4000()) {

    // intervalo4000_c/b: o trem pegou no seize_saida_4000 e ainda vai passar por elas
    boolean caminho_de_saida = (f == gc.faixa_intervalo4000_b || f == gc.faixa_intervalo4000_c)
                               && !trem.resourceUnitsOfPool(f.recurso).isEmpty();

    if (tracksDoTrem.contains(f.track) || caminho_de_saida) {

        f.ocupar(trem);
        Log.info("GesAlivios", "ocupar_faixas_saida_4000", String.format("trem=%d ocupa %s",
            trem.getId(), f.recurso.getName()));

    } else {

        f.liberar();
    }
}
/*ALCODEEND*/}

ResourcePool[][] conjunto_alivio_pcx()
{/*ALCODESTART::1798200000503*/
// alívio do PCX: faixas até o terminal. O rp_acesso_moema é pego antes, sozinho, no pegar_acesso_moema_pcx:
// só quem tem o acesso entra na fila das linhas da área
ArrayList<ResourcePool> todos = new ArrayList<>();
ResourcePool[][] faixas = todosNumConjunto(alivio_em_andamento_pcx.getConjuntoRecursos_faixas());
for (ResourcePool[] linha : faixas) {
    if (linha == null) continue;
    for (ResourcePool rp : linha) {
        if (rp != null && !todos.contains(rp)) todos.add(rp);
    }
}
return new ResourcePool[][] { todos.toArray(new ResourcePool[0]) };
/*ALCODEEND*/}

double diag_alivio_pcx()
{/*ALCODESTART::1798200000605*/
// alívio do PCX parado no bloquear_faixas_ate_terminal: a cada 5 min mostra quem segura o quê
if (t_espera_alivio_pcx < 0 || trem_espera_alivio_pcx == null) return;
double espera = time() - t_espera_alivio_pcx;
if (espera < 60) return;
if (t_ultimo_diag_alivio_pcx >= 0 && time() - t_ultimo_diag_alivio_pcx < 300) return;
t_ultimo_diag_alivio_pcx = time();

GestorDeCirculacao gc = main().gestorDeCirculacao;
Agent dono = gc.dono_acesso_moema;
String destino = (alivio_em_andamento_pcx == null || alivio_em_andamento_pcx.get_moega_atual() == null)
    ? "-" : ((Agent) alivio_em_andamento_pcx.get_moega_atual()).getName();
Log.warn("GesAlivios", "alivio_espera", String.format("trem=%d -> %s | esperando ha %.2fh | acesso idle=%d dono=%s | locos idle=%d busy=%d | wait_4000=%d",
    trem_espera_alivio_pcx.getId(), destino, espera / 3600.0,
    gc.rp_acesso_moema.idle(),
    dono == null ? "-" : (dono instanceof Trem ? ((Trem) dono).prefixo + "/" + dono.getId() : String.valueOf(dono)),
    main().gestorDeLocomotivas.rp_locoFips_pcx.idle(), main().gestorDeLocomotivas.rp_locoFips_pcx.busy(),
    wait_trem_4000.size()));

if (alivio_em_andamento_pcx != null && alivio_em_andamento_pcx.getFaixas() != null) {
    for (Faixa f : alivio_em_andamento_pcx.getFaixas()) {
        if (f == null) continue;
        StringBuilder trens = new StringBuilder();
        if (f.track != null) {
            for (Object t : f.track.getTrains()) trens.append(((Agent) t).getId()).append(" ");
        }
        Log.warn("GesAlivios", "alivio_espera", String.format("  %s | estado=%s id=%d idle=%d | no track: %s",
            f.recurso == null ? "-" : f.recurso.getName(), f.estado, f.idTremLicenciado,
            f.recurso == null ? -1 : f.recurso.idle(),
            trens.length() == 0 ? "vazio" : trens.toString().trim()));
    }
}

T39 t39 = main().t39;
Log.warn("GesAlivios", "alivio_espera", String.format("  T39 | esperar_espaco_corte=%d linha11_idle=%d corte=%d livre=%d wait_espaco_vazios=%d",
    t39.esperar_espaco_corte.size(), t39.linha_11_ocupada.idle(), t39.tamanho_proximo_corte(), t39.espaco_livre_vazios(),
    t39.wait_espaco_vazios.size()));
Log.warn("GesAlivios", "alivio_espera", String.format("  overload | TES=%.2f ADM=%.2f T39=%.2f",
    main().tes.getOverloadVazios(), main().adm.getOverloadVazios(), t39.getOverloadVazios()));
/*ALCODEEND*/}

ResourcePool[] get_recursos_intervalo_criacao(Trem trem)
{/*ALCODESTART::1797210000001*/
// loco nova da 4000: intervalo4000_b/c pegas no bloquear_4000 (a loco nasce na intervalo4000_b).
// Só os pools que o trem possui: o trem que já estava na 4000 passa aqui sem eles.
List<ResourcePool> pools = new ArrayList<>();
for (Faixa f : java.util.List.of(
        main().gestorDeCirculacao.faixa_intervalo4000_b,
        main().gestorDeCirculacao.faixa_intervalo4000_c)) {
    if (!trem.resourceUnitsOfPool(f.recurso).isEmpty()) pools.add(f.recurso);
}
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

ResourcePool[][] pools_intervalo_faltando(Trem trem)
{/*ALCODESTART::1797214000021*/
// intervalo4000_c/b que o trem ainda não segura (vazio se já tem as duas: o Seize passa direto)
List<ResourcePool> pools = new ArrayList<>();
for (Faixa f : java.util.List.of(
        main().gestorDeCirculacao.faixa_intervalo4000_c,
        main().gestorDeCirculacao.faixa_intervalo4000_b)) {
    if (trem.resourceUnitsOfPool(f.recurso).isEmpty()) pools.add(f.recurso);
}
return new ResourcePool[][] { pools.toArray(new ResourcePool[0]) };
/*ALCODEEND*/}

ResourcePool[][] get_intervalo_para_avancar(Trem trem)
{/*ALCODESTART::1797214000022*/
// alívio da 4000 no T39/ADM: acima do limite, o avanço até pos_linha_4000_criar_loco entra na intervalo4000_b
if (main().getNumeroDeVagoes(trem) <= LIMITE_VGS_INTERVALO_4000) return new ResourcePool[][] { new ResourcePool[0] };
return pools_intervalo_faltando(trem);
/*ALCODEEND*/}

ResourcePool[][] mnb_pcx_faltando(Trem trem)
{/*ALCODESTART::1797216000001*/
// mnb_pcx só se o trem ainda não segura (vazio: o Seize passa direto)
ResourcePool rp = main().gestorDeCirculacao.rp_mnb_pcx;
if (!trem.resourceUnitsOfPool(rp).isEmpty()) return new ResourcePool[][] { new ResourcePool[0] };
return new ResourcePool[][] { { rp } };
/*ALCODEEND*/}

ResourcePool[][] get_mnb_pcx_para_avancar(Trem trem)
{/*ALCODESTART::1797216000002*/
// avanço do alívio da 4000 no T39/ADM: acima do limite entra na intervalo4000, então pega o mnb_pcx antes
if (main().getNumeroDeVagoes(trem) <= LIMITE_VGS_INTERVALO_4000) return new ResourcePool[][] { new ResourcePool[0] };
return mnb_pcx_faltando(trem);
/*ALCODEEND*/}
