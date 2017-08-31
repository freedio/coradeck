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

package com.coradec.coratype.ctrl.impl;

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URIgin;
import com.coradec.coracore.time.Duration;
import com.coradec.coractrl.time.impl.BasicDuration;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.coratype.ctrl.TypeConverter;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicTypeConverterTest {

    @Test public void testClassDecodeLookup() {
        final TypeConverter<Origin> origin = TypeConverter.to(Origin.class);
        assertThat(origin.decode("/abc/def"), is(equalTo(new URIgin(URI.create("/abc/def")))));
        final TypeConverter<Duration> duration = TypeConverter.to(Duration.class);
        assertThat(duration.decode("2s"), is(equalTo(new BasicDuration(2, SECONDS))));
    }

    @Test public void testGenericClassDecodeLookup() {
        final TypeConverter<Origin> origin = TypeConverter.to(GenericType.of(Origin.class));
        assertThat(origin.decode("/abc/def"), is(equalTo(new URIgin(URI.create("/abc/def")))));
        final TypeConverter<Duration> duration = TypeConverter.to(GenericType.of(Duration.class));
        assertThat(duration.decode("2s"), is(equalTo(new BasicDuration(2, SECONDS))));
    }

}
