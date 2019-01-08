/*
  Copyright 2016-2017 By_syk

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.by_syk.mdcolor.util;

import android.graphics.Color;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by By_syk on 2016-03-31.
 */
public class Palette implements Serializable {
    private final int NUM = 14;

    // Color name
    private String name = "";

    /*
     * 共11个或14个：50、100、200、300、400、500、600、700、800、900、（A100、A200、A400、A700）
     * 默认值为0，即透明
     */
    private int[] colors = new int[NUM];
    /*
     * 共11个或14个
     * 背景之上的建议文字颜色
     */
    private int[] colorsFloatingText = new int[NUM];
    // The name of color versions
    private String[] gradeNames = {"50", "100", "200", "300", "400", "500", "600", "700", "800", "900",
            "A100", "A200", "A400", "A700"
    };

    private int primaryColorOrder = 5;
    private int darkPrimaryColorOrder = 7;
    private int accentColorOrder = 11;

    private int size = NUM;

    public Palette() {}

    public Palette(String csvData) {
        setAll(csvData);
    }

    public Palette(String name, int[] colors) {
        setName(name);

        setColors(colors);
    }

    private int getArrOrder(int i) {
        if (i >= 0 && i < size) {
            return i;
        }

        switch (i) {
            case 50:
                return 0;
            case 100:
                return 1;
            case 200:
                return 2;
            case 300:
                return 3;
            case 400:
                return 4;
            case 500:
                return 5;
            case 600:
                return 6;
            case 700:
                return 7;
            case 800:
                return 8;
            case 900:
                return 9;
            case 1100:
                return 10;
            case 1200:
                return 11;
            case 1400:
                return 12;
            case 1700:
                return 13;
            default:
                return -1;
        }
    }

    /**
     * @param csvData Like:
     *                Red,
     *                6,8,12,
     *                #FFEBEE,#000000,
     *                #FFCDD2,#000000,
     *                #EF9A9A,#000000,
     *                #E57373,#000000,
     *                #EF5350,#FFFFFF,
     *                #F44336,#FFFFFF,
     *                #E53935,#FFFFFF,
     *                #D32F2F,#FFFFFF,
     *                #C62828,#FFFFFF,
     *                #B71C1C,#FFFFFF,
     *                #FF8A80,#000000,
     *                #FF5252,#FFFFFF,
     *                #FF1744,#FFFFFF,
     *                #D50000,#FFFFFF
     */
    public boolean setAll(String csvData) {
        if (TextUtils.isEmpty(csvData)) {
            return false;
        }

        String[] data = csvData.split(",");

        setName(data[0]);

        try {
            primaryColorOrder = Integer.parseInt(data[1]) - 1;
            darkPrimaryColorOrder = Integer.parseInt(data[2]) - 1;
            accentColorOrder = Integer.parseInt(data[3]) - 1;

            for (int i = 4, len = data.length; i < len; i += 2) {
                colors[(i - 4) / 2] = Color.parseColor(data[i]);
                colorsFloatingText[(i - 4) / 2] = Color.parseColor(data[i + 1]);
            }

            size = (data.length - 4) / 2;

            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean setName(String name) {
        if (name == null) {
            return false;
        }

        this.name = name;

        return true;
    }

    public String getName() {
        return name;
    }

    public boolean setColors(int[] colors) {
        if (colors == null) {
            return false;
        }

        for (int i = 0, len = colors.length; i < len; ++i) {
            this.colors[i] = colors[i];
        }

        size = colors.length;

        for (int i = 0; i < size; ++i) {
            colorsFloatingText[i] = Color.WHITE;
        }

        return true;
    }

    public int[] getColors() {
        return colors;
    }

    public int getColor(int i) {
        int order = getArrOrder(i);

        if (i < 0) {
            return Color.TRANSPARENT;
        }

        return colors[order];
    }

    public String getColorStr(int i) {
        int color = getColor(i);
        if ((color & 0xff000000) == 0x00000000) {
            return "";
        }

        String hex = "00000000" + Integer.toHexString(color);

        return String.format("#%1$s", hex.substring(hex.length() - 6).toUpperCase());
    }

    public int getFloatingTextColor(int i) {
        int order = getArrOrder(i);

        if (i < 0) {
            return Color.WHITE;
        }

        return colorsFloatingText[order];
    }

    public String getGradeName(int i) {
        int order = getArrOrder(i);

        if (i < 0) {
            return "";
        }

        return gradeNames[order];
    }

    public int getPrimaryColor() {
        return getColor(primaryColorOrder);
    }

    public String getPrimaryColorStr() {
        return getColorStr(primaryColorOrder);
    }

    public int getDarkPrimaryColor() {
        return getColor(darkPrimaryColorOrder);
    }

    public String getDarkPrimaryColorStr() {
        return getColorStr(darkPrimaryColorOrder);
    }

    public int getAccentColor() {
        return getColor(accentColorOrder);
    }

    public String getAccentColorStr() {
        return getColorStr(accentColorOrder);
    }

    public int getSize() {
        return size;
    }

    /**
     * 以色调500为准
     * 避免背景与文字对比度不高，若主题为 Theme.Material 则忽略此建议
     * @return false - Theme.Material.Light
     *         true - Theme.Material.Light.DarkActionBar
     */
    public boolean isLightThemeWithDarkABSuggested() {
        return getFloatingTextColor(500) == Color.WHITE;
    }

    /**
     * 500、700、A200(A400)
     */
    public boolean isSuggestedHue(int i) {
        int order = getArrOrder(i);

        if (i < 0) {
            return false;
        }

        return order == primaryColorOrder || order == darkPrimaryColorOrder
                || order == accentColorOrder;
    }
}