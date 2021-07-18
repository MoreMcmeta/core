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

package io.github.soir20.moremcmeta.client.animation;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link WobbleFunction}.
 * @author soir20
 */
public class WobbleFunctionTest {

    @Test
    public void calculate_DaytimeNegative_CorrectLateTime() {
        WobbleFunction function = new WobbleFunction();
        long result = function.calculate(-1880, 1, true);
        assertEquals(23831, result);
    }

    @Test
    public void calculate_DaytimeZero_Zero() {
        WobbleFunction function = new WobbleFunction();
        long result = function.calculate(0, 1, true);
        assertEquals(0, result);
    }

    @Test
    public void calculate_Daytime24000_Zero() {
        WobbleFunction function = new WobbleFunction();
        long result = function.calculate(24000, 1, true);
        assertEquals(0, result);
    }

    @Test
    public void calculate_DaytimeOver24000_CorrectEarlyTime() {
        WobbleFunction function = new WobbleFunction();
        long result = function.calculate(27091, 1, true);
        assertEquals(278, result);
    }

    @Test
    public void calculate_ForwardsBackwards_CorrectTime() {
        WobbleFunction function = new WobbleFunction();
        function.calculate(21193, 1, true);
        long result = function.calculate(241, 2, true);
        assertEquals(23564, result);
    }

    @Test
    public void calculate_SingleDayTime_ApproachesSingleTime() {
        WobbleFunction function = new WobbleFunction();
        long result = 0;
        for (int tick = 1; tick <= 10000; tick++) {
            result = function.calculate(21193, tick, true);
        }
        assertEquals(21193, result);
    }

    @Test
    public void calculate_GameTimeZero_Zero() {
        WobbleFunction function = new WobbleFunction();
        long result = function.calculate(21193, 0, true);
        assertEquals(0, result);
    }

    @Test
    public void calculate_GameTimeBehindLastTime_StillRecalculated() {
        WobbleFunction function = new WobbleFunction();
        function.calculate(21193, 2, true);
        long result = function.calculate(241, 1, true);
        assertEquals(23564, result);
    }

    @Test
    public void calculate_GameTimeReturnsToZero_Recalculated() {
        WobbleFunction function = new WobbleFunction();
        function.calculate(21193, 2, true);
        long result = function.calculate(241, 0, true);
        assertEquals(23564, result);
    }

    @Test
    public void calculate_NaturalGameTimeEqual_NotRecalculated() {
        WobbleFunction function = new WobbleFunction();
        function.calculate(21193, 2, true);
        long result = function.calculate(241, 2, true);
        assertEquals(23747, result);
    }

    @Test
    public void calculate_NotNaturalGameTimeEqual_NotRecalculated() {
        WobbleFunction function = new WobbleFunction(7172021);
        function.calculate(21193, 2, false);
        long result = function.calculate(241, 2, false);
        assertEquals(933, result);
    }

    @Test
    public void calculate_NotNaturalGameTimeNotEqual_NotRecalculated() {
        WobbleFunction function = new WobbleFunction(7172021);
        function.calculate(21193, 2, false);
        long result = function.calculate(241, 3, false);
        assertEquals(822, result);
    }

}