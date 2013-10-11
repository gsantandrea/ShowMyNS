
"Show my network state"
========================
The goal of this application is to graphically display the virtual/physical network topology inside a single host.
You can see additional info by moving the mouse over the network elements and waiting for the tooltip to appear.
Press F5 to refresh the graph. The position of graph elements is saved in a file named layout.json.
The network info is retrieved by parsing the bash commands output.
This application was born from the need to understand in deep the Openstack network internals.
It's written in scala, and the project can be used with sbt or Eclipse. 

NOTE: this program is not meant to be a replacement of command line network commands, but can be really helpful if you want to visualize the topology as a whole.
TIP: if you find the "ip address show" command confusing I also made a shell alias to highlight interface names and IPs in the command output.
Put the following in your .bashrc file:
alias ipa="ip address show | grep --color=always  -E '([0-9]{1,3}\\.){3}[0-9]{1,3}/[0-9]{1,2}|$' | GREP_COLORS='mt=01;34' grep -P '(\s.+:\s)|$'"


the bash commands executed are:
sudo ip netns <namespace> exec sudo ip address show
sudo ip netns <namespace> exec sudo brctl show



IMPORTANT NOTES: READ THIS BEFORE EXECUTING!!!
============================================
* To run this program the file /etc/sudoers must be modified by commenting the following line:
  Defaults    requiretty
  because the scala.sys.Process executes system commands with "sudo" 

* ethtool must be installed!

* If you don't want to insert password every time you have to add NOPASSWD configuration for your user in the /etc/sudoers file.
  If you use Fedora and belong to the sudoer group named wheel you have to modify the file like the following:
    %wheel ALL=(ALL)	NOPASSWD: ALL
  or in Ubuntu: 
    %sudo  ALL=(ALL)	NOPASSWD: ALL
  Use "groups" command to discover the groups you belong to. 
  Alternatively the sudo privilege may be related to your user name: 
    james  ALL=(ALL)	NOPASSWD: ALL


*  To run this program execute: 
     java -jar showMy1.jar
   (the Scala library is already bundled in the jar through sbt assembly )
   if you feel lucky you can try to use "-orglayout" parameter to visually organize the elements. No guarantees of success, though.
	 java -jar showMy1.jar -orglayout


The following network elements are shown:
	* physical interface (ifaceType="physical") 
	* vlan alias ("vlan_alias")
		created with "vconfig .." or "sudo ip link add link ... type vlan ..." 
	* OVS internal interface ("ovsinternal"), optionally with a VLAN tag 
		created with "sudo ovs-vsctl add-port <bridge> <new vif> -- set Interface <new vif> type=internal" 
	* l2tp tunnel interface ("l2tp")
		created with "sudo ip l2tp add ...."
	* loopback interface ("loopback") 
	* linux bridge management interface ("linuxBridgeInterface")
		created when you create a linux bridge ( "sudo brctl addbr ... ")
	* TUN/TAP interface ("tuntap")
		created with "sudo ip tuntap add mode tap vnet0"
	* openvswitch system datapath interface ("ovs-system")		
	* veth pair ("veth-pair")
		created with "ip link add v0 type veth peer name v1"
	* gre tunnel ("gre")
		created with "ovs-vsctl add-port ... set interface ... type=gre"
	* patch port  ("patch")
		created with "ovs-vsctl add-port ... set interface ... type=patch ..." 
	* other OVS port: a OVS port that doesn't fall in any of the previous categories. For example a not completely deleted patch port, that does not have anymore the type=patch attribute and  remains as garbage.

If you want to repackage the application:
   sbt assembly

 
KNOWN LIMITATIONS
=================
Only IPv4 addresses are shown.
The following elements are not shown in the graph:
* "ovs-system", because it's only an implementation detail of openvswitch (it serves as as a shared datapath for the ovs bridges).
* loopback interfaces

It assumes loopback interface having  "lo" name.
It assumes that vlan aliases are shown in the form "eth0.1234@eth0"


WHY SOME LINUX LINUX BRIDGES APPEARS ALSO AS OVS BRIDGES?
=========================================================
From packages.debian.org : 
"openvswitch-brcompat provides a way for applications that use the Linux bridge to gradually migrate to Open vSwitch. Programs that ordinarily control the Linux bridge module, such as "brctl", instead control the Open vSwitch kernel-based switch"


TODO
====
* output parsing is a bad thing. the format may change tomorrow. However a java API does not seem to exist for OVS. 
	* For retrieving interface info:
	  http://java.dzone.com/news/network-interface-details-java   
	  This API does not show all interfaces, only those that have an IP.
	* For OVS there seems to be a JSON RPC API
 
* maybe restructure the hierarchy
  update: problems arise if I introduce a parent abstract class for the Ifaces. The problem shows up when I try to drag graph elements. 
  As pointed in S.O. possible approaches are:
	     * case classes with a parent empty trait (under the hoods an empty trait is implemented as a java interface)
	     * a hierarchy of normal (not case) classes 

* check that the shell commands should be executed only once, to reduce execution time.
* modify the "used iface" discovery mechanism for l2tp ifaces. Actually it works only if it's in the default namespace
* support for ssh?
 

ON THE INTERFACE NAMING  
=======================
Depending on  "Consistent Network Device Naming" (CNDN) is enabled or not, interface names can be very different. 
In Fedora 18/19 CDND  is enabled by default.



VLAN TAG
========
* vlan alias created with "ip link .. tag ..": the VLAN TAG is shown after the @ in the name
* OVS internal interfaces may have a VLAN tag. However VID doesn't appear in "ip address show" but only in "ovs-vsctl show".


IMPLEMENTATIONS NOTES 
=============================
* Scala REGEX api returns a String (if it matches) or null, instead of an Option[String] 
* "\s" matches newline character too; "." does not match newline (unless "dot all mode" is enabled). To enable dotall: (?s) 
* OVSPort that are not also Iface (because it's not displayed in "ip address"): 
 	*  patch port
 	*  gre tunnel

* determine the interface type (goo.gl/cfwiR3)
	virtual iface: it appears in ls -l /sys/class/net/ | egrep virtual
	physical iface:  it appears in  ls -l /sys/class/net/ | egrep -v virtual
	tuntap: the file  /sys/class/net/${iface.name}/tun_flags  exists
	l2tp: it appears in  "ip l2tp show session"
	linux bridge: the file /sys/devices/virtual/net/${iface.name}/bridge  exists
	veth pair: the command  "ethtool -S phy-br-eth1 | awk '/peer_ifindex/ {print $2}' gives a result
	
JGraphX
* graph.getChildVertices(graph.getDefaultParent()) --> get all cells
* graph.removeCells() --> remove cells
* all methods return Object as type 
* if you want to bind the user objects (from the domain model) to the cells of the graph you have to assign
  the reference of the object to the field "value" of a cell, then use cell.getValue.asInstanceOf[..]
* tooltips and cell text:
	create a subclass of mxGraph and override getToolTipForCell() and convertValueToString(),
	then extract the text from the user object

	
	
	
	
	




