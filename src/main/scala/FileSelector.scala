package com.pokutuna.n7logger

import java.io.File
import scala.util.matching.Regex

object FileSelector {

  implicit def stringToFile(string: String): File = new File(string)

  def select(rootPath: File, pattern: Regex = ".*".r): Seq[File] = {
    require(rootPath.isDirectory, "FileSelector requires directory as rootPath")

    def expand(file: File): Seq[File] = {
      if(file.isDirectory){
        file.listFiles.flatMap(expand _)
      } else {
        if(pattern.findFirstIn(file.getName) != None) List(file) else List()
      }
    }
    expand(rootPath)
  }
}
