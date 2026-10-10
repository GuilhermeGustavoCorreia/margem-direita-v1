
public class Alivio implements Serializable {


    public Alivio() {
    	
    	iTermalMoegas_aliviar = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Alivio\n");
        sb.append("  qtd locos: ").append(quantidade_locomotivas).append("\n");
        sb.append("  pos criar loco: ").append(pos_criar_locomotiva == null ? "null" : pos_criar_locomotiva.getName()).append("\n");
        sb.append("  patio: ").append(patio).append("\n");
        sb.append("  ferrovia: ").append(ferrovia).append("\n");
        sb.append("  segmento: ").append(segmento).append("\n");
        sb.append("  moegas (").append(iTermalMoegas_aliviar == null ? 0 : iTermalMoegas_aliviar.size()).append("):\n");
        if (iTermalMoegas_aliviar != null)
            for (ITerminalMoega m : iTermalMoegas_aliviar)
            	sb.append("    - ").append(((Agent) m).getName()).append("\n");
        return sb.toString();
    }

	// Variaveis
	public int 							quantidade_locomotivas;
	public PositionOnTrack 				pos_criar_locomotiva;
	public ArrayList<ITerminalMoega> 	iTermalMoegas_aliviar;
	public String 						patio;
	public String 						ferrovia ;
	public String 						segmento;
	
	public ArrayList<Faixa> getFaixas() {
		if (iTermalMoegas_aliviar.isEmpty()) return null;
		return iTermalMoegas_aliviar.get(0).getPrimeiroAlivioFaixas();
	}
	
	public String getNomesMoegas() {

	    if (finalizou_moegas()) return "Nenhuma moega pendente";

	    StringBuilder sb = new StringBuilder();
	    for (ITerminalMoega m : iTermalMoegas_aliviar) {
	        if (sb.length() > 0) sb.append("\n");
	        sb.append(((Agent) m).getName());
	    }
	    return sb.toString();
	}
	public ResourcePool[][] getConjuntoRecursos_faixas() {
	    
		List<ResourcePool> recursos = new ArrayList<ResourcePool>();
	    
		for (Faixa f : getFaixas()) {
	        
			if (f.recurso != null && !recursos.contains(f.recurso))
	            recursos.add(f.recurso);
	    }

	    if (recursos.isEmpty())
	        throw new IllegalStateException("Alivio.getConjuntoRecursos_faixas(): nenhum recurso nas faixas");

	    return new ResourcePool[][] { recursos.toArray(new ResourcePool[0]) };
	}
	
	public ITerminalMoega get_moega_atual() {

	    if (finalizou_moegas())
	        throw new IllegalStateException("Alivio.get_moega_atual(): lista de moegas vazia");

	    return iTermalMoegas_aliviar.get(0);
	}

	public void atualizar_moegas() {

	    if (!finalizou_moegas())
	        iTermalMoegas_aliviar.remove(0);
	}

	public boolean finalizou_moegas() {

	    return iTermalMoegas_aliviar == null || iTermalMoegas_aliviar.isEmpty();
	}
	
	
	private static final long serialVersionUID = 1L;

}