package me.fourteendoggo.xkingdoms.inventory;

import me.fourteendoggo.xkingdoms.utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private List<String> lore;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
        this.lore = new ArrayList<>();
    }

    public ItemBuilder setDisplayName(String name) {
        itemMeta.setDisplayName(Utils.colorizeWithHex(name));
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(Utils.colorizeWithHex(line));
        }
        this.lore = coloredLore;
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        List<String> coloredLore = new ArrayList<>(lore.length);
        for (String line : lore) {
            coloredLore.add(Utils.colorizeWithHex(line));
        }
        this.lore = coloredLore;
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }
        for (String line : lore) {
            this.lore.add(Utils.colorizeWithHex(line));
        }
        return this;
    }

    public ItemStack build() {
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
