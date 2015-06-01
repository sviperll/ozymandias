/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.resolution;

import com.github.sviperll.maven.profiledep.PropertyName;
import java.util.Collection;
import java.util.List;
import org.apache.maven.model.Profile;

/**
 *
 * @author vir
 */
// It must be final to have correct clone implementation
final class ResolutionState implements Cloneable {
    private final ProfileIDResolver idResolver;
    private final DependencyResolutionValidator validator = new DependencyResolutionValidator();
    private final ProfileCollector collector = new ProfileCollector();

    ResolutionState(Collection<Profile> availableProfiles) {
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

    ResolutionState evolve() throws ResolutionValidationException {
        validator.validate();
        List<Profile> profiles;
        while (!(profiles = idResolver.resolveUnambigous()).isEmpty()) {
            activate(profiles);
        }
        if (!idResolver.ambiguityExists())
            return this;
        else
            return idResolver.findStateForAmbigousIDs(new ChildFactory(this));
    }

    // We do not want to call super.clone, but call constructor instead
    // since it allows up to use final fields.
    // Clone is always called from ResolutionState type  because
    // class is declared final.
    // We don't need to call super.clone method, since
    // we have no need to create instances of any subtype because
    // no subtypes exists.
    @Override
    public ResolutionState clone() {
        ResolutionState result = new ResolutionState(idResolver.availableProfiles());
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
        String profiledep = profile.getProperties().getProperty(PropertyName.PROFILE_DEPENDS, "").trim();
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

    private static class ChildFactory implements ResolutionStateFactory {
        private final ResolutionState parent;

        private ChildFactory(ResolutionState parent) {
            this.parent = parent;
        }

        @Override
        public ResolutionState createResolutionState() {
            return parent.clone();
        }
    }
}
