void ev_relogio()
{/*ALCODESTART::1795840000115*/
gerar_pedidos();
despachar();
// último tique antes do fim da rodada: grava os pedidos que não viraram trem
if (time(HOUR) + 1.0 / 60 >= main().duracao_h - 1e-6) registrar_fila_csv();
/*ALCODEEND*/}

