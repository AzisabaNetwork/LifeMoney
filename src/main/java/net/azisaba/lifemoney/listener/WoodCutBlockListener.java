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

        private int CHANCE = 50;

        @Override
        public int chance() {return CHANCE;}

        @Override
        public void setChance(int chance) {CHANCE = chance;}

        @Override
        public void drop(@NotNull Player p) {
            sound(p);
        }

        @Override
        public void addEnabledWorld() {
            for (World world : Bukkit.getWorlds()) {
                if (world == null) continue;
                if (isEnabledWorld(world.getName())) continue;
                if (world.getName().contains("resource")) {
                    getEnabledWorlds().add(world.getName());

                } else if (world.getName().contains("farm")) {
                    getEnabledWorlds().add(world.getName());
                }
            }
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
            return getEnabledWorlds().contains(worldName) && getMineBlocks().contains(blockType);
        }

        @Override
        public boolean isChanceSuccessful() {
            return random.nextInt(100) < CHANCE;
        }

        @Override
        public void rewardPlayer(Player player, Material blockType) {
            drop(player);
            double coinAmount = getCoinByMaterial(5, blockType);
            Coin.addCoin(player.getUniqueId(), Moneys.WOODCUT, coinAmount);
        }

        @Override
        public Set<Material> getWoodcutBlocks() {
            return new HashSet<>(Tag.MINEABLE_AXE.getValues());
        }

        private enum MaterialCoinOffset {
            BIRCH_LOG(3.5, 4),
            DARK_OAK_LOG(2.0, 3),
            JUNGLE_OAK_LOG(3.0, 3, Material.JUNGLE_LOG, Material.OAK_LOG),
            SPRUCE_LOG(1.0, 3),
            WARPED_CRIMSON_STEM(1.5, 3, Material.WARPED_STEM, Material.CRIMSON_STEM),
            DEFAULT(1.0, 2);

            private final double base;
            private final int range;
            private final Set<Material> materials;

            MaterialCoinOffset(double base, int range, @NotNull Material... materials) {
                this.base = base;
                this.range = range;
                this.materials = materials.length == 0 ? Set.of(Material.valueOf(name())) : Set.of(materials);
            }

            public double getBase() {
                return base;
            }

            public int getRange() {
                return range;
            }

            public static MaterialCoinOffset fromMaterial(Material material) {
                return Arrays.stream(values())
                        .filter(offset -> offset.materials.contains(material))
                        .findFirst()
                        .orElse(DEFAULT);
            }
        }

        @Override
        public double getCoinByMaterial(double offSet, @NotNull Material m) {
            MaterialCoinOffset offset = MaterialCoinOffset.fromMaterial(m);
            return random.nextDouble(offset.getRange()) + offset.getBase();
        }
    }
}
