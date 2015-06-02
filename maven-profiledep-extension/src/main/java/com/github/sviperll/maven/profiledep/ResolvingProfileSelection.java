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

import com.github.sviperll.maven.profiledep.resolution.DependencyResolver;
import com.github.sviperll.maven.profiledep.resolution.ResolutionValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.profile.ProfileActivationContext;

/**
 *
 * @author vir
 */
class ResolvingProfileSelection {
    static ResolvingProfileSelection createInstance(Collection<Profile> availableProfiles, ProfileActivationContext context, ModelProblemCollector problems) {
        DependencyResolver resolver = DependencyResolver.createInstance(availableProfiles);
        resolver.declareForbidden(context.getInactiveProfileIds());

        List<String> requiredProfileIDs = new ArrayList<String>();
        requiredProfileIDs.addAll(context.getActiveProfileIds());
        requiredProfileIDs.removeAll(context.getInactiveProfileIds());
        resolver.declareUnresolved(requiredProfileIDs);
        return new ResolvingProfileSelection(resolver, problems);
    }

    private final DependencyResolver resolver;
    private final ModelProblemCollector problems;
    private boolean isError = false;

    private ResolvingProfileSelection(DependencyResolver resolver, ModelProblemCollector problems) {
        this.resolver = resolver;
        this.problems = problems;
    }

    boolean isError() {
        return isError;
    }

    List<Profile> activeProfiles() {
        return isError ? Collections.<Profile>emptyList() : resolver.activeProfiles();
    }

    void activate(List<Profile> activatedProfiles) {
        if (!isError) {
            try {
                resolver.activate(activatedProfiles);
            } catch (ResolutionValidationException ex) {
                processResolutionException(ex);
            }
        }
    }

    void resolve() {
        if (!isError) {
            try {
                resolver.resolve();
            } catch (ResolutionValidationException ex) {
                processResolutionException(ex);
            }
        }
    }

    private void processResolutionException(ResolutionValidationException ex) {
        ModelProblemCollectorRequest request = new ModelProblemCollectorRequest(ModelProblem.Severity.FATAL, ModelProblem.Version.BASE);
        request.setMessage("\n" + ex.renderResolutionTree());
        problems.add(request);
        isError = true;
    }
    
}
