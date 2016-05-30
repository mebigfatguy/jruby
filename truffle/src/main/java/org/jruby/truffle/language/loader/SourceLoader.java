/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.language.loader;

import com.oracle.truffle.api.source.Source;
import org.jruby.Ruby;
import org.jruby.truffle.RubyContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Locale;

public class SourceLoader {

    public static final String TRUFFLE_SCHEME = "truffle:";
    public static final String JRUBY_SCHEME = "jruby:";

    private final RubyContext context;

    public SourceLoader(RubyContext context) {
        this.context = context;
    }

    public Source load(String canonicalPath) throws IOException {
        if (canonicalPath.equals("-e")) {
            return loadInlineScript();
        } else if (canonicalPath.startsWith(TRUFFLE_SCHEME) || canonicalPath.startsWith(JRUBY_SCHEME)) {
            return loadResource(canonicalPath);
        } else {
            final File file = new File(canonicalPath);
            if (!file.canRead()) {
                throw new IOException("Can't read file " + canonicalPath);
            }
            assert file.getCanonicalPath().equals(canonicalPath) : canonicalPath;
            return Source.fromFileName(canonicalPath);
        }
    }

    private Source loadInlineScript() {
        return Source.fromText(new String(context.getJRubyRuntime().getInstanceConfig().inlineScript(),
                StandardCharsets.UTF_8), "-e");
    }

    private Source loadResource(String path) throws IOException {
        if (!path.toLowerCase(Locale.ENGLISH).endsWith(".rb")) {
            throw new FileNotFoundException(path);
        }

        final Class<?> relativeClass;
        final Path relativePath;

        if (path.startsWith(TRUFFLE_SCHEME)) {
            relativeClass = RubyContext.class;
            relativePath = FileSystems.getDefault().getPath(path.substring(TRUFFLE_SCHEME.length()));
        } else if (path.startsWith(JRUBY_SCHEME)) {
            relativeClass = Ruby.class;
            relativePath = FileSystems.getDefault().getPath(path.substring(JRUBY_SCHEME.length()));
        } else {
            throw new UnsupportedOperationException();
        }

        final Path normalizedPath = relativePath.normalize();
        final InputStream stream = relativeClass.getResourceAsStream(normalizedPath.toString().replace('\\', '/'));

        if (stream == null) {
            throw new FileNotFoundException(path);
        }

        return Source.fromReader(new InputStreamReader(stream, StandardCharsets.UTF_8), path);
    }

}
