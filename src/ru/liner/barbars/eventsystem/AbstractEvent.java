package ru.liner.barbars.eventsystem;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractEvent implements Event {
    protected boolean isProcessing;
    private final NetworkEventHandler eventHandler;

    public AbstractEvent(NetworkEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public final void fireEvent(EventType eventType) {
        eventHandler.fireEvent(eventType);
    }

    public String fireActionLink(String link) {
        try {
            return eventHandler.getConnection().request(link);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String fireActionLinkAndConfirm(String link) {
        try {
            String content = eventHandler.getConnection().request(link);
            String confirmAction = getActionLink("confirmLink", content);
            if (!confirmAction.isEmpty()) {
                String host = link.contains("варвары") ? "https://варвары.рф/" : "https://barbars.ru/";
                return eventHandler.getConnection().request(host + confirmAction);
            } else {
                return content;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getActionLink(String action, String pageContent) {
        Pattern pattern = Pattern.compile("(\\?wicket:interface=:[0-9]*:.*?ILinkListener::.*?)\">");
        Matcher matcher = pattern.matcher(pageContent);
        while (matcher.find()) {
            if (matcher.group(1).contains(action))
                return matcher.group(1);
        }
        return "";
    }

    public <T extends Event> T getEventListener(EventType eventType) {
        return eventHandler.getEventListener(eventType);
    }


    @Override
    public final boolean isProcessing() {
        return isProcessing;
    }
}
