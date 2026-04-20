package com.childwax.quasar.mixins;

import com.childwax.quasar.registry.PortalRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndGatewayBlock.class)
public class EndGatewayMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void cancelIfInProxyPortal(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl, CallbackInfo ci) {
        String portalId = PortalRegistry.findPortalIntersecting(entity.getBoundingBox());
        if (portalId != null) {
            ci.cancel();
        }
    }
}