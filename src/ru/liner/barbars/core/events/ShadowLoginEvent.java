package ru.liner.barbars.core.events;

import ru.liner.barbars.eventsystem.AbstractEvent;
import ru.liner.barbars.eventsystem.EventType;
import ru.liner.barbars.eventsystem.NetworkEventHandler;
import ru.liner.barbars.network.BarbarsConnection;
import ru.liner.barbars.utils.Logger;

import java.io.IOException;

public class ShadowLoginEvent extends AbstractEvent {
    private final String username;
    private final String password;
    private int userId = -1;

    public ShadowLoginEvent(NetworkEventHandler eventHandler, String username, String password) {
        super(eventHandler);
        this.username = username;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public void process(BarbarsConnection connection) {
        isProcessing = true;
        System.out.println(Logger.composeLogout("Starting shadow logging to game"));
        connection.addHeader("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        connection.addHeader("Content-Type: application/x-www-form-urlencoded");
        connection.addPayload("id1_hf_0", "");
        connection.addPayload("login", username);
        connection.addPayload("password", password);
        try {
            connection.request(BarbarsConnection.SHADOW_HOST+"login/wicket:interface/:4:loginForm::IFormSubmitListener::");
            String userIdRaw = connection.getCookie("id");
            if(userIdRaw != null && !userIdRaw.isEmpty()) {
                userId = Integer.parseInt(userIdRaw);
                System.out.println(Logger.composeLogout(String.format("Successful login to %s, obtained id: %s", username, userId)));
            } else {
                System.out.println(Logger.composeLogout(String.format("Failed to auth %s! Username or password incorrect", username)));
                fireEvent(EventType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(Logger.composeLogout("Failed to login: " + e.getMessage()));
            fireEvent(EventType.ERROR);
        }
        isProcessing = false;
    }

    @Override
    public EventType type() {
        return EventType.SHADOW_LOGIN;
    }
}
