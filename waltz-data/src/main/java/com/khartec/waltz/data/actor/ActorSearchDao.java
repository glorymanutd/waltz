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

package com.khartec.waltz.data.actor;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.PredicateUtilities.all;
import static com.khartec.waltz.common.StringUtilities.length;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;


@Repository
public class ActorSearchDao {

    private final DSLContext dsl;
    private final ActorDao actorDao;


    @Autowired
    public ActorSearchDao(DSLContext dsl, ActorDao actorDao) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(actorDao, "actorDao cannot be null");

        this.dsl = dsl;
        this.actorDao = actorDao;
    }


    public List<EntityReference> search(String query) {
        if (length(query) < 3) {
            return emptyList();
        }

        List<String> terms = StringUtilities.mkTerms(query);
        return actorDao.findAll()
                .stream()
                .filter(actor -> {
                    String s = (actor.name() + " " + actor.description()).toLowerCase();
                    return all(
                            terms,
                            t -> s.indexOf(t) > -1);
                })
                .map(actor -> EntityReference.mkRef(EntityKind.ACTOR, actor.id().get(), actor.name(), actor.description()))
                .collect(toList());
    }
}
