package net.azisaba.lifemoney.money;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public interface Worlds {

    default Set<String> getEnabledWorlds() {
        return new HashSet<>();
    }

    void addEnabledWorld();

    default void removeEnabledWorld(String world) {
        getEnabledWorlds().remove(world);
    }

    default boolean isEnabledWorld(String world) {
        return getEnabledWorlds().contains(world);
    }

    boolean isValidWorldAndBlockType(String worldName, Material blockType);

    boolean isChanceSuccessful();

    void rewardPlayer(Player player, Material blockType);
}
