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

import com.google.common.annotations.Beta;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onlab.util.Bandwidth;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentData;
import org.onosproject.net.intent.IntentId;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.onlab.osgi.DefaultServiceDirectory.getService;
import static org.onosproject.net.intent.Intent.DEFAULT_INTENT_PRIORITY;
import static org.onosproject.net.intent.IntentState.FAILED;
import static org.onosproject.net.intent.IntentState.INSTALLED;
import static org.onosproject.net.intent.IntentState.WITHDRAWN;

/**
 * Represents implementation of NAI intent manager.
 */
@Beta
@Service
@Component(immediate = true)
public class NaiIntentManager implements NaiIntentService {

    private static final String APP_ID = "org.onosproject.nai.intent";
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    private ApplicationId appId;
    private final ConcurrentMap<String, Host> hostStore = new ConcurrentHashMap<>();
    private final ConcurrentMap<IntentId, Intent> intentStore = new ConcurrentHashMap<>();
    private int key;

    @Activate
    public void activate() {
        appId = coreService.registerApplication(APP_ID);
        storeHosts();
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    private void storeHosts() {
        for (Host host : hostService.getHosts()) {
            for (IpAddress ip : host.ipAddresses()) {
                hostStore.put(ip.toString(), host);
            }
        }
    }

    @Override
    public Intent addIntent(String bandwidth, String srcHostIp, String
            dstHostIp, int priority) {
        Host src = hostStore.get(srcHostIp);
        Host dst = hostStore.get(dstHostIp);
        if (intentStore.isEmpty()) {
            return create(bandwidth, src, dst, priority);
        }
        return provideNewIntent(bandwidth, srcHostIp, dstHostIp, priority);
    }

    @Override
    public Intent provideNewIntent(String bandwidth, String srcHostIp,
                                   String dstHostIp, int priority) {
        Host src = hostStore.get(srcHostIp);
        Host dst = hostStore.get(dstHostIp);

        deleteAll();
        return create(bandwidth, src, dst, priority);
    }

    @Override
    public boolean deleteAll() {
        Intent intent;
        IntentState state;
        for (Map.Entry<IntentId, Intent> entry : intentStore.entrySet()) {
            intent = entry.getValue();
            intentService.withdraw(intent);
            state = intentService.getIntentState(intent.key());
            //wait until all intents are withdrawn.
            while (state != WITHDRAWN) {
                state = intentService.getIntentState(intent.key());
            }
            intentService.purge(intent);
            intentStore.remove(intent.id());
        }
        return intentStore.isEmpty();
    }

    /**
     * Creates intent and installs it.
     *
     * @param bandwidth bandwidth value
     * @param a         source host id
     * @param b         destination host id
     * @param priority  priority
     * @return intent id
     */
    private Intent create(String bandwidth, Host a, Host b, int priority) {
        IntentService service = getService(IntentService.class);

        HostId oneId = a.id();
        HostId twoId = b.id();

        TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();
        List<Constraint> constraints = new ArrayList<>();

        if (!isNullOrEmpty(bandwidth)) {
            Bandwidth bw;
            try {
                bw = Bandwidth.mbps(Long.parseLong(bandwidth));
                // when the string can't be parsed as long, then try to parse as double
            } catch (NumberFormatException e) {
                bw = Bandwidth.mbps(Double.parseDouble(bandwidth));
            }
            constraints.add(new BandwidthConstraint(bw));
        }

        Key k = Key.of(String.valueOf(key), appId);

        if (priority == 0) {
            priority = DEFAULT_INTENT_PRIORITY;
        }

        HostToHostIntent forward = HostToHostIntent.builder()
                .appId(appId)
                .key(k)
                .one(oneId)
                .two(twoId)
                .selector(selector)
                .treatment(treatment)
                .constraints(constraints)
                .priority(priority)
                .resourceGroup(null)
                .build();
        service.submit(forward);

        //wait until intent get installed/failed.
        if (waitForIntent(k) == INSTALLED) {
            key++;
            intentStore.put(forward.id(), forward);
            log.info("forward intent has been installed.");
        }

        //Adding reverse intent.
        k = Key.of(String.valueOf(key), appId);
        HostToHostIntent reverse = HostToHostIntent.builder()
                .appId(appId)
                .key(k)
                .one(twoId)
                .two(oneId)
                .selector(selector)
                .treatment(treatment)
                .constraints(constraints)
                .priority(priority)
                .resourceGroup(null)
                .build();
        service.submit(reverse);

        //wait until intent get installed/failed.
        if (waitForIntent(k) == INSTALLED) {
            key++;
            intentStore.put(reverse.id(), reverse);
            log.info("reverse intent has been installed.");
        }
        return forward;
    }

    /**
     * Waits for intent to get installed/failed.
     *
     * @param k key for intent
     * @return intent state
     */
    private IntentState waitForIntent(Key k) {
        IntentState state = intentService.getIntentState(k);
        while (state != INSTALLED) {
            state = intentService.getIntentState(k);
            if (state == FAILED) {
                log.error("intent failed to install.");
                break;
            }
        }
        return state;
    }

    @Override
    public void addListener(IntentListener listener) {
        intentService.addListener(listener);
    }

    @Override
    public void removeListener(IntentListener listener) {
        intentService.removeListener(listener);
    }

    @Override
    public void submit(Intent intent) {
        intentService.submit(intent);
    }

    @Override
    public void withdraw(Intent intent) {
        intentService.withdraw(intent);
    }

    @Override
    public void purge(Intent intent) {
        intentService.purge(intent);
    }

    @Override
    public Intent getIntent(Key key) {
        return intentService.getIntent(key);
    }

    @Override
    public Iterable<Intent> getIntents() {
        return intentService.getIntents();
    }

    @Override
    public void addPending(IntentData intentData) {
        intentService.addPending(intentData);
    }

    @Override
    public Iterable<IntentData> getIntentData() {
        return intentService.getIntentData();
    }

    @Override
    public long getIntentCount() {
        return intentService.getIntentCount();
    }

    @Override
    public IntentState getIntentState(Key intentKey) {
        return intentService.getIntentState(intentKey);
    }

    @Override
    public List<Intent> getInstallableIntents(Key intentKey) {
        return intentService.getInstallableIntents(intentKey);
    }

    @Override
    public boolean isLocal(Key intentKey) {
        return intentService.isLocal(intentKey);
    }

    @Override
    public Iterable<Intent> getPending() {
        return intentService.getPending();
    }
}
