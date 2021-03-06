/*
 *  Copyright 2017 LINE Corporation
 *
 *  LINE Corporation licenses this file to you under the Apache License,
 *  version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package com.linecorp.armeria.common.http;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.SessionProtocolProvider;

/**
 * {@link SessionProtocolProvider} that provides the HTTP-related {@link SessionProtocol}s.
 */
public final class HttpSessionProtocolProvider extends SessionProtocolProvider {

    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;

    @Override
    protected Set<Entry> entries() {
        return ImmutableSet.of(
                new Entry("http", false, false, HTTP_PORT),
                new Entry("https", true, false, HTTPS_PORT),
                new Entry("h1c", false, false, HTTP_PORT),
                new Entry("h1", true, false, HTTPS_PORT),
                new Entry("h2c", false, true, HTTP_PORT),
                new Entry("h2", true, true, HTTPS_PORT));
    }
}
