/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.DefaultProfileSelector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author vir
*/
@Component(role = ProfileSelector.class)
public class ConfiguringProfileSelector implements ProfileSelector {
    private final ContextualProfileSelector instance;
    private final Logger logger;

    @Inject
    public ConfiguringProfileSelector(Logger logger, List<ProfileActivator> activators) {
        this.logger = logger;
        DependenciesProfileSelector dependenciesProfileSelector = new DependenciesProfileSelector(logger, activators);
        ActivatingProfileSelector.Factory factory = new ActivatingProfileSelector.Factory(logger, dependenciesProfileSelector);
        instance = new ContextualProfileSelector(logger, factory);
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> profiles, ProfileActivationContext context, ModelProblemCollector problems) {
        logger.info(MessageFormat.format("ConfiguringProfileSelector.getAciveProfiles {0}", context));
        return instance.getActiveProfiles(profiles, context, problems);
    }

    
}
