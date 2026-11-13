/**
 * Log padronizado do modelo (ALT-087).
 * Formato: [   0.320h] INFO  Origem          evento                       mensagem
 *   - tempo sempre em horas (modelo roda em segundos)
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
            ? "      ---"
            : String.format(java.util.Locale.ROOT, "%9.3f", engine.time() / 3600.0);

        System.out.println(String.format(java.util.Locale.ROOT,
            "[%sh] %-5s %-15s %-28s %s", tempo, nivel, origem, evento, msg));
    }
}
