package com.rawlabs.versioner

import sbt._
import sbt.Keys._
import scala.sys.process._

object VersionerPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = plugins.JvmPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    version := {
      def exec(cmd: String): String = {
        try {
          cmd.!!.trim
        } catch {
          case _: Exception => ""
        }
      }

      val tagsOnHead = exec("git tag --points-at HEAD")
      val latestTag = exec("git describe --tags --abbrev=0")
      val branchNameRaw = exec("git rev-parse --abbrev-ref HEAD")
      val branchName = branchNameRaw.replaceAll("[^a-zA-Z0-9.-]", ".")

      def stripV(tag: String): String = {
        if (tag.startsWith("v")) tag.stripPrefix("v") else tag
      }

      if (tagsOnHead.nonEmpty) {
        // When the current commit is tagged, use the tag as the version without the leading 'v'
        val tagVersion = stripV(tagsOnHead.split("\n").head.trim)
        tagVersion
      } else if (latestTag.nonEmpty && branchName.nonEmpty) {
        // When on a branch, use <latest_tag>-<branch_name>-SNAPSHOT as the version
        val cleanLatestTag = stripV(latestTag)
        s"$cleanLatestTag-$branchName-SNAPSHOT"
      } else {
        // Fallback version if there are no tags yet
        "0.0.0-SNAPSHOT"
      }
    }
  )
}
