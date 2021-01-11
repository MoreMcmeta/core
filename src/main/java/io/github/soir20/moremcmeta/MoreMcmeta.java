package io.github.soir20.moremcmeta;

import io.github.soir20.moremcmeta.client.renderer.texture.AnimatedTexture;
import io.github.soir20.moremcmeta.client.renderer.texture.TextureManagerWrapper;
import io.github.soir20.moremcmeta.resource.AtlasReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IReloadableResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

@Mod(MoreMcmeta.MODID)
public final class MoreMcmeta
{
    public static final String MODID = "moremcmeta";
    public static final String[] FOLDERS = {"entity", "gui", "map"};

    public MoreMcmeta() {
        Minecraft minecraft = Minecraft.getInstance();
        IReloadableResourceManager manager =
                ((IReloadableResourceManager) minecraft.getResourceManager());
        TextureManagerWrapper texManager = new TextureManagerWrapper(minecraft.getTextureManager());
        Logger logger = LogManager.getLogger();

        manager.addReloadListener(new AtlasReloadListener<>(FOLDERS, texManager,
                AnimatedTexture::new, AnimationMetadataSection.SERIALIZER, logger));
        logger.debug("Added atlas reload listener");
    }
}
