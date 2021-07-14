package viper.silicon

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScObject, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.{ScClassImpl, SyntheticMembersInjector}
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.plugins.scala.lang.psi.types.TypePresentationContext

import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.roots.ProjectRootManager
import scala.xml.XML

final class SiliconInjector extends SyntheticMembersInjector {
  private val logger = Logger.getInstance(classOf[SiliconInjector])
  logger.info("SiliconInjector plugin was started.")

  private val qualifiedName = "flyweight"
  private val cache = new scala.collection.mutable.HashMap[(String, String), Option[(String, Seq[String], Seq[String])]]



  /*
  var configCache: Option[Option[(String, Seq[String], Seq[String])]] = None

  def getConfig(source: ScTypeDefinition): Option[(String, Seq[String], Seq[String])] = {
    configCache match {
      case None => {
        val projectFilePath = source.getProject.getProjectFilePath
        val until = projectFilePath.indexOfSlice(".idea")
        val configFilePath = projectFilePath.slice(0, until) + "plugin-config.xml"
        //logger.info(s"Config file path is: ${configFilePath}")
        val config = if (new java.io.File(configFilePath).exists) {
          val xml = XML.loadFile(configFilePath)
          val qualifiedName = (xml \\ "macroAnnotationName").text
          val members = (xml \\ "member").map(member => member.text)
          val functions = (xml \\ "function").map(function => function.text)
          Some((qualifiedName, members, functions))
        } else {
          None
        }
        configCache = Some(config)
        config
      }
      case Some(config) => config
    }

  }
  */
  private def getName(source: ScTypeDefinition): Option[String] = {
    source match {
      case clazz: ScClass => {
        return Some(clazz.getName)
      }
      case obj: ScObject => {
        obj.fakeCompanionClassOrCompanionClass match {
          case clazz: ScClass => {
            return Some(clazz.getName)
          }
          case _ => None
        }
      }
      case _ => None
    }
    None
  }


  private def getConfig(source: ScTypeDefinition): Option[(String, Seq[String], Seq[String])] = {
    val projectFilePath = source.getProject.getProjectFilePath
    val termName = getName(source)
    termName match {
      case Some(termName) => {
        cache.get((projectFilePath, termName)) match {
          case Some(data) => {
            logger.info(s"Loaded data from file: $data")
            data
          }
          case None => {
            val until = projectFilePath.indexOfSlice(".idea")
            val configFilePath = projectFilePath.slice(0, until) + ".plugin/" + termName + ".xml"
            logger.info(s"Searching for plugin configuration $configFilePath")
            //logger.info(s"Config file path is: ${configFilePath}")
            val data = if (new java.io.File(configFilePath).exists) {
              logger.info(s"File exists, loading it ...")
              val xml = XML.loadFile(configFilePath)
              val qualifiedName = (xml \\ "macroAnnotationName").text
              val members = (xml \\ "member").map(member => member.text)
              logger.info(s"Members: $members")
              val functions = (xml \\ "function").map(function => function.text)
              logger.info(s"Functions: $functions")
              Some((qualifiedName, members, functions))
            } else {
              logger.info(s"File not found.")
              None
            }
            cache.addOne(((projectFilePath, termName), data))
            logger.info(s"Loaded data from cache: $data")
            data
          }
        }
      }
      case None => None
    }
  }


  override def needsCompanionObject(source: ScTypeDefinition): Boolean = {
    getConfig(source) match {
      case Some((qualifiedName, _, _)) => source.findAnnotationNoAliases(qualifiedName) != null
      case None => false
    }
  }

  override def injectFunctions(source: ScTypeDefinition): Seq[String] = {
    getConfig(source) match {
      case Some((qualifiedName, _, functions)) => {
        source match {
          case obj: ScObject =>
            obj.fakeCompanionClassOrCompanionClass match {
              case clazz: ScClass if clazz.findAnnotationNoAliases(qualifiedName) != null => {
                /*
                val params = clazz.constructor.get.parameterList.params

                val applyArgs =
                  params
                    .map(p =>s"${p.name}: ${p.`type`().get.presentableText(TypePresentationContext.emptyContext)}")
                    .mkString(", ")

                val applyDef = s"def apply($applyArgs): ${clazz.getName} = ???"

                val unapplyTypes =
                  params
                    .map(_.`type`().get.presentableText(TypePresentationContext.emptyContext))
                    .mkString(", ")

                //logger.info(s"params for ${clazz.getName}: $params")
                //logger.info(s"params.isEmpty for ${clazz.getName}: ${params.isEmpty}")
                //logger.info(s"params.lengthCompare(1) for ${clazz.getName}: ${params.lengthCompare(1)}")
                //logger.info(s"unapplyTypes for ${clazz.getName}: $unapplyTypes")

                val unapplyDef =
                  if (params.isEmpty) {
                    s"def unapply(obj: ${clazz.getName}): Boolean = ???"
                  } else if (params.lengthCompare(1) == 0) {
                    s"def unapply(obj: ${clazz.getName}): Option[$unapplyTypes] = ???"
                  } else {
                    s"def unapply(obj: ${clazz.getName}): Option[($unapplyTypes)] = ???"
                  }

                logger.info(s"Generating apply for ${clazz.getName}: $applyDef")
                logger.info(s"Generating unapply for ${clazz.getName}: $unapplyDef")

                Seq(applyDef, unapplyDef) ++ */
                functions
              }
              case _ => Seq.empty
            }

          case _ => Seq.empty
        }
      }
      case None => Seq.empty
    }

  }

  override def injectMembers(source: ScTypeDefinition): Seq[String] = {
    getConfig(source) match {
      case Some((qualifiedName, members, _)) => {
        source match {
          case clazz: ScClass if clazz.findAnnotationNoAliases(qualifiedName) != null =>
            /*
            val params = clazz.constructor.get.parameterList.params

            val copyArgs =
              params
                .map(p =>s"${p.name}: ${p.`type`().get.presentableText(TypePresentationContext.emptyContext)} = ${p.name}")
                .mkString(", ")

            val copyDef = s"def copy($copyArgs): ${clazz.getName} = ???"

            logger.info(s"Generating copy for ${clazz.getName}: $copyDef")

            Seq(copyDef) ++ */
            members
          case _ =>
            Seq.empty
        }
      }
      case None => Seq.empty
    }


  }
}