int _contarVagoes(Trem trem)
{/*ALCODESTART::1793300679212*/
int contador = 0;

for (int i = 0; i < trem.size(); i++) {

    Agent vagao = trem.getCar(i);

    if (!(vagao instanceof Locomotiva)) {
        contador++;
    }
}

return contador;
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1793300679216*/
// ALT-003: CoperXXI ainda não usa esse critério; só não recebe durante alívio
return !fazendoAlivio;
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1793300679218*/
// ALT-066: alívio do CoperXII quando lote_a e lote_b estão cheios e o resto do lote já descarregou na moega
// ALT-071: pede alívio no fim de todo lote (aguardando_alivio)
if (aguardando_alivio) return 2;
int capacidade = CAPACIDADE_LOTE_A + CAPACIDADE_LOTE_B;
if (capacidade <= 0) return 0;
return Math.min(1f, (float) _getVagosVazios() / capacidade);
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1793300679220*/
return "pst";
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1793300679222*/
return produto_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1793300679224*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1793300679226*/
// ALT-069
fazendoAlivio = true;
iniciar_alivio.take(trem);
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1793300679228*/
return 2;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1793300679230*/
// ALT-080: loco do alívio para no lote_b do MoegaoTerra (railwayTrack15)
return main()._moegaoTerra_lote_b;
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_primeiro_alivio()
{/*ALCODESTART::1793300679232*/
// ALT-080: linhas físicas entre a faixa de criação (via1_intermar_d) e a railwayTrack15 (lote_b do MoegaoTerra).
// Não são faixas da circulação: só entram na rota da loco (TrainMoveTo por lista de linhas).
// Sem recurso (não licencia nada) e com desenho invisível (o gestor pinta a faixa ao ocupar).
ArrayList<Faixa> faixas = new ArrayList<Faixa>();
for (RailwayTrack t : new RailwayTrack[] { main().linhaPaqueta_a, main().psnSaidaCopexXXI_a, main().pstSaidaMoegao }) { // ALT-080
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
{/*ALCODESTART::1793300679234*/
// ALT-003: provisório — sem faixas até integrar com o GestorDeAlivios
return new ArrayList<Faixa>();
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1793300679236*/
// ALT-003: provisório — alívio do CoperXXI ainda é próprio (leva tudo das linhas a e b)
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1793300679238*/
// ALT-066: lote_b + lote_a + lote da moega
return _getVagosVazios() + ((lote_vazios_moega == null) ? 0 : lote_vazios_moega.getNumeroDeVagoes());
/*ALCODEEND*/}

int _getVagosVazios()
{/*ALCODESTART::1793300679240*/
// ALT-077: vagões parados em lote_b + lote_a (contagem direta; a conta pelo espaço dava -6 com a moega no espaço)
return ((lote_vazios_b == null) ? 0 : lote_vazios_b.getNumeroDeVagoes())
     + ((lote_vazios_a == null) ? 0 : lote_vazios_a.getNumeroDeVagoes());
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1793300679242*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1793300679244*/
return 1;
/*ALCODEEND*/}

int get_carros_ate_segunda_loco(Trem trem)
{/*ALCODESTART::1793300679246*/
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
{/*ALCODESTART::1793300679248*/
// ALT-065/076: vagões que ainda cabem em lote_b, lote_a ou no lote da moega
Trem lote; int cap;
if ("b".equals(linha))          { lote = lote_vazios_b;     cap = CAPACIDADE_LOTE_B; }
else if ("a".equals(linha))     { lote = lote_vazios_a;     cap = CAPACIDADE_LOTE_A; }
else                            { lote = lote_vazios_moega; cap = get_capacidade_moega(); }
int ocup = (lote == null) ? 0 : lote.getNumeroDeVagoes();
return Math.max(0, cap - ocup);
/*ALCODEEND*/}

int get_espaco_vazios()
{/*ALCODESTART::1793300679250*/
// ALT-076: espaço até fechar o alívio de QTD_ALIVIO (lote_b + lote_a + lote da moega)
return get_espaco_linha("b") + get_espaco_linha("a") + get_espaco_linha("moega");
/*ALCODEEND*/}

double definir_grupo(Trem lote)
{/*ALCODESTART::1793300679252*/
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

traceln(String.format("[%.1f][MoegaoTerra][grupo] trem=%d linha=%s vagoes=%d carros=%d (lote tem %d carros, espaco b=%d a=%d)",
	time(), lote.getId(), _grupo_linha, _grupo_vagoes, _grupo_carros, lote.size(), get_espaco_linha("b"), get_espaco_linha("a")));
/*ALCODEEND*/}

double registrar_vazios(Trem trem)
{/*ALCODESTART::1793300679254*/
// ALT-065: marca a linha do grupo no próprio trem e atualiza o lote de vazios
// (feito antes de liberar a moega, porque o próximo lote recalcula _grupo_linha)
trem.tipoDeLote = "coperXII_vazios_" + _grupo_linha;
if ("b".equals(_grupo_linha)) lote_vazios_b = trem;
else                          lote_vazios_a = trem;

if (trem.getNumeroDeLocomotivas() > 0)
	traceln(String.format("[%.1f][MoegaoTerra][vazios] ⚠️ trem=%d ficou com %d loco(s) no meio do lote de vazios",
		time(), trem.getId(), trem.getNumeroDeLocomotivas()));
/*ALCODEEND*/}

double guardar_vazios(Trem trem)
{/*ALCODESTART::1793300679256*/
// ALT-065: guarda o lote na linha (Hold bloqueado) e solta o resto do lote que espera na moega
if ("coperXII_vazios_b".equals(trem.tipoDeLote)) {
	estoque_b.block();
	entrar_estoque_b.take(trem);
} else {
	estoque_a.block();
	entrar_estoque_a.take(trem);
}

traceln(String.format("[%.1f][MoegaoTerra][vazios] trem=%d guardado (%s) | lote_b=%d/%d lote_a=%d/%d",
	time(), trem.getId(), trem.tipoDeLote,
	CAPACIDADE_LOTE_B - get_espaco_linha("b"), CAPACIDADE_LOTE_B,
	CAPACIDADE_LOTE_A - get_espaco_linha("a"), CAPACIDADE_LOTE_A));

verificar_alivio(); // ALT-076
aguarda_grupo.unblock();
/*ALCODEEND*/}

double liberar_espaco_vazios()
{/*ALCODESTART::1793300679258*/
// ALT-065: chamar depois do alívio das linhas de vazios (GestorDeAlivios, a integrar)
aguarda_espaco_vazios.unblock();
/*ALCODEEND*/}

double alivio_seguir(Trem trem)
{/*ALCODESTART::1793300679260*/
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
{/*ALCODESTART::1793300679262*/
// ALT-073: solta o lote_em_descarga (antiga segunda parte, parada no Wait espera_segunda_parte)
if (lote_em_descarga != null && lote_em_descarga.getNumeroDeVagoesCarregados() > 0 && esta_na_espera(lote_em_descarga)) {
	traceln(String.format("[%.1f][MoegaoTerra][segunda_parte] trem=%d começa a descarga (%d vgs carregados, locos=%d)",
		time(), lote_em_descarga.getId(), lote_em_descarga.getNumeroDeVagoesCarregados(), lote_em_descarga.getNumeroDeLocomotivas()));
	destino_segunda_parte = "descarga";
	espera_segunda_parte.free(lote_em_descarga);
}
/*ALCODEEND*/}

ResourcePool[] recursos_do_lote(Trem trem)
{/*ALCODESTART::1793300679264*/
// ALT-073: recursos que o lote segura no fim da descarga: rp_moega e, se pediu, a unidade da loco FIPS PST
List<ResourcePool> pools = new ArrayList<ResourcePool>();
if (!trem.resourceUnitsOfPool(rp_moega).isEmpty()) pools.add(rp_moega);
ResourcePool fips = main().gestorDeLocomotivas.rp_locoFips_pst;
if (!trem.resourceUnitsOfPool(fips).isEmpty()) pools.add(fips);
return pools.toArray(new ResourcePool[0]);
/*ALCODEEND*/}

double trocar_lote_em_descarga()
{/*ALCODESTART::1793300679266*/
// ALT-073: o lote em descarga acabou; a segunda parte passa a ser o lote em descarga
traceln(String.format("[%.1f][MoegaoTerra][segunda_parte] lote_em_descarga=%s -> %s",
	time(), lote_em_descarga == null ? "-" : String.valueOf(lote_em_descarga.getId()),
	lote_segunda_parte == null ? "-" : String.valueOf(lote_segunda_parte.getId())));
lote_em_descarga   = lote_segunda_parte;
lote_segunda_parte = null;
/*ALCODEEND*/}

double pedir_loco_segunda_parte()
{/*ALCODESTART::1793300679268*/
// ALT-073: solta a segunda parte do Wait para cortar a loco da cabeça dela (ela volta ao Wait sem a loco)
if (lote_segunda_parte != null && esta_na_espera(lote_segunda_parte)) {
	destino_segunda_parte = "loco";
	espera_segunda_parte.free(lote_segunda_parte);
} else {
	error("CoperXII: pedir_loco_segunda_parte sem segunda parte no Wait");
}
/*ALCODEEND*/}

boolean esta_na_espera(Trem trem)
{/*ALCODESTART::1793300679270*/
// ALT-073: o trem está parado no Wait espera_segunda_parte?
for (int i = 0; i < espera_segunda_parte.size(); i++) {
	if (espera_segunda_parte.get(i) == trem) return true;
}
return false;
/*ALCODEEND*/}

int get_capacidade_moega()
{/*ALCODESTART::1793300679272*/
// ALT-076: o lote da moega completa o alívio: QTD_ALIVIO - lote_b - lote_a (hoje 41 - 22 - 13 = 6)
return Math.max(0, QTD_ALIVIO - CAPACIDADE_LOTE_B - CAPACIDADE_LOTE_A);
/*ALCODEEND*/}

double verificar_alivio()
{/*ALCODESTART::1793300679274*/
// ALT-076: pede alívio quando lote_b, lote_a e o lote da moega somam QTD_ALIVIO
if (!aguardando_alivio && get_espaco_vazios() == 0) {
	aguardando_alivio = true;
	traceln(String.format("[%.1f][MoegaoTerra][alivio] %d vazios prontos (b=%d a=%d moega=%d): pedindo alívio",
		time(), QTD_ALIVIO, CAPACIDADE_LOTE_B, CAPACIDADE_LOTE_A, get_capacidade_moega()));
}
/*ALCODEEND*/}

Main main()
{/*ALCODESTART::1793301192443*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

