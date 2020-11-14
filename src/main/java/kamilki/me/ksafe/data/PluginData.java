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

import kamilki.me.ksafe.KSafe;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class PluginData {

    public final Map<UUID, Map<ItemData, Integer>> userSafes = new ConcurrentHashMap<>();
    public final Set<UUID> changedUsers = ConcurrentHashMap.newKeySet();

    private final KSafe plugin;
    private final ConfigData configData;

    public PluginData(final KSafe plugin, final ConfigData configData) {
        this.configData = configData;
        this.plugin = plugin;
    }

    public void loadUsers() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (!this.initTable()) {
                return;
            }

            final String sql = "SELECT * FROM `" + this.configData.tableName + "`;";

            try (final Connection connection = this.configData.database.getConnection();
                            final PreparedStatement stmt = connection.prepareStatement(sql);
                            final ResultSet resultSet = stmt.executeQuery()) {

                while (resultSet.next()) {
                    final UUID user = UUID.fromString(resultSet.getString("uuid"));
                    final Map<ItemData, Integer> userSafe = new ConcurrentHashMap<>();

                    final String[] safeDataSplit = resultSet.getString("safeData").split(";");
                    for (final String safeDataEntry : safeDataSplit) {
                        final String[] entrySplit = safeDataEntry.split("-");
                        userSafe.put(ItemData.fromString(entrySplit[0]), Integer.parseInt(entrySplit[1]));
                    }

                    this.userSafes.put(user, userSafe);
                }
            } catch (final SQLException exception) {
                this.plugin.getLogger().warning("Failed to load user data!");
            }
        });
    }

    public void saveUsers() {
        if (!this.initTable()) {
            return;
        }

        final String sql;
        if (this.configData.usesMysql) {
            sql = "INSERT INTO `" + this.configData.tableName + "` (`uuid`, `safeData`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `safeData`=?;";
        } else {
            sql = "INSERT OR REPLACE INTO `" + this.configData.tableName + "` (`uuid`, `safeData`) VALUES (?, ?);";
        }

        try (final Connection connection = this.configData.database.getConnection();
                        final PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (final UUID user : this.changedUsers) {
                final Map<ItemData, Integer> safe = this.userSafes.get(user);
                final StringBuilder dataBuilder = new StringBuilder();

                for (final Entry<ItemData, Integer> safeEntry : safe.entrySet()) {
                    dataBuilder.append(";").append(safeEntry.getKey().toString()).append("-").append(safeEntry.getValue());
                }

                stmt.setString(1, user.toString());
                stmt.setString(2, dataBuilder.substring(1));
                
                if (this.configData.usesMysql) {
                    stmt.setString(3, dataBuilder.substring(1));
                }

                stmt.executeUpdate();
            }

            this.changedUsers.clear();
        } catch (final SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to save user data!", exception);
        }
    }

    private boolean initTable() {
        final String sql = "CREATE TABLE IF NOT EXISTS `" + this.configData.tableName
                        + "` (`uuid` VARCHAR(36) NOT NULL, `safeData` TEXT NOT NULL, PRIMARY KEY(uuid));";

        try (final Connection connection = this.configData.database.getConnection();
                        final PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (final SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to create database table!", exception);
            return false;
        }

        return true;
    }

}
