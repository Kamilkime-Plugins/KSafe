/*
 * Copyright (C) 2020 Kamil Trysiński
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kamilki.me.ksafe.data;

import com.zaxxer.hikari.HikariDataSource;
import kamilki.me.ksafe.KSafe;
import kamilki.me.ksafe.replacement.ItemReplacementType;
import kamilki.me.ksafe.replacement.ItemReplacer;
import kamilki.me.ksafe.util.ItemUtil;
import kamilki.me.ksafe.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public final class ConfigData {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public HikariDataSource database;

    public String tableName;
    public String inventoryTitle;
    
    public String itemsTakenMsg;
    public String itemsSuppliedMsg;
    public String itemsWithdrawnMsg;
    public String itemsDepositedMsg;
    public String cantWithdrawMsg;
    public String cantDepositMsg;
    public String safeEmptyMsg;
    public String reloadInvCloseMsg;
    public String cfgReloadedMsg;

    public long autoSaveInterval;
    public long limitTaskInterval;
    public long autoSupplyTaskInterval;

    public boolean enableAutoSave;
    public boolean autoLimit;
    public boolean autoSupply;
    public boolean allowInvSafeWithdrawal;
    public boolean withdrawAll;
    public boolean allowInvSafeDeposit;
    public boolean depositAll;
    public boolean usesMysql;

    public final List<ItemStack> inventoryLayout = new ArrayList<>();
    
    public final Map<MaterialData, String> itemNames = new HashMap<>();
    public final Map<MaterialData, Integer> itemLimits = new HashMap<>();
    public final Map<ItemStack, Set<MaterialData>> inventoryItems = new HashMap<>();
    public final Map<ItemStack, ItemReplacementType> replaceableItems = new HashMap<>();

    private final KSafe plugin;

    public ConfigData(final KSafe plugin) {
        this.plugin = plugin;
    }

    public void loadConfig(final boolean reload) {
        this.plugin.saveDefaultConfig();
        
        if (reload) {
            this.replaceableItems.clear();
            this.inventoryLayout.clear();
            this.inventoryItems.clear();
            this.itemLimits.clear();
            
            this.plugin.reloadConfig();
        }
        
        final FileConfiguration cfg = this.plugin.getConfig();
        
        this.inventoryTitle = StringUtil.color(cfg.getString("inventoryTitle"));
        
        this.itemsTakenMsg = StringUtil.color(cfg.getString("itemsTakenMsg"));
        this.itemsSuppliedMsg = StringUtil.color(cfg.getString("itemsSuppliedMsg"));
        this.itemsWithdrawnMsg = StringUtil.color(cfg.getString("itemsWithdrawnMsg"));
        this.cantWithdrawMsg = StringUtil.color(cfg.getString("cantWithdrawMsg"));
        this.cantDepositMsg = StringUtil.color(cfg.getString("cantDepositMsg"));
        this.itemsDepositedMsg = StringUtil.color(cfg.getString("itemsDepositedMsg"));
        this.safeEmptyMsg = StringUtil.color(cfg.getString("safeEmptyMsg"));
        this.reloadInvCloseMsg = StringUtil.color(cfg.getString("reloadInvCloseMsg"));
        this.cfgReloadedMsg = StringUtil.color(cfg.getString("cfgReloadedMsg"));

        this.autoSaveInterval = cfg.getLong("autoSaveInterval");
        this.limitTaskInterval = cfg.getLong("limitTaskInterval");
        this.autoSupplyTaskInterval = cfg.getLong("autoSupplyTaskInterval");
        
        this.enableAutoSave = cfg.getBoolean("enableAutoSave");
        this.autoLimit = cfg.getBoolean("autoLimit");
        this.autoSupply = cfg.getBoolean("autoSupply");
        this.allowInvSafeWithdrawal = cfg.getBoolean("allowInvSafeWithdrawal");
        this.withdrawAll = cfg.getBoolean("withdrawAll");
        this.allowInvSafeDeposit = cfg.getBoolean("allowInvSafeDeposit");
        this.depositAll = cfg.getBoolean("depositAll");
        
        // Load item limits
        for (final String limitString : cfg.getStringList("limits")) {
            final String[] limitSplit = WHITESPACE_PATTERN.split(limitString);
            if (limitSplit.length < 2) {
                this.plugin.getLogger().warning("Incorrect limit: \"" + limitString + "\"");
                continue;
            }
            
            final int limit;
            try {
                limit = Integer.parseInt(limitSplit[1]);
            } catch (final NumberFormatException exception) {
                Bukkit.getLogger().warning("[LimitParser] " + limitSplit[1] + " is not a valid integer!");
                continue;
            }
            
            this.itemLimits.put(ItemUtil.getMaterialData(limitSplit[0]), limit);
        }
        
        // Load inventory items
        final Map<String, ItemStack> loadedItems = new HashMap<>();
        for (final String itemName : cfg.getConfigurationSection("inventoryItems").getKeys(false)) {
            final ConfigurationSection itemSection = cfg.getConfigurationSection("inventoryItems." + itemName);
            
            final ItemStack item = ItemUtil.parseItem(itemSection);
            final ItemMeta itemMeta = item.getItemMeta();

            final boolean replaceName = ItemReplacer.needsReplacement(itemMeta.getDisplayName());
            final boolean replaceLore = ItemReplacer.needsReplacement(itemMeta.getLore());
            
            final Set<MaterialData> withdrawals = new HashSet<>();
            for (final String withdraw : itemSection.getStringList("withdraw")) {
                withdrawals.add(ItemUtil.getMaterialData(withdraw));
            }
            
            loadedItems.put(itemName, item);
            
            this.inventoryItems.put(item, withdrawals);
            this.replaceableItems.put(item, ItemReplacementType.get(replaceName, replaceLore));
        }
        
        // Load inventory layout
        final List<String> inventoryItemNames = cfg.getStringList("inventory");
        if (inventoryItemNames.size() % 9 != 0) {
            Bukkit.getLogger().warning("Inventory size must be a multiple of 9!");
            return;
        }
        
        if (inventoryItemNames.isEmpty() || inventoryItemNames.size() > 54) {
            Bukkit.getLogger().warning("Inventory size must be: 9, 18, 27, 36, 45 or 54!");
            return;
        }
        
        for (final String itemName : inventoryItemNames) {
            final ItemStack item = loadedItems.get(itemName);
            if (item == null) {
                Bukkit.getLogger().warning("No item with name " + itemName + " found when loading inventory!");
                return;
            }
            
            this.inventoryLayout.add(item);
        }
        
        // Load item names
        for (final String nameEntry : cfg.getStringList("itemNames")) {
            final String[] nameEntrySplit = WHITESPACE_PATTERN.split(nameEntry);
            if (nameEntrySplit.length < 2) {
                this.plugin.getLogger().warning("Incorrect item name: \"" + nameEntry + "\"");
                continue;
            }
            
            this.itemNames.put(ItemUtil.getMaterialData(nameEntrySplit[0]), StringUtils.join(nameEntrySplit, " ", 1, nameEntrySplit.length));
        }
    }

    public boolean initDatabase() {
        this.plugin.saveDefaultConfig();
        
        final FileConfiguration cfg = this.plugin.getConfig();

        this.database = new HikariDataSource();

        if ("MYSQL".equalsIgnoreCase(cfg.getString("dataStorage"))) {
            final String hostname = cfg.getString("mysql.hostname");
            final String port = cfg.getString("mysql.port");
            final String database = cfg.getString("mysql.database");
            final String username = cfg.getString("mysql.username");
            final String password = cfg.getString("mysql.password");

            final boolean useSSL = cfg.getBoolean("mysql.useSSL");

            final int poolSize = cfg.getInt("mysql.poolSize");
            final int connectionTimeout = cfg.getInt("mysql.connectionTimeout");

            this.database.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=" + useSSL);
            this.database.setUsername(username);
            this.database.setPassword(password);

            this.database.setMaximumPoolSize(poolSize);
            this.database.setConnectionTimeout(connectionTimeout);

            this.tableName = cfg.getString("mysql.tableName");
            
            this.usesMysql = true;
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (final ClassNotFoundException exception) {
                this.plugin.getLogger().warning("Failed to initialize SQLite driver!");
                return false;
            }
            
            final File sqliteFile = new File(this.plugin.getDataFolder(), cfg.getString("sqlite.fileName"));
            if (!sqliteFile.exists()) {
                try {
                    if (!sqliteFile.createNewFile()) {
                        this.plugin.getLogger().warning("Failed to create SQLite database file!");
                        return false;
                    }
                } catch (final IOException exception) {
                    this.plugin.getLogger().warning("Failed to create SQLite database file!");
                    return false;
                }
            }

            this.database.setJdbcUrl("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
            this.database.setConnectionTestQuery("SELECT * FROM sqlite_master");
            
            this.tableName = cfg.getString("sqlite.tableName");
        }

        this.database.addDataSourceProperty("cachePrepStmts", true);
        this.database.addDataSourceProperty("prepStmtCacheSize", 250);
        this.database.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.database.addDataSourceProperty("useServerPrepStmts", true);

        try (final Connection ignored = this.database.getConnection()) {
            this.plugin.getLogger().info("Test database connection successful!");
        } catch (final SQLException exception) {
            this.plugin.getLogger().warning("Test database connection failed!");
            return false;
        }

        return true;
    }

}
