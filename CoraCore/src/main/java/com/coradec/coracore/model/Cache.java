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

package com.coradec.coracore.model;

import java.util.Map;

/**
 * ​A map whose entries are reclaimed by the garbage collector when their keys are no longer
 * referenced.  In contrast to WeakHashHap whose entries are reclaimed on each garbage collection,
 * the entries of Cache implementations should only be reclaimed upon memory saturation (before an
 * OutOfMemoryException is thrown).
 * <p>
 * The rationale for cache implementations is to make the entries available as long as possible,
 * without holding on to them when a rally for memory occurs.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public interface Cache<K, V> extends Map<K, V> {

}
