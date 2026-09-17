package com.activitytracking.user.constants;

/**
 * Permission name literals, kept in one place instead of scattered as magic strings across
 * every @PreAuthorize annotation. These must exactly match the `permissions.name` values
 * seeded in V6__create_roles_and_permissions_tables.sql.
 */
public final class PermissionNames {

    public static final String USER_READ = "USER_READ";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String MASTERDATA_MANAGE = "MASTERDATA_MANAGE";
    public static final String ACTIVITY_VIEW_TEAM = "ACTIVITY_VIEW_TEAM";

    private PermissionNames() {
    }
}
