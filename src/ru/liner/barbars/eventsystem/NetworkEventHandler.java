package ru.liner.barbars.eventsystem;

import ru.liner.barbars.network.BarbarsConnection;
import ru.liner.barbars.utils.Logger;
import ru.liner.barbars.utils.Worker;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class NetworkEventHandler extends Worker {
    private final BarbarsConnection connection;
    private final List<Event> eventListeners;
    private final List<EventType> eventFire;
    private Event executingEvent;

    public NetworkEventHandler() {
        this.connection = new BarbarsConnection();
        this.eventListeners = new ArrayList<>();
        this.eventFire = new ArrayList<>();
    }

    public void addEventListener(Event event) {
        eventListeners.add(event);
    }

    public <T extends Event> T getEventListener(EventType eventType) {
        for (Event event : eventListeners)
            if (event.type() == eventType)
                return (T) event;
        return null;
    }

    public void fireEvent(EventType eventType) {
        if (executingEvent == null || executingEvent.type() != eventType) {
            eventFire.add(eventType);
        }
    }

    public BarbarsConnection getConnection() {
        return connection;
    }

    @Override
    public void process() {
        if (!eventFire.isEmpty())
            for (int i = 0; i < eventFire.size(); i++) {
                EventType eventType = eventFire.get(i);
                for (Event event : eventListeners)
                    if (event.type() == eventType) {
                        executingEvent = event;
                        event.process(connection);
                        while (event.isProcessing())
                            sleep(16);
                        eventFire.remove(eventType);
                        executingEvent = null;
                        break;
                    }
            }
    }

    @Override
    public long delay() {
        return 16;
    }
}
