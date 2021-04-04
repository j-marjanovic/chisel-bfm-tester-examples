/*
Copyright (c) 2018-2021 Jan Marjanovic

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

package gen_example

import bfmtester._

class GenExampleTester(c: GenExampleModule) extends BfmTester(c) {
  def read_blocking(addr: BigInt, CYC_TIMEOUT: Int = 100): BigInt = {
    mod_axi_manager.readPush(addr)
    for (i <- 0 to CYC_TIMEOUT) {
      val resp = mod_axi_manager.getResponse()
      if (resp.isDefined) {
        return resp.get.rd_data
      }
      step(1)
    }

    throw new RuntimeException("AXI read timeout")
  }

  def write_blocking(addr: BigInt, data: BigInt, CYC_TIMEOUT: Int = 100): Unit = {
    mod_axi_manager.writePush(addr, data)
    for (i <- 0 to CYC_TIMEOUT) {
      val resp = mod_axi_manager.getResponse()
      if (resp.isDefined) {
        return
      }
      step(1)
    }

    throw new RuntimeException("AXI write timeout")
  }

  val mod_axi_manager = BfmFactory.create_axilite_master(c.io.ctrl)

  step(10)

  // some basic checks (ID register, version, scratch)
  val id_reg = read_blocking(0)
  expect(id_reg == 0xa819e201L, "ID reg")

  val ver_reg = read_blocking(addr = 4)
  expect(ver_reg == 0x031416, "Version register")

  val scratch_reg_val = 271828182
  write_blocking(0xc, scratch_reg_val)
  val scratch_reg_readback = read_blocking(0xc)
  expect(scratch_reg_val == scratch_reg_readback, "Scratch reg write + read")

  // blank check
  expect(read_blocking(0x1000) == 0, "mem1 empty")
  expect(read_blocking(0x1004) == 0, "mem1 empty")
  expect(read_blocking(0x1008) == 0, "mem1 empty")

  expect(read_blocking(0x2000) == 0, "mem2 empty")
  expect(read_blocking(0x2004) == 0, "mem2 empty")
  expect(read_blocking(0x2008) == 0, "mem2 empty")

  // start data gen 1
  write_blocking(0x14, 0x80000001L)
  step(300)

  // check mem 1 done
  expect(read_blocking(0x10) == 0x1, "mem1 done")

  // check mem 1 content
  expect(read_blocking(0x1000) == 0, "mem1 addr 0")
  expect(read_blocking(0x1004) == 0x101, "mem1 addr 4")
  expect(read_blocking(0x1008) == 0x202, "mem1 addr 8")
  expect(read_blocking(0x13fc) == 0xffff, "mem1 addr 0x3fc")
  expect(read_blocking(0x1400) == 0xdeadbeefL, "mem1 above the addr")

  // check mem 1 done cleared
  write_blocking(0x14, 0x00000100L)
  expect(read_blocking(0x10) == 0x0, "mem1 done")

  // start data gen 2
  write_blocking(0x14, 0x80000002L)
  step(300)

  // check mem 1 done
  expect(read_blocking(0x10) == 0x2, "mem2 done")

  // check mem 1 content
  expect(read_blocking(0x2000) == 0, "mem2 addr 0")
  expect(read_blocking(0x2004) == 0x01010101, "mem2 addr 4")
  expect(read_blocking(0x2008) == 0x02020202, "mem2 addr 8")
  expect(read_blocking(0x23fc) == 0xffffffffL, "mem2 addr 0x3fc")
  expect(read_blocking(0x2400) == 0xdeadbeefL, "mem2 above the addr")

  // check mem 1 done cleared
  write_blocking(0x14, 0x00000200L)
  expect(read_blocking(0x10) == 0x0, "mem2 done")
}
