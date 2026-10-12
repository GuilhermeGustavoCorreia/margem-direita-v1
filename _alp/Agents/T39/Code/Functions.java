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
int vagoes_linha_10 	 = main().getTamanhoDoTrem(lote_linha_10);
int vagoes_linha_9 		 = main().getTamanhoDoTrem(lote_linha_9);
int vagoes_linha_9_fundo = main().getTamanhoDoTrem(lote_linha_9_fundo);




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

    traceln("erro: trem não tem nenhum vagão");
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
return true;
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
boolean ultimos_vazios_no_terminal = (_getVagoesCarregados() == 0 && _getVagoesVazios() > 0);

//ultimos vagoes
if (moega_esta_desocupada && ultimos_vazios_no_terminal) {
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
int vagoes_linha_10 	 = main().getTamanhoDoTrem(lote_linha_10);
int vagoes_linha_9 		 = main().getTamanhoDoTrem(lote_linha_9);
int vagoes_linha_9_fundo = main().getTamanhoDoTrem(lote_linha_9_fundo);




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

