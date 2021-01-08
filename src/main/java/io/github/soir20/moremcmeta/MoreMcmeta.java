package io.github.soir20.moremcmeta;

import io.github.soir20.moremcmeta.resource.AtlasReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(MoreMcmeta.MODID)
public final class MoreMcmeta
{
    public static final String MODID = "moremcmeta";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String[] FOLDERS = {"colormap", "entity", "gui", "map"};

    public MoreMcmeta() {
        IReloadableResourceManager manager =
                ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager());
        manager.addReloadListener(new AtlasReloadListener());
        MoreMcmeta.LOGGER.debug("Added atlas reload listener");
    }
}
