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

package com.khartec.waltz.data.data_flow_decorator;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDecoratorRatingSummary;
import com.khartec.waltz.schema.tables.records.DataFlowDecoratorRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.sourceAppAlias;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.targetAppAlias;
import static com.khartec.waltz.schema.tables.DataFlowDecorator.DATA_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.stream.Collectors.toList;


@Repository
public class DataFlowDecoratorDao {

    private static final RecordMapper<Record, DataFlowDecorator> TO_DECORATOR_MAPPER = r -> {
        DataFlowDecoratorRecord record = r.into(DATA_FLOW_DECORATOR);

        return ImmutableDataFlowDecorator.builder()
                .dataFlowId(record.getDataFlowId())
                .decoratorEntity(ImmutableEntityReference.builder()
                        .id(record.getDecoratorEntityId())
                        .kind(EntityKind.valueOf(record.getDecoratorEntityKind()))
                        .build())
                .rating(AuthoritativenessRating.valueOf(record.getRating()))
                .provenance(record.getProvenance())
                .build();
    };

    private static final Function<DataFlowDecorator, DataFlowDecoratorRecord> TO_RECORD = d -> {
        DataFlowDecoratorRecord r = new DataFlowDecoratorRecord();
        r.setDecoratorEntityKind(d.decoratorEntity().kind().name());
        r.setDecoratorEntityId(d.decoratorEntity().id());
        r.setDataFlowId(d.dataFlowId());
        r.setProvenance(d.provenance());
        r.setRating(d.rating().name());
        return r;
    };

    private final DSLContext dsl;


    @Autowired
    public DataFlowDecoratorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    // --- FINDERS ---

    public List<DataFlowDecorator> findByAppIdSelectorAndKind(Select<Record1<Long>> appIdSelector,
                                                              EntityKind decoratorEntityKind) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        return dsl
                .select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(DATA_FLOW_DECORATOR.DATA_FLOW_ID)) // join on application to prevent orphan flows
                .innerJoin(sourceAppAlias)                             // being returned
                .on(sourceAppAlias.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID))
                .innerJoin(targetAppAlias)
                .on(targetAppAlias.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID))
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appIdSelector)
                                .or(LOGICAL_FLOW.TARGET_ENTITY_ID.in(appIdSelector)))
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorEntityKind.name()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .fetch(TO_DECORATOR_MAPPER);
    }


    public List<DataFlowDecorator> findByDecoratorEntityIdSelectorAndKind(Select<Record1<Long>> decoratorEntityIdSelector,
                                                                          EntityKind decoratorKind) {
        checkNotNull(decoratorEntityIdSelector, "decoratorEntityIdSelector cannot be null");
        checkNotNull(decoratorKind, "decoratorKind cannot be null");

        Condition condition = DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorKind.name())
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(decoratorEntityIdSelector));

        return dsl
                .select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }


    public List<DataFlowDecorator> findByFlowIds(Collection<Long> flowIds) {
        checkNotNull(flowIds, "flowIds cannot be null");

        Condition condition = DATA_FLOW_DECORATOR.DATA_FLOW_ID.in(flowIds);

        return findByCondition(condition);
    }


    public List<DataFlowDecorator> findByFlowIdsAndKind(Collection<Long> flowIds,
                                                        EntityKind decoratorEntityKind) {
        checkNotNull(flowIds, "flowIds cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        Condition condition = DATA_FLOW_DECORATOR.DATA_FLOW_ID.in(flowIds)
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorEntityKind.name()));

        return findByCondition(condition);
    }


    public Collection<DataFlowDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        Condition condition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .or(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appIdSelector));

        return dsl.select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(DATA_FLOW_DECORATOR.DATA_FLOW_ID))
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }


    // --- STATS ---

    public List<DecoratorRatingSummary> summarizeForSelector(Select<Record1<Long>> selector) {
        // this is intentionally TARGET only as we use to calculate auth source stats
        Condition condition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(selector);

        Condition dataFlowJoinCondition = LOGICAL_FLOW.ID.eq(DATA_FLOW_DECORATOR.DATA_FLOW_ID);

        Collection<Field<?>> groupingFields = newArrayList(
                DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND,
                DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID,
                DATA_FLOW_DECORATOR.RATING);

        Field<Integer> countField = DSL.count(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID).as("count");

        return dsl.select(groupingFields)
                .select(countField)
                .from(DATA_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(dsl.renderInlined(dataFlowJoinCondition))
                .where(dsl.renderInlined(condition))
                .groupBy(groupingFields)
                .fetch(r -> {
                    EntityKind decoratorEntityKind = EntityKind.valueOf(r.getValue(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND));
                    long decoratorEntityId = r.getValue(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID);

                    EntityReference decoratorRef = EntityReference.mkRef(decoratorEntityKind, decoratorEntityId);
                    AuthoritativenessRating rating = AuthoritativenessRating.valueOf(r.getValue(DATA_FLOW_DECORATOR.RATING));
                    Integer count = r.getValue(countField);

                    return ImmutableDecoratorRatingSummary.builder()
                            .decoratorEntityReference(decoratorRef)
                            .rating(rating)
                            .count(count)
                            .build();
                });
    }


    // --- UPDATERS ---

    public int[] deleteDecorators(Long flowId, Collection<EntityReference> decoratorReferences) {
        List<DataFlowDecoratorRecord> records = decoratorReferences
                .stream()
                .map(ref -> {
                    DataFlowDecoratorRecord record = dsl.newRecord(DATA_FLOW_DECORATOR);
                    record.setDataFlowId(flowId);
                    record.setDecoratorEntityId(ref.id());
                    record.setDecoratorEntityKind(ref.kind().name());
                    return record;
                })
                .collect(toList());
        return dsl
                .batchDelete(records)
                .execute();
    }


    @Deprecated
    // Replace with a method that removes decorators for a single flow
    public int removeAllDecoratorsForFlowIds(List<Long> flowIds) {
        return dsl.deleteFrom(DATA_FLOW_DECORATOR)
                .where(DATA_FLOW_DECORATOR.DATA_FLOW_ID.in(flowIds))
                .execute();
    }


    public int[] addDecorators(Collection<DataFlowDecorator> decorators) {
        checkNotNull(decorators, "decorators cannot be null");

        List<DataFlowDecoratorRecord> records = decorators.stream()
                .map(TO_RECORD)
                .collect(toList());

        return dsl.batchInsert(records).execute();
    }


    public int[] updateDecorators(Set<DataFlowDecorator> decorators) {
        Set<DataFlowDecoratorRecord> records = SetUtilities.map(decorators, TO_RECORD);
        return dsl.batchUpdate(records).execute();
    }


    // --- HELPERS ---

    private List<DataFlowDecorator> findByCondition(Condition condition) {
        return dsl
                .select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }

}
