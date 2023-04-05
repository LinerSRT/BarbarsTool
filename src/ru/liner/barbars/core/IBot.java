package ru.liner.barbars.core;

import ru.liner.barbars.eventsystem.Event;
import ru.liner.barbars.eventsystem.EventType;
import ru.liner.barbars.eventsystem.NetworkEventHandler;

public interface IBot {
    NetworkEventHandler getGameHandler();
    NetworkEventHandler getShadowHandler();
    void fireGlobalEvent(EventType event);
    void fireShadowEvent(EventType event);
}
