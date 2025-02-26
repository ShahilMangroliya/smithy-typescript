import type { DefaultExtensionConfiguration } from "@smithy/types";

import { getChecksumConfiguration, resolveChecksumRuntimeConfig } from "./checksum";

/**
 * @internal
 */
export type DefaultExtensionConfigType = Parameters<typeof getChecksumConfiguration>[0];

/**
 * @internal
 *
 * Helper function to resolve default extension configuration from runtime config
 */
export const getDefaultExtensionConfiguration = (runtimeConfig: DefaultExtensionConfigType) => {
  return {
    ...getChecksumConfiguration(runtimeConfig),
  };
};

/**
 * @deprecated use getDefaultExtensionConfiguration
 * @internal
 *
 * Helper function to resolve default extension configuration from runtime config
 */
export const getDefaultClientConfiguration = getDefaultExtensionConfiguration;

/**
 * @internal
 *
 * Helper function to resolve runtime config from default extension configuration
 */
export const resolveDefaultRuntimeConfig = (config: DefaultExtensionConfiguration) => {
  return {
    ...resolveChecksumRuntimeConfig(config),
  };
};
