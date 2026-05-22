[![Modrinth Version](https://img.shields.io/modrinth/v/kmoney?style=for-the-badge&color=purple)](https://modrinth.com/plugin/kmoney)
[![Downloads](https://img.shields.io/modrinth/dt/kmoney?style=for-the-badge&color=white)](https://modrinth.com/plugin/kmoney)
![Build](https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge)

![Players](https://img.shields.io/bstats/players/31019?style=for-the-badge&color=pink)
![Servers](https://img.shields.io/bstats/servers/31019?style=for-the-badge&color=cyan)
# kMoney

### `kMoney` is a Minecraft plugin that provides an **economy**.

- BigDecimal-based balances (precision-safe money math)
- Player-to-player payments
- Admin add/remove/set tools
- Withdrawable paper checks that players can redeem
- **PlaceholderAPI** support
- **Vault** support

## Features

- **Accurate economy math:** uses `BigDecimal` instead of floating-point.
- **Readable number formatting:** supports compact suffixes up to `UDC` (`1e36`), e.g. `1.02K`, `1M`, `3.5NO`.
- **Granular permissions:** each subcommand has its own permission node.
- **Command visibility by permission:** restricted subcommands are hidden from users without access.
- **Check items:** create money checks with `/money withdraw`, redeem by right-clicking.

## Requirements

- Supported Versions: 26.1, 26.1.x, 1.21.4-1.21.11
- Java toolchain: `25`
- Optional plugins:
  - PlaceholderAPI
  - Vault (VaultUnlocked/Vault2)

## Installation

1. Download the plugin and place it in your /plugins folder

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/money` | Show your balance | `kmoney.command.money` |
| `/money balance [player]` | Show balance (self/other) | `kmoney.command.money` |
| `/money pay <player> <amount>` | Send money to a player | `kmoney.command.money.pay` |
| `/money withdraw <amount\|all> [notes]` | Create check item(s) (`all` withdraws full balance into one note) | `kmoney.command.money.withdraw` |
| `/money admin <on\|off>` | Toggle your admin join message | `kmoney.command.money.admin` |
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
- `enable-join-message` - The message admins get when joining the server.
- `update-warning` - Warns about missing updates.
- `top-update-interval-seconds` - How often /money top refreshes its cached list.

`src/main/resources/messages.yml`

- All user-facing messages, including check text and command feedback.
