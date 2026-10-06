package engine.events;

public class SimpleEvent implements ICombatEvent {
    private final String message;

    public SimpleEvent(String message) {
        this.message = message;
    }


    @Override
    public String getMessage() {
        return message;
    }
}
