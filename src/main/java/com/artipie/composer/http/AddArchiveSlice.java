/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/composer-adapter/blob/master/LICENSE.txt
 */
package com.artipie.composer.http;

import com.artipie.asto.Content;
import com.artipie.composer.Repository;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;

/**
 * Slice for adding a package to the repository in ZIP format.
 * See <a href="https://getcomposer.org/doc/05-repositories.md#artifact">Artifact repository</a>.
 * @since 0.4
 */
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
final class AddArchiveSlice implements Slice {
    /**
     * Composer HTTP for entry point.
     * See <a href="https://getcomposer.org/doc/04-schema.md#version">docs</a>.
     */
    public static final Pattern PATH = Pattern.compile(
        "^/(?<full>(?<name>[a-z0-9_.\\-]*)-(?<version>v?\\d+.\\d+.\\d+[-\\w]*).zip)$"
    );

    /**
     * Repository.
     */
    private final Repository repository;

    /**
     * Ctor.
     * @param repository Repository.
     */
    AddArchiveSlice(final Repository repository) {
        this.repository = repository;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        final RequestLineFrom rqline = new RequestLineFrom(line);
        final String uri = rqline.uri().getPath();
        final Matcher matcher = AddArchiveSlice.PATH.matcher(uri);
        final Response resp;
        if (matcher.matches()) {
            resp = new AsyncResponse(
                this.repository
                    .addArchive(
                        new Archive.Zip(
                            new Archive.Name(matcher.group("full"), matcher.group("version"))
                        ),
                        new Content.From(body)
                    )
                    .thenApply(nothing -> new RsWithStatus(RsStatus.CREATED))
            );
        } else {
            resp = new RsWithStatus(RsStatus.BAD_REQUEST);
        }
        return resp;
    }
}
