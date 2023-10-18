package me.jellysquid.mods.sodium.client;


import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

@Mod(modid = SodiumClientMod.MODID)
public class SodiumClientMod {
    public static final String MODID = "rubidium";
    public static final boolean flywheelLoaded = false;
    public static final boolean cclLoaded = false;
    private static final Logger LOGGER = LogManager.getLogger("Rubidium");
    private static SodiumGameOptions CONFIG;
    private static String MOD_VERSION;

    public SodiumClientMod() {
        // FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInitializeClient);
        // ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    public static SodiumGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }

    public static Logger logger() {
        return LOGGER;
    }

    private static SodiumGameOptions loadConfig() {

        return SodiumGameOptions.load(Paths.get(Loader.instance().getConfigDir().getAbsolutePath(), MODID + "-options.json"));
    }

    public static String getVersion() {
        if (MOD_VERSION == null) {
            throw new NullPointerException("Mod version hasn't been populated yet");
        }

        return MOD_VERSION;
    }

    public static boolean isDirectMemoryAccessEnabled() {
        return options().advanced.allowDirectMemoryAccess;
    }

    @Mod.EventHandler
    public void onInitializeClient(final FMLInitializationEvent event) {
        MOD_VERSION = "1.0.0";

    }
}
