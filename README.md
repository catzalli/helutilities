# HelUtilities - Minecraft Reward Plugin

## Description
HelUtilities is a powerful and flexible Minecraft plugin that adds a customizable reward system for breaking blocks. When players break specific blocks, they can receive various rewards including items, money, and special effects.

## Features
- 🎁 Multiple reward types:
  - Items
  - Money (requires Vault)
  - Commands
- ✨ Special effects:
  - Particles
  - Sounds
  - Fireworks
- 🌾 Smart crop handling (only gives rewards for fully grown crops)
- 💰 Economy integration via Vault
- ⚙️ Fully configurable through config.yml
- 🎯 Chance-based reward system

## Requirements
- Minecraft Server 1.13 or higher
- Vault plugin (for economy features)
- Economy plugin (e.g., EssentialsX) that works with Vault

## Installation
1. Download the latest version of HelUtilities
2. Place the .jar file in your server's plugins folder
3. Start/restart your server
4. Configure the plugin in `plugins/HelUtilities/config.yml`

## Configuration
Example configuration:
prefix: "&8[&6HelUtilities&8] "

messages:
  inventory-full:
    enabled: true
    text: "&eItem's are drop cause !"
  item-reward:
    enabled: true
    text: "&a%amount%x %item% kazandın!"
  money-reward:
    enabled: true
    text: "&a%amount% para kazandın!"
  reward-error:
    enabled: true
    text: "&cÖdül verilirken bir hata oluştu!"
  economy-error:
    enabled: true
    text: "&cPara ödülü verilemedi: Ekonomi sistemi bulunamadı!"

reward-blocks:
  stone:
    rewards:
    - "item 1 5 100"
    - "item stone 5 100"
    - "item minecraft:stone 5 100"
    - "money 125 50"
    - "command 50 summon creeper %x% %y% %z%"
    - "command 25 give %player% minecraft:dirt 3"
    effects:
      - "firework"
      - "particle FLAME"
      - "sound BLOCK_NOTE_BLOCK_PLING"
  wheat:
    rewards:
      - "item gold_ingot 1 70"
    effects:
      - "particle VILLAGER_HAPPY"
      - "sound ENTITY_PLAYER_LEVELUP"

### Reward Format
- Items: `item <material> <amount> <chance>`
- Money: `money <amount> <chance>`
- Commands: `command <chance> <command>`

## Commands
- `/helutilitiesreload` or `/hreload` - Reloads the configuration file
  - Permission: `helutilities.admin`

## Permissions
- `helutilities.admin` - Access to reload command (default: op)

## Support
For support, please create an issue on our GitHub repository.

## License
This plugin is licensed under the MIT License. See the LICENSE file for details.

## DMCA and Legal
- This plugin is an original work and does not contain any copyrighted material from other plugins
- All rights reserved by heliostudios
- You may not sell or redistribute this plugin without explicit permission
- You may use and modify this plugin for your own server

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## Changelog
### Version 1.1.1
- Initial public release
- Added support for crop rewards
- Added effect system
- Implemented economy integration

---
Made with ❤️ by heliostudios
