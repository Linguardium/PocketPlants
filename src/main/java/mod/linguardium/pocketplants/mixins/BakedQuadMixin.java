package mod.linguardium.pocketplants.mixins;

import mod.linguardium.pocketplants.impl.QuadSpriteAccessor;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements QuadSpriteAccessor {
    @Shadow
    protected final Sprite sprite;

    public BakedQuadMixin(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public Sprite getSprite() {
        return sprite;
    }
}
