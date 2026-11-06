package net.sssubtlety.economical_villager_trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class EconomicalVillagerTrading {
    private EconomicalVillagerTrading() {
        throw new UnsupportedOperationException();
    }

    public static final String NAMESPACE = "economical_villager_trading";
    public static final Component NAME = Component.translatable("text." + NAMESPACE + ".name");
    public static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public static final TagKey<Item> BUY_TRADE_CURRENCIES =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(NAMESPACE, "buy_trade_currencies"));
}