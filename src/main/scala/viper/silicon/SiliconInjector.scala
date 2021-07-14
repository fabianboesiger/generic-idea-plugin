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

  // Cache to speed up things for testing. Plugin may need to be reloaded if data in cache is expired.
  private val cache = new scala.collection.mutable.HashMap[(String, String), Option[(String, Seq[String], Seq[String])]]

  // Extracts the class name of a ScTypeDefinition.
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

  // Loads the XML data from the .plugin directory.
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
            members
          case _ =>
            Seq.empty
        }
      }
      case None => Seq.empty
    }


  }
}