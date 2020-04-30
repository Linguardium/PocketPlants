package mod.linguardium.pocketplants.api;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class BiomeWaterColorProvider implements BlockColorProvider {
    @Override
    public int getColor(BlockState state, BlockRenderView view, BlockPos pos, int tintIndex) {
        return BiomeColors.getWaterColor(view,pos);
    }
}
