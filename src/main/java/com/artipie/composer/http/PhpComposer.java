/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/composer-adapter/blob/master/LICENSE.txt
 */
package com.artipie.composer.http;

import com.artipie.composer.Repository;
import com.artipie.http.Slice;
import com.artipie.http.auth.Action;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.BasicAuthSlice;
import com.artipie.http.auth.Permission;
import com.artipie.http.auth.Permissions;
import com.artipie.http.rt.ByMethodsRule;
import com.artipie.http.rt.RtRule;
import com.artipie.http.rt.RtRulePath;
import com.artipie.http.rt.SliceRoute;
import java.util.regex.Pattern;

/**
 * PHP Composer repository HTTP front end.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class PhpComposer extends Slice.Wrap {
    /**
     * Ctor.
     * @param repository Repository
     * @param perms Access permissions
     * @param auth Authentication
     */
    public PhpComposer(
        final Repository repository,
        final Permissions perms,
        final Authentication auth) {
        super(
            new SliceRoute(
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.Any(
                            new RtRule.ByPath(PackageMetadataSlice.PACKAGE),
                            new RtRule.ByPath(PackageMetadataSlice.ALL_PACKAGES)
                        ),
                        ByMethodsRule.Standard.GET
                    ),
                    new BasicAuthSlice(
                        new PackageMetadataSlice(repository),
                        auth,
                        new Permission.ByName(perms, Action.Standard.READ)
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(Pattern.compile("^/?artifacts/.*\\.zip$")),
                        ByMethodsRule.Standard.GET
                    ),
                    new BasicAuthSlice(
                        new DownloadArchiveSlice(repository),
                        auth,
                        new Permission.ByName(perms, Action.Standard.READ)
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(AddSlice.PATH_PATTERN),
                        ByMethodsRule.Standard.PUT
                    ),
                    new BasicAuthSlice(
                        new AddSlice(repository),
                        auth,
                        new Permission.ByName(perms, Action.Standard.WRITE)
                    )
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(AddArchiveSlice.PATH),
                        ByMethodsRule.Standard.PUT
                    ),
                    new BasicAuthSlice(
                        new AddArchiveSlice(repository),
                        auth,
                        new Permission.ByName(perms, Action.Standard.WRITE)
                    )
                )
            )
        );
    }

    /**
     * Ctor with existing front and default parameters for free access.
     * @param repository Repository
     */
    public PhpComposer(final Repository repository) {
        this(repository, Permissions.FREE, Authentication.ANONYMOUS);
    }
}
