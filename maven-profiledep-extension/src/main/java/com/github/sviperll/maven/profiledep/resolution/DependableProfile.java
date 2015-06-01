/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.resolution;

import com.github.sviperll.maven.profiledep.PropertyName;
import java.util.Set;
import java.util.TreeSet;
import org.apache.maven.model.Profile;

/**
 *
 * @author vir
 */
class DependableProfile {
    static Set<String> providedIDs(Profile profile) {
        Set<String> ids = new TreeSet<String>();
        ids.add(profile.getId());
        String profileprovide = profile.getProperties().getProperty(PropertyName.PROFILE_PROVIDES, "").trim();
        if (!profileprovide.isEmpty()) {
            String[] providedIDs = profileprovide.split("[;,]", -1);
            for (String providedID: providedIDs) {
                providedID = providedID.trim();
                if (!providedID.isEmpty())
                    ids.add(providedID);
            }
        }
        return ids;
    }
    private DependableProfile() {
    }
}
