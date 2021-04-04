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

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

class DataGenerator(val data_w: Int, val nr_els: Int) extends Module {
  val io = IO(new Bundle {
    // control interface
    val enable = Input(Bool())
    val start = Input(Bool())
    val clear = Input(Bool())
    val done = Output(Bool())

    // mem interface
    val dout = Output(UInt(data_w.W))
    val addr = Output(UInt(log2Ceil(nr_els).W))
    val we = Output(Bool())
  })

  object State extends ChiselEnum {
    val sIdle, sWrite, sDone = Value
  }
  val state = RegInit(State.sIdle)

  val cntr = Reg(UInt(log2Ceil(nr_els).W))

  switch (state) {
    is (State.sIdle) {
      when (io.enable && io.start) {
        state := State.sWrite
        cntr := 0.U
      }
    }
    is (State.sWrite) {
      cntr := cntr + 1.U
      when (cntr === (nr_els-1).U) {
        state := State.sDone
      }
    }
    is (State.sDone) {
      when (io.clear) {
        state := State.sIdle
      }
    }
  }

  io.done := state === State.sDone

  assert(data_w % 8 == 0, "Data width must be a multiple of 8")
  val out_vec = WireInit(VecInit(Seq.fill(data_w/8)(cntr(7, 0))))
  io.dout := out_vec.asUInt()
  io.addr := cntr
  io.we := state === State.sWrite

}
