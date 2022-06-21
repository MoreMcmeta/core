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

package io.github.soir20.moremcmeta.impl.client.io;

import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.soir20.moremcmeta.api.client.texture.Color;
import io.github.soir20.moremcmeta.api.client.texture.ColorTransform;
import io.github.soir20.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.FrameGroup;
import io.github.soir20.moremcmeta.api.client.texture.FrameView;
import io.github.soir20.moremcmeta.api.client.texture.MutableFrameView;
import io.github.soir20.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.TextureComponent;
import io.github.soir20.moremcmeta.api.math.Point;
import io.github.soir20.moremcmeta.impl.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.impl.client.texture.MockCloseableImage;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
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
public class TextureDataAssemblerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullAllocator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureDataAssembler<>(
                null,
                List::of
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
                List::of
        );
        expectedException.expect(NullPointerException.class);
        assembler.assemble(null);
    }

    @Test
    public void assemble_DataGiven_GeneratesCorrectFrameSize() {
        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                List::of
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
                new ParsedMetadata.FrameSize(30, 40),
                false, false,
                new MockCloseableImage(100, 100),
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    checkFunction.accept(frames);
                    return List.of(new TextureComponent<>() {});
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
                (original) -> { checkFunction.accept(original); return List.of(original); }
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, false,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) ->
                    List.of(new TextureComponent<>() {})
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
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, false,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) ->
                        List.of(new TextureComponent<>() {})
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
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                true, false,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) ->
                        List.of(new TextureComponent<>() {})
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
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) ->
                        List.of(new TextureComponent<>() {})
                ))
        ));

        assertTrue(checked.get());
    }

    @Test
    public void assemble_MipmapsGeneratedTooSmall_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(20, 20),
                        new MockCloseableImage(20, 20),
                        new MockCloseableImage(20, 20)
                )
        );

        expectedException.expect(MockCloseableImage.MockSubImageOutsideOriginalException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, false,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) ->
                        List.of(new TextureComponent<>() {})
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
                (original) -> {
                    List<MockCloseableImage> mipmaps = List.of(
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

        EventDrivenTexture texture = assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) ->
                        List.of(new TextureComponent<>() {})
                ))
        )).build();

        allocatedImages.forEach((image) -> assertFalse(image.isClosed()));
        texture.close();
        allocatedImages.forEach((image) -> assertTrue(image.isClosed()));
    }

    @Test
    public void assemble_DataGiven_ComponentsAssembledInOrder() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        List<Integer> actualOrder = new ArrayList<>();

        EventDrivenTexture texture = assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> List.of(
                        new TextureComponent<>() {public void onTick(CurrentFrameView view, FrameGroup<PersistentFrameView> predefinedFrames) { actualOrder.add(0);}},
                        new TextureComponent<>() {public void onTick(CurrentFrameView view, FrameGroup<PersistentFrameView> predefinedFrames) { actualOrder.add(1);}},
                        new TextureComponent<>() {public void onTick(CurrentFrameView view, FrameGroup<PersistentFrameView> predefinedFrames) { actualOrder.add(2);}},
                        new TextureComponent<>() {public void onTick(CurrentFrameView view, FrameGroup<PersistentFrameView> predefinedFrames) { actualOrder.add(3);}}
                )))
        )).build();

        texture.tick();

        assertEquals(List.of(0, 1, 2, 3), actualOrder);
    }

    @Test
    public void assemble_WidthWhileValid_CorrectWidth() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    assertEquals(30, frames.frame(0).width());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_WidthAfterInvalid_IllegalFrameReferenceException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        MutableFrameView[] frameView = new MutableFrameView[1];

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frameView[0] = frames.frame(0);
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        frameView[0].width();
    }

    @Test
    public void assemble_HeightWhileValid_CorrectHeight() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    assertEquals(40, frames.frame(0).height());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_HeightAfterInvalid_IllegalFrameReferenceException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        MutableFrameView[] frameView = new MutableFrameView[1];

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frameView[0] = frames.frame(0);
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        frameView[0].height();
    }

    @Test
    public void assemble_IndexWhileValid_CorrectIndex() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    assertEquals(1, (int) frames.frame(1).index().orElseThrow());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_NullTransform_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform(null, List.of(new Point(0, 0)), List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_NullApplyArea_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), null, List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_NullDependencyList_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(), null);
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_TransformReturnsNull_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> null, List.of(new Point(0, 0)), List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsNull_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        List<Point> applyArea = new ArrayList<>();
        applyArea.add(new Point(0, 0));
        applyArea.add(null);

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), applyArea, List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointNegativeX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(-1, 0)), List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointNegativeY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, -1)), List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointOutOfBoundsX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(30, 0)), List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_ApplyAreaContainsPointOutOfBoundsY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, 40)), List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_ValidTransform_TransformApplied() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, 0), new Point(20, 25)), List.of());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();

        /* The CloseableImageFrame logic is already tested, so focus on just testing that the transformation was
           applied to the original image. Re-creating all the tests would be time-consuming while providing
           limited value. */
        assertEquals(100, originalImage.color(0, 0));
        assertEquals(100, originalImage.color(20, 25));

    }

    @Test
    public void assemble_DependencyListContainsNull_NullPointerException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        List<Point> dependencies = new ArrayList<>();
        dependencies.add(new Point(0, 0));
        dependencies.add(null);

        expectedException.expect(NullPointerException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, 0)), dependencies);
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_DependencyListContainsPointNegativeX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, 0)), List.of(new Point(-1, 0)));
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_DependencyListContainsPointNegativeY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, 0)), List.of(new Point(0, -1)));
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_DependencyListContainsPointOutOfBoundsX_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, 0)), List.of(new Point(30, 0)));
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_DependencyListContainsPointOutOfBoundsY_ExceptionFromImage() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> new Color(100), List.of(new Point(0, 0)), List.of(new Point(0, 40)));
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_NonDependencyPointRequested_NonDependencyRequestException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(ColorTransform.NonDependencyRequestException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform((point, depFunction) -> depFunction.apply(new Point(25, 0)), List.of(new Point(0, 0)), List.of(new Point(0, 25)));
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_ValidDependencyPoint_PointRetrieved() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);
        originalImage.setColor(25, 30, new Color(100, 100, 100, 100).combine());

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(0).transform(
                            (point, depFunction) -> depFunction.apply(new Point(25, 30)),
                            List.of(new Point(0, 0), new Point(20, 25)), 
                            List.of(new Point(25, 30))
                    );
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();

        /* The CloseableImageFrame logic is already tested, so focus on just testing that the transformation was
           applied to the original image. Re-creating all the tests would be time-consuming while providing
           limited value. */
        assertEquals(new Color(100, 100, 100, 100).combine(), originalImage.color(0, 0));
        assertEquals(new Color(100, 100, 100, 100).combine(), originalImage.color(20, 25));

    }

    @Test
    public void assemble_TransformAfterInvalid_IllegalFrameReferenceException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        MutableFrameView[] frameView = new MutableFrameView[1];

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frameView[0] = frames.frame(0);
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();

        expectedException.expect(FrameView.IllegalFrameReference.class);
        frameView[0].transform((point, depFunction) -> new Color(100), List.of(new Point(0, 0)), List.of());
    }

    @Test
    public void assemble_NegativeGroupIndex_FrameIndexOutOfBoundsException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.FrameIndexOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(-1);
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_TooLargeGroupIndex_FrameIndexOutOfBoundsException() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        expectedException.expect(FrameView.FrameIndexOutOfBoundsException.class);
        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    frames.frame(6);
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

    @Test
    public void assemble_GetFrameCount_CorrectNumberOfFrames() {
        MockCloseableImage originalImage = new MockCloseableImage(100, 100);

        TextureDataAssembler<MockCloseableImage> assembler = new TextureDataAssembler<>(
                (width, height, mipmap, blur, clamp) -> new MockCloseableImage(width, height),
                (original) -> List.of(
                        original,
                        new MockCloseableImage(original.width() >> 1, original.height() >> 1),
                        new MockCloseableImage(original.width() >> 2, original.height() >> 2),
                        new MockCloseableImage(original.width() >> 3, original.height() >> 3)
                )
        );

        assembler.assemble(new TextureData<>(
                new ParsedMetadata.FrameSize(30, 40),
                false, true,
                originalImage,
                List.of(Pair.of(new ParsedMetadata() {}, (metadata, frames) -> {
                    assertEquals(6, frames.frames());
                    return List.of(new TextureComponent<>() {});
                }))
        )).build();
    }

}