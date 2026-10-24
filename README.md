# Create: Torque Rails

A Forge 1.20.1 addon for [Create](https://www.curseforge.com/minecraft/mc-mods/create) and [Steam 'n' Rails](https://www.curseforge.com/minecraft/mc-mods/create-steam-n-rails) that makes trains draw real rotational power to move. Build a Train Engine into a train, feed it rotation, and its RPM sets the train's top speed; every carriage adds stress, so longer trains need bigger power plants.

## Requirements

- Java 17
- Minecraft 1.20.1 / Forge `47.4.10`+
- Create `6.0.7` – `6.1.0`
- Steam 'n' Rails `1.7.0`+

Exact version ranges live in `gradle.properties`.

## Building

```bash
./gradlew build
```

The output jar lands in `build/libs/`.

To build and drop the jar straight into a mods folder (and clear out any previous build of this mod from it first):

```bash
./build.sh /path/to/.minecraft/mods
```

Run `./build.sh` with no argument to just build without copying anywhere.

## Running in dev

```bash
./gradlew runClient
./gradlew runServer
```

Run config/configs live under `run/`.

## Project layout

```
src/main/java/me/kmathers/createtorquerails/
├── engine/        Train Engine block, block entity, RPM/stress logic (TrainPower)
├── simulation/     Keeps contraption-mounted machinery (boilers, pumps, basins, etc.) ticking while a train moves
├── assembly/      Hooks fluid source blocks into trains on assembly
├── mixin/         Create/Railways mixins, contraption + train entity hooks
├── network/       Client/server sync packets
├── client/        Rendering, goggle overlay, contraption targeting
│   └── ponder/    Ponder scenes for in-game documentation
└── compat/        Optional integration with other Create addons (e.g. Create Additional)
```

Config options (`TorqueRailsConfig`): `stressPerCarriage` (SU per RPM per carriage) and `fullSpeedRPM` (engine RPM for full top speed).

## Commands

| Command                                           | Permission                 | Description                                                                                                                                                                                                                                                     |
| ------------------------------------------------- | -------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `/createtorquerails toggle` (alias `/ctr toggle`) | `createtorquerails.toggle` | Toggles the engine requirement for all trains. While it's off, every train runs at full speed whether or not it has a powered engine, which is handy for rescuing a train that can't move. It isn't saved, so it switches back on after a server/world restart. |

By default the permission is granted to ops (permission level 2) and the server console. It's registered with Forge's permission API, so a permissions mod such as LuckPerms can grant it to other players. Commands are registered in `TorqueRailsCommands`.

## Mixins

Mixins target SRG names, not MCP/intermediary names, since Forge remaps at runtime using official mappings but mixins are applied pre-remap. When adding or editing a mixin, grab the SRG name from the relevant mapped class via the IDE's "show MCP/SRG name" tooling, or check an existing mixin in `mixin/` targeting the same class for the pattern.

## Debugging

Gameplay/simulation issues are generally easiest to diagnose from `run/logs/debug.log` rather than stepping through a debugger, the mod logs engine RPM, stress, and simulation state transitions there.

## Contributing

Issues and PRs are welcome at the [issue tracker](https://github.com/KohanMathers/CreateTorqueRails/issues).

- Keep the scope casual: this mod is about a simple speed-is-limited-by-torque mechanic, not a physics simulation. If a change pulls toward Create: Sable-style physics simulation, it's probably out of scope, raise it as a discussion first.
- Match the existing code style.
- If you change mixin targets, double check you're using SRG names.
- Test against the Create/Railways version ranges in `gradle.properties`, not just whatever version you have installed.

## License

MIT — see [`LICENSE.txt`](LICENSE.txt).
