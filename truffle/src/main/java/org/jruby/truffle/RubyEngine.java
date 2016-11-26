/*
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import org.jruby.JRubyTruffleInterface;
import org.jruby.RubyInstanceConfig;
import org.jruby.truffle.interop.InstanceConfigWrapper;
import org.jruby.truffle.platform.graal.Graal;
import org.jruby.util.cli.Options;

import java.io.InputStream;

public class RubyEngine {

    private final PolyglotEngine engine;
    private final RubyContext context;

    public RubyEngine(RubyInstanceConfig instanceConfig) {
        engine = PolyglotEngine.newBuilder()
                .globalSymbol(JRubyTruffleInterface.RUNTIME_SYMBOL, new InstanceConfigWrapper(instanceConfig))
                .build();
        Main.printTruffleTimeMetric("before-load-context");
        context = engine.eval(loadSource("Truffle::Boot.context", "context")).as(RubyContext.class);
        Main.printTruffleTimeMetric("after-load-context");
    }

    public int execute(String path) {
        if (!Graal.isGraal() && Options.TRUFFLE_GRAAL_WARNING_UNLESS.load()) {
            System.err.println("WARNING: This JVM does not have the Graal compiler. " +
                    "JRuby+Truffle's performance without it will be limited. " +
                    "See https://github.com/jruby/jruby/wiki/Truffle-FAQ#how-do-i-get-jrubytruffle");
        }

        context.setOriginalInputFile(path);

        return engine.eval(loadSource("Truffle::Boot.main", "main")).as(Integer.class);
    }

    public boolean checkSyntax(InputStream in, String filename) {
        context.setSyntaxCheckInputStream(in);
        context.setOriginalInputFile(filename);

        return engine.eval(loadSource("Truffle::Boot.check_syntax", "check_syntax")).as(Boolean.class);
    }

    public RubyContext getContext() {
        return context;
    }

    public void dispose() {
        engine.dispose();
    }

    @TruffleBoundary
    private Source loadSource(String source, String name) {
        return Source.newBuilder(source).name(name).mimeType(RubyLanguage.MIME_TYPE).build();
    }

}
