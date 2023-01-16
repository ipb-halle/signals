/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals.users;


/** 
 * SNB group reference
 */
public class GroupReference implements IGroup {

    private String id;

    public String dump() {
        return String.format("GroupReference(%s)\n", id);
    }

    public String getId() {
        return id;
    }

    public IGroup setId(String st) {
        id = st;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof IGroup) {
            IGroup igroup = (IGroup) obj;
            if (id == igroup.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }
}

