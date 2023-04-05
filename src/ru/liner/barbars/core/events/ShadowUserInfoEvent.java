package ru.liner.barbars.core.events;

import ru.liner.barbars.eventsystem.AbstractEvent;
import ru.liner.barbars.eventsystem.EventType;
import ru.liner.barbars.eventsystem.NetworkEventHandler;
import ru.liner.barbars.network.BarbarsConnection;
import ru.liner.barbars.utils.Logger;
import ru.liner.barbars.utils.Patterns;

import java.io.IOException;

public class ShadowUserInfoEvent extends AbstractEvent {
    private String username;
    private int userId;
    private int level;
    private int health;
    private int energy;
    private int goldCoins;
    private int silverCoins;
    private int bagUsage;
    private int chestUsage;
    private int bottleCount;
    private int ironCount;
    private int mifrilCount;
    private int actionRate;
    private String currentXp;
    private String requiredXp;
    private int levelProgressXp;
    private boolean fullBag;
    private boolean hasBetterItem;
    private boolean clothesBroken;

    public ShadowUserInfoEvent(NetworkEventHandler eventHandler) {
        super(eventHandler);
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public int getLevel() {
        return level;
    }

    public int getHealth() {
        return health;
    }

    public int getEnergy() {
        return energy;
    }

    public int getGoldCoins() {
        return goldCoins;
    }

    public int getSilverCoins() {
        return silverCoins;
    }

    public int getBagUsage() {
        return bagUsage;
    }

    public int getChestUsage() {
        return chestUsage;
    }

    public int getBottleCount() {
        return bottleCount;
    }

    public int getIronCount() {
        return ironCount;
    }

    public int getMifrilCount() {
        return mifrilCount;
    }

    public int getActionRate() {
        return actionRate;
    }

    public String getCurrentXp() {
        return currentXp;
    }

    public String getRequiredXp() {
        return requiredXp;
    }

    public int getLevelProgressXp() {
        return levelProgressXp;
    }

    public boolean isFullBag() {
        return fullBag;
    }

    public boolean isHasBetterItem() {
        return hasBetterItem;
    }

    public boolean isClothesBroken() {
        return clothesBroken;
    }

    @Override
    public void process(BarbarsConnection connection) {
        isProcessing = true;
        String userIdRaw = connection.getCookie("id");
        if(userIdRaw != null && !userIdRaw.isEmpty()) {
            userId = Integer.parseInt(userIdRaw);
            try {
                String pageContent = connection.request(BarbarsConnection.SHADOW_HOST+"user");
                health = Patterns.get("\\/images\\/icons\\/life\\.png\" alt=\"\" border=\"0\"\\/><span>([0-9]*)<", pageContent, 1, 0);
                energy = Patterns.get("\\/images\\/icons\\/energy\\.png\" alt=\"\" border=\"0\"\\/><span>([0-9]*)<", pageContent, 1, 0);
                username = Patterns.get("<b><span>(.*?)<\\/span><\\/b>,<b><span>([0-9]*)", pageContent, 1, "None");
                level = Patterns.get("<b><span>(.*?)<\\/span><\\/b>,<b><span>([0-9]*)", pageContent, 2, 0);
                goldCoins = Patterns.get("\\/images\\/icons\\/money_gold\\.png\"\\/><span>([0-9]*)<\\/span>", pageContent, 1, 0);
                silverCoins = Patterns.get("\\/images\\/icons\\/money_silver\\.png\"\\/><span>([0-9]*)<\\/span>", pageContent, 1, 0);
                bagUsage = Patterns.get("Рюкзак<\\/a> <span class=\"minor\">\\(([0-9]*)\\/20\\)", pageContent, 1, 0);
                chestUsage = Patterns.get("Сундук<\\/a> <span class=\"minor\">\\(([0-9]*)\\/20\\)", pageContent, 1, 0);
                bottleCount = Patterns.get("bottle\\.png\" alt=\"\" border=\"0\"\\/><span class=\"money\">([0-9]*)<\\/span>", pageContent, 1, 0);
                ironCount = Patterns.get("ironbar\\.png\" alt=\"\" border=\"0\"\\/><span class=\"money\">([0-9]*)<\\/span>", pageContent, 1, 0);
                mifrilCount = Patterns.get("mifrilbar\\.png\" alt=\"\" border=\"0\"\\/><span class=\"money\">([0-9]*)<\\/span>", pageContent, 1, 0);
                actionRate = Patterns.get("участие: <span>([0-9]*)<\\/span>", pageContent, 1, 0);
                currentXp = Patterns.get("опыт: <span>([0-9\\.K]*)<\\/span>\\/<span>([0-9.K]*)<\\/span> <span class=\\\"minor\\\">\\(([0-9]*)", pageContent, 1, "0");
                requiredXp = Patterns.get("опыт: <span>([0-9\\.K]*)<\\/span>\\/<span>([0-9.K]*)<\\/span> <span class=\\\"minor\\\">\\(([0-9]*)", pageContent, 2, "0");
                levelProgressXp = Patterns.get("опыт: <span>([0-9\\.K]*)<\\/span>\\/<span>([0-9.K]*)<\\/span> <span class=\\\"minor\\\">\\(([0-9]*)", pageContent, 3,0);
                fullBag = Patterns.exists("bag_full\\.gif", pageContent);
                hasBetterItem = Patterns.exists("bag_better\\.gif", pageContent);
                clothesBroken = Patterns.exists("clothes_broken\\.gif", pageContent);
                if(fullBag || hasBetterItem || bagUsage > 0)
                    fireEvent(EventType.SHADOW_ANALISE_BAG);
                System.out.println(this);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(Logger.composeLogout("Failed to load user page: " + e.getMessage()));
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

    @Override
    public EventType type() {
        return EventType.SHADOW_USER_INFO;
    }


    @Override
    public String toString() {
        return "ShadowUserInfoEvent{" +
                "username='" + username + '\'' +
                ", userId=" + userId +
                ", level=" + level +
                ", health=" + health +
                ", energy=" + energy +
                ", goldCoins=" + goldCoins +
                ", silverCoins=" + silverCoins +
                ", bagUsage=" + bagUsage +
                ", chestUsage=" + chestUsage +
                ", bottleCount=" + bottleCount +
                ", ironCount=" + ironCount +
                ", mifrilCount=" + mifrilCount +
                ", actionRate=" + actionRate +
                ", currentXp='" + currentXp + '\'' +
                ", requiredXp='" + requiredXp + '\'' +
                ", levelProgressXp=" + levelProgressXp +
                ", fullBag=" + fullBag +
                ", hasBetterItem=" + hasBetterItem +
                ", clothesBroken=" + clothesBroken +
                '}';
    }
}
