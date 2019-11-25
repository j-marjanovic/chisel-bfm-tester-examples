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
  // main
  println(f"${t}%5d Test starting...")

  step(10)

  println(f"${t}%5d Test finished.")
}