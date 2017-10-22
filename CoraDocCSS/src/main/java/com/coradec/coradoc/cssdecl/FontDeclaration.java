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

package com.coradec.coradoc.cssdecl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.FontSize;
import com.coradec.coradoc.cssenum.FontStyle;
import com.coradec.coradoc.cssenum.FontVariant;
import com.coradec.coradoc.cssenum.FontWeight;
import com.coradec.coradoc.cssenum.LineHeight;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Comma;
import com.coradec.coradoc.token.Delimiter;
import com.coradec.coradoc.token.NumericToken;

import java.util.Collections;
import java.util.List;

/**
 * ​​Implementation of the CSS font declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class FontDeclaration extends BasicCssDeclaration {

    FontStyleDeclaration style;
    FontVariantDeclaration variant;
    FontWeightDeclaration weight;
    FontSizeDeclaration size;
    LineHeightDeclaration lineHeight;
    FontFamilyDeclaration family;

    public FontDeclaration(ModifiableDeclaration source) {
        super(source);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    protected ProcessingState base(final ParserToken token) {
        if (FontStyleDeclaration.isStyle(token) && style == null)
            style = new FontStyleDeclaration(token);
        else if (FontVariantDeclaration.isVariant(token) && variant == null)
            variant = new FontVariantDeclaration(token);
        else if (FontWeightDeclaration.isWeight(token) && weight == null)
            weight = new FontWeightDeclaration(token);
        else if (FontSizeDeclaration.isSize(token) && size == null) {
            size = new FontSizeDeclaration(token);
            return this::afterSize;
        } else if (FontFamilyDeclaration.isFamily(token) && family == null) {
            family = new FontFamilyDeclaration(token);
            return this::afterFamily;
        } else end(token);
        return this::base;
    }

    private ProcessingState afterSize(final ParserToken token) {
        if (token instanceof Delimiter && ((Delimiter)token).getDelimiter() == '/')
            return this::lineHeight;
        return base(token);
    }

    private ProcessingState lineHeight(final ParserToken token) {
        if (LineHeightDeclaration.isLineHeight(token))
            lineHeight = new LineHeightDeclaration(token);
        else end(token);
        return this::base;
    }

    private ProcessingState afterFamily(final ParserToken token) {
        if (token instanceof Comma) return this::family;
        return base(token);
    }

    private ProcessingState family(final ParserToken token) {
        if (FontFamilyDeclaration.isFamily(token)) family.addFamily(token);
        else end(token);
        return this::afterFamily;
    }

    public FontStyle getStyle() {
        return style == null ? FontStyleDeclaration.getDefault() : style.getStyle();
    }

    public FontVariant getVariant() {
        return variant == null ? FontVariantDeclaration.getDefault() : variant.getVariant();
    }

    public FontWeight getWeight() {
        return weight == null ? FontWeightDeclaration.getDefault() : weight.getWeight();
    }

    public FontSize getSize() {
        return size == null ? FontSizeDeclaration.getDefault() : size.getSize();
    }

    public @Nullable IntegerMeasure getSizeValue() {
        return size == null ? FontSizeDeclaration.getDefaultValue() : size.getValue();
    }

    public LineHeight getLineHeight() {
        return lineHeight == null ? LineHeightDeclaration.getDefault() : lineHeight.getHeight();
    }

    public @Nullable NumericToken getLineHeightValue() {
        return lineHeight == null ? LineHeightDeclaration.getDefaultValue() : lineHeight.getValue();
    }

    public List<String> getFamily() {
        return family == null ? Collections.emptyList() : family.getFamily();
    }

    @Override public void apply(final Style style) {
        this.style.apply(style);
        variant.apply(style);
        weight.apply(style);
        size.apply(style);
        lineHeight.apply(style);
        family.apply(style);
    }

}
