package me.jellysquid.mods.sodium.client;


import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = SodiumClientMod.MODID)
public class SodiumClientMod {
    public static final String MODID = "rubidium";
    public static final boolean flywheelLoaded = FMLLoader.getLoadingModList().getModFileById("flywheel") != null;
    public static final boolean cclLoaded = FMLLoader.getLoadingModList().getModFileById("codechickenlib") != null;
    private static final Logger LOGGER = LogManager.getLogger("Rubidium");
    private static SodiumGameOptions CONFIG;
    private static String MOD_VERSION;

    public SodiumClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInitializeClient);

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
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
        return SodiumGameOptions.load(FMLPaths.CONFIGDIR.get().resolve(MODID + "-options.json"));
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

    public void onInitializeClient(final FMLClientSetupEvent event) {
        MOD_VERSION = ModList.get().getModContainerById(MODID).get().getModInfo().getVersion().toString();

        if (cclLoaded) {
            CCLCompat.init();
        }
    }
}
