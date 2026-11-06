package net.sssubtlety.economical_villager_trading.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Replaces the original Fabric MidnightLib-backed config with a native
 * Forge ForgeConfigSpec config, keeping the same fields/semantics/defaults.
 */
public final class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue VILLAGER_TRADE_ADJUST_BALANCE_SELL;
    public static final ForgeConfigSpec.DoubleValue VILLAGER_TRADE_ADJUST_BALANCE_BUY;
    public static final ForgeConfigSpec.IntValue DEMAND_GROWTH_START_USES;
    public static final ForgeConfigSpec.DoubleValue DEMAND_RESTOCK_DECAY_FACTOR;

    public static final ForgeConfigSpec.BooleanValue ENABLE_RANDOM_PARTIAL_RESTOCK;
    public static final ForgeConfigSpec.IntValue RESTOCK_TRADES_MIN;
    public static final ForgeConfigSpec.IntValue RESTOCK_TRADES_MAX;

    public static final ForgeConfigSpec.BooleanValue ENABLE_NET_VOLUME_DEMAND_BALANCE;
    public static final ForgeConfigSpec.DoubleValue NET_VOLUME_DECAY_DAMPENING;
    public static final ForgeConfigSpec.DoubleValue NET_VOLUME_TRACKING_DECAY;

    public static final ForgeConfigSpec.BooleanValue ENABLE_CROSS_GROUP_DEMAND_PRESSURE;
    public static final ForgeConfigSpec.DoubleValue CROSS_GROUP_PRESSURE_STRENGTH;
    public static final ForgeConfigSpec.IntValue CROSS_GROUP_PRESSURE_MAX_STEP;

    public static final ForgeConfigSpec.BooleanValue ENABLE_REPUTATION_PRICE_ADJUSTMENTS;
    public static final ForgeConfigSpec.DoubleValue VILLAGER_XP_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_VILLAGER_LEVELUP_WHILE_TRADING;
    public static final ForgeConfigSpec.BooleanValue DO_VANILLA_RESTOCKS;

    public static final ForgeConfigSpec.BooleanValue ENABLE_OVERUSE_LOCKOUT;
    public static final ForgeConfigSpec.IntValue OVERUSE_LOCKOUT_AGGRESSIVENESS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_LOCKOUT_RECOVERY;
    public static final ForgeConfigSpec.IntValue LOCKOUT_RECOVERY_MIN_TICKS;
    public static final ForgeConfigSpec.IntValue LOCKOUT_RECOVERY_MAX_TICKS;

    public static final ForgeConfigSpec.BooleanValue ENABLE_LIMIT_OVERUSE;
    public static final ForgeConfigSpec.IntValue LIMIT_OVERUSE_MIN;
    public static final ForgeConfigSpec.IntValue LIMIT_OVERUSE_MAX;
    public static final ForgeConfigSpec.BooleanValue LIMIT_OVERUSE_CHANCE_ENABLED;
    public static final ForgeConfigSpec.IntValue LIMIT_OVERUSE_CHANCE_AGGRESSIVENESS;

    // New: global, per-trade, purely time-based restock for trades that closed FULLY (via
    // enableLimitOveruse reaching its limit) - independent of, and never applied to, trades
    // frozen by the separate enableOveruseLockout "random" mini-lockout (those keep using
    // lockoutRecoveryMinTicks/lockoutRecoveryMaxTicks instead - see VillagerMixin).
    public static final ForgeConfigSpec.BooleanValue ENABLE_GLOBAL_RESTOCK_COOLDOWN;
    public static final ForgeConfigSpec.IntValue GLOBAL_RESTOCK_COOLDOWN_MIN_TICKS;
    public static final ForgeConfigSpec.IntValue GLOBAL_RESTOCK_COOLDOWN_MAX_TICKS;

    public static final ForgeConfigSpec.BooleanValue FREEZE_PROFESSION_TRADES;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("economical_villager_trading");

        VILLAGER_TRADE_ADJUST_BALANCE_SELL = builder
                .comment(
                        "=== PRICES AND DEMAND / ???? ? ????? ===",
                        "Controls how strongly a SELL-TO-VILLAGER trade's price (you give items, get emeralds -",
                        "e.g. selling sticks/flint/feathers) reacts to its OWN use count - each trade is fully",
                        "independent, there is no comparison to other trades on the villager anymore.",
                        "A negative value (e.g. -1) makes the mod use vanilla's demand bonus behavior instead.",
                        "0 disables demand bonus adjustment entirely for this group.",
                        "1.0 = same raw strength as vanilla's own formula. Higher values react more strongly",
                        "(price swings faster per use), lower values (0-1) react more gently.",
                        "See villagerTradeAdjustBalanceBuy for the equivalent setting on the other direction",
                        "(buying items FROM the villager with emeralds) - they're independent on purpose, so",
                        "repeatedly selling doesn't have to raise prices at the same rate repeated buying does.",
                        "",
                        "RU: ???? ??? ??????? ??????: -1 ? ????????? ?????, 0 ? ?????????, ?????? 1 ? ??????? ????.")
                .defineInRange("villagerTradeAdjustBalanceSell", 1.0, -1.0, Double.MAX_VALUE);

        VILLAGER_TRADE_ADJUST_BALANCE_BUY = builder
                .comment(
                        "Same as villagerTradeAdjustBalanceSell above, but for BUY-FROM-VILLAGER trades (you pay",
                        "emeralds, get items - e.g. buying arrows/bread). Kept as a separate value on purpose:",
                        "before this existed, buying and selling shared one multiplier, so e.g. buying bread 3",
                        "times raised its emerald cost exactly as aggressively as selling potatoes 3 times raised",
                        "the potato count needed per emerald - which isn't necessarily the balance you want.",
                        "Defaults to half of villagerTradeAdjustBalanceSell's default, so out of the box buying",
                        "prices climb more gently than selling amounts shrink.",
                        "",
                        "RU: ???? ??? ??????? ? ??????: -1 ? ????????? ?????, 0 ? ?????????, ?????? 1 ? ??????? ????.")
                .defineInRange("villagerTradeAdjustBalanceBuy", 0.5, -1.0, Double.MAX_VALUE);

        DEMAND_GROWTH_START_USES = builder
                .comment(
                        "The use count at which a trade's price starts rising, instead of vanilla's fixed",
                        "maxUses/2 threshold. E.g. 4 means demand starts climbing on the 4th use of ANY",
                        "trade, regardless of that trade's own maxUses. Only applies when",
                        "the corresponding sell/buy balance is >= 0 (i.e. when vanilla demand is not used).",
                        "",
                        "RU: ?????????? ?????????????, ? ???????? ?????? ????? ??? ??????????????? ????????? ??????.")
                .defineInRange("demandGrowthStartUses", 4, 0, Integer.MAX_VALUE);

        DEMAND_RESTOCK_DECAY_FACTOR = builder
                .comment(
                        "How much of a trade's accumulated demand (price swing) is removed each time it",
                        "restocks/reopens, instead of a hard reset to 0. 1.0 = old behavior, fully resets to",
                        "base price every time. 0.5 (default) = keeps half the swing, so a heavily price-",
                        "boosted trade settles back down gradually over several restocks instead of snapping",
                        "straight back to its base price. 0.0 = never decays at all (permanent).",
                        "",
                        "RU: ???? ??????, ????????? ??? ??????????: 1 ? ?????? ?????, 0.5 ? ????????, 0 ? ??? ????????.")
                .defineInRange("demandRestockDecayFactor", 0.5, 0.0, 1.0);

        ENABLE_RANDOM_PARTIAL_RESTOCK = builder
                .comment(
                        "=== RESTOCKING / ?????????? ===",
                        "Whether a restock (triggered when a trade of the OPPOSITE group runs out and closes)",
                        "only reopens a random subset of the closed group's trades, instead of reopening all",
                        "of them at once. This is what stops \"buy a few cheap junk trades -> every sell trade",
                        "instantly comes back\" abuse. false = old behavior (all matching closed trades reopen",
                        "together). Does not affect the case where the villager has ZERO usable trades left at",
                        "all - that always fully reopens everything, so the villager can never get permanently",
                        "soft-locked.",
                        "",
                        "RU: ????????? ????????? ????? ?????? ??????????????? ??????; ???? ??? ???????, ??????????? ???.")
                .define("enableRandomPartialRestock", true);

        RESTOCK_TRADES_MIN = builder
                .comment(
                        "Minimum number of eligible closed trades reopened per restock event, when",
                        "enableRandomPartialRestock is true. Set equal to restockTradesMax for a fixed",
                        "(non-random) number of trades per restock instead of a range.",
                        "",
                        "RU: ??????? ?????? ??? ????????? ????????? ??????????.")
                .defineInRange("restockTradesMin", 1, 0, Integer.MAX_VALUE);

        RESTOCK_TRADES_MAX = builder
                .comment(
                        "Maximum number of eligible closed trades reopened per restock event, when",
                        "enableRandomPartialRestock is true. If there are fewer eligible closed trades than",
                        "this, all of them are candidates and the actual count is capped to that amount.",
                        "",
                        "RU: ???????? ?????? ??? ????????? ????????? ??????????; ?? ?????? ????????.")
                .defineInRange("restockTradesMax", 4, 0, Integer.MAX_VALUE);

        ENABLE_NET_VOLUME_DEMAND_BALANCE = builder
                .comment(
                        "=== TRADE VOLUME / ????? ???????? ===",
                        "Whether the demandRestockDecayFactor for a group of trades (buy or sell) getting",
                        "restocked is scaled down when the OTHER group (the one that just triggered this",
                        "restock by running out) has seen much less recent trading volume in comparison.",
                        "In practice: if you sold in bulk many times and only bought a couple of cheap items",
                        "afterward, those couple of purchases will barely decay the price swing you built up",
                        "from selling, instead of resetting it almost back to normal. If false, decay always",
                        "uses the flat demandRestockDecayFactor regardless of trade volume on either side.",
                        "",
                        "RU: ????????? ??????????? ??????? ??????? ? ?????? ??? ???????? ?????? ????? ??????????.")
                .define("enableNetVolumeDemandBalance", true);

        NET_VOLUME_DECAY_DAMPENING = builder
                .comment(
                        "How strongly a lopsided buy/sell volume ratio can suppress demand decay on restock,",
                        "when enableNetVolumeDemandBalance is true. 0.0 = no suppression regardless of ratio",
                        "(behaves like demandRestockDecayFactor alone). 1.0 (default) = decay can be fully",
                        "suppressed (down to 0) when the triggering side has seen effectively no trading, so a",
                        "price that's been pushed up can actually make it all the way back to its base value",
                        "given enough real opposite-side trading, instead of asymptotically stalling above it.",
                        "Lower this only if you want restocks to always shave off at least a bit of demand",
                        "regardless of trade balance.",
                        "",
                        "RU: ???? ?????????? ???????? ?????? ??? ??????????: 0 ? ???, 1 ? ????????????.")
                .defineInRange("netVolumeDecayDampening", 1.0, 0.0, 1.0);

        NET_VOLUME_TRACKING_DECAY = builder
                .comment(
                        "Each restock cycle, the villager's tracked buy/sell trade-volume counters (used by",
                        "enableNetVolumeDemandBalance) are multiplied by this factor so old trading activity",
                        "gradually stops influencing the balance instead of accumulating forever. 1.0 = never",
                        "forget anything (permanent memory of all trading with this villager). Lower values",
                        "make the balance react mostly to recent trading.",
                        "",
                        "RU: ????????? ?????????? ??????? ??????? ??? ??????????: 1 ? ?? ????????, ?????? ? ??????? ????????.")
                .defineInRange("netVolumeTrackingDecay", 0.9, 0.0, 1.0);

        ENABLE_CROSS_GROUP_DEMAND_PRESSURE = builder
                .comment(
                        "=== CROSS-GROUP DEMAND / ????? ????? ???????? ===",
                        "Whether every single trade immediately nudges the OPPOSITE group's demand back",
                        "toward baseline, instead of only happening when a trade closes and later restocks.",
                        "Without this, buying/selling something you're not currently trading (e.g. buying",
                        "arrows while a potato trade sits mid-use, still open) never affects that other",
                        "trade at all until it happens to run out on its own - which can make a price hike",
                        "look permanently stuck even while you're actively trading the opposite direction.",
                        "Only applies to offers of the opposite group that are currently OPEN. Closed/",
                        "out-of-stock offers are left alone here and only get corrected once, when they",
                        "reopen, via demandRestockDecayFactor/netVolume settings - so a closed trade's price",
                        "never silently drifts while you're not looking at it, and demandRestockDecayFactor",
                        "doesn't get applied on top of an already-adjusted value.",
                        "",
                        "RU: ?????? ????????? ?????????? ?????? ???????? ?????? ??????????????? ?????? ?? ???????? ????????.")
                .define("enableCrossGroupDemandPressure", true);

        CROSS_GROUP_PRESSURE_STRENGTH = builder
                .comment(
                        "How much a single emerald's worth of trading on one group immediately reduces every",
                        "currently OPEN offer's demand in the OTHER group, per trade (closed offers are left",
                        "alone - see enableCrossGroupDemandPressure). Weighted by the actual number of",
                        "emeralds moved in that one trade - paying/receiving 6 emeralds in a single trade",
                        "counts 6x as much as a 1-emerald trade, not just \"+1 trade\" either way. E.g. 0.5",
                        "(default) means a 6-emerald trade lowers every opposite-group open offer's demand by",
                        "roughly 3 immediately. 0 disables the effect without disabling the option outright.",
                        "This always pulls demand toward baseline (0) from whichever side it's currently on -",
                        "it never overshoots past 0 in a single step, and never pushes demand further away",
                        "from 0 (a markup only ever gets smaller here, a discount only ever gets smaller too).",
                        "",
                        "RU: ???? ???????? ?????? ??????????????? ?????? ?? ?????? ??????? ? ??????; 0 ????????? ??????.")
                .defineInRange("crossGroupPressureStrength", 0.5, 0.0, Double.MAX_VALUE);

        CROSS_GROUP_PRESSURE_MAX_STEP = builder
                .comment(
                        "Hard cap on how much a SINGLE trade's cross-group pressure hit (see",
                        "crossGroupPressureStrength) can move any one opposite-group offer's demand toward",
                        "baseline in one go. Without this, the weight of a hit is based on the CURRENT price",
                        "of the trade you just used (emeraldWeight), which is itself inflated by demand that",
                        "trade already built up - so a heavily-marked-up trade's own inflated price feeds",
                        "back into how hard it pushes everything else, letting one click occasionally swing",
                        "an untouched offer's demand almost all the way to 0 (or even flip its sign) instead",
                        "of nudging it. This clamps the per-trade step size directly, independent of how",
                        "large the underlying weight/strength product gets. 0 = uncapped (vanilla-this-mod's",
                        "old, unclamped behavior).",
                        "",
                        "RU: ???????????? ????????? ?????? ??????????????? ?????? ?? ?????????????; 0 ? ??? ???????????.")
                .defineInRange("crossGroupPressureMaxStep", 6, 0, Integer.MAX_VALUE);

        ENABLE_REPUTATION_PRICE_ADJUSTMENTS = builder
                .comment(
                        "=== GENERAL / ????? ????????? ===","Whether villager reputation/demand \"special\" price discounts are applied.",
                        "",
                        "RU: ????????? ??????????? ????????? ??? ?? ????????? ? ?????? ????????? ????????.")
                .define("enableReputationPriceAdjustments", true);

        VILLAGER_XP_MULTIPLIER = builder
                .comment("Multiplier applied to XP rewarded from villager trades.",
                        "",
                        "RU: ????????? ????? ?? ?????? ? ????????.")
                .defineInRange("villagerXpMultiplier", 1.0, 0.0, Double.MAX_VALUE);

        ENABLE_VILLAGER_LEVELUP_WHILE_TRADING = builder
                .comment("Whether villagers can level up (and refresh trades) while a player has their trade GUI open.",
                        "",
                        "RU: ????????? ?????? ???????? ??????? ??? ???????? ???? ????????.")
                .define("enableVillagerLevelupWhileTrading", true);

        DO_VANILLA_RESTOCKS = builder
                .comment("Whether to keep vanilla's restock timing/behavior instead of the mod's custom restock logic.",
                        "",
                        "RU: ???????? ????????? ?????????? ?????? ?? ???????.")
                .define("doVanillaRestocks", false);

        ENABLE_OVERUSE_LOCKOUT = builder
                .comment(
                        "=== RANDOM LOCKOUT / ????????? ?????????? ===",
                        "Whether trades have a chance to close (go out of stock) early on any use,",
                        "independent of maxUses and of limitOveruse below.",
                        "",
                        "RU: ???????? ????????? ????????? ???????? ?????? ??? ?????????????, ?????????? ?? ??????.")
                .define("enableOveruseLockout", false);

        OVERUSE_LOCKOUT_AGGRESSIVENESS = builder
                .comment(
                        "Chance (0-100), in percent, that a trade closes early on use when enableOveruseLockout is true.",
                        "This is independent of limitOveruseChanceAggressiveness.",
                        "",
                        "RU: ??????????? ?????????? ?????????? ???????? ??? ?????????????, ? ?????????.")
                .defineInRange("overuseLockoutAggressiveness", 10, 0, 100);

        ENABLE_LOCKOUT_RECOVERY = builder
                .comment(
                        "Whether trades closed early by overuseLockoutAggressiveness automatically reopen after a random",
                        "delay (see lockoutRecoveryMinTicks/lockoutRecoveryMaxTicks), instead of waiting for a normal restock.",
                        "Only relevant while enableOveruseLockout is true. If false, such trades stay closed until",
                        "the villager's normal restock logic reopens them.",
                        "NOTE: this \"random\" chance-closure is a fully separate outcome from limitOveruse reaching its",
                        "limit (a \"full\" closure, see enableLimitOveruse/enableGlobalRestockCooldown below) - a trade",
                        "closed here is frozen exactly as it was (same price, same use count) and can ONLY reopen via",
                        "this timer: it ignores normal restocks, opposite-group trading, and villager level-ups entirely",
                        "while locked, ever more so unlike a \"full\" closure.",
                        "",
                        "RU: ????????????? ????????? ???????? ???????? ?????? ????? ????????; ?? ????????? ??????? ??? ??????????.")
                .define("enableLockoutRecovery", false);

        LOCKOUT_RECOVERY_MIN_TICKS = builder
                .comment("Minimum ticks before a chance-closed trade automatically reopens.",
                        "",
                        "RU: ??????????? ???????? ???????? ???????? ???????? ?????? ? ?????; 20 ????? = 1 ???????.")
                .defineInRange("lockoutRecoveryMinTicks", 5000, 0, Integer.MAX_VALUE);

        LOCKOUT_RECOVERY_MAX_TICKS = builder
                .comment("Maximum ticks before a chance-closed trade automatically reopens.",
                        "",
                        "RU: ???????????? ???????? ???????? ???????? ???????? ?????? ? ?????; ?? ?????? ????????.")
                .defineInRange("lockoutRecoveryMaxTicks", 10000, 0, Integer.MAX_VALUE);

        ENABLE_LIMIT_OVERUSE = builder
                .comment(
                        "=== USE LIMIT / ????? ????????????? ===",
                        "Whether individual trades have their own use-count window (limitOveruseMin-limitOveruseMax)",
                        "that controls early closure, replacing (not stacking with) vanilla maxUses for this purpose.",
                        "A trade closed this way is a \"full\" closure (as opposed to the \"random\" chance closure from",
                        "enableOveruseLockout above): it reopens the normal ways (trading, which restocks a matching",
                        "group of closed trades, or the villager leveling up), plus optionally its own global time-based",
                        "cooldown - see enableGlobalRestockCooldown.",
                        "",
                        "RU: ???????? ???????? ????????????? ?? ??????? ???????? ??????.")
                .define("enableLimitOveruse", false);

        LIMIT_OVERUSE_MIN = builder
                .comment("Use count at which limitOveruse starts being able to close a trade (inclusive).",
                        "",
                        "RU: ?????? ?????????????, ?? ??????? ?????? ????? ???????? ????????? ??? ?????????? ???????????.")
                .defineInRange("limitOveruseMin", 5, 0, Integer.MAX_VALUE);

        LIMIT_OVERUSE_MAX = builder
                .comment("Use count at which limitOveruse guarantees the trade is closed (inclusive).",
                        "",
                        "RU: ?????????? ?????????????, ??? ??????? ?????? ??????????? ???????????; ?? ?????? ????????.")
                .defineInRange("limitOveruseMax", 10, 0, Integer.MAX_VALUE);

        LIMIT_OVERUSE_CHANCE_ENABLED = builder
                .comment(
                        "false: limitOveruseMax acts as a hard cap - the trade closes exactly once uses reaches it.",
                        "true: starting at limitOveruseMin, each use rolls limitOveruseChanceAggressiveness to close early;",
                        "if it hasn't closed by limitOveruseMax, it closes automatically there.",
                        "",
                        "RU: ???????? ???? ???????? ????? ????????? ? ??????????; ????? ?????? ??????????? ?? ?????????.")
                .define("limitOveruseChanceEnabled", false);

        LIMIT_OVERUSE_CHANCE_AGGRESSIVENESS = builder
                .comment(
                        "Chance (0-100), in percent, that a trade closes early on each use once uses is within",
                        "[limitOveruseMin, limitOveruseMax), when limitOveruseChanceEnabled is true.",
                        "This is its own pool, independent of overuseLockoutAggressiveness (which belongs to the",
                        "separate enableOveruseLockout mechanic).",
                        "",
                        "RU: ??????????? ?????????? ???????? ?????? ????????? limitOveruse ? ?????????; ?????????? ?? ????????? ??????????.")
                .defineInRange("limitOveruseChanceAggressiveness", 10, 0, 100);

        ENABLE_GLOBAL_RESTOCK_COOLDOWN = builder
                .comment(
                        "=== RESTOCK COOLDOWN / ?????? ?????????? ===",
                        "Whether a trade that's FULLY closed (via enableLimitOveruse reaching its limit - see",
                        "limitOveruseMin/limitOveruseMax) can also restock purely from waiting, on its own global",
                        "per-trade cooldown, in addition to the ways it can already reopen (trading with the",
                        "villager, which restocks a matching group of closed trades, or the villager leveling up).",
                        "Prices stay dynamic while such a trade sits closed - it's still just an ordinary out-of-",
                        "stock offer, not frozen. Does NOT apply to trades closed by the separate enableOveruseLockout",
                        "\"random\" mini-lockout - those always use lockoutRecoveryMinTicks/lockoutRecoveryMaxTicks",
                        "instead, and ignore this cooldown (along with normal restocks and level-ups) entirely until",
                        "that timer expires. Off by default.",
                        "",
                        "RU: ????????? ????????? ???????? ?? limitOveruse ??????? ??????????? ?? ???????; ????????? ?????????? ?? ?? ???????.")
                .define("enableGlobalRestockCooldown", false);

        GLOBAL_RESTOCK_COOLDOWN_MIN_TICKS = builder
                .comment(
                        "Minimum ticks before a FULLY-closed trade (see enableGlobalRestockCooldown) automatically",
                        "restocks purely from waiting. 20 ticks = 1 second; the default of 12000 is 10 minutes.",
                        "",
                        "RU: ??????????? ???????? ?????????? ????????? ???????? ?????? ? ?????; 12000 ????? = 10 ?????.")
                .defineInRange("globalRestockCooldownMinTicks", 12000, 0, Integer.MAX_VALUE);

        GLOBAL_RESTOCK_COOLDOWN_MAX_TICKS = builder
                .comment(
                        "Maximum ticks before a FULLY-closed trade (see enableGlobalRestockCooldown) automatically",
                        "restocks purely from waiting. The default of 30000 is 25 minutes.",
                        "",
                        "RU: ???????????? ???????? ?????????? ????????? ???????? ?????? ? ?????; 30000 ????? = 25 ?????; ?? ?????? ????????.")
                .defineInRange("globalRestockCooldownMaxTicks", 30000, 0, Integer.MAX_VALUE);

        FREEZE_PROFESSION_TRADES = builder
                .comment(
                        "=== PROFESSION / ????????? ===",
                        "Whether each villager permanently remembers the first set of trades it ever rolled for a",
                        "given profession, and re-applies that exact set instead of rolling a new random set",
                        "whenever its job-site block is broken and reassigned (the level<=1/xp==0 profession-reset",
                        "exploit). Does not affect normal trade unlocks gained by leveling up through trading.",
                        "",
                        "RU: ????????? ?????????????? ?????? ????????? ??? ????????? ?????????? ???????? ?????; ????? ?????? ??????????? ??????.")
                .define("freezeProfessionTrades", true);

        builder.pop();

        SPEC = builder.build();
    }

    private Config() {
        throw new UnsupportedOperationException();
    }
}