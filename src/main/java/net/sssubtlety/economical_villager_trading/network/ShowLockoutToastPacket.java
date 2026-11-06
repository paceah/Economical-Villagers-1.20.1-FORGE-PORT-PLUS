package net.sssubtlety.economical_villager_trading.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

/**
 * Server -> client, "a trade you were using just closed early". Carries one field, fullClosure,
 * distinguishing WHICH of the two closure outcomes happened, so the client can show the right
 * message:
 *  - true:  a "full" closure (limitOveruse reached its limit) - reopens via trading, leveling up,
 *           or (if enabled) the global restock cooldown.
 *  - false: a "random" closure (overuseLockoutAggressiveness) - frozen until its own recovery
 *           timer fires, if enabled.
 *
 * IMPORTANT: this class must never reference Minecraft/SystemToast (client-only classes) directly
 * anywhere in its own bytecode - not even inside a private method - because on a dedicated server
 * this class still gets loaded (packets are registered on both sides), and the JVM verifies every
 * method of a class when it's loaded, which would throw NoClassDefFoundError server-side even
 * though the client-only method is never actually called there. The actual toast code lives in
 * ClientToastHandler instead, referenced only via a method handle inside unsafeRunWhenOn.
 */
public final class ShowLockoutToastPacket {
    private final boolean fullClosure;

    public ShowLockoutToastPacket(boolean fullClosure) {
        this.fullClosure = fullClosure;
    }

    public static void encode(ShowLockoutToastPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.fullClosure);
    }

    public static ShowLockoutToastPacket decode(FriendlyByteBuf buf) {
        return new ShowLockoutToastPacket(buf.readBoolean());
    }

    public static void handle(ShowLockoutToastPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        final NetworkEvent.Context ctx = ctxSupplier.get();
        final boolean fullClosure = packet.fullClosure;
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientToastHandler.showLockoutToast(fullClosure))
        );
        ctx.setPacketHandled(true);
    }
}