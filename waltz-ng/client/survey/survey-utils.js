
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

import _ from 'lodash';


export function groupQuestions(questions = []) {
    const sections = _.chain(questions)
        .map(q => q.sectionName || "Other")
        .uniq()
        .value();

    const groupedQuestions = _.groupBy(questions, q => q.sectionName || "Other");

    return _.map(sections, s => {
        return {
            'sectionName': s,
            'questions': groupedQuestions[s]
        }
    });
}