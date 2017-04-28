/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.nai.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.util.Bandwidth;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.BandwidthCapacity;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.resource.Resource;
import org.onosproject.net.resource.ResourceAdminService;
import org.onosproject.net.resource.Resources;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Command to change the bandwidth configurations for a port of a device.
 */
@Command(scope = "onos", name = "port-bw-config",
        description = "change bandwidth for a port of a device")
public class PortBandwidthConfigCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "uri", description = "Device ID",
            required = false, multiValued = false)
    protected String uri = null;

    @Argument(index = 1, name = "port-number", description = "port-number",
            required = false, multiValued = false)
    protected String port = null;

    @Argument(index = 2, name = "bandwidth-value", description = "bandwidth " +
            "value in mbps",
            required = false, multiValued = false)
    protected String bw = null;


    @Override
    protected void execute() {
        ResourceAdminService adminService = get(ResourceAdminService.class);
        NetworkConfigService netCfgService = get(NetworkConfigService.class);
        DeviceService deviceService = get(DeviceService.class);

        DeviceId did = DeviceId.deviceId(uri);
        Bandwidth bandwidth = Bandwidth.mbps(0);
        PortNumber number = PortNumber.portNumber(port);

        Resource resource = Resources.discrete(did, number).resource();

        //get the bandwidth
        if (!isNullOrEmpty(bw)) {
            try {
                bandwidth = Bandwidth.mbps(Long.parseLong(bw));
                // when the string can't be parsed as long, then try to parse as double
            } catch (NumberFormatException e) {
                bandwidth = Bandwidth.mbps(Double.parseDouble(bw));
            }
        }

        //check for these resources.
        if (!adminService.register(resource)) {
            log.error("Failed to register Port: {}", number);
        }

        //check for previous config
        ConnectPoint cp = new ConnectPoint(did, number);
        BandwidthCapacity config = netCfgService.getConfig(
                cp, BandwidthCapacity.class);

        //register new config.
        if (config != null) {
            log.trace("Registering configured bandwidth {} for {}/{}", config.capacity(), did);

            //unreg with current config.
            register(resource, adminService, config.capacity(), bandwidth);
        } else {

            //unreg with port speed config
            Port port = deviceService.getPort(did, number);
            if (port != null) {
                register(resource, adminService, Bandwidth.mbps(
                        port.portSpeed()), bandwidth);
            }
        }
    }

    private void register(Resource resource, ResourceAdminService
            adminService, Bandwidth bandwidth, Bandwidth bandwidth2) {
        Resource r = resource.child(Bandwidth.class, bandwidth.bps());
        if (adminService.unregister(r.id())) {
            log.info("unregistered old bandwidth");
            Resource nr = resource.child(Bandwidth.class, bandwidth2.bps());
            if (adminService.register(nr)) {
                print("configured bandwidth for the port.", bandwidth2.bps());
            }
        }
    }
}
