package kamilki.me.ksafe.replacement;

import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.ItemData;
import kamilki.me.ksafe.data.PluginData;
import org.bukkit.entity.Player;

public interface ReplacementFunction {

    String replace(final ItemData itemData, final Player player, final ConfigData configData, final PluginData pluginData);

}
