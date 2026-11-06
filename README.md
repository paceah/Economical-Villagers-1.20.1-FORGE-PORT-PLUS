# Economical Villager Trading — Forge 1.20.1 port

[Русский](#русский)

A Forge port and extension of [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading) by SuperSaiyanSubtlety. Trading in one direction can restock the opposite group, while demand changes with use. Options cover partial restocks, trade-volume balancing, price pressure, early lockouts, recovery timers and profession-trade preservation.

## Requirements and installation

- Minecraft **1.20.1**, Forge **47.4.10** (the build target), Java **17**.
- Build the mod as described below, or obtain a compatible release if available. Put the mod JAR in the Forge instance's `mods` folder and start the game or server. Forge and Minecraft versions must match. Back up worlds before adding or changing mods.
- This project uses Forge; Fabric API, MidnightLib and Mod Menu are not required. MixinExtras is bundled by the build.

## Build and development

Clone or fork this repository, install JDK 17 and make it available through `JAVA_HOME` or your IDE. From the project root:

```sh
# Windows
.\gradlew.bat build
# Linux / macOS
./gradlew build
```

The wrapper downloads Gradle and dependencies on first use. Built JARs are in `build/libs/`; use the normal mod JAR rather than a sources/development JAR. Import the project as Gradle in an IDE; `./gradlew runClient` and `./gradlew runServer` start development instances in `run/` (accept the server EULA when prompted). These local files are ignored by Git.

## Configuration

Forge generates `config/economical_villager_trading-common.toml` in the game or server instance on first launch. Stop the game/server before editing. Each setting has English and Russian comments, defaults and allowed ranges in the generated file. Existing keys and defaults are preserved. If an existing config lacks updated comments, back it up and regenerate it, then copy your values back. Set minimums no higher than their corresponding maximums. Times use ticks: 20 ticks = 1 second at normal speed.

Settings cover demand and prices, partial restocks, volume balancing, cross-group pressure, reputation and XP, random lockouts and recovery, use limits, cooldowns and profession trades. `true` enables a feature and `false` disables it. Some settings only apply when their associated feature is enabled.

## Project layout and variables

- `src/main/java/` — mod entry point, Forge configuration, trade logic, mixins and networking.
- `src/main/resources/` — mod metadata, translations, item tags and mixin configuration.
- `gradle.properties` — Minecraft/Forge versions, mod ID, name, version, authors, license and description; `org.gradle.jvmargs` controls Gradle memory. These are build properties, not secrets.
- `build.gradle`, `settings.gradle`, `gradle/` and `gradlew*` — build setup and Gradle wrapper.
- `hosted_assets/` — project artwork; `CHANGELOG.md` — inherited project history.

Dependencies are resolved by Gradle from the repositories in the build files; an internet connection is needed for the initial build. No API keys or credentials are required. Keep personal credentials and local settings outside the repository.

## License and credits

MIT; see [LICENSE](LICENSE). The original copyright notice is retained. Original project: [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading) by SuperSaiyanSubtlety. Forks and contributions are welcome under the license.

---

# Русский

Forge-порт и расширение [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading) от SuperSaiyanSubtlety для Minecraft 1.20.1. Торговля в одном направлении может пополнять сделки противоположной группы, а спрос меняется по мере использования. Доступны частичное пополнение, баланс объёмов, влияние на цены, досрочные блокировки, таймеры восстановления и сохранение сделок профессии.

## Требования и установка

- Minecraft **1.20.1**, Forge **47.4.10** (версия для сборки), Java **17**.
- Соберите мод по инструкции ниже или скачайте совместимый релиз, если он опубликован. Поместите JAR мода в папку `mods` экземпляра Forge и запустите игру или сервер. Версии Forge и Minecraft должны совпадать. Перед изменением набора модов сохраните резервную копию мира.
- Проект использует Forge; Fabric API, MidnightLib и Mod Menu не нужны. MixinExtras включается в сборку.

## Сборка и разработка

Клонируйте репозиторий или создайте форк, установите JDK 17 и укажите его через `JAVA_HOME` или настройки IDE. В корне проекта выполните:

```sh
# Windows
.\gradlew.bat build
# Linux / macOS
./gradlew build
```

При первом запуске wrapper скачает Gradle и зависимости. Готовые JAR находятся в `build/libs/`: используйте обычный JAR мода, а не sources/development. Импортируйте проект в IDE как Gradle-проект; `./gradlew runClient` и `./gradlew runServer` запускают среду разработки в `run/` (для сервера при запросе примите EULA). Локальные файлы исключены из Git.

## Конфигурация

При первом запуске Forge создаёт `config/economical_villager_trading-common.toml` в папке игры или сервера. Остановите игру/сервер перед редактированием. У каждого параметра в файле есть описание на английском и русском, значение по умолчанию и допустимый диапазон. Имена параметров и значения по умолчанию сохранены. Если в существующем конфиге нет новых комментариев, сохраните его копию, создайте новый файл и перенесите свои значения. Минимум не должен превышать соответствующий максимум. Время задаётся в тиках: 20 тиков = 1 секунда при нормальной скорости.

Настройки охватывают спрос и цены, частичное пополнение, баланс объёмов, влияние противоположных групп, репутацию и опыт, случайные блокировки и восстановление, лимиты использований, таймеры и сделки профессии. `true` включает функцию, `false` выключает. Некоторые параметры действуют только при включении связанной функции.

## Структура и переменные

- `src/main/java/` — точка входа, конфигурация Forge, логика торговли, mixin-классы и сеть.
- `src/main/resources/` — метаданные мода, переводы, теги предметов и конфигурация mixin.
- `gradle.properties` — версии Minecraft/Forge, ID, название, версия, авторы, лицензия и описание мода; `org.gradle.jvmargs` задаёт память Gradle. Это параметры сборки, не секреты.
- `build.gradle`, `settings.gradle`, `gradle/` и `gradlew*` — настройки сборки и Gradle wrapper.
- `hosted_assets/` — изображения проекта; `CHANGELOG.md` — история исходного проекта.

Gradle скачивает зависимости из репозиториев, указанных в файлах сборки; для первой сборки нужен интернет. Ключи API и учётные данные не требуются. Личные данные и локальные настройки храните вне репозитория.

## Лицензия и авторы

MIT; см. [LICENSE](LICENSE). Уведомление об авторских правах исходного проекта сохранено. Исходный проект: [Economical Villager Trading](https://gitlab.com/supersaiyansubtlety/economical_villager_trading), автор SuperSaiyanSubtlety. Форки и вклад в проект приветствуются на условиях лицензии.
