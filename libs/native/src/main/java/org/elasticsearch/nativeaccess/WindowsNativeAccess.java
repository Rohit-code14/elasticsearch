/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.nativeaccess;

import org.elasticsearch.nativeaccess.lib.NativeLibraryProvider;

import java.util.Optional;

class WindowsNativeAccess extends AbstractNativeAccess {

    WindowsNativeAccess(NativeLibraryProvider libraryProvider) {
        super("Windows", libraryProvider);
    }

    @Override
    public boolean definitelyRunningAsRoot() {
        return false; // don't know
    }

    @Override
    public ProcessLimits getProcessLimits() {
        return new ProcessLimits(ProcessLimits.UNKNOWN, ProcessLimits.UNKNOWN, ProcessLimits.UNKNOWN);
    }

    @Override
    public Optional<VectorSimilarityFunctions> getVectorSimilarityFunctions() {
        return Optional.empty(); // not supported yet
    }
}
