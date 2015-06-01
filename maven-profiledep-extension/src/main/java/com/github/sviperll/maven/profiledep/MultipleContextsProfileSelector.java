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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileSelector;

/**
 *
 * @author vir
 */
public class MultipleContextsProfileSelector implements ProfileSelector {
    private static final Logger logger = Logger.getLogger(MultipleContextsProfileSelector.class.getName());

    /* HACK:
     * Each ModelProblemCollector specifies single chain of profile resolution from child to parent, up to root pom.
     * Each chain should be resolved individually, so we keep a map to hold each chain state.
     */
    private final Map<ProfileActivationContext, ProfileSelector> selectors = new HashMap<ProfileActivationContext, ProfileSelector>();
    private final ProfileSelectorFactory factory;

    MultipleContextsProfileSelector(ProfileSelectorFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Profile> getActiveProfiles(Collection<Profile> profiles, ProfileActivationContext context, ModelProblemCollector problems) {
        logger.log(Level.INFO, "{0}.getActiveProfiles(?, {1}, ?)", new Object[] {MultipleContextsProfileSelector.class.getSimpleName(), context});
        ProfileSelector selector = selectors.get(context);
        if (selector == null) {
            selector = factory.createProfileSelector();
            selectors.put(context, selector);
        }
        return selector.getActiveProfiles(profiles, context, problems);
    }
}
