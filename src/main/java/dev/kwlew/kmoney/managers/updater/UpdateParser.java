package dev.kwlew.kmoney.managers.updater;

import dev.kwlew.kmoney.kernel.LifecycleComponent;
import dev.kwlew.kmoney.managers.config.BuildInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateParser implements LifecycleComponent {

    private static final String API_URL =
            "https://api.modrinth.com/v2/project/kmoney/version";

    private String latestVersion;
    private boolean outdated;

    private final JavaPlugin plugin;

    public UpdateParser(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        plugin.getLogger().info("Checking for updates...");

        verifyLatestVersion();
    }

    private void verifyLatestVersion() {
        check();

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

    public void check() {
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) URI.create(API_URL).toURL().openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "kMoney");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String json = response.toString();

            int index = json.indexOf("\"version_number\":\"");

            if (index == -1) {
                return;
            }

            int start = index + 18;
            int end = json.indexOf("\"", start);

            latestVersion = json.substring(start, end);

            outdated = !BuildInfo.VERSION.equals(latestVersion);

        } catch (Exception ignored) {}
    }

    public boolean isOutdated() {
        return outdated;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

}
