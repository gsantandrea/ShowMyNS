
package showmyns

import javax.swing.SwingUtilities



object MainClass {
  def main(args: Array[String]) {
    val orgLayout = args.contains("-orglayout")
    SwingUtilities.invokeLater(new Runnable() {
      def run() {
        val frame = new MFrame2("Show my network state", orgLayout)
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE) //for interoperability with nailgun
      }
    })
  }

}
