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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/**
 * Database Service for users
 */

@Stateless
public class UserDbService {

    @PersistenceContext(unitName="signalsDB")
    private EntityManager em;

    private Set<IRole> loadRoles(IUser user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UserRole> criteriaQuery = builder.createQuery(UserRole.class);
        Root<UserRole> root = criteriaQuery.from(UserRole.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(UserRoleId.USER_ID), user.getId()));

        Set<IRole> result = new HashSet<> ();
        for (UserRole userRole: em.createQuery(criteriaQuery).getResultList()) {
            result.add(new RoleReference().setId(userRole.getRoleId()));
        }
        return result;
    }

    private Set<IGroup> loadSystemGroups(IUser user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GroupMembership> criteriaQuery = builder.createQuery(GroupMembership.class);
        Root<GroupMembership> root = criteriaQuery.from(GroupMembership.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get(GroupMembershipId.USER_ID), user.getId()));

        Set<IGroup> result = new HashSet<> ();
        for (GroupMembership membership: em.createQuery(criteriaQuery).getResultList()) {
            result.add(new GroupReference().setId(membership.getGroupId()));
        }
        return result;
    }


    /**
     * @return a list of UserEntities
     */
    public List<User> load() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UserEntity> criteriaQuery = builder.createQuery(UserEntity.class);
        Root<UserEntity> root = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(root);

        List<User> result = new ArrayList<> ();
        for (UserEntity entity: em.createQuery(criteriaQuery).getResultList()) {
            User user = new User(entity);
            user.setRoles(loadRoles(user));
            user.setSystemGroups(loadSystemGroups(user));
            result.add(user);
        }
        return result;
    }


    public User loadById(int id) {
        UserEntity ue = this.em.find(UserEntity.class, id);
        if (ue != null) {
            User user = new User(ue);
            // load roles
            // load group memberships
            return user;
        }
        return null;
    }

    public void save(User u) {
        this.em.merge(u.createEntity());
        saveRoles(u);
        saveSystemGroups(u);
    }

    private void saveRoles(User u) {
        for(IRole r: u.getRoles()) {
            this.em.merge(new UserRole(r, u));
        }
    }

    private void saveSystemGroups(User u)  {
        for(IGroup g: u.getSystemGroups()) {
            this.em.merge(new GroupMembership(g, u));
        }
    }
}

