/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2024 soir20
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

package io.github.moremcmeta.moremcmeta.impl.fabricapi;

/*
 * This file was originally part of the FabricMC project under the Apache license:
 * https://github.com/FabricMC/fabric/blob/8cafc1423557e2c32f925a899af026559e78ef6c/fabric-renderer-api-v1/src/client/java/net/fabricmc/fabric/impl/renderer/SpriteFinderImpl.java.
 * Unused methods referencing Fabric API code have been deleted, the find() method has been
 * refactored to return Optional<TextureAtlasSprite>, class names have been updated to use
 * the Mojang mappings, and comments have been edited for clarity. The original copyright
 * notice follows below. See APACHE.md for the full text of the license.
 *
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Indexes an atlas sprite to allow fast lookup of Sprites from
 * baked vertex coordinates.  Implementation is a straightforward
 * quad tree. Other options that were considered were linear search
 * (slow) and direct indexing of fixed-size cells. Direct indexing
 * would be fastest but would be memory-intensive for large atlases
 * and unsuitable for any atlas that isn't consistently aligned to
 * a fixed cell size.
 * @author FabricMC, soir20
 */
@SuppressWarnings("MissingJavadoc")
public final class SpriteFinder {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Node root;
    private int badSpriteCount = 0;

    public SpriteFinder(Map<ResourceLocation, TextureAtlasSprite> texturesByName) {
        root = new Node(0.5f, 0.5f, 0.25f);
        texturesByName.values().forEach(root::add);
    }

    /**
     * <p>Finds the atlas sprite containing the vertex centroid of the quad.
     * Vertex centroid is essentially the mean u,v coordinate - the intent being
     * to find a point that is unambiguously inside the sprite (vs on an edge.)</p>
     *
     * <p>Should be reliable for any convex quad or triangle. May fail for non-convex quads.
     * Note that all the above refers to u,v coordinates. Geometric vertex does not matter,
     * except to the extent it was used to determine u,v.</p>
     */
    public Optional<TextureAtlasSprite> find(float u, float v) {
        return root.find(u, v);
    }

    private final class Node {
        final float midU;
        final float midV;
        final float cellRadius;
        Object lowLow = null;
        Object lowHigh = null;
        Object highLow = null;
        Object highHigh = null;

        Node(float midU, float midV, float radius) {
            this.midU = midU;
            this.midV = midV;
            cellRadius = radius;
        }

        static final float EPS = 0.00001f;

        void add(TextureAtlasSprite sprite) {
            if (sprite.getU0() < 0 - EPS || sprite.getU1() > 1 + EPS || sprite.getV0() < 0 - EPS || sprite.getV1() > 1 + EPS) {
                // Sprite has broken bounds. This SHOULD NOT happen, but in the past some mods have broken this.
                // Prefer failing with a log warning rather than risking a stack overflow.
                if (badSpriteCount++ < 5) {
                    String errorMessage = "SpriteFinderImpl: Skipping sprite {} with broken bounds [{}, {}]x[{}, {}]. Sprite bounds should be between 0 and 1.";
                    LOGGER.error(errorMessage, sprite.contents().name(), sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
                }

                return;
            }

            final boolean lowU = sprite.getU0() < midU - EPS;
            final boolean highU = sprite.getU1() > midU + EPS;
            final boolean lowV = sprite.getV0() < midV - EPS;
            final boolean highV = sprite.getV1() > midV + EPS;

            if (lowU && lowV) {
                addInner(sprite, lowLow, -1, -1, q -> lowLow = q);
            }

            if (lowU && highV) {
                addInner(sprite, lowHigh, -1, 1, q -> lowHigh = q);
            }

            if (highU && lowV) {
                addInner(sprite, highLow, 1, -1, q -> highLow = q);
            }

            if (highU && highV) {
                addInner(sprite, highHigh, 1, 1, q -> highHigh = q);
            }
        }

        private void addInner(TextureAtlasSprite sprite, Object quadrant, int uStep, int vStep, Consumer<Object> setter) {
            if (quadrant == null) {
                setter.accept(sprite);
            } else if (quadrant instanceof Node) {
                ((Node) quadrant).add(sprite);
            } else {
                Node n = new Node(midU + cellRadius * uStep, midV + cellRadius * vStep, cellRadius * 0.5f);

                if (quadrant instanceof TextureAtlasSprite) {
                    n.add((TextureAtlasSprite) quadrant);
                }

                n.add(sprite);
                setter.accept(n);
            }
        }

        private Optional<TextureAtlasSprite> find(float u, float v) {
            if (u < midU) {
                return v < midV ? findInner(lowLow, u, v) : findInner(lowHigh, u, v);
            } else {
                return v < midV ? findInner(highLow, u, v) : findInner(highHigh, u, v);
            }
        }

        private Optional<TextureAtlasSprite> findInner(Object quadrant, float u, float v) {
            if (quadrant instanceof TextureAtlasSprite) {
                return Optional.of((TextureAtlasSprite) quadrant);
            } else if (quadrant instanceof Node) {
                return ((Node) quadrant).find(u, v);
            }

            return Optional.empty();
        }
    }
}
