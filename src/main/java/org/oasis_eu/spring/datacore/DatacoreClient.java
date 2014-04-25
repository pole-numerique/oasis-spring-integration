package org.oasis_eu.spring.datacore;

import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.datacore.model.DCResult;
import org.oasis_eu.spring.datacore.model.DCRights;

import java.util.List;

/**
 * User: schambon
 * Date: 1/2/14
 */
public interface DatacoreClient {

    List<DCResource> findResources(String type);

    List<DCResource> findResources(String type, DCQueryParameters queryParameter, int start, int maxResult);

    DCResult getResource(String type, String iri);

    DCResult saveResource(DCResource resource);

    DCResult updateResource(DCResource resource);

    DCResult deleteResource(DCResource resource);

    DCResult addRightsOnResource(DCResource resource, DCRights rights);

    DCResult setRightsOnResource(DCResource resource, DCRights rights);

}
