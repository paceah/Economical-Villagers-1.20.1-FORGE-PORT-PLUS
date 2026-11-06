package net.sssubtlety.economical_villager_trading.mixin;

import org.jspecify.annotations.NonNull;

import net.sssubtlety.economical_villager_trading.config.ConfigManager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(AbstractVillager.class)
abstract class AbstractVillagerMixin extends AgeableMob implements Merchant {
    @Shadow
    @NonNull
    public abstract MerchantOffers getOffers();

    @Shadow
    public abstract boolean showProgressBar();

    // package-private (not private) so VillagerMixin/AbstractVillagerMixin subclasses in this
    // package can reach it if ever needed; only touched here for now.
    @Shadow
    protected MerchantOffers offers;

    // Keyed by profession registry id, per-entity: the FIRST set of trades this individual villager
    // ever rolled for a given profession. Restored verbatim any time updateTrades() would otherwise
    // regenerate a fresh (differently-randomized) set of trades for that profession - which normally
    // happens after ResetProfession nulls `offers` (job-site block broken while at level<=1, xp==0).
    // This is what closes the "break/replace job site to reroll starting trades" exploit while still
    // leaving normal in-place level-up trade additions (which never null `offers`) completely untouched.
    //
    // IMPORTANT: do NOT use a field initializer (`= new HashMap<>()`). The dummy constructor below
    // throws before field initializers run, and Mixin does not reliably transplant them into the
    // real target constructors. The map is therefore created lazily via getFrozenOffers().
    @Unique
    private Map<ResourceLocation, CompoundTag> economical_villager_trading$frozenOffersByProfession;

    protected AbstractVillagerMixin() {
        // noinspection ConstantConditions
        super(null, null);
        throw new IllegalStateException("MerchantEntityMixin's dummy constructor called!");
    }

    @Unique
    private Map<ResourceLocation, CompoundTag> getFrozenOffers() {
        if (this.economical_villager_trading$frozenOffersByProfession == null) {
            this.economical_villager_trading$frozenOffersByProfession = new HashMap<>();
        }
        return this.economical_villager_trading$frozenOffersByProfession;
    }

    // handler stub to be overriden in VillagerMixin
    @Inject(method = "notifyTrade", at = @At("TAIL"))
    protected void tryRefreshPostTrade(MerchantOffer offer, CallbackInfo ci) { }

    // Fires only inside the `if (this.offers == null) { ...; this.updateTrades(); }` branch of
    // getOffers() - i.e. only when trades are genuinely being (re)generated from scratch, never on
    // ordinary level-ups (those call updateTrades() directly elsewhere without nulling `offers` first).
    @Inject(
            method = "getOffers",
            at = @At(
                    value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/entity/npc/AbstractVillager;updateTrades()V"
            )
    )
    private void economical_villager_trading$freezeOrRestoreOffers(CallbackInfoReturnable<MerchantOffers> cir) {
        if (!ConfigManager.freezeProfessionTradesEnabled()) {
            return;
        }

        if (!(this instanceof VillagerDataHolder dataHolder)) {
            return;
        }

        final VillagerProfession profession = dataHolder.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return;
        }

        final ResourceLocation professionId = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
        if (professionId == null) {
            return;
        }

        final Map<ResourceLocation, CompoundTag> frozenMap = this.getFrozenOffers();
        final CompoundTag frozen = frozenMap.get(professionId);
        if (frozen == null) {
            // first time this individual villager has ever rolled trades for this profession: remember them
            frozenMap.put(professionId, this.offers.createTag());
        } else {
            // this profession's trades were reset (offers nulled) and just re-rolled randomly by
            // updateTrades() above; discard that roll and restore the original pattern instead
            this.offers = new MerchantOffers(frozen);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void economical_villager_trading$saveFrozenOffers(CompoundTag tag, CallbackInfo ci) {
        final Map<ResourceLocation, CompoundTag> frozenMap = this.getFrozenOffers();
        if (frozenMap.isEmpty()) {
            return;
        }

        final CompoundTag frozenTag = new CompoundTag();
        for (final var entry : frozenMap.entrySet()) {
            frozenTag.put(entry.getKey().toString(), entry.getValue());
        }
        tag.put("EconomicalFrozenOffers", frozenTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void economical_villager_trading$loadFrozenOffers(CompoundTag tag, CallbackInfo ci) {
        final Map<ResourceLocation, CompoundTag> frozenMap = this.getFrozenOffers();
        frozenMap.clear();

        if (!tag.contains("EconomicalFrozenOffers", 10)) {
            return;
        }

        final CompoundTag frozenTag = tag.getCompound("EconomicalFrozenOffers");
        for (final String key : frozenTag.getAllKeys()) {
            final ResourceLocation professionId = ResourceLocation.tryParse(key);
            if (professionId != null) {
                frozenMap.put(professionId, frozenTag.getCompound(key));
            }
        }
    }
}