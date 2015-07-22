package org.oasis_eu.spring.kernel.model;

public enum DCOrganizationType {
    Public,
    Private;

    public static DCOrganizationType getDCOrganizationType(OrganizationType organizationType){
        if(organizationType == OrganizationType.PUBLIC_BODY){ return DCOrganizationType.Public;
        }else if(organizationType == OrganizationType.COMPANY){ return DCOrganizationType.Private;}
        return null;
    }

    /**
     * Infers DCOrganizationType from a String containing a OrganizationType or DCOrganizationType
     * @param String
     * @return DCOrganizationType
     */
    public static DCOrganizationType getDCOrganizationType(String xOrganizationType){
        if(xOrganizationType.equalsIgnoreCase(DCOrganizationType.Public.name()) || xOrganizationType.equalsIgnoreCase(OrganizationType.PUBLIC_BODY.name())){ 
            return DCOrganizationType.Public;
        }else if(xOrganizationType.equalsIgnoreCase(DCOrganizationType.Private.name()) || xOrganizationType.equalsIgnoreCase(OrganizationType.COMPANY.name())){
            return DCOrganizationType.Private;}
        return null;
    }
}
