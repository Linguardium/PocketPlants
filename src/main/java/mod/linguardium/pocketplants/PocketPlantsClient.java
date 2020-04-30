package mod.linguardium.pocketplants;

import mod.linguardium.pocketplants.api.BiomeWaterColorProvider;
import mod.linguardium.pocketplants.api.TerrariumRenderer;
import mod.linguardium.pocketplants.blocks.initBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.impl.client.rendering.ColorProviderRegistryImpl;

public class PocketPlantsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //BlockRenderLayerMap.INSTANCE.putBlock(initBlocks.TERRARIUM_BLOCK, RenderLayer.getTranslucent());
        //BlockRenderLayerMap.INSTANCE.putBlock(initBlocks.WATER_SECTION, RenderLayer.getTranslucent());
        ColorProviderRegistryImpl.BLOCK.register(new BiomeWaterColorProvider(), initBlocks.WATER_TERRARIUM_BLOCK);
        BlockEntityRendererRegistry.INSTANCE.register(initBlocks.TERRARIUM_ENTITY_TYPE, TerrariumRenderer::new);

    }
}
