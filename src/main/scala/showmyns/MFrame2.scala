package showmyns

import java.awt.event.KeyListener
import com.mxgraph.model.mxCell
import com.mxgraph.view.mxGraph
import javax.swing.UIManager
import javax.swing.JFrame
import com.mxgraph.swing.handler.mxRubberband
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.layout.mxFastOrganicLayout
import com.mxgraph.swing.util.mxMorphing
import java.io.File
import scala.io.Source
import java.awt.Toolkit
import com.mxgraph.util.mxEvent
import java.awt.event.MouseAdapter
import com.mxgraph.model.mxICell
import java.awt.Dimension
import com.mxgraph.util.mxEventSource.mxIEventListener
import java.awt.event.MouseEvent
import com.mxgraph.util.mxEventObject
import Actions._

class MFrame2(s: String, orgLayout: Boolean) extends JFrame(s) with KeyListener {
  //--constructor--
  setPreferredSize(new Dimension(1000, 600))
  setSize(1000, 600)
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
  javax.swing.ToolTipManager.sharedInstance().setInitialDelay(100) //faster tooltips
  javax.swing.ToolTipManager.sharedInstance().setDismissDelay(70000)
  
  private var graph: mxGraph = null

  addWindowListener(new java.awt.event.WindowAdapter() {
    override def windowClosing(evt: java.awt.event.WindowEvent) {
      println("Layout saved in file \"layout.json\".\nExiting..")
      saveLayout(graph)
    }
  })
  private val dimension = Toolkit.getDefaultToolkit().getScreenSize()
  private val screenx = ((dimension.getWidth() - getWidth()) / 2).toInt
  private val screeny = ((dimension.getHeight() - getHeight()) / 2).toInt
  setLocation(screenx, screeny) //center
  drawGraph()
  setVisible(true)
  //--end of constructor --

  override def keyPressed(e: java.awt.event.KeyEvent) { //used by the JFrame and the mxGraphComponent
    if (e.getKeyCode() == 116) { //F5 KEYCODE
      saveLayout(graph)
      graph.removeCells(graph.getChildVertices(graph.getDefaultParent()))
      drawNetworkElements()
    }
  }
  def keyReleased(x$1: java.awt.event.KeyEvent): Unit = {}
  def keyTyped(x$1: java.awt.event.KeyEvent): Unit = {}

  def drawGraph() {
    
    graph = new mxGraph {
      override def getToolTipForCell(cell: Object): String = {       //tooltip text
        cell match {
          case cell: mxICell => cell.getValue match {
            case iface: Iface =>
              val typ = "type: " + iface.ifaceType
              val state = "<br> state: " + iface.state
              val mac = "<br> MAC: " + iface.macAddr
              val addr = s"<br>adresses: <br>${iface.addresses.mkString("<br> ")}"
              val tag = iface.vlantag match {
                case Some(tag) => s"<br>VLAN ID = $tag"
                case None => ""
              }
              val tun = if (iface.ifaceType == "l2tp") {
                val session = getSessions.find(_.iface == iface.name).get
                val tunnel = getTunnels.find(_.tunId == session.tunId).get
                s"<br>tunnelID: ${tunnel.tunId} <br>peerTunnelID: ${tunnel.peerTunId} " +
                  s"<br>from ${tunnel.from} <br>to: ${tunnel.to} <br>sessID: ${session.sessionID} " +
                  s"<br>peerSessID ${session.peer_sessionID}  <br>source port: ${tunnel.sport} " +
                  s"<br>dport: ${tunnel.dport}"
              } else ""
              val mtu = "<br> mtu: " + iface.mtu
              val namespace = "<br> namespace: " + iface.namespace.getOrElse("")
              val flags = "<br> flags: " + iface.flags
              val ofport= {
                val p=Actions.getOFPort(iface.name)
                p match {  
                  case Some(_) => "<br> ofport: " + p.get
                  case None => ""
                }
              }
              s"<html> $typ $flags $state $mac $addr $mtu $tag $tun $namespace $ofport</html>"
            case linBr: LinuxBr =>
              val name = linBr.name
              val id = s"<br>id: ${linBr.id}"
              val stp = s"<br>stp: ${linBr.stp}"
              val namespace = "<br> namespace: " + linBr.namespace.getOrElse("")
              s"<html>Linux Bridge <br>$name $id $stp $namespace</html>"
            case ovsB: OVSBridge =>
              val flows = Actions.dumpFlows(ovsB.name) match {
                  case Some(f) => "<br> <br>flows: <br> " + f.replaceAll("\n", "<br>")
                  case None => ""
              }
              s"<html> Open vSwitch Bridge<br>${ovsB.name} ${flows} </html>"
            case ovsP: OVSPort =>
              val n = s"<br>name = ${ovsP.name}"
              val o = s"<br>options: ${ovsP.options}" 
              val t = s"<br>type: ${ovsP.portType.getOrElse("")}" 
              val ofport= {
                val p=Actions.getOFPort(ovsP.name)
                p match {  
                  case Some(_) => "<br> ofport: " + p.get
                  case None => ""
                }
              }
              s"<html>OVS port $n $t $o $ofport</html>"
            case _ => super.convertValueToString(cell)

          }
          case _ => throw new Error(s"error in displaying tooltip: $cell not a cell?")
        }
      }

      override def convertValueToString(cell: Object): String = {      //mxCell displayed text
        cell match {
          case cell: mxICell => cell.getValue match {
            case iface: Iface =>
              s"${iface.name}"
            case linBr: LinuxBr =>
              linBr.name + "(bridge)"
            case ovsB: OVSBridge =>
              ovsB.name + "(bridge)"
            case ovsP: OVSPort =>
              s"${ovsP.name}"
            case _ => super.convertValueToString(cell)

          }
          case _ => throw new Error("wtf.. not a cell?")
        }
      }
    }

    drawNetworkElements()

    graph.setCellsCloneable(false)
    graph.setAllowDanglingEdges(false)
    graph.setAllowNegativeCoordinates(true)
    graph.setCellsResizable(false)
    graph.setCellsDisconnectable(false) 
    graph.setDisconnectOnMove(false)
    graph.setSplitEnabled(false) // http://forum.jgraph.com/questions/121/how-do-you-prevent-vertices-dropped-on-edges-from-cutting-that-edge

    val graphComponent = new mxGraphComponent(graph)

    //TODO: implement actions here (for example "interface remove",..)
    graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent) {
        val cell = graphComponent.getCellAt(e.getX(), e.getY())
        
//        println("Mouse click in graph component")
//        if (cell != null) println("cell=" + graph.getLabel(cell))

      }
    })

    graphComponent.setConnectable(false)
    graphComponent.addKeyListener(this)
    graphComponent.setToolTips(true)
    graphComponent.requestFocus()
    val rubberband = new mxRubberband(graphComponent) //enables rectangle selections
    if (orgLayout) useMorphing(graph, graphComponent)
    getContentPane().add(graphComponent)

  }

  def loadLayout() = {
    val f = new File("layout.json")
    if (f.exists) {
      println("loading layout from file..")
      val str = Source.fromFile("layout.json").getLines.mkString("\n")

      import spray.json._
      import DefaultJsonProtocol._
      val pos = str.asJson.convertTo[Array[(String, Double, Double)]]
      val cells = graph.getChildVertices(graph.getDefaultParent()).map(_.asInstanceOf[mxCell])

      pos foreach {
        case (title, x, y) =>
          val cell = cells.find(c => graph.convertValueToString(c) == title)
          cell match {
            case Some(c) =>
              c.getGeometry().setX(x)
              c.getGeometry().setY(y)
            case None =>
          }
      }
    }
  }

  def drawNetworkElements() {
    val parent1 = graph.getDefaultParent.asInstanceOf[mxCell]
    val h = getHeight()
    val w = getWidth()

    //retrieve network info
    val physIfaces = getPhysIfaces
    val ifaces = getAllIfaces
    val ovsbridges = getOVSBridges

    val linuxBridges = getLinuxBridges

    //add styles
    import scala.collection.JavaConversions._, view.Styles._
    val stylesheet = graph.getStylesheet()
    val styles = Array(
      ("OVSBr", ovsBrStyle),
      ("physInt", physIntStyle),
      ("linBr", linBrStyle),
      ("l2tp", l2tpStyle),
      ("vlanalias", vlanAliasStyle),
      ("ovsinternal", ovsInternalStyle),
      ("tuntap", tuntapStyle),
      ("gre", greStyle),
      ("veth", vethStyle),
      ("patch", patchStyle),
      ("linbrif", linbrIfStyle),
      ("otherOVS", otherOVSStyle)
      )

    graph.getModel().beginUpdate()

    styles foreach { case (stName, st) => stylesheet.putCellStyle(stName, st) }

    println(s"Started drawing ${if (orgLayout) "(with organic layout)" else ""} ...") //TODO: organic layout feature is deprecated, remove this... 

    try {
      //draw legenda
      graph.insertVertex(parent1, null, "physical interface", w - 200, 20, 150, 30, "physInt", false)
      graph.insertVertex(parent1, null, "ovs bridge", w - 200, 50, 150, 30, "OVSBr", false)
      graph.insertVertex(parent1, null, "l2tp tunnel", w - 200, 80, 150, 30, "l2tp", false)
      graph.insertVertex(parent1, null, "vlan alias", w - 200, 110, 150, 30, "vlanalias", false)
      graph.insertVertex(parent1, null, "linux bridge", w - 200, 140, 150, 30, "linBr", false)
      graph.insertVertex(parent1, null, "TUN/TAP", w - 200, 170, 150, 30, "tuntap", false)
      graph.insertVertex(parent1, null, "ovs-internal", w - 200, 200, 150, 30, "ovsinternal", false)
      graph.insertVertex(parent1, null, "gre tunnel", w - 200, 230, 150, 30, "gre", false)
      graph.insertVertex(parent1, null, "patch port", w - 200, 260, 150, 30, "patch", false)
      graph.insertVertex(parent1, null, "veth pair", w - 200, 290, 150, 30, "veth", false)
      graph.insertVertex(parent1, null, "LinBr mgmgt iface", w - 200, 320, 150, 30, "linbrif", false)
      graph.insertVertex(parent1, null, "other OVS port", w - 200, 350, 150, 30, "otherOVS", false)
      

      var xstep = 20
      def incXstep(step: Int) = {
        val x = xstep;
        xstep = xstep + step;
        x
      }

      //DRAW ELEMENTS

      //draw physical ifaces 
      var hdraw = h - 70
      ifaces filter (_.ifaceType == "physical") foreach { iface =>
        graph.insertVertex(parent1, null, iface, incXstep(100), hdraw, 80, 30, "physInt", false)
      }

      //draw vlanAlias on the same line
      ifaces filter (_.ifaceType == "vlan_alias") foreach { iface =>
        graph.insertVertex(parent1, null, iface, incXstep(100), hdraw, 90, 30, "vlanalias", false)
      }

      //draw OVS internal interfaces
      xstep = 20
      hdraw = h - 180
      ifaces filter (_.ifaceType == "OVSInternal") foreach { l2tpTun =>
        graph.insertVertex(parent1, null, l2tpTun, incXstep(100), hdraw, 80, 30, "ovsinternal", false)
      }

      //draw linux bridge mgmt interfaces on the same line
      ifaces filter (_.ifaceType == "linuxBridgeInterface") foreach { iface =>
        graph.insertVertex(parent1, null, iface, incXstep(100), hdraw, 80, 30, "linbrif", false)
      }

      //draw patch ports
      xstep = 20
      hdraw = h - 110
      val patchPorts = ovsbridges flatMap (_.ports.filter(_.portType.getOrElse("") == "patch"))
      patchPorts foreach { patchPort =>
        graph.insertVertex(parent1, null, patchPort, incXstep(100), hdraw, 80, 30, "patch", false)
      }

      //draw l2tp tunnels on the same line
      ifaces filter (_.ifaceType == "l2tp") foreach { l2tpTun =>
        graph.insertVertex(parent1, null, l2tpTun, incXstep(100), hdraw, 80, 30, "l2tp", false)
      }

      //draw gre tunnels on the same line
      hdraw = h - 140
      val greTunnels = ovsbridges flatMap (_.ports.filter(_.portType.getOrElse("") == "gre"))
      greTunnels foreach { greTun =>
        graph.insertVertex(parent1, null, greTun, incXstep(100), hdraw, 80, 30, "gre", false)
      }

      //draw veth-pair interfaces on the same line
      ifaces filter (_.ifaceType == "veth-pair") foreach { vethIf =>
        graph.insertVertex(parent1, null, vethIf, incXstep(100), hdraw, 80, 30, "veth", false)
      }

      //draw TUN/TAP interfaces
      hdraw = h - 220
      xstep = 20
      ifaces filter (_.ifaceType == "tuntap") map { iface =>
        graph.insertVertex(parent1, null, iface, incXstep(100), hdraw, 80, 30, "tuntap", false)
      }
      
      //draw remaning OVS ports
      //for example patch port that are not completely deleted and remain as garbage
      //TODO: why do these ports exist?
      hdraw = h - 140
      val otherOVSPorts = for (
          br <- ovsbridges;
          port <- br.ports;
          typ = port.portType.getOrElse("");
          if typ != "gre" &&
          	 typ != "patch" && 
          	 ! ifaces.exists(_.name == port.name) 
          	 ) yield port
          	 
      otherOVSPorts foreach { other  =>
        graph.insertVertex(parent1, null, other, incXstep(100), hdraw, 80, 30, "otherOVS", false)
      }
      
      
      //these are all OVS ports that are not OVS internal 

      //draw OVSBridges 
      hdraw = h - 350
      xstep = 20
      ovsbridges map { br =>
        graph.insertVertex(parent1, null, br, incXstep(200), hdraw, 150, 60, "OVSBr", false)
      }

      //draw Linux Bridges 
      xstep = 20
      hdraw = h - 470
      linuxBridges foreach { lbr =>
        graph.insertVertex(parent1, null, lbr, incXstep(200), hdraw, 150, 60, "linBr", false)
      }

      //DRAW CONNECTIONS

      //draw connections <L2tpTunn mxCell,physical iface mxCell>
      
      
      val vertices: List[mxCell] = graph.getChildVertices(graph.getDefaultParent()).map(_.asInstanceOf[mxCell]).toList

      val totIfaceXList: List[(Iface, mxCell)] = vertices.map(v => (v.getValue(), v)) collect {
        case (iface: Iface, v) => (iface, v)
      }

      val totOVSPortXList: List[(OVSPort, mxCell)] = vertices.map(v => (v.getValue(), v)) collect {
        case (port: OVSPort, v) => (port, v)
      }
      
      
      val l2tptunnXList = totIfaceXList.filter (c => c._1.ifaceType == "l2tp")
      
      val physXList = totIfaceXList.filter (c => c._1.ifaceType == "physical")
      val vethXList = totIfaceXList.filter (c => c._1.ifaceType == "veth-pair")
      val patchXList = totOVSPortXList.filter (c=> c._1.portType == Some("patch"))
      
      
      //println(ovsbridges mkString "\n")
      //println(patchXList mkString "\n")
      
      
      l2tptunnXList foreach { //NOTE: not always the interface used is physical
        case (l2tpIface, l2tpCell) =>
          val physCellOption = totIfaceXList.find {
            case (iface, _) => iface.name == l2tpIface.ifaceUsed.getOrElse("")
          }
          physCellOption match {
            case Some((_, physCell)) => graph.insertEdge(parent1, null, "uses", l2tpCell, physCell)
            case smtgelse => println(s"Could not draw <l2tp,ifaceUsed> binding: mxCell for interface $l2tpIface not found! $smtgelse ")
          }
      }

      val OVSBrXList: List[(OVSBridge, mxCell)] = vertices.collect {
        case cell if cell.getValue().isInstanceOf[OVSBridge] =>
          (cell.getValue().asInstanceOf[OVSBridge], cell)
      }

      //draw connections <OVSBridge,iface> (loop through the OVSPorts of each OVS bridge)
      OVSBrXList foreach {
        case (ovsbr, ovsbrCell) =>
          ovsbr.ports.foreach {
            port =>
              //val ptype = port.portType.getOrElse("")
              if (totIfaceXList.exists(_._1.name == port.name))
                graph.insertEdge(parent1, null, "", ovsbrCell, totIfaceXList.find(_._1.name == port.name).get._2)
              else if (totOVSPortXList.exists(_._1.name == port.name))
                graph.insertEdge(parent1, null, "", ovsbrCell, totOVSPortXList.find(_._1.name == port.name).get._2)
              else {
                println(getOVSBridges mkString "\n");
                println(s"Could not draw <OVSBridge,iface> binding for ${ovsbr.name}: mxCell for interface $port not found!")
              }
          }
      }

      val LinBrXList = vertices.collect {
        case cell if cell.getValue().isInstanceOf[LinuxBr] =>
          (cell.getValue().asInstanceOf[LinuxBr], cell)
      }

      //draw connections from linuxbridge to the ifaces
      LinBrXList foreach {
        case (linbr, linbrCell) =>
          linbr.nics foreach {
            nic =>
              {
                if (totIfaceXList.exists(_._1.name == nic))
                  graph.insertEdge(parent1, null, "", linbrCell, totIfaceXList.find(_._1.name == nic).get._2)
                else if (totOVSPortXList.exists(_._1.name == nic))
                  graph.insertEdge(parent1, null, "", linbrCell, totOVSPortXList.find(_._1.name == nic).get._2)
                else {
                  println(s"Could not draw <LinBridge,iface> binding: mxCell for interface $nic not found!")
                }
              }
          }
      }

      //draw connections between veth pairs
      vethXList foreach {
        case (vethIface, cell) =>
          vethXList.find {
            case (otherVethIface, _) => otherVethIface.name == vethIface.peerIf.get
          } match {
            case Some((_, otherCell)) => graph.insertEdge(parent1, null, "", cell, otherCell)
            case None => println(s"Could not draw veth pair binding: no peer found for $vethIface")
          }
      }
      //draw connections between patch ports
      patchXList foreach {
        case (patch, cell) =>
          val peer_patch = patchXList.find {
            case (port, _) => port.options("peer").replaceAll("\"","") == patch.name
          }
          peer_patch match {
            case Some((_, peerCell)) => graph.insertEdge(parent1, null, "", cell, peerCell)
            case None => println(s"Could not draw patch pair binding: no peer found for $patch")
          }

      }

      //load layout from json file
      loadLayout()

    } finally {
      graph.getModel().endUpdate()
    }

    println("Done drawing.")
  }
  def saveLayout(graph: mxGraph) = {
    println("Saving layout on file.")

    val cells = graph.getChildVertices(graph.getDefaultParent())
    val positions = cells map {
      case c => (graph.convertValueToString(c), graph.getCellGeometry(c).getX(), graph.getCellGeometry(c).getY())
    }

    import spray.json._
    import DefaultJsonProtocol._
    val jsonpos = positions.toJson.prettyPrint
    val f = new File("layout.json")
    val writer = new java.io.PrintWriter(f)
    writer.write(jsonpos)
    writer.close()
  }
  //reorder graphical elements with morphing 
  def useMorphing(graph: mxGraph, graphComponent: mxGraphComponent) {
    // layout using morphing
    val layout1 = new mxFastOrganicLayout(graph)
    graph.getModel().beginUpdate();
    try {
      layout1.execute(graph.getDefaultParent());
    } finally {
      val morph = new mxMorphing(graphComponent, 20, 2, 20);
      morph.addListener(mxEvent.DONE, new mxIEventListener() {
        override def invoke(arg0: Object, arg1: mxEventObject) {
          graph.getModel().endUpdate();
          // fitViewport();
        }
      })
      morph.startAnimation();
    }
  }
}
