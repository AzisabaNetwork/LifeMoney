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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MineBlockListener implements Listener {

    public void initialize(LifeMoney plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new MineBlockListener.Break(), plugin);
    }

    Random random = new Random();

    public static class Break extends MineBlockListener implements MoneyBlocks, Chance, Worlds {

        @Override
        public Set<Material> getMineBlocks() {
            return collectMineBlocks();
        }

        @NotNull
        private Set<Material> collectMineBlocks() {
            Set<Material> materials = new HashSet<>();
            for (Tag<Material> tag : Arrays.asList(
                    Tag.STONE_ORE_REPLACEABLES,
                    Tag.DEEPSLATE_ORE_REPLACEABLES,
                    Tag.COAL_ORES,
                    Tag.COPPER_ORES,
                    Tag.IRON_ORES,
                    Tag.GOLD_ORES,
                    Tag.DIAMOND_ORES,
                    Tag.EMERALD_ORES,
                    Tag.LAPIS_ORES,
                    Tag.REDSTONE_ORES,
                    Tag.MINEABLE_SHOVEL)) {
                materials.addAll(tag.getValues());
            }
            return materials;
        }

        @Override
        public Set<String> getEnabledWorlds() {
            Set<String> worlds = new HashSet<>();
            for (World world : Bukkit.getWorlds()) {
                if (world == null) continue;
                if (world.getName().contains("resource")) {
                    worlds.add(world.getName());
                }
            }
            return worlds;
        }

            private int CHANCE = 5; //8%

        @Override
        public int chance() {return CHANCE;}

        public void setChance(int chance) {CHANCE = chance;}

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
            return getEnabledWorlds().contains(worldName) && getMineBlocks().contains(blockType);
        }

        @Override
        public boolean isChanceSuccessful() {
            return random.nextInt(100) < CHANCE;
        }

        @Override
        public void rewardPlayer(@NotNull Player player, Material blockType) {
            double coinAmount = getCoinByMaterial(1, blockType);
            Coin.addCoin(player.getUniqueId(), Moneys.MINE, coinAmount);
        }

        @Override
        public double getCoinByMaterial(double offSet, Material material) {
            offSet = applyOffset(offSet, material, Tag.STONE_ORE_REPLACEABLES, 5, 5);
            offSet = applyOffset(offSet, material, Tag.DEEPSLATE_ORE_REPLACEABLES, 5, 8);
            offSet = applyOffset(offSet, material, Tag.COAL_ORES, 10, 10);
            offSet = applyOffset(offSet, material, Tag.IRON_ORES, 10, 20);
            offSet = applyOffset(offSet, material, Tag.GOLD_ORES, 10, 30);
            offSet = applyOffset(offSet, material, Tag.DIAMOND_ORES, 10, 50);
            offSet = applyOffset(offSet, material, Tag.EMERALD_ORES, 10, 100);
            offSet = applyOffset(offSet, material, Tag.LAPIS_ORES, 10, 25);
            offSet = applyOffset(offSet, material, Tag.REDSTONE_ORES, 10, 15);
            offSet = applyOffset(offSet, material, Tag.COPPER_ORES, 10, 5);
            offSet = applyOffset(offSet, material, Tag.MINEABLE_SHOVEL, 10, 3);
            return offSet;
        }

        private double applyOffset(double currentOffset, Material material, @NotNull Tag<Material> tag, double randomRange, double baseValue) {
            if (tag.isTagged(material)) {
                currentOffset += random.nextDouble() * randomRange + baseValue;
            }
            return currentOffset;
        }
    }
}
