public interface ITerminalMoega
{
	boolean podeReceberNovoLote();
	float getOverloadVazios();
	
	String getName();
	String getPatio();
	String getSegmento();
	String getFerrovia();
	
	//Alívio
	void takeAlivio(Trem trem);
	int getNumeroLocomotivasAlivio();
	PositionOnTrack get_pos_alivio();
	
	//gestor de alivios
	ArrayList<Faixa> getPrimeiroAlivioFaixas();
	ArrayList<Faixa> get_primeiras_linhas_de_circulacao_vazios();
	void 			 set_quantidade_alivio(int qt_quantidade);
	int  			 get_quantidade_vazios();
}