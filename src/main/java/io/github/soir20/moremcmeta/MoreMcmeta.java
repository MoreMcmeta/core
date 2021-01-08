package io.github.soir20.moremcmeta;

import com.google.common.collect.ImmutableMap;
import io.github.soir20.moremcmeta.resource.AtlasReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Function;

@Mod(MoreMcmeta.MODID)
public final class MoreMcmeta
{
    public static final String MODID = "moremcmeta";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String ATLAS_PREFIX = "moremcmeta:textures/atlas/";

    public static final ImmutableMap<String, Function<ResourceLocation, AtlasTexture>> ATLASES;
    static {
        ImmutableMap.Builder<String, Function<ResourceLocation, AtlasTexture>> builder =
                new ImmutableMap.Builder<>();

        AtlasTexture colormapAtlas = new AtlasTexture(new ResourceLocation(ATLAS_PREFIX + "colormap.png"));
        AtlasTexture entityAtlas = new AtlasTexture(new ResourceLocation(ATLAS_PREFIX + "entity.png"));
        AtlasTexture guiAtlas = new AtlasTexture(new ResourceLocation(ATLAS_PREFIX + "gui.png"));
        AtlasTexture mapAtlas = new AtlasTexture(new ResourceLocation(ATLAS_PREFIX + "map.png"));

        builder//.put("colormap", (tex) -> colormapAtlas)
                //.put("entity", (tex) -> entityAtlas)
                //.put("gui", (tex) -> guiAtlas)
                .put("map", (tex) -> mapAtlas);

        ATLASES = builder.build();
    }

    public MoreMcmeta() {
        IReloadableResourceManager manager =
                ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager());
        manager.addReloadListener(new AtlasReloadListener());
        MoreMcmeta.LOGGER.debug("Added atlas reload listener");
    }
}
