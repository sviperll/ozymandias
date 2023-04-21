/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import com.github.sviperll.maven.profiledep.util.PlexusLoggingHandler;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
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
    private final MultipleContextsProfileSelector instance;
    private final Logger logger;

    // It's somewhat undocumented, but
    // Maven since 3.0 provides plexus implementation build on Google Guice
    // So we can directly use Guice's @Inject
    @Inject
    public ConfiguringProfileSelector(Logger logger, List<ProfileActivator> activators) {
        StrongDefaultActivationProfileSelector defaultProfileSelector = new StrongDefaultActivationProfileSelector(activators);
        DependenciesProfileSelector dependenciesProfileSelector = new DependenciesProfileSelector(defaultProfileSelector);
        ContextModifyingProfileSelector.Factory factory = new ContextModifyingProfileSelector.Factory(dependenciesProfileSelector);
        instance = new MultipleContextsProfileSelector(factory);
        this.logger = logger;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> profiles, ProfileActivationContext context, ModelProblemCollector problems) {
        java.util.logging.Logger rootJavaLogger =
                java.util.logging.Logger.getLogger("com.github.sviperll.maven.profiledep");
        if (rootJavaLogger.getUseParentHandlers()) {
            rootJavaLogger.setUseParentHandlers(false);
            java.util.logging.Handler handler = new PlexusLoggingHandler(logger);
            handler.setLevel(java.util.logging.Level.ALL);
            rootJavaLogger.addHandler(handler);
            rootJavaLogger.setLevel(java.util.logging.Level.ALL);
        }
        return instance.getActiveProfiles(profiles, context, problems);
    }
}
