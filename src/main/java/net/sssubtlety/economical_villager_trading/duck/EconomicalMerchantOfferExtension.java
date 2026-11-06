package net.sssubtlety.economical_villager_trading.duck;

/**
 * Duck-interface implemented by MerchantOfferMixin to track per-offer
 * chance-lockout / limit-lockout state without touching vanilla fields (uses/maxUses/demand).
 *
 * There are two independent lockout states an offer can be in when closed early:
 *  - "locked by chance" (economical_villager_trading$isLockedByChance): the offer was closed by
 *    the enableOveruseLockout "random" mechanic. It is frozen exactly as it was (same use count,
 *    same price) and can ONLY reopen via its own lockoutExpireTick timer - normal restocks,
 *    opposite-group trading, and villager level-ups must all ignore it while this is true.
 *  - "locked by limit" (economical_villager_trading$isLockedByLimit): the offer was closed by the
 *    enableLimitOveruse mechanic reaching its limit (a "full" closure). It behaves like an
 *    ordinary out-of-stock offer - reopenable by trading or leveling up as normal - but ALSO gets
 *    its own optional global time-based restock via limitLockoutExpireTick, when
 *    enableGlobalRestockCooldown is on.
 * The two are mutually exclusive on any given closure (see VillagerMixin#applyOveruseLockoutIfNeeded)
 * but both fields are kept independently since a trade can be closed and reopened many times over
 * its lifetime, alternating between the two outcomes.
 *
 * IMPORTANT: this must live OUTSIDE the "net.sssubtlety.economical_villager_trading.mixin"
 * package tree entirely - not just in a subpackage of it. The "package" entry in
 * economical_villager_trading.mixins.json is a wildcard ("...mixin.*") that matches every
 * subpackage too, so putting this in e.g. "...mixin.duck" is still caught by it. Mixin treats
 * every class under that tree as a mixin implementation detail merged into its target and
 * refuses to let it be loaded directly. Casting to a type from that package
 * (e.g. "(EconomicalMerchantOfferExtension) offer") forces the JVM to load it directly, which
 * throws IllegalClassLoadError. This interface is a plain (non-@Mixin) type referenced from
 * outside the transformer, so it needs to sit in a completely separate package, as a sibling
 * of "mixin" rather than a child of it.
 */
public interface EconomicalMerchantOfferExtension {
    boolean economical_villager_trading$isLockedByChance();

    void economical_villager_trading$setLockedByChance(boolean locked);

    long economical_villager_trading$getLockoutExpireTick();

    void economical_villager_trading$setLockoutExpireTick(long gameTime);

    boolean economical_villager_trading$isLockedByLimit();

    void economical_villager_trading$setLockedByLimit(boolean locked);

    long economical_villager_trading$getLimitLockoutExpireTick();

    void economical_villager_trading$setLimitLockoutExpireTick(long gameTime);
}