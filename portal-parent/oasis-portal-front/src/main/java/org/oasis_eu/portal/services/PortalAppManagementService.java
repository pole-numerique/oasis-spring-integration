package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.ApplicationInstanceStore;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.InstanceACLStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.appsmanagement.*;
import org.oasis_eu.portal.model.appstore.AppInfo;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 7/29/14
 */
@Service
public class PortalAppManagementService {

    private static final Logger logger = LoggerFactory.getLogger(PortalAppstoreService.class);

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private ApplicationInstanceStore applicationInstanceStore;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private InstanceACLStore instanceACLStore;

    @Autowired
    private ImageService imageService;

    @Autowired
    private NetworkService networkService;

    public List<MyAppsInstance> getMyInstances(Authority authority) {

        switch (authority.getType()) {
            case INDIVIDUAL:
                return getPersonalInstances(authority);
            case ORGANIZATION:
                return getOrganizationInstances(authority);
        }

        logger.error("Should never be here - authority is neither an individual or an organization: {}", authority.getType());
        return null;
    }


    private List<MyAppsInstance> getPersonalInstances(Authority personalAuthority) {
        return applicationInstanceStore.findByUserId(personalAuthority.getId())
                .stream()
                .filter(instance -> ! ApplicationInstance.InstantiationStatus.PENDING.equals(instance.getStatus()))
                .map(this::fetchInstance)
                .collect(Collectors.toList());
    }

    private List<MyAppsInstance> getOrganizationInstances(Authority orgAuthority) {
        return applicationInstanceStore.findByOrganizationId(orgAuthority.getId())
                .stream()
                .filter(instance -> !ApplicationInstance.InstantiationStatus.PENDING.equals(instance.getStatus()))
                .map(this::fetchInstance)
                .collect(Collectors.toList());

    }

    public List<MyAppsInstance> getPendingInstances() {
        return applicationInstanceStore.findByUserId(userInfoService.currentUser().getUserId())
                .stream()
                .filter(instance -> ApplicationInstance.InstantiationStatus.PENDING.equals(instance.getStatus()))
                .map(this::fetchInstance)
                .collect(Collectors.toList());
    }

    private MyAppsInstance fetchInstance(ApplicationInstance instance) {

        logger.debug("Fetching instance data for {}", instance);

        CatalogEntry entry = catalogStore.findApplication(instance.getApplicationId());
        AppInfo appInfo = new AppInfo(entry.getId(), entry.getName(RequestContextUtils.getLocale(request)), entry.getDescription(RequestContextUtils.getLocale(request)), null, entry.getType(), entry.getIcon(RequestContextUtils.getLocale(request)));


        return new MyAppsInstance()
                .setApplicationInstance(instance)
                .setApplication(appInfo)
                .setServices(catalogStore.findServicesOfInstance(instance.getInstanceId()).stream().map(this::fetchService).collect(Collectors.toList()));
    }

    private MyAppsService fetchService(CatalogEntry service) {

        logger.debug("Fetching service data for {}", service);

        return new MyAppsService().setService(service).setName(service.getName(RequestContextUtils.getLocale(request))).setIconUrl(imageService.getImageForURL(service.getDefaultIcon(), ImageFormat.PNG_64BY64, false));
    }

    public MyAppsInstance getInstance(String instanceId) {

        return fetchInstance(catalogStore.findApplicationInstance(instanceId));
    }

    public MyAppsService getService(String serviceId) {

        return fetchService(catalogStore.findService(serviceId));

    }

    public CatalogEntry updateService(String serviceId, CatalogEntry entry) {
        if (!networkService.userIsAdmin(catalogStore.findService(serviceId).getProviderId())) {
            throw new AccessDeniedException("Unauthorized");
        }
        return catalogStore.fetchAndUpdateService(serviceId, entry);
    }

    public List<User> getSubscribedUsersOfService(String serviceId) {

        return subscriptionStore.findByServiceId(serviceId)
                .stream()
                .map(s -> new User(s.getUserId(), s.getUserName(), false))
                .collect(Collectors.toList());


    }

    public List<User> getAllUsersOfServiceOrganization(String serviceId) {

        CatalogEntry service = catalogStore.findService(serviceId);
        if (service != null) {
            String instanceId = service.getInstanceId();
            return instanceACLStore.getACL(instanceId)
                    .stream()
                    .map(ace -> new User(ace.getUserId(), ace.getUserName(),false ))
                    .collect(Collectors.toList());
        } else {
            logger.error("Service {} does not exist", serviceId);
            return Collections.emptyList();
        }
    }


    public void updateSubscriptions(String serviceId, Set<String> usersToSubscribe) {
        if (!networkService.userIsAdmin(catalogStore.findService(serviceId).getProviderId())) {
            throw new AccessDeniedException("Unauthorized access");
        }


        Set<String> existing = getSubscribedUsersOfService(serviceId).stream().map(User::getUserid).collect(Collectors.toSet());

        // which ones must we add?
        subscribeUsers(usersToSubscribe.stream().filter(s -> ! existing.contains(s)).collect(Collectors.toSet()), serviceId);
        // which ones must we remove?
        unsubscribeUsers(existing.stream().filter(s -> ! usersToSubscribe.contains(s)).collect(Collectors.toSet()), serviceId);
    }

    public void subscribeUsers(Set<String> users, String serviceId) {
        users.forEach(u -> {
            Subscription s = new Subscription();
            s.setSubscriptionType(SubscriptionType.ORGANIZATION);
            s.setServiceId(serviceId);
            s.setUserId(u);

            subscriptionStore.create(u, s);
        });
    }

    public void unsubscribeUsers(Set<String> users, String serviceId) {
        users.forEach(u -> subscriptionStore.unsubscribe(u, serviceId, SubscriptionType.ORGANIZATION));
    }

    public List<User> getAppUsers(String instanceId) {
        return instanceACLStore.getACL(instanceId)
                .stream()
                .map(ace -> new User(ace.getUserId(), ace.getUserName(), false))
                .collect(Collectors.toList());
    }

    public void saveAppUsers(String instanceId, List<String> userIds) {
        if (!networkService.userIsAdmin(catalogStore.findApplicationInstance(instanceId).getProviderId())) {
            throw new AccessDeniedException("Unauthorized access");
        }

        instanceACLStore.saveACL(instanceId, userIds);
    }


    public void deleteInstance(String instanceId) {
        catalogStore.deleteInstance(instanceId);
    }

}