# kMoney

`kMoney` is a Paper plugin that provides a full `/money` economy command with:

- BigDecimal-based balances (precision-safe money math)
- Player-to-player payments
- Admin add/remove/set tools
- Withdrawable paper checks that players can redeem
- PlaceholderAPI support

## Features

- **Accurate economy math:** uses `BigDecimal` instead of floating-point.
- **Readable number formatting:** supports compact suffixes up to `UDC` (`1e36`), e.g. `1.02K`, `1M`, `3.5NO`.
- **Granular permissions:** each subcommand has its own permission node.
- **Command visibility by permission:** restricted subcommands are hidden from users without access.
- **Check items:** create money checks with `/money withdraw`, redeem by right-clicking.

## Requirements

- Paper API: `26.1.2`
- Java toolchain: `25`
- Optional plugins:
  - PlaceholderAPI
  - Vault

## Installation

1. Donwload the plugin and place it in your /plugins folder

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/money` | Show your balance | `kmoney.command.money` |
| `/money balance [player]` | Show balance (self/other) | `kmoney.command.money` |
| `/money pay <player> <amount>` | Send money to a player | `kmoney.command.money.pay` |
| `/money withdraw <amount> [notes]` | Create check item(s) | `kmoney.command.money.withdraw` |
| `/money add <player> <amount>` | Add money to a player | `kmoney.command.money.add` |
| `/money remove <player> <amount>` | Remove money from a player | `kmoney.command.money.remove` |
| `/money set <player> <amount>` | Set a player balance | `kmoney.command.money.set` |
| `/money reload` | Reload config/messages | `kmoney.command.money.reload` |

`kmoney.admin` grants all of the above.

## Amount Input

You can use plain numbers or suffixes (case-insensitive):

- `k`, `m`, `b`, `t`, `q`, `qq`, `s`, `ss`, `oc`, `no`, `dc`, `udc`

Examples:

- `1000`
- `1.25k`
- `5m`
- `2udc`

## Placeholders (PlaceholderAPI)

- `%kMoney_balance%` - raw balance
- `%kMoney_balance_formatted%` - formatted balance with symbol/suffix

## Configuration

`src/main/resources/config.yml`

- `symbol` - currency symbol (default: `$`)
- `default-balance` - starting balance for new accounts

`src/main/resources/messages.yml`

- All user-facing messages, including check text and command feedback.

