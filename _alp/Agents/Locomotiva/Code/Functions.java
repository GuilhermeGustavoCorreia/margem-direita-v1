int contarVagoesNoTrem()
{/*ALCODESTART::1782083470441*/
Trem train = (Trem) getTrain();
if (train == null) return 1;
int n = 0;
try {
    while (train.getCar(n) != null) {
        n++;
    }
} catch (Exception e) {}
return n;
/*ALCODEEND*/}

