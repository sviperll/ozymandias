/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.resolution;

import com.github.sviperll.maven.profiledep.util.TreeBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.maven.model.Profile;

/**
 *
 * @author vir
 */
class ProfileIDResolver {
    private final Collection<Profile> availableProfiles;
    private final Set<String> unresolvedProfileIDs = new TreeSet<String>();
    private final Set<String> unresolvableProfileIDs = new TreeSet<String>();
    private final Set<String> ambigousProfileIDs = new TreeSet<String>();

    ProfileIDResolver(Collection<Profile> availableProfiles) {
        this.availableProfiles = availableProfiles;
    }

    Collection<Profile> availableProfiles() {
        return availableProfiles;
    }

    boolean canResolve() {
        return !unresolvedProfileIDs.isEmpty() || !ambigousProfileIDs.isEmpty();
    }

    void include(ProfileIDResolver resolver) {
        unresolvedProfileIDs.addAll(resolver.unresolvedProfileIDs);
        unresolvableProfileIDs.addAll(resolver.unresolvableProfileIDs);
        ambigousProfileIDs.addAll(resolver.ambigousProfileIDs);

        ambigousProfileIDs.removeAll(unresolvableProfileIDs);
        unresolvedProfileIDs.removeAll(unresolvableProfileIDs);
        unresolvedProfileIDs.removeAll(ambigousProfileIDs);
    }

    void declareResolved(String profileID) {
        unresolvedProfileIDs.remove(profileID);
        ambigousProfileIDs.remove(profileID);
        unresolvableProfileIDs.remove(profileID);
    }

    void declareUnresolved(Collection<String> profileIDs) {
        unresolvedProfileIDs.addAll(profileIDs);
    }

    void declareUnresolved(String profileID) {
        unresolvedProfileIDs.add(profileID);
    }

    boolean ambiguityExists() {
        return !ambigousProfileIDs.isEmpty();
    }
    
    List<Profile> resolveUnambigous() throws ResolutionValidationException {
        if (unresolvedProfileIDs.isEmpty())
            return Collections.emptyList();
        else {
            List<Profile> discoveredProfiles = new ArrayList<Profile>();
            Set<String> profileIDs = new TreeSet<String>();
            profileIDs.addAll(unresolvedProfileIDs);
            for (String profileID : profileIDs) {
                List<Profile> candidates = new ArrayList<Profile>();
                for (Profile profile : availableProfiles) {
                    Set<String> candidateProfileIDs = DependableProfile.providedIDs(profile);
                    if (candidateProfileIDs.contains(profileID)) {
                        candidates.add(profile);
                    }
                }
                if (candidates.isEmpty()) {
                    unresolvableProfileIDs.add(profileID);
                    unresolvedProfileIDs.remove(profileID);
                } else if (candidates.size() > 1) {
                    ambigousProfileIDs.add(profileID);
                    unresolvedProfileIDs.remove(profileID);
                } else {
                    Profile candidate = candidates.get(0);
                    discoveredProfiles.add(candidate);
                }
            }
            return discoveredProfiles;
        }
    }

    ResolutionState findStateForAmbigousIDs(ResolutionStateFactory stateFactory) throws ResolutionValidationException {
        String profileID = ambigousProfileIDs.iterator().next();
        List<Profile> candidates = new ArrayList<Profile>();
        for (Profile profile : availableProfiles) {
            Set<String> profileIDs = DependableProfile.providedIDs(profile);
            if (profileIDs.contains(profileID)) {
                candidates.add(profile);
            }
        }
        TreeBuilder<String> resolutionTreeBuilder = TreeBuilder.createInstance(".");
        resolutionTreeBuilder.beginSubtree("Can't resolve " + profileID);
        for (Profile profile : candidates) {
            ResolutionState state = stateFactory.createResolutionState();
            try {
                state.activate(Collections.singletonList(profile));
                return state.evolve();
            } catch (ResolutionValidationException ex) {
                resolutionTreeBuilder.subtree(" to " + profile.getId(), ex.tree().children());
            }
        }
        resolutionTreeBuilder.endSubtree();
        throw new ResolutionValidationException(resolutionTreeBuilder.build());
    }
}
