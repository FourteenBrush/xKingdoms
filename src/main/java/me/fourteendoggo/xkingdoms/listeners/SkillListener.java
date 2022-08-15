package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.skill.SkillType;
import me.fourteendoggo.xkingdoms.skill.SkillsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

public class SkillListener implements Listener {
    private static final Set<Material> HOE_TYPES = EnumSet.of(
            Material.WOODEN_HOE, Material.STONE_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE
    );
    private static final Set<Material> AXE_TYPES = EnumSet.of(
            Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    );
    private static final Set<Material> PICKAXE_TYPES = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE
    );
    private final XKingdoms plugin;
    private final SkillsManager skillsManager;

    public SkillListener(XKingdoms plugin) {
        this.plugin = plugin;
        this.skillsManager = plugin.getSkillsManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        Player player = event.getPlayer();

        if (Tag.CROPS.isTagged(material)) {
            handleProgress(SkillType.FARMING, HOE_TYPES::contains, player, event);
        } else if (Tag.LOGS_THAT_BURN.isTagged(material)) {
            handleProgress(SkillType.WOODCUTTING, AXE_TYPES::contains, player, event);
        } else if (Tag.STONE_ORE_REPLACEABLES.isTagged(material)) {
            handleProgress(SkillType.MINING, PICKAXE_TYPES::contains, player, event); // TODO
        }
    }

    private void handleProgress(SkillType type, Predicate<Material> toolPredicate, Player player, BlockBreakEvent event) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem.getType().isAir()) return;

        if (!toolPredicate.test(mainHandItem.getType())) {
            player.sendMessage(ChatColor.RED + "Please use the correct tool for this!");
            event.setCancelled(true);
            return;
        }

        KingdomPlayer kPlayer = plugin.getUserManager().getUser(player.getUniqueId());

        kPlayer.getData().incrementSkillXP(type, 1); // TODO: change this to a proper values for each material or smth
        skillsManager.handleLevelling(type, kPlayer);
    }
}
