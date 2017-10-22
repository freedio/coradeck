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
import com.coradec.coradoc.cssenum.BackgroundAttachment;
import com.coradec.coradoc.cssenum.BackgroundClip;
import com.coradec.coradoc.cssenum.BackgroundOrigin;
import com.coradec.coradoc.cssenum.BackgroundRepeat;
import com.coradec.coradoc.cssenum.BackgroundSize;
import com.coradec.coradoc.model.CssPosition;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.ColorCssDeclaration;
import com.coradec.coradoc.token.Comma;
import com.coradec.coradoc.token.Delimiter;
import com.coradec.coradoc.trouble.ImageUrlInvalidException;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * ​​​​Implementation of the CSS background declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BackgroundDeclaration extends ColorCssDeclaration {

    BackgroundColorDeclaration color;
    BackgroundImageDeclaration image;
    BackgroundPositionDeclaration position;
    BackgroundSizeDeclaration size;
    BackgroundRepeatDeclaration repeat;
    BackgroundOriginDeclaration origin;
    BackgroundClipDeclaration clip;
    BackgroundAttachmentDeclaration attachment;
    ParserToken part;

    public BackgroundDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    private ProcessingState base(final ParserToken token) {
        if (BackgroundColorDeclaration.isColor(token))
            color = new BackgroundColorDeclaration(token);
        else if (BackgroundImageDeclaration.isImage(token)) {
            try {
                image = new BackgroundImageDeclaration(token);
            } catch (MalformedURLException e) {
                throw new ImageUrlInvalidException(token);
            }
            return this::afterImage;
        } else if (BackgroundPositionDeclaration.isPosition(token)) {
            position = new BackgroundPositionDeclaration(token);
        } else if (BackgroundPositionDeclaration.isPositionPart(token)) {
            part = token;
            position = new BackgroundPositionDeclaration(token);
            return this::position2;
        } else if (BackgroundRepeatDeclaration.isRepetition(token))
            repeat = new BackgroundRepeatDeclaration(token);
        else if (BackgroundOriginDeclaration.isOrigin(token))
            origin = new BackgroundOriginDeclaration(token);
        else if (BackgroundClipDeclaration.isClip(token))
            clip = new BackgroundClipDeclaration(token);
        else if (BackgroundAttachmentDeclaration.isAttachment(token))
            attachment = new BackgroundAttachmentDeclaration(token);
        else end(token);
        return this::base;
    }

    private ProcessingState position2(final ParserToken token) {
        if (token instanceof Delimiter && ((Delimiter)token).getDelimiter() == '/')
            return size1(token);
        if (BackgroundPositionDeclaration.isPositionPart(token)) {
            position = new BackgroundPositionDeclaration(part, token);
            return this::afterPosition;
        }
        position = new BackgroundPositionDeclaration(part);
        return base(token);
    }

    private ProcessingState afterPosition(final ParserToken token) {
        return token instanceof Delimiter && ((Delimiter)token).getDelimiter() == '/' ? this::size1
                                                                                      : base(token);
    }

    private ProcessingState size1(final ParserToken token) {
        if (BackgroundSizeDeclaration.isSize(token)) {
            size = new BackgroundSizeDeclaration(token);
            return this::base;
        } else if (BackgroundSizeDeclaration.isSizePart(token)) {
            part = token;
            size = new BackgroundSizeDeclaration(token);
            return this::size2;
        } else end(token);
        return this::end;
    }

    private ProcessingState size2(final ParserToken token) {
        if (BackgroundSizeDeclaration.isSizePart(token)) {
            size = new BackgroundSizeDeclaration(part, token);
            return this::base;
        }
        size = new BackgroundSizeDeclaration(part);
        return base(token);
    }

    private ProcessingState afterImage(final ParserToken token) {
        return token instanceof Comma ? this::image : base(token);
    }

    private ProcessingState image(final ParserToken token) {
        if (BackgroundImageDeclaration.isImage(token)) try {
            image.addImage(token);
        } catch (MalformedURLException e) {
            throw new ImageUrlInvalidException(token);
        }
        else end(token);
        return this::afterImage;
    }

    public Color getColor() {
        return color.getColor();
    }

    public List<URL> getImage() {
        return image == null ? BackgroundImageDeclaration.getDefault() : image.getImage();
    }

    public CssPosition getPosition() {
        return position == null ? BackgroundPositionDeclaration.getDefault()
                                : position.getPosition();
    }

    public BackgroundSize getSize() {
        return size == null ? BackgroundSizeDeclaration.getDefault() : size.getSize();
    }

    public @Nullable IntegerMeasure getHeight() {
        return size == null ? null : size.getHeight();
    }

    public @Nullable IntegerMeasure getWidth() {
        return size == null ? null : size.getWidth();
    }

    public BackgroundRepeat getRepeat() {
        return repeat == null ? BackgroundRepeatDeclaration.getDefault() : repeat.getRepetition();
    }

    public BackgroundOrigin getOrigin() {
        return origin == null ? BackgroundOriginDeclaration.getDefault() : origin.getOrigin();
    }

    public BackgroundClip getClip() {
        return clip == null ? BackgroundClipDeclaration.getDefault() : clip.getClip();
    }

    public BackgroundAttachment getAttachment() {
        return attachment == null ? BackgroundAttachmentDeclaration.getDefault()
                                  : attachment.getAttachment();
    }

    @Override public void apply(final Style style) {
        color.apply(style);
        image.apply(style);
        position.apply(style);
        size.apply(style);
        repeat.apply(style);
        origin.apply(style);
        clip.apply(style);
        attachment.apply(style);
    }

}
