int getNumeroDeLocomotivas()
{/*ALCODESTART::1773066591676*/
int nLocos = 0;

for(int i = 0 ; i < this.size(); i++){
	if(this.getCar(i).getClass() == Locomotiva .class){
	      nLocos ++;
    }
}
return nLocos;   
/*ALCODEEND*/}

int getNumeroDeVagoes()
{/*ALCODESTART::1773066591678*/
int nVagoes = 0;

for(int i = 0 ; i < this.size(); i++){
	if(this.getCar(i).getClass() == Vagao .class){
	      nVagoes ++;
    }
}
return nVagoes;  
/*ALCODEEND*/}

double getComprimentoTrem()
{/*ALCODESTART::1773066591680*/
   double comprimento = 0;

    for (int i = 0; i < this.size(); i++) {
        comprimento += this.getCar(i).getLength();
    }

    return comprimento; 
/*ALCODEEND*/}

int getNumeroDeVagoesCarregados()
{/*ALCODESTART::1773066591682*/
int nVagoes = 0;

for(int i = 0 ; i < this.size(); i++){
	if(this.getCar(i).getClass() == Vagao .class && ((Vagao)getCar(i)).carregado){
	      nVagoes ++;
    }
}
return nVagoes;  
/*ALCODEEND*/}

int getPosicaoLocomotivaComando()
{/*ALCODESTART::1773066591684*/
if(this.getFirst().getClass() == Locomotiva.class ){

	if(((Locomotiva)this.getFirst()).comando){ 
		return 1; 
	} 
}

if(this.getLast().getClass() == Locomotiva.class){ 

	if(((Locomotiva)this.getLast()).comando){ 
		return -1; 
	} 
}

//error ("Sentido de Comando não definido"); 
 
return 0;
/*ALCODEEND*/}

int getNumeroDeVagoesVazios()
{/*ALCODESTART::1773066591686*/
int nVagoes = 0;

for(int i = 0 ; i < this.size(); i++){
	if(this.getCar(i).getClass() == Vagao .class && !((Vagao)getCar(i)).carregado){
	      nVagoes ++;
    }
}
return nVagoes;  
/*ALCODEEND*/}

boolean estaNaLinha(Object linha)
{/*ALCODESTART::1773066591688*/
for (Object t : getAllOccupiedTracks()) {

    com.anylogic.engine.markup.RailwayTrack track =
        (com.anylogic.engine.markup.RailwayTrack) t;

    if (track == linha) {
        return true;
    }
}

return false;
/*ALCODEEND*/}

Vagao getUltimoVagao()
{/*ALCODESTART::1781654909463*/
for(int i = this.size() - 1; i >= 0; i--) {
    
    if(this.getCar(i) instanceof Vagao) {
        
        return (Vagao) this.getCar(i);
    
    }

}

return null;

/*ALCODEEND*/}

int descarregarVagoes_LoteCompleto()
{/*ALCODESTART::1781892219143*/
for(int i = 0; i < this.size(); i++){
    if(this.getCar(i).getClass() == Vagao.class && ((Vagao)getCar(i)).carregado){
          ((Vagao)getCar(i)).descarregarGrao();
    }
}
/*ALCODEEND*/}

Vagao getPrimeiroVagao()
{/*ALCODESTART::1782846452674*/
if (this.size() == 0) {
    return null;
}

for (int i = 0; i < this.size(); i++) {

    Object car = this.getCar(i);

    if (car != null && car instanceof Vagao) {
        return (Vagao) car;
    }
}

return null;
/*ALCODEEND*/}

String getAssociada()
{/*ALCODESTART::1786391824975*/
return getPrimeiroVagao().ferrovia;
/*ALCODEEND*/}

String getNameDestinoMoega()
{/*ALCODESTART::1789386771033*/
return ITerminalMoega_destino.getName();
/*ALCODEEND*/}

boolean destinoMoegaEh(String... nomes)
{/*ALCODESTART::1789389224726*/
return List.of(nomes).contains(getNameDestinoMoega());
/*ALCODEEND*/}

String getFerrovia()
{/*ALCODESTART::1790621845211*/
Agent primeiro = getFirst();
if (primeiro instanceof Vagao) {
    return ((Vagao) primeiro).ferrovia;
}
return "";
/*ALCODEEND*/}

String getSegmento()
{/*ALCODESTART::1790621848828*/
Agent primeiro = getFirst();
if (primeiro instanceof Vagao) {
    return ((Vagao) primeiro).segmento;
}
return "";
/*ALCODEEND*/}

int descarregarVagoes_LoteParcial(int qtd,boolean doInicio)
{/*ALCODESTART::1790858621323*/
int descarregados = 0;
int n = this.size();

for (int k = 0; k < n && descarregados < qtd; k++) {
    int i = doInicio ? k : n - 1 - k;
    Agent carro = getCar(i);

    if (carro instanceof Vagao && ((Vagao) carro).carregado) {
        ((Vagao) carro).descarregarGrao();
        descarregados++;
    }
}
/*ALCODEEND*/}

boolean esta_totalmente_descarregado()
{/*ALCODESTART::1790858980986*/
for (int i = 0; i < this.size(); i++) {
    Agent carro = getCar(i);
    if (carro instanceof Vagao && ((Vagao) carro).carregado)
        return false;
}
return true;
/*ALCODEEND*/}

int fracionar(int qtd,boolean ateFimOuLoco,boolean locoNaCabeca,boolean doInicio)
{/*ALCODESTART::1790865877787*/
int n = this.size();
int inicio = 0;

// Locos na cabeça (início ou fim, conforme a direção)
if (locoNaCabeca) {
    while (inicio < n && getCar(doInicio ? inicio : n - 1 - inicio) instanceof Locomotiva)
        inicio++;

}

// Limite: fim do trem ou próxima locomotiva
int limite = n;
if (ateFimOuLoco) {
    for (int k = inicio; k < n; k++) {
        if (getCar(doInicio ? k : n - 1 - k) instanceof Locomotiva) {
            limite = k;

            break;
        }
    }

}

int resultado = Math.min(inicio + qtd, limite);

// TrainDecouple conta sempre a partir do início do trem
if (!doInicio)
    resultado = n - resultado;

return resultado;
/*ALCODEEND*/}

boolean temLocomotiva(boolean naCabeca)
{/*ALCODESTART::1790870054848*/
if (this.size() == 0) return false;
return (naCabeca ? getFirst() : getLast()) instanceof Locomotiva;
/*ALCODEEND*/}

