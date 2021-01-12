package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextureManagerWrapperTest {
    private TextureManager createTexManager() {
        return new TextureManager(new SimpleReloadableResourceManager(ResourcePackType.CLIENT_RESOURCES));
    }

    @Test
    public void loadTexture_ManagerExists_ManagerHasTextures() {
        TextureManager textureManager = createTexManager();
        TextureManagerWrapper wrapper = new TextureManagerWrapper(() -> textureManager);
        MockAnimatedTexture tex1 = new MockAnimatedTexture();
        MockAnimatedTexture tex2 = new MockAnimatedTexture();
        ResourceLocation loc1 = new ResourceLocation("hello.png");
        ResourceLocation loc2 = new ResourceLocation("world.png");

        wrapper.loadTexture(loc1, tex1);
        wrapper.loadTexture(loc2, tex2);

        assertEquals(tex1, textureManager.getTexture(loc1));
        assertEquals(tex2, textureManager.getTexture(loc2));
    }

}