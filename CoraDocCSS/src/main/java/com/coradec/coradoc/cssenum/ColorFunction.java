/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the
 * GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 * @author Dominik Wezel <dom@coradec.com>
 *
 */

package com.coradec.coradoc.cssenum;

import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.token.IntegerPercentage;
import com.coradec.coradoc.token.IntegerToken;
import com.coradec.coradoc.token.RealToken;
import com.coradec.coradoc.trouble.ColorComponentValueInvalid;
import com.coradec.coradoc.trouble.ColorComponentValuesInvalid;

import java.util.List;

/**
 * ​Enumeration of color functions.
 */
public enum ColorFunction {
    RGB {
        @Override public Long apply(final List<ParserToken> componentValues) {
            expect3(componentValues);
            int red = colorComponent(componentValues.get(0));
            int green = colorComponent(componentValues.get(1));
            int blue = colorComponent(componentValues.get(2));
            return (long)((red << 16) + (green << 8) + blue);
        }
    },
    RGBA {
        @Override public Long apply(final List<ParserToken> componentValues) {
            expect4(componentValues);
            int red = colorComponent(componentValues.get(0));
            int green = colorComponent(componentValues.get(1));
            int blue = colorComponent(componentValues.get(2));
            int alpha = alphaComponent(componentValues.get(3));
            return (long)((alpha << 24) + (red << 16) + (green << 8) + blue);
        }
    },
    HSL {
        @Override public Long apply(final List<ParserToken> componentValues) {
            expect3(componentValues);
            int hue = hueComponent(componentValues.get(0));
            double sat = pctComponent(componentValues.get(1));
            double lum = pctComponent(componentValues.get(2));
            return hsl2rgb(hue % 360, sat, lum);
        }
    },
    HSLA {
        @Override public Long apply(final List<ParserToken> componentValues) {
            expect4(componentValues);
            int hue = hueComponent(componentValues.get(0));
            double sat = pctComponent(componentValues.get(1));
            double lum = pctComponent(componentValues.get(2));
            int alpha = alphaComponent(componentValues.get(3));
            return (long)(alpha << 24) + hsl2rgb(hue % 360, sat, lum);
        }
    };

    @SuppressWarnings("NonAsciiCharacters")
    static long hsl2rgb(final int hue, final double sat, final double lum) {
        final double c = (1 - Math.abs(2 * lum - 1)) * sat;
        final double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        final double μ = lum - c / 2.0;
        double ρ, γ, β;
        if (hue < 60) {
            ρ = c;
            γ = x;
            β = 0.0;
        } else if (hue < 120) {
            ρ = x;
            γ = c;
            β = 0.0;
        } else if (hue < 180) {
            ρ = 0.0;
            γ = c;
            β = x;
        } else if (hue < 240) {
            ρ = 0.0;
            γ = x;
            β = c;
        } else if (hue < 300) {
            ρ = x;
            γ = 0.0;
            β = c;
        } else /*if (hue < 360)*/ {
            ρ = c;
            γ = 0.0;
            β = x;
        }
        int r = (int)((ρ + μ) * 255.0);
        int g = (int)((γ + μ) * 255.0);
        int b = (int)((β + μ) * 255.0);
        return r << 16 + g << 8 + b;
    }

    static int alphaComponent(final ParserToken parserToken) {
        if (parserToken instanceof RealToken) {
            final double value = ((RealToken)parserToken).getValue();
            if (value < 0.0 || value > 1.0) throw new ColorComponentValueInvalid(parserToken);
            return (int)(255 * value);
        }
        throw new ColorComponentValueInvalid(parserToken);
    }

    static int colorComponent(final ParserToken parserToken) {
        if (parserToken instanceof IntegerPercentage) {
            int value = ((IntegerPercentage)parserToken).getValue();
            if (value < 0 || value > 100) throw new ColorComponentValueInvalid(parserToken);
            return 255 * value / 100;
        }
        if (parserToken instanceof IntegerToken) {
            int value = ((IntegerToken)parserToken).getValue();
            if (value < 0 || value > 255) throw new ColorComponentValueInvalid(parserToken);
            return value;
        }
        throw new ColorComponentValueInvalid(parserToken);
    }

    static int hueComponent(final ParserToken parserToken) {
        if (parserToken instanceof IntegerToken) {
            int value = ((IntegerToken)parserToken).getValue();
            if (value < 0 || value > 360) throw new ColorComponentValueInvalid(parserToken);
            return value;
        }
        throw new ColorComponentValueInvalid(parserToken);
    }

    static double pctComponent(final ParserToken parserToken) {
        if (parserToken instanceof IntegerPercentage) {
            final int value = ((IntegerPercentage)parserToken).getValue();
            if (value < 0 || value > 100) throw new ColorComponentValueInvalid(parserToken);
            return value / 100.0;
        }
        throw new ColorComponentValueInvalid(parserToken);
    }

    static void expect3(final List<ParserToken> componentValues) {
        if (componentValues.size() != 3) throw new ColorComponentValuesInvalid(componentValues);
    }

    static void expect4(final List<ParserToken> componentValues) {
        if (componentValues.size() != 4) throw new ColorComponentValuesInvalid(componentValues);
    }

    public abstract Long apply(final List<ParserToken> componentValues);
}
