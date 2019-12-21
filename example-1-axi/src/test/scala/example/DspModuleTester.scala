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

class DspModuleTester(c: DspModule) extends BfmTester(c) {

  //==========================================================================
  // modules

  val mst_a = BfmFactory.create_axis_master(c.io.a, "Master A")
  val mst_b = BfmFactory.create_axis_master(c.io.b, "Master B")

  val slv_q1 = BfmFactory.create_axis_slave(c.io.q1, "Slave 1")
  val slv_q2 = BfmFactory.create_axis_slave(c.io.q2, "Slave 2")
  slv_q1.setVerbose(false)
  slv_q2.setVerbose(false)

  val axi_ctrl = BfmFactory.create_axilite_master(c.io.ctrl, ident = "Lite")

  //==========================================================================
  // main
  println(f"${t}%5d Test starting...")

  axi_ctrl.readPush(0)
  axi_ctrl.readPush(4)

  mst_a.stimAppend(2, 0)
  step(1)
  mst_b.stimAppend(1, 0)

  step(5)

  mst_a.stimAppend(0, 0)
  mst_b.stimAppend(0, 0)

  step(5)

  axi_ctrl.writePush(0xc, 5)
  axi_ctrl.writePush(0x10, 6)
  mst_a.stimAppend(7, 0)
  mst_b.stimAppend(8, 0)

  step(10)

  val id_reg = axi_ctrl.getResponse().get.rd_data
  println(f"${t}%5d Ident reg = ${id_reg}%08x")

  println(f"${t}%5d Test finished.")
}
