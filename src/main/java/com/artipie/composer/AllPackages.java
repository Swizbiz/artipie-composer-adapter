/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/composer-adapter/blob/master/LICENSE.txt
 */
package com.artipie.composer;

import com.artipie.asto.Key;
import java.util.Optional;

/**
 * Key for all packages value.
 *
 * @since 0.1
 */
public final class AllPackages implements Key {
    @Override
    public String string() {
        return "packages.json";
    }

    @Override
    public Optional<Key> parent() {
        return Optional.empty();
    }
}
