package showmyns

import javax.swing.AbstractAction
import java.awt.event.ActionEvent
import scala.sys.process._
import javax.swing.JOptionPane

class Sniff(ifname: String) extends AbstractAction("tcpdump on it") {
  override def actionPerformed(e: ActionEvent) {
    println(s"To sniff type the following:\nsudo tcpdump -nnvei $ifname")
  }
}

class AddPortToOvs(ovsbr: String, frame: MFrame2) extends AbstractAction("Add a port") {
  override def actionPerformed(e: ActionEvent) {
    val iface = JOptionPane.showInputDialog("Type the interface to add", "")
    if (iface == null) return
    val cmd = s"sudo ovs-vsctl add-port $ovsbr $iface"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}
class RemPortFromOvs(ovsbr: String, frame: MFrame2) extends AbstractAction("Remove a port") {
  override def actionPerformed(e: ActionEvent) {
    val iface = JOptionPane.showInputDialog("Type the interface to remove", "")
    if (iface == null) return
    val cmd = s"sudo ovs-vsctl del-port $ovsbr $iface"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}
class CreateOvs(frame: MFrame2) extends AbstractAction("Create an OVS bridge") {
  override def actionPerformed(e: ActionEvent) {
    val ovsbr= JOptionPane.showInputDialog("Type the OVS bridge name to create", "")
    if (ovsbr == null) return
    val cmd = s"sudo ovs-vsctl add-br $ovsbr"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}

class RemoveOvs(ovsbr:String,frame: MFrame2) extends AbstractAction("Remove this OVS bridge") {
  override def actionPerformed(e: ActionEvent) {
    val cmd = s"sudo ovs-vsctl del-br $ovsbr"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}

class CreateLinBr(frame: MFrame2) extends AbstractAction("Create a linux bridge") {
  override def actionPerformed(e: ActionEvent) {
    val lbr= JOptionPane.showInputDialog("Type the Linux bridge name to create", "")
    if (lbr == null) return
    val cmd = s"sudo brctl addbr $lbr"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}



class RemoveLinBr(lbr:String,frame: MFrame2) extends AbstractAction("Remove this Linux bridge") {
  override def actionPerformed(e: ActionEvent) {
    val cmd0 = s"sudo ip link set $lbr down"
    println(s"-->   $cmd0")
    cmd0.!!
    val cmd = s"sudo brctl delbr $lbr"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}

class RemVethPair (name:String,frame: MFrame2) extends AbstractAction("Remove this veth pair") {
  override def actionPerformed(e: ActionEvent) {
    val cmd = s"sudo ip link del $name"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}

class CreateVethPair(frame: MFrame2) extends AbstractAction("Create a veth pair") {
  override def actionPerformed(e: ActionEvent) {
    val first= JOptionPane.showInputDialog("Type the name of the first interface of the veth pair", "")
    val second= JOptionPane.showInputDialog("Now type the veth peer name", "")
    if (first == null || second == null) return
    val cmd = s"sudo ip link add $first type veth peer name $second"
    println(s"-->   $cmd")
    cmd.!!
    frame.drawNetworkElements
  }
}

class AddOVSInternal(ovsbr:String, frame: MFrame2) extends AbstractAction("Add OVS internal port") {
  override def actionPerformed(e: ActionEvent) {
    val name= JOptionPane.showInputDialog("Type the name of interface", "")
    val ip= JOptionPane.showInputDialog("Type the IP address (example: 10.0.0.1/24) ", "10.0.0.1/24")
    if(name==null || ip==null) return
    val cmd1 = s"sudo ovs-vsctl add-port $ovsbr $name -- set Interface $name type=internal"
    println(s"-->   $cmd1")
    cmd1.!!
    val cmd2 = s"sudo ip link set $name up"
    println(s"-->   $cmd2")
    cmd2.!!
    val cmd3 = s"sudo ip address add $ip dev $name"
    println(s"-->   $cmd3")
    cmd3.!!
    
    frame.drawNetworkElements
  }
}



