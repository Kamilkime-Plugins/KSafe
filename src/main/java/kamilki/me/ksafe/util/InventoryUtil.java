package kamilki.me.ksafe.util;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.util.Map;

public final class InventoryUtil {

    public static int getInventoryAmount(final HumanEntity human, final Material material, final short durability) {
        int amount = 0;

        for (final ItemStack item : human.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                continue;
            }

            if (item.getDurability() != durability) {
                continue;
            }

            amount += item.getAmount();
        }

        return amount;
    }

    public static int getInventoryAmount(final HumanEntity human, final MaterialData materialData) {
        return getInventoryAmount(human, materialData.getItemType(), materialData.getData());
    }

    public static int addToInventory(final HumanEntity human, final Material material, final short durability, int amount) {
        final PlayerInventory inventory = human.getInventory();
        for (int slot = 0; slot <= 35 && amount > 0; slot++) {
            final ItemStack item = inventory.getItem(slot);
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                continue;
            }

            if (item.getDurability() != durability) {
                continue;
            }

            final int freeSpace = material.getMaxStackSize() - item.getAmount();
            if (freeSpace <= 0) {
                continue;
            }

            final int afterAddition = amount - freeSpace;
            if (afterAddition > 0) {
                item.setAmount(item.getAmount() + freeSpace);
            } else {
                item.setAmount(item.getAmount() + amount);
            }

            amount = afterAddition;
        }

        if (amount > 0) {
            final Map<Integer, ItemStack> notAdded = inventory.addItem(new ItemStack(material, amount, durability));
            if (notAdded != null && !notAdded.isEmpty()) {
                int newAmount = 0;
                for (final ItemStack notAddedItem : notAdded.values()) {
                    newAmount += notAddedItem.getAmount();
                }

                amount = newAmount;
            } else {
                amount = 0;
            }
        }

        return Math.max(amount, 0);
    }

    public static int addToInventory(final HumanEntity human, final MaterialData materialData, final int amount) {
        return addToInventory(human, materialData.getItemType(), materialData.getData(), amount);
    }

    public static void removeFromInventory(final HumanEntity human, final Material material, final short durability, int amount) {
        final PlayerInventory inventory = human.getInventory();
        for (int slot = 35; slot >= 0 && amount > 0; slot--) {
            final ItemStack item = inventory.getItem(slot);
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                continue;
            }

            if (item.getDurability() != durability) {
                continue;
            }

            final int afterRemoval = amount - item.getAmount();
            if (afterRemoval < 0) {
                item.setAmount(item.getAmount() - amount);
            } else {
                inventory.setItem(slot, null);
            }

            amount = afterRemoval;
        }
    }

    public static void removeFromInventory(final HumanEntity human, final MaterialData materialData, final int amount) {
        removeFromInventory(human, materialData.getItemType(), materialData.getData(), amount);
    }

    private InventoryUtil() {}

}
