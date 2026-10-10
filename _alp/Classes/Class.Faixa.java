public class Faixa implements Serializable {

    public Faixa() {
    }

    // Enum de estado
    public enum EstadoFaixa {
        LIVRE,
        OCUPADA,
        LICENCIADA,
        OCUPADA_BLOQUEADA // <- esta so se desbloquear via codigo. atualizar a linha nao libera neste estado.
    }

    // Variaveis
    public int 				ordem;
    public ResourcePool 	recurso;
    public RailwayTrack 	track;
    public EstadoFaixa 		estado;
    public Enter 			enter;
    public int				idTremLicenciado;
    public ShapePolyLine 	linhaTMDS;		
    //funções
    public boolean estaLicenciadaParaEsteTrem(int idTrem) {
    	
    	boolean estadoLicenciado 	= (estado == EstadoFaixa.LICENCIADA || estado == EstadoFaixa.OCUPADA_BLOQUEADA);
	    boolean mesmoTrem 			= (idTremLicenciado == idTrem);
	    
	    if (estadoLicenciado && mesmoTrem) {
	        return true;
	    } else {
	        return false;
	    }
    }
    
    public String getStatusTexto() {
        String s = recurso.getName() + ": " + estado;
        if (idTremLicenciado != 0)   // ajuste se o "sem trem" for -1 ou outro valor
            s += " | trem " + idTremLicenciado;
        return s;
    }
    
    public boolean faixaEstaVazia() {
        return track.getTrains().isEmpty();
    }
    
    public void liberar() {
    	 idTremLicenciado 	= 0;
         estado 			= EstadoFaixa.LIVRE;
         linhaTMDS.setLineColor(Color.WHITE);
    }
    
    public void licenciar(Trem trem) {
   	 	idTremLicenciado 	= trem.getId();
        estado 				= EstadoFaixa.LICENCIADA;
        linhaTMDS.setLineColor(Color.GREEN);
    }
    
    public void ocupar(Trem trem) {
   	 	idTremLicenciado 	= trem.getId();
        estado 				= EstadoFaixa.OCUPADA;
        linhaTMDS.setLineColor(Color.RED);
    }
    
    public void bloquear(Trem trem) {
    	idTremLicenciado 	= (trem != null) ? trem.getId() : 999999;
        estado 				= EstadoFaixa.OCUPADA_BLOQUEADA;
        linhaTMDS.setLineColor(Color.MAGENTA);
    }
    
    public void ocupar_agente(int id_agente) {
   	 	idTremLicenciado 	= id_agente;
        estado 				= EstadoFaixa.OCUPADA;
        linhaTMDS.setLineColor(Color.RED);
    }
    
    @Override
    public String toString() {
        return super.toString();
    }

    private static final long serialVersionUID = 1L;

}