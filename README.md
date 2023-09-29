# BattleShipsClient
This is a reference client for the [BattleShipsClient](https://github.com/gerwintrip/BattleShipsClient) that connects to the [BattleShips](https://github.com/AppleSaph/BattleShips) server. Both of those projects were made as an extra assignment for school and to showcase what is possible with the BattleShipClient, I decided to make a Minecraft implementation of it. 

This project was made by forking the client repository, which is not the intended way to make a client implementation. The correct way to do this is to download the client as a jar file and to add it as a module dependency to your project. Since we haven't heard back from our lecturer about the extra assignment, I have not taken the time to remake and improve this project.

## Installtion
1. Set up the BattleShips server. See instructions for that [here](https://github.com/AppleSaph/BattleShips#installation).
2. Set up a Spigot Minecraft server locally. Find more about that [here](https://www.spigotmc.org/wiki/spigot-installation).
3. Compile this project using Maven.
4. Move the compiled jar into the plugins directory of your Spigot Minecraft server.
5. Start the server.
6. Edit the config file. Put in the host of the BattleShips server and the port that it is running on.
7. Restart the server.

## How to play
1. Join the Minecraft server. (do not leave the server, it will break 😔)
2. Wait for the host to start the game.
3. Click on blocks to start guessing tiles.
4. That's it. Now repeat step 3 until someone has won.
