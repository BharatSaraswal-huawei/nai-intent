package org.onosproject.nai.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.nai.intent.NaiIntentService;
import org.onosproject.net.intent.Intent;


/**
 * Command to update nai intent on hosts.
 */
@Command(scope = "onos", name = "nai-intent-update",
        description = "creates a new intent and withdraw previous")
public class NaiIntentUpdateCommand extends NaiIntentCreateCommand {

    @Option(name = "-p", aliases = "--priority", description = "Priority",
            required = false, multiValued = false)
    private int priority = Intent.DEFAULT_INTENT_PRIORITY;

    @Override
    protected void execute() {
        NaiIntentService adminService = get(NaiIntentService.class);
        Intent intent = adminService.provideNewIntent(bw, srcHostIp, dstHostIp,
                                                      priority);
        print("Nai intent updated:\n%s", intent.toString());
    }
}
