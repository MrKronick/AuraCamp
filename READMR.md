# 🔥 AuraCamp

*[English](#english) | [Русский](#русский)*

---

<a name="english"></a>
## 🇬🇧 English

A modern, high-performance, and dependency-free campfire management plugin for **Minecraft Paper (1.21+)**. Enhance your survival and SMP experience with tiered campfires, custom aura buffs, team management, waypoint teleportation, and an in-game progression shop!

### ✨ Features

- **Multi-Tier Campfire Ecosystem (Levels 1–3):** Upgrade your campfires to expand the aura radius, increase healing power, and unlock advanced capabilities.
- **In-Game Campfire Shop (Level 3):** Reaching the maximum level clears base effects and unlocks a dedicated GUI shop. Spend diamonds on permanent potion effects (Regeneration, Fire Resistance), anti-mob aggression, full PvP & PvE immunity, larger radius tiers (up to 100 blocks), and expanded team slots (up to 16 players).
- **Team System (`/cp team`):** Create a campfire team, manage allies, and safely share all aura buffs and base protections with your friends.
- **Toggleable Physical Shield:** Enable or disable campfire protection directly from the management GUI to prevent enemies from extinguishing your fire.
- **Waypoint Compass:** Bind a compass to a Level 3 campfire for quick returns to your base (blocked during combat).
- **Flexible Message Output:** Choose how alerts are delivered (`CHAT`, `ACTIONBAR`, or `BOTH`) to keep your chat clean.
- **Modern UI & Localization:** Powered by Kyori Adventure (MiniMessage) with full multi-language support (`en_US`, `ru_RU`) and optional resource pack title hooks.

### 🎮 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/cp team add <player>` | Add a player to your campfire team (Owner only) | None |
| `/cp team remove <player>` | Remove a player from your campfire team (Owner only) | None |
| `/cp team list` | View current campfire team members and slot limits | None |
| `/cp reload` | Reload configuration and localization files | `auracamp.admin` |

### 📥 Installation

1. Download the latest `AuraCamp-1.0.0.jar` from the [Releases](../../releases) page.
2. Place the `.jar` file into your server's `plugins/` folder.
3. Restart your Paper (1.21+) server.
4. Customize `config.yml` and language files in `plugins/AuraCamp/` to fit your server balance.

---

<a name="русский"></a>
## 🇷🇺 Русский

Современный, производительный плагин без лишних зависимостей для **Minecraft Paper (1.21+)**, превращающий обычные костры в полноценный центр командной базы. Прокачивайте уровни костра, объединяйтесь в команды, защищайте территорию и покупайте постоянные улучшения в специальном магазине!

### ✨ Основные возможности

- **Многоуровневая прокачка (1–3 уровни):** Улучшайте костёр за ресурсы, чтобы увеличить радиус ауры, ускорить восстановление здоровья и открыть доступ к продвинутым функциям.
- **Магазин костра (на 3 уровне):** При достижении максимального уровня базовые эффекты сбрасываются и открывается GUI-магазин за алмазы. Покупайте постоянные эффекты (Регенерация, Огнестойкость), защиту от агра монстров, полную неуязвимость от PvP и мобов, расширение радиуса (до 100 блоков в каждую сторону) и увеличение лимита команды (до 16 игроков).
- **Командная система (`/cp team`):** Добавляйте друзей в свой костёр, чтобы аура лечения, эффекты и защита базы распространялись на всю вашу команду.
- **Переключаемая защита костра:** Включайте и выключайте физический щит костра в меню управления (`Shift + ПКМ`), чтобы враги не могли его потушить.
- **Компас возврата:** Привяжите компас к костру 3-го уровня для телепортации на базу (блокируется в режиме боя).
- **Гибкий вывод сообщений:** Настраивайте в конфиге, куда отправлять уведомления (`CHAT`, `ACTIONBAR` или `BOTH`), чтобы не засорять чат.
- **Современный интерфейс и переводы:** Полная поддержка форматирования Kyori Adventure (MiniMessage), встроенные языки (`en_US`, `ru_RU`) и задел под кастомный ресурс-пак.

### 🎮 Команды и права

| Команда | Описание | Право |
| :--- | :--- | :--- |
| `/cp team add <игрок>` | Добавить игрока в команду костра (только владелец) | Нет |
| `/cp team remove <игрок>` | Удалить игрока из команды костра (только владелец) | Нет |
| `/cp team list` | Посмотреть список участников и лимит слотов | Нет |
| `/cp reload` | Перезагрузить конфигурацию и файлы перевода | `auracamp.admin` |

### 📥 Установка

1. Скачайте актуальный файл `AuraCamp-1.0.0.jar` во вкладке [Releases](../../releases).
2. Поместите файл в папку `plugins/` вашего сервера.
3. Перезапустите сервер Paper (1.21+).
4. Настройте цены, радиусы и лимиты под баланс вашего сервера в файле `plugins/AuraCamp/config.yml`.
