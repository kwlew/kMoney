package dev.kwlew.kmoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.kwlew.kmoney.economy.EconomyManager;
import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.economy.api.EconomyTopEntry;
import dev.kwlew.kmoney.economy.utils.Formatter;
import dev.kwlew.kmoney.economy.utils.MoneyValidator;
import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.managers.check.Check;
import dev.kwlew.kmoney.managers.check.CheckSettings;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import dev.kwlew.kmoney.managers.sound.SoundFactory;
import dev.kwlew.kmoney.managers.sound.SoundType;
import dev.kwlew.kmoney.managers.utils.MessageManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles the /money command for balance queries, transfers, and money checks.
 * <p>
 * Supports subcommands:
 * - /money - View own balance
 * - /money balance [player] - Check balance of yourself or another player
 * - /money pay &lt;player&gt; &lt;amount&gt; - Send money to another player
 * - /money add &lt;player&gt; &lt;amount&gt; - Admin: Add money to a player (requires permission)
 * - /money remove &lt;player&gt; &lt;amount&gt; - Admin: Remove money from a player (requires permission)
 * - /money set &lt;player&gt; &lt;amount&gt; - Admin: Set a player's balance (requires permission)
 * - /money withdraw &lt;amount&gt; [notes] - Create physical money checks
 * - /money admin &lt;on|off&gt; - Enable or disable your admin join message
 * - /money reload - Admin: Reload plugin configuration (requires permission)
 * <p>
 * All monetary amounts support suffixes: k (thousand), m (million), b (billion), t (trillion), etc.
 */
public final class MoneyCommand extends StandardCommand {

    private final NamespacedKey key;
    private final SoundFactory soundFactory;

    private static final int TOP_PAGE_SIZE = 10;
    private static final List<String> EXAMPLE_AMOUNTS = List.of(
            "all", "100", "1K", "500k", "1M", "1B", "1T", "1Q"
    );

    @Inject
    public MoneyCommand(JavaPlugin plugin,
                        EconomyService economy,
                        MessageManager messages,
                        ConfigManager config,
                        CheckSettings checkSettings,
                        SoundFactory soundFactory) {
        super(plugin, economy, messages, config, checkSettings);
        this.key = new NamespacedKey(plugin, "money");
        this.soundFactory = soundFactory;
    }

    @Override
    public void start() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(build())
        );
    }

    private LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("money")
                .executes(ctx -> {
                    if (requirePermission(ctx.getSource(), "kmoney.command.money")) {
                        sendSelfBalance(ctx.getSource());
                        return Command.SINGLE_SUCCESS;
                    }

                    return 0;
                })
                .then(buildTopCommand())
                .then(buildAdminCommand())
                .then(buildBalanceCommand())
                .then(buildReloadCommand())
                .then(buildPayCommand())
                .then(buildAddCommand())
                .then(buildRemoveCommand())
                .then(buildSetCommand())
                .then(buildWithdrawCommand())
                .build();
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildTopCommand() {
        return Commands.literal("top")
                .requires(source -> hasPermission(source, "kmoney.command.money.top"))
                .executes(ctx -> {
                    if (requirePermission(ctx.getSource(), "kmoney.command.money.top")) {
                        return handleTop(ctx.getSource(), 1);
                    }

                    return 0;
                })
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            if (requirePermission(ctx.getSource(), "kmoney.command.money.top")) {
                                int page = IntegerArgumentType.getInteger(ctx, "page");
                                return handleTop(ctx.getSource(), page);
                            }

                            return 0;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildAdminCommand() {
        return Commands.literal("admin")
                .requires(source -> hasPermission(source, "kmoney.command.money.admin"))
                .executes(ctx -> {
                    if (requirePermission(ctx.getSource(), "kmoney.command.money.admin")) {
                        messages.send(ctx.getSource().getSender(), "usage.admin");
                    }
                    return 0;
                })
                .then(Commands.literal("on")
                        .executes(ctx -> {
                            if (requirePermission(ctx.getSource(), "kmoney.command.money.admin")) {
                                return handleAdminToggle(ctx.getSource(), true);
                            }

                            return 0;
                        }))
                .then(Commands.literal("off")
                        .executes(ctx -> {
                            if (requirePermission(ctx.getSource(), "kmoney.command.money.admin")) {
                                return handleAdminToggle(ctx.getSource(), false);
                            }

                            return 0;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildBalanceCommand() {
        return Commands.literal("balance")
                .executes(ctx -> {
                    if (requirePermission(ctx.getSource(), "kmoney.command.money")) {
                        sendSelfBalance(ctx.getSource());
                        return Command.SINGLE_SUCCESS;
                    }

                    return 0;
                })
                .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(ctx -> {
                            if (requirePermission(ctx.getSource(), "kmoney.command.money")) {
                                Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                        .resolve(ctx.getSource())
                                        .getFirst();
                                sendBalance(ctx.getSource().getSender(), target);
                                return Command.SINGLE_SUCCESS;
                            }

                            return 0;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildReloadCommand() {
        return Commands.literal("reload")
                .requires(source -> hasPermission(source, "kmoney.command.money.reload"))
                .executes(ctx -> {
                    if (requirePermission(ctx.getSource(), "kmoney.command.money.reload")) {
                        config.reloadAll();
                        messages.reload();
                        checkSettings.reload();
                        if (economy instanceof EconomyManager manager) {
                            manager.rescheduleTopUpdater();
                        }
                        messages.send(ctx.getSource().getSender(), "money.reloaded");
                        soundFactory.reload();
                        soundFactory.play(getPlayer(ctx.getSource()), SoundType.RELOAD);
                        return Command.SINGLE_SUCCESS;
                    }

                    return 0;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildPayCommand() {
        return Commands.literal("pay")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .then(Commands.argument("amount", StringArgumentType.word())
                                .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                                    EXAMPLE_AMOUNTS.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (requirePermission(ctx.getSource(), "kmoney.command.money.pay")) {
                                        Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                                .resolve(ctx.getSource())
                                                .getFirst();
                                        return handlePay(ctx.getSource(), target, StringArgumentType.getString(ctx, "amount"));
                                    }

                                    return 0;
                                })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildAddCommand() {
        return Commands.literal("add")
                .requires(source -> hasPermission(source, "kmoney.command.money.add"))
                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                builder.suggest(player.getName());
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("amount", StringArgumentType.word())
                                .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                                    EXAMPLE_AMOUNTS.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (requirePermission(ctx.getSource(), "kmoney.command.money.add")) {
                                        String targetName = StringArgumentType.getString(ctx, "target");
                                        OfflinePlayer target = resolveKnownTarget(ctx.getSource(), targetName);
                                        if (target == null) {
                                            return 0;
                                        }

                                        String amount = StringArgumentType.getString(ctx, "amount");
                                        return handleAdd(ctx.getSource(), target, targetName, amount);
                                    }

                                    return 0;
                                })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildRemoveCommand() {
        return Commands.literal("remove")
                .requires(source -> hasPermission(source, "kmoney.command.money.remove"))
                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                builder.suggest(player.getName());
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("amount", StringArgumentType.word())
                                .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                                    EXAMPLE_AMOUNTS.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (requirePermission(ctx.getSource(), "kmoney.command.money.remove")) {
                                        String targetName = StringArgumentType.getString(ctx, "target");
                                        OfflinePlayer target = resolveKnownTarget(ctx.getSource(), targetName);
                                        if (target == null) {
                                            return 0;
                                        }

                                        String amount = StringArgumentType.getString(ctx, "amount");
                                        return handleRemove(ctx.getSource(), target, targetName, amount);
                                    }

                                    return 0;
                                })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildSetCommand() {
        return Commands.literal("set")
                .requires(source -> hasPermission(source, "kmoney.command.money.set"))
                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                builder.suggest(player.getName());
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("amount", StringArgumentType.word())
                                .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                                    EXAMPLE_AMOUNTS.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (requirePermission(ctx.getSource(), "kmoney.command.money.set")) {
                                        String targetName = StringArgumentType.getString(ctx, "target");
                                        OfflinePlayer target = resolveKnownTarget(ctx.getSource(), targetName);
                                        if (target == null) {
                                            return 0;
                                        }
                                        String amount = StringArgumentType.getString(ctx, "amount");
                                        return handleSet(ctx.getSource(), target, targetName, amount);
                                    }

                                    return 0;
                                })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildWithdrawCommand() {
        return Commands.literal("withdraw")
                .then(Commands.argument("amount", StringArgumentType.word())
                        .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                            EXAMPLE_AMOUNTS.forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            if (requirePermission(ctx.getSource(), "kmoney.command.money.withdraw")) {
                                String amount = StringArgumentType.getString(ctx, "amount");
                                return handleWithdraw(ctx.getSource(), amount, null);
                            }

                            return 0;
                        })
                        .then(Commands.argument("notes", StringArgumentType.word())
                                .suggests((@SuppressWarnings("unused") var ctx, var builder) -> {
                                    List<String> suggestions = List.of("2", "4", "8", "16");
                                    suggestions.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (requirePermission(ctx.getSource(), "kmoney.command.money.withdraw")) {
                                        String amount = StringArgumentType.getString(ctx, "amount");
                                        String notes = StringArgumentType.getString(ctx, "notes");
                                        return handleWithdraw(ctx.getSource(), amount, notes);
                                    }

                                    return 0;
                                })));
    }

    private int handleTop(CommandSourceStack source, int page) {
        int fetchCount = page * TOP_PAGE_SIZE;
        int start = (page - 1) * TOP_PAGE_SIZE;

        CommandSender sender = source.getSender();
        int updateIntervalSeconds = config.getTopUpdateIntervalSeconds();

        List<EconomyTopEntry> top = economy.getTopEntries(fetchCount);
        if (top.isEmpty() || start >= top.size()) {
            sender.sendMessage(messages.getRaw("money.top-empty",
                    messages.placeholder("page", String.valueOf(page))));
            sender.sendMessage(messages.getRaw("money.top-footer",
                    messages.placeholder("seconds", String.valueOf(updateIntervalSeconds))));
            return 0;
        }

        sender.sendMessage(messages.get("money.top-header",
                messages.placeholder("page", String.valueOf(page))));

        int end = Math.min(start + TOP_PAGE_SIZE, top.size());
        for (int i = start; i < end; i++) {
            EconomyTopEntry entry = top.get(i);
            sender.sendMessage(messages.getRaw("money.top-line",
                    messages.placeholder("rank", String.valueOf(i + 1)),
                    messages.placeholder("player", resolveTopPlayerName(entry.uuid())),
                    messages.placeholder("amount", formatMoney(entry.balance()))
            ));
        }

        sender.sendMessage(messages.getRaw("money.top-footer",
                messages.placeholder("seconds", String.valueOf(updateIntervalSeconds))));
        soundFactory.play(getPlayer(source), SoundType.TOP);
        return Command.SINGLE_SUCCESS;
    }

    private int handleWithdraw(CommandSourceStack source, String amountInput, String notesAmount) {
        Player player = getPlayer(source);
        BigDecimal amount;
        final int notes;

        if (player == null) {
            messages.send(source.getSender(), "money.player-only");
            return 0;
        }

        economy.createAccount(player.getUniqueId());

        try {
            notes = notesAmount == null ? 1 : Integer.parseInt(notesAmount);
            MoneyValidator.validateNotesCount(notes);
        } catch (NumberFormatException e) {
            messages.send(source.getSender(), "money.invalid-amount",
                    messages.placeholder("amount", notesAmount)
            );
            return 0;
        } catch (IllegalArgumentException e) {
            messages.send(source.getSender(), "money.max-notes");
            plugin.getLogger().warning("Invalid notes count from " + source.getSender().getName() + ": " + notesAmount);
            return 0;
        }

        BigDecimal playerBalance = economy.getBalance(player.getUniqueId());
        if ("all".equalsIgnoreCase(amountInput)) {
            amount = playerBalance;
        } else {
            amount = parsePositiveAmount(source.getSender(), amountInput);
            if (amount == null) {
                return 0;
            }
        }

        BigDecimal total = amount.multiply(BigDecimal.valueOf(notes));
        if (total.signum() <= 0) {
            messages.send(source.getSender(), "money.invalid-amount",
                    messages.placeholder("amount", amountInput)
            );
            return 0;
        }

        if (total.compareTo(playerBalance) > 0) {
            messages.send(source.getSender(), "money.insufficient-funds",
                    messages.placeholder("amount", formatMoney(total)),
                    messages.placeholder("balance", formatMoney(playerBalance))
            );
            return 0;
        }

        if (!createdCheck(source, amount, notes)) {
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private boolean createdCheck(CommandSourceStack source, BigDecimal amount, int notesAmount) {
        Player player = getPlayer(source);

        assert player != null;
        if (player.getInventory().firstEmpty() == -1) {
            messages.send(source.getSender(), "player.inventory-full");
            return false;
        }

        ItemStack check = new ItemStack(Check.getMaterial(), notesAmount);
        ItemMeta meta = check.getItemMeta();

        String symbol = config.getCurrencySymbol();
        BigDecimal total = amount.multiply(BigDecimal.valueOf(notesAmount));
        String formattedPerNote = Formatter.format(amount, symbol);
        String formattedTotal = Formatter.format(total, symbol);

        meta.displayName(
            messages.getRaw("check.create-name")
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(java.util.List.of(
                messages.getRaw("check.create-value", messages.placeholder("amount", formattedPerNote))
                        .decoration(TextDecoration.ITALIC, false),
                messages.getRaw("check.create-creator", messages.placeholder("player", source.getSender().getName()))
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, amount.toPlainString());

        check.setItemMeta(meta);

        soundFactory.play(player, SoundType.WITHDRAW);
        player.getInventory().addItem(check);
        economy.removeBalance(player.getUniqueId(), total);

        messages.send(source.getSender(), "check.success", messages.placeholder("amount", formattedTotal));

        Check.incrementChecksCreated();
        return true;
    }

    private void sendSelfBalance(CommandSourceStack source) {
        Player player = getPlayer(source);
        if (player == null) {
            messages.send(source.getSender(), "money.player-only");
            return;
        }

        economy.createAccount(player.getUniqueId());
        messages.send(source.getSender(), "money.balance-self",
                messages.placeholder("balance", formatMoney(economy.getBalance(player.getUniqueId())))
        );
    }

    private void sendBalance(CommandSender sender, Player target) {
        economy.createAccount(target.getUniqueId());
        messages.send(sender, "money.balance-other",
                messages.placeholder("player", target.getName()),
                messages.placeholder("balance", formatMoney(economy.getBalance(target.getUniqueId())))
        );
    }

    private int handleSet(CommandSourceStack source, OfflinePlayer target, String targetLabel, String amountInput) {
        BigDecimal amount = parsePositiveAmount(source.getSender(), amountInput);
        if (amount == null) {
            return 0;
        }

        UUID targetUuid = target.getUniqueId();
        economy.createAccount(targetUuid);
        economy.setBalance(targetUuid, amount);
        String formattedAmount = formatMoney(amount);
        String displayName = resolveTargetName(target, targetLabel);

        messages.send(source.getSender(), "money.set",
                messages.placeholder("player", displayName),
                messages.placeholder("amount", formattedAmount));

        if (target.isOnline() && target.getPlayer() != null) {
            messages.send(target.getPlayer(), "money.got-set",
                    messages.placeholder("player", source.getSender().getName()),
                    messages.placeholder("amount", formattedAmount)
            );
        }
        soundFactory.play(getPlayer(source), SoundType.SET);
        if (target.isOnline() && target.getPlayer() != null) {
            soundFactory.play(target.getPlayer(), SoundType.SET);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int handleRemove(CommandSourceStack source, OfflinePlayer target, String targetLabel, String amountInput) {

        BigDecimal amount;

        if (Objects.equals(economy.getBalance(target.getUniqueId()), BigDecimal.ZERO)) {
            messages.send(source.getSender(), "money.no-money",
                    messages.placeholder("player", source.getSender().getName()));
            return 0;
        }

        if ("all".equalsIgnoreCase(amountInput)) {
            amount = economy.getBalance(target.getUniqueId());
        } else {
            amount = parsePositiveAmount(source.getSender(), amountInput);
            if (amount == null) {
                return 0;
            }
        }

        UUID targetUuid = target.getUniqueId();
        economy.createAccount(targetUuid);
        economy.removeBalance(targetUuid, amount);
        String formattedAmount = formatMoney(amount);
        String displayName = resolveTargetName(target, targetLabel);

        messages.send(source.getSender(), "money.removed",
                messages.placeholder("player", displayName),
                messages.placeholder("amount", formattedAmount));

        if (target.isOnline() && target.getPlayer() != null) {
            messages.send(target.getPlayer(), "money.got-removed",
                    messages.placeholder("player", source.getSender().getName()),
                    messages.placeholder("amount", formattedAmount)
            );
        }
        soundFactory.play(getPlayer(source), SoundType.REMOVE);
        if (target.isOnline() && target.getPlayer() != null) {
            soundFactory.play(target.getPlayer(), SoundType.REMOVE);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int handleAdd(CommandSourceStack source, OfflinePlayer target, String targetLabel, String amountInput) {

        BigDecimal amount = parsePositiveAmount(source.getSender(), amountInput);
        if (amount == null) {
            return 0;
        }

        UUID targetUuid = target.getUniqueId();
        economy.createAccount(targetUuid);
        economy.addBalance(targetUuid, amount);
        String formattedAmount = formatMoney(amount);
        String displayName = resolveTargetName(target, targetLabel);

        messages.send(source.getSender(), "money.added",
                messages.placeholder("player", displayName),
                messages.placeholder("amount", formattedAmount)
        );
        if (target.isOnline() && target.getPlayer() != null) {
            messages.send(target.getPlayer(), "money.got-added",
                    messages.placeholder("player", source.getSender().getName()),
                    messages.placeholder("amount", formattedAmount)
            );
        }
        soundFactory.play(getPlayer(source), SoundType.ADD);
        if (target.isOnline() && target.getPlayer() != null) {
            soundFactory.play(target.getPlayer(), SoundType.ADD);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int handlePay(CommandSourceStack source, Player target, String amountInput) {
        Player sender = getPlayer(source);
        if (sender == null) {
            messages.send(source.getSender(), "money.player-only");
            return 0;
        }

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            messages.send(sender, "money.same-player");
            return 0;
        }

        BigDecimal amount = parsePositiveAmount(sender, amountInput);
        if (amount == null) {
            return 0;
        }

        economy.createAccount(sender.getUniqueId());
        economy.createAccount(target.getUniqueId());

        BigDecimal senderBalance = economy.getBalance(sender.getUniqueId());
        if (senderBalance.compareTo(amount) < 0) {
            messages.send(sender, "money.insufficient-funds",
                    messages.placeholder("balance", formatMoney(senderBalance)),
                    messages.placeholder("amount", formatMoney(amount))
            );
            return 0;
        }

        economy.removeBalance(sender.getUniqueId(), amount);
        economy.addBalance(target.getUniqueId(), amount);

        String formattedAmount = formatMoney(amount);
        messages.send(sender, "money.pay-sent",
                messages.placeholder("player", target.getName()),
                messages.placeholder("amount", formattedAmount)
        );
        messages.send(target, "money.pay-received",
                messages.placeholder("player", sender.getName()),
                messages.placeholder("amount", formattedAmount)
        );
        soundFactory.play(sender, SoundType.PAY);
        soundFactory.play(target, SoundType.PAY);

        return Command.SINGLE_SUCCESS;
    }

    private int handleAdminToggle(CommandSourceStack source, boolean enabled) {
        Player player = getPlayer(source);
        if (player == null) {
            messages.send(source.getSender(), "money.player-only");
            return 0;
        }

        economy.createAccount(player.getUniqueId());
        economy.setAdminMessage(player.getUniqueId(), enabled);
        messages.send(player, enabled ? "money.admin-enabled" : "money.admin-disabled");
        return Command.SINGLE_SUCCESS;
    }

    private Player getPlayer(CommandSourceStack source) {
        if (source.getExecutor() instanceof Player player) {
            return player;
        }

        if (source.getSender() instanceof Player player) {
            return player;
        }

        return null;
    }

    private OfflinePlayer resolveKnownTarget(CommandSourceStack source, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target != null) {
            return target;
        }

        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            String name = offline.getName();
            if (name != null && name.equalsIgnoreCase(targetName)) {
                return offline;
            }
        }

        messages.send(source.getSender(), "player.not-found");
        return null;
    }

    private String resolveTopPlayerName(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String name = player.getName();
        if (name != null && !name.isBlank()) {
            return name;
        }

        String raw = uuid.toString();
        return raw.substring(0, Math.min(8, raw.length()));
    }

    private String resolveTargetName(OfflinePlayer target, String fallback) {
        String name = target.getName();
        return name != null ? name : fallback;
    }

    private BigDecimal parsePositiveAmount(CommandSender sender, String amountInput) {
        try {
            BigDecimal amount = MoneyValidator.validateMoneyAmount(amountInput);
            MoneyValidator.validatePositiveAmount(amount);
            return amount;
        } catch (IllegalArgumentException ex) {
            messages.send(sender, "money.invalid-amount",
                    messages.placeholder("amount", amountInput)
            );
            plugin.getLogger().warning("Invalid money amount from " + sender.getName() + ": " + amountInput + " (" + ex.getMessage() + ")");
            return null;
        }
    }

    private boolean requirePermission(CommandSourceStack source, String permission) {
        if (hasPermission(source, permission)) {
            return true;
        }

        messages.send(source.getSender(), "money.no-permission");
        return false;
    }

    private boolean hasPermission(CommandSourceStack source, String permission) {
        return source.getSender().hasPermission(permission) || source.getSender().hasPermission("kmoney.admin");
    }
}