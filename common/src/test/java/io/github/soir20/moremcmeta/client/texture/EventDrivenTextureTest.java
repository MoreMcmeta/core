/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.soir20.moremcmeta.client.texture;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Tests the {@link EventDrivenTexture}.
 * @author soir20
 */
public class EventDrivenTextureTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void build_NullImage_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();

        expectedException.expect(NullPointerException.class);
        builder.setImage(null);
    }

    @Test
    public void build_NoImage_IllegalStateException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.REGISTRATION, (state) -> {})));

        expectedException.expect(IllegalStateException.class);
        builder.build();
    }

    @Test
    public void build_SetImageTwice_HasLastImage() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame(1));
        builder.setImage(new MockRGBAImageFrame(2));
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.REGISTRATION,
                (state) -> assertEquals(2, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));
        builder.build().load(null);
    }

    @Test
    public void build_NullComponent_NullPointerException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());

        expectedException.expect(NullPointerException.class);
        builder.add(null);
    }

    @Test
    public void build_NoListeners_NoException() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.setImage(new MockRGBAImageFrame());
        builder.build();
    }

    @Test
    public void runListeners_GetTexture_SameTexture() {

        /* Use this array to get around final restriction on lambdas
           because we have to create listener before texture is built. */
        final Object[] textureGetter = new Object[1];

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.TICK,
                (state) -> assertEquals(textureGetter[0], state.getTexture())
        )));
        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();
        textureGetter[0] = texture;

        texture.tick();
    }

    @Test
    public void runListeners_MarkForUpload_MarkedForUpload() {
        AtomicInteger timesUploaded = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.TICK,
                EventDrivenTexture.TextureState::markNeedsUpload
        ), new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> timesUploaded.incrementAndGet()
        )));

        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.bind();

        texture.tick();
        texture.bind();

        assertEquals(2, timesUploaded.get());
    }

    @Test
    public void runListeners_GetImage_MarkedForUpload() {
        AtomicInteger timesUploaded = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.TICK,
                EventDrivenTexture.TextureState::getImage
        ), new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> timesUploaded.incrementAndGet()
        )));

        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.bind();

        texture.tick();
        texture.bind();

        assertEquals(2, timesUploaded.get());
    }

    @Test
    public void runListeners_GetImageInUpload_MarkedForUpload() {
        AtomicInteger timesUploaded = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.UPLOAD,
                EventDrivenTexture.TextureState::getImage
        ), new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> timesUploaded.incrementAndGet()
        )));

        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.bind();

        texture.bind();

        assertEquals(1, timesUploaded.get());
    }

    @Test
    public void runListeners_SetImageNull_NullPointerException() {

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.TICK,
                (state) -> state.replaceImage(null)
        )));
        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        expectedException.expect(NullPointerException.class);
        texture.tick();
    }

    @Test
    public void runListeners_SetImage_MarkedForUpload() {
        AtomicInteger timesUploaded = new AtomicInteger(0);

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.TICK,
                (state) -> state.replaceImage(new MockRGBAImageFrame(5))
        ), new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> timesUploaded.incrementAndGet()
        )));

        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        // We have to bind once first because the texture is always uploaded on the first bind
        texture.bind();

        texture.tick();
        texture.bind();

        assertEquals(2, timesUploaded.get());
    }

    @Test
    public void runListeners_SetImage_ImageReplaced() {

        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> Stream.of(new TextureListener(TextureListener.Type.TICK,
                (state) -> state.replaceImage(new MockRGBAImageFrame(5))
        ), new TextureListener(TextureListener.Type.CLOSE,
                (state) -> assertEquals(5, ((MockRGBAImageFrame) state.getImage()).getFrameNumber())
        )));

        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        texture.tick();
        texture.close();
    }

    @Test
    public void register_FirstRegistration_RegisterFiredInOrder() {
        Integer[] expected = {1, 2, 3};
        testExpectedOrder((texture) -> texture.load(null), false, expected);
    }

    @Test
    public void register_SecondRegistration_RegisterFiredInOrder() {
        Integer[] expected = {1, 2, 3, 1, 2, 3};
        testExpectedOrder((texture) -> { texture.load(null);
            texture.load(null); }, false, expected);
    }

    @Test
    public void bind_FirstBind_BindAndUploadFiredInOrder() {
        Integer[] expected = {4, 5, 6, 7, 8, 9};
        testExpectedOrder(EventDrivenTexture::bind, false, expected);
    }

    @Test
    public void bind_SecondBindUploadNotNeeded_BindFiredInOrder() {
        Integer[] expected = {4, 5, 6, 7, 8, 9, 4, 5, 6};
        testExpectedOrder((texture) -> {texture.bind(); texture.bind();}, false, expected);
    }

    @Test
    public void bind_SecondBindUploadNeeded_BindAndUploadFiredInOrder() {
        Integer[] expected = {4, 5, 6, 7, 8, 9, 4, 5, 6, 7, 8, 9};
        testExpectedOrder((texture) -> {texture.bind(); texture.bind();}, true, expected);
    }

    @Test
    public void upload_FirstUpload_UploadFiredInOrder() {
        Integer[] expected = {7, 8, 9};
        testExpectedOrder(EventDrivenTexture::upload, false, expected);
    }

    @Test
    public void upload_SecondUploadNotNeeded_UploadFiredInOrder() {

        // The upload method should run regardless of whether an upload is needed
        Integer[] expected = {7, 8, 9, 7, 8, 9};
        testExpectedOrder((texture) -> {texture.upload(); texture.upload();}, false, expected);

    }

    @Test
    public void tick_FirstTick_TickFiredInOrder() {
        Integer[] expected = {10, 11, 12};
        testExpectedOrder(EventDrivenTexture::tick, false, expected);
    }

    @Test
    public void tick_SecondTick_TickFiredInOrder() {
        Integer[] expected = {10, 11, 12, 10, 11, 12};
        testExpectedOrder((texture) -> { texture.tick(); texture.tick(); }, false, expected);
    }

    @Test
    public void close_FirstClose_CloseFiredInOrder() {
        Integer[] expected = {13, 14, 15};
        testExpectedOrder(EventDrivenTexture::close, false, expected);
    }

    @Test
    public void close_SecondClose_CloseFiredInOrder() {
        Integer[] expected = {13, 14, 15, 13, 14, 15};
        testExpectedOrder((texture) -> { texture.close(); texture.close(); }, false, expected);
    }

    private void testExpectedOrder(Consumer<EventDrivenTexture> action, boolean flagForUpload,
                                   Integer[] expected) {
        EventDrivenTexture.Builder texture = new EventDrivenTexture.Builder();
        texture.setImage(new MockRGBAImageFrame());

        int lastId = 0;
        TextureListener.Type[] types = {
                TextureListener.Type.REGISTRATION,
                TextureListener.Type.BIND,
                TextureListener.Type.UPLOAD,
                TextureListener.Type.TICK,
                TextureListener.Type.CLOSE
        };

        List<Integer> execOrder = new ArrayList<>();

        for (TextureListener.Type type : types) {
            for (int index = 0; index < 3; index++) {
                lastId++;
                final int listenerId = lastId;
                texture.add(() -> Stream.of(new TextureListener(type,
                        (state) -> {
                            if (flagForUpload) state.markNeedsUpload();
                            execOrder.add(listenerId);
                        }
                )));
            }
        }

        action.accept(texture.build());
        assertArrayEquals(expected, execOrder.toArray(new Integer[expected.length]));
    }

}