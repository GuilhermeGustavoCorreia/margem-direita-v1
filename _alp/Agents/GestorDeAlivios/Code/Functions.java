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

int vazios_no_terminal 				= terminal.get_quantidade_vazios();
int quantos_vagoes_cabem_na_4000 	= quantos_vagoes_pegar_do_terminal();

int quantidade = Math.min(vazios_no_terminal, quantos_vagoes_cabem_na_4000);

traceln(time() + " | [qtd_alivio] " + ((Agent) terminal).getName()
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
    wait_trem_4000.free(wait_trem_4000.get(0));
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

// DEBUG ALT-003 (remover depois): a cada 60s, mostra por que um terminal em overload não está sendo aliviado
if (((int) time()) % 60 == 0) {
    for (ITerminalMoega t : colecaoMoegas) {
        float ov = t.getOverloadVazios();
        if (ov > 1) {
            traceln(time() + " | [diag_pst] " + ((Agent) t).getName()
                + " | overload=" + String.format("%.2f", ov)
                + " | em terminais_pst=" + terminais_pst.contains(t)
                + " | podeReceber(DVs)=" + podeReceber
                + " | rp_alivio_pst livre=" + rp_alivio_pst.idle() + "/" + rp_alivio_pst.size()
                + " | ferrovia=" + t.getFerrovia());
        }
    }
}

if (podeReceber && rp_alivio_pst.idle() > 0) {

    if (terminais_pst.isEmpty()) {
        
        return;
    }

    for (ITerminalMoega terminal : new ArrayList<>(terminais_pst)) {

        float overload = terminal.getOverloadVazios();
        boolean precisa_aliviar = (overload > 1);


        if (precisa_aliviar) {
            
            fazer_procedimento_de_alivio_pst(terminal);
            
        }
    }
}
/*ALCODEEND*/}

double fazer_procedimento_de_alivio_pst(ITerminalMoega terminal)
{/*ALCODESTART::1791469616777*/
String nomeTerminal = ((Agent) terminal).getName();
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
        if (((int) time()) % 60 == 0) {
            traceln(time() + " | [diag_pst] " + nomeTerminal + " | aguardando " + qtdLoco + " loco(s) '"
                + ("manobra".equals(tipoLoco) ? "manobra" : terminal.getFerrovia()) + "' para aliviar");
        }
        return;
    }

    colocar_terminal_em_alivio_pst(terminal);
    
    //criar novo trem
    boolean podeSolicitarLoco = condicao_solicitar_locoFips_pst();
   
    if (podeSolicitarLoco) {

        aux_esta_ocorrendo_alivio_pst = true;
        chamar_locomotiva_de_alivio_pst();
		
    } else {
        traceln(time() + " | [diag_pst] " + nomeTerminal + " | NAO solicitou loco: lote_alivio_pst != null");
    }

}
/*ALCODEEND*/}

boolean condicao_4000_pode_aliviar()
{/*ALCODESTART::1791470655489*/
int quantos_vagoes_faltam_linha_4000 = (CAPACIDADE_LINHA_4000 - main().getTamanhoDoTrem(lote_linha_4000));
boolean trem_esta_parado_na_4000 	 = !aux_esta_ocorrendo_alivio_pcx;
boolean precisa_de_vagoes_na_4000	 = quantos_vagoes_faltam_linha_4000 > 0;

if (trem_esta_parado_na_4000 && precisa_de_vagoes_na_4000){
	
	return true;
	
}else{

	return false;
	
}

/*ALCODEEND*/}

boolean condicao_4000_eh_compativel_com_terminal(ITerminalMoega terminal)
{/*ALCODESTART::1791470834261*/
String segmento_terminal = terminal.getSegmento();
String ferrovia_terminal = terminal.getFerrovia();

boolean mesma_ferrovia = (ferrovia_terminal == ferrovia_na_linha_4000);
boolean mesmo_segmento = (segmento_terminal == segmento_na_linha_4000);

boolean existe_tem_na_4000 		      = ferrovia_na_linha_4000 != null;
boolean produto_e_ferrovia_incompativel = !(mesma_ferrovia && mesmo_segmento);

if (existe_tem_na_4000 && produto_e_ferrovia_incompativel){
  
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

double colocar_terminal_em_alivio_pst(ITerminalMoega terminal)
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
int cabem_na_dv = 25; //main().gestorDeDVs.quantos_vagoes_cabem(terminal);
int quantidade = Math.min(vazios_no_terminal, cabem_na_dv);

traceln(time() + " | [qtd_alivio] " + ((Agent) terminal).getName()
    + " | vazios no terminal: " + vazios_no_terminal
    + " | cabem_na_dv0: " + cabem_na_dv
    + " | definido: " + quantidade
    + (cabem_na_dv < vazios_no_terminal ? " (limitado pela 4000)" : ""));

terminal.set_quantidade_alivio(quantidade);
/*ALCODEEND*/}

int quantos_vagoes_as_dvs_precisam(ITerminalMoega terminal)
{/*ALCODESTART::1791479898277*/
int quantos_vagoes_cabem = main().gestorDeDVs.quantos_vagoes_cabem(terminal);

return quantos_vagoes_cabem;
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
    traceln(time() + " | [diag_pst] criar " + alivio_em_andamento_pst.quantidade_locomotivas + " loco(s) de manobra em "
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

// DEBUG ALT-003
traceln(time() + " | [diag_pst] chamar loco | ferrovia=" + ferrovia + " | locos no wait=" + wait.size());

if (wait.size() > 0) {
    Agent loco = wait.remove(wait.get(0));
    loco_alivio_pst.take(loco);
} else {
    // DEBUG ALT-003
    traceln(time() + " | [diag_pst] SEM LOCO '" + ferrovia + "' -> alivio NAO iniciado e terminal ficou FORA de terminais_pst");
}
/*ALCODEEND*/}

Faixa get_faixa_da_posicao(PositionOnTrack pos)
{/*ALCODESTART::1791700000015*/
// faixa do GestorDeCirculacao cuja track contém a posição
if (pos == null) return null;

for (Faixa f : main().gestorDeCirculacao.faixas) {
    if (f != null && f.track == pos.getTrack()) return f;
}

traceln(time() + " | [diag_pst] nenhuma faixa contem a posicao " + pos.getName());
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
// ALT-013 alívio na linha 4000: pools da linha 4000 que o trem realmente possui
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

