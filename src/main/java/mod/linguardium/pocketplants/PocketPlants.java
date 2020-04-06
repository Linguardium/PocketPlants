package mod.linguardium.pocketplants;

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
        initItems();
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}