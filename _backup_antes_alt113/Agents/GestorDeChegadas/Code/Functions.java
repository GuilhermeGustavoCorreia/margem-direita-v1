Main main()
{/*ALCODESTART::1795850000208*/
// acesso ao Main (mesmo padrão dos outros agentes)
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

double iniciar()
{/*ALCODESTART::1795840000107*/
Log.iniciar(this);
// monta os fluxos (terminal x ferrovia) a partir do tempo_entre_chegadas.csv.
// O arquivo já foi validado por main().carregar_configuracao().
fluxos.clear(); fila.clear(); atendidos.clear();
pedido_atual = null; contador_pedidos = 0;

for (String[] c : main().ler_csv(main().pasta_simulacao + "/input/tempo_entre_chegadas.csv")) {
	FluxoChegada f = new FluxoChegada();
	f.instancia   = c[1];
	f.ferrovia    = c[2].toLowerCase();
	f.intervalo_h = Double.parseDouble(c[3].replace(',', '.'));

	Object t = get_campo(main(), f.instancia);
	if (!(t instanceof ITerminalMoega))
		error("[chegadas] main()." + f.instancia + " não é um ITerminalMoega");
	f.terminal = (ITerminalMoega) t;

	Object fonte = get_campo(t, "tremTeste_" + f.ferrovia);
	if (!(fonte instanceof com.anylogic.libraries.rail.TrainSource))
		error("[chegadas] " + f.instancia + " não tem o bloco tremTeste_" + f.ferrovia + " (TrainSource)");
	f.fonte = (com.anylogic.libraries.rail.TrainSource) fonte;

	// 1ª chegada sorteada dentro do 1º intervalo: evita todos os fluxos em t = 0
	f.proxima_h = time(HOUR) + uniform(0, f.intervalo_h);
	fluxos.add(f);
	Log.info("Chegadas", "fluxo", String.format("%s %s: 1ª chegada em %.2f h, depois a cada %.2f h",
		f.instancia, f.ferrovia, f.proxima_h, f.intervalo_h));
}
Log.info("Chegadas", "fluxos", fluxos.size() + " fluxos de chegada");
/*ALCODEEND*/}

double gerar_pedidos()
{/*ALCODESTART::1795840000108*/
// cria os pedidos dos fluxos vencidos. O horário do pedido é o
// programado (proxima_h), não o do relógio, para a espera ficar correta.
double agora = time(HOUR);
for (FluxoChegada f : fluxos) {
	while (agora >= f.proxima_h) {
		Pedido p = new Pedido();
		p.numero        = ++contador_pedidos;
		p.terminal      = f.terminal;
		p.instancia     = f.instancia;
		p.ferrovia      = f.ferrovia;
		p.fonte         = f.fonte;
		p.hora_pedido_h = f.proxima_h;
		fila.add(p);
		f.gerados++;
		f.proxima_h += f.intervalo_h;
		Log.info("Chegadas", "pedido_criado", String.format("%s pedido criado (fila=%d)", p, fila.size()));
	}
}
/*ALCODEEND*/}

double despachar()
{/*ALCODESTART::1795840000109*/
// percorre a fila em ordem; entra o primeiro que puder.
// Um pedido parado (terminal ocupado e ISN cheio) não trava os outros.
// Um trem por vez: o próximo só depois que este chegou ao ISN e liberou a entrada.
if (pedido_atual != null || fila.isEmpty()) return;
if (!entrada_livre()) return;

for (Pedido p : fila) {
	String d = decidir_destino(p);
	if (d == null) continue;
	fila.remove(p);
	p.destino = d;
	colocar(p);
	return;
}
/*ALCODEEND*/}

boolean entrada_livre()
{/*ALCODESTART::1795840000110*/
// sobreposição na entrada. circ_pos_inicio_placa_2 fica na isn_linha01_a,
// dentro da faixa a do ISN: só coloca outro trem com a faixa livre e o trecho vazio.
if (main().gestorDeISN.faixa_isn_a.estado != Faixa.EstadoFaixa.LIVRE) return false;
return main().isn_linha01_a.getTrains().isEmpty();
/*ALCODEEND*/}

String decidir_destino(Pedido p)
{/*ALCODESTART::1795840000111*/
// se o terminal pode receber e não há outro trem a caminho dele, vai direto para a circulação.
// Senão estaciona no ISN, se a linha 02 estiver livre. Senão espera na fila.
boolean trem_a_caminho = false;
for (Trem t : main().gestorDeCirculacao.trens_em_circulacao) {
	if (t.ITerminalMoega_destino == p.terminal) { trem_a_caminho = true; break; }
}
// trem esperando na linha 02 pelo mesmo terminal tem a vez: o pedido novo não passa na frente
Trem na_linha02 = main().gestorDeISN.lote_linha02;
if (na_linha02 != null && na_linha02.ITerminalMoega_destino == p.terminal) trem_a_caminho = true;
if (p.terminal.podeReceberNovoLote() && !trem_a_caminho) return "circulacao";
if (main().gestorDeISN.pode_estacionar()) return "isn";
return null;
/*ALCODEEND*/}

double colocar(Pedido p)
{/*ALCODESTART::1795840000112*/
// o TrainSource do terminal cria o trem na circ_pos_inicio_placa_2.
// Ele sai pelo Exit paraGestorCirculacao_chegadaCarregados do terminal, que chama receber_trem().
pedido_atual = p;
Log.info("Chegadas", "inject", String.format("%s -> inject (destino=%s, espera=%.2fh)",
	p, p.destino, time(HOUR) - p.hora_pedido_h));
p.fonte.inject();
/*ALCODEEND*/}

double receber_trem(Trem trem)
{/*ALCODESTART::1795840000113*/
// o trem acabou de nascer. Liga o trem ao pedido, reserva a linha 02
// se o destino for o ISN e entrega o trem ao GestorDeISN.
Pedido p = pedido_atual;
pedido_atual = null;

// prefixo na criação (também para o trem do botão manual)
trem.prefixo = gerar_prefixo(trem);

if (p == null) {
	// trem criado pelo botão manual do terminal: segue direto para a circulação
	Log.info("Chegadas", "sem_pedido", String.format("trem=%d/%s sem pedido (botão manual)", trem.getId(), trem.prefixo));
	if (trem.ITerminalMoega_destino != null) main().gestorDeCirculacao.trens_em_circulacao.add(trem);
} else {
	p.trem           = trem;
	p.hora_entrada_h = time(HOUR);
	trem.ITerminalMoega_destino = p.terminal;
	if ("isn".equals(p.destino)) {
		main().gestorDeISN.lote_linha02 = trem;   // reserva da vaga
		trem.estacionarISN = true;               // o GestorDeISN manda para a linha 02
	} else {
		main().gestorDeCirculacao.trens_em_circulacao.add(trem);   // a caminho do terminal
	}
	atendidos.add(p);
	Log.info("Chegadas", "entrou_trilho", String.format("%s trem=%d/%s entrou no trilho -> %s",
		p, trem.getId(), trem.prefixo, p.destino));
}
main().gestorDeISN.chegada.take(trem);
/*ALCODEEND*/}

Object get_campo(Object alvo,String nome)
{/*ALCODESTART::1795840000114*/
try {
	java.lang.reflect.Field f;
	try { f = alvo.getClass().getField(nome); }
	catch (NoSuchFieldException nf) { f = alvo.getClass().getDeclaredField(nome); f.setAccessible(true); }
	return f.get(alvo);
} catch (Exception ex) {
	return null;
}
/*ALCODEEND*/}

String gerar_prefixo(Trem trem)
{/*ALCODESTART::1795850000301*/
// prefixo = letra + 2 dígitos pares, sorteados pela regra ferrovia x segmento.
// Segmento e ferrovia vêm do 1º vagão (gravados no carSetup do tremTeste_*).
String seg = null, fer = null;
for (int i = 0; i < trem.size(); i++) {
	Agent c = trem.getCar(i);
	if (c instanceof Vagao) { seg = ((Vagao) c).segmento; fer = ((Vagao) c).ferrovia; break; }
}
if (seg == null || fer == null)
	error("[chegadas] trem=" + trem.getId() + " sem vagão com segmento/ferrovia: não dá para gerar o prefixo");
seg = seg.toLowerCase(); fer = fer.toLowerCase();

String letra = null; int min = 0, max = 0;
switch (fer) {
	case "rumo":
		min = 2; max = 98;
		if      (seg.equals("grao"))     letra = new String[]{"X", "I", "Y"}[uniform_discr(0, 2)];
		else if (seg.equals("acucar"))   letra = "R";
		else if (seg.equals("celulose")) letra = "L";
		break;
	case "mrs":
		if      (seg.equals("grao"))     { letra = "M"; min = 60; max = 78; }
		else if (seg.equals("acucar"))   { letra = "M"; min = 80; max = 98; }
		else if (seg.equals("celulose")) { letra = "U"; min = 30; max = 48; }
		break;
	case "vli":
		letra = "U"; min = 2; max = 98;
		break;
}
if (letra == null)
	error("[chegadas] sem regra de prefixo para ferrovia=" + fer + " segmento=" + seg);

int numero = min + 2 * uniform_discr(0, (max - min) / 2);   // sempre par
return String.format("%s%02d", letra, numero);
/*ALCODEEND*/}

