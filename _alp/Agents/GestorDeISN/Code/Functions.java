boolean pode_estacionar()
{/*ALCODESTART::1793876837451*/
// a linha 02 está livre quando não há trem reservado nela
return lote_linha02 == null;
/*ALCODEEND*/}

double verificar_saida()
{/*ALCODESTART::1795840000218*/
// chamada a cada minuto pelo evento ev_verificar_saida
// 1) trem já liberado: a vaga só é solta quando ele deixa todos os trechos da linha 02
if (saindo_linha02 != null) {
	if (!esta_na_linha02(saindo_linha02)) {
		Log.info("ISN", "saiu_linha02", String.format("trem=%d/%s saiu da linha 02: vaga livre", saindo_linha02.getId(), ((Trem) saindo_linha02).prefixo));
		saindo_linha02 = null;
		lote_linha02   = null;
	}
	return;
}
// 2) trem parado cujo terminal já pode receber e sem outro trem a caminho dele: libera
if (!parado_linha02 || lote_linha02 == null) return;

ITerminalMoega destino = lote_linha02.ITerminalMoega_destino;
boolean trem_a_caminho = false;
for (Trem t : main().gestorDeCirculacao.trens_em_circulacao) {
	if (t.ITerminalMoega_destino == destino) { trem_a_caminho = true; break; }
}
// a Chegadas pode ter acabado de despachar um trem direto para o mesmo terminal
Pedido p = main().gestorDeChegadas.pedido_atual;
if (p != null && "circulacao".equals(p.destino) && p.terminal == destino) trem_a_caminho = true;

if (destino.podeReceberNovoLote() && !trem_a_caminho) {
	main().gestorDeCirculacao.trens_em_circulacao.add(lote_linha02);
	Log.info("ISN", "liberado_linha02", String.format("trem=%d/%s liberado: %s pode receber", lote_linha02.getId(), ((Trem) lote_linha02).prefixo, ((Agent) lote_linha02.ITerminalMoega_destino).getName()));
	parado_linha02 = false;
	saindo_linha02 = lote_linha02;
	espera_linha02.free(lote_linha02);   // segue pelo sair_linha02 -> faixa da porteira
}
/*ALCODEEND*/}

boolean esta_na_linha02(Trem t)
{/*ALCODESTART::1795840000219*/
// o trem ainda ocupa algum trecho isn_linha02_a..i?
RailwayTrack[] trechos = { main().isn_linha02_a, main().isn_linha02_b, main().isn_linha02_c,
	main().isn_linha02_d, main().isn_linha02_e, main().isn_linha02_f,
	main().isn_linha02_g, main().isn_linha02_h, main().isn_linha02_i };
for (RailwayTrack tr : trechos)
	if (tr.getTrains().contains(t)) return true;
return false;
/*ALCODEEND*/}

double seguir_para_faixa(Trem trem,Faixa atual,Faixa proxima,String bloco)
{/*ALCODESTART::1795850000201*/
// define o par de faixas do trem e entrega ao mecanismo de licença do GestorDeCirculacao
// (tentar_usar_faixa licencia atual e próxima, libera as faixas já deixadas e chama atual.enter)
trem.faixa_atual   = atual;
trem.proxima_faixa = proxima;
Log.info("ISN", bloco, String.format("trem=%d/%s faixa_atual=%s proxima_faixa=%s", trem.getId(), ((Trem) trem).prefixo,
	atual   == null ? "null" : atual.recurso.getName(),
	proxima == null ? "null" : proxima.recurso.getName()));
main().gestorDeCirculacao.tentar_usar_faixa.take(trem);
/*ALCODEEND*/}

double registrar_faixas()
{/*ALCODESTART::1795850000202*/
Log.iniciar(this);
// as faixas do ISN entram na lista do GestorDeCirculacao para que
// liberar_faixas_ocupadas() as libere quando o trem sair delas
List<Faixa> faixas_isn = java.util.List.of(faixa_isn_a, faixa_isn_b, faixa_isn_c, faixa_isn_d, faixa_isn_e, faixa_isn_porteira, faixa_isn_linha02_e);
for (Faixa f : faixas_isn)
	if (!main().gestorDeCirculacao.faixas.contains(f)) main().gestorDeCirculacao.faixas.add(f);
Log.info("ISN", "registrar_faixas", String.format("%d faixas registradas no GestorDeCirculacao", faixas_isn.size()));
/*ALCODEEND*/}

Main main()
{/*ALCODESTART::1793905848556*/
// acesso ao Main (mesmo padrão dos outros agentes)
return (Main) getEngine().getRoot();
/*ALCODEEND*/}

