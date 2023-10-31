/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.io;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.moremcmeta.api.client.metadata.AnalyzedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.texture.Color;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameIndexOutOfBoundsException;
import io.github.moremcmeta.moremcmeta.api.client.texture.IllegalFrameReferenceException;
import io.github.moremcmeta.moremcmeta.api.client.texture.MutableFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.PixelOutOfBoundsException;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import io.github.moremcmeta.moremcmeta.api.math.Area;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import io.github.moremcmeta.moremcmeta.impl.client.texture.CoreTextureComponent;
import io.github.moremcmeta.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.MockCloseableImage;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TextureDataAssembler}. This class uses some Minecraft IO functions, and it
 * is planned to completely revamp how this class works in the future. Its code is mostly creating
 * other things that are tested. Therefore, the tests for it are less thorough.
 * @author soir20
 */
public final class TextureDataAssemblerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullAllocator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureDataAssembler<>(
                null,
                (image, mipmap) -> ImmutableList.of(image)
        );
    }

    @Test
    public void construct_NullMipmapGenerator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                null
        );
    }

    @Test
    public void assemble_NullData_NullPointerException() {
        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (image, mipmap) -> ImmutableList.of(image)
        );
        expectedException.expect(NullPointerException.class);
        assembler.assemble(null);
    }

    @Test
    public void assemble_DataGiven_GeneratesCorrectFrameSize() {
        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (image, mipmap) -> ImmutableList.of(image)
        );

        AtomicBoolean checked = new AtomicBoolean();
        Consumer<FrameGroup<? extends MutableFrameView>> checkFunction = (group) -> {
            checked.set(true);

            for (int index = 0; index < group.frames(); index++) {
                assertEquals(30, group.frame(index).width());
                assertEquals(40, group.frame(index).height());
            }

            assertEquals(6, group.frames());
        };

        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, false,
                Optional.empty(),
                new MockCloseableImage(100, 100),
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    checkFunction.accept(frames);
                    return new TextureComponent<>() {};
                }))
        ));

        assertTrue(checked.get());
    }

    @Test
    public void assemble_DataGiven_MipmapGeneratorProvidedCorrectImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        AtomicBoolean checked = new AtomicBoolean();
        Consumer<MockCloseableImage> checkFunction = (source) -> {
            checked.set(true);
            assertTrue(originalImage.hasSamePixels(source));
        };

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> { checkFunction.accept(original); return ImmutableList.of(original); }
        );

        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, false,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) ->
                    new TextureComponent<>() {}
                ))
        ));

        assertTrue(checked.get());
    }

    @Test
    public void assemble_DataGiven_AllocatorProvidedCorrectWidthHeight() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        AtomicBoolean checked = new AtomicBoolean();
        Consumer<Triple<Integer, Integer, Integer>> checkFunction = (widthHeightMipmap) -> {
            checked.set(true);
            int mipmap = widthHeightMipmap.getRight();
            assertEquals(30 >> mipmap, (int) widthHeightMipmap.getLeft());
            assertEquals(40 >> mipmap, (int) widthHeightMipmap.getMiddle());
        };

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> {
                    checkFunction.accept(Triple.of(width, height, mipmap));
                    return new MockCloseableImage(width, height);
                },
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2)
                )
        );

        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, false,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) ->
                        new TextureComponent<>() {}
                ))
        ));

        assertTrue(checked.get());
    }

    @Test
    public void assemble_BlurTrueClampFalse_AllocatorProvidedCorrectBlurClamp() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        AtomicBoolean checked = new AtomicBoolean();
        Consumer<Pair<Boolean, Boolean>> checkFunction = (blurClamp) -> {
            checked.set(true);
            assertTrue(blurClamp.getFirst());
            assertFalse(blurClamp.getSecond());
        };

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> {
                    checkFunction.accept(Pair.of(blur, clamp));
                    return new MockCloseableImage(width, height);
                },
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2)
                )
        );

        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                true, false,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) ->
                        new TextureComponent<>() {}
                ))
        ));

        assertTrue(checked.get());
    }

    @Test
    public void assemble_BlurFalseClampTrue_AllocatorProvidedCorrectBlurClamp() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        AtomicBoolean checked = new AtomicBoolean();
        Consumer<Pair<Boolean, Boolean>> checkFunction = (blurClamp) -> {
            checked.set(true);
            assertFalse(blurClamp.getFirst());
            assertTrue(blurClamp.getSecond());
        };

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> {
                    checkFunction.accept(Pair.of(blur, clamp));
                    return new MockCloseableImage(width, height);
                },
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) ->
                        new TextureComponent<>() {}
                ))
        ));

        assertTrue(checked.get());
    }

    @Test
    public void assemble_MipmapsGeneratedTooSmall_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(20, 20),
                        new MockCloseableImage(20, 20),
                        new MockCloseableImage(20, 20)
                )
        );

        expectedException.expect(MockCloseableImage.MockSubImageOutsideOriginalException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, false,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) ->
                        new TextureComponent<>() {}
                ))
        ));
    }

    @Test
    public void assemble_DataGiven_BuiltTextureClosesAllFrames() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        List<MockCloseableImage> allocatedImages = new ArrayList<>();
        allocatedImages.add(originalImage);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> {
                    MockCloseableImage image = new MockCloseableImage(width, height);
                    allocatedImages.add(image);
                    return image;
                },
                (original, mipmap) -> {
                    List<MockCloseableImage> mipmaps = ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                    );

                    allocatedImages.add(mipmaps.get(1));
                    allocatedImages.add(mipmaps.get(2));
                    allocatedImages.add(mipmaps.get(3));

                    return mipmaps;
                }
        );

        EventDrivenTexture texture = addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) ->
                        new TextureComponent<>() {}
                ))
        ))).build();

        allocatedImages.forEach((image) -> assertFalse(image.isClosed()));
        texture.close();
        allocatedImages.forEach((image) -> assertTrue(image.isClosed()));
    }

    @Test
    public void assemble_WidthWhileValid_CorrectWidth() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    assertEquals(30, frames.frame(0).width());
                    return new TextureComponent<>() {};
                }))
        ))).build();
    }

    @Test
    public void assemble_WidthAfterInvalid_IllegalFrameReferenceException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        MutableFrameView[] frameView = new MutableFrameView[1];

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frameView[0] = frames.frame(0);
                    return new TextureComponent<>() {};
                }))
        ))).build();

        expectedException.expect(IllegalFrameReferenceException.class);
        frameView[0].width();
    }

    @Test
    public void assemble_HeightWhileValid_CorrectHeight() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    assertEquals(40, frames.frame(0).height());
                    return new TextureComponent<>() {};
                }))
        ))).build();
    }

    @Test
    public void assemble_HeightAfterInvalid_IllegalFrameReferenceException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        MutableFrameView[] frameView = new MutableFrameView[1];

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frameView[0] = frames.frame(0);
                    return new TextureComponent<>() {};
                }))
        ))).build();

        expectedException.expect(IllegalFrameReferenceException.class);
        frameView[0].height();
    }

    @Test
    public void assemble_NullTransform_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform(null, Area.of(Point.pack(0, 0)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_NullApplyArea_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> 100, null);
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointNegativeX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> 100, Area.of(Point.pack(-1, 0)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointNegativeY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> 100, Area.of(Point.pack(0, -1)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointOutOfBoundsX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> 100, Area.of(Point.pack(30, 0)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointOutOfBoundsY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> 100, Area.of(Point.pack(0, 40)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_ValidTransform_TransformApplied() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> 100, Area.of(Point.pack(0, 0), Point.pack(20, 25)));
                    return new TextureComponent<>() {};
                }))
        ))).build();

        /* The CloseableImageFrame logic is already tested, so focus on just testing that the transformation was
           applied to the original image. Re-creating all the tests would be time-consuming while providing
           limited value. */
        assertEquals(100, originalImage.color(0, 0));
        assertEquals(100, originalImage.color(20, 25));

    }

    @Test
    public void assemble_DependencyListContainsPointNegativeX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> depFunction.color(-1, 0), Area.of(Point.pack(0, 0)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_DependencyListContainsPointNegativeY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> depFunction.color(0, -1), Area.of(Point.pack(0, 0)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_DependencyListContainsPointOutOfBoundsX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> depFunction.color(30, 0), Area.of(Point.pack(0, 0)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_DependencyListContainsPointOutOfBoundsY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((x, y, depFunction) -> depFunction.color(0, 40), Area.of(Point.pack(0, 0)));
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_ValidDependencyPoint_PointRetrieved() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);
        originalImage.setColor(25, 30, Color.pack(100, 100, 100, 100));

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform(
                            (x, y, depFunction) -> depFunction.color(25, 30),
                            Area.of(Point.pack(0, 0), Point.pack(20, 25))
                    );
                    return new TextureComponent<>() {};
                }))
        ))).build();

        /* The CloseableImageFrame logic is already tested, so focus on just testing that the transformation was
           applied to the original image. Re-creating all the tests would be time-consuming while providing
           limited value. */
        assertEquals(Color.pack(100, 100, 100, 100), originalImage.color(0, 0));
        assertEquals(Color.pack(100, 100, 100, 100), originalImage.color(20, 25));

    }

    @Test
    public void assemble_TransformAfterInvalid_IllegalFrameReferenceException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        MutableFrameView[] frameView = new MutableFrameView[1];

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frameView[0] = frames.frame(0);
                    return new TextureComponent<>() {};
                }))
        ))).build();

        expectedException.expect(IllegalFrameReferenceException.class);
        frameView[0].transform((x, y, depFunction) -> 100, Area.of(Point.pack(0, 0)));
    }

    @Test
    public void assemble_NegativeGroupIndex_FrameIndexOutOfBoundsException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameIndexOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(-1);
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_TooLargeGroupIndex_FrameIndexOutOfBoundsException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameIndexOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    frames.frame(6);
                    return new TextureComponent<>() {};
                }))
        )).build();
    }

    @Test
    public void assemble_GetFrameCount_CorrectNumberOfFrames() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original, mipmap) -> ImmutableList.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        addExtraDefaultComponents(assembler.assemble(new TextureData<>(
                new TextureData.FrameSize(30, 40),
                false, true,
                Optional.empty(),
                originalImage,
                ImmutableList.of(Triple.of("plugin", new AnalyzedMetadata() {}, (metadata, frames) -> {
                    assertEquals(6, frames.frames());
                    return new TextureComponent<>() {};
                }))
        ))).build();
    }

    private EventDrivenTexture.Builder addExtraDefaultComponents(EventDrivenTexture.Builder builder) {
        for (int index = 0; index < TextureDataAssembler.EXTERNAL_DEFAULT_COMPONENTS; index++) {
            builder.add(new CoreTextureComponent() {});
        }

        return builder;
    }

}