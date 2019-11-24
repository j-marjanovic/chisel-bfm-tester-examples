
package example

import chisel3._

object DspModuleMain extends App {
  chisel3.Driver.execute(
    Array[String]("--target-dir", "output") ++ args,
    () => new DspModule)
}
