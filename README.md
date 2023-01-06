# Macrocosm
[![wakatime](https://wakatime.com/badge/user/4f3de2e1-f9cb-4480-9047-74ecccf9f9c0/project/9b9055db-2125-49ba-ab9e-ae424a54a0af.svg)](https://wakatime.com/badge/user/4f3de2e1-f9cb-4480-9047-74ecccf9f9c0/project/9b9055db-2125-49ba-ab9e-ae424a54a0af)

🎉 Open source 🎉

# Completion Status
Macroocsm is far from completed and is not ready for production use. If you want to try out current features of Macrocosm you can compile it yourself.

## Running Macrocosm locally
It's really not that hard.

**NOTE:** all the steps of this tutorial were tested on a Linux system, but I don't really see a reason why it should not work on Windows/macOS.

### Prerequisites
1. Java 17 JDK (I use Temurin)
2. A PostgreSQL server
3. Like 10 minutes you are willing to spend looking at gradle compiling kotlin

### Actual compilation steps:

1. Clone the repository
```sh
git clone https://github.com/Maxuss/Macrocosm && cd Macrocosm
```

2. Compile the JAR file
Make sure to use JDK for Java 17
```sh
./gradlew reobfJar
```
Two jar files will appear in `build/libs` directory, you want the one, titled something like `Macrocosm-<version>.jar`, and not `Macrocosm-<version>-dev.jar`.

3. Setting up the PostgreSQL database

You will need an empty database.
```sql
CREATE DATABASE macrocosm;
```

Remember the credentials, we will need them later.

4. Getting the dependencies for Macrocosm

Macrocosm really requires only two dependency jars that it does not pull automatically: [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) and [LibsDisguises](https://github.com/libraryaddict/LibsDisguises).

5. Running a server with Macrocosm

**Macrocosm requires Paper 1.19.3**. So don't use Spigot or Bukkit.
Put previously obtained `Macrocosm-<version>.jar` into the `plugins` folder, as well as `ProtocolLib` and `LibsDisguises`.

**Launch the server with SQL parameters:**
```
-Dmacrocosm.postgres.remote=<url of the remote (usually localhost)> -Dmacrocosm.postgres.user=<postgres username> -Dmacrocosm.postgress.pass=<postgres pasword>
```

6. Fill out the configs

The first run the server is probably going to error out, so you need to fill the configs.

`plugins/Macrocosm/config.yml`:
```yaml
connections:
    discord-bot-token: string <AUTH TOKEN OF THE DISCORD BOT>
    discord:
        communication-channel: int <DISCORD CHANNEL ID TO BROADCAST SERVER CHAT (optional)>
        communication-webhook: string <DISCORD WEBHOOK THAT BROADCASTS THE SERVER CHAT (optional)>
        media-channel: null # legacy field, unused now
        guild-id: int <YOUR DISCORD SERVER ID>
game:
    sandbox: bool <WHETHER THE GAME IS IN SANDBOX MODE> 
```

After that you can launch the server. Do note, that not using the discord features may give you tons of errors in the console, it was not tested yet.
