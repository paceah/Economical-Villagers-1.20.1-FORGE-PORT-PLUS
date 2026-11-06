package net.sssubtlety.economical_villager_trading.network;

import net.sssubtlety.economical_villager_trading.EconomicalVillagerTrading;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only. Must stay in its own class, separate from ShowLockoutToastPacket - see the comment
 * on that class for why. Never call anything in here except from inside a DistExecutor
 * client-only block; this class is only safe to load on the physical client.
 */
public final class ClientToastHandler {
    private ClientToastHandler() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param fullClosure true for a "full" closure (limitOveruse reached its limit - reopens via
     *                    trading, leveling up, or the global restock cooldown), false for a
     *                    "random" closure (overuseLockoutAggressiveness - frozen until its own
     *                    recovery timer fires). Each uses its own pair of translation keys:
     *                    text.economical_villager_trading.trade_closed_limit(_title) and
     *                    text.economical_villager_trading.trade_closed_early(_title) respectively -
     *                    make sure your lang file actually gives those two key pairs different text,
     *                    the code itself already picks between them. Each outcome also gets its own
     *                    short vanilla sound cue so the two are distinguishable without reading the
     *                    toast: VILLAGER_NO (a disapproving grunt) for the "full" closure, and
     *                    PLAYER_HURT_FREEZE (a short icy hit) for the "random"/frozen closure.
     */
    public static void showLockoutToast(boolean fullClosure) {
        final String key = fullClosure ? "trade_closed_limit" : "trade_closed_early";
        SystemToast.addOrUpdate(
                Minecraft.getInstance().getToasts(),
                SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.translatable("text." + EconomicalVillagerTrading.NAMESPACE + "." + key + "_title"),
                Component.translatable("text." + EconomicalVillagerTrading.NAMESPACE + "." + key)
        );

        final Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (fullClosure) {
                // "Full" closure (limitOveruse hit its cap): a flat, final-sounding villager grunt.
                player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.8F);
            } else {
                // "Random" closure (overuseLockoutAggressiveness): short icy hit, evokes "frozen".
                player.playSound(SoundEvents.PLAYER_HURT_FREEZE, 1.0F, 1.4F);
            }
        }
    }
}