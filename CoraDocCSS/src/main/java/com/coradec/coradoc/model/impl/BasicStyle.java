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

package com.coradec.coradoc.model.impl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.util.ClassUtil;
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
import com.coradec.coradoc.model.CssPosition;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.NumericToken;

import java.awt.*;
import java.net.URL;
import java.util.List;

/**
 * ​​Basic implementation of a style.
 */
public class BasicStyle implements Style {

    private BackgroundSize backgroundSize;
    private IntegerMeasure bacgroundHeight;
    private IntegerMeasure backgroundWidth;
    private BackgroundAttachment backgroundAttachment;
    private BackgroundClip backgroundClip;
    private Color backgroundColor;
    private List<URL> backgroundImage;
    private CssPosition backgroundPosition;
    private BackgroundRepeat backgroundRepeat;
    private BackgroundOrigin backgroundOrigin;
    private BorderStyle borderTopStyle;
    private BorderStyle borderLeftStyle;
    private BorderStyle borderRightStyle;
    private BorderStyle borderBottomStyle;
    private BorderWidth borderTopWidth;
    private @Nullable IntegerDimension borderTopWidthValue;
    private BorderWidth borderLeftWidth;
    private @Nullable IntegerDimension borderLeftWidthValue;
    private BorderWidth borderRightWidth;
    private @Nullable IntegerDimension borderRightWidthValue;
    private BorderWidth borderBottomWidth;
    private @Nullable IntegerDimension borderBottomWidthValue;
    private Color foregroundColor;
    private List<String> fontFamily;
    private FontSize fontSize;
    private IntegerMeasure fontSizeValue;
    private FontStyle fontStyle;
    private FontVariant fontVariant;
    private FontWeight fontWeight;
    private LineHeight lineHeight;
    private IntegerMeasure paddingTop;
    private IntegerMeasure paddingLeft;
    private IntegerMeasure paddingRight;
    private IntegerMeasure paddingBottom;
    private Color borderTopColor;
    private Color borderLeftColor;
    private Color borderRightColor;
    private Color borderBottomColor;
    private Position position;
    private Distance top;
    private @Nullable IntegerMeasure topValue;
    private Distance left;
    private @Nullable IntegerMeasure leftValue;
    private Distance right;
    private @Nullable IntegerMeasure rightValue;
    private Distance bottom;
    private @Nullable IntegerMeasure bottomValue;
    private Distance height;
    private @Nullable IntegerMeasure heightValue;
    private Distance width;
    private @Nullable IntegerMeasure widthValue;

    @Override public void setBackgroundSize(final BackgroundSize size) {
        backgroundSize = size;
    }

    @Override public void setBackgroundHeight(final IntegerMeasure height) {
        bacgroundHeight = height;
    }

    @Override public void setBackgroundWidth(final IntegerMeasure width) {
        backgroundWidth = width;
    }

    @Override public void setBackgroundAttachment(final BackgroundAttachment attachment) {
        backgroundAttachment = attachment;
    }

    @Override public void setBackgroundClip(final BackgroundClip clip) {
        backgroundClip = clip;
    }

    @Override public void setBackgroundColor(final Color color) {
        backgroundColor = color;
    }

    @Override public void setBackgroundImage(final List<URL> image) {
        backgroundImage = image;
    }

    @Override public void setBackgroundPosition(final CssPosition position) {
        backgroundPosition = position;
    }

    @Override public void setBackgroundRepeat(final BackgroundRepeat repeat) {
        backgroundRepeat = repeat;
    }

    @Override public void setBackgroundOrigin(final BackgroundOrigin origin) {
        backgroundOrigin = origin;
    }

    @Override public void setBorderTopStyle(final BorderStyle topStyle) {
        borderTopStyle = topStyle;
    }

    @Override public void setBorderLeftStyle(final BorderStyle leftStyle) {
        borderLeftStyle = leftStyle;
    }

    @Override public void setBorderRightStyle(final BorderStyle rightStyle) {
        borderRightStyle = rightStyle;
    }

    @Override public void setBorderBottomStyle(final BorderStyle bottomStyle) {
        borderBottomStyle = bottomStyle;
    }

    @Override public void setBorderTopWidth(final BorderWidth topWidth,
            final @Nullable IntegerDimension topValue) {
        borderTopWidth = topWidth;
        borderTopWidthValue = topValue;
    }

    @Override public void setBorderLeftWidth(final BorderWidth leftWidth,
            final @Nullable IntegerDimension leftValue) {
        borderLeftWidth = leftWidth;
        borderLeftWidthValue = leftValue;
    }

    @Override public void setBorderRightWidth(final BorderWidth rightWidth,
            final @Nullable IntegerDimension rightValue) {
        borderRightWidth = rightWidth;
        borderRightWidthValue = rightValue;
    }

    @Override public void setBorderBottomWidth(final BorderWidth bottomWidth,
            final @Nullable IntegerDimension bottomValue) {
        borderBottomWidth = bottomWidth;
        borderBottomWidthValue = bottomValue;
    }

    @Override public void setForegroundColor(final Color color) {
        foregroundColor = color;
    }

    @Override public void setFontFamily(final List<String> family) {
        fontFamily = family;
    }

    @Override public void setFontSize(final FontSize size, final IntegerMeasure value) {
        fontSize = size;
        fontSizeValue = value;
    }

    @Override public void setFontStyle(final FontStyle style) {
        fontStyle = style;
    }

    @Override public void setFontVariant(final FontVariant variant) {
        fontVariant = variant;
    }

    @Override public void setFontWeight(final FontWeight weight) {
        fontWeight = weight;
    }

    @Override public void setLineHeight(final LineHeight height, final NumericToken value) {
        lineHeight = height;
    }

    @Override public void setPaddingTop(final IntegerMeasure top) {
        paddingTop = top;
    }

    @Override public void setPaddingLeft(final IntegerMeasure left) {
        paddingLeft = left;
    }

    @Override public void setPaddingRight(final IntegerMeasure right) {
        paddingRight = right;
    }

    @Override public void setPaddingBottom(final IntegerMeasure bottom) {
        paddingBottom = bottom;
    }

    @Override public void setBorderTopColor(final Color topColor) {
        borderTopColor = topColor;
    }

    @Override public void setBorderLeftColor(final Color leftColor) {
        borderLeftColor = leftColor;
    }

    @Override public void setBorderRightColor(final Color rightColor) {
        borderRightColor = rightColor;
    }

    @Override public void setBorderBottomColor(final Color bottomColor) {
        borderBottomColor = bottomColor;
    }

    @Override public void setPosition(final Position position) {
        this.position = position;
    }

    @Override public void setTop(final Distance top, @Nullable final IntegerMeasure topValue) {
        this.top = top;
        this.topValue = topValue;
    }

    @Override public void setLeft(final Distance left, @Nullable final IntegerMeasure leftValue) {
        this.left = left;
        this.leftValue = leftValue;
    }

    @Override
    public void setRight(final Distance right, @Nullable final IntegerMeasure rightValue) {
        this.right = right;
        this.rightValue = rightValue;
    }

    @Override
    public void setBottom(final Distance bottom, @Nullable final IntegerMeasure bottomValue) {
        this.bottom = bottom;
        this.bottomValue = bottomValue;
    }

    @Override
    public void setHeight(final Distance height, @Nullable final IntegerMeasure heightValue) {
        this.height = height;
        this.heightValue = heightValue;
    }

    @Override
    public void setWidth(final Distance width, @Nullable final IntegerMeasure widthValue) {
        this.width = width;
        this.widthValue = widthValue;
    }

    @Override public Distance getTop() {
        return top;
    }

    @Override public @Nullable IntegerMeasure getTopValue() {
        return topValue;
    }

    @Override public Distance getLeft() {
        return left;
    }

    @Override public @Nullable IntegerMeasure getLeftValue() {
        return leftValue;
    }

    @Override public Distance getHeight() {
        return height;
    }

    @Override public @Nullable IntegerMeasure getHeightValue() {
        return heightValue;
    }

    @Override public Distance getWidth() {
        return width;
    }

    @Override public @Nullable IntegerMeasure getWidthValue() {
        return widthValue;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
