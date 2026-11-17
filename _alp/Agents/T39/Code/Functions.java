Main main()
{/*ALCODESTART::1790853272871*/
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

double liberar_1vg()
{/*ALCODESTART::1790867513660*/
if (aguardar_moega.size() > 0) {
    aguardar_moega.free(aguardar_moega.get(0));
}
/*ALCODEEND*/}

int _getVagoesVazios()
{/*ALCODESTART::1790875052692*/
// só vagões (uma loco parada numa linha de vazios não conta)
int vagoes_linha_10 	 = main().getNumeroDeVagoes(lote_linha_10);
int vagoes_linha_9 		 = main().getNumeroDeVagoes(lote_linha_9);
int vagoes_linha_9_fundo = main().getNumeroDeVagoes(lote_linha_9_fundo);




return (vagoes_linha_9_fundo + vagoes_linha_9 + vagoes_linha_10);

/*ALCODEEND*/}

int _getCapacidadeVazios()
{/*ALCODESTART::1790876157092*/
return (
	CAPACIDADE_LINHA_10 + 
	CAPACIDADE_LINHA_9 + 
	CAPACIDADE_LINHA_9_FUNDO
);

/*ALCODEEND*/}

boolean trem_possui_locomotiva(Trem train)
{/*ALCODESTART::1790883461898*/
boolean temLoco = false;
for (int i = 0; i < train.size(); i++) {
    Agent carro = train.getCar(i);
    if (carro instanceof Locomotiva && !((Locomotiva) carro).de_tracao) {
        temLoco = true;
        break;
    }
}

return temLoco;
/*ALCODEEND*/}

int _getVagoesCarregados()
{/*ALCODESTART::1790941164714*/
int vagoes_linha_1000_emDescarga = main().getNumeroDeVagoes(lote_linha_1000_emDescarga);
int vagoes_linha_1000_proximo 	 = main().getNumeroDeVagoes(lote_linha_1000_proximo);
int vagoes_linha_11				 = main().getNumeroDeVagoes(lote_linha_11);
return (vagoes_linha_1000_emDescarga + vagoes_linha_1000_proximo + vagoes_linha_11);
/*ALCODEEND*/}

int _getVagoes_linha_1000()
{/*ALCODESTART::1790942063724*/
int vagoes_linha_1000_emDescarga = main().getTamanhoDoTrem(lote_linha_1000_emDescarga);
int vagoes_linha_1000_proximo 	 = main().getTamanhoDoTrem(lote_linha_1000_proximo);

return (vagoes_linha_1000_emDescarga + vagoes_linha_1000_proximo);
/*ALCODEEND*/}

boolean sobraram_somente_locomotivas(Trem train)
{/*ALCODESTART::1790946504960*/
//traceln("[soLocos] trem " + train + " | n=" + train.size());

if (train.size() == 0) {
    //traceln("[soLocos]   trem vazio -> false");
    return false;
}

for (int i = 0; i < train.size(); i++) {
    Agent car = train.getCar(i);
    if (!(car instanceof Locomotiva)) {
        //traceln("[soLocos]   carro " + i + " não é loco (" + car.getClass().getSimpleName() + ") -> false");
        return false;
    }
}

//traceln("[soLocos]   todos os " + train.size() + " carros são locos -> true");
return true;
/*ALCODEEND*/}

double atualizarFerroviaEProdutoEmAtendimento(Trem trem)
{/*ALCODESTART::1790949291562*/
for (int i = 0; i < trem.size(); i++) {
        Agent carro = trem.getCar(i); // confere o nome exato do método com Ctrl+Espaço

        if (carro instanceof Vagao) {
            Vagao vagao = (Vagao) carro;
            ferrovia_em_atendimento = vagao.ferrovia;
            produto_em_atendimento = vagao.segmento;
            return; // achou o primeiro vagão, encerra
        }
    }

    Log.erro("T39", "trem_sem_vagao", "trem não tem nenhum vagão");
/*ALCODEEND*/}

String getPatio()
{/*ALCODESTART::1790949914449*/
return "pcx";
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790949914451*/
return produto_em_atendimento;
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790949914453*/
return ferrovia_em_atendimento;
/*ALCODEEND*/}

boolean podeReceberNovoLote()
{/*ALCODESTART::1790949987803*/
// trem entrando: do encoste_linha1000 até parar na linha 1000
if (_trem_chegando != null) return false;
// a 1000 comporta um trem só: o 2º espera no ISN
return lote_linha_1000_emDescarga == null && lote_linha_1000_proximo == null;
/*ALCODEEND*/}

PositionOnTrack get_pos_alivio()
{/*ALCODESTART::1790950057448*/
return main().pos_t39_alivio;
/*ALCODEEND*/}

int getNumeroLocomotivasAlivio()
{/*ALCODESTART::1790950057450*/
return 2;
/*ALCODEEND*/}

ArrayList<Faixa> get_faixas_primeiro_alivio()
{/*ALCODESTART::1790950057452*/
return primeiro_alivio_faixas;
/*ALCODEEND*/}

ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios()
{/*ALCODESTART::1790950057456*/
return new ArrayList<>(Arrays.asList(
	main().gestorDeCirculacao.faixa_via1_intermar_d, 
	main().gestorDeCirculacao.faixa_via1_intermar_c
));
/*ALCODEEND*/}

float getOverloadVazios()
{/*ALCODESTART::1790950228294*/
boolean moega_esta_desocupada     = moega_ocupada.idle() > 0;
// segundo lote de outra ferrovia/segmento não conta: ele só entra depois do alívio destes vazios
int carregados = _getVagoesCarregados();
if (lote_linha_1000_proximo != null && !eh_mesmo_tipo_em_atendimento(lote_linha_1000_proximo))
    carregados -= main().getNumeroDeVagoes(lote_linha_1000_proximo);
boolean ultimos_vazios_no_terminal = (carregados <= 0 && _getVagoesVazios() > 0);

//ultimos vagoes
if (moega_esta_desocupada && ultimos_vazios_no_terminal) {
    return (float) 2.0;
}

// lote esperando lugar para o próximo corte: força o alívio
if (moega_esta_desocupada && _getVagoesVazios() > 0 && esperar_espaco_corte.size() > 0
    && espaco_livre_vazios() < tamanho_proximo_corte()) {
    return (float) 2.0;
}

if (QUANTIDADE_PARA_ALIVIAR <= 0) return 0;
return (float) _getVagoesVazios() / QUANTIDADE_PARA_ALIVIAR;
/*ALCODEEND*/}

double takeAlivio(Trem trem)
{/*ALCODESTART::1790950531805*/
iniciar_alivio.take(trem);
/*ALCODEEND*/}

int atualizar_faixa_linha_1000_a(Trem trem)
{/*ALCODESTART::1790966264316*/
Faixa faixa_linha_1000_a = main().gestorDeCirculacao.faixa_linha1000_a;

boolean temTrem_linha_1000_a = !faixa_linha_1000_a.faixaEstaVazia();

if(temTrem_linha_1000_a){
    faixa_linha_1000_a.ocupar(trem);
}else{

    faixa_linha_1000_a.liberar();
}
/*ALCODEEND*/}

double liberar_wait_lote_linha_09()
{/*ALCODESTART::1791221470640*/
// libera o primeiro que entrou
if (wait_lote_linha_09.size() > 0) {
    wait_lote_linha_09.free(wait_lote_linha_09.get(0));
}
/*ALCODEEND*/}

double set_quantidade_alivio(int qt_quantidade)
{/*ALCODESTART::1791222699002*/
quantidade_alivio = qt_quantidade;
/*ALCODEEND*/}

int get_quantidade_vazios()
{/*ALCODESTART::1791224200391*/
// só vagões (uma loco parada numa linha de vazios não conta)
int vagoes_linha_10 	 = main().getNumeroDeVagoes(lote_linha_10);
int vagoes_linha_9 		 = main().getNumeroDeVagoes(lote_linha_9);
int vagoes_linha_9_fundo = main().getNumeroDeVagoes(lote_linha_9_fundo);




return (vagoes_linha_9_fundo + vagoes_linha_9 + vagoes_linha_10);
/*ALCODEEND*/}

double liberar_wait_lote_linha_10()
{/*ALCODESTART::1791284655682*/
// libera o primeiro que entrou
if (wait_lote_linha_10.size() > 0) {
    wait_lote_linha_10.free(wait_lote_linha_10.get(0));
}
/*ALCODEEND*/}

double conectarSeLivre(RailwaySwitch sw,RailwayTrack a,RailwayTrack  b)
{/*ALCODESTART::1791285282176*/
if (!a.getCars().isEmpty() || !b.getCars().isEmpty()) {
   
    return;
}
sw.connectTracks(a, b);
/*ALCODEEND*/}

double liberar_wait_lote_linha_9_fundo()
{/*ALCODESTART::1791289594831*/
// libera o primeiro que entrou
if (wait_lote_linha_9_fundo.size() > 0) {
    wait_lote_linha_9_fundo.free(wait_lote_linha_9_fundo.get(0));
}
/*ALCODEEND*/}

double alvoAcumulado4000(Trem trem)
{/*ALCODESTART::1791296866655*/
RailwayTrack[] linhas = { main().pmc_linha4000_a, main().pmc_linha4000_b, main().pmc_linha4000_c };

double total = 0;
for (RailwayTrack l : linhas) total += l.length(METER);

double compTrem = trem.getLength(METER);
double alvo = Math.max(0, total - compTrem);


// posição atual da cauda (último carro, lidera no recuo)
Agent cauda = trem.getLast();
try {
    
    RailwayTrack tr = (RailwayTrack) cauda.getClass().getMethod("getTrack", boolean.class).invoke(cauda, false);
    
    double off = (double) cauda.getClass().getMethod("getOffset", boolean.class).invoke(cauda, false);
    double acum = 0;
    
    for (RailwayTrack l : linhas) {
        if (tr == l) {
            double atual = acum + off;

            if (atual <= alvo + 0.5) {
                
                double novo = Math.max(0, atual - 10);
                return novo;
                
            }
            
            break;
            
        }
        
        acum += l.length(METER);
        
    }
} catch (Exception e) {
    error("alvoAcumulado4000: " + e.getMessage());
}

return alvo;
/*ALCODEEND*/}

RailwayTrack getTrack4000(Trem trem)
{/*ALCODESTART::1791296902104*/
RailwayTrack[] linhas = { main().pmc_linha4000_a, main().pmc_linha4000_b, main().pmc_linha4000_c };
double alvo = alvoAcumulado4000(trem);

double acum = 0;
for (RailwayTrack l : linhas) {
    double comp = l.length(METER);
    if (alvo < acum + comp) return l;
    acum += comp;
}
return linhas[linhas.length - 1];
/*ALCODEEND*/}

double getOffset4000(Trem trem)
{/*ALCODESTART::1791296932666*/
RailwayTrack[] linhas = { main().pmc_linha4000_a, main().pmc_linha4000_b, main().pmc_linha4000_c };
double alvo = alvoAcumulado4000(trem);

double acum = 0;
for (RailwayTrack l : linhas) {
    double comp = l.length(METER);
    if (alvo < acum + comp) return alvo - acum;
    acum += comp;
}
return linhas[linhas.length - 1].length(METER);
/*ALCODEEND*/}

String get_tipo_loco_alivio()
{/*ALCODESTART::1791700000011*/
// "manobra": loco FIPS PST criada em pos_locoManobra_PST | "ferrovia": loco da ferrovia em atendimento
return "ferrovia";
/*ALCODEEND*/}

int get_quantidade_loco_alivio()
{/*ALCODESTART::1791700000012*/
return 1;
/*ALCODEEND*/}

boolean terminal_vazio()
{/*ALCODESTART::1798200000302*/
// sem trem chegando, sem lote na 1000 e sem vagões (carregados ou vazios) no terminal
return _trem_chegando == null
    && lote_linha_1000_emDescarga == null
    && lote_linha_1000_proximo == null
    && _getVagoesCarregados() == 0
    && _getVagoesVazios() == 0;
/*ALCODEEND*/}

double tentar_liberar_espaco_vazios()
{/*ALCODESTART::1798200000411*/
// solta o vagão parado no wait_espaco_vazios quando abrir lugar na linha 10, na 9 ou no fundo da 9
if (wait_espaco_vazios.size() == 0) return;
boolean tem_espaco =
	main().getTamanhoDoTrem(lote_linha_10)      < CAPACIDADE_LINHA_10 ||
	main().getTamanhoDoTrem(lote_linha_9)       < CAPACIDADE_LINHA_9  ||
	main().getTamanhoDoTrem(lote_linha_9_fundo) < CAPACIDADE_LINHA_9_FUNDO;
if (!tem_espaco) return;
Agent vagao = wait_espaco_vazios.get(0);
Log.info("T39", "descarga", String.format("espaco liberado, vagao=%d segue para as linhas de vazios", vagao.getId()));
wait_espaco_vazios.free(vagao);
/*ALCODEEND*/}

boolean eh_mesmo_tipo_em_atendimento(Trem trem)
{/*ALCODESTART::1798200000442*/
// compara o 1º vagão do trem com a ferrovia/segmento em atendimento
for (int i = 0; i < trem.size(); i++) {
    Agent carro = trem.getCar(i);
    if (carro instanceof Vagao) {
        Vagao v = (Vagao) carro;
        return java.util.Objects.equals(v.ferrovia, getFerrovia())
            && java.util.Objects.equals(v.segmento, getSegmento());
    }
}
return false;
/*ALCODEEND*/}

int espaco_livre_vazios()
{/*ALCODESTART::1798200000543*/
// lugares livres nas linhas 9, 10 e fundo da 9, descontando o que ainda vai chegar nelas
// (vagões esperando espaço, o vagão na moega e o corte na linha 11)
int capacidade = CAPACIDADE_LINHA_10 + CAPACIDADE_LINHA_9 + CAPACIDADE_LINHA_9_FUNDO;
int ocupados = main().getTamanhoDoTrem(lote_linha_10) + main().getTamanhoDoTrem(lote_linha_9)
    + main().getTamanhoDoTrem(lote_linha_9_fundo);
int a_caminho = wait_espaco_vazios.size() + moega_ocupada.busy();
if (linha_11_ocupada.idle() == 0) a_caminho += main().getNumeroDeVagoes(lote_linha_11);
return capacidade - ocupados - a_caminho;
/*ALCODEEND*/}

int tamanho_proximo_corte()
{/*ALCODESTART::1798200000544*/
// próximo corte do lote parado no esperar_espaco_corte
if (esperar_espaco_corte.size() == 0) return 0;
Trem lote = (Trem) esperar_espaco_corte.get(0);
return Math.min(CAPACIDADE_LINHA_11, main().getNumeroDeVagoes(lote));
/*ALCODEEND*/}

double tentar_liberar_espaco_corte()
{/*ALCODESTART::1798200000545*/
// o lote só sai da 1000_a para tirar o corte com a linha 11 livre e lugar para todo o corte
// nas linhas de vazios; senão a cauda do corte para em cima da 1000_b/c e trava o alívio
if (esperar_espaco_corte.size() == 0) return;
if (linha_11_ocupada.idle() == 0) return;
int corte = tamanho_proximo_corte();
int livre = espaco_livre_vazios();
if (livre < corte) return;
Agent lote = esperar_espaco_corte.get(0);
Log.info("T39", "corte", String.format("lote=%d segue: corte=%d livre=%d", lote.getId(), corte, livre));
esperar_espaco_corte.free(lote);
/*ALCODEEND*/}

String lote_txt(Trem t)
{/*ALCODESTART::1798200000712*/
if (t == null) return "-";
return t.getId() + ":" + main().getNumeroDeVagoes(t);
/*ALCODEEND*/}

String estado_resumo()
{/*ALCODESTART::1798200000713*/
return String.format("vz=%d cg=%d ov=%.2f moega=%s", _getVagoesVazios(), _getVagoesCarregados(), getOverloadVazios(),
    (moega_ocupada.idle() > 0 ? "livre" : "ocupada"));
/*ALCODEEND*/}

double log_analise(String evento,Agent a,String msg)
{/*ALCODESTART::1798200000714*/
// log de análise: só escreve com log_de_analise = true
if (!log_de_analise) return;
String t = (a == null) ? "-" : String.valueOf(a.getId());
if (a instanceof Trem) {
	Trem tr = (Trem) a;
	t = String.format("trem=%d carros=%d vgs=%d locos=%d", tr.getId(), tr.size(), tr.getNumeroDeVagoes(), tr.getNumeroDeLocomotivas());
}
Log.info("T39", evento, t + " | " + msg + " | " + estado_resumo());
/*ALCODEEND*/}

double log_estado()
{/*ALCODESTART::1798200000715*/
// snapshot do terminal para o log de análise (idle/busy dos pools, tamanho dos waits)
if (!log_de_analise) return;
Log.info("T39", "estado", estado_resumo());
Log.info("T39", "estado_lotes", "lote_linha_1000_emDescarga=" + lote_txt(lote_linha_1000_emDescarga) +
    " lote_linha_1000_proximo=" + lote_txt(lote_linha_1000_proximo) +
    " lote_linha_11=" + lote_txt(lote_linha_11) +
    " lote_linha_10=" + lote_txt(lote_linha_10) +
    " lote_linha_9=" + lote_txt(lote_linha_9) +
    " lote_linha_9_fundo=" + lote_txt(lote_linha_9_fundo) +
    " _trem_chegando=" + lote_txt(_trem_chegando) +
    " lote_moega=" + (int) lote_moega);
Log.info("T39", "estado_pools", "linha_11_ocupada=" + linha_11_ocupada.idle() + "/" + linha_11_ocupada.busy() +
    " moega_ocupada=" + moega_ocupada.idle() + "/" + moega_ocupada.busy() +
    " fazendo_alivio=" + fazendo_alivio.idle() + "/" + fazendo_alivio.busy() +
    " linha_1000_emDescarga=" + linha_1000_emDescarga.idle() + "/" + linha_1000_emDescarga.busy());
Log.info("T39", "estado_waits", "aguardar_moega=" + aguardar_moega.size() +
    " wait_lote_linha_09=" + wait_lote_linha_09.size() +
    " wait_lote_linha_10=" + wait_lote_linha_10.size() +
    " wait_lote_linha_9_fundo=" + wait_lote_linha_9_fundo.size() +
    " wait_espaco_vazios=" + wait_espaco_vazios.size() +
    " esperar_espaco_corte=" + esperar_espaco_corte.size());
/*ALCODEEND*/}
