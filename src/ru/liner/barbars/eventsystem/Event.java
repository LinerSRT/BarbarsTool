package ru.liner.barbars.eventsystem;

import ru.liner.barbars.network.BarbarsConnection;
import ru.liner.barbars.network.Connection;

public interface Event {
    void process(BarbarsConnection connection);
    boolean isProcessing();
    EventType type();
}
