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

package com.khartec.waltz.model.person;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePerson.class)
@JsonDeserialize(as = ImmutablePerson.class)
public abstract class Person implements IdProvider {

    public abstract String employeeId();
    public abstract String displayName();
    public abstract String email();
    public abstract PersonKind kind();
    public abstract Optional<String> title();
    public abstract Optional<String> mobilePhone();
    public abstract Optional<String> officePhone();
    public abstract Optional<String> userPrincipalName();
    public abstract Optional<String> managerEmployeeId();
    public abstract Optional<String> departmentName();
    public abstract Optional<Long> organisationalUnitId();

    @Value.Default
    public String userId() { //TODO change as part of 247
        return email();
    }
}
