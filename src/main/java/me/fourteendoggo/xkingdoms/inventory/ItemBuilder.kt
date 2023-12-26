package me.fourteendoggo.xkingdoms.inventory

import me.fourteendoggo.xkingdoms.utils.Utils
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class ItemBuilder(private val itemStack: ItemStack) {
    private val itemMeta = itemStack.itemMeta
    private var lore: MutableList<String> = ArrayList()

    constructor(material: Material, amount: Int = 1) : this(ItemStack(material, amount))

    fun setDisplayName(name: String): ItemBuilder {
        itemMeta?.setDisplayName(Utils.colorizeWithHex(name))
        return this
    }

    fun addEnchant(enchantment: Enchantment, level: Int): ItemBuilder {
        itemMeta?.addEnchant(enchantment, level, true)
        return this
    }

    fun setLore(lore: List<String>): ItemBuilder {
        this.lore = lore.map { Utils.colorizeWithHex(it) }.toMutableList()
        return this
    }

    fun setLore(vararg lore: String): ItemBuilder {
        this.lore = lore.map { Utils.colorizeWithHex(it) }.toMutableList()
        return this
    }

    fun addLore(vararg lore: String?): ItemBuilder {
        for (line in lore) {
            this.lore.add(Utils.colorizeWithHex(line))
        }
        return this
    }

    fun build(): ItemStack {
        itemMeta?.lore = lore
        itemStack.setItemMeta(itemMeta)
        return itemStack
    }
}
