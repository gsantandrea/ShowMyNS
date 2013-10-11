package showmyns

/*
 * These are the output from H2 host machine
 * TODO: make a consistent set of sample outputs
 * 
 */

object SampleOutputs {
  val ip_route = ""
  val phys = ""
  val ipaddr = List(
      ("",
"""
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN 
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,PROMISC,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 08:00:27:b6:03:76 brd ff:ff:ff:ff:ff:ff
    inet6 fe80::a00:27ff:feb6:376/64 scope link 
       valid_lft forever preferred_lft forever
3: eth1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 08:00:27:22:37:db brd ff:ff:ff:ff:ff:ff
    inet 10.10.10.51/24 brd 10.10.10.255 scope global eth1
    inet6 fe80::a00:27ff:fe22:37db/64 scope link 
       valid_lft forever preferred_lft forever
4: eth2: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 08:00:27:5e:f1:97 brd ff:ff:ff:ff:ff:ff
    inet 10.20.20.51/24 brd 10.20.20.255 scope global eth2
    inet6 fe80::a00:27ff:fe5e:f197/64 scope link 
       valid_lft forever preferred_lft forever
5: br-int: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN 
    link/ether 0a:bd:6a:5f:da:4d brd ff:ff:ff:ff:ff:ff
7: br-ex: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UNKNOWN 
    link/ether 08:00:27:b6:03:76 brd ff:ff:ff:ff:ff:ff
    inet 1.1.1.1/24 brd 1.1.1.255 scope global br-ex
    inet6 fe80::a00:27ff:feb6:376/64 scope link 
       valid_lft forever preferred_lft forever
9: tap1cf5ad8d-c5: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN 
    link/ether 02:55:2c:07:70:55 brd ff:ff:ff:ff:ff:ff
12: br-tun: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN 
    link/ether 96:08:6b:b4:ee:4b brd ff:ff:ff:ff:ff:ff
""")
      )	
  val ovs_show = """
539cf4b9-0203-476d-9dfc-de08a6fe9c70
    Bridge br-ex
        Port br-ex
            Interface br-ex
                type: internal
        Port phy-br-ex
            Interface phy-br-ex
        Port "eth0"
            Interface "eth0"
    Bridge br-int
        Port int-br-tun
            Interface int-br-tun
        Port int-br-ex
            Interface int-br-ex
        Port "tap1cf5ad8d-c5"
            Interface "tap1cf5ad8d-c5"
                type: internal
        Port br-int
            Interface br-int
                type: internal
        Port patch-tun
            Interface patch-tun
                type: patch
                options: {peer=patch-int}
    Bridge br-tun
        Port br-tun
            Interface br-tun
                type: internal
        Port patch-int
            Interface patch-int
                type: patch
                options: {peer=patch-tun}
    ovs_version: "1.4.0+build0"
"""
  val ltun = ""
  val lsess = ""
  val brctl = ""  
      
}


   //    
    //    val o = """
    // Bridge br-tun
    //        Port br-tun
    //            Interface br-tun
    //                type: internal
    //        Port "gre-2"
    //            Interface "gre-2"
    //                type: gre
    //                options: {in_key=flow, out_key=flow, remote_ip="10.10.10.5"}
    //        Port "gre-4"
    //            Interface "gre-4"
    //                type: gre
    //                options: {in_key=flow, out_key=flow, remote_ip="10.10.10.7"}
    //        Port "gre-5"
    //            Interface "gre-5"
    //                type: gre
    //                options: {in_key=flow, out_key=flow, remote_ip="10.10.10.8"}
    //        Port patch-int
    //            Interface patch-int
    //                type: patch
    //                options: {peer=patch-tun}
    //        Port "gre-6"
    //            Interface "gre-6"
    //                type: gre
    //                options: {in_key=flow, out_key=flow, remote_ip="10.10.10.90"}
    //        Port "gre-3"
    //            Interface "gre-3"
    //                type: gre
    //               options: {in_key=flow, out_key=flow, remote_ip="10.10.10.6"}
    //    Bridge br-int
    //        Port "qr-922c91a2-67"
    //            tag: 4
    //            Interface "qr-922c91a2-67"
    //                type: internal
    //        Port br-int
    //            Interface br-int
    //                type: internal
    //        Port "qr-91d5396c-ea"
    //            tag: 2
    //            Interface "qr-91d5396c-ea"
    //                type: internal
    //        Port "tap18b780b1-b6"
    //            tag: 1
    //            Interface "tap18b780b1-b6"
    //                type: internal
    //        Port patch-tun
    //            Interface patch-tun
    //                type: patch
    //                options: {peer=patch-int}
    //        Port "tapecdc8545-a6"
    //            tag: 2
    //            Interface "tapecdc8545-a6"
    //                type: internal
    //        Port "tap7ea1345f-c1"
    //            tag: 4
    //            Interface "tap7ea1345f-c1"
    //                type: internal
    //        Port "tapdb73dc75-b0"
    //            tag: 3
    //            Interface "tapdb73dc75-b0"
    //                type: internal
    //        Port "qr-db59ddd4-e3"
    //            tag: 1
    //            Interface "qr-db59ddd4-e3"
    //                type: internal
    //        Port "qr-a8ffe9d6-99"
    //            tag: 2
    //            Interface "qr-a8ffe9d6-99"
    //                type: internal
    //    Bridge br-ex
    //        Port br-ex
    //            Interface br-ex
    //                type: internal
    //        Port "eth0"
    //            Interface "eth0"
    //        Port "qg-4a73d26e-6d"
    //            Interface "qg-4a73d26e-6d"
    //                type: internal
    //        Port "qg-8fa55eb3-45"
    //            Interface "qg-8fa55eb3-45"
    //                type: internal    
    //"""
