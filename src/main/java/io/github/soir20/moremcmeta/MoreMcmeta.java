package io.github.soir20.moremcmeta;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(MoreMcmeta.MODID)
public final class MoreMcmeta
{
    public static final String MODID = "moremcmeta";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation ATLAS_LOCATION = Atlases.SHULKER_BOX_ATLAS;
}
