/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.DefaultProfileActivationContext;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;

/**
 *
 * @author vir
 */
public class ActivatingProfileSelector implements ProfileSelector {
    private final ProfileSelector defaultProfileSelector;
    private final Set<String> additionalProfileIDs = new TreeSet<String>();
    private final Set<String> additionallyExcludedProfileIDs = new TreeSet<String>();

    ActivatingProfileSelector(DependenciesProfileSelector defaultProfileSelector) {
        this.defaultProfileSelector = defaultProfileSelector;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        ProfileActivationContext effectiveContext = createProfileActivationContext(context);
        List<Profile> activatedProfiles = defaultProfileSelector.getActiveProfiles(availableProfiles, effectiveContext, problems);
        updateAdditionalProfileIDs(context.getProjectProperties());
        return activatedProfiles;
    }

    private void updateAdditionalProfileIDs(Map<String, String> projectProperties) {
        String activateparentprofiles = projectProperties.get("activateparentprofiles");
        if (activateparentprofiles != null) {
            activateparentprofiles = activateparentprofiles.trim();
            if (!activateparentprofiles.isEmpty()) {
                String[] profileIDs = activateparentprofiles.split("[;,]", -1);
                Set<String> activateSet = new TreeSet<String>();
                Set<String> deactivateSet = new TreeSet<String>();
                for (String profileID: profileIDs) {
                    profileID = profileID.trim();
                    if (profileID.startsWith("!")) {
                        profileID = profileID.substring(1).trim();
                        deactivateSet.add(profileID);
                    } else {
                        activateSet.add(profileID);
                    }
                }
                activateSet.removeAll(deactivateSet);
                additionalProfileIDs.removeAll(deactivateSet);
                additionallyExcludedProfileIDs.removeAll(activateSet);
                additionalProfileIDs.addAll(activateSet);
                additionallyExcludedProfileIDs.addAll(deactivateSet);
            }
        }
    }

    private ProfileActivationContext createProfileActivationContext(ProfileActivationContext source) {
        DefaultProfileActivationContext result = new DefaultProfileActivationContext();
        result.setProjectDirectory(source.getProjectDirectory());
        Properties projectProperties = new Properties();
        projectProperties.putAll(source.getProjectProperties());
        result.setProjectProperties(projectProperties);
        result.setSystemProperties(source.getSystemProperties());
        result.setUserProperties(source.getUserProperties());
        
        List<String> activeProfileIds = new ArrayList<String>();
        activeProfileIds.addAll(source.getActiveProfileIds());
        activeProfileIds.addAll(additionalProfileIDs);
        result.setActiveProfileIds(activeProfileIds);

        List<String> inactiveProfileIds = new ArrayList<String>();
        inactiveProfileIds.addAll(source.getInactiveProfileIds());
        inactiveProfileIds.addAll(additionallyExcludedProfileIDs);
        result.setInactiveProfileIds(inactiveProfileIds);
        return result;
    }
}
