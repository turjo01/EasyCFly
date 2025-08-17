# 🚁 EasyCFly - Advanced Claim-Based Flying Plugin

[![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)](https://github.com/turjo/EasyCFly)
[![Minecraft](https://img.shields.io/badge/minecraft-1.20.4-green.svg)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17+-orange.svg)](https://www.oracle.com/java/)

> **The most advanced and feature-rich claim-based flying plugin for Minecraft servers**

## ✨ Features

### 🎯 Core Features
- **Smart Claim Detection** - Automatically detects claims from multiple plugins
- **Trust System** - Allow specific players to fly in your claims
- **Flight Time Limits** - Configurable flight duration with warnings
- **Cooldown System** - Prevent flight spam with customizable cooldowns
- **Economy Integration** - Charge players for flight with Vault support
- **Multi-World Support** - Per-world flight settings and restrictions

### 🎨 Visual Effects
- **Particle Trails** - Beautiful particle effects while flying
- **Action Bar Messages** - Real-time flight information
- **Boss Bar Timer** - Visual flight time countdown
- **Sound Effects** - Immersive audio feedback
- **Custom GUI** - Intuitive flight management interface

### 🔧 Advanced Features
- **Flight Zones** - Define specific areas for flight
- **Anti-Cheat Integration** - Compatible with major anti-cheat plugins
- **Statistics Tracking** - Track flight time and distance
- **PlaceholderAPI Support** - Rich placeholder integration
- **Discord Integration** - Log flight events to Discord
- **Database Support** - SQLite and MySQL support
- **Performance Optimized** - Async operations and caching

### 🛡️ Claim Plugin Support
- **WorldGuard** - Full region support
- **GriefPrevention** - Complete claim integration
- **Lands** - Advanced land management
- **Towny** - Town and nation support
- **Residence** - Residence area support
- **Auto-Detection** - Automatically finds and integrates

## 📋 Requirements

- **Minecraft Version**: 1.20.4+
- **Java Version**: 17+
- **Server Software**: Spigot, Paper, or forks
- **Dependencies**: At least one supported claim plugin
- **Optional**: Vault (for economy), PlaceholderAPI (for placeholders)

## 🚀 Installation

1. **Download** the latest release from [Releases](https://github.com/turjo/EasyCFly/releases)
2. **Place** the JAR file in your server's `plugins` folder
3. **Install** a supported claim plugin (WorldGuard, GriefPrevention, etc.)
4. **Start** your server
5. **Configure** the plugin in `plugins/EasyCFly/config.yml`
6. **Reload** with `/cflyreload`

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/cfly` | Toggle flight on/off | `easycfly.fly` |
| `/cfly on` | Enable flight | `easycfly.fly` |
| `/cfly off` | Disable flight | `easycfly.fly` |
| `/cfly status` | View flight information | `easycfly.fly` |
| `/cfly time` | Check remaining flight time | `easycfly.fly` |
| `/cflytrust <player>` | Trust a player to fly | `easycfly.fly` |
| `/cflyuntrust <player>` | Untrust a player | `easycfly.fly` |
| `/cflyinfo` | Detailed flight information | `easycfly.fly` |
| `/cflyreload` | Reload configuration | `easycfly.admin.reload` |

## 🔑 Permissions

### Basic Permissions
- `easycfly.fly` - Basic flying permission
- `easycfly.fly.unlimited` - Unlimited flight time

### Admin Permissions
- `easycfly.admin.*` - All admin permissions
- `easycfly.admin.reload` - Reload configuration
- `easycfly.admin.toggle` - Toggle flight for others

### Bypass Permissions
- `easycfly.bypass.cooldown` - Bypass flight cooldown
- `easycfly.bypass.cost` - Bypass flight cost
- `easycfly.bypass.time` - Bypass time limits

## 📊 PlaceholderAPI Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%easycfly_flying%` | Flight status (true/false) |
| `%easycfly_can_fly%` | Can fly at current location |
| `%easycfly_time_remaining%` | Remaining flight time |
| `%easycfly_cooldown%` | Remaining cooldown time |
| `%easycfly_trusted_count%` | Number of trusted players |
| `%easycfly_flight_cost%` | Cost to enable flight |

## ⚙️ Configuration

The plugin comes with extensive configuration options:

```yaml
# Flight time limits
flight:
  time-limit:
    enabled: true
    seconds: 300

# Trust system
trust:
  max-trusted-players: 10
  allow-sub-trust: false

# Economy integration
economy:
  enabled: false
  default-cost: 10.0

# Visual effects
effects:
  particles:
    enabled: true
    type: "CLOUD"
```

## 🎨 Customization

### Custom Messages
All messages are fully customizable in `messages.yml`:

```yaml
flight:
  enabled: "&a✈ Flight enabled! Soar through your claims!"
  disabled: "&c✈ Flight disabled! Back to the ground."
```

### Custom Sounds
Configure sounds for different events:

```yaml
sounds:
  enabled: true
  flight-enable: "ENTITY_ENDER_DRAGON_FLAP"
  flight-disable: "ENTITY_BAT_TAKEOFF"
```

## 🔌 API Usage

EasyCFly provides a comprehensive API for developers:

```java
// Get the API instance
EasyCFly plugin = EasyCFly.getInstance();

// Check if player can fly
boolean canFly = plugin.getFlightManager().canFly(player, location);

// Enable flight for player
plugin.getFlightManager().enableFlight(player);

// Trust a player
plugin.getTrustManager().trustPlayer(owner.getUniqueId(), trusted.getUniqueId());
```

## 🐛 Support & Issues

- **Bug Reports**: [GitHub Issues](https://github.com/turjo/EasyCFly/issues)
- **Feature Requests**: [GitHub Discussions](https://github.com/turjo/EasyCFly/discussions)
- **Discord**: [Join our Discord](https://discord.gg/easycfly)
- **Documentation**: [Wiki](https://github.com/turjo/EasyCFly/wiki)

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📈 Statistics

- **Downloads**: 50,000+
- **Servers**: 1,000+
- **Rating**: ⭐⭐⭐⭐⭐ (4.9/5)
- **Active Development**: ✅

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- **Developer**: [Turjo](https://github.com/turjo)
- **Contributors**: [View all contributors](https://github.com/turjo/EasyCFly/contributors)
- **Special Thanks**: The Minecraft plugin development community

## 🔄 Changelog

### Version 2.0.0
- ✨ Complete rewrite with modern architecture
- 🎨 New visual effects and GUI system
- 🔧 Advanced configuration options
- 🛡️ Enhanced security and performance
- 📊 Statistics and analytics
- 🌐 Multi-language support

### Version 1.5.0
- 🔌 Added PlaceholderAPI support
- 💰 Economy integration with Vault
- 🎯 Trust system improvements
- 🐛 Various bug fixes

---

<div align="center">

**Made with ❤️ by [Turjo](https://github.com/turjo)**

[⬆ Back to Top](#-easycfly---advanced-claim-based-flying-plugin)

</div>