/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.resolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.model.Profile;

/**
 *
 * @author vir
 */
class ProfileCollector {
    private final List<Profile> activeProfiles = new ArrayList<Profile>();
    private final Set<Profile> activeProfilesSet = new HashSet<Profile>();

    ProfileCollector() {
    }

    private ProfileCollector(List<Profile> activeProfiles, Set<Profile> activeProfilesSet) {
        activeProfiles.addAll(activeProfiles);
        activeProfilesSet.addAll(activeProfilesSet);
    }

    void addAll(List<Profile> profiles) {
        for (Profile profile: profiles) {
            add(profile);
        }
    }

    void add(Profile profile) {
        if (!activeProfilesSet.contains(profile)) {
            activeProfilesSet.add(profile);
            activeProfiles.add(profile);
        }
    }

    List<Profile> activeProfiles() {
        List<Profile> result = new ArrayList<Profile>();
        result.addAll(activeProfiles);
        return result;
    }
    
}
