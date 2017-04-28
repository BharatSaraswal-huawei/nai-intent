# nai-intent

Step:1: terminal 1:
cd ~/onos/tools/dev/mininet
sudo mn --custom onos.py --controller onos,1 --topo torus,3,3

Step2: on mininet console. run below command. onos will detect hosts after this.
    pingall

Step3: terminal 2:
    cd /tmp/onos1/karaf/bin
    client -h 192.168.123.

Step4: activate nai app on onos cli/UI:

      app activate org.onosproject.nai.intent

Step5: in onos cli to configure bandwidth value for each port. of each device. port 1 should have more bandwidth then others.(value in mbps)

    port-bw-config of:0000000000000101 1 1000
    port-bw-config of:0000000000000101 2 100
    port-bw-config of:0000000000000101 3 100
    port-bw-config of:0000000000000101 4 100
    port-bw-config of:0000000000000101 5 100

    port-bw-config of:0000000000000102 1 1000
    port-bw-config of:0000000000000102 2 100
    port-bw-config of:0000000000000102 3 100
    port-bw-config of:0000000000000102 4 100
    port-bw-config of:0000000000000102 5 100

    port-bw-config of:0000000000000103 1 1000
    port-bw-config of:0000000000000103 2 100
    port-bw-config of:0000000000000103 3 100
    port-bw-config of:0000000000000103 4 100
    port-bw-config of:0000000000000103 5 100

    port-bw-config of:0000000000000201 1 1000
    port-bw-config of:0000000000000201 2 100
    port-bw-config of:0000000000000201 3 100
    port-bw-config of:0000000000000201 4 100
    port-bw-config of:0000000000000201 5 100

    port-bw-config of:0000000000000202 1 1000
    port-bw-config of:0000000000000202 2 100
    port-bw-config of:0000000000000202 3 100
    port-bw-config of:0000000000000202 4 100
    port-bw-config of:0000000000000202 5 100

    port-bw-config of:0000000000000203 1 1000
    port-bw-config of:0000000000000203 2 100
    port-bw-config of:0000000000000203 3 100
    port-bw-config of:0000000000000203 4 100
    port-bw-config of:0000000000000203 5 100

    port-bw-config of:0000000000000301 1 1000
    port-bw-config of:0000000000000301 2 100
    port-bw-config of:0000000000000301 3 100
    port-bw-config of:0000000000000301 4 100
    port-bw-config of:0000000000000301 5 100

    port-bw-config of:0000000000000302 1 1000
    port-bw-config of:0000000000000302 2 100
    port-bw-config of:0000000000000302 3 100
    port-bw-config of:0000000000000302 4 100
    port-bw-config of:0000000000000302 5 100

    port-bw-config of:0000000000000303 1 1000
    port-bw-config of:0000000000000303 2 100
    port-bw-config of:0000000000000303 3 100
    port-bw-config of:0000000000000303 4 100
    port-bw-config of:0000000000000303 5 100

to verify this : will show all the resources for device 303.

    resources of:0000000000000303

------------------------------------------------------
Step6:
A. post an intent.

Note: here value for src host ip and dst host ip will change as per the hostIp in onos.

http://192.168.123.1:8181/onos/nai/nai

    json: 'POST'
    {
        "nai_intent":{
        "bandwidth" : "100",
        "srcHostIp":"10.0.0.1",
        "dstHostIp":"10.0.0.5",
            "priority" : "100"
        }
    }

----------------------------------------------

B. update an intent.

Note: here value for src host ip and dst host ip will change as per the hostIp in onos.

http://192.168.123.1:8181/onos/nai/nai

    json: 'PUT'
    {
        "nai_intent":{
        "bandwidth" : "50",
        "srcHostIp":"10.0.0.1",
        "dstHostIp":"10.0.0.5"
        }
    }

----------------------------------------------

C. revert an intent.

Note: here value for intent id in end may change based on the intent id.

http://192.168.123.1:8181/onos/nai/nai/0x0  

'DELETE'


----------------------------------------------

D. Delete all intent.

http://192.168.123.1:8181/onos/nai/nai/

'DELETE'

