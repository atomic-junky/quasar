# Quasar

A server-side Fabric mod for managing proxy connections using portals, mannequins and commands.

## Requirements

- Servers running on fabric
- [Velocity](https://papermc.io/software/velocity/) proxy with `bungee-plugin-message-channel = true` in `velocity.toml`
- [FabricProxy-Lite](https://modrinth.com/mod/fabricproxy-lite) on each server

## Configuration

> [!IMPORTANT]
> You need to setup FabricProxy-Lite for each of your fabric servers for it to work!

`config/quasar.json`

```json
{
  "servers": {
    //"<command_name>": "<server_name>"
    "lobby": "lobby"
  },
  "joinBehaviour": "NONE"
}
```

**joinBehaviour** controls where players are teleported when they join.
- `NONE` — no teleportation
- `WORLD_SPAWN` — teleport to the world spawn point
- `PLAYER_SPAWN` — teleport to the player's individual spawn point

## Commands

You can summon mannequins that send players to another server when clicked.

> [!NOTE]
> Quasar's mannequins can be modified in the same way as vanilla mannequins.

```
/quasar mannequin summon <name> <server> [<pos>] [<nbt>]
/quasar mannequin kill <uuid>
/quasar mannequin list
/quasar mannequin info <uuid>
/quasar mannequin edit <uuid> server <server>
/quasar mannequin edit <uuid> name <name>
/quasar mannequin edit <uuid> pos <pos>
/quasar mannequin edit <uuid> yaw <degrees>
```

And you can set up portals are invisible zone that send player to another server when they walk through.

> [!NOTE]
> Quasar's portals block other portal teleportation inside their zone.

```
/quasar portal create <server> [<pos1>] [<pos2>]
/quasar portal remove <id>
/quasar portal list
/quasar portal info <id>
/quasar portal edit <uuid> server <server>
/quasar portal edit <uuid> pos1 <pos>
/quasar portal edit <uuid> pos2 <pos>
```