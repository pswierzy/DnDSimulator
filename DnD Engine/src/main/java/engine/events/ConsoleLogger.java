package engine.events;

public class ConsoleLogger implements ICombatLogger {
    @Override
    public void log(ICombatEvent e) {
        System.out.println(e.getMessage());
    }
}
