package jacobreich.path_to_grass.event;

import jacobreich.path_to_grass.util.PathBlockData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ServerTickHandler {
    private static final int SAVE_INTERVAL_TICKS = 600; // Save every 30 seconds (600 ticks at 20 TPS)
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ServerTickHandler::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerTickHandler::onServerStop);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter >= SAVE_INTERVAL_TICKS) {
            tickCounter = 0;
            // Get instance for the overworld (default dimension for file storage)
            if (server.overworld() != null) {
                PathBlockData.get(server.overworld()).savePeriodically();
            }
        }
    }

    private static void onServerStop(MinecraftServer server) {
        // Save all dirty chunks before shutdown (graceful or crash recovery)
        if (server.overworld() != null) {
            PathBlockData.get(server.overworld()).savePeriodically();
        }
    }
}
