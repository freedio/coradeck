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
 * Implementation of a default style for unmatched components.
 */
public class DefaultStyle implements Style {

    @Override public void setBackgroundSize(final BackgroundSize size) {

    }

    @Override public void setBackgroundHeight(final IntegerMeasure height) {

    }

    @Override public void setBackgroundWidth(final IntegerMeasure width) {

    }

    @Override public void setBackgroundAttachment(final BackgroundAttachment attachment) {

    }

    @Override public void setBackgroundClip(final BackgroundClip clip) {

    }

    @Override public void setBackgroundColor(final Color color) {

    }

    @Override public void setBackgroundImage(final List<URL> image) {

    }

    @Override public void setBackgroundPosition(final CssPosition position) {

    }

    @Override public void setBackgroundRepeat(final BackgroundRepeat repeat) {

    }

    @Override public void setBackgroundOrigin(final BackgroundOrigin origin) {

    }

    @Override public void setBorderTopStyle(final BorderStyle topStyle) {

    }

    @Override public void setBorderLeftStyle(final BorderStyle leftStyle) {

    }

    @Override public void setBorderRightStyle(final BorderStyle rightStyle) {

    }

    @Override public void setBorderBottomStyle(final BorderStyle bottomStyle) {

    }

    @Override
    public void setBorderTopWidth(final BorderWidth topWidth, final IntegerDimension topValue) {

    }

    @Override
    public void setBorderLeftWidth(final BorderWidth leftWidth, final IntegerDimension leftValue) {

    }

    @Override public void setBorderRightWidth(final BorderWidth rightWidth,
            final IntegerDimension rightValue) {

    }

    @Override public void setBorderBottomWidth(final BorderWidth bottomWidth,
            final IntegerDimension bottomValue) {

    }

    @Override public void setForegroundColor(final Color color) {

    }

    @Override public void setFontFamily(final List<String> family) {

    }

    @Override public void setFontSize(final FontSize size, final IntegerMeasure value) {

    }

    @Override public void setFontStyle(final FontStyle style) {

    }

    @Override public void setFontVariant(final FontVariant variant) {

    }

    @Override public void setFontWeight(final FontWeight weight) {

    }

    @Override public void setLineHeight(final LineHeight height, final NumericToken value) {

    }

    @Override public void setPaddingTop(final IntegerMeasure top) {

    }

    @Override public void setPaddingLeft(final IntegerMeasure left) {

    }

    @Override public void setPaddingRight(final IntegerMeasure right) {

    }

    @Override public void setPaddingBottom(final IntegerMeasure bottom) {

    }

    @Override public void setBorderTopColor(final Color topColor) {

    }

    @Override public void setBorderLeftColor(final Color leftColor) {

    }

    @Override public void setBorderRightColor(final Color rightColor) {

    }

    @Override public void setBorderBottomColor(final Color bottomColor) {

    }

    @Override public void setPosition(final Position position) {

    }

    @Override public void setTop(final Distance top, @Nullable final IntegerMeasure topValue) {

    }

    @Override public void setLeft(final Distance left, @Nullable final IntegerMeasure leftValue) {

    }

    @Override
    public void setRight(final Distance right, @Nullable final IntegerMeasure rightValue) {

    }

    @Override
    public void setBottom(final Distance bottom, @Nullable final IntegerMeasure bottomValue) {

    }

    @Override
    public void setHeight(final Distance height, @Nullable final IntegerMeasure heightValue) {

    }

    @Override
    public void setWidth(final Distance width, @Nullable final IntegerMeasure widthValue) {

    }

    @Override public Distance getTop() {
        return Distance.AUTO;
    }

    @Override public @Nullable IntegerMeasure getTopValue() {
        return null;
    }

    @Override public Distance getLeft() {
        return Distance.AUTO;
    }

    @Override public @Nullable IntegerMeasure getLeftValue() {
        return null;
    }

    @Override public Distance getHeight() {
        return Distance.AUTO;
    }

    @Override public @Nullable IntegerMeasure getHeightValue() {
        return null;
    }

    @Override public Distance getWidth() {
        return Distance.AUTO;
    }

    @Override public @Nullable IntegerMeasure getWidthValue() {
        return null;
    }

}
