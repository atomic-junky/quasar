package com.childwax.quasar;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;



public record QuasarPacket(String... args) implements CustomPacketPayload {
    public static final Identifier BUNGEECORD_ID = Identifier.fromNamespaceAndPath("bungeecord", "main");
    public static final CustomPacketPayload.Type<QuasarPacket> PACKET_ID = new CustomPacketPayload.Type<>(BUNGEECORD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, QuasarPacket> codec = new StreamCodec<>() {

        @Override
        public void encode(RegistryFriendlyByteBuf buf, QuasarPacket value) {
            try (ByteBufDataOutput output = new ByteBufDataOutput(new FriendlyByteBuf(Unpooled.buffer()))) {
                for (String arg : value.args()) {
                    output.writeUTF(arg);
                }
                buf.writeBytes(output.getBuf());
            } catch (Exception _) {

            }
        }

        @Override
        public QuasarPacket decode(RegistryFriendlyByteBuf input) {
            return null;
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

    private static class ByteBufDataOutput extends OutputStream {
        private final FriendlyByteBuf packedByteBuf;
        private final DataOutputStream dataOutputStream;

        public ByteBufDataOutput(FriendlyByteBuf buf) {
            this.packedByteBuf = buf;
            this.dataOutputStream = new DataOutputStream(this);
        }

        public FriendlyByteBuf getBuf() {
            return packedByteBuf;
        }

        @Override
        public void write(int b) {
            packedByteBuf.writeByte(b);
        }

        public void writeUTF(String s) {
            try {
                this.dataOutputStream.writeUTF(s);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
