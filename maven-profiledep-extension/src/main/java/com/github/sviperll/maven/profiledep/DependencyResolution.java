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
class DependencyResolution implements Cloneable {
    private final ProfileIDResolver idResolver;
    private final DependencyResolutionValidator validator = new DependencyResolutionValidator();
    private final ProfileCollector collector = new ProfileCollector();

    DependencyResolution(Collection<Profile> availableProfiles) {
        this.idResolver = new ProfileIDResolver(availableProfiles);
    }

    List<Profile> activeProfiles() {
        return collector.activeProfiles();
    }

    void declareUnresolved(Collection<String> profileIDs) {
        idResolver.declareUnresolved(profileIDs);
    }

    void declareForbidden(Collection<String> profileIDs) {
        for (String profileID: profileIDs) {
            validator.forbidExplicitly(profileID);
        }
    }

    void activate(List<Profile> profiles) throws ResolutionValidationException {
        for (Profile profile : profiles) {
            setActive(profile);
        }
        validator.validate();
        for (Profile profile : profiles) {
            collectDependencies(profile);
        }
        validator.validate();
    }

    DependencyResolution resolve() throws ResolutionValidationException {
        validator.validate();
        List<Profile> profiles;
        while (!(profiles = idResolver.resolveUnambigous()).isEmpty()) {
            activate(profiles);
        }
        if (!idResolver.ambiguityExists())
            return this;
        else
            return idResolver.createResolutionForAmbigousIDs(new ChildFactory(this));
    }

    @Override
    public DependencyResolution clone() {
        DependencyResolution result = new DependencyResolution(idResolver.availableProfiles());
        result.idResolver.include(idResolver);
        result.validator.addAll(validator);
        result.collector.addAll(collector.activeProfiles());
        return result;
    }

    private void setActive(Profile profile) {
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

    private static class ChildFactory implements DependencyResolutionFactory {
        private final DependencyResolution parent;

        private ChildFactory(DependencyResolution parent) {
            this.parent = parent;
        }

        @Override
        public DependencyResolution createDependencyResolution() {
            return parent.clone();
        }
    }
}
