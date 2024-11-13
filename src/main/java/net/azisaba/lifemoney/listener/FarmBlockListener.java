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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class FarmBlockListener implements Listener {

    public void initialize(LifeMoney plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new FarmBlockListener.Break(), plugin);
    }

    Random random = new Random();

    public static class Break extends FarmBlockListener implements MoneyBlocks, Chance, Worlds {

        private int CHANCE = 35; // 35%

        @Override
        public int chance() {
            return CHANCE;
        }

        @Override
        public void setChance(int chance) {
            CHANCE = chance;
        }

        @Override
        public double getCoinByMaterial(double offSet, Material m) {
            double baseAmount;
            int range;
            if (Tag.MAINTAINS_FARMLAND.isTagged(m)) {
                baseAmount = 0.5;
                range = 3;
            } else {
                baseAmount = 0.25;
                range = 2;
            }
            return calculateCoinAmount(baseAmount, range);
        }

        private double calculateCoinAmount(double baseAmount, int range) {
            return random.nextDouble(range) + baseAmount;
        }

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
            return getEnabledWorlds().contains(worldName) && getFarmBlocks().contains(blockType);
        }

        @Override
        public boolean isChanceSuccessful() {
            return random.nextInt(100) < CHANCE;
        }

        @Override
        public void rewardPlayer(Player player, Material blockType) {
            double coinAmount = getCoinByMaterial(0, blockType);
            Coin.addCoin(player.getUniqueId(), Moneys.FARM, coinAmount);
        }

        @Override
        public Set<Material> getFarmBlocks() {
            return new HashSet<>(Tag.MINEABLE_HOE.getValues());
        }
    }
}
