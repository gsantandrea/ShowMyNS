package showmyns

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner



@RunWith(classOf[JUnitRunner])
class NetCompSuite extends FunSuite {
  
import Actions._
  
  test("192.168.0.255 is contained in 192.168.0.0/24 "){
    assert(containsIP("192.168.0.0/24","192.168.0.255")===true)
  }
  test("192.168.0.255 is NOT contained in 191.168.0.0/24"){
    assert(containsIP("191.168.0.0/24","192.168.0.255")===false)
  }
}
