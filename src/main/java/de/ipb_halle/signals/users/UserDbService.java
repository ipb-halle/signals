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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
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
        criteriaQuery.where(builder.equal(root
                .get(UserRole.USER_ROLE_ID)
                .get(UserRoleId.USER_ID), 
                user.getId()));

        Set<IRole> result = new HashSet<> ();
        for (UserRole userRole: em.createQuery(criteriaQuery).getResultList()) {
            Role role = this.em.find(Role.class, userRole.getRoleId());
            if (role != null) {
                result.add(role);
            }
        }
        return result;
    }

    private Set<IGroup> loadSystemGroups(IUser user) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GroupMembership> criteriaQuery = builder.createQuery(GroupMembership.class);
        Root<GroupMembership> root = criteriaQuery.from(GroupMembership.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root
                .get(GroupMembership.GROUP_MEMBERSHIP_ID)
                .get(GroupMembershipId.USER_ID), 
                user.getId()));

        Set<IGroup> result = new HashSet<> ();
        for (GroupMembership membership: em.createQuery(criteriaQuery).getResultList()) {
            Group group = this.em.find(Group.class, membership.getGroupId());
            if (group != null) {
                result.add(group);
            }
        }
        return result;
    }


    /**
     * @return a list of UserEntities
     */
    public List<User> load() {
        return loadBy(new HashMap<String, Object> ());
    }

    public User loadByUserName(String userName) {
        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(User.USER_USERNAME, userName);
        List<User> result = loadBy(cmap);
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    public List<User> loadBy(Map<String, Object> cmap) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UserEntity> criteriaQuery = builder.createQuery(UserEntity.class);
        Root<UserEntity> root = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (cmap.get(User.USER_USERNAME) != null) {
            predicates.add(builder.equal(root.get(User.USER_USERNAME), cmap.get(User.USER_USERNAME)));
        }
        if (cmap.get(User.USER_MUTABLE) != null) {
            predicates.add(builder.equal(root.get(User.USER_MUTABLE), cmap.get(User.USER_MUTABLE)));
        }
        if (cmap.get(User.USER_ENABLED) != null) {
            predicates.add(builder.equal(root.get(User.USER_ENABLED), cmap.get(User.USER_ENABLED)));
        }

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));

        List<User> result = new ArrayList<> ();
        for (UserEntity entity: em.createQuery(criteriaQuery).getResultList()) {
            User user = new User(entity);
            user.setRoles(loadRoles(user));
            user.setSystemGroups(loadSystemGroups(user));
            result.add(user);
        }
        return result;
    }

    public Map<String, User> loadMappedById(Map<String, Object> cmap) {
        Map<String, User> resultMap = new HashMap<> ();
        for (User user: loadBy(cmap)) {
            resultMap.put(user.getId(), user);
        }
        return resultMap;
    }

    public User loadById(String id) {
        UserEntity ue = this.em.find(UserEntity.class, id);
        if (ue != null) {
            User user = new User(ue);
            user.setRoles(loadRoles(user));
            user.setSystemGroups(loadSystemGroups(user));
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
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaDelete<UserRole> criteriaDelete = builder.createCriteriaDelete(UserRole.class);
        Root<UserRole> root = criteriaDelete.from(UserRole.class);

        criteriaDelete.where(builder.equal(root
                .get(UserRole.USER_ROLE_ID)
                .get(UserRoleId.USER_ID),
                u.getId()));
        this.em.createQuery(criteriaDelete).executeUpdate();

        for(IRole r: u.getRoles()) {
            this.em.merge(new UserRole(r, u));
        }
    }

    private void saveSystemGroups(User u)  {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaDelete<GroupMembership> criteriaDelete = builder.createCriteriaDelete(GroupMembership.class);
        Root<GroupMembership> root = criteriaDelete.from(GroupMembership.class);

        criteriaDelete.where(builder.equal(root
                .get(GroupMembership.GROUP_MEMBERSHIP_ID)
                .get(GroupMembershipId.USER_ID),
                u.getId()));
        this.em.createQuery(criteriaDelete).executeUpdate();

        for(IGroup g: u.getSystemGroups()) {
            this.em.merge(new GroupMembership(g, u));
        }
    }
}

