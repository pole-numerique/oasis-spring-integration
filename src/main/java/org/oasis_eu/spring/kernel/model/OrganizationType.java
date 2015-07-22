package org.oasis_eu.spring.kernel.model;

/**
 * User: schambon
 * Date: 10/6/14
 */
public enum OrganizationType {
    PUBLIC_BODY,
    COMPANY;

    public static OrganizationType getOrganizationType(DCOrganizationType dcOrganizationType){
        if(dcOrganizationType.equals(DCOrganizationType.Public) ){ return OrganizationType.PUBLIC_BODY;
        }else if(dcOrganizationType.equals(DCOrganizationType.Private) ){ return OrganizationType.COMPANY;}
        return null;
    }

    /**
     * Infers OrganizationType from a string containing a OrganizationType or DCOrganizationType
     * @param String
     * @return organizationType
     */
    public static OrganizationType getOrganizationType(String xOrganizationType){
        if(xOrganizationType.equalsIgnoreCase(OrganizationType.PUBLIC_BODY.name()) || xOrganizationType.equalsIgnoreCase(DCOrganizationType.Public.name())){
            return OrganizationType.PUBLIC_BODY;
        }else if(xOrganizationType.equalsIgnoreCase(OrganizationType.COMPANY.name()) || xOrganizationType.equalsIgnoreCase(DCOrganizationType.Private.name())){
            return OrganizationType.COMPANY;
        }
        return null;
    }
}