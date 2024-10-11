# FallbackServer

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/your-repo-link)

## Overview

FallbackServer is a robust plugin designed to manage player redirection when servers go offline unexpectedly. It ensures that players are routed to fallback servers or hubs instead of being disconnected, providing a seamless experience during server crashes or restarts. FallbackServer supports various proxy types and includes advanced customization features to meet the needs of complex server networks.

## Features

- **Automatic Player Redirection**: Redirect players to pre-defined fallback servers when game servers crash or restart.
- **Customizable Configurations**: Easily set up fallback groups, lobbies, and custom commands through the configuration file.
- **Reconnect Mode**: Choose between fallback or reconnect modes for more flexible player routing.
- **Extensive API**: Includes API methods for easy integration and customization.
- **Multi-Proxy Support**: Works with different proxies, ensuring flexibility across server setups.

## Supported Proxies

- **BungeeCord**: Versions 1.7 - 1.18
- **Waterfall**: Versions 1.8 - 1.21
- **Travertine**: Versions 1.7 - 1.21
- **FlameCord**
- **SSCord**
- **InsaneProxy**

## Getting Started

1. Download the plugin and place it in your `plugins/` folder.
2. Follow the setup instructions for your proxy type (BungeeCord, Velocity, etc.).
3. Configure your `config.yml` file to define fallback groups and lobbies.
4. Restart your proxy and enjoy seamless fallback handling!

For detailed setup instructions, visit our [Wiki](https://fallbackserver-wiki.gitbook.io/).
