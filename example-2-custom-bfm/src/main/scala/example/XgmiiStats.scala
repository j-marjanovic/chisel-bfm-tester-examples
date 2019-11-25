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
import chisel3.util.PopCount

/** Counts the number of bytes and packets on XGMII interface */
class XgmiiStats extends Module {
  val io = IO(new Bundle {
    val xgmii_in = Flipped(new XgmiiInterface)
    val xgmii_out = new XgmiiInterface
    val ctrl = new AxiLiteIf(4.W)
  })

  // from/to Axi
  val cntr_bytes = RegInit(UInt(32.W), 0.U)
  val cntr_pkts  = RegInit(UInt(32.W), 0.U)
  val cntr_bytes_clear = Wire(Bool())
  val cntr_pkts_clear = Wire(Bool())

  // =========================================================================
  //  modules

  val axi_ctrl = Module(new XgmiiStatsAxiLiteSlave)
  io.ctrl <> axi_ctrl.io.ctrl
  axi_ctrl.io.cntr_bytes := cntr_bytes
  axi_ctrl.io.cntr_pkts := cntr_pkts
  cntr_bytes_clear := axi_ctrl.io.cntr_bytes_clear
  cntr_pkts_clear := axi_ctrl.io.cntr_pkts_clear

  // =========================================================================
  //  logic

  val xgmii_reg = RegNext(io.xgmii_in)
  val xgmii_prev = RegNext(xgmii_reg)
  io.xgmii_out := xgmii_reg

  when (cntr_bytes_clear) {
    cntr_bytes := 0.U
  } .otherwise {
    cntr_bytes := cntr_bytes + PopCount(xgmii_reg.ctrl ^ 0xFF.U)
  }

  when (cntr_pkts_clear) {
    cntr_pkts := 0.U
  } .elsewhen ((xgmii_prev.ctrl === 0.U) && (xgmii_reg.ctrl =/= 0.U)) {
    cntr_pkts := cntr_pkts + 1.U
  }

}
