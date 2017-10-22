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

import com.coradec.coradoc.cssenum.BackgroundImage;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Comma;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.UrlToken;
import com.coradec.coradoc.trouble.ImageUrlInvalidException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ​​Implementation of the CSS background-image declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BackgroundImageDeclaration extends BasicCssDeclaration {

    static List<URL> getDefault() {
        return Collections.emptyList();
    }

    static boolean isImage(final ParserToken token) {
        return token instanceof UrlToken ||
               token instanceof Identifier && isValidImageIdentifier((Identifier)token);
    }

    private static boolean isValidImageIdentifier(final Identifier image) {
        try {
            BackgroundImage.valueOf(image.getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private BackgroundImage image;

    private List<URL> images;

    /**
     * Initializes a new instance of BackgroundImageDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected BackgroundImageDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of BackgroundImageDeclaration with the specified image token.
     *
     * @param image the image token.
     * @throws IllegalArgumentException if the specified token is not a valid background image.
     * @throws MalformedURLException    if the specified image URL is ill-formed.
     */
    protected BackgroundImageDeclaration(final ParserToken image) throws MalformedURLException {
        super("background-image");
        addImage(image);
    }

    void addImage(final ParserToken image) throws MalformedURLException {
        if (image instanceof UrlToken) {
            images().add(((UrlToken)image).toURL());
        } else throw new ImageUrlInvalidException(image);
    }

    private List<URL> images() {
        if (images == null) images = new ArrayList<>();
        return images;
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    private ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) {
            try {
                image = BackgroundImage.valueOf(((Identifier)token).getEnumTag());
            } catch (IllegalArgumentException e) {
                end(token);
            }
        } else if (isImage(token)) try {
            addImage(token);
        } catch (MalformedURLException e) {
            throw new ImageUrlInvalidException(token);
        }
        else end(token);
        return this::afterImage;
    }

    private ProcessingState afterImage(final ParserToken token) {
        if (token instanceof Comma) return this::base;
        return end(token);
    }

    public List<URL> getImage() {
        return image == BackgroundImage.NONE ? Collections.emptyList()
                                             : Collections.unmodifiableList(images);
    }

    @Override public void apply(final Style style) {
        style.setBackgroundImage(getImage());
    }
}
