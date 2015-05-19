package com.github.sviperll.maven.plugin.coreext;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vir
 */
public class ExtensionProcessor {
    private final List<Extension> installed;

    ExtensionProcessor(List<Extension> installedExtensions) {
        this.installed = installedExtensions;
    }

    List<Extension> getUninstalled(List<Extension> extensions) {
        List<Extension> uninstalled = new ArrayList<Extension>();
        for (Extension extension: extensions) {
            if (!uninstalled.contains(extension))
                uninstalled.add(extension);
        }
        uninstalled.removeAll(installed);
        return uninstalled;
    }

    Map<Extension, String> getDifferentInstalledVersions(List<Extension> requiredExtensions) {
        Map<Extension, String> installedVersions = new HashMap<Extension, String>();
        for (Extension extension: installed) {
            installedVersions.put(extension, extension.version);
        }
        Map<Extension, String> differences = new HashMap<Extension, String>();
        for (Extension required: requiredExtensions) {
            String installedVersion = installedVersions.get(required);
            if (installedVersion != null
                    && !installedVersion.trim().equals(required.version.trim())) {
                differences.put(required, installedVersion);
            }
        }
        return differences;
    }

    void install(List<Extension> extensions) {
        Map<Extension, String> differences = getDifferentInstalledVersions(extensions);
        installed.removeAll(differences.keySet());
        List<Extension> uninstalled = getUninstalled(extensions);
        installed.addAll(uninstalled);
    }
}
