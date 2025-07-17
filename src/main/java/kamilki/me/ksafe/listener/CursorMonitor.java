package kamilki.me.ksafe.listener;

import java.util.AbstractMap;
import java.util.logging.Logger;
import kamilki.me.ksafe.data.PluginData;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class CursorMonitor implements Listener {

    private final Logger logger;
    private final PluginData pluginData;

    public CursorMonitor(final Logger logger, final PluginData pluginData) {
        this.logger = logger;
        this.pluginData = pluginData;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void monitorCursor(final InventoryClickEvent event) {
        final ClickType click = event.getClick();
        final InventoryAction action = event.getAction();
        final ItemStack item = event.getCurrentItem();
        final ItemStack cursor = event.getCursor();
        final HumanEntity human = event.getWhoClicked();

        if (click == ClickType.CREATIVE && human.getGameMode() != GameMode.CREATIVE) {
            this.logger.warning(human.getName() + " used ClickType.CREATIVE with gamemode " + human.getGameMode());
            return;
        }

        switch (click) {
            case LEFT:
                switch (action) {
                    case PICKUP_ALL:
                    case SWAP_WITH_CURSOR:
                        putToCursorCache(human, item);
                        break;
                    case PLACE_ALL:
                    case PLACE_ONE:
                    case DROP_ALL_CURSOR:
                        clearCursorCache(human);
                        break;
                    case PLACE_SOME:
                        putToCursorCache(human, item, cursor.getAmount() - (cursor.getMaxStackSize() - item.getAmount()));
                        break;
                }

                break;
            case RIGHT:
                switch (action) {
                    case SWAP_WITH_CURSOR:
                        putToCursorCache(human, item);
                        break;
                    case PLACE_ONE:
                    case DROP_ONE_CURSOR:
                        putToCursorCache(human, cursor, cursor.getAmount() - 1);
                        break;
                    case PICKUP_HALF:
                        putToCursorCache(human, item, item.getAmount() / 2 + 1);
                }

                break;
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        clearCursorCache(event.getPlayer());
    }

    private void putToCursorCache(final HumanEntity human, final ItemStack item) {
        this.putToCursorCache(human, item, item.getAmount());
    }

    private void putToCursorCache(final HumanEntity human, final ItemStack item, final int amount) {
        this.pluginData.cursorCache.put(human.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(item.getType(), amount));
    }

    private void clearCursorCache(final HumanEntity human) {
        this.pluginData.cursorCache.remove(human.getUniqueId());
    }
}
