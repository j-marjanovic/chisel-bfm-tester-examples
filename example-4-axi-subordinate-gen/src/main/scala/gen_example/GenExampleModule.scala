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

import bfmtester.util._
import bfmtester._
import chisel3._

class GenExampleModule extends Module {
  import bfmtester.util.AxiLiteSubordinateGenerator._

  // format: off
  val area_map = new AreaMap(
    new Reg("ID_REG", 0,
      new Field("ID", hw_access = Access.NA, sw_access = Access.R,  hi = 31, Some(0), reset = Some(0xa819e201L.U))
    ),
    new Reg("VERSION", 4,
      new Field("PATCH", hw_access = Access.W, sw_access = Access.R, hi = 7, lo = Some(0), reset = Some(3.U)),
      new Field("MINOR", hw_access = Access.W, sw_access = Access.R, hi = 15, lo = Some(8), reset = Some(2.U)),
      new Field("MAJOR", hw_access = Access.W, sw_access = Access.R, hi = 23, lo = Some(16), reset = Some(1.U))
    ),
    new Reg("SCRATCH", 0xc,
      new Field("FIELD", hw_access = Access.NA, sw_access = Access.RW, hi = 31, lo = Some(0))
    ),
    new Reg("STATUS", 0x10,
      new Field("DONE_M1", hw_access = Access.W, sw_access = Access.R, hi = 0, lo = None, singlepulse = true),
      new Field("DONE_M2", hw_access = Access.W, sw_access = Access.R, hi = 1, lo = None, singlepulse = true),
    ),
    new Reg("CONTROL", 0x14,
      new Field("START_M1", hw_access = Access.R, sw_access = Access.RW, hi = 0, lo = None, singlepulse = true),
      new Field("START_M2", hw_access = Access.R, sw_access = Access.RW, hi = 1, lo = None, singlepulse = true),
      new Field("CLEAR_M1", hw_access = Access.R, sw_access = Access.RW, hi = 8, lo = None, singlepulse = true),
      new Field("CLEAR_M2", hw_access = Access.R, sw_access = Access.RW, hi = 9, lo = None, singlepulse = true),
      new Field("ENABLE", hw_access = Access.R, sw_access = Access.RW, hi = 31, lo = None)
    ),
    new Mem("M1", addr = 0x1000, nr_els = 256, data_w = 16),
    new Mem("M2", addr = 0x2000, nr_els = 256, data_w = 32)
  )
  // format: on

  val io = IO(new Bundle {
    val ctrl = new AxiLiteIf(addr_w = 14.W)
  })

  val mod_ctrl = Module(new AxiLiteSubordinateGenerator(area_map = area_map, addr_w = 14))
  io.ctrl <> mod_ctrl.io.ctrl

  mod_ctrl.io.inp("VERSION_MAJOR") := 0x03.U
  mod_ctrl.io.inp("VERSION_MINOR") := 0x14.U
  mod_ctrl.io.inp("VERSION_PATCH") := 0x16.U

  val mod_mem1 = Module(new DualPortRam(16, 256))
  mod_mem1.io.clk := this.clock
  mod_mem1.io.addra := mod_ctrl.io.out("MEM_M1_ADDR").asUInt()
  mod_mem1.io.dina := mod_ctrl.io.out("MEM_M1_DIN").asUInt()
  mod_mem1.io.wea := mod_ctrl.io.out("MEM_M1_WE").asUInt().asBool()
  mod_ctrl.io.inp("MEM_M1_DOUT") := mod_mem1.io.douta

  val mod_mem2 = Module(new DualPortRam(32, 256))
  mod_mem2.io.clk := this.clock
  mod_mem2.io.addra := mod_ctrl.io.out("MEM_M2_ADDR").asUInt()
  mod_mem2.io.dina := mod_ctrl.io.out("MEM_M2_DIN").asUInt()
  mod_mem2.io.wea := mod_ctrl.io.out("MEM_M2_WE").asUInt().asBool()
  mod_ctrl.io.inp("MEM_M2_DOUT") := mod_mem2.io.douta

  val mod_dg1 = Module(new DataGenerator(16, 256))
  mod_dg1.io.enable := mod_ctrl.io.out("CONTROL_ENABLE")
  mod_dg1.io.start := mod_ctrl.io.out("CONTROL_START_M1")
  mod_dg1.io.clear := mod_ctrl.io.out("CONTROL_CLEAR_M1")
  mod_ctrl.io.inp("STATUS_DONE_M1") := mod_dg1.io.done
  mod_mem1.io.addrb := mod_dg1.io.addr
  mod_mem1.io.dinb := mod_dg1.io.dout
  mod_mem1.io.web := mod_dg1.io.we

  val mod_dg2 = Module(new DataGenerator(32, 256))
  mod_dg2.io.enable := mod_ctrl.io.out("CONTROL_ENABLE")
  mod_dg2.io.start := mod_ctrl.io.out("CONTROL_START_M2")
  mod_dg2.io.clear := mod_ctrl.io.out("CONTROL_CLEAR_M2")
  mod_ctrl.io.inp("STATUS_DONE_M2") := mod_dg2.io.done
  mod_mem2.io.addrb := mod_dg2.io.addr
  mod_mem2.io.dinb := mod_dg2.io.dout
  mod_mem2.io.web := mod_dg2.io.we
}
