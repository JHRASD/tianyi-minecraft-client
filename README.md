tianyi-minecraft-client
一个由大语言模型开发Minecraft 1.21.8 客户端模组，项目已暂停，烂在GitHub上吧

折腾几个月实在是累了（给了AI那些方法类，却还是猜项目中并不存在的方法名和类结构，为了纠正AI的一个错误，我需要反复向它提供项目结构、已有类代码，并解释错误信息.
Mixin更是费劲，网上找不到那些映射方法名，只好去复制meteor开源项目的mixin来让ai知道映射方法名...然而还是一堆奇奇怪怪的问题，一个全亮的模块都写不好.）

现在项目就暂时停下罢，如果你看到的话，并且有兴趣，可以下载项目自己玩玩，起码这个客户端能大概的运行嘛

![运行图像](1.png)
![运行图像](2.png)
![运行图像](3.png)
![运行图像](4.png)


TianyiClient/   mc1.21.8版本
├── src/main/java/com/example/tianyiclient/
│   ├── TianyiClient.java                  # 主入口类
│   ├── managers/
│   │   ├── ModuleManager.java            # 模块管理器
│   │   ├── Manager.java                  # 管理器基类
│   │   ├── KeybindManager.java           # 按键绑定管理器
│   │   └── HudManager.java               # HUD管理器（已移动到hud/）
│   ├── modules/
│   │   ├── Module.java                   # 模块基类
│   │   ├── Category.java                 # 分类枚举
│   │   ├── client/
│   │   ├── combat/
│   │   ├    └── AutoTotem.java
│   │   ├── movement/
│   │   │   └── Flight.java              # 飞行模块
│   │   ├── render/
│   │   │   ├── Fullbright.java          # 夜视模块//没用
│   │   │   ├── HUD.java                 # HUD控制模块（原HUDModule.java已删除）
│   │   │   └── EntityInfoModule.java    # 实体信息模块
│   │   ├── player/
│   │   ├── misc/
│   │   ├── test/
│   │   │   ├── DebugModule.java
│   │   │   ├── TestModule.java
│   │   │   └── EventBusTestHudDebugModule.java
│   │   ├── impl/hud/
│   │   │   └── HudEditModule.java       # HUD编辑模块
│   │   └── settings/SettingGroup.java
│   ├── event/
│   │   ├── Event.java                    # 事件基类
│   │   ├── Cancelable.java               # 可取消接口
│   │   ├── Priority.java                 # 优先级枚举
│   │   ├── EventHandler.java             # 事件处理器注解
│   │   ├── EventBus.java                 # 事件总线
│   │   └── events/
│   │       ├── client/
│   │       │   ├── TickEvent.java
│   │       │   ├── KeyEvent.java
│   │       │   ├── MouseEvent.java
│   │       │   ├── GameJoinedEvent.java
│   │       │   └── GameLeftEvent.java
│   │       ├── render/
│   │       │   ├── RenderEvent.java
│   │       │   ├── HudRenderEvent.java
│   │       │   ├── WorldRenderEvent.java
│   │       │   └── NametagRenderEvent.java
│   │       ├── world/
│   │       │   ├── BlockUpdateEvent.java
│   │       │   ├── EntityAddedEvent.java
│   │       │   ├── EntityRemovedEvent.java
│   │       │   └── ChunkDataEvent.java
│   │       ├── network/
│   │       │   ├── PacketEvent.java           // abstract class PacketEvent
│   │       │   ├── PacketReceiveEvent.java    // public class PacketReceiveEvent
│   │       │   ├── PacketSendEvent.java       // public class PacketSendEvent
│   │       │   ├── PacketEventHelper
│   │       │   └── ChatMessageEvent.java
│   │       └── player/
│   │           ├── MoveEvent.java
│   │           ├── JumpEvent.java
│   │           └── DeathEvent.java
│   ├── settings/
│   │   ├── Setting.java                  # 设置基类
│   │   ├── BoolSetting.java              # 布尔设置
│   │   ├── BindSetting.java              # 绑定设置
│   │   ├── ColorSetting.java             # 颜色设置
│   │   ├── DoubleSetting.java            # 双精度设置
│   │   ├── EnumSetting.java              # 枚举设置
│   │   ├── KeybindSetting.java           # 按键绑定设置
│   │   ├── NumberSetting.java            # 数字设置
│   │   ├── IntegerSetting.java           # 整数设置
│   │   ├── StringSetting.java            # 字符串设置
│   │   └── SettingGroup.java             # 设置分组
│   │── utils/
│   │     ├── FullScanInventoryManager.java
│   ├── gui/
│   │   ├── ClickGUI.java                 # 点击GUI
│   │   ├── ClickGuiWindow.java           # GUI窗口
│   │   └── hud/                          # 【新增】HUD编辑器GUI
│   │       └── HudEditorScreen.java     # HUD可视化编辑器
│   └── hud/                              # HUD系统（重构后）
│       ├── HudManager.java               # HUD管理器（实际实现）
│       ├── HudElement.java               # HUD元素基类（已增强）
│       ├── HudRenderer.java              # HUD渲染器（旧的，建议逐渐废弃）
│       ├── elements/                     # HUD元素实现
│       │   ├── WatermarkElement.java    # 水印元素
│       │   ├── FpsElement.java          # FPS显示
│       │   ├── CoordinatesElement.java  # 坐标显示
│       │   ├── ModuleListElement.java   # 模块列表
│       │   ├── PlayerStatusElement.java # 玩家状态
│       │   ├── PerformanceMonitorElement.java # 性能监控
│       │   ├── EnemyStatusElement.java  # 敌人状态
│       │   ├── GameInfoElement.java     # 游戏信息
│       │   ├── NetworkInfoElement.java  # 网络信息
│       │   └── EntityInfoElement.java   # 实体信息
│       ├── binding/                      # 【新增】数据绑定系统
│       │   ├── DataBinding.java         # 数据绑定类
│       │   └── DataProviderRegistry.java # 数据提供器注册表
│       └── config/
│           └── HudConfig.java           # HUD配置
└── mixin/
    ├── ClientPlayerEntityMixin.java
    ├── ClientPlayNetworkHandlerMixin.java//这个是空的
    ├── ExampleMixin.java
    ├── InGameHudMixin.java
    ├── KeyboardMixin.java
    ├── MinecraftClientMixin.java
    └── TitleScreenMixin.java
