/*
MIT License

Copyright (c) 2019 Jan Marjanovic

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package example

import bfmtester._

class XgmiiStatsTester(c: XgmiiStats) extends BfmTester(c) {

  //==========================================================================
  // modules

  val m_drv = new XgmiiDriver(c.io.xgmii_in, peek, poke, println)
  val m_mon = new XgmiiMonitor(c.io.xgmii_out, peek, poke, println)
  val axi_ctrl = BfmFactory.create_axilite_master(c.io.ctrl, ident="Control")

  //==========================================================================
  // utils
  def send_pkt(lst: List[BigInt]): Unit = {
    lst.foreach((x: BigInt) => m_drv.send(new XgmiiDriverData(x, 0)))
  }

  //==========================================================================
  // main
  val C_PKT1_DATA = List[BigInt](0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0xd5,
    0x01, 0x02, 0x03, 0x04, 0x05, 0x6,
    0x21, 0x22, 0x23, 0x24, 0x25, 0x26,
    0x80, 0x06,
    0x11, 0x22, 0x33, 0x44
  )

  println(f"${t}%5d Test starting...")

  step(5)

  // drive one packet
  m_drv.send(new XgmiiDriverData(0xfb, 1))
  send_pkt(C_PKT1_DATA)
  m_drv.send(new XgmiiDriverData(0xfd, 1))
  step(5 + C_PKT1_DATA.length / 8)
  val resp: List[BigInt] = m_mon.data_get()

  // check the packet
  expect(resp.length == C_PKT1_DATA.length, "packet length")
  (resp zip C_PKT1_DATA).zipWithIndex.foreach(
    (xyi: ((BigInt, BigInt), Int)) => expect(xyi._1._1 == xyi._1._2,
      f"packet element ${xyi._2} (got ${xyi._1._1}%02x, expect ${xyi._1._2}%02x)")
  )

  // check status on AXI4-Lite port
  axi_ctrl.readPush(0x8)
  step(5)
  val cntr_pkts = axi_ctrl.getResponse().get.rd_data
  expect(cntr_pkts == 1, "AXI4-Lite register: packet counter")

  axi_ctrl.readPush(0xc)
  step(5)
  val cntr_data = axi_ctrl.getResponse().get.rd_data
  expect(cntr_data == C_PKT1_DATA.length, "AXI4-Lite register: packet length")

  println(f"${t}%5d Test finished.")
}
