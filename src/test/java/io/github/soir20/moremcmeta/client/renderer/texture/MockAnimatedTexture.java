package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class MockAnimatedTexture extends Texture implements ITickable {
    private final ResourceLocation LOCATION;
    private final int WIDTH;
    private final int HEIGHT;
    private final AnimationMetadataSection METADATA;
    private final int MIPMAP;
    private final NativeImage IMAGE;

    public MockAnimatedTexture() {
        LOCATION = null;
        WIDTH = 0;
        HEIGHT = 0;
        METADATA = null;
        MIPMAP = 0;
        IMAGE = null;
    }

    public MockAnimatedTexture(ResourceLocation location, int width, int height,
                               AnimationMetadataSection metadata, int mipmapLevels, NativeImage nativeImage) {
        LOCATION = location;
        WIDTH = width;
        HEIGHT = height;
        METADATA = metadata;
        MIPMAP = mipmapLevels;
        IMAGE = nativeImage;
    }

    public ResourceLocation getLocation() {
        return LOCATION;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public AnimationMetadataSection getMetadata() {
        return METADATA;
    }

    public int getMipmap() {
        return MIPMAP;
    }

    public NativeImage getImage() {
        return IMAGE;
    }

    @Override
    public void tick() {}

    @Override
    public void loadTexture(IResourceManager manager) {}
}
