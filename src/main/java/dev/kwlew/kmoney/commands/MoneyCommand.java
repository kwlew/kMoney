package dev.kwlew.kmoney.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.kwlew.kmoney.economy.api.EconomyService;
import dev.kwlew.kmoney.economy.utils.Formatter;
import dev.kwlew.kmoney.economy.utils.MoneyValidator;
import dev.kwlew.kmoney.kernel.Inject;
import dev.kwlew.kmoney.managers.MessageManager;
import dev.kwlew.kmoney.managers.config.ConfigManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.List;

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
 * - /money reload - Admin: Reload plugin configuration (requires permission)
 * <p>
 * All monetary amounts support suffixes: k (thousand), m (million), b (billion), t (trillion), etc.
 */
public final class MoneyCommand extends BaseCommand {

    private final NamespacedKey key;

    @Inject
    public MoneyCommand(JavaPlugin plugin,
                        EconomyService economy,
                        MessageManager messages,
                        ConfigManager config) {
        super(plugin, economy, messages, config);
        this.key = new NamespacedKey(plugin, "money");
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
                    if (!requirePermission(ctx.getSource(), "kmoney.command.money")) {
                        return 0;
                    }

                    sendSelfBalance(ctx.getSource());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("balance")
                        .executes(ctx -> {
                            if (!requirePermission(ctx.getSource(), "kmoney.command.money")) {
                                return 0;
                            }

                            sendSelfBalance(ctx.getSource());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes(ctx -> {
                                    if (!requirePermission(ctx.getSource(), "kmoney.command.money")) {
                                        return 0;
                                    }

                                    Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource())
                                            .getFirst();
                                    sendBalance(ctx.getSource().getSender(), target);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal("reload")
                        .requires(source -> hasPermission(source, "kmoney.command.money.reload"))
                        .executes(ctx -> {
                            if (!requirePermission(ctx.getSource(), "kmoney.command.money.reload")) {
                                return 0;
                            }

                            config.reloadAll();
                            messages.reload();
                            messages.send(ctx.getSource().getSender(), "money.reloaded");
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("pay")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.argument("amount", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            List<String> suggestions = List.of("100", "1k", "5k", "10k", "100k");
                                            suggestions.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            if (!requirePermission(ctx.getSource(), "kmoney.command.money.pay")) {
                                                return 0;
                                            }

                                            Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                                    .resolve(ctx.getSource())
                                                    .getFirst();
                                            return handlePay(ctx.getSource(), target, StringArgumentType.getString(ctx, "amount"));
                                        }))))
                .then(Commands.literal("add")
                        .requires(source -> hasPermission(source, "kmoney.command.money.add"))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        builder.suggest(player.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            List<String> suggestions = List.of("100", "1k", "5k", "10k", "100k");
                                            suggestions.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            if (!requirePermission(ctx.getSource(), "kmoney.command.money.add")) {
                                                return 0;
                                            }

                                            Player target = resolveOnlineTarget(
                                                    ctx.getSource(),
                                                    StringArgumentType.getString(ctx, "target")
                                            );
                                            if (target == null) {
                                                return 0;
                                            }

                                            String amount = StringArgumentType.getString(ctx, "amount");
                                            return handleAdd(ctx.getSource(), target, amount);
                                        }))))
                .then(Commands.literal("remove")
                        .requires(source -> hasPermission(source, "kmoney.command.money.remove"))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        builder.suggest(player.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            List<String> suggestions = List.of("100", "1k", "5k", "10k", "100k");
                                            suggestions.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            if (!requirePermission(ctx.getSource(), "kmoney.command.money.remove")) {
                                                return 0;
                                            }

                                            Player target = resolveOnlineTarget(
                                                    ctx.getSource(),
                                                    StringArgumentType.getString(ctx, "target")
                                            );
                                            if (target == null) {
                                                return 0;
                                            }

                                            String amount = StringArgumentType.getString(ctx, "amount");
                                            return handleRemove(ctx.getSource(), target, amount);
                                        }))))
                .then(Commands.literal("set")
                        .requires(source -> hasPermission(source, "kmoney.command.money.set"))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        builder.suggest(player.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            List<String> suggestions = List.of("100", "1k", "5k", "10k", "100k");
                                            suggestions.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            if (!requirePermission(ctx.getSource(), "kmoney.command.money.set")) {
                                                return 0;
                                            }

                                            Player target = resolveOnlineTarget(
                                                    ctx.getSource(),
                                                    StringArgumentType.getString(ctx, "target")
                                            );
                                            if (target == null) {
                                                return 0;
                                            }
                                            String amount = StringArgumentType.getString(ctx, "amount");
                                            return handleSet(ctx.getSource(), target, amount);
                                        }))))
                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    List<String> suggestions = List.of("100", "1k", "5k", "10k", "100k");
                                    suggestions.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (!requirePermission(ctx.getSource(), "kmoney.command.money.withdraw")) {
                                        return 0;
                                    }

                                    String amount = StringArgumentType.getString(ctx, "amount");
                                    return handleWithdraw(ctx.getSource(), amount, null);
                                })
                                .then(Commands.argument("notes", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            List<String> suggestions = List.of("2", "4", "8", "16");
                                            suggestions.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            if (!requirePermission(ctx.getSource(), "kmoney.command.money.withdraw")) {
                                                return 0;
                                            }

                                            String amount = StringArgumentType.getString(ctx, "amount");
                                            String notes = StringArgumentType.getString(ctx, "notes");
                                            return handleWithdraw(ctx.getSource(), amount, notes);
                                        }))))
                .build();
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

        amount = parsePositiveAmount(source.getSender(), amountInput);
        if (amount == null) {
            return 0;
        }

        BigDecimal playerBalance = economy.getBalance(player.getUniqueId());
        BigDecimal total = amount.multiply(BigDecimal.valueOf(notes));

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

        ItemStack paper = new ItemStack(Material.PAPER, notesAmount);
        ItemMeta meta = paper.getItemMeta();

        String symbol = config.getCurrencySymbol();
        BigDecimal total = amount.multiply(BigDecimal.valueOf(notesAmount));
        String formatted = Formatter.format(total, symbol);

        meta.displayName(
            messages.getRaw("check.create-name")
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(java.util.List.of(
                messages.getRaw("check.create-value", messages.placeholder("amount", formatted))
                        .decoration(TextDecoration.ITALIC, false),
                messages.getRaw("check.create-creator", messages.placeholder("player", source.getSender().getName()))
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, amount.toPlainString());

        paper.setItemMeta(meta);

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        player.getInventory().addItem(paper);
        economy.removeBalance(player.getUniqueId(), total);

        messages.send(source.getSender(), "check.success", messages.placeholder("amount", formatted));

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

    private int handleSet(CommandSourceStack source, Player target, String amountInput) {
        BigDecimal amount = parsePositiveAmount(source.getSender(), amountInput);
        if (amount == null) {
            return 0;
        }

        economy.setBalance(target.getUniqueId(), amount);
        String formattedAmount = formatMoney(amount);

        messages.send(source.getSender(), "money.set",
                messages.placeholder("player", target.getName()),
                messages.placeholder("amount", formattedAmount));

        messages.send(target, "money.got-set",
                messages.placeholder("player", source.getSender().getName()),
                messages.placeholder("amount", formattedAmount)
        );

        return Command.SINGLE_SUCCESS;
    }

    private int handleRemove(CommandSourceStack source, Player target, String amountInput) {

        BigDecimal amount = parsePositiveAmount(source.getSender(), amountInput);
        if (amount == null) {
            return 0;
        }

        economy.removeBalance(target.getUniqueId(), amount);
        String formattedAmount = formatMoney(amount);

        messages.send(source.getSender(), "money.removed",
                messages.placeholder("player", target.getName()),
                messages.placeholder("amount", formattedAmount));

        messages.send(target, "money.got-removed",
                messages.placeholder("player", source.getSender().getName()),
                messages.placeholder("amount", formattedAmount)
        );

        return Command.SINGLE_SUCCESS;
    }

    private int handleAdd(CommandSourceStack source, Player target, String amountInput) {

        BigDecimal amount = parsePositiveAmount(source.getSender(), amountInput);
        if (amount == null) {
            return 0;
        }

        economy.addBalance(target.getUniqueId(), amount);
        String formattedAmount = formatMoney(amount);
        messages.send(source.getSender(), "money.added",
                messages.placeholder("player", target.getName()),
                messages.placeholder("amount", formattedAmount)
        );
        messages.send(target, "money.got-added",
                messages.placeholder("player", source.getSender().getName()),
                messages.placeholder("amount", formattedAmount)
        );

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

    private Player resolveOnlineTarget(CommandSourceStack source, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target != null) {
            return target;
        }

        messages.send(source.getSender(), "player.not-found");
        return null;
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