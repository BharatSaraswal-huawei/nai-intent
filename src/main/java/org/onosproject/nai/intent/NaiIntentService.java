/*
 * Copyright 2017-present Open Networking Laboratory
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
package org.onosproject.nai.intent;

import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentId;
import org.onosproject.net.intent.IntentService;

/**
 * Represents NAI intent service.
 */
public interface NaiIntentService extends IntentService {

    /**
     * Adds a fresh intent between two hosts. but does not delete intent if
     * already present.
     *
     * @param bandwidth bandwidth value in mbps
     * @param srcHostIp source ip address
     * @param dstHostIp destination ip address
     * @param priority  intent priority
     * @return intent id
     */
    Intent addIntent(String bandwidth, String srcHostIp, String dstHostIp,
                     int priority);

    /**
     * Provides a new intent between two host. also deletes previous intent.
     *
     * @param bandwidth bandwidth value in mbps
     * @param srcHostIp source ip address
     * @param dstHostIp destination ip address
     * @param priority  intent priority
     * @return intent id
     */
    Intent provideNewIntent(String bandwidth, String srcHostIp, String
            dstHostIp, int priority);

    /**
     * Reinstall previously installed intent.
     *
     * @param id intent id
     */
    Intent reinstallPreviousIntent(IntentId id);

    /**
     * Deletes all intents.
     */
    boolean deleteAll();
}
