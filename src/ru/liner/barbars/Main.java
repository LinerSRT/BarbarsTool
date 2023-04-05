package ru.liner.barbars;

import ru.liner.barbars.core.Bot;
import ru.liner.barbars.eventsystem.NetworkEventHandler;
import ru.liner.barbars.core.events.LoginEvent;
import ru.liner.barbars.eventsystem.EventType;

import java.io.IOException;

public class Main {
    private static final String username = "OxcOde";
    private static final String password = "coder228";

    public static void main(String[] args) throws IOException {
        Bot bot = new Bot(username, password);
    }
}
