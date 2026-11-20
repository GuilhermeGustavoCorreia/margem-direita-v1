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
}
