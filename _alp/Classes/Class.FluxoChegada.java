public class FluxoChegada implements Serializable {

    public FluxoChegada() {
    }

    public ITerminalMoega   terminal;
    public String           instancia;
    public String           ferrovia;          // "rumo" | "mrs" | "vli"
    public com.anylogic.libraries.rail.TrainSource fonte;
    public double           intervalo_h;
    public double           proxima_h;
    public int              gerados;

    private static final long serialVersionUID = 1L;
}
