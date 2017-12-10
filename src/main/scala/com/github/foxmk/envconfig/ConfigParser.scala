package com.github.foxmk.envconfig

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._
import scala.util.Try

sealed trait EnvConfigAnnotation                             extends StaticAnnotation
case class env(name: String, default: Option[String] = None) extends EnvConfigAnnotation
case class envprefix(name: String)                           extends EnvConfigAnnotation

trait FromString[T] {
  def fromString(s: String): Try[T]
}

object ConfigParser {

  sealed abstract class ParseError(message: String) extends RuntimeException(message)
  case class EnvVariableNotFound(name: String)      extends ParseError(s"Variable $name not found")

  case class UnmarkedFields(className: String, unmarkedFields: Seq[String])
      extends ParseError(s"$className contains unmarked fields: ${unmarkedFields.mkString(",")}")

  def parse[T: TypeTag](prefix: String): Try[T] = parse[T](prefix, env = sys.env)

  def parse[T: TypeTag](env: Map[String, String] = sys.env): Try[T] = parse[T](prefix = "", env)

  def parse[T: TypeTag](prefix: String, env: Map[String, String]): Try[T] = {
    val description = getConfigDescription[T]
    getFromEnv(prefix, description, env).flatMap { args =>
      println(args)
      instantiate[T](args: _*)
    }
  }

  private case class AnnotatedField(name: String, tpe: Type, annotation: Option[env])
  private case class ConfigDescription(name: String, prefix: Option[String], fields: Seq[AnnotatedField])

  private def getConfigDescription[T: TypeTag]: ConfigDescription = {
    val cls = typeOf[T].typeSymbol.asClass
    ConfigDescription(name = cls.name.toString,
                      prefix = getPrefix(cls),
                      fields = getClassFields(cls).map(annotateField))
  }

  private def getFromEnv(prefix: String, classDescription: ConfigDescription, env: Map[String, String]): Try[Seq[Any]] =
    Try {
      classDescription.fields.map { f =>
        f.annotation match {
          case None => ConfigParser.parse(classDescription.prefix.getOrElse(""), env)
          case Some(annotation) =>
            val envName = prefix + classDescription.prefix.getOrElse("") + annotation.name
            env.get(envName).orElse(annotation.default) match {
              case Some(value) =>
                f.tpe match {
                  case t if t =:= typeOf[Int]     => 1
                  case t if t =:= typeOf[Boolean] => value == "true" || value == "yes"
                  case t if t =:= typeOf[String]  => value
                  case _ =>
                    ConfigParser.parse(classDescription.prefix.getOrElse(""), env)
                }
              case None =>
                println(f.annotation)
                f.annotation match {
                  case Some(ann) if ann.default.isDefined => ann.default.get
                  case _                                  => throw EnvVariableNotFound(envName)
                }

            }
        }
      }
    }

  private def instantiate[T: TypeTag](args: Any*): Try[T] = Try {
    val mirror = runtimeMirror(getClass.getClassLoader)
    val ctr = mirror
      .reflectClass(typeOf[T].typeSymbol.asClass)
      .reflectConstructor(typeOf[T].decl(termNames.CONSTRUCTOR).asMethod)
    ctr(args: _*).asInstanceOf[T]
  }

  private def getPrefix(symbol: Symbol): Option[String] = getAnnotation(symbol, typeOf[envprefix]).flatMap { ann =>
    ann.tree.children.collectFirst {
      case Literal(Constant(value: String)) => value
    }
  }

  private def getEnvAnnotation(symbol: Symbol): Option[env] = getAnnotation(symbol, typeOf[env]).map { ann =>
    val children = ann.tree.children
    println(children)

    val name = children.collectFirst {
      case Literal(Constant(value: String)) => value
    }

    val default = children.collectFirst {
      case Apply(_, Literal(Constant(arg: String)) :: _) => Some(arg)
      case Select(_, TermName("None"))                   => None
    }

    env(name = name.getOrElse(throw new RuntimeException), default = default.flatten)
  }

  private def getClassFields(cls: ClassSymbol): Seq[Symbol] = {
    val ctr = cls.primaryConstructor.asMethod
    ctr.typeSignature.paramLists.head
  }

  private def annotateField(field: Symbol): AnnotatedField = {
    val fieldName  = field.name.toString
    val cls        = field.typeSignature
    val annotation = getEnvAnnotation(field)
    AnnotatedField(fieldName, cls, annotation)
  }

  private def getAnnotation(symbol: Symbol, annotationType: Type): Option[Annotation] = {
    val annotations = symbol.annotations
    annotations.find(_.tree.tpe =:= annotationType)
  }
}
