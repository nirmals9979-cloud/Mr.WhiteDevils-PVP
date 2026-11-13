# Mr.WhiteDevil's PvP — status, build, report (revision 2)

## Honest status
| Item | State |
|---|---|
| Core logic (features, SwapController, PvpController, config) | Compiled, **47/47 unit tests pass** (`./run_core_tests.sh`) |
| Fabric adapter + entrypoint + settings screen | Fixed against 26.x sources (below); **compiles against my own stubs only**; NOT compiled against the real 26.3 jar |
| `Mr.WhiteDevilsPvP.jar` | **NOT built** (sandbox: Java 21, no Gradle, no network, no MC jars) |
| In-game testing | **None** |
| Auto Lunge Swap trigger / Always Shield behaviour from your reference mods | **Not done** — reference JARs/source never provided (both ZIP uploads are identical, 17 files) |

## What was missing from the uploaded ZIP
- Reference JARs/source: Auto Lunge Swap, Always Shield
- Gradle build, Fabric entrypoint, `fabric.mod.json`, resources, lang file, keybinds, settings screen, config persistence, hooks (I added all of these)
- No Minecraft 26.3 jars or Gradle caches (must be downloaded by Loom on your machine)

## Verified against public sources (not by running the game)
| Topic | Finding | Source type |
|---|---|---|
| Loom plugin id | `net.fabricmc.fabric-loom` (non-remap) for MC 26.1+; Loom needs Gradle 9.2+ | Fabric docs / Loom release notes |
| build.gradle shape | `minecraft "com.mojang:minecraft:..."`, `implementation` for loader + fabric-api, `loom_version` from gradle.properties | FabricMC/fabric-example-mod (26.1.2 branch) |
| Java | 25 | 26.x mod release notes |
| Action-bar message | `displayClientMessage` removed in 26.x → `Player.sendOverlayMessage(Component)` (**was a compile error in my first draft; fixed**) | 26.3 port PR, 26.1 port PRs |
| Open a screen | `Minecraft.setScreen/screen` → `minecraft.gui.setScreen()/gui.screen()` since 26.2 (**was a compile error; fixed**) | fabricmc.net 26.2 post |
| Keybinds | `KeyMappingHelper.registerKeyMapping`, `KeyMapping.Category.register(Identifier)`, `consumeClick`, `InputConstants.KEY_*`, lang key `key.category.<ns>.<path>` | Fabric docs "Key Mappings" |
| Tick event | `ClientTickEvents.END_CLIENT_TICK` unchanged (only WORLD→LEVEL ticks renamed) | Fabric API 26.1 porting guide |
| Inventory | `getSelectedSlot()/setSelectedSlot(int)/getItem(int)` | 26.2 mod sources |
| Item ids (26.3) | `wooden/copper/iron/golden/diamond/netherite_spear`, `mace`, `shield`, `trident`; all `*_sword`/`*_axe` | 26.3 item registry listings; covered by tests |

Versions pinned in `gradle.properties`: Loom 1.19.3, Loader 0.20.1, Fabric API 0.163.0+26.3, Gradle 9.8.2.
These each appear in public sources, but **I have not seen this exact combination build for 26.3** — confirm at https://fabricmc.net/develop.

## Still unverified (will fail loudly at compile time if wrong)
`LivingEntity.isBlocking()`, `Minecraft.options.keyAttack`, `MultiPlayerGameMode.useItem(Player, InteractionHand)`,
`AttackEntityCallback` / `UseItemCallback` exact signatures, `Screen.init/onClose/addRenderableWidget`, `Button.builder(..).bounds(..).build()`.

## Build
1. JDK 25 + Gradle 9.x. In this folder: `gradle wrapper --gradle-version 9.8.2`
2. `./gradlew build` → `build/libs/Mr.WhiteDevilsPvP.jar`
3. Mods folder + Fabric Loader 0.19.5+ + Fabric API for 26.3.
4. Core-only tests: `sh run_core_tests.sh` (tests live in core-tests/, outside Gradle on purpose). Stub check: see `tools/stubcheck/README.txt`.
Send me the build log if anything fails and I'll fix it.

## Feature wiring audit
| # | Feature | Controller entry | Fabric caller | Status |
|---|---|---|---|---|
| 1 | Auto Lunge Swap | `onLungeWindow()` | **none** | Logic tested; **trigger missing** (needs reference mod) |
| 2 | Always Shield | `onShieldKey()` | H key | Wired; only selects shield, no block-hold (reference needed) |
| 3 | Auto Stun Slam | `onAttackAttempt()` | `AttackEntityCallback` | Wired |
| 4 | Wind Charge Swap | `onWindChargeKey()`, `onItemUsed()` | G key, `UseItemCallback` | Wired |
| 5 | Automatic Return | `tick()` schedule | END_CLIENT_TICK | Wired |
| 6 | Emergency Return | `onEmergencyKey()` | R key | Wired |
| 7 | Custom Swap Delay | `applySettings()` | settings screen | Wired |
| 8 | Action Notifications | `notifications.show()` | `sendOverlayMessage` | Wired |
| 9 | Settings Menu | — | RShift → `PvpScreen`; `ConfigStore` | Wired, screen never opened |
| 10 | Smart Weapon Detection | `detection.find()` | `FabricClientAccess.hotbar()` | Wired; id-based only |

Game-tested: **none of the 10.**
Note: many servers prohibit automated hotbar swapping.

## Build without installing anything (GitHub Actions)
1. Create a new GitHub repo and upload the **contents of the ZIP at the repo root** (`build.gradle`, `.github/`, `src/` must be at the top level, not inside another folder).
2. Open the repo's **Actions** tab, run **Build Mr.WhiteDevilsPvP** (or it runs on push).
3. When it finishes, download the **Mr.WhiteDevilsPvP** artifact. If it fails, send me the log.
