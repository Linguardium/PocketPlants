package mod.linguardium.pocketplants.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;

public class TerrariumBlockView implements BlockRenderView {
    public static BlockPos SoilPos = new BlockPos(0,69,0);
    public static BlockPos PlantPos = new BlockPos(0,70,0);
    BlockState plantBlockState = Blocks.AIR.getDefaultState();
    BlockEntity plantBlockEntity = null;
    BlockState soilBlockState=Blocks.WATER.getDefaultState();

    public TerrariumBlockView(PlantTag tag) {
        super();
        if (tag!=null) {
            plantBlockState = tag.getBlockState();
            plantBlockEntity = tag.getBlockEntity();
            soilBlockState = tag.getSoilState();
        }
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (pos.equals(PlantPos))
            return plantBlockEntity;

        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.equals(PlantPos)) {
            return plantBlockState;
        }else if (pos.equals(SoilPos)) {
            return soilBlockState;
        }
        if (pos.getY() < 70) {
            return Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, 15);
        }else{
            return Blocks.AIR.getDefaultState();
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (!pos.equals(PlantPos) && !pos.equals(SoilPos) && pos.getY()<70) {
            return Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, 15).getFluidState();
        }
        if (pos.equals(PlantPos) || pos.equals(SoilPos)) {
            return plantBlockState.getFluidState();
        }
        return Fluids.EMPTY.getDefaultState();
    }

    @Override
    public LightingProvider getLightingProvider() {
        return null;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return colorResolver.getColor(Biomes.JUNGLE,0,0);
    }

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return 15;
    }
}
