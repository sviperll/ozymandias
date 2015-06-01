/*
 * Copyright (c) 2015, vir
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.maven.profiledep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.model.profile.activation.ProfileActivator;

/**
 *
 * @author vir
 */
public class StrongDefaultActivationProfileSelector implements ProfileSelector {
    private static final Logger logger = Logger.getLogger(StrongDefaultActivationProfileSelector.class.getName());
    private final List<ProfileActivator> activators;

    StrongDefaultActivationProfileSelector(List<ProfileActivator> activators) {
        this.activators = activators;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        logger.log(Level.INFO, "{0}.getActiveProfiles(?, '{'inactiveProfileIds = {1}, activeProfileIds = {2}'}', ?)", new Object[] {StrongDefaultActivationProfileSelector.class.getSimpleName(), context.getInactiveProfileIds(), context.getActiveProfileIds()});
        String activatesActiveByDefaultString = context.getProjectProperties().get(PropertyName.ACTIVATE_ACTIVE_BY_DEFAULT_PROFILES);
        boolean activatesActiveByDefault = activatesActiveByDefaultString != null
                && (activatesActiveByDefaultString.equals("true")
                    || activatesActiveByDefaultString.equals("yes"));
        List<Profile> activatedProfiles = getActivatedProfiles(availableProfiles, context, problems);
        if (activatesActiveByDefault) {
            for (Profile profile: getActiveByDefaultProfiles(availableProfiles, context, problems)) {
                if (!activatedProfiles.contains(profile))
                    activatedProfiles.add(profile);
            }
        } else if (activatedProfiles.isEmpty()) {
            // Default profiles are activated only if nothing else is activated
            // This behaviour is the same as DefaultProfileSelector

            activatedProfiles = getActiveByDefaultProfiles(availableProfiles, context, problems);
        }
        return activatedProfiles;
    }

    List<Profile> getActivatedProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        List<Profile> result = new ArrayList<Profile>();
        for (Profile profile : availableProfiles) {
            if (!context.getInactiveProfileIds().contains(profile.getId())
                    && (context.getActiveProfileIds().contains(profile.getId())
                        || isActive(profile, context, problems))) {
                result.add(profile);
            }
        }
        return result;
    }

    List<Profile> getActiveByDefaultProfiles(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        List<Profile> result = new ArrayList<Profile>();
        for (Profile profile : availableProfiles) {
            if (!context.getInactiveProfileIds().contains(profile.getId())
                    && isActiveByDefault(profile)) {
                result.add(profile);
            }
        }
        return result;
    }

    private boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        boolean isActive = true;
        for (ProfileActivator activator : activators) {
            try {
                isActive &= activator.isActive(profile, context, problems);
            } catch (RuntimeException e) {
                problems.add(new ModelProblemCollectorRequest(ModelProblem.Severity.ERROR, ModelProblem.Version.BASE).setMessage("Failed to determine activation for profile " + profile.getId()).setLocation(profile.getLocation("")).setException(e));
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
