/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.format.unpack;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import org.truffleruby.RubyContext;
import org.truffleruby.core.format.LoopRecovery;
import org.truffleruby.core.format.pack.SimplePackParser;
import org.truffleruby.language.RubyNode;

import java.nio.charset.StandardCharsets;

public class UnpackCompiler {

    private final RubyContext context;
    private final RubyNode currentNode;

    public UnpackCompiler(RubyContext context, RubyNode currentNode) {
        this.context = context;
        this.currentNode = currentNode;
    }

    public CallTarget compile(String format) {
        if (format.length() > context.getOptions().PACK_RECOVER_LOOP_MIN) {
            format = LoopRecovery.recoverLoop(format);
        }

        final SimpleUnpackTreeBuilder builder = new SimpleUnpackTreeBuilder(context, currentNode);

        builder.enterSequence();

        final SimplePackParser parser = new SimplePackParser(builder, format.getBytes(StandardCharsets.US_ASCII));
        parser.parse();

        builder.exitSequence();

        return Truffle.getRuntime().createCallTarget(
                new UnpackRootNode(context, currentNode.getEncapsulatingSourceSection(), builder.getNode()));
    }

}
