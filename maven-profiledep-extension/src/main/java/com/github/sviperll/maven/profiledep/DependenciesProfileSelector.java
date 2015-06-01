/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import com.github.sviperll.maven.profiledep.resolution.DependencyResolver;
import com.github.sviperll.maven.profiledep.resolution.ResolutionValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;

/**
 *
 * @author vir
 */
public class DependenciesProfileSelector implements ProfileSelector {
    private static final Logger logger = Logger.getLogger(DependenciesProfileSelector.class.getName());
    private final ProfileSelector defaultProfileSelector;

    DependenciesProfileSelector(ProfileSelector defaultProfileSelector) {
        this.defaultProfileSelector = defaultProfileSelector;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        DependencyResolver resolver = DependencyResolver.createInstance(availableProfiles);
        resolver.declareForbidden(context.getInactiveProfileIds());

        List<String> requiredProfileIDs = new ArrayList<String>();
        requiredProfileIDs.addAll(context.getActiveProfileIds());
        requiredProfileIDs.removeAll(context.getInactiveProfileIds());
        resolver.declareUnresolved(requiredProfileIDs);

        List<Profile> activatedProfiles = defaultProfileSelector.getActiveProfiles(availableProfiles, context, problems);
        try {
            resolver.activate(activatedProfiles);
            resolver.resolve();
        } catch (ResolutionValidationException ex) {
            ModelProblemCollectorRequest request = new ModelProblemCollectorRequest(ModelProblem.Severity.FATAL, ModelProblem.Version.BASE);
            request.setMessage("\n" + ex.renderResolutionTree());
            problems.add(request);
            return Collections.emptyList();
        }
        return resolver.activeProfiles();
    }
}
