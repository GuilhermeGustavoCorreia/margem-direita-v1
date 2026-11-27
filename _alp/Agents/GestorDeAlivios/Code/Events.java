void atualizaEstados()
{/*ALCODESTART::1786124811763*/
analisar_lotesVazios();
/*ALCODEEND*/}

void atualiza_funcoes()
{/*ALCODESTART::1786378223770*/
atualiza_terminaisEmOverload();
fazerUmAlivio();
/*ALCODEEND*/}

void atualizar_analises()
{/*ALCODESTART::1791469801983*/
analisar_pst();
analisar_pmc();
analisar_pcx();
// 2º lote do ADM: promove quando o terminal libera (descarga e alívio)
main().adm.tentar_promover_segundo_lote();
// T39: vagão parado por falta de espaço nas linhas de vazios
main().t39.tentar_liberar_espaco_vazios();
// Moegões: próximo lote só depois que o trem do alívio sai do terminal
main().moegaoMar.tentar_iniciar_proximo_lote();
main().moegaoTerra.tentar_iniciar_proximo_lote();
main().t39.tentar_liberar_espaco_corte();
// diagnóstico: alívio do PCX esperando acesso/faixas
diag_alivio_pcx();
// log de análise dos terminais do PCX a cada 5 min (cada terminal tem o seu log_de_analise)
if (t_ultimo_log_estado_terminais < 0 || time() - t_ultimo_log_estado_terminais >= 300) {
    t_ultimo_log_estado_terminais = time();
    main().adm.log_estado();
    main().tes.log_estado();
    main().t39.log_estado();
}
/*ALCODEEND*/}

