package net.sssubtlety.economical_villager_trading;

import net.sssubtlety.economical_villager_trading.config.ConfigManager;
import net.sssubtlety.economical_villager_trading.network.ModNetworking;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EconomicalVillagerTrading.NAMESPACE)
public final class EconomicalVillagerTradingMod {
    public EconomicalVillagerTradingMod() {
        ConfigManager.init(FMLJavaModLoadingContext.get());
        ModNetworking.register();
    }
}