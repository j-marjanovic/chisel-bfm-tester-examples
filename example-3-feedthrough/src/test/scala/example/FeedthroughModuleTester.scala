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

class FeedthroughModuleTester(c: FeedthroughModule) extends BfmTester(c) {

  //==========================================================================
  // modules

  val m_mst = BfmFactory.create_axis_master(c.io.in)
  val m_slv = BfmFactory.create_axis_slave(c.io.out)
  // m_slv.setVerbose(true)
  m_slv.backpressure = 0.9

  //==========================================================================
  // main
  println(f"${t}%5d Test starting...")

  m_mst.stimAppend(22, 0)
  m_mst.stimAppend(33, 0)
  m_mst.stimAppend(44, 0)
  step(50)

  val resps: List[(BigInt, BigInt)] = m_slv.respGet()
  expect(resps.size == 3, "number of samples received")
  expect(resps(n = 0)._1 == 22, "first sample")
  expect(resps(n = 1)._1 == 33, "second sample")
  expect(resps(n = 2)._1 == 44, "third sample")

  println(f"${t}%5d Test finished.")
}
