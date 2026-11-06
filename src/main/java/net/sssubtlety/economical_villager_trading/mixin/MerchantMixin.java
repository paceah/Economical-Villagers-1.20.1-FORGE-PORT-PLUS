package net.sssubtlety.economical_villager_trading.mixin;

import net.sssubtlety.economical_villager_trading.config.ConfigManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalInt;
import java.util.stream.Collectors;

@Mixin(Merchant.class)
interface MerchantMixin extends Merchant {
    @Shadow
    MerchantOffers getOffers();

    @Shadow
    int getVillagerXp();

    @Shadow
    boolean showProgressBar();

    @Shadow
    boolean canRestock();

    /**
     * @author sssubtlety
     * @reason strip the "special price" (hero-of-the-village discount) indicator from offers
     * sent to the client when special discounts are disabled in the config, without mutating
     * the merchant's actual server-side offer state.
     */
    @Overwrite
    default void openTradingScreen(Player player, Component displayName, int level) {
        OptionalInt optionalint = player.openMenu(new SimpleMenuProvider((syncId, inventory, p) ->
                new MerchantMenu(syncId, inventory, this), displayName));

        if (optionalint.isPresent()) {
            MerchantOffers merchantoffers = this.getOffers();
            if (!merchantoffers.isEmpty()) {
                if (!ConfigManager.specialDiscountsEnabled()) {
                    merchantoffers = merchantoffers.stream()
                            .map(offer -> {
                                final MerchantOffer nonSpecialOffer = new MerchantOffer(offer.createTag());
                                nonSpecialOffer.resetSpecialPriceDiff();
                                return nonSpecialOffer;
                            })
                            .collect(Collectors.toCollection(MerchantOffers::new));
                }

                player.sendMerchantOffers(
                        optionalint.getAsInt(), merchantoffers, level,
                        this.getVillagerXp(), this.showProgressBar(), this.canRestock()
                );
            }
        }
    }
}