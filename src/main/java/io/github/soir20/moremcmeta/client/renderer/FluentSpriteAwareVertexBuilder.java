package io.github.soir20.moremcmeta.client.renderer;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.SpriteAwareVertexBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * SpriteAwareVertexBuilder returns its internal builder, not the sprite-aware builder itself,
 * and prevents the proper chaining of its methods. This class corrects the sprite-aware builder's
 * fluent pattern.
 */
@MethodsReturnNonnullByDefault
public class FluentSpriteAwareVertexBuilder extends SpriteAwareVertexBuilder {
    public FluentSpriteAwareVertexBuilder(IVertexBuilder bufferIn, TextureAtlasSprite spriteIn) {
        super(bufferIn, spriteIn);
    }

    @Override
    public IVertexBuilder pos(double x, double y, double z) {
        super.pos(x, y, z);
        return this;
    }

    @Override
    public IVertexBuilder color(int red, int green, int blue, int alpha) {
        super.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public IVertexBuilder tex(float u, float v) {
        super.tex(u, v);
        return this;
    }

    @Override
    public IVertexBuilder overlay(int u, int v) {
        super.overlay(u, v);
        return this;
    }

    @Override
    public IVertexBuilder lightmap(int u, int v) {
        super.lightmap(u, v);
        return this;
    }

    @Override
    public IVertexBuilder normal(float x, float y, float z) {
        super.normal(x, y, z);
        return this;
    }
}
