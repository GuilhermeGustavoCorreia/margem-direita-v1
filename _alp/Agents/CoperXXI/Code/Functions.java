Main main()
{/*ALCODESTART::1785421553067*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1791650000004*/
// trem entrando: do comecar_encoste até o lote 1 entrar no ciclo da moega
if (_trem_chegando != null) return false;
// alívio pedido ou em andamento: os vazios do trem anterior saem antes, com a loco da ferrovia dele.
// Ferrovia e segmento do terminal são lidos na chegada do carregado (comecar_encoste); com vazios de
// uma ferrovia ainda no terminal, um trem de outra ferrovia trocaria a loco pedida pelo alívio.
if (aguardando_alivio || fazendoAlivio) return false;
// ainda há carregados no terminal (lote em descarga ou segunda parte esperando)
return get_vagoes_carregados() == 0;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1791650000005*/
// ALT-066: alívio do CoperXII quando lote_a e lote_b estão cheios e o resto do lote já descarregou na moega
// ALT-071: pede alívio no fim de todo lote (aguardando_alivio)
if (aguardando_alivio) return 2;
int capacidade = CAPACIDADE_LOTE_A + CAPACIDADE_LOTE_B;
if (capacidade <= 0) return 0;
return Math.min(1f, (float) _getVagosVazios() / capacidade);
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1791650000006*/
return "pst";
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1791650000007*/
return produto_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1791650000008*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1791650000009*/
// ALT-069
fazendoAlivio = true;
iniciar_alivio.take(trem);
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1791650000010*/
return 2;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1791650000011*/
// ALT-068: loco do alívio para logo antes da cabeça do lote_b (offset 14 da psnSaidaCopexXXI_b)
return main()._coperXXI_lote_b;
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_primeiro_alivio()
{/*ALCODESTART::1791650000012*/
// ALT-068: linhas físicas entre a faixa de criação (via1_intermar_d) e a psnSaidaCopexXXI_b.
// Não são faixas da circulação: só entram na rota da loco (TrainMoveTo por lista de linhas).
// Sem recurso (não licencia nada) e com desenho invisível (o gestor pinta a faixa ao ocupar).
ArrayList<Faixa> faixas = new ArrayList<Faixa>();
for (RailwayTrack t : new RailwayTrack[] { main().linhaPaqueta_a, main().linhaCostaLeita_a }) {
	Faixa f     = new Faixa();
	f.track     = t;
	f.recurso   = null;
	f.estado    = Faixa.EstadoFaixa.LIVRE;
	f.linhaTMDS = new ShapePolyLine();
	faixas.add(f);
}
return faixas;
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1791650000013*/
// ALT-003: provisório — sem faixas até integrar com o GestorDeAlivios
return new ArrayList<Faixa>();
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791650000014*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791650000015*/
// ALT-066: lote_b + lote_a + lote da moega
return _getVagosVazios() + ((lote_vazios_moega == null) ? 0 : lote_vazios_moega.getNumeroDeVagoes());
/*ALCODEEND*/}

int _getVagosVazios()
{/*ALCODESTART::1791650000016*/
// ALT-077: vagões parados em lote_b + lote_a (contagem direta; a conta pelo espaço dava -6 com a moega no espaço)
return ((lote_vazios_b == null) ? 0 : lote_vazios_b.getNumeroDeVagoes())
     + ((lote_vazios_a == null) ? 0 : lote_vazios_a.getNumeroDeVagoes());
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1791700000005*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1791700000006*/
return 1;
/*ALCODEEND*/}

int get_carros_ate_segunda_loco(Trem trem)
{/*ALCODESTART::1793200200001*/
// ALT-065: quantos carros vêm antes da 2ª loco (pula as locos da cabeça). 0 = não tem 2ª loco
int n = trem.size();
int i = 0;
while (i < n && trem.getCar(i) instanceof Locomotiva) i++;
for (; i < n; i++) {
	if (trem.getCar(i) instanceof Locomotiva) return i;
}
return 0;
/*ALCODEEND*/}

int get_espaco_linha(String linha)
{/*ALCODESTART::1793200200002*/
// ALT-065/076: vagões que ainda cabem em lote_b, lote_a ou no lote da moega
Trem lote; int cap;
if ("b".equals(linha))          { lote = lote_vazios_b;     cap = CAPACIDADE_LOTE_B; }
else if ("a".equals(linha))     { lote = lote_vazios_a;     cap = CAPACIDADE_LOTE_A; }
else                            { lote = lote_vazios_moega; cap = get_capacidade_moega(); }
int ocup = (lote == null) ? 0 : lote.getNumeroDeVagoes();
return Math.max(0, cap - ocup);
/*ALCODEEND*/}

int get_espaco_vazios()
{/*ALCODESTART::1793200200003*/
// ALT-076: espaço até fechar o alívio de QTD_ALIVIO (lote_b + lote_a + lote da moega)
return get_espaco_linha("b") + get_espaco_linha("a") + get_espaco_linha("moega");
/*ALCODEEND*/}

double definir_grupo(Trem lote)
{/*ALCODESTART::1793200200004*/
// ALT-065: linha de destino (b antes de a) e tamanho do grupo que passa pela moega
// ALT-076: b, depois a, depois o lote da moega
if (get_espaco_linha("b") > 0)      _grupo_linha = "b";
else if (get_espaco_linha("a") > 0) _grupo_linha = "a";
else                                _grupo_linha = "moega";

int espaco     = get_espaco_linha(_grupo_linha);
int carregados = lote.getNumeroDeVagoesCarregados();
_grupo_vagoes  = Math.min(QTD_VAGOES_GRUPO, Math.min(espaco, carregados));

int locos = 0;
while (locos < lote.size() && lote.getCar(locos) instanceof Locomotiva) locos++;
_grupo_carros = locos + _grupo_vagoes;
// ALT-073: se o grupo leva todos os carregados, leva o lote inteiro (inclui a loco que empurra na cauda)
if (_grupo_vagoes >= carregados) _grupo_carros = lote.size();

traceln(String.format("[%.1f][CoperXII][grupo] trem=%d linha=%s vagoes=%d carros=%d (lote tem %d carros, espaco b=%d a=%d)",
	time(), lote.getId(), _grupo_linha, _grupo_vagoes, _grupo_carros, lote.size(), get_espaco_linha("b"), get_espaco_linha("a")));
/*ALCODEEND*/}

double log_analise(String evento,Agent a,String msg)
{/*ALCODESTART::1794506038078*/
// log de análise: só escreve com log_de_analise = true
if (!log_de_analise) return;
String t = (a == null) ? "-" : String.valueOf(a.getId());
if (a instanceof Trem) {
	Trem tr = (Trem) a;
	t = String.format("trem=%d carros=%d vgs=%d locos=%d", tr.getId(), tr.size(), tr.getNumeroDeVagoes(), tr.getNumeroDeLocomotivas());
}
Log.info("CoperXXI", evento, t + " | " + msg);
/*ALCODEEND*/}

double registrar_vazios(Trem trem)
{/*ALCODESTART::1793200200005*/
// ALT-065: marca a linha do grupo no próprio trem e atualiza o lote de vazios
// (feito antes de liberar a moega, porque o próximo lote recalcula _grupo_linha)
trem.tipoDeLote = "coperXII_vazios_" + _grupo_linha;
if ("b".equals(_grupo_linha)) lote_vazios_b = trem;
else                          lote_vazios_a = trem;

if (trem.getNumeroDeLocomotivas() > 0)
	traceln(String.format("[%.1f][CoperXII][vazios] ⚠️ trem=%d ficou com %d loco(s) no meio do lote de vazios",
		time(), trem.getId(), trem.getNumeroDeLocomotivas()));
/*ALCODEEND*/}

double guardar_vazios(Trem trem)
{/*ALCODESTART::1793200200006*/
// ALT-065: guarda o lote na linha (Wait do estoque) e solta o resto do lote que espera na moega
if ("coperXII_vazios_b".equals(trem.tipoDeLote)) {
	entrar_estoque_b.take(trem);
} else {
	entrar_estoque_a.take(trem);
}

traceln(String.format("[%.1f][CoperXII][vazios] trem=%d guardado (%s) | lote_b=%d/%d lote_a=%d/%d",
	time(), trem.getId(), trem.tipoDeLote,
	CAPACIDADE_LOTE_B - get_espaco_linha("b"), CAPACIDADE_LOTE_B,
	CAPACIDADE_LOTE_A - get_espaco_linha("a"), CAPACIDADE_LOTE_A));

verificar_alivio(); // ALT-076
aguarda_grupo.unblock();
/*ALCODEEND*/}

double liberar_espaco_vazios()
{/*ALCODESTART::1793200200007*/
// ALT-065: chamar depois do alívio das linhas de vazios (GestorDeAlivios, a integrar)
aguarda_espaco_vazios.unblock();
/*ALCODEEND*/}

double alivio_seguir(Trem trem)
{/*ALCODESTART::1793230200001*/
// ALT-069: próximo engate do alívio, na ordem lote_b -> lote_a -> moega (do lado da saída para dentro)
if (lote_vazios_b != null) {
	etapa_alivio = "b";
	entrar_engate_b.take(trem);
} else if (lote_vazios_a != null) {
	etapa_alivio = "a";
	entrar_engate_a.take(trem);
} else if (lote_vazios_moega != null) {
	etapa_alivio = "moega";
	entrar_engate_moega.take(trem);
} else {
	etapa_alivio = "";
	fim_alivio.take(trem);
}
/*ALCODEEND*/}

double iniciar_proximo_lote()
{/*ALCODESTART::1793240200001*/
// ALT-073: solta o lote_em_descarga (antiga segunda parte, parada no Wait espera_segunda_parte)
if (lote_em_descarga != null && lote_em_descarga.getNumeroDeVagoesCarregados() > 0 && esta_na_espera(lote_em_descarga)) {
	traceln(String.format("[%.1f][CoperXII][segunda_parte] trem=%d começa a descarga (%d vgs carregados, locos=%d)",
		time(), lote_em_descarga.getId(), lote_em_descarga.getNumeroDeVagoesCarregados(), lote_em_descarga.getNumeroDeLocomotivas()));
	destino_segunda_parte = "descarga";
	espera_segunda_parte.free(lote_em_descarga);
}
/*ALCODEEND*/}

ResourcePool[] recursos_do_lote(Trem trem)
{/*ALCODESTART::1793260200001*/
// ALT-073: recursos que o lote segura no fim da descarga: rp_moega e, se pediu, a unidade da loco FIPS PST
List<ResourcePool> pools = new ArrayList<ResourcePool>();
if (!trem.resourceUnitsOfPool(rp_moega).isEmpty()) pools.add(rp_moega);
ResourcePool fips = main().gestorDeLocomotivas.rp_locoFips_pst;
if (!trem.resourceUnitsOfPool(fips).isEmpty()) pools.add(fips);
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

double trocar_lote_em_descarga()
{/*ALCODESTART::1793260200002*/
// ALT-073: o lote em descarga acabou; a segunda parte passa a ser o lote em descarga
traceln(String.format("[%.1f][CoperXII][segunda_parte] lote_em_descarga=%s -> %s",
	time(), lote_em_descarga == null ? "-" : String.valueOf(lote_em_descarga.getId()),
	lote_segunda_parte == null ? "-" : String.valueOf(lote_segunda_parte.getId())));
lote_em_descarga   = lote_segunda_parte;
lote_segunda_parte = null;
/*ALCODEEND*/}

double pedir_loco_segunda_parte()
{/*ALCODESTART::1793260200003*/
// ALT-073: solta a segunda parte do Wait para cortar a loco da cabeça dela (ela volta ao Wait sem a loco)
if (lote_segunda_parte != null && esta_na_espera(lote_segunda_parte)) {
	destino_segunda_parte = "loco";
	espera_segunda_parte.free(lote_segunda_parte);
} else {
	error("CoperXII: pedir_loco_segunda_parte sem segunda parte no Wait");
}
/*ALCODEEND*/}

boolean esta_na_espera(Trem trem)
{/*ALCODESTART::1793260200004*/
// ALT-073: o trem está parado no Wait espera_segunda_parte?
for (int i = 0; i < espera_segunda_parte.size(); i++) {
	if (espera_segunda_parte.get(i) == trem) return true;
}
return false;
/*ALCODEEND*/}

int get_capacidade_moega()
{/*ALCODESTART::1793280200001*/
// ALT-076: o lote da moega completa o alívio: QTD_ALIVIO - lote_b - lote_a (hoje 41 - 22 - 13 = 6)
return Math.max(0, QTD_ALIVIO - CAPACIDADE_LOTE_B - CAPACIDADE_LOTE_A);
/*ALCODEEND*/}

double verificar_alivio()
{/*ALCODESTART::1793280200002*/
// pede alívio quando lote_b, lote_a e o lote da moega somam QTD_ALIVIO (terminal cheio)
// ou quando não há mais carregados no terminal e sobrou ao menos 1 vazio
if (aguardando_alivio || fazendoAlivio) return;

boolean terminalCheio = get_espaco_vazios() == 0;
boolean semCarga      = get_vagoes_carregados() == 0 && get_quantidade_vazios() >= 1;

if (terminalCheio) {
	aguardando_alivio = true;
	traceln(String.format("[%.1f][CoperXII][alivio] %d vazios prontos (b=%d a=%d moega=%d): pedindo alívio",
		time(), QTD_ALIVIO, CAPACIDADE_LOTE_B, CAPACIDADE_LOTE_A, get_capacidade_moega()));
} else if (semCarga) {
	aguardando_alivio = true;
	Log.info("CoperXXI", "pedir_alivio", String.format("sem carregados no terminal | vazios=%d (b=%d a=%d moega=%d)",
		get_quantidade_vazios(),
		CAPACIDADE_LOTE_B - get_espaco_linha("b"), CAPACIDADE_LOTE_A - get_espaco_linha("a"), get_capacidade_moega() - get_espaco_linha("moega")));
}
/*ALCODEEND*/}

double get_vagoes_carregados()
{/*ALCODESTART::1794506038053*/
return (
 
	main().getTamanhoDoTrem(lote_segunda_parte) + 
	main().getTamanhoDoTrem(lote_em_descarga)

);
/*ALCODEEND*/}

int get_vgs_trazidos_alivio(Trem trem)
{/*ALCODESTART::1794506038075*/
// vagões que a loco já engatou neste alívio
return trem.getNumeroDeVagoes() - qt_vgs_inicio_alivio;
/*ALCODEEND*/}

boolean ainda_tem_lote_vazios()
{/*ALCODESTART::1794600000031*/
// ainda há lote de vazios parado em b, a ou moega? O alívio engata todos antes de cortar a sobra
return lote_vazios_b != null || lote_vazios_a != null || lote_vazios_moega != null;
/*ALCODEEND*/}

int get_capacidade_linha(String linha)
{/*ALCODESTART::1794600000033*/
// capacidade de vazios de cada posição da linha
if ("b".equals(linha)) return CAPACIDADE_LOTE_B;
if ("a".equals(linha)) return CAPACIDADE_LOTE_A;
return get_capacidade_moega();
/*ALCODEEND*/}

double liberar_estoque(String linha)
{/*ALCODESTART::1794600000037*/
// solta o lote de vazios parado no Wait da linha para o TrainCouple
if ("b".equals(linha))      { if (estoque_b.size() > 0)     estoque_b.free(estoque_b.get(0)); }
else if ("a".equals(linha)) { if (estoque_a.size() > 0)     estoque_a.free(estoque_a.get(0)); }
else                        { if (estoque_moega.size() > 0) estoque_moega.free(estoque_moega.get(0)); }
/*ALCODEEND*/}

double proxima_peca(Trem trem)
{/*ALCODESTART::1794800000029*/
// deixa a sobra do alívio peça a peça, do rabo (lado da moega) para a frente: moega -> a -> b.
// A cauda do trem é o lado da moega; cada peça é cortada com cortar_peca (primeiros carros).
int sobra = get_vgs_trazidos_alivio(trem) - quantidade_alivio;
if (sobra <= 0) {
	fim_alivio.take(trem);
} else if (sobra > CAPACIDADE_LOTE_B + CAPACIDADE_LOTE_A) {
	// moega: peça de sobra - 35, do ponto da moega para o lado do encoste
	_peca_linha  = "moega";
	_peca_vagoes = sobra - CAPACIDADE_LOTE_B - CAPACIDADE_LOTE_A;
	// a cauda vai até moega + peça: a peça começa no ponto da moega e o lote em descarga completa a partir dela
	if (distancia_peca_moega(trem) < 0.5) entrar_cortar_peca.take(trem);
	else                                  entrar_mover_moega.take(trem);
} else if (sobra > CAPACIDADE_LOTE_B) {
	// a: cauda no _coperXXI_lote_a_aux_mnb (cauda do lote a)
	_peca_linha  = "a";
	_peca_vagoes = sobra - CAPACIDADE_LOTE_B;
	// lote a foi o último engatado: a cauda do trem é a cauda do próprio lote, já no lugar
	if ("a".equals(_ultima_linha_engatada) || cauda_no_ponto(trem, main()._coperXXI_lote_a_aux_mnb)) entrar_cortar_peca.take(trem);
	else                                                                                               entrar_mover_a.take(trem);
} else {
	// b: cauda no _coperXXI_lote_b_aux_mnb (cauda do lote b)
	_peca_linha  = "b";
	_peca_vagoes = sobra;
	// lote b foi o único engatado: a cauda do trem é a cauda do próprio lote, já no lugar
	if ("b".equals(_ultima_linha_engatada) || cauda_no_ponto(trem, main()._coperXXI_lote_b_aux_mnb)) entrar_cortar_peca.take(trem);
	else                                                                                               entrar_mover_b.take(trem);
}
if (sobra > 0)
	Log.info("CoperXXI", "proxima_peca", String.format("trem=%d sobra=%d -> %d vgs no lote %s (pedido=%d)",
		trem.getId(), sobra, _peca_vagoes, _peca_linha, quantidade_alivio));
/*ALCODEEND*/}

double guardar_peca(Trem lote)
{/*ALCODESTART::1794800000030*/
// peça da sobra cortada: vira o lote de vazios da linha e entra no estoque (Wait) dela
lote.tipoDeLote = "coperXII_vazios_" + _peca_linha;
if ("b".equals(_peca_linha))      { lote_vazios_b = lote;     entrar_estoque_b.take(lote); }
else if ("a".equals(_peca_linha)) { lote_vazios_a = lote;     entrar_estoque_a.take(lote); }
else                              { lote_vazios_moega = lote; entrar_estoque_moega.take(lote); }
Log.info("CoperXXI", "peca_guardada", String.format("trem=%d %d vgs no lote %s | b=%d a=%d moega=%d",
	lote.getId(), lote.getNumeroDeVagoes(), _peca_linha,
	CAPACIDADE_LOTE_B - get_espaco_linha("b"), CAPACIDADE_LOTE_A - get_espaco_linha("a"), get_capacidade_moega() - get_espaco_linha("moega")));
/*ALCODEEND*/}

boolean cauda_no_ponto(Trem trem,PositionOnTrack ponto)
{/*ALCODESTART::1794800000031*/
// a cauda do trem (primeiro carro, lado da moega) já está no ponto? Evita um TrainMoveTo de distância 0
try {
	Agent carro = trem.getFirst();
	java.lang.reflect.Method getTrack  = carro.getClass().getMethod("getTrack", boolean.class);
	java.lang.reflect.Method getOffset = carro.getClass().getMethod("getOffset", boolean.class);
	double p = ponto.getOffset(METER);
	for (boolean frente : new boolean[] { true, false }) {
		if ((RailwayTrack) getTrack.invoke(carro, frente) == ponto.getTrack()
			&& Math.abs((double) getOffset.invoke(carro, frente) - p) < 0.5) return true;
	}
} catch (Exception e) {
	Log.warn("CoperXXI", "cauda_no_ponto", "nao leu a posicao do trem: " + e);
}
return false;
/*ALCODEEND*/}

double distancia_peca_moega(Trem trem)
{/*ALCODESTART::1794810000008*/
// quanto o trem anda de ré (para o Valongo) até a cauda (primeiro carro, lado do encoste) ficar em
// _coperXII_moega + comprimento da peça. Assim a peça da moega começa sempre no ponto da moega.
try {
	double alvo = main()._coperXII_moega.getOffset(METER);
	for (int i = 0; i < _peca_vagoes; i++) alvo += trem.getCar(i).getLength();
	Agent cauda = trem.getFirst();
	java.lang.reflect.Method getOffset = cauda.getClass().getMethod("getOffset", boolean.class);
	double fim = Math.max((double) getOffset.invoke(cauda, true), (double) getOffset.invoke(cauda, false));
	double d = fim - alvo;
	Log.info("CoperXXI", "distancia_peca_moega", String.format("trem=%d cauda=%.1f alvo=%.1f distancia=%.1f", trem.getId(), fim, alvo, d));
	return Math.max(0, d);
} catch (Exception e) {
	Log.warn("CoperXXI", "distancia_peca_moega", "nao leu a posicao do trem: " + e);
	return 0;
}
/*ALCODEEND*/}

boolean pode_descarregar_grupo()
{/*ALCODESTART::1794900000002*/
// cabe vazio no terminal e não há alívio pedido nem em andamento (descarga e alívio nunca ao mesmo tempo)
return get_espaco_vazios() > 0 && !aguardando_alivio && !fazendoAlivio;
/*ALCODEEND*/}

