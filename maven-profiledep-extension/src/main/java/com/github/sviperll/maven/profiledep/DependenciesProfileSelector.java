/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private final ProfileSelector defaultProfileSelector;

    DependenciesProfileSelector(ProfileSelector defaultProfileSelector) {
        this.defaultProfileSelector = defaultProfileSelector;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        List<Profile> activatedProfiles = defaultProfileSelector.getActiveProfiles(availableProfiles, context, problems);
        try {
            DependencyResolution resolution = DependencyResolution.resolve(availableProfiles, activatedProfiles, context.getActiveProfileIds());
            return resolution.activeProfiles();
        } catch (ResolutionValidationException ex) {
            ModelProblemCollectorRequest request = new ModelProblemCollectorRequest(ModelProblem.Severity.FATAL, ModelProblem.Version.BASE);
            request.setMessage("\n" + ex.renderResolutionTree());
            problems.add(request);
            return Collections.emptyList();
        }
    }
}
