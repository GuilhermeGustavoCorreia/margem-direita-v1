int __verificaSeTremMenorQueLote(Trem trem)
{/*ALCODESTART::1785765587624*/
if (trem.size() < numberOfCars){

	return trem.size();

}else{

	return numberOfCars;

}
/*ALCODEEND*/}

int __considerarLocomotivas(int tamanhoLote,boolean frente,Trem trem)
{/*ALCODESTART::1785767661800*/
int nLocos = 0;
int n = Math.min(tamanhoLote, trem.size());

for (int i = 0; i < n; i++) {
    
    int idx = frente ? i : trem.size() - 1 - i;

    if (trem.getCar(idx) instanceof Locomotiva) {
        nLocos++;
    }
    
}

if (nLocos > 0) {

    return tamanhoLote + nLocos;
    
}

return tamanhoLote;
/*ALCODEEND*/}

int _getNumeroDeVagoes(Trem trem)
{/*ALCODESTART::1785767887902*/
int numVgs = __verificaSeTremMenorQueLote(trem);

if (considerarSomenteVagoes){

	return __considerarLocomotivas(numVgs, fistCars, trem);									

}else{

	return numVgs;

}
/*ALCODEEND*/}

