package net.sssubtlety.economical_villager_trading.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.sssubtlety.economical_villager_trading.config.ConfigManager;
import net.sssubtlety.economical_villager_trading.duck.EconomicalMerchantOfferExtension;

import org.objectweb.asm.Opcodes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.trading.MerchantOffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantOffer.class)
abstract class MerchantOfferMixin implements EconomicalMerchantOfferExtension {

    // NBT keys for persisting the 4 fields below - without this, they only ever live in memory.
    // On a dedicated server the chunk holding a villager regularly unloads/reloads (player walks
    // away, /reload, server restart...), which recreates MerchantOffer objects fresh from NBT.
    // Vanilla's own uses/maxUses state survives that fine (it's vanilla's own NBT), but these
    // custom lockout fields would silently reset to their defaults - leaving a trade that's still
    // genuinely out-of-stock (per vanilla) with no active "locked by limit"/"locked by chance"
    // bookkeeping, so tickGlobalRestockCooldown/tickOveruseLockoutRecovery in VillagerMixin would
    // never find it and it would stay closed forever, un-tracked, until something unrelated
    // (a normal restock/level-up) happened to reopen it by chance.
    @Unique
    private static final String LOCKED_BY_CHANCE_KEY = "EconomicalLockedByChance";
    @Unique
    private static final String LOCKOUT_EXPIRE_TICK_KEY = "EconomicalLockoutExpireTick";
    @Unique
    private static final String LOCKED_BY_LIMIT_KEY = "EconomicalLockedByLimit";
    @Unique
    private static final String LIMIT_LOCKOUT_EXPIRE_TICK_KEY = "EconomicalLimitLockoutExpireTick";

    @Unique
    private boolean economical_villager_trading$lockedByChance = false;

    @Unique
    private long economical_villager_trading$lockoutExpireTick = -1L;

    @Unique
    private boolean economical_villager_trading$lockedByLimit = false;

    @Unique
    private long economical_villager_trading$limitLockoutExpireTick = -1L;

    @Override
    public boolean economical_villager_trading$isLockedByChance() {
        return this.economical_villager_trading$lockedByChance;
    }

    @Override
    public void economical_villager_trading$setLockedByChance(boolean locked) {
        this.economical_villager_trading$lockedByChance = locked;
    }

    @Override
    public long economical_villager_trading$getLockoutExpireTick() {
        return this.economical_villager_trading$lockoutExpireTick;
    }

    @Override
    public void economical_villager_trading$setLockoutExpireTick(long gameTime) {
        this.economical_villager_trading$lockoutExpireTick = gameTime;
    }

    @Override
    public boolean economical_villager_trading$isLockedByLimit() {
        return this.economical_villager_trading$lockedByLimit;
    }

    @Override
    public void economical_villager_trading$setLockedByLimit(boolean locked) {
        this.economical_villager_trading$lockedByLimit = locked;
    }

    @Override
    public long economical_villager_trading$getLimitLockoutExpireTick() {
        return this.economical_villager_trading$limitLockoutExpireTick;
    }

    @Override
    public void economical_villager_trading$setLimitLockoutExpireTick(long gameTime) {
        this.economical_villager_trading$limitLockoutExpireTick = gameTime;
    }

    // Persist the 4 fields above whenever vanilla serializes this offer to NBT (chunk save,
    // villager save, and also the temp clone MerchantMixin makes for network sync - harmless
    // there, the clone is discarded either way).
    @ModifyReturnValue(method = "createTag", at = @At("RETURN"))
    private CompoundTag economical_villager_trading$saveLockoutState(CompoundTag tag) {
        tag.putBoolean(LOCKED_BY_CHANCE_KEY, this.economical_villager_trading$lockedByChance);
        tag.putLong(LOCKOUT_EXPIRE_TICK_KEY, this.economical_villager_trading$lockoutExpireTick);
        tag.putBoolean(LOCKED_BY_LIMIT_KEY, this.economical_villager_trading$lockedByLimit);
        tag.putLong(LIMIT_LOCKOUT_EXPIRE_TICK_KEY, this.economical_villager_trading$limitLockoutExpireTick);
        return tag;
    }

    // Restore the 4 fields above whenever vanilla deserializes a MerchantOffer from NBT (chunk
    // load). Missing keys (offers saved before this fix existed) just fall back to the defaults
    // set on the fields above - never locked, exactly like a freshly-generated offer.
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void economical_villager_trading$loadLockoutState(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(LOCKED_BY_CHANCE_KEY)) {
            this.economical_villager_trading$lockedByChance = tag.getBoolean(LOCKED_BY_CHANCE_KEY);
        }
        if (tag.contains(LOCKOUT_EXPIRE_TICK_KEY)) {
            this.economical_villager_trading$lockoutExpireTick = tag.getLong(LOCKOUT_EXPIRE_TICK_KEY);
        }
        if (tag.contains(LOCKED_BY_LIMIT_KEY)) {
            this.economical_villager_trading$lockedByLimit = tag.getBoolean(LOCKED_BY_LIMIT_KEY);
        }
        if (tag.contains(LIMIT_LOCKOUT_EXPIRE_TICK_KEY)) {
            this.economical_villager_trading$limitLockoutExpireTick = tag.getLong(LIMIT_LOCKOUT_EXPIRE_TICK_KEY);
        }
    }

    @ModifyExpressionValue(
            method = "getCostA",
            at = @At(
                    value = "FIELD", opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/world/item/trading/MerchantOffer;specialPriceDiff:I"
            )
    )
    private int return0IfSpecialPriceDisabled(int specialPrice) {
        return ConfigManager.specialDiscountsEnabled() ? specialPrice : 0;
    }
}