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
package org.trustedanalytics.uaa;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import lombok.AllArgsConstructor;
import org.cloudfoundry.identity.uaa.rest.SearchResults;
import org.cloudfoundry.identity.uaa.scim.ScimGroup;
import org.cloudfoundry.identity.uaa.scim.ScimGroupMember;
import org.cloudfoundry.identity.uaa.scim.ScimUser;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;
import org.trustedanalytics.org.cloudfoundry.identity.uaa.scim.ScimUserFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@AllArgsConstructor
public class UaaClient implements UaaOperations {

    private final String uaaBaseUrl;

    private final RestOperations uaaRestTemplate;

    @Override
    public ScimUser createUser(String username, String password) {
        return uaaRestTemplate
            .postForObject(uaaBaseUrl + "/Users", ScimUserFactory.newUser(username,
                    password), ScimUser.class);
    }

    @Override
    public ScimGroupMember addUserToGroup(ScimGroup group, String userGuid) {
        Map<String, Object> pathVars = ImmutableMap.of("groupId", group.getId());
        return uaaRestTemplate.postForObject(uaaBaseUrl + "/Groups/{groupId}/members",
                new ScimGroupMember(userGuid), ScimGroupMember.class, pathVars);
    }

    @Override
    public void removeUserFromGroup(ScimGroup group, String userGuid) {
        Map<String, String> pathVars = ImmutableMap.of("groupId", group.getId(), "userGuid", userGuid);
        uaaRestTemplate.delete(uaaBaseUrl + "/Groups/{groupId}/members/{userGuid}", pathVars);
    }

    @Override
    public SearchResults<ScimUser> getUsers() {
        ResponseEntity<SearchResults<ScimUser>> result = uaaRestTemplate.exchange(
            uaaBaseUrl + "/Users",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<SearchResults<ScimUser>>() {
            });

        return result.getBody();
    }

    @Override
    public void deleteUser(String userGuid) {
        uaaRestTemplate.delete(uaaBaseUrl + "/Users/{id}", userGuid);
    }
    
    @Override
    public Collection<UserIdNamePair> findUserNames(Collection<String> users) {
        String filter = users.stream()
                .map(uuid -> "Id eq \"" + uuid + "\"")
                .collect(joining(" or "));
        
        String path = uaaBaseUrl + "/Users?attributes=id,userName&filter=" + filter;
        return uaaRestTemplate.getForObject(path, UserIdNameList.class).getUsers();
    }

    @Override
    public Optional<ScimGroup> getGroup(String groupName) {
        String query = uaaBaseUrl + "/Groups?filter=displayName eq '{groupName}'&startIndex=1";
        Map<String, Object> pathVars = ImmutableMap.of("groupName", groupName);
        return Optional.ofNullable(uaaRestTemplate.getForObject(query, ScimGroup.class, pathVars));
    }
    
    @Override
    public void changePassword(String guid, ChangePasswordRequest request) {
        uaaRestTemplate.put(uaaBaseUrl + "/Users/{id}/password", request, guid);
    }

    @Override
    public Optional<UserIdNamePair> findUserIdByName(String userName) {
        String query = "/Users?attributes=id,userName&filter=userName eq '{name}'";
        Map<String, Object> pathVars = ImmutableMap.of("name", userName);
        UserIdNameList result = uaaRestTemplate.getForObject(uaaBaseUrl + query, UserIdNameList.class, pathVars);
        return Optional.ofNullable(Iterables.getFirst(result.getUsers(), null));
    }

    @Override
    public String getUaaHealth() {
        return uaaRestTemplate.getForObject(uaaBaseUrl + "/healthz", String.class);
    }
}
