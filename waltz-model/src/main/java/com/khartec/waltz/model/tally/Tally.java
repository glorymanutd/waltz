/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.tally;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Simple class used for representing a simple count of a given id.
 *
 * Typically used for returning the results of a query similar to:
 * <pre>SELECT id, COUNT(something) FROM somewhere GROUP BY something</pre>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTally.class)
@JsonDeserialize(as = ImmutableTally.class)
public abstract class Tally<T> {

    public abstract T id();
    public abstract double count();

}
