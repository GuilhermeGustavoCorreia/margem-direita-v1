double pararSimulacao()
{/*ALCODESTART::1773258126585*/
traceln("parando simulacao");
pauseSimulation();
traceln("parou simulacao");
/*ALCODEEND*/}

int getVagosNaLinha(Object track)
{/*ALCODESTART::1782267899505*/
int totalVagoes = 0;

com.anylogic.engine.markup.RailwayTrack linha =
    (com.anylogic.engine.markup.RailwayTrack) track;

for( Agent a : linha.getTrains() ){
    if( a instanceof Trem ){
        totalVagoes += ((Trem) a).getNumeroDeVagoes();
    }
}

return totalVagoes;
/*ALCODEEND*/}

RailwayTrack getLinhaCirculacao()
{/*ALCODESTART::1782412019217*/
if (_getTotalCarrosNasTracks(new RailwayTrack[] { pstL7 }) == 0) {
	return pstL7;
}

if (_getTotalCarrosNasTracks(new RailwayTrack[] { pstL5 }) == 0) {
	return pstL5;
}

if (_getTotalCarrosNasTracks(new RailwayTrack[] { pstL3 }) == 0) {
	return pstL3;
}

if (_getTotalCarrosNasTracks(new RailwayTrack[] { via3_a }) == 0) {
	return pstL1;
}

return null;

/*ALCODEEND*/}

String getDestinoDoTrem(Trem train)
{/*ALCODESTART::1782498772574*/
if (train == null) return null;

int n = 0;
try {
    
    while (train.getCar(n) != null) {
        
        Agent car = train.getCar(n);
        
        if (car instanceof Vagao) {
        	
            return ((Vagao) car).moegaDestino;
            
        }
        n++;
    }
} catch (Exception e) {}

return null;
/*ALCODEEND*/}

String getBlocoParaAtentimento(Trem train)
{/*ALCODESTART::1782501199582*/
if (train == null) return null;

int n = 0;
try {
    
    while (train.getCar(n) != null) {
        
        Agent car = train.getCar(n);
        
        if (car instanceof Vagao) {
        	
            return ((Vagao) car).moegaDestino;
            
        }
        n++;
    }
} catch (Exception e) {}

return null;
/*ALCODEEND*/}

int _getTotalCarrosNasTracks(RailwayTrack[] tracks)
{/*ALCODESTART::1782842439071*/
java.util.HashSet<Object> carrosContados = new java.util.HashSet<Object>();

for (RailwayTrack track : tracks) {
	
	if (track == null) {
		continue;
	}
	
	carrosContados.addAll(track.getCars());
}

return carrosContados.size();
/*ALCODEEND*/}

int dev_chamarLocoMnb()
{/*ALCODESTART::1782842640969*/
/*if (
	_getTotalCarrosNasTracks(
		new RailwayTrack[] { pstLinha03, pstLinha04 }
	) >= 8 
){
	cliMar.locoMnb.inject();
}*/
/*ALCODEEND*/}

int getVagosNaLinhaSemTrem(Trem tremAtual,Object track)
{/*ALCODESTART::1782845058198*/
int totalVagoes = 0;

com.anylogic.engine.markup.RailwayTrack linha =
    (com.anylogic.engine.markup.RailwayTrack) track;

for (Agent a : linha.getTrains()) {
    if (a instanceof Trem && a != tremAtual) {
        totalVagoes += ((Trem) a).getNumeroDeVagoes();
    }
}

return totalVagoes;
/*ALCODEEND*/}

int getLoteAtendimento_efetivo(Trem trem)
{/*ALCODESTART::1782845989180*/
Vagao primeiroVagao = trem.getPrimeiroVagao();

int numero_de_vagoes = trem.size();

String moega 		= primeiroVagao.moegaDestino;

int loteEmVagoes 	= getLoteAtendimento_nominal(moega);
trem.posicaoDeCorte = loteEmVagoes;

return (loteEmVagoes > numero_de_vagoes) ? numero_de_vagoes : loteEmVagoes ;
/*ALCODEEND*/}

int getLoteAtendimento_nominal(String moega)
{/*ALCODESTART::1782846039922*/
List<Number> valores = selectValues(
    Number.class,
    "SELECT lote_atendimento FROM parametros_terminais WHERE moega = ?",
    moega
);

if (valores.isEmpty()) {
    error("Moega não encontrada em parametros_terminais: " + moega);
    return 0;
}

return valores.get(0).intValue();
/*ALCODEEND*/}

int getVagoesLinhaAtendimento()
{/*ALCODESTART::1782932707570*/
int loteEmVagoes = getVagosNaLinha(pst_marginal_a);

return loteEmVagoes;
/*ALCODEEND*/}

String getDestinoLogica(String moega,Agent agent)
{/*ALCODESTART::1782992207626*/
try {

    List<String> agentes = selectValues(
        String.class,
        "SELECT agente FROM parametros_terminais WHERE moega = ?",
        moega
    );

    List<String> blocos = selectValues(
        String.class,
        "SELECT atendimento FROM parametros_terminais WHERE moega = ?",
        moega
    );

    if (agentes.isEmpty() || blocos.isEmpty()) {
        error("Moega não encontrada em parametros_terminais: " + moega);
        return;
    }

    String nomeAgente = agentes.get(0);
    String nomeBlocoEnter = blocos.get(0);

    // 1. Busca o campo do agente (ex: CliMar) dentro de Main
    java.lang.reflect.Field campoAgente = Main.class.getDeclaredField(nomeAgente);
    campoAgente.setAccessible(true);
    Object blocoAgente = campoAgente.get(this);

    // 2. Busca o campo do bloco Enter (ex: cliMar_atendimento) dentro do agente
    java.lang.reflect.Field campoEnter = blocoAgente.getClass().getDeclaredField(nomeBlocoEnter);
    campoEnter.setAccessible(true);
    Object blocoEnter = campoEnter.get(blocoAgente);

    // 3. Chama o método take(agent) nesse bloco Enter
    java.lang.reflect.Method metodoTake = blocoEnter.getClass().getMethod("take", Agent.class);
    metodoTake.invoke(blocoEnter, agent);

} catch (Exception e) {
    error("Erro ao rotear atendimento para moega '" + moega + "': " + e.getMessage());
}

/*ALCODEEND*/}

ITerminalMoega getTerminalAgent(String moega)
{/*ALCODESTART::1783018617194*/
try {

    List<String> agentes = selectValues(
        String.class,
        "SELECT agente FROM parametros_terminais WHERE moega = ?",
        moega
    );

    if (agentes.isEmpty()) {
        error("Moega não encontrada em parametros_terminais: " + moega);
        return null;
    }

    String nomeAgente = agentes.get(0);

    // 1. Busca o campo do agente (ex: CliMar) dentro de Main
    java.lang.reflect.Field campoAgente = Main.class.getDeclaredField(nomeAgente);
    campoAgente.setAccessible(true);
    Object blocoAgente = campoAgente.get(this);

    if (blocoAgente instanceof ITerminalMoega) {
        return (ITerminalMoega) blocoAgente;
    } else {
        error("Agente '" + nomeAgente + "' não implementa ITerminalMoega");
        return null;
    }

} catch (Exception e) {

    Throwable causa = e.getCause();
    error("Erro ao rotear atendimento para moega '" + moega + "': " 
        + e.getMessage() 
        + (causa != null ? " | Causa: " + causa.getMessage() : " | Sem causa detalhada")
        + " | Tipo da exceção: " + e.getClass().getName());

    return null;
}
/*ALCODEEND*/}

PositionOnTrack getLinhaCirculacao_v2(Trem trem)
{/*ALCODESTART::1783520667992*/
Vagao primeiroVagao = trem.getPrimeiroVagao();
String moegaDestino = primeiroVagao.moegaDestino;

switch (moegaDestino) {
			
	case "coper-XXI":
	
		return posAtend_coperXXI;
		
	case "moegao-mar":
	
		return posAtend_moegaoMar;
	
			
	case "moegao-terra":
	
		return posAtend_moegaoTerra;
	
	
    default:
    
        return posAtend_Santa;
}
/*ALCODEEND*/}

String getAgenteMoega(String moega)
{/*ALCODESTART::1783949206630*/
try {
    
    List<String> agentes = selectValues(
        String.class,
        "SELECT agente FROM parametros_terminais WHERE moega = ?",
        moega
    );

    String nomeAgente = agentes.get(0);
 	
 	return nomeAgente;
 	
} catch (Exception e) {
    error("Erro ao rotear atendimento para moega '" + moega + "': " + e.getMessage());
	return null;
}

/*ALCODEEND*/}

Object getBlocoPeloNome(String nomeBloco,Object container)
{/*ALCODESTART::1783950130657*/
try {

    java.lang.reflect.Field campo = container.getClass().getDeclaredField(nomeBloco);
    campo.setAccessible(true);

    return campo.get(container);

} catch (Exception e) {

    error("Erro ao buscar campo '" + nomeBloco + "' em "
        + container.getClass().getSimpleName() + ": " + e.getMessage());
    return null;
}
/*ALCODEEND*/}

int fraciona_trem(Trem trem,int qt_fracao)
{/*ALCODESTART::1783968921731*/
int numero_de_vagoes = trem.size();

return (qt_fracao > numero_de_vagoes) ? numero_de_vagoes : qt_fracao ;
/*ALCODEEND*/}

int getTotalCarrosNasTracks_v2(List<RailwayTrack> tracks)
{/*ALCODESTART::1784038039506*/
java.util.HashSet<Object> carrosContados = new java.util.HashSet<Object>();

for (RailwayTrack track : tracks) {
	
	if (track == null) {
		continue;
	}
	
	carrosContados.addAll(track.getCars());
}

return carrosContados.size();
/*ALCODEEND*/}

int getVagoesEntrePontos_mesmaTrack(PositionOnTrack pontoA,PositionOnTrack pontoB)
{/*ALCODESTART::1784039086096*/
RailwayTrack track 	= pontoA.getTrack();

double offsetA 		= pontoA.getOffset(METER);
double offsetB 		= pontoB.getOffset(METER);

double inicio 		= Math.min(offsetA, offsetB);
double fim 			= Math.max(offsetA, offsetB);

int contador = 0;

for (Object carroObj : track.getCars()) {

    Vagao carro = (Vagao) carroObj;

    double offsetFrente = carro.getOffset(true);
    double offsetTras 	= carro.getOffset(false);

    boolean dentroDoIntervalo =
        (offsetFrente 	>= inicio && offsetFrente 	<= fim) ||
        (offsetTras 	>= inicio && offsetTras 	<= fim);

    if (dentroDoIntervalo) {
        contador++;
    }
}

return contador;
/*ALCODEEND*/}

int getVagoesEntrePontos_variasTracks(PositionOnTrack pontoA,PositionOnTrack pontoB,List<RailwayTrack> tracksNoCaminho)
{/*ALCODESTART::1784146673120*/
int contador = 0;

for (RailwayTrack track : tracksNoCaminho) {

    double inicio;
    double fim;

    if (track == pontoA.getTrack()) {
        double offsetA = pontoA.getOffset(METER);
        inicio = offsetA;
        fim = track.length(METER);

    } else if (track == pontoB.getTrack()) {
        double offsetB = pontoB.getOffset(METER);
        inicio = 0;
        fim = offsetB;

    } else {
        inicio = 0;
        fim = track.length(METER);
    }

    for (Object carroObj : track.getCars()) {

        double offsetFrente;
        double offsetTras;

        try {
            java.lang.reflect.Method metodoGetOffset = carroObj.getClass().getMethod("getOffset", boolean.class);
            offsetFrente = (double) metodoGetOffset.invoke(carroObj, true);
            offsetTras   = (double) metodoGetOffset.invoke(carroObj, false);
        } catch (Exception e) {
            error("Erro ao obter offset do carro '" + carroObj.getClass().getSimpleName() + "': " + e.getMessage());
            continue;
        }

        boolean dentroDoIntervalo =
            (offsetFrente >= inicio && offsetFrente <= fim) ||
            (offsetTras   >= inicio && offsetTras   <= fim);

        if (dentroDoIntervalo) {
            contador++;
        }
    }
}

return contador;
/*ALCODEEND*/}

int getTrensEmListaDeTracks(List<RailwayTrack> tracks)
{/*ALCODESTART::1784289730389*/
java.util.Set<Object> trensEncontrados = new java.util.HashSet<>();

for (RailwayTrack track : tracks) {
    trensEncontrados.addAll(track.getTrains());
}

return trensEncontrados.size();
/*ALCODEEND*/}

boolean jaEstaNoDestino(Trem trem,PositionOnTrack  destino)
{/*ALCODESTART::1784657298071*/
Agent primeiroVagao = trem.getFirst();

if (primeiroVagao == null) {
    return false;
}

try {

    java.lang.reflect.Method metodoGetTrack 	= primeiroVagao.getClass().getMethod("getTrack",  boolean.class);
    java.lang.reflect.Method metodoGetOffset 	= primeiroVagao.getClass().getMethod("getOffset", boolean.class);

    RailwayTrack trackAtual = (RailwayTrack) metodoGetTrack.invoke(primeiroVagao, true);
    double offsetAtual 		= (double) metodoGetOffset.invoke(primeiroVagao, true);

    RailwayTrack trackDestino 	= destino.getTrack();
    double offsetDestino 		= destino.getOffset(METER);

    return (trackAtual == trackDestino) && (Math.abs(offsetAtual - offsetDestino) < 0.01);

} catch (Exception e) {
    error("Erro ao verificar posição do trem: " + e.getMessage());
    return false;
}
/*ALCODEEND*/}

int getTamanhoDoTrem(Trem trem)
{/*ALCODESTART::1784915523107*/
return (
	trem != null ? 
	trem.size() : 0
);
/*ALCODEEND*/}

int getNumeroDeVagoes(Trem trem)
{/*ALCODESTART::1791226566308*/
if (trem == null) return 0;

int vagoes = 0;
for (int i = 0; i < trem.size(); i++) {
    if (trem.getCar(i) instanceof Vagao) vagoes++;
}
return vagoes;
/*ALCODEEND*/}

double configurar_log_em_arquivo()
{/*ALCODESTART::1791820000001*/
// ALT-011: espelha o console em logs/sim.log (sobrescrito a cada execução)
// Guarda os streams originais nas propriedades do sistema para não
// encadear o espelho de novo se a simulação for reiniciada na mesma JVM.
java.util.Properties props = System.getProperties();
if (!props.containsKey("md.out.original")) {
	props.put("md.out.original", System.out);
	props.put("md.err.original", System.err);
}
java.io.PrintStream outOriginal = (java.io.PrintStream) props.get("md.out.original");
java.io.PrintStream errOriginal = (java.io.PrintStream) props.get("md.err.original");

try {
	java.io.File arquivo = new java.io.File("logs/sim.log").getAbsoluteFile();
	arquivo.getParentFile().mkdirs();
	final java.io.PrintStream log = new java.io.PrintStream(
		new java.io.FileOutputStream(arquivo, false), true, "UTF-8");

	java.io.PrintStream[][] pares = { { outOriginal, log }, { errOriginal, log } };
	java.io.PrintStream[] espelhos = new java.io.PrintStream[2];
	for (int i = 0; i < 2; i++) {
		final java.io.PrintStream a = pares[i][0], b = pares[i][1];
		espelhos[i] = new java.io.PrintStream(new java.io.OutputStream() {
			public void write(int x) { a.write(x); b.write(x); }
			public void write(byte[] x, int o, int n) { a.write(x, o, n); b.write(x, o, n); }
			public void flush() { a.flush(); b.flush(); }
		}, true, "UTF-8");
	}
	System.setOut(espelhos[0]);
	System.setErr(espelhos[1]);

	traceln("[log] " + java.time.LocalDateTime.now() + " gravando em " + arquivo.getPath());
} catch (Exception e) {
	e.printStackTrace();
}
/*ALCODEEND*/}

