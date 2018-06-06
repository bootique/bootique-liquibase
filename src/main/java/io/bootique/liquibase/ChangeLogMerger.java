/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.liquibase;

import io.bootique.resource.ResourceFactory;

import java.util.Collection;
import java.util.Objects;

/**
 * A simple change log merger that uses "last wins" strategy, returning the last change log collection passed to the
 * method.
 *
 * @since 0.11
 */
public class ChangeLogMerger {

    public Collection<ResourceFactory> merge(Collection<ResourceFactory> logs1,
                                             Collection<ResourceFactory> logs2) {
        return logs2 != null ? logs2 : Objects.requireNonNull(logs1);
    }
}
