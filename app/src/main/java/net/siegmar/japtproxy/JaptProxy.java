/**
 * Japt-Proxy: The JAVA(TM) based APT-Proxy
 *
 * Copyright (C) 2006-2008  Oliver Siegmar <oliver@siegmar.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.siegmar.japtproxy;

import net.siegmar.japtproxy.exception.HandlingException;
import net.siegmar.japtproxy.exception.InvalidRequestException;
import net.siegmar.japtproxy.exception.UnknownBackendException;
import net.siegmar.japtproxy.misc.Backend;
import net.siegmar.japtproxy.misc.BackendType;
import net.siegmar.japtproxy.misc.Configuration;
import net.siegmar.japtproxy.misc.HttpHeaderConstants;
import net.siegmar.japtproxy.misc.RequestedData;
import net.siegmar.japtproxy.packages.RepoPackageFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * The JaptProxy is the starting point for Japt-Proxy.
 *
 * @author Oliver Siegmar
 */
public class JaptProxy {

    /**
     * The logger instance.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JaptProxy.class);

    private Configuration configuration;

    private Map<BackendType, RepoPackageFinder> repoPackageFinders;

    @Required
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Required
    public void setRepoPackageFinders(final Map<BackendType, RepoPackageFinder> repoPackageFinders) {
        this.repoPackageFinders = repoPackageFinders;
    }

    /**
     * Handles the incoming request.
     *
     * @param req the HttpServletRequest object.
     * @param res the HttpServletResponse object
     * @throws HandlingException is thrown if a handling error occurs.
     * @throws IOException       is thrown if an I/O error occurs.
     */
    public void handleRequest(final HttpServletRequest req, final HttpServletResponse res)
        throws HandlingException, IOException {
        final RequestedData requestedData = buildRequestedData(req);

        LOG.debug("Gathered information from request: {}", requestedData);

        final String requestedBackend = requestedData.getRequestedBackend();

        final Backend backend = configuration.getBackend(requestedBackend);

        if (backend == null) {
            throw new UnknownBackendException("No matching backend found for request: " + requestedData);
        }

        final RepoPackageFinder rpf = repoPackageFinders.get(backend.getType());

        if (rpf == null) {
            // configuration should already prevent this
            throw new IllegalStateException("No RepoPackageFinder found for type: " + backend.getType());
        }

        rpf.findSendSave(requestedData, res);
    }

    /**
     * Analyzes (validates) request url and extract required information.
     *
     * @param request the object to populate with extracted information.
     * @throws net.siegmar.japtproxy.exception.InvalidRequestException is thrown if requested url is invalid.
     */
    private static RequestedData buildRequestedData(final HttpServletRequest request)
        throws InvalidRequestException {
        final String resource = request.getPathInfo();
        final RequestedData requestedData = new RequestedData();
        requestedData.setRequestedResource(resource);
        requestedData.setRequestModifiedSince(request.getDateHeader(HttpHeaderConstants.IF_MODIFIED_SINCE));

        requestedData.setUserAgent(request.getHeader(HttpHeaderConstants.USER_AGENT));

        final String requestedResource = requestedData.getRequestedResource();

        // Reject if no requested resource is specified
        if (requestedResource == null) {
            throw new InvalidRequestException("Rejected request because it doesn't contain a resource request");
        }

        // Reject requests that contain /../ for security reason
        if (requestedResource.contains("/../")) {
            throw new InvalidRequestException("Rejected request '" + requestedResource + "' because it contains /../");
        }

        // Reject requests that doesn't contain a backend
        final int endIdx = requestedResource.indexOf('/', 1);
        if (endIdx <= 1) {
            throw new InvalidRequestException("Rejected request '" + requestedResource +
                "' because it doesn't specify a backend");
        }

        // Reject requests that doesn't contain a target resource
        if (requestedResource.length() == endIdx + 1) {
            throw new InvalidRequestException("Rejected request '" + requestedResource +
                "' because it doesn't specify a target " + "resource");
        }

        // Extract the backend and target resource parts of the request
        final String requestedBackend = requestedResource.substring(1, endIdx);
        final String requestedTarget = requestedResource.substring(endIdx);

        // Set requestedData
        requestedData.setRequestedTarget(requestedTarget);
        requestedData.setRequestedBackend(requestedBackend);

        return requestedData;
    }

}
