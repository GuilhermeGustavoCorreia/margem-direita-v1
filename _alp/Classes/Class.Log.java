/**
 * Log padronizado do modelo (ALT-087).
 * Formato: [00:19 - 01/01] INFO  Origem          evento                       mensagem
 *   - tempo em hh:mm - dd/mm, contado a partir de 00:00 de data_inicio (modelo roda em segundos)
 *   - nivel: DEBUG | INFO | WARN | ERRO
 *   - saida por System.out, que o Main espelha em logs/sim.log
 * Uso: Log.info("ADM", "decidir_moega", "trem=1 carros=85");
 * Antes do primeiro uso, cada replicacao chama Log.iniciar(this) em um agente.
 */
public class Log {

    public enum Nivel { DEBUG, INFO, WARN, ERRO }

    // DEBUG some quando nivelMinimo = INFO. Mude no startup para depurar.
    public static Nivel nivelMinimo = Nivel.INFO;

    // Origens silenciadas (so DEBUG/INFO; WARN e ERRO sempre saem).
    public static java.util.Set<String> silenciados = new java.util.HashSet<String>();

    // dia do tempo 0 da simulação (00:00). O Main preenche a partir do simulacao.csv (coluna data_inicio).
    public static java.time.LocalDate data_inicio = java.time.LocalDate.of(java.time.LocalDate.now().getYear(), 1, 1);
    private static final java.time.format.DateTimeFormatter FMT_TEMPO = java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM");

    private static com.anylogic.engine.Engine engine = null;

    public static void iniciar(com.anylogic.engine.Agent agente) {
        engine = agente.getEngine();
    }

    public static void debug(String origem, String evento, String msg) { log(Nivel.DEBUG, origem, evento, msg); }
    public static void info (String origem, String evento, String msg) { log(Nivel.INFO,  origem, evento, msg); }
    public static void warn (String origem, String evento, String msg) { log(Nivel.WARN,  origem, evento, msg); }
    public static void erro (String origem, String evento, String msg) { log(Nivel.ERRO,  origem, evento, msg); }

    private static void log(Nivel nivel, String origem, String evento, String msg) {
        if (nivel.ordinal() < nivelMinimo.ordinal()) return;
        evento = evento.trim().replace(' ', '_');
        if (nivel.ordinal() < Nivel.WARN.ordinal() && silenciados.contains(origem)) return;

        String tempo = (engine == null)
            ? "--:-- - --/--"
            : data_inicio.atStartOfDay().plusSeconds((long) engine.time()).format(FMT_TEMPO);

        System.out.println(String.format(java.util.Locale.ROOT,
            "[%s] %-5s %-15s %-28s %s", tempo, nivel, origem, evento, msg));
    }

    // Posição do trem: c0 = frente do carro 0, cN = traseira do último carro (track/offset em m).
    public static String pos(com.anylogic.engine.Agent a) {
        return (a instanceof Trem) ? pos((Trem) a) : "-";
    }

    public static String pos(Trem t) {
        if (t == null || t.size() == 0) return "-";
        try {
            com.anylogic.engine.Agent a = t.getCar(0);
            com.anylogic.engine.Agent b = t.getCar(t.size() - 1);
            return String.format(java.util.Locale.ROOT, "c0=%s/%.0f cN=%s/%.0f",
                nome_track(track_de(a, true)), offset_de(a, true), nome_track(track_de(b, false)), offset_de(b, false));
        } catch (Exception e) {
            return "fora_da_rede";
        }
    }

    // Outros trens nas tracks das duas pontas de t.
    public static String trens_vizinhos(com.anylogic.engine.Agent a) {
        return (a instanceof Trem) ? trens_vizinhos((Trem) a) : "-";
    }

    public static String trens_vizinhos(Trem t) {
        if (t == null || t.size() == 0) return "-";
        java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<String>();
        try {
            com.anylogic.engine.Agent a = t.getCar(0);
            com.anylogic.engine.Agent b = t.getCar(t.size() - 1);
            for (com.anylogic.engine.markup.RailwayTrack tr : new com.anylogic.engine.markup.RailwayTrack[] { track_de(a, true), track_de(b, false) }) {
                if (tr == null) continue;
                for (Object o : tr.getCars()) {
                    com.anylogic.engine.Agent outro = trem_de((com.anylogic.engine.Agent) o);
                    if (outro != null && outro != t) ids.add(outro.getId() + "@" + tr.getName());
                }
            }
        } catch (Exception e) {
            return "erro";
        }
        return ids.isEmpty() ? "nenhum" : String.join(",", ids);
    }

    private static String nome_track(com.anylogic.engine.markup.RailwayTrack tr) {
        return tr == null ? "-" : tr.getName();
    }

    // getTrack/getOffset/getTrain só existem nos tipos de carro (Vagao e Locomotiva), não no Agent.
    private static com.anylogic.engine.markup.RailwayTrack track_de(com.anylogic.engine.Agent c, boolean frente) {
        if (c instanceof Vagao)      return ((Vagao) c).getTrack(frente);
        if (c instanceof Locomotiva) return ((Locomotiva) c).getTrack(frente);
        return null;
    }

    private static double offset_de(com.anylogic.engine.Agent c, boolean frente) {
        if (c instanceof Vagao)      return ((Vagao) c).getOffset(frente);
        if (c instanceof Locomotiva) return ((Locomotiva) c).getOffset(frente);
        return -1;
    }

    private static com.anylogic.engine.Agent trem_de(com.anylogic.engine.Agent c) {
        if (c instanceof Vagao)      return ((Vagao) c).getTrain();
        if (c instanceof Locomotiva) return ((Locomotiva) c).getTrain();
        return null;
    }
}
