package mod.linguardium.pocketplants;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

import java.util.Arrays;

@Config.Gui.CategoryBackground(category="ExternalModSupport",background="minecraft:textures/block/cyan_shulker_box.png")
@Config.Gui.CategoryBackground(category="default",background="minecraft:textures/block/cyan_shulker_box.png")
@Config(name = PocketPlants.MOD_ID)
public class PocketPlantsConfig implements ConfigData {


    public boolean bEnableSpeedIncrease = true;
    public String sRateOfSpeedIncrease =  "0.02";


    //@ConfigEntry.Category("ExternalModSupport")
    //public boolean bEnableConvenientThingsSupport = true;

    @ConfigEntry.Category("ExternalModSupport")
    public String sModIdBlacklist = "";


    @Override
    public void validatePostLoad() throws ValidationException {
        try {
            float n = Float.parseFloat(sRateOfSpeedIncrease);
        }catch (NumberFormatException e){
            sRateOfSpeedIncrease="0.02";
        }
            //throw new ValidationException("Invalid Speed Multiplier per Harvest. Must only contain numbers and decimal.");
    }

    public float RateOfSpeedIncrease() {
        try {
            float n = Float.parseFloat(sRateOfSpeedIncrease);
        }catch (NumberFormatException e){
            sRateOfSpeedIncrease="0.02";
        }

        return Float.parseFloat(sRateOfSpeedIncrease);
    }
    public boolean isBlacklisted(String s) {
        String[] mods;
        mods = sModIdBlacklist.split(" *, *");
        return Arrays.stream(mods).anyMatch(s::equalsIgnoreCase);
    }
}