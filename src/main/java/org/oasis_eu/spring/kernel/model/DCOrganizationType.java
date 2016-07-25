package org.oasis_eu.spring.kernel.model;

public enum DCOrganizationType {
    Public,
    Private;

    /**
     * Infers DCOrganizationType from a String containing a OrganizationType or DCOrganizationType
     */
    public static DCOrganizationType getDCOrganizationType(String xOrganizationType) {
        if (xOrganizationType.equalsIgnoreCase(DCOrganizationType.Public.name())
            || xOrganizationType.equalsIgnoreCase(OrganizationType.PUBLIC_BODY.name())) {
            return DCOrganizationType.Public;
        } else if (xOrganizationType.equalsIgnoreCase(DCOrganizationType.Private.name())
            || xOrganizationType.equalsIgnoreCase(OrganizationType.COMPANY.name())) {
            return DCOrganizationType.Private;
        }

        return null;
    }
}
