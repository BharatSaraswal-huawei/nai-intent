package org.onosproject.nai.cli;

import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractChoicesCompleter;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.Host;
import org.onosproject.net.host.HostService;

import java.util.ArrayList;
import java.util.List;

/**
 * Host ip completer.
 */
public class HostIpCompleter extends AbstractChoicesCompleter {

    @Override
    protected List<String> choices() {

        HostService service = AbstractShellCommand.get(HostService.class);
        List<String> id = new ArrayList<>();
        for (Host host : service.getHosts()) {
            for (IpAddress ip : host.ipAddresses()) {
                id.add(ip.toString());
            }
        }
        return id;
    }
}
