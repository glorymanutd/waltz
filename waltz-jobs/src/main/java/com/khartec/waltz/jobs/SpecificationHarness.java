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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class SpecificationHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        PhysicalSpecificationDao physicalSpecificationDao = ctx.getBean(PhysicalSpecificationDao.class);

        ProduceConsumeGroup<PhysicalSpecification> specifications = physicalSpecificationDao.findByEntityReference(EntityReference.mkRef(EntityKind.APPLICATION, 12));
        System.out.println(specifications);
    }

}
