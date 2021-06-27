package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.math.Point;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class SpriteUploadComponentTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullSprite_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SpriteUploadComponent(null);
    }

    @Test
    public void upload_FirstUpload_FrameUploadedAtMipmappedPoints() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SpriteUploadComponent(new MockSprite(new Point(2, 3)))).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();

        assertEquals(1, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtMipmappedPoints() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SpriteUploadComponent(new MockSprite(new Point(2, 3)))).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.upload();

        assertEquals(2, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void tick_FirstTick_BoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(new Point(2, 3));
        builder.add(() -> (new SpriteUploadComponent(sprite)).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.tick();

        assertEquals(1, sprite.getBindCount());
        assertEquals(1, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void tick_SecondTick_BoundAndUploaded() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        MockSprite sprite = new MockSprite(new Point(2, 3));
        builder.add(() -> (new SpriteUploadComponent(sprite)).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.tick();

        assertEquals(2, sprite.getBindCount());
        assertEquals(2, frame.getUploadCount());
        assertEquals(new Point(2, 3), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(1, 1), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

}