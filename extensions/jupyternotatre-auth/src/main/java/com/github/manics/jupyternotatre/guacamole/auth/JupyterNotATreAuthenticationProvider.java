/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.manics.jupyternotatre.guacamole.auth;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.apache.guacamole.net.auth.Credentials;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication provider implementation that configures a single server using
 * environment variables.
 *
 * The follow environment variables are supported
 * - PROTOCOL: The protocol to use (e.g. rdp, vnc)
 * - HOSTNAME: The hostname of the server
 * - USERNAME: The username to use (optional)
 * - PASSWORD: The password to use (optional)
 * - PORT: The port to use (optional)
 * - DISABLE_COPY: Disable copy (e.g. true, false, default false)
 * - DISABLE_PASTE: Disable paste (e.g. true, false, default false)
 */
public class JupyterNotATreAuthenticationProvider extends SimpleAuthenticationProvider {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(JupyterNotATreAuthenticationProvider.class);

    @Override
    public String getIdentifier() {
        return "jupyternotatre";
    }

    @Override
    public Map<String, GuacamoleConfiguration> getAuthorizedConfigurations(Credentials credentials)
            throws GuacamoleException {
        // No authorisation, return a single configuration.
        Map<String, GuacamoleConfiguration> configs = new HashMap<String, GuacamoleConfiguration>();

        String protocol = System.getenv("PROTOCOL");
        String hostname = System.getenv("HOSTNAME");
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        String port = System.getenv("PORT");
        String disableCopy = System.getenv("DISABLE_COPY");
        String disablePaste = System.getenv("DISABLE_PASTE");

        if (protocol == null || protocol.isEmpty() || hostname == null || hostname.isEmpty()) {
            // Equivalent to unauthorised
            logger.debug("Insufficient parameters");
            return null;
        }

        if (port == null) {
            if (protocol.equals("rdp")) {
                port = "3389";
            } else if (protocol.equals("vnc")) {
                port = "5901";
            } else {
                return null;
            }
        }

        // Create new configuration
        GuacamoleConfiguration config = new GuacamoleConfiguration();

        config.setProtocol(protocol);
        config.setParameter("hostname", hostname);
        config.setParameter("port", port);
        if (username != null) {
            config.setParameter("username", username);
        }
        if (password != null) {
            config.setParameter("password", password);
        }

        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "any");
        config.setParameter("resize-method", "display-update");
        config.setParameter("server-layout", "en-gb-qwerty");

        if (BooleanUtils.toBoolean(disableCopy)) {
            config.setParameter("disable-copy", "true");
        }
        if (BooleanUtils.toBoolean(disablePaste)) {
            config.setParameter("disable-paste", "true");
        }

        logger.debug(config.getParameters().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(" ")));

        configs.put("default", config);
        return configs;
    }
}