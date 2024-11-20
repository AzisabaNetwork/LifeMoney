package net.azisaba.lifemoney.listener;

import net.azisaba.lifemoney.LifeMoney;
import net.azisaba.lifemoney.money.Chance;
import net.azisaba.lifemoney.money.MoneyBlocks;
import net.azisaba.lifemoney.money.Moneys;
import net.azisaba.lifemoney.money.Worlds;
import net.azisaba.lifemoney.money.coin.Coin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WoodCutBlockListener implements Listener {

    public void initialize(LifeMoney lifeMoney) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new WoodCutBlockListener.Break(), lifeMoney);
    }

    Random random = new Random();

    public static class Break extends WoodCutBlockListener implements MoneyBlocks, Chance, Worlds {

        private int CHANCE = 35; //35%

        @Override
        public int chance() {return CHANCE;}

        @Override
        public void setChance(int chance) {CHANCE = chance;}

        @Override
        public Set<String> getEnabledWorlds() {
            Set<String> worlds = new HashSet<>();
            for (World world : Bukkit.getWorlds()) {
                if (world == null) continue;
                if (world.getName().contains("resource")) {
                    worlds.add(world.getName());
                } else if (world.getName().contains("farm")) {
                    worlds.add(world.getName());
                }
            }
            return worlds;
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockBreak(@NotNull BlockBreakEvent event) {
            Block block = event.getBlock();
            String worldName = block.getWorld().getName();
            Material blockType = block.getType();

            if (!isValidWorldAndBlockType(worldName, blockType)) return;
            if (!isChanceSuccessful()) return;

            Player player = event.getPlayer();
            rewardPlayer(player, blockType);
        }

        @Override
        public boolean isValidWorldAndBlockType(String worldName, Material blockType) {
            return getEnabledWorlds().contains(worldName) && getWoodcutBlocks().contains(blockType);
        }

        @Override
        public boolean isChanceSuccessful() {
            return random.nextInt(100) < CHANCE;
        }

        @Override
        public void rewardPlayer(@NotNull Player player, Material blockType) {
            double coinAmount = getCoinByMaterial(5, blockType);
            Coin.addCoin(player.getUniqueId(), Moneys.WOODCUT, coinAmount);
        }

        @Override
        public Set<Material> getWoodcutBlocks() {
            return new HashSet<>(Tag.LOGS.getValues());
        }

        public record MaterialCoinOffset(double base, int range) {}

        private double applyOffset(double currentOffset, Material material, @NotNull Tag<Material> tag, double randomRange, double baseValue) {
            if (tag.isTagged(material)) {
                currentOffset += random.nextDouble() * randomRange + baseValue;
            }
            return currentOffset;
        }

        @Override
        public double getCoinByMaterial(double offSet, Material material) {
            offSet = applyOffset(offSet, material, Tag.ACACIA_LOGS, 5  * getMultiplier(), 7.5 * getMultiplier());
            offSet = applyOffset(offSet, material, Tag.BIRCH_LOGS, 3.5 * getMultiplier(), 5 * getMultiplier());
            offSet = applyOffset(offSet, material, Tag.DARK_OAK_LOGS, 2 * getMultiplier(), 5 * getMultiplier());
            offSet = applyOffset(offSet, material, Tag.JUNGLE_LOGS, 3 * getMultiplier(), 4 * getMultiplier());
            offSet = applyOffset(offSet, material,  Tag.OAK_LOGS, 3 * getMultiplier(), 4 * getMultiplier());
            offSet = applyOffset(offSet, material, Tag.SPRUCE_LOGS, getMultiplier(), 3 * getMultiplier());
            offSet = applyOffset(offSet, material,  Tag.CRIMSON_STEMS, 1.5 * getMultiplier(), 5 * getMultiplier());
            offSet = applyOffset(offSet, material,  Tag.WARPED_STEMS, 1.5 * getMultiplier(), 5 * getMultiplier());
            return offSet;
        }
    }
}
