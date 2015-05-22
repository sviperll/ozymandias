/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import java.util.Collection;
import java.util.List;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author vir
 */
@Component(role = ProfileSelector.class)
public class ConfiguringProfileSelector implements ProfileSelector {
    @Requirement(role = ProfileSelector.class)
    private List<ProfileSelector> profileSelectors;
    private ProfileSelector configuredProfileSelector;

    @Requirement
    private Logger logger;

    private void init() {
        if (configuredProfileSelector == null) {
            ProfileSelector defaultProfileSelector = null;
            for (ProfileSelector profileSelector: profileSelectors) {
                if (profileSelector.getClass() != ConfiguringProfileSelector.class) {
                    defaultProfileSelector = profileSelector;
                }
            }
            configuredProfileSelector = new ContextualProfileSelector(logger, new DependenciesProfileSelector(logger, defaultProfileSelector));
        }
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        init();
        return configuredProfileSelector.getActiveProfiles(availableProfiles, context, problems);
    }   
}
