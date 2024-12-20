/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.esql.index;

import org.elasticsearch.action.fieldcaps.FieldCapabilitiesFailure;
import org.elasticsearch.core.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class IndexResolution {

    public static IndexResolution valid(EsIndex index, Map<String, FieldCapabilitiesFailure> unavailableClusters) {
        Objects.requireNonNull(index, "index must not be null if it was found");
        Objects.requireNonNull(unavailableClusters, "unavailableClusters must not be null");
        return new IndexResolution(index, null, unavailableClusters);
    }

    public static IndexResolution valid(EsIndex index) {
        return valid(index, Collections.emptyMap());
    }

    public static IndexResolution invalid(String invalid) {
        Objects.requireNonNull(invalid, "invalid must not be null to signal that the index is invalid");
        return new IndexResolution(null, invalid, Collections.emptyMap());
    }

    public static IndexResolution notFound(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return invalid("Unknown index [" + name + "]");
    }

    private final EsIndex index;
    @Nullable
    private final String invalid;

    // remote clusters included in the user's index expression that could not be connected to
    private final Map<String, FieldCapabilitiesFailure> unavailableClusters;

    private IndexResolution(EsIndex index, @Nullable String invalid, Map<String, FieldCapabilitiesFailure> unavailableClusters) {
        this.index = index;
        this.invalid = invalid;
        this.unavailableClusters = unavailableClusters;
    }

    public boolean matches(String indexName) {
        return isValid() && this.index.name().equals(indexName);
    }

    /**
     * Get the {@linkplain EsIndex}
     * @throws MappingException if the index is invalid for use with ql
     */
    public EsIndex get() {
        if (invalid != null) {
            throw new MappingException(invalid);
        }
        return index;
    }

    /**
     * Is the index valid for use with ql? Returns {@code false} if the
     * index wasn't found.
     */
    public boolean isValid() {
        return invalid == null;
    }

    /**
     * @return Map of unavailable clusters (could not be connected to during field-caps query). Key of map is cluster alias,
     * value is the {@link FieldCapabilitiesFailure} describing the issue.
     */
    public Map<String, FieldCapabilitiesFailure> getUnavailableClusters() {
        return unavailableClusters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        IndexResolution other = (IndexResolution) obj;
        return Objects.equals(index, other.index)
            && Objects.equals(invalid, other.invalid)
            && Objects.equals(unavailableClusters, other.unavailableClusters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, invalid, unavailableClusters);
    }

    @Override
    public String toString() {
        return invalid != null ? invalid : index.name();
    }
}
