
package showmyns

import scala.sys.process._
import scala.util.matching.Regex
import spray.json._
import DefaultJsonProtocol._
    
trait GenericNetElem {
  val name: String
}

case class Session(sessionID: Int,
  peer_sessionID: Int,
  tunId: Int,
  peerTunId: Int,
  iface: String,
  namespace: Option[String])

case class Tunnel(tunId: Int,
  from: String,
  to: String,
  peerTunId: Int,
  sport: Int,
  dport: Int,
  namespace: Option[String])

//class containing only the info parsed from "ip address show" command
case class TempIface(name: String,
  index: Int,
  namespace: Option[String],
  state: String,
  mac: Option[String],
  addrList: List[(String, String)],
  mtu: Int,
  flags: List[String])

//ifaceType can be  "ovs-system","veth-pair","linuxBridgeInterface","physical","loopback","tuntap","gre","vlan_alias"	
case class Iface(name: String,
  ifaceType: String,
  state: String,
  macAddr: Option[String],
  addresses: List[(String, String)],
  mtu: Int,
  vlantag: Option[Int],
  ifaceUsed: Option[String],
  peerIf: Option[String],
  namespace: Option[String],
  index: Int,
  flags: List[String]) extends GenericNetElem {
  override def toString = s"Iface(name: $name, ifaceType: $ifaceType, state: $state, " +
    s"macAddr: $macAddr,\n addresses: $addresses, mtu: $mtu, vlantag: " +
    s"$vlantag , ifaceUsed: $ifaceUsed,\n peerIf: $peerIf, " +
    s"namespace: $namespace, index: $index, flags: $flags)"
}

//case class OVSPort(name: String,
//  tag: Option[Int],
//  ovsiface: Option[String],
//  portType: Option[String],
//  options: Map[String, String]) extends GenericNetElem

case class OVSPort(name: String,
  trunks: List[Int],
  tagOpt: Option[Int],
  vlan_modeOpt: Option[String],
  ovsiface: Option[String],
  portType: Option[String],
  options: Map[String, String]) extends GenericNetElem

case class OVSBridge(name: String, ports: List[OVSPort]) extends GenericNetElem {
  override def toString = s"OVSBridge(name: $name, ports:\n${ports.map(" " + _).mkString("\n")}\n)"
}

case class LinuxBr(name: String, id: String, stp: Boolean, nics: List[String], namespace: Option[String]) extends GenericNetElem

/*
 * Test application
 */
object Test extends App {
  println("Interfaces: \n" + Actions.getAllIfaces.mkString("\n----\n"))
  println("\nOVS Bridges: \n" + Actions.getOVSBridges.mkString("\n----\n"))
  println("\nLinux bridges: \n" + Actions.getLinuxBridges.mkString("\n") + "\n")
  println("\nRoutes: \n" + Actions.getRoutes.mkString("\n----\n"))
  println("\nL2tp sessions : \n" + Actions.getSessions.mkString("\n") + "\n")
  println("\nL2tp tunnels : \n" + Actions.getTunnels.mkString("\n") + "\n")
//  println(s"port: ${Actions.dumpFlows("br-ex")}")
}

object ShowJSON extends App {
  import spray.json._
  import Actions._
  object MyJsonProtocol extends DefaultJsonProtocol with NullOptions{
    implicit val interfaceFormat = jsonFormat12(Iface)
    implicit val OVSPortFormat = jsonFormat7(OVSPort)
    implicit val OVSBridgeFormat = jsonFormat2(OVSBridge)
    implicit val LBFormat = jsonFormat5(LinuxBr)
    implicit val routeFormat = jsonFormat3(NetRoute)
    implicit val sessionFormat = jsonFormat6(Session)
    implicit val tunnelFormat = jsonFormat7(Tunnel)
  }

  import MyJsonProtocol._
  if (args.length > 0) args(0) match {
    case "--ovsbridges" =>   println(getOVSBridges.toJson)
    case "--linuxbridges" => println(getLinuxBridges.toJson)
    case "--ifaces" => println(getAllIfaces.toJson)
    case "--routes" => println(getRoutes.toJson)
    case "--sessions" =>  println(getSessions.toJson)
    case "--tunnels" => println(getTunnels.toJson)
  }
//

//  
//  
// 
// 
}
/*
 * Utility functions to get network information
 */
object Actions {

  val sampleOutputs = false //if true use terminal outputs in SampleOutputs 
  val IPRegEx = """\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}"""


  def getOFPort(iface:String):Option[Int] = {
    
    val out= try {
      (s"ovs-vsctl get interface $iface ofport").!!(ProcessLogger(line => ())) //suppress output
    }catch {
      case e:Exception => ""
    }
    val port = out match {
      case "" => None
      case s => Some(s.replaceAll("\n", "").toInt)
    }
    
    port
  }
  
  def dumpFlows (ovsbridge:String):Option[String]={
     val out= try {
      val o1=(s"ovs-ofctl dump-flows ${ovsbridge}").!! //(ProcessLogger(line => ())) //suppress output
      o1.split("\n").drop(1).map{
        str => str.split(",").drop(6).mkString(",") 
      }.mkString("\n")
    }catch {
      case e:Exception => println(s"Catched exception $e"); ""
    }
    val flows = out match {
      case "" => None
      case s => Some(s)
    }
    
    flows
  }
    
  //checkers for the interface type (see goo.gl/cfwiR3) 
  def isPhys(iface: String): Boolean = getPhysIfaces.contains(iface)

  def isL2tp(iface: String): Boolean = getSessions exists (_.iface == iface)

  def isLoopBack(iface: String): Boolean = iface == "lo"
  // s"cat /sys/class/net/${iface.name}/address".!! == "00:00:00:00:00:00\n"   --> not sure if correct 

  def isTunTap(tempIface: TempIface): Boolean = {
    val netnsprefix = tempIface.namespace match {
      case Some(ns) => s"ip netns exec $ns "
      case None => ""
    }
    val cmd = s"${netnsprefix}ls /sys/class/net/${tempIface.name}/tun_flags"

    exitCode(cmd) == 0
  }

  //a vlan interface created with "ip link .. tag" or vconfig and shown with an @ in the name 
  def isVLanAlias(iface: String): Boolean = iface.matches("""(.+)\.\d+@\1$""")
  //exitCode(s"ls /proc/net/vlan/${iface}") == 0

  //def isTunTap2(iface: String): Boolean = isTunTap(Iface(iface, "", "", "", List(), 0, None, None, None, None, 0)) //utility method

  def isOVSSystem(iface: String): Boolean = iface == "ovs-system"

  /**
   * For a veth pair interface get the peer interface index from "ip addr" list
   *
   * It executes the following bash command:
   *  if I'm in the default namespace:  ethtool -S <iface> | awk '/peer_ifindex/ {print $2}'
   *  if in another namespace:  ip netns exec <ns> ethtool -S <iface>  | awk '/peer_ifindex/ {print $2}'
   *
   */
  def getVethPeerIndex(iface: Iface): Option[Int] = {
    val netnsprefix = iface.namespace match {
      case Some(ns) => Seq("ip", "netns", "exec", ns)
      case None => Seq()
    }
    val peerIdxStr = ((netnsprefix ++ Seq("ethtool", "-S", iface.name)) #| Seq("awk", "/peer_ifindex/ {print $2}")) !! (ProcessLogger(line => ())) //suppress output

    val res = peerIdxStr match {
      case "" => None
      case str => Some(str.replaceAll("\n", "").toInt)
    }
    //println(s"The peer of $iface is $res")
    res

  }

  /**
   * Return an exit code for a bash command
   */
  def exitCode(cmd: String): Int = cmd.!(ProcessLogger(line => ())) //discard output

  /**
   * Return a list of physical interfaces  (see goo.gl/FEqxI7)
   */
  def getPhysIfaces(): List[String] = {

    //execute the bash command "ls -l /sys/class/net/ | egrep -v virtual"
    val phys = if (sampleOutputs) SampleOutputs.phys else (Seq("ls", "-l", "/sys/class/net/") #| Seq("egrep", "-v", "virtual")) !!

    val physRegEx = """([^\s]+) ->""".r
    val s = for (physRegEx(iface) <- physRegEx findAllIn phys) yield iface
    s toList
  }

  def getNamespaces(): Array[String] = {
    val nsStr = "ip netns list".!!
    val namespaces = if (nsStr != "") nsStr.split("\n") else Array[String]()
    if (namespaces.isEmpty) Array("") else "" +: namespaces //add default namespace as empty string
  }

 

  /**
   * Parse the bash command "ip addr show" in every namespace
   */
  def parseIpAddrOutputs(): List[TempIface] = {
    val outputs = if (sampleOutputs) SampleOutputs.ipaddr else execInAllNS("ip address show")
    
    val regEx = """(?m)^(\d+): """.r; 	//regex "multi line mode" set
    val tempIfList = 
      for( 
        (namespace, output) <- outputs;                        //list of (namespace, command output) tuples
        (ifIndex, rest) <- parseElemsAndProps(output, regEx)   //list of (interface index,rest) tuples
        ) yield {
            val ns = if (namespace.isEmpty()) None else Some(namespace)

            val nameRegEx = """^(\S+): """.r //extract iface name
            val name = (nameRegEx findFirstIn rest) match {
              case Some(nameRegEx(s)) => s
              case None => throw new Error(s"ERR: no name found for $ifIndex!!")
            }
            val stateRegEx = """state (\S+)""".r
            val state = (stateRegEx findFirstIn rest) match {
              case Some(stateRegEx(s)) => s
              case None => throw new Error(s"ERR: no state found for $ifIndex!!")
            }
            val macRegEx = """link/\S+ (\S+)""".r
            val macAddr:Option[String] = (macRegEx findFirstIn rest) match {
              case Some(macRegEx(ethMatch)) => Some(ethMatch)
              case None => None //throw new Error(s"ERR: no mac found in $ifIndex!!")
            }

            val addrRegEx = """inet (\S+)( brd \S+)? scope \S+ (\S+)""".r
            val ip_addresses = (for (addrRegEx(ipMatch, _, labelMatch) <- addrRegEx findAllIn rest) yield (ipMatch, labelMatch)).toList

            val mtuRegEx = """mtu (\d+)""".r
            val mtu = (mtuRegEx findFirstIn rest) match {
              case Some(mtuRegEx(mtuMatch)) => mtuMatch toInt
              case None => throw new Error(s"ERR: no mtu found in $ifIndex!!")
            }
            val flagsRegEx = """<(\S+)> """.r
            val flags = (flagsRegEx findFirstIn rest) match {
              case Some(flagsRegEx(flagsMatch)) => flagsMatch.split(",").toList
              case None => List()
            }
            TempIface(name, ifIndex.toInt, ns, state, macAddr, ip_addresses, mtu, flags)
    }
    tempIfList
  }

  /**
   * Return ALL interfaces, in all namespaces
   * It relies on getOVSBridges, getPhysIfaces, getTunnelInfo to obtain informations
   */
  def getAllIfaces(): List[Iface] = {

    val tempIfaces2 = parseIpAddrOutputs()

    val ovsbridges = getOVSBridges()
    val physIfaces = getPhysIfaces()
    val sessions = getSessions()
    val tunnels = getTunnels()

    def getInfo(temp: TempIface): Iface = { //determine interface type (and related info)

      //TODO: find out why with OpenStack an ovs interface is both ovsinternal and linux bridge
      def isOVSInternal(ifaceName: String): Boolean = ovsbridges exists { br => br.ports exists { port => (port.name == ifaceName && port.portType.getOrElse("") == "internal") } }

      def isGRE(iface: String): Boolean = ovsbridges exists { br => br.ports exists { port => (port.name == iface && port.portType.getOrElse("") == "gre") } }

      def isLinuxBrIface(tempIface: TempIface): Boolean = {

        val netnsprefix = tempIface.namespace match {
          case Some(ns) => s"ip netns exec $ns "
          case None => ""
        }
        val cmd = s"${netnsprefix}ls /sys/devices/virtual/net/${tempIface.name}/bridge"

        exitCode(cmd) == 0 && !isOVSInternal(tempIface.name)
      }

      //TODO: insert test for multi-type
      var iface1 = Iface(temp.name, "to be determined", temp.state, temp.mac, temp.addrList,
        temp.mtu, None, None, None, temp.namespace, temp.index, temp.flags)

      if (isOVSSystem(temp.name)) iface1 = iface1.copy(ifaceType = "ovs-system")

      //if it's a veth peer 
      getVethPeerIndex(iface1) match {
        case Some(idx) => iface1 = iface1.copy(ifaceType = "veth-pair", peerIf = Some(tempIfaces2.find(_.index == idx).get.name))
        case None =>
      }

      if (isLinuxBrIface(temp)) iface1 = iface1.copy(ifaceType = "linuxBridgeInterface")

      if (isOVSInternal(temp.name)) {
        val br = (ovsbridges find { _.ports exists (_.name == temp.name) }).get //find the OVS bridge containing our interface
        val port = br.ports.find(_.name == temp.name).get
        iface1 = iface1.copy(ifaceType = "OVSInternal", vlantag = port.tagOpt)
      }

      if (isPhys(temp.name)) { iface1 = iface1.copy(ifaceType = "physical") }

      //if it is a l2tp interface
      if (sessions.exists(_.iface == temp.name)) {
        val relatedSess = sessions.find(_.iface == temp.name).get
        val relatedTun = tunnels.find(_.tunId == relatedSess.tunId).get
        val physIface = findOutIface(relatedTun.from)
        iface1 = iface1.copy(ifaceType = "l2tp", ifaceUsed = Some(physIface))
      }

      if (isLoopBack(temp.name)) iface1 = iface1.copy(ifaceType = "loopback")

      if (isTunTap(temp)) iface1 = iface1.copy(ifaceType = "tuntap")

      if (isGRE(temp.name)) iface1 = iface1.copy(ifaceType = "gre")

      if (isVLanAlias(temp.name)) {
        val tag = temp.name.substring(temp.name.indexOf('.') + 1, temp.name.indexOf('@')).toInt //extract tag from interface name
        val physStr = temp.name.substring(0, temp.name.indexOf('.'))

        iface1 = iface1.copy( ifaceType = "vlan_alias", vlantag = Some(tag), ifaceUsed = Some(physStr))
      }

      //if (iface1.ifaceType == "<still to be determined>") throw new Error(s"Interface type not found for ${temp.name} !")
      if (iface1.name.contains("@")) {
        iface1 = iface1.copy(name = iface1.name.substring(0, iface1.name.indexOf('@')) )
      }


      iface1
    }

    tempIfaces2 map getInfo

  }

  /**
   * Given a string of couples and a regex extract the list of (element,property).
   * Useful to extract outputs of "ip address show", "ovs-vsctl show"
   */
  def parseElemsAndProps(s: String, r: scala.util.matching.Regex): List[(String, String)] = {
    val elems = (for (r(elem) <- r findAllIn s) yield elem).toList
    val props = ((r split s).toList drop 1)
    elems zip props
  }

  /**
   * Parse ovs-vsctl commmand and return a list of OVS bridges
   * NOTE: OVS bridges (and their mgmt ifaces) are always created in the default namespace
   *
   */
  def getOVSBridges(): List[OVSBridge] = {
 
    val ovs_show_out =
      if (sampleOutputs) SampleOutputs.ovs_show
      else if (exitCode("ovs-vsctl show") != 0) { 
        println("openvswitch not installed or not running!")
        return List()
      } else "ovs-vsctl show".!!
      
    def parseOpts(opts: Option[String]): Map[String, String] = { //parse options: {remote_ip="8.0.1.1", ....}
      opts match {
        case Some(s) =>
          s.split(", ").map {
            propStr =>
              val prop = propStr.split("=")
              val optkey = prop(0).replaceAll("\"", "")
              val optvalue = prop(1).replaceAll("\"", "")
              (optkey -> optvalue)
          }.toMap
        case None => Map()
      }
    }

    val brRegex = """Bridge (\S+)""".r
    val bridges: List[(String, String)] = parseElemsAndProps(ovs_show_out, brRegex)

    bridges.map {
      case (brname, brprop) =>
        val brname1 = brname.replaceAll("\"", "")
        val portRegex = """Port (\S+)""".r
        val portElems: List[(String, String)] = parseElemsAndProps(brprop, portRegex)
        val ports = portElems.map {
          case (portname, portprop) =>
            val portname1 = portname.replaceAll("\"", "")
            val ifnameRegex = """Interface (\S+)""".r
            val ifname = ifnameRegex.findFirstIn(portprop) match {
              case Some(ifnameRegex(n)) => Some(n.replaceAll("\"", ""))
              case None => None
            }
            val tagRegex = """tag: (\d+)""".r
            val tag = tagRegex findFirstIn portprop match {
              case Some(tagRegex(t)) => Some(t)
              case None => None
            }  //not used ...
            val typeRegex = """type: (\S+)""".r
            val typ = typeRegex findFirstIn portprop match {
              case Some(typeRegex(n)) => Some(n.replaceAll("\"", ""))
              case None => None
            }
            val optRegex = """options: \{([^\}]+)\}""".r
            val opt = optRegex findFirstIn portprop match {
              case Some(optRegex(n)) => Some(n)
              case None => None
            }
            val (trunks,tagOpt,vlan_modeOpt) = getVLANFields(portname1)  
            //OVSPort(portname1, tag.map(_.toInt), ifname, typ, parseOpts(opt)) //parseOpts(opts))
            OVSPort(portname1, trunks, tagOpt, vlan_modeOpt, ifname, typ, parseOpts(opt)) //parseOpts(opts))
        }
        OVSBridge(brname1, ports)
    }
  }

  /**
   * Parse "ip l2tp show tunnel" commmands in all namespaces
   */
  def getTunnels(): List[Tunnel] = {
    if (exitCode("ip l2tp show tunnel") != 0) { //println("ip l2tp not available in this host." )
      return List()
    }
    val tunnels = execInAllNS("ip l2tp show tunnel")

    tunnels.flatMap {
      case (ns, tun) =>
        val namespace = if (ns == "") None else Some(ns)
        val tunRegEx = ("""(?s)Tunnel (\d+).*?From (\S+).*?to (\S+)\s+Peer tunnel (\d+).*?ports: (\d+)/(\d+)""").r
        val m = for (substr <- tunRegEx findAllIn tun) yield substr
        m.map {
          case tunRegEx(tid, from, to, ptid, sport, dport) => //extractor
            Tunnel(tid.toInt, from, to, ptid.toInt, sport.toInt, dport.toInt, namespace)
        }.toList

    }
  }

  /**
   * Parse "ip l2tp show session" commmands in all namespaces
   */
  def getSessions(): List[Session] = {
    if (exitCode("ip l2tp show session") != 0) { //println("ip l2tp not available in this host." )
      return List()
    }
    val sessions = execInAllNS("ip l2tp show session")
    sessions.flatMap {
      case (ns, sess) =>
        val namespace = if (ns == "") None else Some(ns)
        val sessRegEx = """Session (\d+) in tunnel (\d+)\s+Peer session (\d+), tunnel (\d+)\s+interface name: (\S+)""".r
        val l = for (substr <- sessRegEx findAllIn sess) yield substr
        l.map {
          case sessRegEx(sess, tid, psess, ptid, iface) => Session(sess.toInt, psess.toInt, tid.toInt, ptid.toInt, iface, namespace)
        }.toList
    }
  }

  def execInAllNS(cmd: String): List[(String, String)] = {
    val namespaces = getNamespaces().toList
    for (ns <- namespaces) yield {
      val cmd_out = if (ns.isEmpty) cmd.!! else s"ip netns exec $ns $cmd".!!
      (ns -> cmd_out)
    }
  }

  /**
   * Parse "brctl show" commmand and return a list of Linux bridges
   *
   * NOTE: if a OVS bridge with the same name exists, doesn't consider it.
   * 	   it relies on getOVSBridges
   *
   */
  def getLinuxBridges: List[LinuxBr] = {

    val br_outputs0 = execInAllNS("brctl show")
    val br_outputs = br_outputs0.map {
      case (ns, out) => (ns, out.substring(out.indexOf('\n') + 1)) //remove first line
    }

    val nameidSTP_RegEx = """(\S+)\s+(\S+)\s+(yes|no)""".r

    br_outputs.flatMap {
      case (ns, brout) =>
        val ns2 = if (ns == "") None else Some(ns)
        val brnames = (for (nameidSTP_RegEx(br, id, stp) <- nameidSTP_RegEx findAllIn brout) yield (br, id, stp)) toList
        val rests = ((nameidSTP_RegEx split brout).toList drop 1)
        val tot = brnames zip rests
        val allLinBridges = tot map {
          case ((name, id, stp), rest) =>
            val nicRegEx = """(\S+)""".r
            val ifaces = for (nicRegEx(iface) <- nicRegEx findAllIn rest) yield iface
            val stp2 = stp match {
              case "yes" => true
              case "no" => false

            }
            LinuxBr(name, id, stp2, name :: ifaces.toList, ns2) //explicitly add mgmt interface (it is not shown in "brctl show")
        }
        val ovsbridges = getOVSBridges
        allLinBridges.filterNot(lbr => ovsbridges.exists(_.name == lbr.name)) //remove ovs bridges that are listed as linux bridges 
    }
  }

  case class NetRoute(net: String, dev: String, src: String)

  /**
   * Return the list of routes for this host machine
   *
   */
  def getRoutes: List[NetRoute] = {
    val route_out = if (sampleOutputs) SampleOutputs.ip_route else "ip route show".!!
    val defRouteRegEx = ("""default via \b(.*)\b dev \b(.*)\b""").r
    val defaultRoutes = (for (defRouteRegEx(src, dev) <- defRouteRegEx findAllIn route_out) yield NetRoute("0.0.0.0/0", dev, src)) toList //multiple default routes may exist?
    val rest = route_out split "\n" drop defaultRoutes.size mkString "\n" //remove the first (default) routes
    val routeRegEx = """(\S+) dev (\S+)  (proto \S+  )?scope (\S+)  (src (\S+))?""".r
    val routes = (for (routeRegEx(ipnet, dev, _, scope, _, src) <- routeRegEx findAllIn route_out) yield NetRoute(ipnet, dev, src)) toList

    (defaultRoutes ::: routes).reverse
  }

  /**
   * Return the Long from an IP string representation (ex. "192.168.0.2"->3232235522)
   */
  def ipToLong(ip: String) = {
    val l = ip.split("""\.""").map(_.toLong)
    (l(0) << 24) + (l(1) << 16) + (l(2) << 8) + (l(3))
  }

  def nmaskToLong(nmask: Int): Long = {
    val s = "".padTo(nmask, '1').padTo(32, '0')
    java.lang.Long.parseLong(s, 2)
  }

  /**
   * Given a net "ip/netmask" and a IP, find if the IP is contained in the net
   * It's the classic netmask matching used in packet routing
   */
  def containsIP(net: String, ip: String) = {
    val n = net.split("""/""")
    val nmaskLong = nmaskToLong(n(1).toInt)
    val net_andbitwise_nmask = ipToLong(n(0)) & nmaskLong
    val ip_andbitwise_nmask = ipToLong(ip) & nmaskLong
    net_andbitwise_nmask == ip_andbitwise_nmask
  }

  /**
   * Given an IP address "ip/netmask" find the interface used to send out the packet
   * NOTE: an alternative may be with the bash command  "ip route get <IP>"
   * NOTE: IT WORKS ONLY IN THE DEFAULT NAMESPACE
   */
  def findOutIface(ip: String): String = getRoutes.find(route => containsIP(route.net, ip)) match {
    case Some(route) => route.dev
    case None => throw new Exception(s"No route found for $ip (not even a default route)!")
  }


  def getVLANFields(port: String): (List[Int],Option[Int],Option[String]) = {
  	def stripLast(str:String):String = str.substring(0, str.length-1)  

    val trunksStr = s"ovs-vsctl --format=json get port $port trunks".!!
    val trunksStr2 = stripLast(trunksStr)   
    val tagStr = (s"ovs-vsctl --format=json get port $port tag").!!
    val tagStr2 = stripLast(tagStr)
    val vlan_modeStr = s"ovs-vsctl --format=json  get port $port vlan_mode".!!
    val vlan_modeStr2 = stripLast(vlan_modeStr)
    val trunks= JsonParser(trunksStr2).convertTo[List[Int]]
	  val tagOpt = if (tagStr2=="[]") None else Some(tagStr2.toInt) 
	  val vlan_modeOpt = if (vlan_modeStr2=="[]") None else Some(vlan_modeStr2)
    (trunks,tagOpt,vlan_modeOpt)
  }
  /**
   * Given an OVS port determine the VLAN type (native-tagged, native-untagged,trunk,access) 
   * see also:    man ovs-vswitchd.conf.db
   */
  def getVLANMode(port:String):String = {
  
    val (trunks,tagOpt,vlan_modeOpt) = getVLANFields(port)  
	//println((trunks,tagOpt,vlan_modeOpt))
	val trunking = if (trunks.isEmpty) "all" else trunks.mkString(",")
	val natT = s"native-tagged: trunking [$trunking], ingress untagged packets considered VLAN ${tagOpt.getOrElse("?")}"
	val natU = s"native-untagged: trunking [$trunking], ingress untagged packets considered VLAN ${tagOpt.getOrElse("?")} (native), <br>egress native VLAN packets exit untagged"
	val acc = s"access for tag ${tagOpt.getOrElse("?")} "
	val out = vlan_modeOpt match {
	  case Some("native-tagged") => 
	  	natT
	  case Some("native-untagged") => 
	  	natU
	  case Some("trunk") =>
	    s"trunking ${trunking}"
	  case Some("access") =>
	    acc
	  case None =>
	    tagOpt match {
	      case None => s"trunking ${trunking}"
	      case Some(accessTag) => 
	        trunks match {
	          case Nil => acc  
	          case t =>  natU
	        }
	    }
    case _ =>  throw new Error(s"VLAN mode option error (${vlan_modeOpt}) !!")
	}
    out
  }
  def execute(cmd:String) = {
    println(s"-->   $cmd")
    cmd.!!
  }
}
