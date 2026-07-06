# Intangible
[![English](https://img.shields.io/badge/English-README--EN.md-blue)](README.md)

当你终于搭好了一个巨大的工厂、一套复杂的管线系统，或者一台层层嵌套的多方块机器，却发现某个方块、管道或接口藏在结构最深处时，你真的想为了调整它而拆开整台机器吗？

也许你只想钻进建筑、流水线和机器的缝隙里，改掉那个小小的问题就出来，不破坏任何无关结构，你是否希望自己能短暂地像幽灵一样行动？

**Intangible** 正是为这种场景而生。

这个名称灵感来自《杀戮尖塔》的模组添加了一个简单但强大的状态效果：**无实体**。

当玩家处于无实体状态时，可以像幽灵一样缓慢飞行、穿过墙体，从而更方便地进入大型建筑、自动化机器和复杂结构的内部进行检查与修改。

此外，模组还还原了《杀戮尖塔》中的无实体Buff减伤机制：玩家可以以消耗效果持续时间为代价，将大部分受到的伤害削减至 1 点。此效果可以在配置项中关闭。

## 伤害削减

无实体的主要用途是穿墙、飞行和结构维护。伤害削减只是一个附加功能，并且**可配置**。

默认情况下，模组会启用类似《杀戮尖塔》的无实体逻辑：

- 大部分高于 1 点的伤害会被削减至 1 点
- 每次成功削减伤害都会消耗无实体效果持续时间
- 默认每次消耗 `600` tick，也就是 30 秒
- 如果剩余持续时间不足，效果会被移除

如果你只想保留飞行和穿墙，不想启用伤害削减，可以在`intangible-server.toml`中关闭：
```toml
# intangible-server.toml
[features]
    isSlayTheSpire = false
```

或者修改消耗的持续时间：
```toml
# intangible-server.toml
[features]
    intangibleDurationCostOnDamage = 823
```

模组还提供两个 `damage_type` 标签来进一步控制无实体与伤害类型的交互：
- `intangible:bypasses_intangible`：该标签中的伤害会绕过无实体效果，不会被无实体处理。
- `intangible:intangible_immune_to`：该标签中的伤害会在无实体状态下直接免疫。 默认包含 `minecraft:in_wall`，用于避免玩家穿墙时受到窒息伤害。

## 药水与酿造

模组添加了两种药水：
- 无形药水
- 长效无形药水

Forge 1.20.1 版本中的酿造配方是固定的：
- 粗制药水 + 末影之眼 -> 无形药水
- 无形药水 + 红石 -> 长效无形药水

## 渲染

如果你不想显示无实体玩家的半透明覆盖层或者想修改颜色及其透明度，可以在`intangible-client.toml`中关闭。

如果你想修改覆盖层的纹理，可以在资源包中添加`assets/intangible/textures/misc/intangible_overlay.png`文件。

