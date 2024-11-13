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
import org.jetbrains.annotations.Contract;
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

        private static final Map<Tag<Material>, MaterialCoinOffset> MATERIAL_OFFSETS = Map.of(
                Tag.ACACIA_LOGS, new MaterialCoinOffset(4, 3),
                Tag.BIRCH_LOGS, new MaterialCoinOffset(3.5, 4),
                Tag.DARK_OAK_LOGS, new MaterialCoinOffset(2, 3),
                Tag.JUNGLE_LOGS, new MaterialCoinOffset(3, 3),
                Tag.OAK_LOGS, new MaterialCoinOffset(3, 3),
                Tag.SPRUCE_LOGS, new MaterialCoinOffset(1, 3),
                Tag.CRIMSON_STEMS, new MaterialCoinOffset(1.5, 3),
                Tag.WARPED_STEMS, new MaterialCoinOffset(1.5, 3)
        );

        @NotNull
        @Contract("_ -> new")
        public static MaterialCoinOffset fromMaterial(Material material) {
            for (Map.Entry<Tag<Material>, MaterialCoinOffset> entry : MATERIAL_OFFSETS.entrySet()) {
                if (entry.getKey().isTagged(material)) {
                    return entry.getValue();
                }
            }
            return new MaterialCoinOffset(1, 1);
        }

        @Override
        public double getCoinByMaterial(double offSet, @NotNull Material m) {
            MaterialCoinOffset offset = fromMaterial(m);
            return random.nextDouble(offset.range()) + offset.base();
        }
    }
}
