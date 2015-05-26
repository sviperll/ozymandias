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
import java.util.List;
import org.apache.maven.model.Profile;

/**
 *
 * @author vir
 */
class StatefullDependencyResolver implements DependencyResolver {

    static StatefullDependencyResolver createInstance(Collection<Profile> availableProfiles) {
        return new StatefullDependencyResolver(new ResolutionState(availableProfiles));
    }
    private ResolutionState state;
    
    StatefullDependencyResolver(ResolutionState state) {
        this.state = state;
    }

    ResolutionState getState() {
        return state;
    }

    void setState(ResolutionState state) {
        this.state = state;
    }

    @Override
    public List<Profile> activeProfiles() {
        return state.activeProfiles();
    }

    @Override
    public void declareUnresolved(Collection<String> profileIDs) {
        state.declareUnresolved(profileIDs);
    }

    @Override
    public void declareForbidden(Collection<String> profileIDs) {
        state.declareForbidden(profileIDs);
    }

    @Override
    public void activate(List<Profile> profiles) throws ResolutionValidationException {
        state.activate(profiles);
    }

    @Override
    public void resolve() throws ResolutionValidationException {
        state = state.evolve();
    }
}
