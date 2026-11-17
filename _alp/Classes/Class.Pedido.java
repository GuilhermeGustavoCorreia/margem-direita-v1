/**
 * ALT-084: pedido de chegada de um trem, criado pelo GestorDeChegadas
 * a partir de um FluxoChegada (tempo_entre_chegadas.csv).
 */
public class Pedido implements Serializable {

    public Pedido() {
    }

    public int              numero;
    public ITerminalMoega   terminal;
    public String           instancia;
    public String           ferrovia;          // "rumo" | "mrs" | "vli"
    public String           segmento;          // "grao" | "acucar" | ...; null = segmento padrão do terminal
    public com.anylogic.libraries.rail.TrainSource fonte;   // tremTeste_<ferrovia> do terminal
    public double           hora_pedido_h  = -1;
    public double           hora_entrada_h = -1;
    public String           destino;           // "circulacao" | "isn"
    public Trem             trem;
    public boolean          manual = false;     // criado pelo botão do terminal
    public String           ultimo_motivo;      // último motivo de espera registrado no log

    public double espera_h() {
        return hora_entrada_h < 0 ? -1 : hora_entrada_h - hora_pedido_h;
    }

    @Override
    public String toString() {
        return "P" + numero + " " + instancia + "/" + ferrovia;
    }

    private static final long serialVersionUID = 1L;
}
