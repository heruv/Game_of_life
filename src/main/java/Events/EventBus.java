package Events;

import java.util.HashSet;
import java.util.Set;

public class EventBus {
    private final Set<GameEventListener> listeners;

    public EventBus() {
        this.listeners = new HashSet<>();
    }

    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    public void unSubscribe(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(GameEvent event, Object data) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event, data);
        }
    }

    public void publish(GameEvent event) {
        publish(event, null);
    }
}
