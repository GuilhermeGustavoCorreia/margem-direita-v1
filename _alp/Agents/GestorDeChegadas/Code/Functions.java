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
iniciar_csv_trens();
iniciar_csv_vagoes();
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
		if (log_analise_detalhada) Log.info("Chegadas", "pedido_criado", String.format("%s pedido criado (fila=%d)", p, fila.size()));
	}
}
/*ALCODEEND*/}

double despachar()
{/*ALCODESTART::1795840000109*/
// percorre a fila em ordem; entra o primeiro que puder.
// Um pedido parado (terminal ocupado e ISN cheio) não trava os outros.
// Um trem por vez: o próximo só depois que este nasceu e passou pelo receber_trem.
if (pedido_atual != null || fila.isEmpty()) return;

String entrada = motivo_entrada();
if (entrada != null) {
	for (Pedido p : fila) registrar_motivo(p, "entrada ocupada: " + entrada);
	return;
}

for (Pedido p : fila) {
	String d = decidir_destino(p);
	if (d == null) { registrar_motivo(p, motivo_destino(p)); continue; }
	String porque = "circulacao".equals(d)
		? "terminal pode receber, nenhum trem a caminho"
		: motivo_destino(p) + "; linha 02 livre";
	Log.info("Chegadas", "decisao", String.format("%s -> %s (%s)", p, d, porque));
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
return motivo_entrada() == null;
/*ALCODEEND*/}

String decidir_destino(Pedido p)
{/*ALCODESTART::1795840000111*/
// se o terminal pode receber e não há outro trem a caminho dele, vai direto para a circulação.
// Senão estaciona no ISN, se a linha 02 estiver livre. Senão espera na fila.
if (pode_receber_tipo(p.terminal, p.ferrovia, p.segmento) && trem_a_caminho(p) == null) return "circulacao";
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
	// trem criado fora do GestorDeChegadas: os botões passam por pedir_trem, então isto não deveria acontecer
	Log.warn("Chegadas", "sem_pedido", String.format("trem=%d/%s nasceu sem pedido: não passou pelas condições de entrada", trem.getId(), trem.prefixo));
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
	registrar_trem_csv(p, trem);
	Log.info("Chegadas", "entrou_trilho", String.format("%s trem=%d/%s entrou no trilho -> %s",
		p, trem.getId(), trem.prefixo, p.destino));
}
// o vagão é separado do trem ao longo do processo: guarda o terminal e a posição em que nasceu
marcar_vagoes(trem);
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
if (seg == null || fer == null) {
	int locos = 0;
	for (int i = 0; i < trem.size(); i++) if (trem.getCar(i) instanceof Locomotiva) locos++;
	Log.warn("Chegadas", "sem_prefixo", String.format("trem=%d sem vagão com segmento/ferrovia | carros=%d locos=%d destino=%s",
		trem.getId(), trem.size(), locos,
		trem.ITerminalMoega_destino == null ? "null" : ((Agent) trem.ITerminalMoega_destino).getName()));
	return "???";
}
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

double pedir_trem(ITerminalMoega terminal,String ferrovia,com.anylogic.libraries.rail.TrainSource fonte)
{/*ALCODESTART::1796100000401*/
// chamada pelos botões dos terminais: o trem entra na fila e passa pelas mesmas condições do pedido programado
Pedido p = new Pedido();
p.numero        = ++contador_pedidos;
p.terminal      = terminal;
p.instancia     = ((Agent) terminal).getName();
p.ferrovia      = ferrovia.toLowerCase();
p.fonte         = fonte;
p.hora_pedido_h = time(HOUR);
p.manual        = true;
fila.add(p);
Log.info("Chegadas", "pedido_manual", String.format("%s pedido pelo botão (fila=%d)", p, fila.size()));
despachar();
/*ALCODEEND*/}

String motivo_entrada()
{/*ALCODESTART::1796100000402*/
// null se a entrada está livre; senão o motivo, para o log
Faixa f = main().gestorDeISN.faixa_isn_a;
int n = main().isn_linha01_a.getTrains().size();
if (f.estado == Faixa.EstadoFaixa.LIVRE && n == 0) return null;
return String.format("faixa_isn_a=%s, %d trem(ns) na isn_linha01_a", f.estado, n);
/*ALCODEEND*/}

Trem trem_a_caminho(Pedido p)
{/*ALCODESTART::1796100000403*/
// trem já despachado para o mesmo terminal: em circulação ou esperando na linha 02 (este tem a vez)
for (Trem t : main().gestorDeCirculacao.trens_em_circulacao)
	if (t.ITerminalMoega_destino == p.terminal) return t;
Trem l2 = main().gestorDeISN.lote_linha02;
if (l2 != null && l2.ITerminalMoega_destino == p.terminal) return l2;
return null;
/*ALCODEEND*/}

String motivo_destino(Pedido p)
{/*ALCODESTART::1796100000404*/
// descreve por que o pedido não vai direto para a circulação
java.util.List<String> m = new java.util.ArrayList<String>();
if (!p.terminal.podeReceberNovoLote()) m.add("terminal não pode receber");
else if (!pode_receber_tipo(p.terminal, p.ferrovia, p.segmento))
	m.add(String.format("terminal com outro tipo (%s/%s)", p.terminal.getFerrovia(), p.terminal.getSegmento()));
Trem c = trem_a_caminho(p);
if (c != null) m.add(String.format("trem a caminho do terminal: %d/%s", c.getId(), c.prefixo));
if (!main().gestorDeISN.pode_estacionar()) {
	Trem l2 = main().gestorDeISN.lote_linha02;
	m.add("linha 02 ocupada por " + (l2 == null ? "?" : l2.getId() + "/" + l2.prefixo));
}
return String.join("; ", m);
/*ALCODEEND*/}

double registrar_motivo(Pedido p,String motivo)
{/*ALCODESTART::1796100000405*/
// registra a espera só quando o motivo muda, para o log não repetir a cada minuto
if (motivo.equals(p.ultimo_motivo)) return;
p.ultimo_motivo = motivo;
if (log_analise_detalhada) Log.info("Chegadas", "aguardando", String.format("%s %s", p, motivo));
/*ALCODEEND*/}

boolean pode_receber_tipo(ITerminalMoega terminal,String ferrovia,String segmento)
{/*ALCODESTART::1798200000303*/
// um terminal não descarrega duas ferrovias/segmentos ao mesmo tempo:
// TES, T39 e ADM só recebem outro tipo com o terminal vazio
// segmento null = segmento padrão do terminal (o pedido ainda não traz segmento)
if (!terminal.podeReceberNovoLote()) return false;

boolean vazio;
if      (terminal instanceof TES) vazio = ((TES) terminal).terminal_vazio();
else if (terminal instanceof T39) vazio = ((T39) terminal).terminal_vazio();
else if (terminal instanceof ADM) vazio = ((ADM) terminal).terminal_vazio_para_novo_tipo();
else return true;
if (vazio) return true;

boolean mesma_ferrovia = java.util.Objects.equals(ferrovia, terminal.getFerrovia());
boolean mesmo_segmento = segmento == null || java.util.Objects.equals(segmento, terminal.getSegmento());
return mesma_ferrovia && mesmo_segmento;
/*ALCODEEND*/}

boolean pode_receber_trem(ITerminalMoega terminal,Trem trem)
{/*ALCODESTART::1798200000304*/
// mesma regra do pode_receber_tipo, com a ferrovia e o segmento do 1º vagão do trem
String fer = null, seg = null;
for (int i = 0; i < trem.size(); i++) {
	Agent c = trem.getCar(i);
	if (c instanceof Vagao) { fer = ((Vagao) c).ferrovia; seg = ((Vagao) c).segmento; break; }
}
return pode_receber_tipo(terminal, fer, seg);
/*ALCODEEND*/}

double iniciar_csv_trens()
{/*ALCODESTART::1798300000501*/
// cria (sobrescreve) o CSV dos trens criados, só com o cabeçalho.
// Rodada normal: trens_criados.csv; replicações: trens_criados_rep<n>.csv
int rep = main().replicacao_atual;
java.io.File arq = new java.io.File(main().pasta_simulacao + "/output/trens_criados/"
	+ (rep > 0 ? "trens_criados_rep" + rep + ".csv" : "trens_criados.csv")).getAbsoluteFile();
arq.getParentFile().mkdirs();
csv_trens = arq.getPath();
try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(arq, false), "UTF-8")) {
	w.write("pedido;prefixo;trem_id;terminal;ferrovia;segmento;data_pedido;data_entrada;warm_up\n");
} catch (Exception ex) {
	error("[chegadas] não foi possível criar " + csv_trens + ": " + ex.getMessage());
}
Log.info("Chegadas", "csv", "trens criados em " + csv_trens);
/*ALCODEEND*/}

double registrar_trem_csv(Pedido p,Trem trem)
{/*ALCODESTART::1798300000502*/
// uma linha por trem criado, gravada na hora (append) para não perder dados se a rodada cair.
// data_pedido = horário programado do pedido; data_entrada = trem no trilho.
// warm_up = o trem entrou no trilho antes do fim do warm-up.
String seg = "";
for (int i = 0; i < trem.size(); i++) {
	Agent c = trem.getCar(i);
	if (c instanceof Vagao) { seg = ((Vagao) c).segmento; break; }
}
java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
java.time.LocalDateTime t0 = Log.data_inicio.atStartOfDay();
String data_pedido  = t0.plusSeconds(Math.round(p.hora_pedido_h  * 3600)).format(fmt);
String data_entrada = t0.plusSeconds(Math.round(p.hora_entrada_h * 3600)).format(fmt);
boolean warm_up = p.hora_entrada_h < main().warmup_h;

String linha = String.join(";", String.valueOf(p.numero), trem.prefixo, String.valueOf(trem.getId()),
	p.instancia, p.ferrovia, seg == null ? "" : seg.toLowerCase(), data_pedido, data_entrada, String.valueOf(warm_up));
try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(csv_trens, true), "UTF-8")) {
	w.write(linha + "\n");
} catch (Exception ex) {
	Log.warn("Chegadas", "csv", "falha ao gravar trem " + trem.getId() + " em " + csv_trens + ": " + ex.getMessage());
}
/*ALCODEEND*/}

double marcar_vagoes(Trem trem)
{/*ALCODESTART::1801260000011*/
// copia o terminal, o id e o prefixo do trem para cada vagão e grava a posição no trem criado.
// posicao_absoluta = índice do carro (locos incluídas, 0 = cabeça, lado Ponta da Praia)
// posicao_relativa = ordem só entre os vagões, a partir de 1
int rel = 0;
for (int i = 0; i < trem.size(); i++) {
	Agent c = trem.getCar(i);
	if (!(c instanceof Vagao)) continue;
	Vagao v = (Vagao) c;
	rel++;
	v.posicao_absoluta = i;
	v.posicao_relativa = rel;
	v.trem_criado_id      = trem.getId();
	v.prefixo_trem_criado = trem.prefixo;
	vagoes_ativos.add(v);   // vagão vivo no modelo até o trainExit
	if (trem.ITerminalMoega_destino != null) v.setTerminal(trem.ITerminalMoega_destino);
}
/*ALCODEEND*/}

double iniciar_csv_vagoes()
{/*ALCODESTART::1801260000012*/
// cria (sobrescreve) o CSV dos vagões, só com o cabeçalho.
// Rodada normal: vagoes.csv; replicações: vagoes_rep<n>.csv
int rep = main().replicacao_atual;
java.io.File arq = new java.io.File(main().pasta_simulacao + "/output/wagons/"
	+ (rep > 0 ? "vagoes_rep" + rep + ".csv" : "vagoes.csv")).getAbsoluteFile();
arq.getParentFile().mkdirs();
csv_vagoes = arq.getPath();
try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(arq, false), "UTF-8")) {
	w.write("vagao_id;trem_criado_id;prefixo_trem_criado;terminal;ferrovia;segmento;posicao_absoluta;posicao_relativa;data_criacao;data_descarga;data_descarte;"
		+ "tempo_ate_descarga_h;tempo_vazio_h;tempo_no_sistema_h;warm_up\n");
} catch (Exception ex) {
	error("[chegadas] não foi possível criar " + csv_vagoes + ": " + ex.getMessage());
}
Log.info("Chegadas", "csv", "vagões em " + csv_vagoes);
/*ALCODEEND*/}

double registrar_vagoes_saida(Trem trem)
{/*ALCODESTART::1801260000013*/
// chamado no trainExit do GestorDeDVs: o trem sai da Margem Direita.
// Marca o descarte e grava uma linha por vagão.
double agora = time(HOUR);
for (int i = 0; i < trem.size(); i++) {
	Agent c = trem.getCar(i);
	if (!(c instanceof Vagao)) continue;
	Vagao v = (Vagao) c;
	v.hora_descarte_h = agora;
	vagoes_ativos.remove(v);
	registrar_vagao_csv(v);
}
/*ALCODEEND*/}

double registrar_vagao_csv(Vagao v)
{/*ALCODESTART::1801260000014*/
// datas em dd/MM/yyyy HH:mm (mesmo padrão do trens_criados); vazio quando o evento não aconteceu.
// tempos em horas, com vírgula decimal.
java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
java.time.LocalDateTime t0 = Log.data_inicio.atStartOfDay();
java.util.Locale br = new java.util.Locale("pt", "BR");
double cri = v.hora_criacao_h, des = v.hora_descarga_h, fim = v.hora_descarte_h;

String data_criacao  = cri < 0 ? "" : t0.plusSeconds(Math.round(cri * 3600)).format(fmt);
String data_descarga = des < 0 ? "" : t0.plusSeconds(Math.round(des * 3600)).format(fmt);
String data_descarte = fim < 0 ? "" : t0.plusSeconds(Math.round(fim * 3600)).format(fmt);
String ate_descarga  = (cri < 0 || des < 0) ? "" : String.format(br, "%.2f", des - cri);
String vazio         = (des < 0 || fim < 0) ? "" : String.format(br, "%.2f", fim - des);
String no_sistema    = (cri < 0 || fim < 0) ? "" : String.format(br, "%.2f", fim - cri);
boolean warm_up = cri < main().warmup_h;

String linha = String.join(";", String.valueOf(v.getId()),
	String.valueOf(v.trem_criado_id), v.prefixo_trem_criado == null ? "" : v.prefixo_trem_criado,
	v.terminal == null ? "" : v.terminal.getName(),
	v.ferrovia == null ? "" : v.ferrovia.toLowerCase(),
	v.segmento == null ? "" : v.segmento.toLowerCase(),
	String.valueOf(v.posicao_absoluta), String.valueOf(v.posicao_relativa),
	data_criacao, data_descarga, data_descarte, ate_descarga, vazio, no_sistema, String.valueOf(warm_up));
try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(csv_vagoes, true), "UTF-8")) {
	w.write(linha + "\n");
} catch (Exception ex) {
	Log.warn("Chegadas", "csv", "falha ao gravar vagão " + v.getId() + " em " + csv_vagoes + ": " + ex.getMessage());
}
/*ALCODEEND*/}

