Show my network state
=====================


Description
-----------

"Show my network state" is a graphical, single-host, network topology viewer.

Official site: https://sites.google.com/site/showmynetworkstate/

<br>


The goal of this application is to graphically display the virtual/physical network topology inside a single host.
You can see additional info by moving the mouse over the network elements and waiting for the tooltip to appear.
Press F5 to refresh the graph or to save the current layout. The position of graph elements will be saved in a file named `.showmynslayout` in the home folder for the next launch. The network info is retrieved by parsing bash commands output. JRE (Java Runtime Environment) is  needed to run this application. It was compiled with OpenJDK 1.7.0_40 through Scala SBT. I've also added some basic functionality to add/remove network elements.



This application was born from the need to understand in deep the Openstack network internals: it's written in Scala, and the project source code can be managed with SBT and/or Scala IDE. 

NOTE: this program is not meant to be a replacement of command line network commands, but can be really helpful if you want to visualize the topology as a whole.


If you find the `ip address show` CLI command confusing I also made a shell alias to highlight interface names and IPs in the command output.
Put the following line in your .bashrc file:

`alias ipa="ip address show | grep --color=always  -E '([0-9]{1,3}\\.){3}[0-9]{1,3}/[0-9]{1,2}|$' | GREP_COLORS='mt=01;34' grep -P '(\s.+:\s)|$'"`


The main bash commands executed by the application are:

* `ip netns <namespace> exec ip address show`
* `ip netns <namespace> exec brctl show`
* `ip netns <namespace> exec ovs-vsctl show`
* `ip netns <namespace> exec brctl show`
* `ip netns <namespace> exec brctl delbr`
* `ip netns <namespace> exec brctl addbr`




IMPORTANT NOTES: read  this before executing!
---------------------------------------------

 

* using your packet manager you must install `ethtool`,`openvswitch` , `bridge-utils`

*  You can download an already compiled jar file from the [official site](https://sites.google.com/site/showmynetworkstate/). To run this program execute: 
     `sudo java -jar showMy1.jar`

   Scala and JGraphx library are already bundled in the jar through sbt assembly. If you want to repackage the application (with SBT and Scala installed) type `sbt assembly` in the root project folder, it will create a file named showMy1.jar in the target/scala-2.10  folder.
   


The following network elements are shown:

 * physical interface (ifaceType="physical") 
 * vlan alias ("vlan_alias")   created with `vconfig ..` or `sudo ip link add link ... type vlan ...` 
 * OVS internal interface ("ovsinternal"), optionally with a VLAN tag  created with `sudo ovs-vsctl add-port <bridge> <new vif> -- set Interface <new vif> type=internal`
 * l2tp tunnel interface ("l2tp")   created with `sudo ip l2tp add ....`
 * loopback interface ("loopback") 
 * linux bridge management interface ("linuxBridgeInterface")   created when you create a linux bridge ( `sudo brctl addbr ... `)
 * TUN/TAP interface ("tuntap")  created with `sudo ip tuntap add mode tap vnet0`
 * openvswitch system datapath interface ("ovs-system")
 * veth pair ("veth-pair")  created with `ip link add v0 type veth peer name v1`
 * gre tunnel ("gre")  created with "ovs-vsctl add-port ... set interface ... type=gre"
 * patch port  ("patch")   created with `ovs-vsctl add-port ... set interface ... type=patch ...`
 * other OVS port: a OVS port that doesn't fall in any of the previous categories. For example a not completely deleted patch port, that does not have anymore the type=patch attribute and  remains as garbage.


JSON OUTPUT
-----------

You can obtain the same informations in machine-readable format too, in the JSON format! Use the following flags:

  * `--ovsbridges`     show the Open vSwitch bridges
  * `--linuxbridges`   show the Linux Bridges  
  * `--ifaces`         show the network interfaces 
  * `--sessions`       show the L2TP sessions
  * `--tunnels`        show the L2TP tunnels

Note that you have to specify "ShowJSON" as the main class. Example:
  *  `sudo java -cp target/scala-2.10/showMy1.jar showmyns.ShowJSON --ifaces | python -m json.tool | less -S`

 
Limitations
-----------

* Only IPv4 addresses are shown. 

* The following elements are not shown in the graph:
 * the interface "ovs-system", because it's only an implementation detail of openvswitch (it serves as as a shared datapath for the ovs bridges).
 * loopback interfaces

* The application assumes loopback interface having  "lo" name.
* It assumes that vlan aliases are shown in the form "eth0.1234@eth0"


* Some OVS Bridges appear as linux bridges.
From packages.debian.org : 
"openvswitch-brcompat provides a way for applications that use the Linux bridge to gradually migrate to Open vSwitch. Programs that ordinarily control the Linux bridge module, such as "brctl", instead control the Open vSwitch kernel-based switch"


Troubleshooting
--------
The program terminates with the error: "seting the network namespace failed". 
It's a known namespace linux problem. Reboot the machine.

TODO
----

* Doing output parsing is a bad thing(the format may change tomorrow), however a Java API does not seem to exist for OVS. 
  * Retrieving interface info: http://java.dzone.com/news/network-interface-details-java
    However this API does not show all interfaces, only those that have an IP.
  * For OVS there seems to be a JSON RPC API
* maybe restructure the hierarchy. However problems arise if I introduce a parent abstract class for the Ifaces. The problem shows up when I try to drag graph elements.  As pointed in S.O. possible approaches are: 
  * case classes with a parent empty trait (under the hoods an empty trait is implemented as a java interface)
  * a hierarchy of normal (not case) classes 
* modify the "used iface" discovery mechanism for l2tp ifaces. Actually it works only if it's in the default namespace
 

On the interface naming
-----------------------
Depending on  "Consistent Network Device Naming" (CNDN) is enabled or not, interface names can be very different. 
In Fedora 18/19 CDND  is enabled by default.



VLAN tag
--------
* vlan alias created with `ip link .. tag ..`: the VLAN TAG is shown after the @ in the name
* OVS internal interfaces may have a VLAN tag. However VID doesn't appear in "ip address show" but only in `ovs-vsctl show`.



	
	
LICENSE 
-------
The license is BSD. See the license file.	
	
	



