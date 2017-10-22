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

package com.coradec.coradoc.model;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.BackgroundAttachment;
import com.coradec.coradoc.cssenum.BackgroundClip;
import com.coradec.coradoc.cssenum.BackgroundOrigin;
import com.coradec.coradoc.cssenum.BackgroundRepeat;
import com.coradec.coradoc.cssenum.BackgroundSize;
import com.coradec.coradoc.cssenum.BorderStyle;
import com.coradec.coradoc.cssenum.BorderWidth;
import com.coradec.coradoc.cssenum.Distance;
import com.coradec.coradoc.cssenum.FontSize;
import com.coradec.coradoc.cssenum.FontStyle;
import com.coradec.coradoc.cssenum.FontVariant;
import com.coradec.coradoc.cssenum.FontWeight;
import com.coradec.coradoc.cssenum.LineHeight;
import com.coradec.coradoc.cssenum.Position;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.NumericToken;

import java.awt.*;
import java.net.URL;
import java.util.List;

/**
 * ​A set of CSS properties for a widget.
 */
public interface Style {

    /**
     * Sets the background size.
     *
     * @param size the background size.
     */
    void setBackgroundSize(BackgroundSize size);

    /**
     * Sets the background height, if specified.
     *
     * @param height the background height (optional).
     */
    void setBackgroundHeight(@Nullable IntegerMeasure height);

    /**
     * Sets the background width, if specified.
     *
     * @param width the background width.
     */
    void setBackgroundWidth(@Nullable IntegerMeasure width);

    /**
     * Sets the background attachment.
     *
     * @param attachment the background attachment.
     */
    void setBackgroundAttachment(BackgroundAttachment attachment);

    /**
     * Sets the background clip.
     *
     * @param clip the background clip.
     */
    void setBackgroundClip(BackgroundClip clip);

    /**
     * Sets the background color.
     *
     * @param color the background color.
     */
    void setBackgroundColor(Color color);

    /**
     * Sets the background image(s).
     *
     * @param image the background image(s).
     */
    void setBackgroundImage(List<URL> image);

    /**
     * Sets the background position.
     *
     * @param position the background position.
     */
    void setBackgroundPosition(CssPosition position);

    /**
     * Sets the background repeat.
     *
     * @param repeat the background repeat.
     */
    void setBackgroundRepeat(BackgroundRepeat repeat);

    /**
     * Sets the background origin.
     *
     * @param origin the background origin.
     */
    void setBackgroundOrigin(BackgroundOrigin origin);

    /**
     * Sets the border top style.
     *
     * @param topStyle the border top style.
     */
    void setBorderTopStyle(BorderStyle topStyle);

    /**
     * Sets the border left style.
     *
     * @param leftStyle the border left style.
     */
    void setBorderLeftStyle(BorderStyle leftStyle);

    /**
     * Sets the border right style.
     *
     * @param rightStyle the border right style.
     */
    void setBorderRightStyle(BorderStyle rightStyle);

    /**
     * Sets the border bottom style.
     *
     * @param bottomStyle the border bottom style.
     */
    void setBorderBottomStyle(BorderStyle bottomStyle);

    /**
     * Sets the border top width.
     *
     * @param topWidth the top width code.
     * @param topValue the top width value.
     */
    void setBorderTopWidth(BorderWidth topWidth, IntegerDimension topValue);

    /**
     * Sets the border left width.
     *
     * @param leftWidth the left width code.
     * @param leftValue the left width value.
     */
    void setBorderLeftWidth(BorderWidth leftWidth, IntegerDimension leftValue);

    /**
     * Sets the border right width.
     *
     * @param rightWidth the right width code.
     * @param rightValue the right width value.
     */
    void setBorderRightWidth(BorderWidth rightWidth, IntegerDimension rightValue);

    /**
     * Sets the border bottom width.
     *
     * @param bottomWidth the bottom width code.
     * @param bottomValue the bottom width value.
     */
    void setBorderBottomWidth(BorderWidth bottomWidth, IntegerDimension bottomValue);

    /**
     * Sets the foreground color.
     *
     * @param color the color.
     */
    void setForegroundColor(Color color);

    /**
     * Sets the font family.
     *
     * @param family the font family.
     */
    void setFontFamily(List<String> family);

    /**
     * Sets the font size.
     *
     * @param size  the f0nt size code.
     * @param value the font size value.
     */
    void setFontSize(FontSize size, @Nullable IntegerMeasure value);

    /**
     * Sets the font style.
     *
     * @param style the font style.
     */
    void setFontStyle(FontStyle style);

    /**
     * Sets the font variant.
     *
     * @param variant the font variant.
     */
    void setFontVariant(FontVariant variant);

    /**
     * Sets the font weight.
     *
     * @param weight the font weight.
     */
    void setFontWeight(FontWeight weight);

    /**
     * Sets the line height.
     *
     * @param height the line height code.
     * @param value  the line height value.
     */
    void setLineHeight(LineHeight height, @Nullable final NumericToken value);

    /**
     * Sets the top padding.
     *
     * @param top the top padding.
     */
    void setPaddingTop(IntegerMeasure top);

    /**
     * Sets the left padding.
     *
     * @param left the left padding.
     */
    void setPaddingLeft(IntegerMeasure left);

    /**
     * Sets the right padding.
     *
     * @param right the right padding.
     */
    void setPaddingRight(IntegerMeasure right);

    /**
     * Sets the bottom padding.
     *
     * @param bottom the bottom padding.
     */
    void setPaddingBottom(IntegerMeasure bottom);

    /**
     * Sets the border top color.
     *
     * @param topColor the top color.
     */
    void setBorderTopColor(Color topColor);

    /**
     * Sets the border left color.
     *
     * @param leftColor the left color.
     */
    void setBorderLeftColor(Color leftColor);

    /**
     * Sets the border right color.
     *
     * @param rightColor the right color.
     */
    void setBorderRightColor(Color rightColor);

    /**
     * Sets the border bottom color.
     *
     * @param bottomColor the bottom color.
     */
    void setBorderBottomColor(Color bottomColor);

    /**
     * Sets the component position.
     *
     * @param position the position.
     */
    void setPosition(Position position);

    /**
     * Sets the top distance.
     *
     * @param top      the top code.
     * @param topValue the top distance, if present.
     */
    void setTop(Distance top, @Nullable IntegerMeasure topValue);

    /**
     * Sets the left distance.
     *
     * @param left      the left code.
     * @param leftValue the left distance, if present.
     */
    void setLeft(Distance left, @Nullable IntegerMeasure leftValue);

    /**
     * Sets the right distance.
     *
     * @param right      the right code.
     * @param rightValue the right distance, if present.
     */
    void setRight(Distance right, @Nullable IntegerMeasure rightValue);

    /**
     * Sets the bottom distance.
     *
     * @param bottom      the bottom code.
     * @param bottomValue the bottom distance, if present.
     */
    void setBottom(Distance bottom, @Nullable IntegerMeasure bottomValue);

    /**
     * Sets the component height.
     *
     * @param height      the height code.
     * @param heightValue the height value.
     */
    void setHeight(Distance height, @Nullable IntegerMeasure heightValue);

    /**
     * Sets the component width.
     *
     * @param width      the width code.
     * @param widthValue the width value, if present.
     */
    void setWidth(Distance width, @Nullable IntegerMeasure widthValue);

    /**
     * Returns the top distance code.
     *
     * @return the top distance code.
     */
    Distance getTop();

    /**
     * Returns the top distance.
     *
     * @return the top distance.
     */
    @Nullable IntegerMeasure getTopValue();

    /**
     * Returns the left distance code.
     *
     * @return the left distance code.
     */
    Distance getLeft();

    /**
     * Returns the left distance.
     *
     * @return the left distance.
     */
    @Nullable IntegerMeasure getLeftValue();

    /**
     * Returns the height code.
     *
     * @return the height code.
     */
    Distance getHeight();

    /**
     * Returns the height.
     *
     * @return the height.
     */
    @Nullable IntegerMeasure getHeightValue();

    /**
     * Returns the width code.
     *
     * @return the width code.
     */
    Distance getWidth();

    /**
     * Returns the width.
     *
     * @return the width.
     */
    @Nullable IntegerMeasure getWidthValue();

}
