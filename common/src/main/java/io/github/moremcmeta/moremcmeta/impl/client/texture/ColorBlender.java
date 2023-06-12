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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import io.github.moremcmeta.moremcmeta.api.client.texture.Color;

/**
 * Utility class to blend colors with gamma correction. Based off
 * {@link net.minecraft.client.renderer.texture.MipmapGenerator} implementation.
 * Colors are in AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB format (32 bits).
 * @author soir20
 * @see <a href="https://learnopengl.com/Advanced-Lighting/Gamma-Correction">OpenGL Reference</a>
 */
public final class ColorBlender {
    private static final double GAMMA = 2.2F;
    private static final double INVERSE_GAMMA = 1 / GAMMA;

    private static final int COMPONENT_MAX = 255;
    private static final float[] POW_GAMMA_LOOKUP = new float[COMPONENT_MAX + 1];
    static {
        for (int componentVal = 0; componentVal < POW_GAMMA_LOOKUP.length; componentVal++) {
            POW_GAMMA_LOOKUP[componentVal] = (float) Math.pow(
                    componentVal / (float) COMPONENT_MAX,
                    GAMMA
            );
        }
    }
    private static final int ALPHA_OFFSET = 24;
    private static final int RED_OFFSET = 0;
    private static final int GREEN_OFFSET = 8;
    private static final int BLUE_OFFSET = 16;

    /**
     * Applies alpha blending/compositing to two colors, one of which overlays the other.
     * @param topColor      color above the other color
     * @param bottomColor   color below the other color
     * @return alpha-blended color
     */
    public static int alphaBlend(int topColor, int bottomColor) {
        float alphaTop = (float) Color.alpha(topColor) / COMPONENT_MAX;
        float alphaTopComplement = 1 - alphaTop;
        float alphaBottom = (float) Color.alpha(bottomColor) / COMPONENT_MAX;
        float alphaOutput = alphaTop + alphaBottom * alphaTopComplement;

        int alpha = (int) (COMPONENT_MAX * alphaOutput);
        int red = alphaBlendComponent(topColor, bottomColor, RED_OFFSET, alphaTop, alphaTopComplement,
                alphaBottom, alphaOutput);
        int green = alphaBlendComponent(topColor, bottomColor, GREEN_OFFSET, alphaTop, alphaTopComplement,
                alphaBottom, alphaOutput);
        int blue = alphaBlendComponent(topColor, bottomColor, BLUE_OFFSET, alphaTop, alphaTopComplement,
                alphaBottom, alphaOutput);

        return packResult(alpha, red, green, blue);
    }

    /**
     * Averages four colors with gamma correction.
     * @param color1        first color
     * @param color2        second color
     * @param color3        third color
     * @param color4        fourth color
     * @return the averaged color
     */
    public static int blend(int color1, int color2, int color3, int color4) {
        color1 = zeroIfInvisible(color1);
        color2 = zeroIfInvisible(color2);
        color3 = zeroIfInvisible(color3);
        color4 = zeroIfInvisible(color4);

        // MC's MipmapGenerator applies gamma correction to the alpha component, so this method does, too
        int blendedAlpha = blendComponent(color1, color2, color3, color4, ALPHA_OFFSET);
        int blendedRed = blendComponent(color1, color2, color3, color4, RED_OFFSET);
        int blendedGreen = blendComponent(color1, color2, color3, color4, GREEN_OFFSET);
        int blendedBlue = blendComponent(color1, color2, color3, color4, BLUE_OFFSET);

        return packResult(blendedAlpha, blendedRed, blendedGreen, blendedBlue);
    }

    /**
     * Returns 0 if the color is invisible (0 alpha value) or the given color
     * if it is not invisible.
     * @param color                 color to check
     * @return 0 if the color is invisible or the color
     */
    private static int zeroIfInvisible(int color) {
        return Color.alpha(color) == 0 ? 0 : color;
    }

    /**
     * Converts a color component from linear space to how it will be displayed on a
     * monitor.
     * @param color         color whose component to convert
     * @param offset        offset in bits from the right where the component is
     *                      located in the color
     * @return the gamma-adjusted value associated with that component (between 0 and 1)
     */
    private static float adjustGamma(int color, int offset) {
        return POW_GAMMA_LOOKUP[(color >> offset) & COMPONENT_MAX];
    }

    /**
     * Averages one component of each of four colors with gamma correction.
     * @param color1        first color
     * @param color2        second color
     * @param color3        third color
     * @param color4        fourth color
     * @param offset        offset in bits from the right where the component is
     *                      located in the color
     * @return the value of the blended component, which is not offset
     */
    private static int blendComponent(int color1, int color2, int color3, int color4, int offset) {
        float gammaAdjusted1 = adjustGamma(color1, offset);
        float gammaAdjusted2 = adjustGamma(color2, offset);
        float gammaAdjusted3 = adjustGamma(color3, offset);
        float gammaAdjusted4 = adjustGamma(color4, offset);

        float average = (gammaAdjusted1 + gammaAdjusted2 + gammaAdjusted3 + gammaAdjusted4) / 4;

        return (int) (Math.pow(average, INVERSE_GAMMA) * COMPONENT_MAX);
    }

    /**
     * Applies alpha blending/compositing to a component in two colors.
     * @param topColor              color above the other color
     * @param bottomColor           color below the other color
     * @param offset                offset of the component in each color
     * @param alphaTop              alpha proportion (between 0 and 1) of the top color
     * @param alphaTopComplement    1 - alphaTop
     * @param alphaBottom           alpha proportion (between 0 and 1) of the bottom color
     * @param alphaOutput           alpha proportion (between 0 and 1) of the output color
     * @return alpha-blended component
     */
    private static int alphaBlendComponent(int topColor, int bottomColor, int offset, float alphaTop,
                                           float alphaTopComplement, float alphaBottom, float alphaOutput) {
        float gammaAdjustedTop = adjustGamma(topColor, offset);
        float gammaAdjustedBottom = adjustGamma(bottomColor, offset);

        float noGammaOutput = (gammaAdjustedTop * alphaTop + gammaAdjustedBottom * alphaBottom * alphaTopComplement)
                / alphaOutput;
        return (int) (Math.pow(noGammaOutput, INVERSE_GAMMA) * COMPONENT_MAX);
    }

    /**
     * Packs individual components into a resultant color.
     * @param alpha     alpha component
     * @param red       red component
     * @param green     green component
     * @param blue      blue component
     * @return components packed as a single color
     */
    private static int packResult(int alpha, int red, int green, int blue) {
        return alpha << ALPHA_OFFSET | red << RED_OFFSET | green << GREEN_OFFSET | blue << BLUE_OFFSET;
    }

    /**
     * Prevents the blender from being constructed.
     */
    private ColorBlender() {}

}
