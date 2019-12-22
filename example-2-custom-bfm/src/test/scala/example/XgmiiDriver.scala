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
import chisel3.Bits

import scala.collection.mutable.ListBuffer

class XgmiiDriverData(val d: BigInt, val c: BigInt)

class XgmiiDriver(val interface: XgmiiInterface,
                  val peek: Bits => BigInt,
                  val poke: (Bits, BigInt) => Unit,
                  val println: String => Unit)
    extends Bfm {

  private val data_buf = ListBuffer[XgmiiDriverData]()

  def send(x: XgmiiDriverData): Unit = data_buf += x
  def send(xs: Iterable[XgmiiDriverData]): Unit = data_buf ++= xs

  private def bl_to_bigint(shift: Int): List[BigInt] => BigInt =
    _.foldRight(BigInt(0))((x, y) => x | (y << shift))

  private def drive_empty_cycle(): Unit = {
    poke(interface.data, 0x0707070707070707L)
    poke(interface.ctrl, 0xFF)
  }

  override def update(t: Long, poke: (Bits, BigInt) => Unit): Unit = {
    if (data_buf.nonEmpty) {
      val nr_els = Math.min(8, data_buf.length)

      val dl: List[BigInt] = data_buf.toList.slice(0, nr_els).map(_.d) ++ List
        .fill(8 - nr_els)(BigInt(0x07))
      val cl: List[BigInt] = data_buf.toList.slice(0, nr_els).map(_.c) ++ List
        .fill(8 - nr_els)(BigInt(0x1))
      val d = bl_to_bigint(8)(dl)
      val c = bl_to_bigint(1)(cl)
      data_buf.remove(0, nr_els)

      println(f"${t}%5d XgmiiDriver: send ${d}%016x ${c}%02x")

      poke(interface.data, d)
      poke(interface.ctrl, c)
    } else {
      drive_empty_cycle()
    }
  }

  private def init(): Unit = {
    println(f"${0}%5d XgmiiDriver: init()")
    drive_empty_cycle()
  }

  init()

}
