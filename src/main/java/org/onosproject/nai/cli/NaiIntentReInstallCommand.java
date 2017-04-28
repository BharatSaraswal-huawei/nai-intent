package org.onosproject.nai.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.net.ConnectivityIntentCommand;
import org.onosproject.nai.intent.NaiIntentService;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentId;

/**
 * Command to update nai intent on hosts.
 */
@Command(scope = "onos", name = "nai-intent-reinstall",
        description = "reinstall a previous intent and withdraw current")
public class NaiIntentReInstallCommand extends ConnectivityIntentCommand {

    @Argument(index = 0, name = "intentId", description = "intent identifier",
            required = false, multiValued = false)
    protected String id = null;

    @Override
    protected void execute() {
        NaiIntentService adminService = get(NaiIntentService.class);
        Intent intent = adminService.reinstallPreviousIntent(
                IntentId.valueOf(id));
        print("Nai intent resubmitted:\n%s", intent.toString());
    }
}
