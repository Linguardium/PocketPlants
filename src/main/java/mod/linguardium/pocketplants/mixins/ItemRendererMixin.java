package mod.linguardium.pocketplants.mixins;

import mod.linguardium.pocketplants.api.TerrariumRenderer;
import mod.linguardium.pocketplants.items.PocketTerrarium;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void customRenderer(ItemStack stack, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, CallbackInfo info) {
        if (stack.getItem() instanceof PocketTerrarium) {
            matrix.push();
            TerrariumRenderer.render(stack,matrix,vertexConsumerProvider,light,overlay);
            matrix.pop();

            info.cancel();
        }
    }
}
