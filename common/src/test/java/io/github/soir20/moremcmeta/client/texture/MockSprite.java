package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.resources.ResourceLocation;

/**
 * A fake {@link ISprite}.
 * @author soir20
 */
public class MockSprite implements ISprite {
    private final Point UPLOAD_POINT;
    private int timesBound;

    public MockSprite(Point uploadPoint) {
        UPLOAD_POINT = uploadPoint;
    }

    @Override
    public void bind() {
        timesBound++;
    }

    @Override
    public ResourceLocation getName() {
        return new ResourceLocation("dummy");
    }

    @Override
    public Point getUploadPoint() {
        return UPLOAD_POINT;
    }

    public int getBindCount() {
        return timesBound;
    }
}
