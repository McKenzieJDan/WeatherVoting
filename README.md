# WeatherVoting

A Minecraft is a Spigot plugin that allows players to vote on changing the current weather in the Minecraft server. When enough players vote for a specific weather type, the weather will change accordingly.

[![SpigotMC](https://img.shields.io/badge/SpigotMC-WeatherVoting-orange)](https://www.spigotmc.org/resources/weathervoting.122848/)
[![Donate](https://img.shields.io/badge/Donate-PayPal-blue.svg)](https://www.paypal.com/paypalme/mckenzio)

## Features

- 🗳️ Players can vote to change the current weather in the server
- 📊 Configurable voting threshold based on percentage of online players
- ⏱️ Cooldown system prevents spam voting and frequent weather changes
- ⌛ Control how long each weather type lasts after being voted in
- 📢 Broadcast announcements when players vote for weather changes
- 🔮 Weather forecast command to check current weather and time until natural change
- 💬 Fully customizable messages for all plugin text

## Installation

1. Download from [Spigot](https://www.spigotmc.org/resources/weathervoting.122848/) or [GitHub Releases](https://github.com/McKenzieJDan/WeatherVoting/releases)
2. Place the .jar in your server's `plugins` folder
3. Restart your server

## Commands

- `/voteweather` - Shows the current vote status
- `/voteweather sunny` - Vote for sunny weather
- `/voteweather rain` - Vote for rainy weather
- `/voteweather thunder` - Vote for thunderstorm weather
- `/forecast` - View the current weather and when it will change naturally

## Requirements

- Spigot/Paper 1.21.4
- Java 17+

## Development
To build the plugin yourself:

1. Clone the repository
2. Run `mvn clean package`
3. Find the built jar in the `target` folder

## Support
If you find this plugin helpful, consider [buying me a coffee](https://www.paypal.com/paypalme/mckenzio) ☕

## License

[MIT License](LICENSE)

Made with ❤️ by [McKenzieJDan](https://github.com/McKenzieJDan)