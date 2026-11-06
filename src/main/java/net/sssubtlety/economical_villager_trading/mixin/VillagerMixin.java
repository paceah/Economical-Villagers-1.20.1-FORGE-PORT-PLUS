package net.sssubtlety.economical_villager_trading.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.sssubtlety.economical_villager_trading.EconomicalVillagerTrading;
import net.sssubtlety.economical_villager_trading.config.ConfigManager;
import net.sssubtlety.economical_villager_trading.mixin.accessor.MerchantOfferAccessor;
import net.sssubtlety.economical_villager_trading.duck.EconomicalMerchantOfferExtension;

import org.objectweb.asm.Opcodes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
abstract class VillagerMixin extends AbstractVillagerMixin implements ReputationEventHandler {
    @Unique
    private static boolean isBuyTrade(MerchantOffer offer) {
        // Native tag check, replacing Fabric API's TagUtil.isIn(...)
        return offer.getResult().getItem().builtInRegistryHolder().is(EconomicalVillagerTrading.BUY_TRADE_CURRENCIES);
    }

    // NOTE: deliberately NOT named "shouldRestock" - this mixin class also has @Inject(method =
    // "shouldRestock", ...) targeting the vanilla instance method Villager#shouldRestock(). Giving
    // a @Unique helper the exact same name causes Mixin's injector to get confused about which
    // "shouldRestock" it's checking the static-ness of, throwing InvalidInjectionException
    // ('static' modifier of handler method does not match target) even though the actual @Inject
    // handlers are correctly non-static.
    @Unique
    private static boolean matchesRestockGroup(MerchantOffer offer, boolean buys) {
        return buys == isBuyTrade(offer);
    }

    // How many emeralds actually changed hands in ONE use of this offer. Sell-to-villager offers
    // (isBuyTrade == true) have the emerald stack AS their result; buy-from-villager offers have the
    // emerald stack as costA (vanilla convention). Used to weight cross-group demand pressure so a
    // trade paid for with 6 emeralds counts ~6x as much as a 1-emerald trade, not flatly "+1" either way.
    @Unique
    private static int economical_villager_trading$emeraldWeight(MerchantOffer offer) {
        return Math.max(1, isBuyTrade(offer) ? offer.getResult().getCount() : offer.getCostA().getCount());
    }

    // these are "parameters" passed to `restock` and `updateDemandBonus`
    @Unique private ThreadLocal<Boolean> restockingBuys;
    @Unique private ThreadLocal<Boolean> restockingAll;
    @Unique private ThreadLocal<Boolean> trackRestocks;
    // The one specific offer that was just traded, if any. balancePriceAdjustments only ever grows
    // THIS offer's demand (never sweeps the whole group) - null means "no growth this call", used
    // by restock(boolean)'s internal updateDemand() call, which must never cause growth by itself.
    @Unique private ThreadLocal<MerchantOffer> tradedOffer;

    // Rolling per-villager trade-volume counters (see restock(boolean) / decayDemand), used to keep
    // a big pile of sells from getting almost fully forgiven by a few unrelated buys, and vice versa.
    // NOT ThreadLocal - this is persistent villager state, not a per-call "parameter".
    @Unique private int economical_villager_trading$sellVolume = 0;
    @Unique private int economical_villager_trading$buyVolume = 0;

    @SuppressWarnings("FieldMayBeFinal")
    @Shadow private long lastRestockGameTime;

    @Shadow public abstract void restock();

    @Shadow private void updateDemand() {}

    @Shadow private void resendOffersToTradingPlayer() {}

    @Shadow public abstract net.minecraft.world.entity.npc.VillagerData getVillagerData();

    // overrides handler stub in AbstractVillagerMixin
    @Override
    protected void tryRefreshPostTrade(MerchantOffer offer, CallbackInfo ci) {
        this.applyOveruseLockoutIfNeeded(offer);

        // Recalculate demand for the traded offer's OWN group (buy or sell) right after every single use,
        // not only when a trade of the opposite group happens to close later. Previously, price for a given
        // offer only updated once some unrelated offer of the opposite type ran out - which looked like
        // prices jumping on the wrong item and not rising while you were actually using a trade.
        final boolean closedIsBuy = isBuyTrade(offer);
        final int weight = economical_villager_trading$emeraldWeight(offer);

        if (closedIsBuy) {
            this.economical_villager_trading$sellVolume += weight;
        } else {
            this.economical_villager_trading$buyVolume += weight;
        }

        // Immediately push the OPPOSITE group's demand back toward baseline on every single trade,
        // instead of only when some offer happens to close and go through restock(). This is what
        // lets buying arrows actually bring an untouched, still-open potato trade back down in real
        // time, rather than requiring the potato trade itself to run out before anything changes.
        this.economical_villager_trading$applyCrossGroupPressure(closedIsBuy, weight);

        this.restockingBuys.set(closedIsBuy);
        this.tradedOffer.set(offer);
        try {
            this.updateDemand();
        } finally {
            this.restockingBuys.remove();
            this.tradedOffer.remove();
        }
        this.updateTradeGui();
        org.slf4j.LoggerFactory.getLogger("EVT-DEBUG").info(
                "[EVT-DEBUG] result={} closedIsBuy={} uses={} maxUses={} demand={} costA={}",
                offer.getResult(), closedIsBuy, offer.getUses(), offer.getMaxUses(), offer.getDemand(), offer.getCostA()
        );

        if (offer.isOutOfStock()) {
            final boolean buys = !closedIsBuy;
            this.restockingBuys.set(buys);
            try {
                this.restock(buys);
            } finally {
                this.restockingBuys.remove();
            }
        }
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void initFields(CallbackInfo ci) {
        this.restockingBuys = ThreadLocal.withInitial(() -> false);
        this.restockingAll = ThreadLocal.withInitial(() -> false);
        this.tradedOffer = new ThreadLocal<>();
        this.trackRestocks = ThreadLocal.withInitial(() -> true);
    }

    // inject after lastRestockCheckTime:
    // - after lastRestockCheckTime is set
    // - before resetNumberOfRestocks() is called, which resets all trade uses
    @Inject(
            method = "shouldRestock", cancellable = true,
            at = @At(
                    value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/npc/Villager;lastRestockCheckDayTime:J"
            )
    )
    private void shouldNotRestockUnlessAllTradesDisabled(CallbackInfoReturnable<Boolean> cir) {
        if (
                ConfigManager.doVanillaRestocks()
                        || this.level().getGameTime() > this.lastRestockGameTime + 12000L && this.areAllTradesDisabled()
        ) {
            // set restockingAll for updateDemand (in catchUpDemand in resetNumberOfRestocks)
            this.restockingAll.set(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    // Remove `restockingAll` after `Villager#resetNumberOfRestocks`, because if `resetNumberOfRestocks` is
    // reached, `shouldNotRestockUnlessAllTradesDisabled` set `refreshingAll`.
    // `shouldRestock` will return `false` and prevent `restock` from being called in this case,
    // so this won't prevent `restock` from getting `refreshingAll`.
    @Inject(
            method = "shouldRestock",
            at = @At(
                    shift = At.Shift.AFTER,
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/Villager;resetNumberOfRestocks()V"
            )
    )
    private void removeRefreshingAll(CallbackInfoReturnable<Boolean> cir) {
        this.restockingAll.remove();
    }

    @WrapWithCondition(
            method = "restock",
            at = @At(
                    value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/npc/Villager;lastRestockGameTime:J"
            )
    )
    private boolean ifTracking(Villager instance, long value) {
        return this.trackRestocks.get();
    }

    @WrapWithCondition(
            method = "restock",
            at = @At(
                    value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/npc/Villager;numberOfRestocksToday:I"
            )
    )
    private boolean ifTracking(Villager instance, int value) {
        return this.trackRestocks.get();
    }

    // Vanilla Villager#restock() unconditionally calls offer.resetUses() on EVERY offer. This
    // covers both the normal daily restock cycle AND increaseMerchantCareer's level-up refresh
    // (see refreshAllPostLevelUp below), which is exactly why a trade "locked by chance" (the
    // enableOveruseLockout "random" mini-lockout, see applyOveruseLockoutIfNeeded) must be
    // protected here: such a trade is frozen at an exact use count/price and may ONLY reopen via
    // its own recovery timer (tickOveruseLockoutRecovery) - never by a normal restock, and never
    // by the villager leveling up. Trades locked "by limit" (a full closure from enableLimitOveruse)
    // are NOT touched here - they're meant to keep reopening normally via trading/leveling, on top
    // of their own optional global cooldown (tickGlobalRestockCooldown).
    @Redirect(
            method = "restock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/trading/MerchantOffer;resetUses()V"
            )
    )
    private void economical_villager_trading$skipResetIfLockedByChance(MerchantOffer offer) {
        final var ext = (EconomicalMerchantOfferExtension) offer;
        if (ext.economical_villager_trading$isLockedByChance()) {
            return;
        }

        offer.resetUses();
        // If this offer had also been earmarked for the global limit-cooldown, a normal
        // restock/level-up just reopened it directly - clear that bookkeeping so
        // tickGlobalRestockCooldown doesn't later try to "reopen" an already-open offer.
        ext.economical_villager_trading$setLockedByLimit(false);
        ext.economical_villager_trading$setLimitLockoutExpireTick(-1L);
    }

    @Inject(method = "restock", at = @At("RETURN"))
    private void updateTradeGui(CallbackInfo ci) {
        // Vanilla's own restock() can reset offer.uses back to 0 through paths that never go
        // through our restock(boolean) helper above (e.g. increaseMerchantCareer calling the
        // real restock() directly). Whenever that happens, decay the offer's demand toward 0
        // instead of hard-resetting it - a fully-reopened trade keeps a fraction of whatever
        // price swing it had built up, rather than snapping straight back to the base price.
        for (final MerchantOffer offer : this.getOffers()) {
            if (offer.getUses() == 0) {
                this.economical_villager_trading$decayDemand(offer, isBuyTrade(offer));
            }
        }
        this.updateTradeGui();
    }

    @Inject(method = "increaseMerchantCareer", at = @At("RETURN"))
    private void refreshAllPostLevelUp(CallbackInfo info) {
        this.restockingAll.set(true);

        this.trackRestocks.set(false);
        try {
            this.restock();
        } finally {
            this.trackRestocks.remove();
        }

        if (ConfigManager.villagerLevelingWithGuiOpenEnabled()) {
            this.updateTradeGui();
        }
    }

    @Inject(method = "updateDemand", at = @At("HEAD"), cancellable = true)
    private void balancePriceAdjustments(CallbackInfo ci) {
        final double sellMultiplier = ConfigManager.getVillagerTradeAdjustBalanceSell();
        final double buyMultiplier = ConfigManager.getVillagerTradeAdjustBalanceBuy();

        final MerchantOffer offer = this.tradedOffer.get();

        if (offer != null) {
            // Growth only ever applies to the ONE offer that was actually just traded (carried via
            // tradedOffer), never to the rest of its group. Sell-to-villager and buy-from-villager
            // trades now use fully independent multipliers, so repeatedly selling something doesn't
            // have to raise prices at the same rate repeatedly buying something else does.
            final double balanceMultiplier = isBuyTrade(offer) ? sellMultiplier : buyMultiplier;

            // balanceMultiplier < 0 means use vanilla bonuses for this offer's group
            if (balanceMultiplier >= 0) {
                ci.cancel();
                ((MerchantOfferAccessor) offer).economical_villager_trading$setDemand(
                        balanceMultiplier == 0 ? 0 : this.getAdjustedDemand(offer, balanceMultiplier)
                );
            }
            return;
        }

        // This call also fires from restock(boolean)'s internal updateDemand() (vanilla behavior
        // we're overriding) with no traded offer set at all - restocked offers get their demand
        // decayed separately in restock(boolean)/updateTradeGui, not grown here. This must ALWAYS
        // cancel vanilla's own updateDemand(), regardless of sell/buyMultiplier sign: restock(boolean)
        // runs every time ANY offer on this villager closes (see restock(boolean) call site in
        // tryRefreshPostTrade), for the WHOLE villager, not just the offer that was actually traded.
        // If vanilla's real updateDemand() were allowed through here, it sweeps EVERY offer with
        // uses > 0 using its own formula - which is exactly what let closing one unrelated trade
        // (e.g. finishing a stack of arrows) silently bump the demand on a completely different,
        // untouched trade (e.g. bread/sticks) that merely happened to have accumulated uses already.
        // Growth must only ever happen through the offer != null branch above, tied to one specific
        // just-completed trade - never through this bulk/internal path, no matter how
        // villagerTradeAdjustBalanceSell/Buy are configured.
        ci.cancel();
        if (sellMultiplier == 0 || buyMultiplier == 0) {
            for (final MerchantOffer other : this.getOffers()) {
                final boolean groupDisabled = isBuyTrade(other) ? sellMultiplier == 0 : buyMultiplier == 0;
                if (groupDisabled) {
                    ((MerchantOfferAccessor) other).economical_villager_trading$setDemand(0);
                }
            }
        }
    }

    @ModifyArg(method = "rewardTradeXp", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ExperienceOrb;<init>(Lnet/minecraft/world/level/Level;DDDI)V"
    ))
    private int multiplyXp(int originalValue) {
        return (int)(ConfigManager.getVillagerXpMultiplier() * originalValue);
    }

    @ModifyExpressionValue(
            method = "customServerAiStep",
            allow = 1, require = 1,
            slice = @Slice(to = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/Villager;increaseMerchantCareer()V"
            )),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;isTrading()Z")
    )
    private boolean allowLevelingWithCustomerIfEnabled(boolean hasCustomer) {
        return hasCustomer && !ConfigManager.villagerLevelingWithGuiOpenEnabled();
    }

    // Tick both lockout-recovery timers once per AI step, alongside vanilla's own per-tick
    // bookkeeping in customServerAiStep (trade timers, reputation events, etc.). Each is a no-op
    // unless its own config option(s) are on, so together they never affect vanilla behavior or
    // each other when left at defaults.
    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    private void tickOveruseRecovery(CallbackInfo ci) {
        this.tickOveruseLockoutRecovery();
        this.tickGlobalRestockCooldown();
    }

    @Unique
    private void restock(boolean buys) {
        this.updateDemand();

        if (this.hasWorkStation()) {
            if (this.areAllTradesDisabled()) {
                // Safety net: if literally every trade is closed, reopen everything that isn't
                // frozen by the "locked by chance" mini-lockout, regardless of group/randomness,
                // so the villager can never end up permanently unusable because the random
                // partial restock below happened to pick nothing. Chance-locked offers are still
                // deliberately skipped here - they can only ever come back via their own timer.
                for (final MerchantOffer offer : this.getOffers()) {
                    if (
                            offer.isOutOfStock()
                                    && !((EconomicalMerchantOfferExtension) offer).economical_villager_trading$isLockedByChance()
                    ) {
                        offer.resetUses();
                        this.economical_villager_trading$decayDemand(offer, isBuyTrade(offer));
                        this.economical_villager_trading$clearLimitLock(offer);
                    }
                }
            } else {
                this.economical_villager_trading$partialRestock(buys);
            }

            if (ConfigManager.netVolumeDemandBalanceEnabled()) {
                final double trackingDecay = ConfigManager.getNetVolumeTrackingDecay();
                this.economical_villager_trading$sellVolume =
                        (int) Math.round(this.economical_villager_trading$sellVolume * trackingDecay);
                this.economical_villager_trading$buyVolume =
                        (int) Math.round(this.economical_villager_trading$buyVolume * trackingDecay);
            }
        }

        this.resendOffersToTradingPlayer();
        this.updateTradeGui();
    }

    // Reopens only a random subset of the closed trades belonging to the `buys` group (the group
    // OPPOSITE the one that just triggered this restock by running dry), instead of reopening every
    // closed trade in that group at once. Count is picked per-event between restockTradesMin and
    // restockTradesMax (inclusive); equal min/max gives a fixed non-random count. This is what stops
    // "buy/sell a handful of cheap junk trades -> everything on the other side instantly reopens".
    // Offers locked by chance (enableOveruseLockout) are never eligible - they can only reopen via
    // their own recovery timer, never by trading on some other offer.
    @Unique
    private void economical_villager_trading$partialRestock(boolean buys) {
        final java.util.List<MerchantOffer> eligible = new java.util.ArrayList<>();
        for (final MerchantOffer offer : this.getOffers()) {
            if (
                    offer.isOutOfStock() && matchesRestockGroup(offer, buys)
                            && !((EconomicalMerchantOfferExtension) offer).economical_villager_trading$isLockedByChance()
            ) {
                eligible.add(offer);
            }
        }

        if (eligible.isEmpty()) {
            return;
        }

        final java.util.List<MerchantOffer> toReopen;
        if (ConfigManager.randomPartialRestockEnabled()) {
            final int min = Math.max(0, Math.min(ConfigManager.getRestockTradesMin(), eligible.size()));
            final int max = Math.max(min, Math.min(ConfigManager.getRestockTradesMax(), eligible.size()));
            final int count = (min == max) ? min : min + this.getRandom().nextInt(max - min + 1);

            java.util.Collections.shuffle(eligible, new java.util.Random(this.getRandom().nextLong()));
            toReopen = eligible.subList(0, count);
        } else {
            toReopen = eligible;
        }

        for (final MerchantOffer offer : toReopen) {
            offer.resetUses();
            this.economical_villager_trading$decayDemand(offer, buys);
            this.economical_villager_trading$clearLimitLock(offer);
        }
    }

    @Unique
    private void updateTradeGui() {
        if (
                this.getTradingPlayer() != null &&
                        this.getTradingPlayer().containerMenu instanceof MerchantMenu
        ) {
            this.getTradingPlayer().sendMerchantOffers(
                    this.getTradingPlayer().containerMenu.containerId, this.getOffers(),
                    this.getVillagerData().getLevel(), this.getVillagerXp(),
                    this.showProgressBar(), this.canRestock()
            );
        }
    }

    // `restockedGroupIsBuys` identifies which group `offer` itself belongs to (true = the "buy from
    // villager" group, e.g. arrows; false = the "sell to villager" group, e.g. sticks/flint/feathers).
    @Unique
    private void economical_villager_trading$decayDemand(MerchantOffer offer, boolean restockedGroupIsBuys) {
        final double decayFactor = ConfigManager.getDemandRestockDecayFactor();
        final double effectiveDecayFactor = ConfigManager.netVolumeDemandBalanceEnabled()
                ? decayFactor * this.economical_villager_trading$netBalanceFactor(restockedGroupIsBuys)
                : decayFactor;
        final int decayed = (int) Math.round(offer.getDemand() * (1.0 - effectiveDecayFactor));
        ((MerchantOfferAccessor) offer).economical_villager_trading$setDemand(decayed);
    }

    // Scales demand decay down when the group that just triggered this restock (by running dry) has
    // seen far less recent trade volume than the group being restocked. E.g. if you sold in bulk many
    // times (building up sellVolume) and only bought a couple of arrows afterward (tiny buyVolume),
    // reopening the sell trades should barely decay their price swing - a few unrelated purchases
    // shouldn't be able to "pay off" a huge pile of selling. Returns a multiplier in
    // [1 - netVolumeDecayDampening, 1.0] applied on top of demandRestockDecayFactor.
    @Unique
    private double economical_villager_trading$netBalanceFactor(boolean restockedGroupIsBuys) {
        final int triggeringVolume = restockedGroupIsBuys
                ? this.economical_villager_trading$buyVolume
                : this.economical_villager_trading$sellVolume;
        final int restockedVolume = restockedGroupIsBuys
                ? this.economical_villager_trading$sellVolume
                : this.economical_villager_trading$buyVolume;

        final double ratio = (double) triggeringVolume / Math.max(1, restockedVolume);
        // dampeningFloor used to hard-cap how much decay could ever be suppressed (e.g. always at
        // least 30% at default settings), which is what let prices asymptotically stall above their
        // base value even under heavy sustained opposite-side trading. With netVolumeDecayDampening
        // at its new default of 1.0, dampeningFloor is 0 and the full [0, 1] ratio range is used.
        final double dampeningFloor = 1.0 - ConfigManager.getNetVolumeDecayDampening();
        return Math.min(1.0, Math.max(dampeningFloor, ratio));
    }

    // Immediately pulls demand on every currently OPEN offer of the OPPOSITE group toward baseline
    // (0), by an amount proportional to `weight` (see emeraldWeight). This is the real-time
    // counterpart to the restock-time decay above: it's what lets ongoing opposite-side trading
    // actually walk a still-open, untouched trade's price back toward baseline while you keep
    // playing, instead of that only being possible when that specific trade happens to close and
    // restock on its own.
    //
    // Deliberately SKIPS offers that are currently closed/out-of-stock: those already get corrected
    // exactly once, when they reopen, via decayDemand() in restock(boolean). If this also nudged
    // closed offers on every unrelated trade while they sat there unseen, that value would silently
    // drift for a while and THEN get decayed again on top when it finally reopened - two different
    // mechanisms stacking on a value the player never saw change, which is exactly what produced the
    // unexplained "price jumped on an item I wasn't even touching" swings. Closed offers now only
    // ever change at one well-defined moment (restock), so the number on reopen is always explainable
    // from that single step.
    @Unique
    private void economical_villager_trading$applyCrossGroupPressure(boolean closedIsBuy, int weight) {
        if (!ConfigManager.crossGroupPressureEnabled()) {
            return;
        }

        final int rawDelta = (int) Math.round(weight * ConfigManager.getCrossGroupPressureStrength());
        if (rawDelta <= 0) {
            return;
        }

        // Clamp the per-trade step. `weight` (see emeraldWeight) is derived from the trade's CURRENT
        // price, which already includes whatever demand that same trade has built up - so a heavily
        // marked-up trade's own inflation feeds back into how hard it pushes every other open offer.
        // Left unclamped, that feedback loop lets one click on an expensive trade swing an untouched
        // offer's demand almost all the way to baseline (or past it) in a single step, which is what
        // shows up as prices "teleporting" on items you never touched. The clamp caps only the STEP
        // SIZE, not the strength setting itself - repeated trades still add up over time exactly as
        // before, they just can't do it all in one hit.
        final int maxStep = ConfigManager.getCrossGroupPressureMaxStep();
        final int delta = maxStep > 0 ? Math.min(rawDelta, maxStep) : rawDelta;

        for (final MerchantOffer other : this.getOffers()) {
            if (isBuyTrade(other) != closedIsBuy && !other.isOutOfStock()) {
                final int currentDemand = other.getDemand();
                final int newDemand;
                if (currentDemand > 0) {
                    newDemand = Math.max(0, currentDemand - delta);
                } else if (currentDemand < 0) {
                    newDemand = Math.min(0, currentDemand + delta);
                } else {
                    newDemand = 0;
                }
                ((MerchantOfferAccessor) other).economical_villager_trading$setDemand(newDemand);
            }
        }
    }

    @Unique
    private int getAdjustedDemand(MerchantOffer offer, double balanceMultiplier) {
        // FIXED: the previous version recomputed `2 * (uses - growthStartUses)` from the offer's
        // ABSOLUTE use count on every single trade, then ADDED that onto the existing demand each
        // time. Since this fires on every trade (not once per day like vanilla), and uses grows by
        // 1 each call, that produced an accelerating (roughly quadratic) price ramp instead of a
        // steady one - and, worse, went NEGATIVE (reducing demand while the player was actively
        // selling) any time uses was still below growthStartUses, e.g. right after a restock reset
        // uses back to 0.
        //
        // Fixed version: a flat, constant step per use once past the threshold. No acceleration,
        // and never negative - below the threshold, demand simply doesn't move yet.
        final int growthStartUses = ConfigManager.getDemandGrowthStartUses();
        if (offer.getUses() <= growthStartUses) {
            return offer.getDemand();
        }
        final int step = (int) Math.round(2 * balanceMultiplier);
        return offer.getDemand() + step;
    }

    @Unique
    private boolean areAllTradesDisabled() {
        for (final MerchantOffer offer : this.getOffers()) {
            if (!offer.isOutOfStock()) {
                return false;
            }
        }

        return true;
    }

    @Unique
    private boolean hasWorkStation() {
        return this.getBrain().getMemory(MemoryModuleType.JOB_SITE).isPresent();
    }

    // Called right after a successful trade (from notifyTrade, before the existing restock-on-empty
    // check above). Decides whether this specific offer should be force-closed early, via either
    // of two mutually-exclusive mechanics, and marks WHICH one closed it so the rest of the mod
    // (restocking, level-ups, the toast shown to the player) can treat the two outcomes differently:
    //
    //  - limitOveruse (a "full" closure - sales-limit reached): a per-offer use-count window (hard
    //    cap, or chance-based within the window). Reopens the normal ways (trading a matching group
    //    of closed offers, or the villager leveling up) plus, optionally, its own global time-based
    //    restock cooldown (see enableGlobalRestockCooldown/tickGlobalRestockCooldown). Its price
    //    keeps adjusting normally on the villager's usual demand mechanics even while closed.
    //
    //  - overuseLockoutAggressiveness (a "random" closure): a flat chance on every use, only rolled
    //    if limitOveruse didn't already close the offer. Freezes the offer exactly as it was (use
    //    count and price) and, if enableLockoutRecovery is on, reopens ONLY via its own recovery
    //    timer - normal restocks, opposite-group trading, and villager level-ups all ignore it
    //    entirely for as long as it's locked (see the resetUses() redirect above).
    //
    // Both share the same aggressiveness percentage on purpose, per the requested design, rather
    // than adding a second separate chance value.
    @Unique
    private void applyOveruseLockoutIfNeeded(MerchantOffer offer) {
        if (offer.isOutOfStock()) {
            return;
        }

        final var ext = (EconomicalMerchantOfferExtension) offer;
        final int uses = offer.getUses();
        boolean lockedByLimit = false;
        boolean lockedByChance = false;

        if (ConfigManager.limitOveruseEnabled()) {
            final int min = ConfigManager.getLimitOveruseMin();
            final int max = ConfigManager.getLimitOveruseMax();

            if (uses >= max) {
                // guaranteed close once the window's upper bound is reached
                lockedByLimit = true;
            } else if (uses >= min && ConfigManager.limitOveruseChanceEnabled()) {
                lockedByLimit = this.getRandom().nextInt(100) < ConfigManager.getLimitOveruseChanceAggressiveness();
            }
            // uses < min, or (uses in [min, max) with chance mode off): hard-limit mode simply
            // waits for uses >= max, handled above - no roll needed here.
        }

        if (!lockedByLimit && ConfigManager.overuseLockoutEnabled()) {
            lockedByChance = this.getRandom().nextInt(100) < ConfigManager.getOveruseLockoutAggressiveness();
        }

        if (!lockedByLimit && !lockedByChance) {
            return;
        }

        offer.setToOutOfStock();

        if (lockedByLimit) {
            // "Full" closure. Never chance-locked, so it stays reopenable by trading/leveling as normal.
            ext.economical_villager_trading$setLockedByChance(false);
            ext.economical_villager_trading$setLockoutExpireTick(-1L);

            this.economical_villager_trading$notifyLockoutClose(true);

            if (ConfigManager.globalRestockCooldownEnabled()) {
                final int min = ConfigManager.getGlobalRestockCooldownMinTicks();
                final int max = ConfigManager.getGlobalRestockCooldownMaxTicks();
                final int span = Math.max(1, max - min + 1);
                final long delay = min + this.getRandom().nextInt(span);

                ext.economical_villager_trading$setLockedByLimit(true);
                ext.economical_villager_trading$setLimitLockoutExpireTick(this.level().getGameTime() + delay);
            } else {
                ext.economical_villager_trading$setLockedByLimit(false);
                ext.economical_villager_trading$setLimitLockoutExpireTick(-1L);
            }
            return;
        }

        // "Random" closure. Freezes uses/price exactly as-is; only its own recovery timer (if
        // enabled) can reopen it - see the resetUses() redirect and partialRestock/areAllTradesDisabled
        // above, which all explicitly skip offers locked by chance.
        ext.economical_villager_trading$setLockedByLimit(false);
        ext.economical_villager_trading$setLimitLockoutExpireTick(-1L);

        this.economical_villager_trading$notifyLockoutClose(false);

        if (ConfigManager.lockoutRecoveryEnabled()) {
            final int min = ConfigManager.getLockoutRecoveryMinTicks();
            final int max = ConfigManager.getLockoutRecoveryMaxTicks();
            final int span = Math.max(1, max - min + 1);
            final long delay = min + this.getRandom().nextInt(span);

            ext.economical_villager_trading$setLockedByChance(true);
            ext.economical_villager_trading$setLockoutExpireTick(this.level().getGameTime() + delay);
        } else {
            ext.economical_villager_trading$setLockedByChance(false);
            ext.economical_villager_trading$setLockoutExpireTick(-1L);
        }
    }

    @Unique
    private void economical_villager_trading$clearLimitLock(MerchantOffer offer) {
        final var ext = (EconomicalMerchantOfferExtension) offer;
        ext.economical_villager_trading$setLockedByLimit(false);
        ext.economical_villager_trading$setLimitLockoutExpireTick(-1L);
    }

    // Cooldown so a player mass-trading doesn't get spammed with a toast on every single closed
    // trade - one real-time notification per player every few seconds is plenty.
    @Unique
    private static final java.util.Map<java.util.UUID, Long> economical_villager_trading$lastToastMillis =
            new java.util.concurrent.ConcurrentHashMap<>();
    @Unique
    private static final long economical_villager_trading$TOAST_COOLDOWN_MILLIS = 3000L;

    // Particles + toast (server->client packet) when a trade closes early via
    // applyOveruseLockoutIfNeeded. The toast renders on top of any open screen (unlike the
    // actionbar, which would be hidden behind the open MerchantScreen), and only the player who
    // caused the closure receives it - it's never broadcast to chat or to other players.
    // `fullClosure` picks which of the two toast messages the client shows: true for a "full"
    // closure (limitOveruse), false for a "random" closure (overuseLockoutAggressiveness).
    @Unique
    private void economical_villager_trading$notifyLockoutClose(boolean fullClosure) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    this.getX(), this.getEyeY() + 0.5, this.getZ(),
                    8, 0.2, 0.2, 0.2, 0.02
            );
        }

        if (this.getTradingPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            final java.util.UUID id = serverPlayer.getUUID();
            final long now = System.currentTimeMillis();
            final Long last = economical_villager_trading$lastToastMillis.get(id);
            if (last == null || now - last >= economical_villager_trading$TOAST_COOLDOWN_MILLIS) {
                economical_villager_trading$lastToastMillis.put(id, now);
                net.sssubtlety.economical_villager_trading.network.ModNetworking.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new net.sssubtlety.economical_villager_trading.network.ShowLockoutToastPacket(fullClosure)
                );
            }
        }
    }

    // randomly-picked expiry tick has passed. Fully independent of vanilla/mod restock logic:
    // it only ever touches offers it itself marked as locked-by-chance.
    @Unique
    private void tickOveruseLockoutRecovery() {
        if (!ConfigManager.overuseLockoutEnabled() || !ConfigManager.lockoutRecoveryEnabled()) {
            return;
        }

        final long now = this.level().getGameTime();
        boolean changed = false;

        for (final MerchantOffer offer : this.getOffers()) {
            final var ext = (EconomicalMerchantOfferExtension) offer;
            if (ext.economical_villager_trading$isLockedByChance()
                    && now >= ext.economical_villager_trading$getLockoutExpireTick()) {
                offer.resetUses();
                ext.economical_villager_trading$setLockedByChance(false);
                ext.economical_villager_trading$setLockoutExpireTick(-1L);
                changed = true;
            }
        }

        if (changed) {
            this.resendOffersToTradingPlayer();
            this.updateTradeGui();
        }
    }

    // Global, per-trade, purely time-based restock for offers that closed FULLY (locked by
    // limitOveruse reaching its limit) once their limitLockoutExpireTick has passed. Completely
    // independent of the chance-lockout recovery above and of vanilla/mod restock logic: it only
    // ever touches offers it itself marked as locked-by-limit, and treats reopening them exactly
    // like any other restock (decaying demand, not hard-resetting it), consistent with the fact
    // that limit-closed trades keep their prices dynamic while closed.
    @Unique
    private void tickGlobalRestockCooldown() {
        if (!ConfigManager.globalRestockCooldownEnabled()) {
            return;
        }

        final long now = this.level().getGameTime();
        boolean changed = false;

        for (final MerchantOffer offer : this.getOffers()) {
            final var ext = (EconomicalMerchantOfferExtension) offer;
            if (ext.economical_villager_trading$isLockedByLimit()
                    && now >= ext.economical_villager_trading$getLimitLockoutExpireTick()) {
                offer.resetUses();
                this.economical_villager_trading$decayDemand(offer, isBuyTrade(offer));
                ext.economical_villager_trading$setLockedByLimit(false);
                ext.economical_villager_trading$setLimitLockoutExpireTick(-1L);
                changed = true;
            }
        }

        if (changed) {
            this.resendOffersToTradingPlayer();
            this.updateTradeGui();
        }
    }
}