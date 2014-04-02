package showmyns

import javax.swing.JPopupMenu
import javax.swing.AbstractAction
import javax.swing.JMenuItem
import com.mxgraph.model.mxCell
import javax.swing.JMenu

class PopupMenu(frame: MFrame2) extends JPopupMenu{
  val g= frame.graphComponent.getGraph()
  //val selected = !g.isSelectionEmpty();
  val selectedCell:Option[mxCell] = Option(g.getSelectionCell()).map(_.asInstanceOf[mxCell])
  
  //val menuItem = new JMenuItem("A popup menu item");
  selectedCell match {
    case Some(cell) =>  cell.getValue() match {
      case i:Iface => 
        add(new Sniff(i.name))
        if(i.ifaceType=="veth-pair") add(new RemVethPair(i.name,frame))
        if(i.ifaceType=="tuntap") add(new RemTunTap(i.name,frame))
        
      case p:OVSPort => 
        add(new Sniff(p.name)) 
      case ovsbr:OVSBridge =>
        add(new AddPortToOvs(ovsbr.name,frame))
        add(new RemPortFromOvs(ovsbr.name,frame))
        add(new RemoveOvs(ovsbr.name,frame))
        add(new AddOVSInternal(ovsbr.name,frame))
        
      case lbr:LinuxBr=>
        add(new RemoveLinBr(lbr.name,frame))
        
      case _ =>
    }
    case None =>
        add(new CreateOvs(frame))
        add(new CreateLinBr(frame))
        add(new CreateVethPair(frame))
//      val submenu = new JMenu("Create element");
//      val menuItem = new JMenuItem("An item in the submenu");
//      submenu.add(menuItem);
//	  add(submenu);

  }
  

  

  
}