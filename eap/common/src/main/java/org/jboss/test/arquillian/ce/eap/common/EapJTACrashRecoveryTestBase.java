/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.arquillian.ce.eap.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Set;

import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.ce.api.OpenShiftHandle;
import org.jboss.arquillian.ce.api.OpenShiftResource;
import org.jboss.arquillian.ce.api.OpenShiftResources;
import org.jboss.arquillian.ce.api.RoleBinding;
import org.jboss.arquillian.ce.httpclient.HttpClient;
import org.jboss.arquillian.ce.httpclient.HttpClientBuilder;
import org.jboss.arquillian.ce.httpclient.HttpClientExecuteOptions;
import org.jboss.arquillian.ce.httpclient.HttpRequest;
import org.jboss.arquillian.ce.httpclient.HttpResponse;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.runner.RunWith;

import io.undertow.util.StatusCodes;

import org.junit.Test;

/**
 * @author Douglas Palmer
 */
@RunWith(Arquillian.class)
@RoleBinding(roleRefName = "view", userName = "system:serviceaccount:${kubernetes.namespace}:default")
@OpenShiftResources({
    @OpenShiftResource("classpath:tx-internal-imagestream.json") // custom dev imagestream; remove when image is in prod
})
public class EapJTACrashRecoveryTestBase {

    private static final long ITERATION_SLEEP = 2000L;
    private static final int MAX_ITERATIONS = 100;
    private static final String RECOVERY_SUCCEEDED_MSG = "JTA Crash Record Quickstart: key value pair updated via JMS";

    @RouteURL("eap-app") private URL url;
    @ArquillianResource OpenShiftHandle adapter;
    private String trans1 = "/jboss-jta-crash-rec/XA?key=test&value=value&submit=Submit";
    private String trans2 = "/jboss-jta-crash-rec/XA?key=XXX&value=XXX&submit=Submit";

    @Test
    @RunAsClient
    public void testCrashRecovery() throws Exception {
        HttpClientExecuteOptions execOptions = new HttpClientExecuteOptions.Builder().desiredStatusCode(200).build();
        HttpClient client = HttpClientBuilder.untrustedConnectionClient();

        HttpRequest request = HttpClientBuilder.doGET(url + trans1);
        HttpResponse response = client.execute(request, execOptions);
        final String firstResponse = response.getResponseBodyAsString();
        assertTrue(firstResponse, firstResponse.contains("<tr><td>test</td><td>value updated via JMS</td>"));

        request = HttpClientBuilder.doGET(url + trans2);
        response = client.execute(request, execOptions);
        assertEquals("Gateway Timeout not received", StatusCodes.GATEWAY_TIME_OUT, response.getResponseCode());

        adapter.scaleDeployment("eap-app", 0);

        // Wait for transaction recovery to happen
        assertTrue("Recovery did not happen within the timeout period", waitForRecovery());
    }

    private boolean waitForRecovery() throws Exception {
        for(int count = 0 ; count < MAX_ITERATIONS ; count++) {
            final Set<String> readyPods = adapter.getReadyPods("eap-app-migration");
            if (!readyPods.isEmpty()) {
                final String recoveryLog = adapter.getLog(readyPods.iterator().next());
                if (recoveryLog != null) {
                    if (recoveryLog.contains(RECOVERY_SUCCEEDED_MSG)) {
                        return true;
                    }
                }
            }
            Thread.sleep(ITERATION_SLEEP);
        }
        return false;
    }
}
