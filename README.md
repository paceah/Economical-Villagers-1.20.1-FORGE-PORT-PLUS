# Economical Villager Trading — Forge 1.20.1 Port Plus

> **Manual configuration is required to tune this mod.** Start Minecraft or the server once, stop it, then edit **`config/economical_villager_trading-common.toml`** in the game or server root. The `config` folder is normally next to `mods`, not inside it. The file contains the options; the detailed guide is below. Defaults work without editing, but change the file yourself to choose your balance.

[Русская версия ↓](#русский)

A Forge port and extension of [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading) by SuperSaiyanSubtlety. Selling items to a villager and buying items from one affect demand and can restock the opposite group. This version adds control over prices, partial restocks, volume balancing, temporary lockouts and profession trades.

## Install and build

Use Minecraft **1.20.1**, Forge **47.4.10** (build target) and Java **17**. Put the built mod JAR into the instance's `mods/` folder. Forge generates the configuration on first launch. Back up your world before changing mods or settings. Fabric API, MidnightLib and Mod Menu are not required.

To build, clone or fork the repository, install JDK 17, set `JAVA_HOME`, and run:

```sh
# Windows
.\gradlew.bat build
# Linux / macOS
./gradlew build
```

Gradle downloads dependencies on the first build. The normal mod JAR is in `build/libs/`. Import the project as a Gradle project in an IDE; `./gradlew runClient` and `./gradlew runServer` start development instances in `run/` (accept the server EULA when prompted).

## Configuration guide

1. Start the game/server once with the mod, then stop it. Open `config/economical_villager_trading-common.toml` with a UTF-8 text editor.
2. Change values under `[economical_villager_trading]` and save. `true` means enabled, `false` disabled; use a dot for decimals. Keep minimums no higher than their corresponding maximums.
3. Restart the game/server. Back up the file before editing. If an older file lacks updated comments, back it up, regenerate it and copy your values back.

**Reading the tables:** “Default” is the value built into this version; “Example” is the current local test configuration, provided as an optional starting point. It is not applied automatically. The example values are not a claim about any other installation. Times are game ticks: **20 ticks ≈ 1 second** at normal speed. Percentages use `0–100`. Integer settings accept non-negative whole numbers; factors use `0.0–1.0` where specified. Forge writes the exact permitted ranges in the generated file.

**Trade directions:** “sell” means the player gives items and receives currency; “buy” means the player pays currency for an item. The mod uses its currency item tags to classify trades. Demand is tracked per offer; repeated use can change its price. Trading the opposite direction can bring demand closer to baseline and reopen eligible closed offers.

### Prices and demand

| Setting | Default | Example | Effect |
|---|---:|---:|---|
| `villagerTradeAdjustBalanceSell` | `1.0` | `1.0` | Strength of demand price changes when selling to the villager. `-1` uses vanilla demand; `0` disables this adjustment; higher positive values react more strongly. |
| `villagerTradeAdjustBalanceBuy` | `0.5` | `0.5` | Same control for purchases from the villager, independently of selling. |
| `demandGrowthStartUses` | `4` | `1` | Use count at which demand starts increasing for a trade when the applicable multiplier is non-negative. |
| `demandRestockDecayFactor` | `0.5` | `0.5` | Fraction of accumulated demand removed on restock: `1.0` removes all, `0.5` half, `0.0` none. Volume balancing can reduce the effective decay. |

### Restocking and volume

| Setting | Default | Example | Effect |
|---|---:|---:|---|
| `enableRandomPartialRestock` | `true` | `true` | Reopen a random subset of eligible opposite-group offers instead of all of them. When all offers are exhausted, the mod attempts a full restock; offers frozen by a random lockout are treated separately. |
| `restockTradesMin` | `1` | `1` | Minimum number of offers reopened per partial restock. |
| `restockTradesMax` | `4` | `3` | Maximum number reopened, capped by eligible offers. Set equal to the minimum for a fixed count. |
| `enableNetVolumeDemandBalance` | `true` | `true` | Scale down demand decay when the opposite-side trading volume that triggered restock is small relative to the restocked side. |
| `netVolumeDecayDampening` | `1.0` | `0.7` | Strength of that suppression, from `0.0` (none) to `1.0` (maximum). |
| `netVolumeTrackingDecay` | `0.9` | `0.9` | Retention factor for tracked buy/sell volume per restock; `1.0` retains history, smaller values favor recent trades. |

### Opposite-group pressure

| Setting | Default | Example | Effect |
|---|---:|---:|---|
| `enableCrossGroupDemandPressure` | `true` | `true` | Each trade pulls demand of currently open opposite-group offers toward zero; closed offers are handled when they restock. |
| `crossGroupPressureStrength` | `0.5` | `0.5` | Approximate demand reduction per emerald of trade value. `0` disables the effect. |
| `crossGroupPressureMaxStep` | `6` | `6` | Maximum demand adjustment per opposite offer from a single trade; `0` removes the cap. The adjustment cannot cross zero. |

### General

| Setting | Default | Example | Effect |
|---|---:|---:|---|
| `enableReputationPriceAdjustments` | `true` | `true` | Apply special price adjustments such as villager reputation. |
| `villagerXpMultiplier` | `1.0` | `1.0` | Multiplier for XP rewarded by trades; `0` removes it. |
| `enableVillagerLevelupWhileTrading` | `true` | `true` | Allow a villager to level up while the trade screen is open. |
| `doVanillaRestocks` | `false` | `false` | Allow vanilla timed restocking in addition to the mod's trade-based logic. |
| `freezeProfessionTrades` | `true` | `true` | Remember the first trades rolled for a profession when a job site is reassigned; normal level-up unlocks still work. |

### Early closure and recovery

Two independent mechanics can close an offer early. A **random lockout** rolls on each use. With its recovery timer enabled, the offer is frozen and ignores regular restocks until its timer expires. With recovery disabled, it can reopen through normal restocking. A **use-limit closure** can reopen through eligible trading or level-ups, and optionally its own timer. When both mechanics are enabled, the use limit is checked first.

| Setting | Default | Example | Effect |
|---|---:|---:|---|
| `enableOveruseLockout` | `false` | `true` | Enable independent random early closure. |
| `overuseLockoutAggressiveness` | `10` | `20` | Chance of random closure per use, in percent. |
| `enableLockoutRecovery` | `false` | `true` | Give randomly closed offers their own recovery timer. |
| `lockoutRecoveryMinTicks` | `5000` | `600` | Minimum random-lockout recovery time (example: 30 seconds). |
| `lockoutRecoveryMaxTicks` | `10000` | `1200` | Maximum recovery time (example: 60 seconds). |
| `enableLimitOveruse` | `false` | `true` | Enable a separate per-offer use limit. |
| `limitOveruseMin` | `5` | `1` | First use eligible for the optional early-close roll. |
| `limitOveruseMax` | `10` | `7` | Use count at which closure is guaranteed. |
| `limitOveruseChanceEnabled` | `false` | `true` | Roll for early closure from the minimum; if off, wait for the maximum. |
| `limitOveruseChanceAggressiveness` | `10` | `20` | Early-close chance per eligible use, in percent, independent of the random lockout chance. |
| `enableGlobalRestockCooldown` | `false` | `true` | Also reopen use-limit closures after a per-offer timer. Does not apply to random lockouts. |
| `globalRestockCooldownMinTicks` | `12000` | `12000` | Minimum timer for use-limit closures: 10 minutes. |
| `globalRestockCooldownMaxTicks` | `30000` | `30000` | Maximum timer for use-limit closures: 25 minutes. |

## Project layout and license

`src/main/java/` contains mod logic, mixins, networking and configuration; `src/main/resources/` contains metadata, translations, tags and mixin configuration. `gradle.properties` defines the Minecraft/Forge versions, mod metadata and Gradle memory. `build.gradle`, `settings.gradle`, `gradle/` and `gradlew*` define the build; `hosted_assets/` contains artwork. Dependencies are downloaded from the repositories in the build files; no API credentials are required.

Licensed under [MIT](LICENSE). The original copyright notice is retained. Original project: [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading) by SuperSaiyanSubtlety. Forks are welcome under the license.

---

# Русский

> **Для настройки мода необходимо вручную изменить конфиг.** Один раз запустите игру или сервер, остановите его и откройте **`config/economical_villager_trading-common.toml`** в папке игры или сервера. Обычно `config` находится рядом с `mods`, а не внутри неё. Настройки находятся в самом файле, подробное описание — ниже. Со стандартными значениями мод работает без редактирования; для своего баланса измените файл вручную.

Forge-порт и расширение [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading) от SuperSaiyanSubtlety. Продажа предметов жителю и покупка у него влияют на спрос и могут пополнять сделки противоположной группы. Можно настроить цены, частичное пополнение, баланс объёмов, временные блокировки и сохранение сделок профессии.

## Установка и сборка

Нужны Minecraft **1.20.1**, Forge **47.4.10** (версия для сборки) и Java **17**. Поместите собранный JAR мода в папку `mods/` экземпляра игры или сервера. При первом запуске Forge создаст конфиг. Перед изменением модов или настроек сохраните резервную копию мира. Fabric API, MidnightLib и Mod Menu не нужны.

Для сборки клонируйте репозиторий или создайте форк, установите JDK 17, задайте `JAVA_HOME` и выполните:

```sh
# Windows
.\gradlew.bat build
# Linux / macOS
./gradlew build
```

При первой сборке Gradle скачает зависимости. Обычный JAR мода находится в `build/libs/`. Проект можно открыть в IDE как Gradle-проект; `./gradlew runClient` и `./gradlew runServer` запускают среду разработки в `run/` (для сервера при запросе примите EULA).

## Руководство по конфигу

1. Запустите игру или сервер с модом один раз, затем остановите. Откройте `config/economical_villager_trading-common.toml` в текстовом редакторе с поддержкой UTF-8.
2. Измените значения в разделе `[economical_villager_trading]` и сохраните файл. `true` — включено, `false` — выключено; в дробных числах используйте точку. Минимум не должен превышать соответствующий максимум.
3. Перезапустите игру или сервер. Перед изменениями сохраните копию конфига. Если в старом файле нет новых комментариев, сохраните его, создайте новый и перенесите свои значения.

**Как читать таблицы:** «Стандарт» — значение, встроенное в эту версию; «Пример» — текущие настройки локального теста, предлагаемые как необязательный вариант. Автоматически они не применяются и не описывают настройки других установок. Время задаётся в игровых тиках: **20 тиков ≈ 1 секунда** при нормальной скорости. Вероятности — от `0` до `100` процентов. Целочисленные настройки неотрицательны; указанные коэффициенты принимают значения от `0.0` до `1.0`. Точные диапазоны Forge записывает в созданный файл.

**Направления торговли:** «продажа» — игрок отдаёт предметы и получает валюту; «покупка» — отдаёт валюту и получает предмет. Для классификации мод использует теги валютных предметов. Спрос отслеживается отдельно для каждой сделки: повторное использование может менять цену. Торговля в обратном направлении может приблизить спрос к базовому и открыть подходящие закрытые сделки.

### Цены и спрос

| Параметр | Стандарт | Пример | Что делает |
|---|---:|---:|---|
| `villagerTradeAdjustBalanceSell` | `1.0` | `1.0` | Сила изменения цены при продаже жителю. `-1` — ванильный спрос, `0` — отключить изменение, большее положительное значение усиливает реакцию. |
| `villagerTradeAdjustBalanceBuy` | `0.5` | `0.5` | То же для покупок у жителя, независимо от продаж. |
| `demandGrowthStartUses` | `4` | `1` | Номер использования, с которого растёт спрос, если множитель соответствующей группы неотрицателен. |
| `demandRestockDecayFactor` | `0.5` | `0.5` | Доля накопленного спроса, снимаемая при пополнении: `1.0` — весь, `0.5` — половина, `0.0` — ничего. Баланс объёмов может уменьшить фактическое снижение. |

### Пополнение и объём торговли

| Параметр | Стандарт | Пример | Что делает |
|---|---:|---:|---|
| `enableRandomPartialRestock` | `true` | `true` | Открывать случайную часть подходящих сделок противоположной группы. Если все сделки исчерпаны, мод пытается пополнить их полностью; замороженные случайной блокировкой сделки обрабатываются отдельно. |
| `restockTradesMin` | `1` | `1` | Минимальное число сделок при частичном пополнении. |
| `restockTradesMax` | `4` | `3` | Максимальное число, ограниченное количеством подходящих сделок. Равенство минимуму задаёт фиксированное число. |
| `enableNetVolumeDemandBalance` | `true` | `true` | Уменьшает снижение спроса, если объём торговли противоположной группы, вызвавшей пополнение, был сравнительно небольшим. |
| `netVolumeDecayDampening` | `1.0` | `0.7` | Сила этого подавления: `0.0` — нет, `1.0` — максимальная. |
| `netVolumeTrackingDecay` | `0.9` | `0.9` | Доля истории объёмов, сохраняемая при пополнении: `1.0` хранит историю, меньшие значения придают больший вес недавней торговле. |

### Влияние противоположной группы

| Параметр | Стандарт | Пример | Что делает |
|---|---:|---:|---|
| `enableCrossGroupDemandPressure` | `true` | `true` | Каждая сделка приближает спрос открытых предложений противоположной группы к нулю; закрытые обрабатываются при пополнении. |
| `crossGroupPressureStrength` | `0.5` | `0.5` | Примерное снижение спроса на каждый изумруд стоимости сделки. `0` отключает эффект. |
| `crossGroupPressureMaxStep` | `6` | `6` | Предел изменения спроса одного противоположного предложения за сделку; `0` убирает предел. Изменение не переходит через ноль. |

### Общие параметры

| Параметр | Стандарт | Пример | Что делает |
|---|---:|---:|---|
| `enableReputationPriceAdjustments` | `true` | `true` | Применять специальные изменения цен, например от репутации у жителей. |
| `villagerXpMultiplier` | `1.0` | `1.0` | Множитель опыта от сделок; `0` убирает опыт. |
| `enableVillagerLevelupWhileTrading` | `true` | `true` | Разрешить жителю повысить уровень при открытом окне торговли. |
| `doVanillaRestocks` | `false` | `false` | Разрешить ванильное пополнение по времени дополнительно к торговой логике мода. |
| `freezeProfessionTrades` | `true` | `true` | Сохранять первые сделки профессии при повторном назначении рабочего места; обычное открытие сделок при повышении уровня сохраняется. |

### Досрочное закрытие и восстановление

Есть два независимых механизма. **Случайная блокировка** проверяется при каждом использовании. Если включён её таймер, сделка замораживается и игнорирует обычное пополнение до его окончания. Если таймер выключен, сделка может открыться при обычном пополнении. **Закрытие по лимиту** может сниматься подходящей торговлей или повышением уровня жителя, а также отдельным таймером. При включении обоих механизмов сначала проверяется лимит.

| Параметр | Стандарт | Пример | Что делает |
|---|---:|---:|---|
| `enableOveruseLockout` | `false` | `true` | Включить независимое случайное досрочное закрытие. |
| `overuseLockoutAggressiveness` | `10` | `20` | Вероятность случайного закрытия при использовании, в процентах. |
| `enableLockoutRecovery` | `false` | `true` | Дать случайно закрытым сделкам отдельный таймер восстановления. |
| `lockoutRecoveryMinTicks` | `5000` | `600` | Минимальная задержка случайной блокировки в тиках (в примере 30 секунд). |
| `lockoutRecoveryMaxTicks` | `10000` | `1200` | Максимальная задержка (в примере 60 секунд). |
| `enableLimitOveruse` | `false` | `true` | Включить отдельный лимит использований сделки. |
| `limitOveruseMin` | `5` | `1` | Первое использование, с которого возможна дополнительная проверка шанса закрытия. |
| `limitOveruseMax` | `10` | `7` | Количество использований, при котором закрытие гарантировано. |
| `limitOveruseChanceEnabled` | `false` | `true` | Проверять шанс закрытия с минимума; если выключено, ждать максимума. |
| `limitOveruseChanceAggressiveness` | `10` | `20` | Вероятность досрочного закрытия при каждом подходящем использовании, в процентах; независима от случайной блокировки. |
| `enableGlobalRestockCooldown` | `false` | `true` | Дополнительно открывать сделки, закрытые по лимиту, по отдельному таймеру. На случайные блокировки не влияет. |
| `globalRestockCooldownMinTicks` | `12000` | `12000` | Минимальный таймер закрытия по лимиту: 10 минут. |
| `globalRestockCooldownMaxTicks` | `30000` | `30000` | Максимальный таймер закрытия по лимиту: 25 минут. |

## Структура проекта и лицензия

`src/main/java/` содержит логику мода, mixin-классы, сеть и конфиг; `src/main/resources/` — метаданные, переводы, теги и настройки mixin. В `gradle.properties` заданы версии Minecraft/Forge, метаданные мода и память Gradle. `build.gradle`, `settings.gradle`, `gradle/` и `gradlew*` отвечают за сборку; `hosted_assets/` содержит изображения. Зависимости скачиваются из репозиториев в файлах сборки; ключи API не требуются.

Лицензия [MIT](LICENSE). Уведомление об авторских правах исходного проекта сохранено. Исходный проект: [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading), автор SuperSaiyanSubtlety. Форки приветствуются на условиях лицензии.
