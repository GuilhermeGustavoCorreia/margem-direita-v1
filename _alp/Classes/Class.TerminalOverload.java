/**
 * TerminalOverload
 */	
public class TerminalOverload implements Serializable {

	public ITerminalMoega terminal;
	public float overload;
	
    /**
     * Construtor padrão
     */
    public TerminalOverload() {
    }
    
	public TerminalOverload(ITerminalMoega terminal, float overload) {
		
		this.terminal = terminal;
		this.overload = overload;
		
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * Este número está aqui para o propósito de armazenagem do snapshot<br>
	 * Precisa ser alterado quando a classe é alterada
	 */ 
	private static final long serialVersionUID = 1L;

}