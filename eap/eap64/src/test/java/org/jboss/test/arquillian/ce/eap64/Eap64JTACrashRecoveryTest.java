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

package org.jboss.test.arquillian.ce.eap64;

import org.jboss.arquillian.ce.api.Template;
import org.jboss.arquillian.ce.api.TemplateParameter;
import org.jboss.test.arquillian.ce.eap.common.EapJTACrashRecoveryTestBase;

/**
 * @author Douglas Palmer
 */
@Template(url = "https://raw.githubusercontent.com/${quickstart.repository:jboss-openshift}/openshift-quickstarts/${quickstart.branch:master}/jta-crash-rec-eap6/eap64-qs-test-s2i.json", parameters = {
        @TemplateParameter(name = "IMAGE_STREAM_NAMESPACE", value = "${kubernetes.namespace}"),
        @TemplateParameter(name = "SOURCE_REPOSITORY_URL", value = "https://github.com/${quickstart.repository:jboss-openshift}/openshift-quickstarts"),
        @TemplateParameter(name = "SOURCE_REPOSITORY_REF", value = "${quickstart.branch:master}"),
        @TemplateParameter(name = "CONTEXT_DIR", value = "jta-crash-rec-eap6")})

public class Eap64JTACrashRecoveryTest extends EapJTACrashRecoveryTestBase {

}
