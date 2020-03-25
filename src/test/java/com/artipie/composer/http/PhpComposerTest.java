/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.composer.http;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.composer.AllPackages;
import com.artipie.http.Response;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rs.RsStatus;
import com.google.common.io.ByteStreams;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import org.cactoos.io.ResourceOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PhpComposer}.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
class PhpComposerTest {

    /**
     * Request line to get all packages.
     */
    private static final String GET_PACKAGES = "GET /base/packages.json";

    /**
     * Storage used in tests.
     */
    private Storage storage;

    /**
     * Tested PhpComposer slice.
     */
    private PhpComposer php;

    @BeforeEach
    void init() {
        this.storage = new InMemoryStorage();
        this.php = new PhpComposer("/base", this.storage);
    }

    @Test
    void shouldGetPackageContent() {
        final byte[] data = "data".getBytes();
        new BlockingStorage(this.storage).save(
            new Key.From("vendor", "package.json"),
            data
        );
        final Response response = this.php.response(
            "GET /base/p/vendor/package.json",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            "Package metadata should be returned in response",
            response,
            new AllOf<>(
                Arrays.asList(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasBody(data)
                )
            )
        );
    }

    @Test
    void shouldFailGetPackageMetadataFromNotBasePath() {
        final Response response = this.php.response(
            "GET /not-base/p/vendor/package.json",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            "Resources from outside of base path should not be found",
            response,
            new RsHasStatus(RsStatus.NOT_FOUND)
        );
    }

    @Test
    void shouldFailGetPackageMetadataWhenNotExists() {
        final Response response = this.php.response(
            "GET /base/p/vendor/unknown-package.json",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            "Not existing metadata should not be found",
            response,
            new RsHasStatus(RsStatus.NOT_FOUND)
        );
    }

    @Test
    void shouldFailPutPackageMetadata() {
        final Response response = this.php.response(
            "PUT /base/p/vendor/package.json",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            "Package metadata cannot be put",
            response,
            new RsHasStatus(RsStatus.METHOD_NOT_ALLOWED)
        );
    }

    @Test
    void shouldGetAllPackages() {
        final byte[] data = "all packages".getBytes();
        new BlockingStorage(this.storage).save(new AllPackages(), data);
        final Response response = this.php.response(
            PhpComposerTest.GET_PACKAGES,
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new AllOf<>(
                Arrays.asList(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasBody(data)
                )
            )
        );
    }

    @Test
    void shouldFailGetAllPackagesWhenNotExists() {
        final Response response = this.php.response(
            PhpComposerTest.GET_PACKAGES,
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            response,
            new RsHasStatus(RsStatus.NOT_FOUND)
        );
    }

    @Test
    void shouldPutRoot() throws Exception {
        final Response response = this.php.response(
            "PUT /base",
            Collections.emptyList(),
            Flowable.just(
                ByteBuffer.wrap(
                    ByteStreams.toByteArray(new ResourceOf("minimal-package.json").stream())
                )
            )
        );
        MatcherAssert.assertThat(
            "Package should be created by put",
            response,
            new RsHasStatus(RsStatus.CREATED)
        );
    }

    @Test
    void shouldFailGetRootFromNotBasePath() {
        final Response response = this.php.response(
            "GET /not-base",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            "Root resource from outside of base path should not be found",
            response,
            new RsHasStatus(RsStatus.NOT_FOUND)
        );
    }

    @Test
    void shouldFailGetRoot() {
        final Response response = this.php.response(
            "GET /base",
            Collections.emptyList(),
            Flowable.empty()
        );
        MatcherAssert.assertThat(
            "It should not be possible to get root resource",
            response,
            new RsHasStatus(RsStatus.METHOD_NOT_ALLOWED)
        );
    }
}
