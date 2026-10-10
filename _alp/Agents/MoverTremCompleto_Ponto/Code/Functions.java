boolean _jaEstaNoDestino(Trem trem,PositionOnTrack destino)
{/*ALCODESTART::1784749899348*/
RailwayTrack trackDestino = destino.getTrack();
double offsetDestino = destino.getOffset(METER);
double tolerancia = 0.01; // metros

try {

    if (mostrarLogs) traceln("");
    if (mostrarLogs) traceln("==============(_jaEstaNoDestino)===================" );
    if (mostrarLogs) traceln("offsetDestino: \t" + String.format(java.util.Locale.US, "%.2f", offsetDestino));
    if (mostrarLogs) traceln("trackDestino: \t" + trackDestino.getName());
	
	//procura se algum vagão esta sobre a track de destino
    for (int i = 0; i < trem.size(); i++) {

        Agent vagao = trem.getCar(i);

        java.lang.reflect.Method metodoGetTrack  = vagao.getClass().getMethod("getTrack", boolean.class);
        java.lang.reflect.Method metodoGetOffset = vagao.getClass().getMethod("getOffset", boolean.class);

        RailwayTrack trackFrente  = (RailwayTrack) metodoGetTrack.invoke(vagao, true);
        double offsetFrente       = (double) metodoGetOffset.invoke(vagao, true);

        RailwayTrack trackTras    = (RailwayTrack) metodoGetTrack.invoke(vagao, false);
        double offsetTras         = (double) metodoGetOffset.invoke(vagao, false);

        if (mostrarLogs) {
        
            traceln("vagao indice: \t" + i
                + "\ttrackFrente: \t" + trackFrente.getName()
                + "\toffsetFrente: \t" + String.format(java.util.Locale.US, "%.2f", offsetFrente)
                + "\ttrackTras: \t" + trackTras.getName()
                + "\toffsetTras: \t" + String.format(java.util.Locale.US, "%.2f", offsetTras)
            );
                
        }

        // Caso normal: o vagão inteiro está na mesma track do destino.
        if (trackFrente == trackTras) {
            if (trackFrente == trackDestino) {
                double minOffset = Math.min(offsetFrente, offsetTras);
                double maxOffset = Math.max(offsetFrente, offsetTras);

                if (offsetDestino >= minOffset - tolerancia && offsetDestino <= maxOffset + tolerancia) {
                    if (mostrarLogs) traceln("vagao sobre o ponto, indice: \t" + i);
                    if (mostrarLogs) traceln("esta sobre a track: true");
                    return true;
                }
            }
        }
        
        // Caso de borda: o vagão atravessa um switch, frente e traseira em tracks diferentes.
        // O trecho do vagão em cada track vai do offset conhecido até a extremidade
        // da track mais próxima (0 ou comprimento), pois é ali que ela se conecta.
        else {
            if (trackFrente == trackDestino) {
                double comprimentoTrack = trackFrente.length(METER);
                double extremidade = (offsetFrente <= comprimentoTrack - offsetFrente) ? 0 : comprimentoTrack;
                double minOffset = Math.min(offsetFrente, extremidade);
                double maxOffset = Math.max(offsetFrente, extremidade);

                if (offsetDestino >= minOffset - tolerancia && offsetDestino <= maxOffset + tolerancia) {
                    if (mostrarLogs) traceln("vagao sobre o ponto (atravessando, lado frente), indice: \t" + i);
                    if (mostrarLogs) traceln("esta sobre a track: true");
                    return true;
                }
            }
            if (trackTras == trackDestino) {
                double comprimentoTrack = trackTras.length(METER);
                double extremidade = (offsetTras <= comprimentoTrack - offsetTras) ? 0 : comprimentoTrack;
                double minOffset = Math.min(offsetTras, extremidade);
                double maxOffset = Math.max(offsetTras, extremidade);

                if (offsetDestino >= minOffset - tolerancia && offsetDestino <= maxOffset + tolerancia) {
                    if (mostrarLogs) traceln("vagao sobre o ponto (atravessando, lado traseira), indice: \t" + i);
                    if (mostrarLogs) traceln("esta sobre a track: true");
                    return true;
                }
            }
        }
    }
} catch (Exception e) {
    error("Erro ao verificar se algum ponto do trem está sobre o destino: " + e.getMessage());
    return false;
}
if (mostrarLogs) traceln("esta sobre a track: false");
return false;
/*ALCODEEND*/}

double _distanciaResidual(Trem trem,PositionOnTrack ponto,boolean avancar)
{/*ALCODESTART::1784751838873*/
Agent vagaoCabeca = avancar ? trem.getLast() : trem.getFirst();
if (vagaoCabeca == null) {
    return 0;
}

try {
    if (mostrarLogs) traceln("");
    if (mostrarLogs) traceln("==============(_distânciaResidual)===================" );
    RailwayTrack trackPonto = ponto.getTrack();
    double offsetPonto = ponto.getOffset(METER);

    int n = trem.size();

    int idxCabeca = avancar ? (n - 1) : 0;
    if (mostrarLogs) traceln("indice do vagao-cabeca: \t" + idxCabeca);

    int idxMelhor = -1;
    double melhorResiduo = Double.MAX_VALUE;

    for (int i = 0; i < n; i++) {

        // percorre do vagao-cabeca em direcao ao fim do trem
        int idx = avancar ? (n - 1 - i) : i;
        Agent vagao = trem.getCar(idx);

        java.lang.reflect.Method metodoGetTrack  = vagao.getClass().getMethod("getTrack", boolean.class);
        java.lang.reflect.Method metodoGetOffset = vagao.getClass().getMethod("getOffset", boolean.class);

        RailwayTrack trackVagao = (RailwayTrack) metodoGetTrack.invoke(vagao, true);
        double offsetVagao = (double) metodoGetOffset.invoke(vagao, true);

        if (trackVagao == trackPonto) {
            double residuo = Math.abs(offsetVagao - offsetPonto);
            if (residuo < melhorResiduo) {
                melhorResiduo = residuo;
                idxMelhor = i; // guarda a posicao NA ORDEM DE PERCURSO (i), nao o idx real ainda
            }
        }
    }

    if (idxMelhor >= 0) {

        // soma comprimento dos vagoes entre a cabeca e o vagao encontrado
        double somaComprimentos = 0;

        for (int j = 0; j < idxMelhor; j++) {
            int idxAnterior = avancar ? (n - 1 - j) : j;
            somaComprimentos += trem.getCar(idxAnterior).getLength();
        }

        int idxReal = avancar ? (n - 1 - idxMelhor) : idxMelhor;

        if (mostrarLogs) {
            traceln("vagao mais proximo do ponto, indice: \t" + idxReal);
            traceln("residuoOffset: \t" + String.format(java.util.Locale.US, "%.2f", melhorResiduo));
            traceln("somaComprimentos: \t" + String.format(java.util.Locale.US, "%.2f", somaComprimentos));
        }

        return somaComprimentos + melhorResiduo;
    }

    // nenhum vagao esta no track do ponto -> trem ainda nao chegou la
    if (mostrarLogs) traceln("nenhum vagao encontrado no track do ponto");
    return trem.getDistanceToTarget();

} catch (Exception e) {
    error("Erro ao calcular distância residual: " + e.getMessage());
    return 0;
}
/*ALCODEEND*/}

