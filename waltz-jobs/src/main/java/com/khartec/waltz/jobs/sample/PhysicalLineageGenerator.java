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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.schema.tables.records.PhysicalFlowLineageRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.data.physical_flow.PhysicalFlowDao.targetEntityNameField;
import static com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao.owningEntityNameField;
import static com.khartec.waltz.schema.Tables.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;

public class PhysicalLineageGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<PhysicalFlow> physicalFlows = dsl.select(PHYSICAL_FLOW.fields())
                .select(targetEntityNameField)
                .select(APPLICATION.NAME)
                .from(PHYSICAL_FLOW)
                .join(APPLICATION).on(PHYSICAL_FLOW.TARGET_ENTITY_ID.eq(APPLICATION.ID))
                .fetch(PhysicalFlowDao.TO_DOMAIN_MAPPER);

        List<PhysicalSpecification> specifications = dsl.select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .fetch(PhysicalSpecificationDao.TO_DOMAIN_MAPPER);

        Map<Long, PhysicalSpecification> specsById = indexBy(s -> s.id().get(), s -> s, specifications);
        Map<Long, Collection<PhysicalFlow>> flowsByTarget = groupBy(f -> f.target().id(), f -> f, physicalFlows);


        List<PhysicalFlowLineageRecord> records = physicalFlows.stream()
                .flatMap(f -> {
                    final PhysicalFlow[] workingFlow = {f};
                    return IntStream.range(0, rnd.nextInt(6))
                            .mapToObj(i -> {
                                // get contributor flow
                                PhysicalSpecification spec = specsById.get(workingFlow[0].specificationId());
                                long sourceApp = spec.owningEntity().id();

                                Collection<PhysicalFlow> contributingFlows = flowsByTarget.get(sourceApp);
                                if (contributingFlows != null && contributingFlows.size() > 0) {
                                    PhysicalFlow contributor = ListUtilities.randomPick(new ArrayList<>(contributingFlows));
                                    workingFlow[0] = contributor;

                                    PhysicalFlowLineageRecord record = new PhysicalFlowLineageRecord();
                                    record.setContributorFlowId(contributor.id().get());
                                    record.setDescribedFlowId(f.id().get());
                                    record.setDescription(String.format("%s described by %s", f, contributor));
                                    record.setLastUpdatedBy("admin");
                                    return Optional.of(record);
                                } else {
                                    return Optional.<PhysicalFlowLineageRecord>empty();
                                }
                            });
                })
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .filter(r -> !r.getDescribedFlowId().equals(r.getContributorFlowId()))
                .distinct()
                .collect(Collectors.toList());


        System.out.println("---removing demo records");
        dsl.deleteFrom(PHYSICAL_FLOW_LINEAGE)
                .execute();

        System.out.println("---saving record: "+records.size());
        dsl.batchInsert(records).execute();

        System.out.println("---done");


    }
}
