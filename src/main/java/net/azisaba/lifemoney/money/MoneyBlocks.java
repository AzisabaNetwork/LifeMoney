package net.azisaba.lifemoney.money;

import org.bukkit.Material;

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

    double getCoinByMaterial(double offSet, Material m);
}
