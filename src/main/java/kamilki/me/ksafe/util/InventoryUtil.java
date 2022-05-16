package kamilki.me.ksafe.util;

import java.util.Arrays;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

public final class InventoryUtil {

    public static int getInventoryAmount(final HumanEntity human, final Material material, final short durability) {
        return Arrays.stream(human.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> item.getType() == material)
                .filter(item -> item.getDurability() == durability)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    public static int getInventoryAmount(final HumanEntity human, final MaterialData materialData) {
        return getInventoryAmount(human, materialData.getItemType(), materialData.getData());
    }

    public static int addToInventory(final HumanEntity human, final Material material, final short durability, int amount) {
        final PlayerInventory inventory = human.getInventory();

        for (int slot = 0; slot <= 35; slot++) {
            final ItemStack item = inventory.getItem(slot);

            if (item == null || item.getType() != material || item.getDurability() != durability) {
                continue;
            }

            final int freeSpace = material.getMaxStackSize() - item.getAmount();
            if (freeSpace <= 0) {
                continue;
            }

            final int afterAddition = amount - freeSpace;
            if (afterAddition <= 0) {
                item.setAmount(item.getAmount() + amount);
                amount = 0;
                break;
            }

            item.setAmount(item.getAmount() + freeSpace);
            amount = afterAddition;
        }

        if (amount > 0) {
            amount = inventory.addItem(new ItemStack(material, amount, durability)).values().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(ItemStack::getAmount)
                    .sum();
        }

        return Math.max(amount, 0);
    }

    public static int addToInventory(final HumanEntity human, final MaterialData materialData, final int amount) {
        return addToInventory(human, materialData.getItemType(), materialData.getData(), amount);
    }

    public static void removeFromInventory(final HumanEntity human, final Material material, final short durability, int amount) {
        final PlayerInventory inventory = human.getInventory();

        for (int slot = 35; slot >= 0; slot--) {
            final ItemStack item = inventory.getItem(slot);

            if (item == null || item.getType() != material || item.getDurability() != durability) {
                continue;
            }

            final int afterRemoval = amount - item.getAmount();
            if (afterRemoval < 0) {
                item.setAmount(item.getAmount() - amount);
                break;
            }

            if (afterRemoval == 0) {
                inventory.setItem(slot, null);
                break;
            }

            inventory.setItem(slot, null);
            amount = afterRemoval;
        }
    }

    public static void removeFromInventory(final HumanEntity human, final MaterialData materialData, final int amount) {
        removeFromInventory(human, materialData.getItemType(), materialData.getData(), amount);
    }

    private InventoryUtil() {}

}
