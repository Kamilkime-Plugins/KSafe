package kamilki.me.ksafe.replacement;

import kamilki.me.ksafe.data.ConfigData;
import kamilki.me.ksafe.data.PluginData;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

public interface ReplacementFunction {

    String replace(final MaterialData materialData, final Player player, final ConfigData configData, final PluginData pluginData);

}
