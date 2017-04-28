package org.onosproject.nai.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.net.ConnectivityIntentCommand;
import org.onosproject.nai.intent.NaiIntentService;
import org.onosproject.net.intent.Intent;

/**
 * Command to create nai intent on hosts.
 */
@Command(scope = "onos", name = "nai-intent-create",
        description = "change bandwidth for a port of a device")
public class NaiIntentCreateCommand extends ConnectivityIntentCommand {

    @Argument(index = 0, name = "srcHostIp", description = "source ip address",
            required = false, multiValued = false)
    protected String srcHostIp = null;

    @Argument(index = 1, name = "dstHostIp", description = "destination ip " +
            "address", required = false, multiValued = false)
    protected String dstHostIp = null;

    @Argument(index = 2, name = "bandwidth-value", description = "bandwidth " +
            "value in mbps",
            required = false, multiValued = false)
    protected String bw = null;

    @Option(name = "-p", aliases = "--priority", description = "Priority",
            required = false, multiValued = false)
    private int priority = Intent.DEFAULT_INTENT_PRIORITY;

    @Override
    protected void execute() {
        NaiIntentService adminService = get(NaiIntentService.class);
        Intent intent = adminService.addIntent(bw, srcHostIp, dstHostIp,
                                               priority);
        print("Nai intent created:\n%s", intent.toString());
    }
}
