/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.nai.web.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.CoreService;
import org.onosproject.nai.intent.NaiIntentService;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentId;
import org.onosproject.net.intent.Key;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static javax.ws.rs.core.Response.Status.OK;
import static org.onlab.util.Tools.nullIsIllegal;
import static org.onlab.util.Tools.nullIsNotFound;

/**
 * Query and program nai intent.
 */
@Path("intent")
public class NaiIntentWebResource extends AbstractWebResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String BANDWIDTH = "bandwidth";
    private static final String SRC_HOST_IP = "srcHostIp";
    private static final String DST_HOST_IP = "dstHostIp";
    private static final String PRIORITY = "priority";
    private static final String MISSING_MEMBER_MESSAGE = " member is required" +
            " in nai intent.";
    public static final String NAI_INTENT_NOT_FOUND = "intent not found";

    /**
     * Get all intent created.
     *
     * @return 200 OK
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIntents() {
        Iterable<Intent> iterable = get(NaiIntentService.class)
                .getIntents();
        ObjectNode result = mapper().createObjectNode();
        ArrayNode intents = result.putArray("nai_intent");
        if (iterable != null) {
            for (final Intent intent : iterable) {
                intents.add(codec(Intent.class).encode(intent, this));
            }
        }
        return ok(result.toString()).build();
    }

    /**
     * Get details of a intent.
     *
     * @param id key
     * @return 200 OK , 404 if given identifier does not exist
     */
    @GET
    @Path("{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIntent(@PathParam("key") String id) {
        Intent intent = nullIsNotFound(get(
                NaiIntentService.class).getIntent(Key.of(
                Long.parseLong(id), get(CoreService.class)
                        .getAppId("org.onosproject.nai.intent"))),
                                       NAI_INTENT_NOT_FOUND);

        ObjectNode result = mapper().createObjectNode();
        result.set("intent", codec(Intent.class).encode(intent, this));
        return ok(result.toString()).build();
    }

    /**
     * Creates and stores a new intent.
     *
     * @param stream intent from JSON
     * @return status of the request - CREATED if the JSON is correct,
     * BAD_REQUEST if the JSON is invalid
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createIntent(InputStream stream) {
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
            JsonNode flow = jsonTree.get("nai_intent");

            Intent intent = decode((ObjectNode) flow, true);
            Boolean issuccess = get(NaiIntentService.class)
                    .getIntent(intent.key()) != null;
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (IOException ex) {
            log.error("Exception while creating nai intent {}.", ex.toString());
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Update intent.
     *
     * @param stream InputStream
     * @return 200 OK, 404 if given identifier does not exist
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateIntent(final InputStream stream) {
        try {

            JsonNode jsonTree = mapper().readTree(stream);
            JsonNode flow = jsonTree.get("nai_intent");

            Intent intent = decode((ObjectNode) flow, false);
            Boolean issuccess = get(NaiIntentService.class)
                    .getIntent(intent.key()) != null;
            return Response.status(OK).entity(issuccess.toString()).build();
        } catch (IOException e) {
            log.error("Update intent failed because of exception {}.", e.toString());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Reverts intent.
     *
     * @param id intent id
     * @return 204 NO CONTENT
     */
    @Path("{intent_id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response revetIntent(@PathParam("intent_id") String id) {
        IntentId intentId = IntentId.valueOf(id);
        Intent intent = get(NaiIntentService.class)
                .reinstallPreviousIntent(intentId);
        return Response.status(OK).entity(intent != null).build();
    }

    /**
     * delete all intent.
     *
     * @return 204 NO CONTENT
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAll() {
        boolean v = get(NaiIntentService.class).deleteAll();
        return Response.status(OK).entity(v).build();
    }

    private Intent decode(ObjectNode json, boolean isAdd) {

        if (json == null || !json.isObject()) {
            return null;
        }

        NaiIntentService intentService = AbstractShellCommand.get(
                NaiIntentService.class);

        String srcHostIp = nullIsIllegal(json.get(
                SRC_HOST_IP), SRC_HOST_IP + MISSING_MEMBER_MESSAGE).asText();
        String dstHostIp = nullIsIllegal(json.get(
                DST_HOST_IP), DST_HOST_IP + MISSING_MEMBER_MESSAGE).asText();
        String bw = nullIsIllegal(json.get(
                BANDWIDTH), BANDWIDTH + MISSING_MEMBER_MESSAGE).asText();

        int priority = Intent.DEFAULT_INTENT_PRIORITY;
        if (json.get(PRIORITY) != null && !"null".equals((json.get(PRIORITY)).asText())) {
            priority = (json.get(PRIORITY)).asInt();
        }

        if (isAdd) {
            return intentService.addIntent(bw, srcHostIp, dstHostIp, priority);
        }
        return intentService.provideNewIntent(bw, srcHostIp, dstHostIp, priority);
    }
}
