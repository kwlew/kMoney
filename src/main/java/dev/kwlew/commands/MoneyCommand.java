package dev.kwlew.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.kwlew.economy.utils.MoneyParser;
import dev.kwlew.kernel.Inject;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class MoneyCommand extends BaseCommand {

    @Inject
    public MoneyCommand(JavaPlugin plugin,
                        dev.kwlew.economy.api.EconomyService economy,
                        dev.kwlew.managers.MessageManager messages,
                        dev.kwlew.managers.config.ConfigManager config) {
        super(plugin, economy, messages, config);
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
                    sendSelfBalance(ctx.getSource());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("balance")
                        .executes(ctx -> {
                            sendSelfBalance(ctx.getSource());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .executes(ctx -> {
                                    Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource())
                                            .getFirst();
                                    sendBalance(ctx.getSource().getSender(), target);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal("pay")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.argument("amount", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            List<String> suggestions = List.of("100", "1k", "5k", "10k", "100k");
                                            suggestions.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                                    .resolve(ctx.getSource())
                                                    .getFirst();
                                            return handlePay(ctx.getSource(), target, StringArgumentType.getString(ctx, "amount"));
                                        }))))
                .then(Commands.literal("add")
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
                                            String name = StringArgumentType.getString(ctx, "target");
                                            Player target = Bukkit.getPlayerExact(name);

                                            if (target == null) {
                                                messages.send(ctx.getSource().getSender(), "player.not-found");
                                                return 0;
                                            }

                                            String amount = StringArgumentType.getString(ctx, "amount");
                                            return handleAdd(ctx.getSource(), target, amount);
                                        })
                                )
                        )
                )
                .build();
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

    private int handleAdd(CommandSourceStack source, Player target, String amountInput) {

        final double amount;

        try {
            amount = MoneyParser.parse(amountInput);
        } catch (NumberFormatException e) {
            messages.send(source.getSender(), "money.invalid-amount",
                    messages.placeholder("amount", amountInput)
            );
            return 0;
        }

        if (amount <= 0) {
            messages.send(source.getSender(), "money.invalid-amount",
                    messages.placeholder("amount", amountInput)
            );
            return 0;
        }

        economy.addBalance(target.getUniqueId(), amount);
        String formattedAmount = formatMoney(amount);
        messages.send(source.getSender(), "money.pay-sent",
                messages.placeholder("player", target.getName()),
                messages.placeholder("amount", formattedAmount)
        );
        messages.send(target, "money.pay-received",
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

        final double amount;
        try {
            amount = MoneyParser.parse(amountInput);
        } catch (NumberFormatException ex) {
            messages.send(sender, "money.invalid-amount",
                    messages.placeholder("amount", amountInput)
            );
            return 0;
        }

        if (amount <= 0) {
            messages.send(sender, "money.invalid-amount",
                    messages.placeholder("amount", amountInput)
            );
            return 0;
        }

        economy.createAccount(sender.getUniqueId());
        economy.createAccount(target.getUniqueId());

        double senderBalance = economy.getBalance(sender.getUniqueId());
        if (senderBalance < amount) {
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
}
