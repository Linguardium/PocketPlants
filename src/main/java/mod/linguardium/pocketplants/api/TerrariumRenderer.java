package mod.linguardium.pocketplants.api;

import mod.linguardium.pocketplants.blocks.PlantTerrariumEntity;
import mod.linguardium.pocketplants.blocks.initBlocks;
import mod.linguardium.pocketplants.impl.QuadSpriteAccessor;
import mod.linguardium.pocketplants.utils.Nbt;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TerrariumRenderer extends BlockEntityRenderer<PlantTerrariumEntity> {
    public TerrariumRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    private static class SoilModel extends Model {
        ModelPart dirtModel;
        public SoilModel(Sprite sprite) {
            super(RenderLayer::getEntityCutoutNoCull);
            textureHeight=sprite.getHeight();
            textureWidth=sprite.getWidth();
            dirtModel = new ModelPart(this);
            dirtModel.addCuboid(3,3,3,10,2,10);

        }
        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
            dirtModel.render(matrices, vertexConsumer, light, overlay,red,green,blue,alpha);
        }

    }
    private static class WaterModel extends Model {
        ModelPart waterModel;
        public WaterModel(Sprite sprite) {
            super(RenderLayer::getEntityTranslucent);
            textureHeight=sprite.getHeight();
            textureWidth=sprite.getWidth();
            waterModel = new ModelPart(this);
            waterModel.addCuboid(2,3.01f,2,12,8.98f,12);

        }
        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
            waterModel.render(matrices, vertexConsumer, light, overlay,red,green,blue,alpha);
        }

    }

    private static class CustomRenderLayer extends RenderLayer {

        public CustomRenderLayer(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        }

        public static RenderLayer getRenderLayer(Identifier texture) {
            RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new Texture(texture, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).lightmap(DISABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
            return of("entity_solid", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, multiPhaseParameters);
        }
    }

    public static void render(ItemStack stack, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        CompoundTag tag = stack.getOrCreateTag();
        PlantTag pTag = null;
        if (tag.contains("PlantTag", Nbt.TagTypes.get(CompoundTag.class))) {
            pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
        }
        render(pTag,matrix,vertexConsumerProvider,light,overlay,0,0,0,1);
                    //MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer().renderFlat(tbv,)

    }
    @Override
    public void render(PlantTerrariumEntity blockEntity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrix.push();
        PlantTag pTag = blockEntity.getPlantTag();
        render(pTag,matrix,vertexConsumers,light,overlay,0,0,0,1);
        matrix.pop();
    }
    public static void render(PlantTag pTag, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, float offsetX, float offsetY, float offsetZ, float scale) {
        Random random=new Random();

        matrix.scale(scale,scale,scale);
        matrix.translate(offsetX*scale,offsetY*scale,offsetZ*scale);
//        VertexConsumer vertex = ;
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        BlockModelRenderer blockRenderer = blockRenderManager.getModelRenderer();
        TerrariumBlockView tbv = new TerrariumBlockView(pTag);

        matrix.push();
        FluidState fState = tbv.getFluidState(TerrariumBlockView.PlantPos);
        BlockState terrState;
        terrState = initBlocks.TERRARIUM_BLOCK.getDefaultState();
        BakedModel TerrariumModel = blockRenderManager.getModel(terrState);
        blockRenderer.render(tbv,TerrariumModel,terrState,TerrariumBlockView.PlantPos,matrix,vertexConsumerProvider.getBuffer(RenderLayer.getCutout()),true,random,0,overlay);
        matrix.pop();
        if (pTag == null) {
            return;
        }
        BakedModel soilBakedModel = blockRenderManager.getModels().getModel(pTag.getSoilState());
        Sprite soilSprite = blockRenderManager.getModels().getSprite(pTag.getSoilState());
        SoilModel soilModel = new SoilModel(soilSprite);
//                    soilModel.render(matrix,vertexConsumerProvider.getBuffer(CustomRenderLayer.getRenderLayer(blockTexture)),light,overlay,1,1,1,1);
        HashMap<Direction, Identifier> textures = new HashMap<>();
        List<BakedQuad> qList= new ArrayList<>();
        qList.addAll(soilBakedModel.getQuads(pTag.getSoilState(), Direction.UP, random));
        qList.addAll(soilBakedModel.getQuads(pTag.getSoilState(),null,random));
        qList.removeIf(bq->!bq.getFace().equals(Direction.UP));
        Identifier soilBlockTexture=soilSprite.getId();
        if (qList.size()>0) {
            try {
                soilBlockTexture = ((QuadSpriteAccessor)qList.get(0)).getSprite().getId();
            }catch(Exception ignored){}
        }
        soilBlockTexture = new Identifier(soilBlockTexture.getNamespace(), "textures/" + soilBlockTexture.getPath() + ".png");
        int color=0xFFFFFF;
        if (pTag.getSoilState().getBlock() instanceof GrassBlock) {
            color = BiomeColors.getGrassColor(tbv, TerrariumBlockView.SoilPos);
        }
        soilModel.render(matrix,vertexConsumerProvider.getBuffer(CustomRenderLayer.getRenderLayer(soilBlockTexture)),light,overlay,((color>>16)&0xFF)/255.0F, ((color>>8)&0xFF)/255.0f, (color&0xFF)/255.0f,1);

        matrix.push();
        float plantScale=(6.0f/16.0f)/scale;
        matrix.translate((5/16.0F),(5/16.0F),(5/16.0F));
        matrix.scale(plantScale*scale,plantScale*scale,plantScale*scale);
        blockRenderer.render(tbv,blockRenderManager.getModel(pTag.getBlockState()),pTag.getBlockState(),TerrariumBlockView.PlantPos,matrix,vertexConsumerProvider.getBuffer(RenderLayers.getBlockLayer(pTag.getBlockState())),false,random,0,overlay);
        matrix.pop();

        if (!fState.isEmpty()) {

            color = BiomeColors.getWaterColor(tbv, TerrariumBlockView.SoilPos.add(1,0,0));
            Sprite waterSprite = blockRenderManager.getModel(Blocks.WATER.getDefaultState()).getSprite();
            Identifier waterTexture = new Identifier("pocketplants:textures/block/semitransparent.png");
            WaterModel waterModel = new WaterModel(waterSprite);
            waterModel.render(matrix,vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(waterTexture)),light,overlay,((color>>16)&0xFF)/255.0F, ((color>>8)&0xFF)/255.0f, (color&0xFF)/255.0f,0.5f);

        }


        }
}
