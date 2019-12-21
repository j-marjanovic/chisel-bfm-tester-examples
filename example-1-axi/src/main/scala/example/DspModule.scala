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

import chisel3._
import bfmtester._

/** Calculate a+b+c0 and a-b+c1 */
class DspModule extends Module {
  val io = IO(new Bundle {
    val ctrl = new AxiLiteIf(addr_w = 8.W)
    val a = new AxiStreamIf(16.W)
    val b = new AxiStreamIf(16.W)
    val q1 = Flipped(new AxiStreamIf(16.W))
    val q2 = Flipped(new AxiStreamIf(16.W))
  })

  val a_valid = RegInit(Bool(), false.B)
  val b_valid = RegInit(Bool(), false.B)
  val a_reg = Reg(UInt(io.a.tdata.getWidth.W))
  val b_reg = Reg(UInt(io.a.tdata.getWidth.W))

  val q1_valid = RegInit(Bool(), false.B)
  val q2_valid = RegInit(Bool(), false.B)
  val q1_reg = Reg(UInt(io.a.tdata.getWidth.W))
  val q2_reg = Reg(UInt(io.a.tdata.getWidth.W))

  // from/to Axi
  val c0 = Wire(UInt(8.W))
  val c1 = Wire(UInt(8.W))
  val stats_nr_samp = RegInit(UInt(32.W), 0.U)

  // =========================================================================
  //  modules

  val axi_ctrl = Module(new DspModuleAxiLiteSlave)
  io.ctrl <> axi_ctrl.io.ctrl
  c0 := axi_ctrl.io.c0
  c1 := axi_ctrl.io.c1
  axi_ctrl.io.stats_nr_samp := stats_nr_samp

  // =========================================================================
  //  handle inputs

  when(io.a.tvalid && !a_valid) {
    a_valid := true.B
    a_reg := io.a.tdata
  }.elsewhen(io.a.tvalid && a_valid && !q1_valid && !q2_valid) {
      a_valid := true.B
      a_reg := io.a.tdata
    }
    .elsewhen(a_valid && b_valid && !q1_valid && !q2_valid) {
      a_valid := false.B
    }

  when(io.b.tvalid && !b_valid) {
    b_valid := true.B
    b_reg := io.b.tdata
  }.elsewhen(io.a.tvalid && b_valid && !q1_valid && !q2_valid) {
      b_valid := true.B
      b_reg := io.b.tdata
    }
    .elsewhen(a_valid && b_valid && !q1_valid && !q2_valid) {
      b_valid := false.B
    }

  io.a.tready := !a_valid || (a_valid && !q1_valid && !q2_valid)
  io.b.tready := !b_valid || (b_valid && !q1_valid && !q2_valid)

  // =========================================================================
  //  handle outputs

  val q1_result = WireInit(a_reg + b_reg + c0)
  val q2_result = WireInit(a_reg - b_reg + c1)

  when(a_valid && b_valid && !q1_valid) {
    q1_valid := true.B
    q1_reg := q1_result
  }.elsewhen(a_valid && b_valid && q1_valid && io.q1.tready) {
      q1_valid := true.B
      q1_reg := q1_result
    }
    .elsewhen(q1_valid && io.q1.tready) {
      q1_valid := false.B
      q1_reg := 0.U
    }

  when(a_valid && b_valid && !q2_valid) {
    q2_valid := true.B
    q2_reg := q2_result
  }.elsewhen(a_valid && b_valid && q2_valid && io.q2.tready) {
      q2_valid := true.B
      q2_reg := q2_result
    }
    .elsewhen(q2_valid && io.q2.tready) {
      q2_valid := false.B
      q2_reg := 0.U
    }

  io.q1.tvalid := q1_valid
  io.q1.tdata := q1_reg
  io.q2.tvalid := q2_valid
  io.q2.tdata := q2_reg

  io.q1.tlast := false.B
  io.q1.tuser := 0.U
  io.q2.tlast := false.B
  io.q2.tuser := 0.U

}
