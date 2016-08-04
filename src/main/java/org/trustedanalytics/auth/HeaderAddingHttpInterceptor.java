/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.auth;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.io.IOException;

@EqualsAndHashCode
@AllArgsConstructor
public class HeaderAddingHttpInterceptor implements ClientHttpRequestInterceptor {

    private final String headerKey;
    private final String headerValue;

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);
        requestWrapper.getHeaders().set(headerKey, headerValue);

        return clientHttpRequestExecution.execute(requestWrapper, bytes);
    }
}
