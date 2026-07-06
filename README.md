# Intangible
[![简体中文](https://img.shields.io/badge/如果你看不懂英文-请点击这里-red)](README_CN.md)

When you have finally built a huge factory, a complex pipe network, or a deeply nested multiblock machine, only to find that one block, pipe, or interface is buried at the very center, do you really want to tear the whole machine apart just to adjust it?

Maybe you only want to slip into the gaps between buildings, production lines, and machines, fix that one small issue, and get back out without breaking anything unrelated. Wouldn't it be useful to move like a ghost for a short while?

**Intangible** is made for exactly that situation.

Inspired by the name of the Intangible buff from *Slay the Spire*, this mod adds a simple but powerful status effect: **Intangible**.

While a player has the Intangible effect, they can slowly fly and pass through walls like a ghost, making it much easier to inspect and modify the inside of large buildings, automated machines, and complex structures.

The mod also recreates the damage-reduction behavior of the Intangible buff from *Slay the Spire*: players can reduce most incoming damage to 1 at the cost of effect duration. This behavior can be disabled in the config.

## Damage Reduction

The main purpose of Intangible is wall phasing, flight, and structure maintenance. Damage reduction is only an extra feature, and it is **configurable**.

By default, the mod enables Intangible behavior similar to *Slay the Spire*:

- Most damage above 1 is reduced to 1
- Each successful damage reduction consumes Intangible effect duration
- The default cost is `600` ticks, or 30 seconds
- If the remaining duration is not enough, the effect is removed

If you only want flight and wall phasing without damage reduction, disable it in `intangible-server.toml`:

```toml
# intangible-server.toml
[features]
    isSlayTheSpire = false
```

Or change the duration cost:

```toml
# intangible-server.toml
[features]
    intangibleDurationCostOnDamage = 823
```

The mod also provides two `damage_type` tags to further control how Intangible interacts with damage types:

- `intangible:bypasses_intangible`: damage types in this tag bypass the Intangible effect and are not handled by it.
- `intangible:intangible_immune_to`: damage types in this tag are directly ignored while Intangible is active. By default, it includes `minecraft:in_wall` to prevent suffocation damage while phasing through walls.

## Potions and Brewing

The mod adds two potions:

- Potion of Intangibility
- Long Potion of Intangibility

Potion brewing is fixed in this Forge 1.20.1 build:

- Awkward Potion + Ender Eye -> Potion of Intangibility
- Potion of Intangibility + Redstone -> Long Potion of Intangibility

## Rendering

If you do not want the translucent overlay on Intangible players, or if you want to change its color and transparency, you can configure it in `intangible-client.toml`.

If you want to change the overlay texture, add `assets/intangible/textures/misc/intangible_overlay.png` in a resource pack.
