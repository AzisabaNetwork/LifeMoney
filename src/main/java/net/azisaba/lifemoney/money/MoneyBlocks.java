package net.azisaba.lifemoney.money;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public interface MoneyBlocks {

    default Set<Material> getMineBlocks() {
        return new HashSet<>();
    }

    default Set<Material> getFarmBlocks() {
        return new HashSet<>();
    }

    default Set<Material> getWoodcutBlocks() {
        return new HashSet<>();
    }

    default Set<Material> getFishBlocks() {
        return new HashSet<>();
    }

    default void sound(@NotNull Player p) {
        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 2F ,0.5F);
    }

    void drop(@NotNull Player p);

    double getCoinByMaterial(double offSet, Material m);
}
