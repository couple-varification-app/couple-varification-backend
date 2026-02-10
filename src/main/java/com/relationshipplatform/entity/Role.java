package com.relationshipplatform.entity;

/**
 * User roles for authorization
 * 
 * ROLE_USER: Regular user (can create couples, promises, etc.)
 * ROLE_ADMIN: Administrator (can view all data, manage users)
 * ROLE_MODERATOR: Moderator (can handle reports, conflicts)
 */
public enum Role {
    /**
     * Regular user - default role
     * Permissions:
     * - Create/manage their own profile
     * - Create couple requests
     * - Manage their relationship data
     * - View their own data only
     */
    ROLE_USER,

    /**
     * Administrator
     * Permissions:
     * - All USER permissions
     * - View all users
     * - View all couples
     * - Deactivate users
     * - View platform statistics
     * - Manage system settings
     */
    ROLE_ADMIN,

    /**
     * Moderator
     * Permissions:
     * - All USER permissions
     * - Handle conflict reports
     * - Review flagged content
     * - Assist with verification
     */
    ROLE_MODERATOR
}