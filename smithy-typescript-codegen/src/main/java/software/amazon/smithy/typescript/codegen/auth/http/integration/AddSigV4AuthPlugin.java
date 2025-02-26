/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.typescript.codegen.auth.http.integration;

import java.util.Optional;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.typescript.codegen.ApplicationProtocol;
import software.amazon.smithy.typescript.codegen.ConfigField;
import software.amazon.smithy.typescript.codegen.LanguageTarget;
import software.amazon.smithy.typescript.codegen.TypeScriptDependency;
import software.amazon.smithy.typescript.codegen.TypeScriptSettings;
import software.amazon.smithy.typescript.codegen.auth.http.HttpAuthOptionProperty;
import software.amazon.smithy.typescript.codegen.auth.http.HttpAuthOptionProperty.Type;
import software.amazon.smithy.typescript.codegen.auth.http.HttpAuthScheme;
import software.amazon.smithy.typescript.codegen.auth.http.HttpAuthSchemeParameter;
import software.amazon.smithy.utils.SmithyInternalApi;

/**
 * Support for generic @aws.auth#sigv4.
 *
 * This is the experimental behavior for `experimentalIdentityAndAuth`.
 */
@SmithyInternalApi
public final class AddSigV4AuthPlugin implements HttpAuthTypeScriptIntegration {

    /**
     * Integration should only be used if `experimentalIdentityAndAuth` flag is true.
     */
    @Override
    public boolean matchesSettings(TypeScriptSettings settings) {
        return settings.getExperimentalIdentityAndAuth();
    }

    @Override
    public Optional<HttpAuthScheme> getHttpAuthScheme() {
        return Optional.of(HttpAuthScheme.builder()
                .schemeId(ShapeId.from("aws.auth#sigv4"))
                .applicationProtocol(ApplicationProtocol.createDefaultHttpApplicationProtocol())
                .putDefaultIdentityProvider(LanguageTarget.SHARED, w -> {
                    w.write("async () => { throw new Error(\"`credentials` is missing\"); }");
                })
                .putDefaultSigner(LanguageTarget.SHARED, w -> {
                    w.addDependency(TypeScriptDependency.EXPERIMENTAL_IDENTITY_AND_AUTH);
                    w.addImport("SigV4Signer", null,
                        TypeScriptDependency.EXPERIMENTAL_IDENTITY_AND_AUTH);
                    w.write("new SigV4Signer()");
                })
                .addConfigField(new ConfigField("region", w -> {
                    w.addDependency(TypeScriptDependency.SMITHY_TYPES);
                    w.addImport("Provider", "__Provider", TypeScriptDependency.SMITHY_TYPES);
                    w.write("string | __Provider<string>");
                }, w -> w.write("The AWS region to which this client will send requests.")))
                .addConfigField(new ConfigField("credentials", w -> {
                    w.addDependency(TypeScriptDependency.SMITHY_TYPES);
                    w.addImport("AwsCredentialIdentity", null, TypeScriptDependency.SMITHY_TYPES);
                    w.addImport("AwsCredentialIdentityProvider", null, TypeScriptDependency.SMITHY_TYPES);
                    w.write("AwsCredentialIdentity | AwsCredentialIdentityProvider");
                }, w -> w.write("The credentials used to sign requests.")))
                .addHttpAuthSchemeParameter(new HttpAuthSchemeParameter(
                    "region", w -> w.write("string"), w -> {
                    w.addDependency(TypeScriptDependency.UTIL_MIDDLEWARE);
                    w.addImport("normalizeProvider", null, TypeScriptDependency.UTIL_MIDDLEWARE);
                    w.openBlock("await normalizeProvider(config.region)() || (() => {", "})()", () -> {
                        w.write("throw new Error(\"expected `region` to be configured for `aws.auth#sigv4`\");");
                    });
                }))
                .addHttpAuthOptionProperty(new HttpAuthOptionProperty(
                    "name", Type.SIGNING, t -> w -> {
                    w.write("$S", t.toNode().expectObjectNode().getMember("name"));
                }))
                .addHttpAuthOptionProperty(new HttpAuthOptionProperty(
                    "region", Type.SIGNING, t -> w -> {
                    w.write("authParameters.region");
                }))
                .build());
    }
}
