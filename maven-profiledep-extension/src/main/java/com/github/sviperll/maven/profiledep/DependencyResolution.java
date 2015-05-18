/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import java.util.Collection;
import java.util.List;
import org.apache.maven.model.Profile;

/**
 *
 * @author vir
 */
class DependencyResolution {
    static DependencyResolution resolve(Collection<Profile> availableProfiles, List<Profile> activatedProfiles, Collection<String> unresolvedProfileIDs) throws ResolutionValidationException {
        DependencyResolution resolution = new DependencyResolution(availableProfiles);
        resolution.declareUnresolved(unresolvedProfileIDs);
        return resolution.addAll(activatedProfiles);
    }

    private final ProfileIDResolver idResolver;
    private final DependencyResolutionValidator validator = new DependencyResolutionValidator();
    private final ProfileCollector collector = new ProfileCollector();

    private DependencyResolution(Collection<Profile> availableProfiles) {
        this.idResolver = new ProfileIDResolver(availableProfiles);
    }

    DependencyResolution addAll(List<Profile> profiles) throws ResolutionValidationException {
        for (;;) {
            for (Profile profile : profiles) {
                activate(profile);
            }
            validator.validate();
            for (Profile profile : profiles) {
                collectDependencies(profile);
            }
            validator.validate();
            profiles = idResolver.resolveUnambigous();
            if (profiles.isEmpty())
                break;
        }
        if (!idResolver.ambiguityExists())
            return this;
        else
            return idResolver.createResolutionForAmbigousIDs(new ChildFactory(this));
    }

    private void activate(Profile profile) {
        Collection<String> profileIDs = DependableProfile.providedIDs(profile);
        for (String profileID : profileIDs) {
            idResolver.declareResolved(profileID);
            validator.provide(profileID, profile);
        }
        collector.add(profile);
    }

    private void collectDependencies(Profile profile) {
        String profiledep = profile.getProperties().getProperty("profiledep", "").trim();
        if (!profiledep.isEmpty()) {
            String[] dependencies = profiledep.split("[,;]", -1);
            for (String dependency : dependencies) {
                dependency = dependency.trim();
                if (dependency.startsWith("!")) {
                    dependency = dependency.substring(1).trim();
                    validator.forbid(dependency, profile);
                } else {
                    if (!validator.isAlreadyProvided(dependency)) {
                        idResolver.declareUnresolved(dependency);
                    }
                }
            }
        }
    }

    List<Profile> activeProfiles() {
        return collector.activeProfiles();
    }

    private void declareUnresolved(Collection<String> unresolvedProfileIDs) {
        idResolver.declareUnresolved(unresolvedProfileIDs);
    }

    private static class ChildFactory implements DependencyResolutionFactory {
        private final DependencyResolution parent;

        private ChildFactory(DependencyResolution parent) {
            this.parent = parent;
        }

        @Override
        public DependencyResolution createDependencyResolution() {
            DependencyResolution result = new DependencyResolution(parent.idResolver.availableProfiles());
            result.idResolver.include(parent.idResolver);
            result.validator.addAll(parent.validator);
            result.collector.addAll(parent.collector.activeProfiles());
            return result;
        }
    }
}
