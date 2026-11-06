package net.sssubtlety.economical_villager_trading.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class ConfigManager {
    private static boolean initialized = false;

    private ConfigManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * Registers the mod's ForgeConfigSpec with the given mod loading context.
     * Call this from the mod's constructor (see EconomicalVillagerTradingMod).
     */
    public static void init(ModLoadingContext context) {
        if (initialized) {
            throw new IllegalStateException("Can't reinitialize!");
        }

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        initialized = true;
    }

    public static double getVillagerTradeAdjustBalanceSell() {
        return Config.VILLAGER_TRADE_ADJUST_BALANCE_SELL.get();
    }

    public static double getVillagerTradeAdjustBalanceBuy() {
        return Config.VILLAGER_TRADE_ADJUST_BALANCE_BUY.get();
    }

    public static int getDemandGrowthStartUses() {
        return Config.DEMAND_GROWTH_START_USES.get();
    }

    public static double getDemandRestockDecayFactor() {
        return Config.DEMAND_RESTOCK_DECAY_FACTOR.get();
    }

    public static boolean randomPartialRestockEnabled() {
        return Config.ENABLE_RANDOM_PARTIAL_RESTOCK.get();
    }

    public static int getRestockTradesMin() {
        return Config.RESTOCK_TRADES_MIN.get();
    }

    public static int getRestockTradesMax() {
        return Config.RESTOCK_TRADES_MAX.get();
    }

    public static boolean netVolumeDemandBalanceEnabled() {
        return Config.ENABLE_NET_VOLUME_DEMAND_BALANCE.get();
    }

    public static double getNetVolumeDecayDampening() {
        return Config.NET_VOLUME_DECAY_DAMPENING.get();
    }

    public static double getNetVolumeTrackingDecay() {
        return Config.NET_VOLUME_TRACKING_DECAY.get();
    }

    public static boolean crossGroupPressureEnabled() {
        return Config.ENABLE_CROSS_GROUP_DEMAND_PRESSURE.get();
    }

    public static double getCrossGroupPressureStrength() {
        return Config.CROSS_GROUP_PRESSURE_STRENGTH.get();
    }

    public static int getCrossGroupPressureMaxStep() {
        return Config.CROSS_GROUP_PRESSURE_MAX_STEP.get();
    }

    public static boolean specialDiscountsEnabled() {
        return Config.ENABLE_REPUTATION_PRICE_ADJUSTMENTS.get();
    }

    public static double getVillagerXpMultiplier() {
        return Config.VILLAGER_XP_MULTIPLIER.get();
    }

    public static boolean villagerLevelingWithGuiOpenEnabled() {
        return Config.ENABLE_VILLAGER_LEVELUP_WHILE_TRADING.get();
    }

    public static boolean doVanillaRestocks() {
        return Config.DO_VANILLA_RESTOCKS.get();
    }

    public static boolean overuseLockoutEnabled() {
        return Config.ENABLE_OVERUSE_LOCKOUT.get();
    }

    public static int getOveruseLockoutAggressiveness() {
        return Config.OVERUSE_LOCKOUT_AGGRESSIVENESS.get();
    }

    public static boolean lockoutRecoveryEnabled() {
        return Config.ENABLE_LOCKOUT_RECOVERY.get();
    }

    public static int getLockoutRecoveryMinTicks() {
        return Config.LOCKOUT_RECOVERY_MIN_TICKS.get();
    }

    public static int getLockoutRecoveryMaxTicks() {
        return Config.LOCKOUT_RECOVERY_MAX_TICKS.get();
    }

    public static boolean limitOveruseEnabled() {
        return Config.ENABLE_LIMIT_OVERUSE.get();
    }

    public static int getLimitOveruseMin() {
        return Config.LIMIT_OVERUSE_MIN.get();
    }

    public static int getLimitOveruseMax() {
        return Config.LIMIT_OVERUSE_MAX.get();
    }

    public static boolean limitOveruseChanceEnabled() {
        return Config.LIMIT_OVERUSE_CHANCE_ENABLED.get();
    }

    public static int getLimitOveruseChanceAggressiveness() {
        return Config.LIMIT_OVERUSE_CHANCE_AGGRESSIVENESS.get();
    }

    public static boolean globalRestockCooldownEnabled() {
        return Config.ENABLE_GLOBAL_RESTOCK_COOLDOWN.get();
    }

    public static int getGlobalRestockCooldownMinTicks() {
        return Config.GLOBAL_RESTOCK_COOLDOWN_MIN_TICKS.get();
    }

    public static int getGlobalRestockCooldownMaxTicks() {
        return Config.GLOBAL_RESTOCK_COOLDOWN_MAX_TICKS.get();
    }

    public static boolean freezeProfessionTradesEnabled() {
        return Config.FREEZE_PROFESSION_TRADES.get();
    }
}