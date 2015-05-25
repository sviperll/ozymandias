/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.building.ModelProblem.Version;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author vir
 */
public class DependenciesProfileSelector implements ProfileSelector {
    private final Logger logger;
    private final List<ProfileActivator> activators;

    DependenciesProfileSelector(Logger logger, List<ProfileActivator> activators) {
        this.logger = logger;
        this.activators = activators;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        DependencyResolution resolution = new DependencyResolution(availableProfiles);
        resolution.declareForbidden(context.getInactiveProfileIds());
        resolution.declareUnresolved(context.getActiveProfileIds());
        try {
            resolution = resolution.resolve();
        } catch (ResolutionValidationException ex) {
            ModelProblemCollectorRequest request = new ModelProblemCollectorRequest(ModelProblem.Severity.FATAL, ModelProblem.Version.BASE);
            request.setMessage("\n" + ex.renderResolutionTree());
            problems.add(request);
            return Collections.emptyList();
        }
        for (Profile profile: availableProfiles) {
            if (isActive(profile, context, problems)) {
                DependencyResolution possibleResolution = resolution.clone();
                try {
                    possibleResolution.activate(Collections.singletonList(profile));
                    resolution = possibleResolution.resolve();
                } catch (ResolutionValidationException ex) {
                    // Skip implicitly activated profile, since
                    // it conflicts with explicitly activated
                }
            }
        }
        for (Profile profile: availableProfiles) {
            if (isActiveByDefault(profile)) {
                DependencyResolution possibleResolution = resolution.clone();
                try {
                    possibleResolution.activate(Collections.singletonList(profile));
                    resolution = possibleResolution.resolve();
                } catch (ResolutionValidationException ex) {
                    // Skip activated by default profile, since
                    // it conflicts with explicitly activated
                }
            }
        }
        return resolution.activeProfiles();
    }

    private boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        boolean isActive = true;
        for (ProfileActivator activator : activators) {
            try {
                isActive &= activator.isActive(profile, context, problems);
            } catch (RuntimeException e) {
                problems.add(new ModelProblemCollectorRequest(Severity.ERROR, Version.BASE)
                        .setMessage("Failed to determine activation for profile " + profile.getId())
                        .setLocation(profile.getLocation(""))
                        .setException(e));
                return false;
            }
        }
        return isActive;
    }

    private boolean isActiveByDefault(Profile profile) {
        Activation activation = profile.getActivation();
        return activation != null && activation.isActiveByDefault();
    }
}
