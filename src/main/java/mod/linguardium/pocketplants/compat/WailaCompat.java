package mod.linguardium.pocketplants.compat;

import mcp.mobius.waila.WailaPlugins;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.impl.DataAccessor;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import mod.linguardium.pocketplants.api.PlantTag;
import mod.linguardium.pocketplants.blocks.PlantTerrariumEntity;
import mod.linguardium.pocketplants.items.PocketTerrarium;
import mod.linguardium.pocketplants.items.initItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class WailaCompat implements IComponentProvider, IWailaPlugin {
    @Override
    public ItemStack getStack(IDataAccessor accessor, IPluginConfig config) {
        PlantTag pTag = PlantTag.fromTag(accessor.getStack().getOrCreateTag().getCompound("PlantTag"));
        if (pTag != null) {
            BlockState bs = pTag.getBlockState();
            return new ItemStack(bs.getBlock().asItem());
        }
        return new ItemStack(initItems.TERRARIUM);
    }

    public DataAccessor pTagDataAccessor(PlantTag pTag, IDataAccessor accessor) {
        if (pTag != null) {
            BlockState bs = pTag.getBlockState();
            BlockEntity be = pTag.getBlockEntity();
            BlockState ss = pTag.getSoilState();
            DataAccessor subAccessor = new DataAccessor();
            subAccessor.block = bs.getBlock();
            subAccessor.blockEntity = be;
            subAccessor.state = bs;
            subAccessor.world = accessor.getWorld();
            subAccessor.blockRegistryName = Registry.BLOCK.getId(bs.getBlock());
            return subAccessor;
        }
        return null;
    }
    @Override
    public void appendBody(List<Text> tooltip, IDataAccessor accessor, IPluginConfig config) {
        BlockEntity targetBlockEntity = accessor.getBlockEntity();
        if (targetBlockEntity!=null && targetBlockEntity instanceof PlantTerrariumEntity) {
            PlantTag pTag = ((PlantTerrariumEntity) targetBlockEntity).getPlantTag();
            if (pTag != null) {
                List<Text> pluginTooltip = new ArrayList<>();
                BlockState bs = pTag.getBlockState();
                BlockEntity be = pTag.getBlockEntity();
                BlockState ss = pTag.getSoilState();
                DataAccessor subAccessor = pTagDataAccessor(pTag, accessor);
                tooltip.add(new TranslatableText("info.pocketplants.age",pTag.getAge(),pTag.getMaxAge()));
                if (bs.getBlock() != null && !bs.isAir()) {
                    if (!(be instanceof Nameable))
                        tooltip.add(new TranslatableText("waila.pocketplants.contains_block", new TranslatableText(bs.getBlock().getTranslationKey())));
                    else
                        tooltip.add(new TranslatableText("waila.pocketplants.contains_block", ((Nameable) be).getName()));
                    pluginTooltip.clear();
                    WailaRegistrar.INSTANCE.getBodyProviders(bs.getBlock()).forEach((plugin_name, providers_by_plugin) -> {
                        providers_by_plugin.forEach(provider -> {
                            if (provider != null && !(provider instanceof WailaCompat) && !(provider instanceof WailaPlugins))
                                provider.appendBody(pluginTooltip, subAccessor, config);
                        });
                    });
                    for(Text txt : pluginTooltip) {
                        tooltip.add(new LiteralText("  ").append(txt));
                    }
                }
                if (be != null) {
                    pluginTooltip.clear();
                    WailaRegistrar.INSTANCE.getBodyProviders(be).forEach((integer, providers_by_plugin) -> {
                        providers_by_plugin.forEach(provider -> {
                            if (provider != null && !(provider instanceof WailaCompat))
                            provider.appendBody(pluginTooltip, subAccessor, config);
                        });
                    });
                    for(Text txt : pluginTooltip) {
                        tooltip.add(new LiteralText("  ").append(txt));
                    }

                }
                if (ss != null && !ss.isAir()) {
                    tooltip.add(new TranslatableText("waila.pocketplants.contains_soil", new TranslatableText(ss.getBlock().getTranslationKey())));
                    subAccessor.block = ss.getBlock();
                    subAccessor.blockRegistryName = Registry.BLOCK.getId(ss.getBlock());
                    subAccessor.blockEntity = null;
                    pluginTooltip.clear();
                    WailaRegistrar.INSTANCE.getBodyProviders(ss.getBlock()).forEach((plugin_name, providers_by_plugin) -> {
                        providers_by_plugin.forEach(provider -> {
                            if (provider != null && !(provider instanceof WailaCompat))
                                provider.appendBody(pluginTooltip, subAccessor, config);
                        });
                    });
                    for(Text txt : pluginTooltip) {
                        tooltip.add(new LiteralText("  ").append(txt));
                    }

                }
            }
        }
    }

    @Override
    public void register(IRegistrar iRegistrar) {
        iRegistrar.registerStackProvider(this, PocketTerrarium.class);
        iRegistrar.registerComponentProvider(this,TooltipPosition.BODY, PlantTerrariumEntity.class);
    }
}
