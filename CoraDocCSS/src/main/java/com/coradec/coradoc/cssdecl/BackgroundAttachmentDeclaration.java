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

import com.coradec.coradoc.cssenum.BackgroundAttachment;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS background-attachment declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BackgroundAttachmentDeclaration extends BasicCssDeclaration {

    static BackgroundAttachment getDefault() {
        return BackgroundAttachment.SCROLL;
    }

    static boolean isAttachment(final ParserToken token) {
        if (!(token instanceof Identifier)) return false;
        try {
            BackgroundAttachment.valueOf(((Identifier)token).getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private BackgroundAttachment attachment;

    /**
     * Initializes a new instance of BackgroundAttachmentDeclaration from the specified
     * declaration.
     *
     * @param source the declaration.
     */
    protected BackgroundAttachmentDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes a new instance of BackgroundAttachmentDeclaration with the specified attachment
     * token.
     *
     * @param attachment the attachment token.
     * @throws IllegalArgumentException if the specified token is not a valid background
     *                                  attachment.
     */
    protected BackgroundAttachmentDeclaration(final ParserToken attachment) {
        super("background-attachment");
        process(attachment);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    protected ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) try {
            attachment = BackgroundAttachment.valueOf(((Identifier)token).getEnumTag());
        } catch (IllegalArgumentException e) {
            throw new InvalidDeclarationTokenException(token);
        }
        else end(token);
        return this::end;
    }

    public BackgroundAttachment getAttachment() {
        return attachment != null ? attachment : getDefault();
    }

    @Override public void apply(final Style style) {
        style.setBackgroundAttachment(getAttachment());
    }
}
