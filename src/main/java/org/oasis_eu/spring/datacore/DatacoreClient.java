package org.oasis_eu.spring.datacore;

import org.oasis_eu.spring.datacore.model.*;

import java.util.List;

/**
 * User: schambon
 * Date: 1/2/14
 */
public interface DatacoreClient {

    List<DCModel> findModels(int limit);

    DCModel findModel(String type);

    List<DCResource> findResources(String project, String type);

    List<DCResource> findResources(String project, String type, DCQueryParameters queryParameter, int start, int maxResult);

    DCResult getResource(String project, String type, String iri);

    DCResult saveResource(String project, DCResource resource);

    DCResult updateResource(String project, DCResource resource);

    DCResult deleteResource(String project, DCResource resource);

    DCResult addRightsOnResource(String project, DCResource resource, DCRights rights);

    DCResult getRightsOnResource(String project, DCResource resource);

    DCResult setRightsOnResource(String project, DCResource resource, DCRights rights);

    DCResult getResourceFromURI(String project, String url);

    List<String> getResourceAliases(String project, String type, String iri);
}
