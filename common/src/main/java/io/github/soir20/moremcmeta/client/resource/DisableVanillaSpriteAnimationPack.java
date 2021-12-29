/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

package io.github.soir20.moremcmeta.client.resource;

import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.RGBAImageFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DisableVanillaSpriteAnimationPack implements PackResources {
    private static final String VANILLA_METADATA_EXTENSION = ".mcmeta";

    private final Map<ResourceLocation, EventDrivenTexture.Builder> TEXTURES;

    public DisableVanillaSpriteAnimationPack() {
        TEXTURES = new HashMap<>();
    }

    public void setTextures(Map<ResourceLocation, EventDrivenTexture.Builder> textures) {
        TEXTURES.clear();
        TEXTURES.putAll(textures);
    }

    @Nullable
    @Override
    public InputStream getRootResource(String string) {
        return null;
    }

    @Override
    public InputStream getResource(PackType packType, ResourceLocation location) throws IOException {
        if (packType != PackType.CLIENT_RESOURCES) {
            throw new IOException("MoreMcmeta's internal pack only contains client resources");
        }

        ResourceLocation textureLocation = getTextureLocation(location);
        boolean isKnownTexture = TEXTURES.containsKey(textureLocation);
        boolean isVanillaMetadata = location.getPath().endsWith(VANILLA_METADATA_EXTENSION);

        if (isKnownTexture && isVanillaMetadata) {
            Pair<Integer, Integer> frameSize = getFrameSize(textureLocation);
            int frameWidth = frameSize.getFirst();
            int frameHeight = frameSize.getSecond();
            return new ByteArrayInputStream(
                    makeEmptyAnimationJson(frameWidth, frameHeight).getBytes(StandardCharsets.UTF_8)
            );
        } else if (!isKnownTexture) {
            throw new IOException("Requested non-animated resource from MoreMcmeta's internal pack");
        }

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        List<PackResources> allPacks = resourceManager.listPacks().collect(Collectors.toList());

        /* Reverse the list since highest packs are at the end.
           We don't need to search the files of more packs than necessary */
        Collections.reverse(allPacks);

        Optional<PackResources> packWithResource = allPacks.stream().filter(
                (pack) -> otherPackHasResource(pack, textureLocation)
        ).findFirst();

        if (packWithResource.isEmpty()) {
            throw new IOException("Texture set in sprite fix pack does not actually exist!");
        }

        return packWithResource.get().getResource(packType, textureLocation);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String namespace, String pathStart,
                                                     int depth, Predicate<String> pathFilter) {

        // Vanilla packs exclude .mcmeta metadata files, so we should not include them here
        return TEXTURES.keySet().stream().filter((location) -> {
            String path = location.getPath();
            boolean isRightNamespace = location.getNamespace().equals(namespace);
            boolean isRightPath = path.startsWith(pathStart + "/") && pathFilter.test(path);

            return isRightNamespace && isRightPath;
        }).collect(Collectors.toList());

    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation location) {
        if (packType != PackType.CLIENT_RESOURCES) {
            return false;
        }

        return TEXTURES.containsKey(getTextureLocation(location));
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        if (packType != PackType.CLIENT_RESOURCES) {
            return Set.of();
        }

        return Minecraft.getInstance().getResourcePackRepository().openAllSelected().stream()
                .filter((pack) -> !pack.getName().equals(getName()))
                .flatMap((pack) -> {
                    Stream<String> namespaces = pack.getNamespaces(PackType.CLIENT_RESOURCES).stream();
                    pack.close();
                    return namespaces;
                }).collect(Collectors.toSet());
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
        return null;
    }

    @Override
    public String getName() {
        return "__MoreMcmeta Internal__";
    }

    @Override
    public void close() {}

    private ResourceLocation getTextureLocation(ResourceLocation location) {
        return new ResourceLocation(
                location.getNamespace(), location.getPath().replace(VANILLA_METADATA_EXTENSION, "")
        );
    }

    private boolean otherPackHasResource(PackResources otherPack, ResourceLocation location) {
        boolean isOtherPack = otherPack != this;

        if (isOtherPack && otherPack instanceof DisableVanillaSpriteAnimationPack) {
            throw new IllegalStateException("Two sprite fix packs were added to the resource manager!");
        }

        return isOtherPack
                && otherPack.getNamespaces(PackType.CLIENT_RESOURCES).contains(location.getNamespace())
                && otherPack.hasResource(PackType.CLIENT_RESOURCES, location);
    }

    /**
     * Gets the frame size of a texture, assuming it is an animated texture.
     * @param location      the resource location of the texture (non-sprite location)
     * @return a (frameWidth, frameHeight) tuple
     */
    private Pair<Integer, Integer> getFrameSize(ResourceLocation location) {
        Optional<RGBAImageFrame> initialFrame = TEXTURES.get(location).getImage();
        if (initialFrame.isEmpty()) {
            throw new IllegalStateException("Missing initial image guaranteed by texture reader");
        }

        int frameWidth = initialFrame.get().getWidth();
        int frameHeight = initialFrame.get().getHeight();

        return Pair.of(frameWidth, frameHeight);
    }

    private String makeEmptyAnimationJson(int frameWidth, int frameHeight) {
        return String.format(
                "{" +
                        "\"animation\": {" +
                        "\"width\": %d," +
                        "\"height\": %d," +
                        "\"frames\": [0]" +
                        "}" +
                        "}",
                frameWidth, frameHeight
        );
    }
}