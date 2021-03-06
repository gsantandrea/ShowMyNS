package showmyns

import javax.swing.AbstractAction
import java.awt.event.ActionEvent
import scala.sys.process._
import javax.swing.JOptionPane
import Actions._

class Sniff(ifname: String) extends AbstractAction("tcpdump on it") {
  override def actionPerformed(e: ActionEvent) {
    println(s"To sniff type the following:\ntcpdump -nnvei $ifname")
  }
}

class AddPortToOvs(ovsbr: String, frame: MFrame2) extends AbstractAction("Add a port") {
  override def actionPerformed(e: ActionEvent) {
    val iface = JOptionPane.showInputDialog("Type the interface to add", "")
    if (iface == null) return
    execute(s"ovs-vsctl add-port $ovsbr $iface")
    frame.drawNetworkElements
  }
}
class RemPortFromOvs(ovsbr: String, frame: MFrame2) extends AbstractAction("Remove a port") {
  override def actionPerformed(e: ActionEvent) {
    val iface = JOptionPane.showInputDialog("Type the interface to remove", "")
    if (iface == null) return
    execute(s"ovs-vsctl del-port $ovsbr $iface")
    frame.drawNetworkElements
  }
}
class CreateOvs(frame: MFrame2) extends AbstractAction("Create an OVS bridge") {
  override def actionPerformed(e: ActionEvent) {
    val ovsbr= JOptionPane.showInputDialog("Type the OVS bridge name to create", "")
    if (ovsbr == null) return
    execute(s"ovs-vsctl add-br $ovsbr")
    frame.drawNetworkElements
  }
}

class RemoveOvs(ovsbr:String,frame: MFrame2) extends AbstractAction("Remove this OVS bridge") {
  override def actionPerformed(e: ActionEvent) {
    execute(s"ovs-vsctl del-br $ovsbr")
    frame.drawNetworkElements
  }
}

class CreateLinBr(frame: MFrame2) extends AbstractAction("Create a linux bridge") {
  override def actionPerformed(e: ActionEvent) {
    val lbr= JOptionPane.showInputDialog("Type the Linux bridge name to create", "")
    if (lbr == null) return
    execute(s"brctl addbr $lbr")
    execute(s"ip link set up $lbr")
    
    frame.drawNetworkElements
  }
}



class RemoveLinBr(lbr:String,frame: MFrame2) extends AbstractAction("Remove this Linux bridge") {
  override def actionPerformed(e: ActionEvent) {
    execute(s"ip link set $lbr down")
    execute(s"brctl delbr $lbr")
    frame.drawNetworkElements
  }
}

class RemVethPair (name:String,frame: MFrame2) extends AbstractAction("Remove this veth pair") {
  override def actionPerformed(e: ActionEvent) {
    execute(s"ip link del $name")
    frame.drawNetworkElements
  }
}

class RemTunTap (name:String,frame: MFrame2) extends AbstractAction("Remove this tuntap") {
  override def actionPerformed(e: ActionEvent) {
    execute(s"ip link del $name")
    frame.drawNetworkElements
  }
}


class CreateVethPair(frame: MFrame2) extends AbstractAction("Create a veth pair") {
  override def actionPerformed(e: ActionEvent) {
    val first= JOptionPane.showInputDialog("Type the name of the first interface of the veth pair", "")
    val second= JOptionPane.showInputDialog("Now type the veth peer name", "")
    if (first == null || second == null) return
    execute(s"ip link add $first type veth peer name $second")
    execute(s"ip link set $first up")
    execute(s"ip link set $second up")
    frame.drawNetworkElements
  }
}

class AddOVSInternal(ovsbr:String, frame: MFrame2) extends AbstractAction("Add OVS internal port") {
  override def actionPerformed(e: ActionEvent) {
    val name= JOptionPane.showInputDialog("Type the name of interface", "")
    val ip= JOptionPane.showInputDialog("Type the IP address (example: 10.0.0.1/24) ", "10.0.0.1/24")
    if(name==null || ip==null) return
    execute(s"ovs-vsctl add-port $ovsbr $name -- set Interface $name type=internal")
    execute(s"ip link set $name up")
    execute(s"ip address add $ip dev $name")
    frame.drawNetworkElements
  }
}