/*
 * Views to support an external dashboard application.
 * The views may require adaption to local settings (i.e.
 * due to hardwired identifiers).
 */

/*
 * Users by (primary) Department
 * list of group-ids could be replaced by a configuration table
 */
CREATE VIEW stat_users_by_dept AS
  SELECT u.id, u.alias, u.first_name, u.last_name, u.email, u.mutable,
    u.is_enabled, g.name
    FROM USERS AS u LEFT OUTER JOIN (SELECT m.user_id, MIN(m.group_id) AS group_id
    FROM group_memberships AS m
    WHERE m.group_id IN  ('101', '105', '108') GROUP BY m.user_id) AS dept
    ON u.id = dept.user_id LEFT OUTER JOIN groups AS g ON g.id = dept.group_id;

/*
 * Entities by creator, creation date and type
 */
CREATE VIEW stat_signalsentities_creat AS
 SELECT se.id, se.created_at, de.value AS snb_type,
   concat(uc.first_name, ' ', uc.last_name) AS created_by
   FROM signalsentities se
     JOIN dyn_enums de ON de.id = se.snb_type
     JOIN users uc ON se.created_by::text = uc.id::text;

