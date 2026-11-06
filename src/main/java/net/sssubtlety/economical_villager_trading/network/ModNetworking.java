package net.sssubtlety.economical_villager_trading.network;

import net.sssubtlety.economical_villager_trading.EconomicalVillagerTrading;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(EconomicalVillagerTrading.NAMESPACE, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNetworking() {
        throw new UnsupportedOperationException();
    }

    /**
     * Call this once from the mod's constructor (EconomicalVillagerTradingMod), after the mod
     * bus/event setup - order relative to other init calls doesn't matter, just needs to run once.
     */
    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                ShowLockoutToastPacket.class,
                ShowLockoutToastPacket::encode,
                ShowLockoutToastPacket::decode,
                ShowLockoutToastPacket::handle
        );
    }
}
