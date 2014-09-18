package org.oasis_eu.portal.front.store;

import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.oasis_eu.portal.services.PortalAppstoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: schambon
 * Date: 6/24/14
 */
@Controller
@RequestMapping("/appstore")
public class AppStoreController extends PortalController {

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired
    private PortalAppstoreService appstoreService;

    @Autowired
    private PortalAppManagementService appManagementService;

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation(null); // TODO point nav towards the appstore
    }

    @RequestMapping(method = RequestMethod.GET, value = {"", "/"})
    public String main(Model model) {
        model.addAttribute("hits", appstoreService.getAll(Arrays.asList(Audience.values()), null));

        return "store/appstore";
    }

    @RequestMapping(method = RequestMethod.POST, value="/search")
    public String search(Model model, @RequestParam String query, @RequestParam List<Audience> audience, @RequestParam List<String> installOptions) {
        model.addAttribute("hits", appstoreService.getAll(audience, installOptions));

        return "store/appstore::hits";
    }

    @RequestMapping(method = RequestMethod.POST, value="/buy")
    public String buy(@RequestParam String appId, @RequestParam CatalogEntryType appType, @RequestParam(required = false) String organizationId) {
        appstoreService.buy(appId, appType, organizationId);
        return "redirect:/my/dashboard";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/application/{appId}/{appType}")
    public String application(@PathVariable String appId, @PathVariable String appType, Model model) {
        applicationModel(appId, appType, model);

        return "store/application";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/application/{appId}/{appType}/inner")
    public String applicationInner(@PathVariable String appId, @PathVariable String appType, Model model) {
        applicationModel(appId, appType, model);
        return "store/application::content";
    }

    private void applicationModel(String appId, String appType, Model model) {
        model.addAttribute("app", appstoreService.getInfo(appId, CatalogEntryType.valueOf(appType)));
        model.addAttribute("authorities", appManagementService.getMyAuthorities(false));
    }

    @ExceptionHandler(ApplicationInstanceCreationException.class)
    public ModelAndView instantiationError(ApplicationInstanceCreationException e) {
        Map<String, Object> model = new HashMap<>();
        model.put("appname", e.getRequested().getName());
        model.put("appid", e.getApplicationId());
        model.put("errortype", e.getType().toString());

        model.put("navigation", myNavigationService.getNavigation(null));
        model.put("currentLanguage", currentLanguage());
        model.put("user", user());

        return new ModelAndView("store/instantiation-error", model);
    }
}