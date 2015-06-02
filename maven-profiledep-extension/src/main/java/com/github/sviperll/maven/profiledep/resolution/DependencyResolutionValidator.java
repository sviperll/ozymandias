/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.resolution;

import com.github.sviperll.maven.profiledep.util.TreeBuilder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.maven.model.Profile;

/**
 *
 * @author vir
 */
class DependencyResolutionValidator {
    private final static Profile EXPLICIT_DEMAND = new Profile();
    private final Map<String, Set<Profile>> activeProfileIDs = new TreeMap<String, Set<Profile>>();
    private final Map<String, Set<Profile>> forbiddenProfileIDs = new TreeMap<String, Set<Profile>>();

    boolean isAlreadyProvided(String profileID) {
        return activeProfileIDs.containsKey(profileID);
    }

    void provide(String profileID, Profile provider) {
        Set<Profile> providedBy = providingProfilesFor(profileID);
        providedBy.add(provider);
    }

    void forbidExplicitly(String profileID) {
        forbid(profileID, EXPLICIT_DEMAND);
    }

    void forbid(String conflictingProfileID, Profile profile) {
        Set<Profile> forbiddenBy = forbiddingProfilesFor(conflictingProfileID);
        forbiddenBy.add(profile);
    }

    void addAll(DependencyResolutionValidator validator) {
        for (Map.Entry<String, Set<Profile>> entry : validator.activeProfileIDs.entrySet()) {
            Set<Profile> providedBy = providingProfilesFor(entry.getKey());
            providedBy.addAll(entry.getValue());
        }
        for (Map.Entry<String, Set<Profile>> entry : validator.forbiddenProfileIDs.entrySet()) {
            Set<Profile> forbiddenBy = forbiddingProfilesFor(entry.getKey());
            forbiddenBy.addAll(entry.getValue());
        }
    }

    private Set<Profile> providingProfilesFor(String profileID) {
        Set<Profile> providedBy = activeProfileIDs.get(profileID);
        if (providedBy == null) {
            providedBy = new HashSet<Profile>();
            activeProfileIDs.put(profileID, providedBy);
        }
        return providedBy;
    }

    private Set<Profile> forbiddingProfilesFor(String conflictingProfileID) {
        Set<Profile> forbiddenBy = forbiddenProfileIDs.get(conflictingProfileID);
        if (forbiddenBy == null) {
            forbiddenBy = new HashSet<Profile>();
            forbiddenProfileIDs.put(conflictingProfileID, forbiddenBy);
        }
        return forbiddenBy;
    }

    void validate() throws ResolutionValidationException {
        boolean isError = false;
        TreeBuilder<String> resolutionTreeBuilder = TreeBuilder.createInstance(".");
        for (String profileID : activeProfileIDs.keySet()) {
            try {
                validate(profileID);
            } catch (ResolutionValidationException ex) {
                isError = true;
                resolutionTreeBuilder.subtree("Can't provide " + profileID, ex.tree().children());
            }
        }
        if (isError) {
            throw new ResolutionValidationException(resolutionTreeBuilder.build());
        }
    }

    private void validate(String profileID) throws ResolutionValidationException {
        boolean isError = false;
        TreeBuilder<String> resulutionTreeBuilder = TreeBuilder.createInstance("Can't provide " + profileID);
        Set<Profile> providedBy = activeProfileIDs.get(profileID);
        if (providedBy != null && providedBy.size() > 1) {
            isError = true;
            resulutionTreeBuilder.beginSubtree("more than one profile provides it");
            for (Profile profile : providedBy) {
                resulutionTreeBuilder.node(profileID(profile));
            }
            resulutionTreeBuilder.endSubtree();
        }
        Set<Profile> forbiddenBy = forbiddenProfileIDs.get(profileID);
        if (forbiddenBy != null && !forbiddenBy.isEmpty()) {
            isError = true;
            resulutionTreeBuilder.beginSubtree("it conflicts with some profiles");
            for (Profile profile : forbiddenBy) {
                resulutionTreeBuilder.node(profileID(profile));
            }
        }
        if (isError) {
            throw new ResolutionValidationException(resulutionTreeBuilder.build());
        }
    }

    public String profileID(Profile profile) {
        if (profile == EXPLICIT_DEMAND)
            return "<explicit demand>";
        else
            return profile.getId();
    }
}
