package dev.kwlew.kmoney.managers.update;

import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.managers.config.BuildInfo;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateParser implements LifecycleComponent {

    private static final String API_URL =
            "https://api.modrinth.com/v2/project/kmoney/version";

    private String latestVersion;
    private boolean outdated;

    private final JavaPlugin plugin;
    private final ConfigManager config;

    public UpdateParser(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void start() {
        if (!config.isUpdateWarningEnabled()) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLogger().info("Checking for updates...");
            verifyLatestVersion();
        });
    }

    private void verifyLatestVersion() {
        if (!check()) {
            return;
        }

        if (outdated) {
            plugin.getLogger().warning(
                    "Plugin is outdated! Current: "
                            + BuildInfo.VERSION
                            + " Latest: "
                            + latestVersion

            );
            plugin.getLogger().warning("Update at: " + BuildInfo.MODRINTH_URL);
            return;
        }

        plugin.getLogger().info("The plugin is updated!");
    }

    public boolean check() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "kMoney");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                plugin.getLogger().warning("Update check failed with HTTP status: " + statusCode);
                return false;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String json = response.toString();

            int index = json.indexOf("\"version_number\":\"");

            if (index == -1) {
                plugin.getLogger().warning("Update check failed: version_number was not found in response.");
                return false;
            }

            int start = index + 18;
            int end = json.indexOf("\"", start);
            if (end == -1) {
                plugin.getLogger().warning("Update check failed: invalid version_number format in response.");
                return false;
            }

            latestVersion = json.substring(start, end);

            outdated = !BuildInfo.VERSION.equals(latestVersion);
            return true;
        } catch (IOException | RuntimeException e) {
            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isOutdated() {
        return outdated;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

}
