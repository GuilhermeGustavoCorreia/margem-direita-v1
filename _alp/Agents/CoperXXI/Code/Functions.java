Main main()
{/*ALCODESTART::1785421553067*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1791650000004*/
// trem entrando: do comecar_encoste até o lote 1 entrar no ciclo da moega
if (_trem_chegando != null) return false;
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
// ALT-076: pede alívio quando lote_b, lote_a e o lote da moega somam QTD_ALIVIO
if (!aguardando_alivio && get_espaco_vazios() == 0) {
	aguardando_alivio = true;
	traceln(String.format("[%.1f][CoperXII][alivio] %d vazios prontos (b=%d a=%d moega=%d): pedindo alívio",
		time(), QTD_ALIVIO, CAPACIDADE_LOTE_B, CAPACIDADE_LOTE_A, get_capacidade_moega()));
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

double guardar_excesso_alivio(Trem lote)
{/*ALCODESTART::1794506038076*/
// sobra do alívio (rabo do trem, lado da moega): espera a loco sair da linha,
// anda até o lote_b e é repartida em b -> a -> moega (sobra_cabe / cortar_sobra / guardar_sobra)
trem_sobra = lote;
lote.tipoDeLote = "coperXII_sobra";
Log.info("CoperXXI", "alivio_sobra", String.format("trem=%d sobra de %d vgs (pedido=%d)",
	lote.getId(), lote.getNumeroDeVagoes(), quantidade_alivio));
entrar_sobra.take(lote);
/*ALCODEEND*/}

boolean ainda_tem_lote_vazios()
{/*ALCODESTART::1794600000031*/
// ainda há lote de vazios parado em b, a ou moega? O alívio engata todos antes de cortar a sobra
return lote_vazios_b != null || lote_vazios_a != null || lote_vazios_moega != null;
/*ALCODEEND*/}

boolean linha_livre_para_sobra()
{/*ALCODESTART::1794600000032*/
// a sobra só anda depois que o trem do alívio saiu da psnSaidaCopexXXI_b (as duas pontas fora da linha)
if (fazendoAlivio) return false;
if (trem_alivio == null) return true;
try {
	RailwayTrack linha = main().psnSaidaCopexXXI_b;
	return trem_alivio.getTrack(true) != linha && trem_alivio.getTrack(false) != linha;
} catch (Exception e) {
	// o trem do alívio já não existe (acoplado nas DVs ou removido)
	return true;
}
/*ALCODEEND*/}

int get_capacidade_linha(String linha)
{/*ALCODESTART::1794600000033*/
// capacidade de vazios de cada posição da linha
if ("b".equals(linha)) return CAPACIDADE_LOTE_B;
if ("a".equals(linha)) return CAPACIDADE_LOTE_A;
return get_capacidade_moega();
/*ALCODEEND*/}

String sobra_linha_alvo()
{/*ALCODESTART::1794600000034*/
// a sobra enche b, depois a, depois o lote da moega
if (lote_vazios_b == null) return "b";
if (lote_vazios_a == null) return "a";
return "moega";
/*ALCODEEND*/}

double registrar_sobra(Trem trem)
{/*ALCODESTART::1794600000035*/
// marca o pedaço da sobra com a linha alvo e grava o lote de vazios dela
String linha = sobra_linha_alvo();
trem.tipoDeLote = "coperXII_vazios_" + linha;
if ("b".equals(linha))      lote_vazios_b = trem;
else if ("a".equals(linha)) lote_vazios_a = trem;
else                        lote_vazios_moega = trem;
/*ALCODEEND*/}

double guardar_sobra(Trem trem,boolean ultima)
{/*ALCODESTART::1794600000036*/
// guarda um pedaço da sobra no estoque da linha dele; o último pedaço retoma a descarga
if (trem != lote_vazios_b && trem != lote_vazios_a && trem != lote_vazios_moega) registrar_sobra(trem);
if (trem == lote_vazios_b)      entrar_estoque_b.take(trem);
else if (trem == lote_vazios_a) entrar_estoque_a.take(trem);
else                            entrar_estoque_moega.take(trem);
Log.info("CoperXXI", "sobra_guardada", String.format("trem=%d %d vgs em %s | b=%d a=%d moega=%d",
	trem.getId(), trem.getNumeroDeVagoes(), trem.tipoDeLote,
	CAPACIDADE_LOTE_B - get_espaco_linha("b"), CAPACIDADE_LOTE_A - get_espaco_linha("a"), get_capacidade_moega() - get_espaco_linha("moega")));
if (ultima) {
	trem_sobra = null;
	verificar_alivio();
	liberar_espaco_vazios();
	iniciar_proximo_lote();
}
/*ALCODEEND*/}

double liberar_estoque(String linha)
{/*ALCODESTART::1794600000037*/
// solta o lote de vazios parado no Wait da linha para o TrainCouple
if ("b".equals(linha))      { if (estoque_b.size() > 0)     estoque_b.free(estoque_b.get(0)); }
else if ("a".equals(linha)) { if (estoque_a.size() > 0)     estoque_a.free(estoque_a.get(0)); }
else                        { if (estoque_moega.size() > 0) estoque_moega.free(estoque_moega.get(0)); }
/*ALCODEEND*/}

