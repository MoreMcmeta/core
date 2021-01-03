package io.github.soir20.moremcmeta;

import com.google.common.collect.ImmutableMap;
import io.github.soir20.moremcmeta.resource.AtlasReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
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
    public static final ResourceLocation ATLAS_LOCATION = Atlases.SHULKER_BOX_ATLAS;
    public static final String ATLAS_PREFIX = "moremcmeta:textures/atlas/";

    public static final ImmutableMap<String, Function<ResourceLocation, AtlasTexture>> ATLASES;
    static {
        ImmutableMap.Builder<String, Function<ResourceLocation, AtlasTexture>> builder =
                new ImmutableMap.Builder<>();

        AtlasTexture colormapAtlas = new AtlasTexture(new ResourceLocation(ATLAS_PREFIX + "colormap.png"));
        AtlasTexture entityAtlas = new AtlasTexture(new ResourceLocation(ATLAS_PREFIX + "entity.png"));
        AtlasTexture guiAtlas = new AtlasTexture(new ResourceLocation(ATLAS_PREFIX + "gui.png"));

        builder.put("colormap", (tex) -> colormapAtlas)
                .put("entity", (tex) -> entityAtlas)
                .put("environment", MoreMcmeta::getEnvironmentTextureAtlas)
                .put("gui", (tex) -> guiAtlas);

        ATLASES = builder.build();
    }

    private static AtlasTexture getEnvironmentTextureAtlas(ResourceLocation texture) {
        String path = texture.getPath();
        int lastDirIndex = path.lastIndexOf('/');
        String atlasLocation = ATLAS_PREFIX + path.substring(lastDirIndex);
        return new AtlasTexture(new ResourceLocation(atlasLocation));
    }

    public MoreMcmeta() {
        IReloadableResourceManager manager =
                ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager());
        manager.addReloadListener(new AtlasReloadListener());
        MoreMcmeta.LOGGER.debug("Added atlas reload listener");
    }
}
