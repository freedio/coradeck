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

import com.coradec.coraconf.model.ValueMap;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coradoc.ctrl.impl.BasicCssParser;
import com.coradec.coradoc.model.CascadingStyleSheet;
import com.coradec.coradoc.model.CssDocumentModel;
import com.coradec.coradoc.model.QualifiedRule;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.struct.AtRule;
import com.coradec.coradoc.struct.BasicQualifiedRule;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ​​Basic implementation of a CSS.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicCssStyleSheet extends BasicDocumentModel
        implements CascadingStyleSheet, CssDocumentModel {

    private static final Style DEFAULT_STYLE = new DefaultStyle();
    private final Set<AtRule> atRules;
    private final Set<QualifiedRule> qualifiedRules;

    public BasicCssStyleSheet(URL source) {
        final BasicCssParser<CssDocumentModel> parser = new BasicCssParser<>();
        atRules = new HashSet<>();
        qualifiedRules = new HashSet<>();
        parser.from(source).to(this).parse();
    }

    @Override public void onEndOfDocument() {

    }

    @Override public void onComment(final String comment) {
        // skip: we're not interested
    }

    @Override public void onAtRule(final AtRule atRule) {
        atRules.add(atRule);
    }

    @Override public void onQualifiedRule(final BasicQualifiedRule qualifiedRule) {
        qualifiedRules.add(qualifiedRule);
    }

    @Override public Style getStyle(final List<String> path, final ValueMap attributes) {
        return qualifiedRules.stream()
                             .filter(rule -> rule.matches(path, attributes))
                             .sorted((rule1, rule2) -> rule2.getSpecifity() - rule1.getSpecifity())
                             .findFirst()
                             .map(QualifiedRule::toStyle)
                             .orElse(DEFAULT_STYLE);
    }

}
