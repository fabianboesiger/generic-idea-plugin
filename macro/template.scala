package viper.silicon.annotation

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("Could not expand macro.")
class flyweight(genCopy: Boolean = true) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro flyweightMacro.impl
}

object flyweightMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*) = {
    import c.universe._

    // Your macro implementation goes here.
    val inputs = annottees.map(_.tree).toList
    val output = q""

    saveConfig(c)(output)

    output
  }

  // Saves the relevant data to an XML file.
  private def saveConfig(c: whitebox.Context)(output: Any): Unit = {
    import c.universe._
    import scala.xml.XML
    import java.nio.file.{Paths, Files}

    val q"""
      class $className $_ (..$_) extends ..$_ {
        ..$classDefns
      }

      object $companionName extends ..$_ {
        ..$companionDefns
      }
    """ = output

    // Extracts all method signatures of methods defined in class.
    val members = classDefns.map(member => {
      try {
        val q"$_ def $methodName(...$methodFields): $methodReturnType = $methodBody" = member
        val rewrittenMember = q"def $methodName(...$methodFields): $methodReturnType = ???"
        //println(s"Generating member: $rewrittenMember")
        Some(<member>{rewrittenMember.toString}</member>)
      } catch {
        case _: MatchError => None
      }
    })
      .filter(_.nonEmpty)
      .map(_.get)

    // Extracts all function signatures of functions defined in companion object.
    val functions = companionDefns.map(function => {
      try {
        val q"$_ def $methodName(...$methodFields): $methodReturnType = $methodBody" = function
        val rewrittenFunction = q"def $methodName(...$methodFields): $methodReturnType = ???"
        //println(s"Generating function: $rewrittenFunction")
        Some(<function>{rewrittenFunction.toString}</function>)
      } catch {
        case _: MatchError => None
      }
    })
      .filter(_.nonEmpty)
      .map(_.get)

    // Extracts the macro name.
    val macroName = c.prefix.tree match {
      case q"new $name" => name.toString
      case q"new $name(..$_)" => name.toString
      case _ => assert(false, "Macro name not found")
    }

    // Generate and save XML file.
    val xml =
      <info>
        <macroAnnotationName>{macroName}</macroAnnotationName>
        <members>{members}</members>
        <functions>{functions}</functions>
      </info>

    if (!Files.exists(Paths.get(".plugin"))) {
      Files.createDirectory(Paths.get(".plugin"))
    }
    XML.save(s".plugin/${className.toString}.xml", xml)
  }


}