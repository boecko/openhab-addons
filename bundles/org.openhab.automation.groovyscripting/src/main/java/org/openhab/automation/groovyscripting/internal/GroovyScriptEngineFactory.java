/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.groovyscripting.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Component;

import groovy.lang.GroovyClassLoader;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for Groovy.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class GroovyScriptEngineFactory extends AbstractScriptEngineFactory {

    private static final String FILE_DIRECTORY = "automation" + File.separator + "groovy";
    private static final String LIBS_DIRECTORY = "automation" + File.separator + "groovylibs";
    private final org.codehaus.groovy.jsr223.GroovyScriptEngineFactory factory = new org.codehaus.groovy.jsr223.GroovyScriptEngineFactory();

    private final List<String> scriptTypes = (List<String>) Stream.of(factory.getExtensions(), factory.getMimeTypes())
            .flatMap(List::stream) //
            .collect(Collectors.toUnmodifiableList());

    private final GroovyClassLoader gcl = new GroovyClassLoader();

    public GroovyScriptEngineFactory() {
        String scriptDir = OpenHAB.getConfigFolder() + File.separator + FILE_DIRECTORY;
        logger.debug("Adding script directory {} to the GroovyScriptEngine class path.", scriptDir);
        gcl.addClasspath(scriptDir);

        String scriptLibPath = OpenHAB.getConfigFolder() + File.separator + LIBS_DIRECTORY;
        File scriptLibDir = new File(scriptLibPath);
        if(scriptLibDir.isDirectory()) {
            final FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(".jar");
            File[] files = scriptLibDir.listFiles(filter);
            for (File jarFile : files) {
                logger.debug("Adding {} to the GroovyScriptEngine class path.", jarFile.getPath());
                gcl.addClasspath(jarFile.getPath());
            }
        }
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        if (scriptTypes.contains(scriptType)) {
            return new org.codehaus.groovy.jsr223.GroovyScriptEngineImpl(gcl);
        }
        return null;
    }
}
