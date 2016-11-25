package view

import com.mxgraph.util.mxConstants
object Styles {
  
  
  val ovsBrStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(true),
      mxConstants.STYLE_FILLCOLOR -> "#FF8080")
  val physIntStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#99CCFF")

  val linBrStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(true),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#9999FF")

  val linbrIfStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#7f7f7f")
      
  val l2tpStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#EFF490")
      
  val vlanAliasStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#F95757")
  
  val ovsInternalStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#a5ffd2")
      

  val tuntapStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#0c7252")
            

  val greStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#f9ca63")
      
  val vethStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#e0e9ff")
  val patchStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#ffb31c")      
  val otherOVSStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#4a73b9")
  val otherIfaceStyle: Map[String, Object] =
    Map(mxConstants.STYLE_SHAPE -> mxConstants.SHAPE_RECTANGLE,
      mxConstants.STYLE_OPACITY -> new java.lang.Integer(80),
      mxConstants.STYLE_ROUNDED -> new java.lang.Boolean(false),
      mxConstants.STYLE_FONTCOLOR -> "#000000",
      mxConstants.STYLE_FILLCOLOR -> "#c0ce9a")

}
