
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
        sb.append("  tipo loco: ").append(tipo_loco).append("\n");
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
	public String 						tipo_loco;      // "manobra" ou "ferrovia"
	public Faixa 						faixa_criacao;  // faixa onde a loco de alívio é criada/injetada
	
	public ArrayList<Faixa> getFaixas() {
		if (iTermalMoegas_aliviar.isEmpty()) return null;
		return iTermalMoegas_aliviar.get(0).get_faixas_primeiro_alivio();
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
		ArrayList<Faixa> faixas = getFaixas();
		
		if (faixas == null)
			throw new IllegalStateException("Alivio.getConjuntoRecursos_faixas(): terminal sem faixas de primeiro alivio");
	    
		for (Faixa f : faixas) {
			
			if (f == null)
				throw new IllegalStateException("Alivio.getConjuntoRecursos_faixas(): faixa null em get_faixas_primeiro_alivio()");
			
			// a faixa de criação da loco já é apreendida no seize anterior
			if (faixa_criacao != null && f.recurso == faixa_criacao.recurso) continue;
	        
			if (f.recurso != null && !recursos.contains(f.recurso))
	            recursos.add(f.recurso);
	    }

	    return new ResourcePool[][] { recursos.toArray(new ResourcePool[0]) };
	}
	
	public ResourcePool[][] getConjuntoRecursos_faixa_criacao() {
		
		if (faixa_criacao == null || faixa_criacao.recurso == null)
			throw new IllegalStateException("Alivio.getConjuntoRecursos_faixa_criacao(): nenhuma faixa contem a posicao de criacao da loco ("
				+ (pos_criar_locomotiva == null ? "null" : pos_criar_locomotiva.getName()) + ")");
		
		List<ResourcePool> recursos = new ArrayList<ResourcePool>();
		recursos.add(faixa_criacao.recurso);
		
		// faixas de circulação em que o trem pode entrar durante o alívio (ex.: via1_intermar_c no
		// avanço do CoperXI): apreendidas junto com a faixa de criação, no mesmo conjunto
		for (Faixa f : get_faixas_saida())
			if (!recursos.contains(f.recurso)) recursos.add(f.recurso);
		
		return new ResourcePool[][] { recursos.toArray(new ResourcePool[0]) };
	}
	
	// faixas de get_primeiras_linhas_de_circulacao_vazios() do terminal, sem a faixa de criação
	// e sem faixas sem recurso. Lista vazia quando o terminal devolve null.
	public ArrayList<Faixa> get_faixas_saida() {
		
		ArrayList<Faixa> saida = new ArrayList<Faixa>();
		if (finalizou_moegas()) return saida;
		
		ArrayList<Faixa> faixas = get_moega_atual().get_primeiras_linhas_de_circulacao_vazios();
		if (faixas == null) return saida;
		
		for (Faixa f : faixas) {
			if (f == null || f.recurso == null) continue;
			if (faixa_criacao != null && f.recurso == faixa_criacao.recurso) continue;
			if (!saida.contains(f)) saida.add(f);
		}
		return saida;
	}
	
	// no retorno do alívio: devolve as faixas que o trem ainda possui mas onde ele não está mais.
	// As faixas debaixo do trem continuam com ele e são liberadas pela circulação
	// (liberar_faixas_ocupadas) quando ele sair delas.
	public ResourcePool[] liberar_faixas_fora_do_trem(Trem trem, List<Faixa> faixas) {
		
		List<ResourcePool> liberar = new ArrayList<ResourcePool>();
		
		for (Faixa f : faixas) {
			
			if (f == null || f.track == null) continue;
			if (f.track.getTrains().contains(trem)) continue;
			// a faixa de criação fica com o trem: ele passa por ela na volta
			// e a circulação libera quando ele sair (liberar_faixas_ocupadas).
			// Tgrao sai pela marginal_c e não passa por ela.
			if (f == faixa_criacao && !"tgrao".equals(trem.referencia_alivio)) continue;
			
			boolean temUnidade = (f.recurso != null && !trem.resourceUnitsOfPool(f.recurso).isEmpty());
			boolean marcadaParaOTrem = (f.idTremLicenciado == trem.getId());
			
			if (temUnidade && !liberar.contains(f.recurso)) liberar.add(f.recurso);
			if (temUnidade || marcadaParaOTrem) {
				f.liberar();
				Log.info("GesAlivios", "retorno_alivio", String.format("(%s - %d) liberou %s (fora do trem)",
					trem.prefixo, trem.getId(), f.recurso == null ? f.track.getName() : f.recurso.getName()));
			}
		}
		
		return liberar.toArray(new ResourcePool[0]);
	}
	
	// rota da loco = faixa de criação + faixas do primeiro alívio + track do destino.
	// usada no TrainMoveTo com "A given list of tracks": a loco só anda pelo caminho licenciado
	// e vira os AMVs conforme a lista.
	public RailwayTrack[] getRotaLicenciada() {
		
		List<RailwayTrack> rota = new ArrayList<RailwayTrack>();
		
		if (faixa_criacao != null) rota.add(faixa_criacao.track);
		
		ArrayList<Faixa> faixas = getFaixas();
		if (faixas != null)
			for (Faixa f : faixas)
				if (f != null && f.track != null && !rota.contains(f.track)) rota.add(f.track);
		
		RailwayTrack destino = get_moega_atual().get_pos_alivio().getTrack();
		if (!rota.contains(destino)) rota.add(destino);
		
		return rota.toArray(new RailwayTrack[0]);
	}
	
	public boolean ehLocoManobra() {
		return "manobra".equals(tipo_loco);
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