package Events;

public interface GameEventListener {
    void onEvent(GameEvent event, Object data);
}
