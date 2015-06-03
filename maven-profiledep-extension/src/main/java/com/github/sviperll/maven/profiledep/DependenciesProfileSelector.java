/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;

/**
 *
 * @author vir
 */
public class DependenciesProfileSelector implements ProfileSelector {
    private static final Logger logger = Logger.getLogger(DependenciesProfileSelector.class.getName());
    private final StrongDefaultActivationProfileSelector defaultProfileSelector;

    DependenciesProfileSelector(StrongDefaultActivationProfileSelector defaultProfileSelector) {
        this.defaultProfileSelector = defaultProfileSelector;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        ResolvingProfileSelection activation = ResolvingProfileSelection.createInstance(availableProfiles, context, problems);
        List<Profile> activatedProfiles = defaultProfileSelector.getActivatedProfiles(availableProfiles, context, problems);
        activation.activate(activatedProfiles);
        activation.resolve();
        if (!activation.isError() && activation.activeProfiles().isEmpty()) {
            // Default profiles are activated only if nothing else is activated
            // This repeats normal maven behaviour

            activatedProfiles = defaultProfileSelector.getActiveByDefaultProfiles(availableProfiles, context, problems);
            activation.activate(activatedProfiles);
            activation.resolve();
        }
        return activation.activeProfiles();
    }

}
