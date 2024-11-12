package net.azisaba.lifemoney.money;

import org.jetbrains.annotations.NotNull;

public enum Moneys {
    MYTHIC_KILL,
    MINE,
    FARM,
    WOODCUT,
    FISH,
    TIME_ELAPSED,
    ADMIN_SHOP_BUY,
    ADMIN_SHOP_SELL,
    PLAYER_SHOP_BUY,
    PLAYER_SHOP_SELL,
    SHOP_TAX,
    OTHER;

    public static Moneys getFromName(@NotNull String name) {
        return valueOf(name.toUpperCase());
    }
}
