package net.sssubtlety.economical_villager_trading;

import net.minecraft.network.chat.Component;

public final class Util {
    private Util() {
        throw new UnsupportedOperationException();
    }

    public static Component replace(Component text, String regex, String replacement) {
        String string = text.getString();
        string = string.replaceAll(regex, replacement);
        return Component.literal(string).setStyle(text.getStyle());
    }
}
