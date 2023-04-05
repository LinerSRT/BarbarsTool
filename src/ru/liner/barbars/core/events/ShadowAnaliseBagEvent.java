package ru.liner.barbars.core.events;

import ru.liner.barbars.eventsystem.AbstractEvent;
import ru.liner.barbars.eventsystem.EventType;
import ru.liner.barbars.eventsystem.NetworkEventHandler;
import ru.liner.barbars.network.BarbarsConnection;
import ru.liner.barbars.utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShadowAnaliseBagEvent extends AbstractEvent {
    private static final String BAG_ENTRY = "https://варвары.рф/user/rack";

    public ShadowAnaliseBagEvent(NetworkEventHandler eventHandler) {
        super(eventHandler);
    }

    @Override
    public void process(BarbarsConnection connection) {
        isProcessing = true;
        String userIdRaw = connection.getCookie("id");
        if (userIdRaw != null && !userIdRaw.isEmpty()) {
            try {
                String pageContent = connection.request(BAG_ENTRY);
                String disassembleAll = getActionLink("crackAllIronLink", pageContent);
                if (!disassembleAll.isEmpty()) {
                    System.out.println(Logger.composeLogout("Disassembling all possible items"));
                    fireActionLinkAndConfirm(BarbarsConnection.SHADOW_HOST + disassembleAll);
                    process(connection);
                    return;
                }
                processBag(pageContent);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(Logger.composeLogout("Failed to load bag page: " + e.getMessage()));
                isProcessing = false;
                return;
            }
        } else {
            System.out.println(Logger.composeLogout("Not logged in user, re-login"));
            fireEvent(EventType.SHADOW_LOGIN);
            fireEvent(type());
        }
        isProcessing = false;
    }

    private void processBag(String pageContent) {
        if (pageContent == null)
            return;
        List<Item> itemList = new ArrayList<>();
        Pattern itemsPattern = Pattern.compile("<span>([а-яА-Я- ]*)<\\/span><\\/a>.*?<span class=\\\"(itemBad|)\\\">([0-9]*)<\\/span> ур.*?<\\/td>");
        Matcher itemsMatcher = itemsPattern.matcher(pageContent);
        ShadowUserInfoEvent userInfoEvent = getEventListener(EventType.SHADOW_USER_INFO);
        if (userInfoEvent == null)
            return;
        while (itemsMatcher.find()) {
            String itemName = itemsMatcher.group(1);
            int itemLevel = Integer.parseInt(itemsMatcher.group(3));
            boolean isBetterThanEquipped = itemsMatcher.group().contains("Лучше");
            String equipAction = getActionLink("wearLink", itemsMatcher.group());
            String storeAction = getActionLink("toStoreLink", itemsMatcher.group());
            String disassembleAction = getActionLink("crackLink", itemsMatcher.group());
            itemList.add(new Item(
                    itemName,
                    itemLevel,
                    isBetterThanEquipped,
                    equipAction,
                    disassembleAction,
                    storeAction
            ));
        }
        for (Item item : itemList) {
            if(item.hasEquipAction()){
                if(item.level <= userInfoEvent.getLevel()){
                    System.out.println(Logger.composeLogout(String.format("Found better item, equipping (%s|%s)", item.name, item.level)));
                    processBag(fireActionLinkAndConfirm(BarbarsConnection.SHADOW_HOST + item.equipAction));
                    return;
                } else if(item.hasStoreAction()){
                    System.out.println(Logger.composeLogout(String.format("Found high-level item, storing (%s|%s)", item.name, item.level)));
                    processBag(fireActionLinkAndConfirm(BarbarsConnection.SHADOW_HOST + item.storeAction));
                    return;
                }
            } else if(item.hasDisassembleAction()){
                System.out.println(Logger.composeLogout(String.format("Found possible disassemble-able item: %s, disassembling", item.name)));
                processBag(fireActionLinkAndConfirm(BarbarsConnection.SHADOW_HOST + item.disassembleAction));
                return;
            } else if(item.hasStoreAction()){
                System.out.println(Logger.composeLogout(String.format("Found item: %s, storing", item.name)));
                processBag(fireActionLinkAndConfirm(BarbarsConnection.SHADOW_HOST + item.storeAction));
                return;
            } else {
                System.out.println(Logger.composeLogout(String.format("Unknown item: %s, skipping", item.name)));
            }
        }
    }

    @Override
    public EventType type() {
        return EventType.SHADOW_ANALISE_BAG;
    }


    private record Item(String name, int level, boolean isBetterItem, String equipAction, String disassembleAction,
                        String storeAction) {

        public boolean hasEquipAction() {
            return !equipAction.isEmpty();
        }

        public boolean hasDisassembleAction() {
            return !disassembleAction.isEmpty();
        }

        public boolean hasStoreAction() {
            return !storeAction.isEmpty();
        }

        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    ", level=" + level +
                    ", isBetterItem=" + isBetterItem +
                    ", equipAction='" + equipAction + '\'' +
                    ", disassembleAction='" + disassembleAction + '\'' +
                    ", storeAction='" + storeAction + '\'' +
                    '}';
        }
    }
}
