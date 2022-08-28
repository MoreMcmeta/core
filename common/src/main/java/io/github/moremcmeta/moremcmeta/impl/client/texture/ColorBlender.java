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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

/**
 * Utility class to blend colors with gamma correction. Based off
 * {@link net.minecraft.client.renderer.texture.MipmapGenerator} implementation.
 * Colors are in AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB format (32 bits).
 * @author soir20
 * @see <a href="https://learnopengl.com/Advanced-Lighting/Gamma-Correction">OpenGL Reference</a>
 */
public class ColorBlender {
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

        int blendedAlpha = blendComponent(color1, color2, color3, color4, 24);
        int blendedRed = blendComponent(color1, color2, color3, color4, 16);
        int blendedGreen = blendComponent(color1, color2, color3, color4, 8);
        int blendedBlue = blendComponent(color1, color2, color3, color4, 0);

        return blendedAlpha << 24 | blendedRed << 16 | blendedGreen << 8 | blendedBlue;
    }

    /**
     * Returns 0 if the color is invisible (0 alpha value) or the given color
     * if it is not invisible.
     * @param color                 color to check
     * @return 0 if the color is invisible or the color
     */
    private static int zeroIfInvisible(int color) {
        return color >> 24 == 0 ? 0 : color;
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

}
