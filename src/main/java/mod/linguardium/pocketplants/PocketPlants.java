package mod.linguardium.pocketplants;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mod.linguardium.pocketplants.items.initItems.initItems;

public class PocketPlants implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "pocketplants";
    public static final String MOD_NAME = "Pocket Plants";
    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        AutoConfig.register(PocketPlantsConfig.class, JanksonConfigSerializer::new);
        initItems();
    }
    public static PocketPlantsConfig getConfig() {
        return AutoConfig.getConfigHolder(PocketPlantsConfig.class).getConfig();
    }
    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}