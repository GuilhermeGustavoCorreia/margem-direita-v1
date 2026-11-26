void verificar_saida_alivio()
{/*ALCODESTART::1797219000002*/
// o alívio só termina para o terminal quando o trem dele sai da psnSaidaCopexXXI_b
if (trem_alivio_na_linha == null) return;
if (main().psnSaidaCopexXXI_b.getTrains().contains(trem_alivio_na_linha)) return;
Log.info("CoperXXI", "alivio_saiu_da_linha", String.format("trem=%d saiu da psnSaidaCopexXXI_b", trem_alivio_na_linha.getId()));
trem_alivio_na_linha = null;
finalizar_alivio();
/*ALCODEEND*/}

