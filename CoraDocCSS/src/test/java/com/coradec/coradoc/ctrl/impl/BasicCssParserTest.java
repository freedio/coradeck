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

package com.coradec.coradoc.ctrl.impl;

import static com.coradec.coradoc.cssenum.CssUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coracore.trouble.ResourceNotFoundException;
import com.coradec.coradoc.cssdecl.BackgroundDeclaration;
import com.coradec.coradoc.cssdecl.BorderDeclaration;
import com.coradec.coradoc.cssdecl.ColorDeclaration;
import com.coradec.coradoc.cssdecl.FontDeclaration;
import com.coradec.coradoc.cssdecl.PaddingDeclaration;
import com.coradec.coradoc.cssenum.BackgroundAttachment;
import com.coradec.coradoc.cssenum.BackgroundClip;
import com.coradec.coradoc.cssenum.BackgroundOrigin;
import com.coradec.coradoc.cssenum.BackgroundRepeat;
import com.coradec.coradoc.cssenum.BackgroundSize;
import com.coradec.coradoc.cssenum.ColorCode;
import com.coradec.coradoc.cssenum.FontSize;
import com.coradec.coradoc.cssenum.FontStyle;
import com.coradec.coradoc.cssenum.FontVariant;
import com.coradec.coradoc.cssenum.FontWeight;
import com.coradec.coradoc.cssenum.LineHeight;
import com.coradec.coradoc.model.CssDocumentModel;
import com.coradec.coradoc.model.CssPosition;
import com.coradec.coradoc.model.QualifiedRule;
import com.coradec.coradoc.model.Rule;
import com.coradec.coradoc.model.impl.BasicCssPosition;
import com.coradec.coradoc.struct.AtRule;
import com.coradec.coradoc.struct.BasicQualifiedRule;
import com.coradec.coradoc.struct.ChildSelector;
import com.coradec.coradoc.struct.DescendantSelector;
import com.coradec.coradoc.struct.NeighborSelector;
import com.coradec.coradoc.struct.SiblingSelector;
import com.coradec.coradoc.struct.UniversalSelector;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.IntegerPercentage;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicCssParserTest {

    @Test public void testCorrectCSS() throws IOException {
        final String resource = getClass().getSimpleName() + "_testCorrectCSS.css";
        final URL source = getClass().getClassLoader().getResource(resource);
        if (source == null) throw new ResourceNotFoundException(resource);
        final TestModel model = new TestModel();
        final BasicCssParser<CssDocumentModel> testee = new BasicCssParser<>();
        testee.from(source).to(model).parse();

        assertThat(model.getDocument(), is(equalTo(new URLigin(source))));
        assertThat(model.getRules().size(), is(2));

        Rule rule = model.getRules().get(0);
        assertThat(rule, is(instanceOf(BasicQualifiedRule.class)));
        QualifiedRule qrule = (BasicQualifiedRule)rule;
        assertThat(qrule.getSelectors().size(), is(1));
        assertThat(qrule.getSelector(0), is(instanceOf(UniversalSelector.class)));

        assertThat(qrule.getStyleDeclarations().size(), is(3));
        assertThat(qrule.getStyleDeclaration("font"), is(instanceOf(FontDeclaration.class)));
        assertThat(qrule.getStyleDeclaration("background"),
                is(instanceOf(BackgroundDeclaration.class)));
        assertThat(qrule.getStyleDeclaration("color"), is(instanceOf(ColorDeclaration.class)));

        final FontDeclaration font = (FontDeclaration)qrule.getStyleDeclaration("font");
        assertThat(font.getStyle(), is(FontStyle.ITALIC));
        assertThat(font.getVariant(), is(FontVariant.NORMAL));
        assertThat(font.getWeight(), is(FontWeight.NORMAL));
        assertThat(font.getSize(), is(FontSize.EXPLICIT));
        assertThat(font.getSizeValue(), is(equalTo(new IntegerDimension("36", 36, pt))));
        assertThat(font.getLineHeight(), is(LineHeight.EXPLICIT));
        assertThat(font.getLineHeightValue(), is(equalTo(new IntegerDimension("40", 40, pt))));
        assertThat(font.getFamily(), is(not(nullValue())));
        final List<String> family = font.getFamily();
        assertThat(family.size(), is(2));
        assertThat(family.contains("Arial"), is(true));
        assertThat(family.contains("sans-serif"), is(true));

        BackgroundDeclaration background =
                (BackgroundDeclaration)qrule.getStyleDeclaration("background");
        assertThat((long)background.getColor().getRGB(), is(ColorCode.MIDNIGHTBLUE.getValue()));
        assertThat(background.getImage(), is(not(nullValue())));
        assertThat(background.getImage().isEmpty(), is(true));
        assertThat(background.getPosition(), is(instanceOf(CssPosition.class)));
        final CssPosition position = background.getPosition();
        assertThat(position.getX(), is(equalTo(new IntegerPercentage("100", 100))));
        assertThat(position.getY(), is(equalTo(new IntegerPercentage("100", 100))));
        assertThat(background.getSize(), is(equalTo(BackgroundSize.AUTO)));
        assertThat(background.getHeight(), is(nullValue()));
        assertThat(background.getWidth(), is(nullValue()));
        assertThat(background.getRepeat(), is(BackgroundRepeat.REPEAT));
        assertThat(background.getOrigin(), is(BackgroundOrigin.PADDING_BOX));
        assertThat(background.getClip(), is(BackgroundClip.BORDER_BOX));
        assertThat(background.getAttachment(), is(BackgroundAttachment.SCROLL));

        final ColorDeclaration color = (ColorDeclaration)qrule.getStyleDeclaration("color");
        assertThat((long)color.getColor().getRGB(), is(ColorCode.GOLD.getValue()));

        rule = model.getRules().get(1);
        assertThat(rule, is(instanceOf(BasicQualifiedRule.class)));
        qrule = (BasicQualifiedRule)rule;
        assertThat(qrule.getSelectors().size(), is(4));
        assertThat(qrule.getSelector(0), is(instanceOf(DescendantSelector.class)));
        assertThat(qrule.getSelector(1), is(instanceOf(ChildSelector.class)));
        assertThat(qrule.getSelector(2), is(instanceOf(NeighborSelector.class)));
        assertThat(qrule.getSelector(3), is(instanceOf(SiblingSelector.class)));

        assertThat(qrule.getStyleDeclarations().size(), is(3));
        assertThat(qrule.getStyleDeclaration("background"),
                is(instanceOf(BackgroundDeclaration.class)));
        assertThat(qrule.getStyleDeclaration("padding"), is(instanceOf(PaddingDeclaration.class)));
        assertThat(qrule.getStyleDeclaration("border"), is(instanceOf(BorderDeclaration.class)));

        background = (BackgroundDeclaration)qrule.getStyleDeclaration("background");
        assertThat((long)background.getColor().getRGB(), is(0x66FAFA80L));
        assertThat(background.getImage(), is(not(nullValue())));
        final List<URL> images = background.getImage();
        assertThat(images.size(), is(1));
        final URL flowers = getClass().getClassLoader().getResource("flowers.png");
        assertThat(images.get(0), is(equalTo(flowers)));
        assertThat(background.getRepeat(), is(BackgroundRepeat.NO_REPEAT));
        assertThat(background.getAttachment(), is(BackgroundAttachment.FIXED));
        assertThat(background.getPosition(), is(equalTo(
                new BasicCssPosition(new IntegerPercentage("50", 50),
                        new IntegerPercentage("50", 50)))));
    }

    private class TestModel implements CssDocumentModel {

        private Origin document;
        private final List<Rule> rules = new ArrayList<>();

        List<Rule> getRules() {
            return rules;
        }

        @Override public void onStartOfDocument(final Origin document) {
            this.document = document;
        }

        @Override public void onEndOfDocument() {

        }

        @Override public void onComment(final String comment) {
            // ignore
        }

        @Override public Origin getDocument() {
            return document;
        }

        @Override public void onAtRule(final AtRule atRule) {
            rules.add(atRule);
        }

        @Override public void onQualifiedRule(final BasicQualifiedRule qualifiedRule) {
            rules.add(qualifiedRule);
        }

    }

}
