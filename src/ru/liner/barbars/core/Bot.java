package ru.liner.barbars.core;

import ru.liner.barbars.core.events.LoginEvent;
import ru.liner.barbars.core.events.ShadowAnaliseBagEvent;
import ru.liner.barbars.core.events.ShadowLoginEvent;
import ru.liner.barbars.core.events.ShadowUserInfoEvent;
import ru.liner.barbars.eventsystem.EventType;
import ru.liner.barbars.eventsystem.NetworkEventHandler;
import ru.liner.barbars.network.Connection;
import ru.liner.barbars.utils.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Bot implements IBot {
    private final NetworkEventHandler gameHandler;
    private final NetworkEventHandler shadowHandler;

    public Bot(String userName, String password) {
        Connection.disableSslVerification();
        this.gameHandler = new NetworkEventHandler();
        this.shadowHandler = new NetworkEventHandler();
        this.gameHandler.addEventListener(new LoginEvent(gameHandler, userName, password));
        this.shadowHandler.addEventListener(new ShadowLoginEvent(shadowHandler, userName, password));
        this.shadowHandler.addEventListener(new ShadowUserInfoEvent(shadowHandler));
        this.shadowHandler.addEventListener(new ShadowAnaliseBagEvent(shadowHandler));
        this.gameHandler.start();
        this.shadowHandler.start();
        fireShadowEvent(EventType.SHADOW_LOGIN);
        fireGlobalEvent(EventType.LOGIN);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ShadowLoginEvent shadowLoginEvent = shadowHandler.getEventListener(EventType.SHADOW_LOGIN);
                if (!shadowLoginEvent.isProcessing() && shadowLoginEvent.getUserId() != -1)
                    fireShadowEvent(EventType.SHADOW_USER_INFO);
            }
        }, 0, TimeUnit.SECONDS.toMillis(5));
    }

    @Override
    public NetworkEventHandler getGameHandler() {
        return gameHandler;
    }

    @Override
    public NetworkEventHandler getShadowHandler() {
        return shadowHandler;
    }

    @Override
    public void fireGlobalEvent(EventType eventType) {
        if (eventType.name().contains("SHADOW")) {
            System.out.println(Logger.composeLogout("Cannot fire shadow event to game handler! Skipping"));
            return;
        }
        gameHandler.fireEvent(eventType);
    }

    @Override
    public void fireShadowEvent(EventType eventType) {
        if (!eventType.name().contains("SHADOW")) {
            System.out.println(Logger.composeLogout("Cannot fire global event to shadow handler! Skipping"));
            return;
        }
        shadowHandler.fireEvent(eventType);
    }
}
