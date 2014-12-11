package fr.eurecom.kvstore.http

import fr.eurecom.kvstore.smr.KVStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future => ScalaFuture}
import scala.util.Failure
import scala.util.Success
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.{ Request, Response }
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.Method
import com.twitter.finagle.http.Version.Http11
import com.twitter.finagle.http.path._
import com.twitter.finagle.Service
import com.twitter.util.Future
import com.twitter.util.Promise

class HttpService(kvs: KVStore) extends Service[Request, Response] {

  val mapper = new ObjectMapper
  mapper.registerModule(DefaultScalaModule)
  val printer = new DefaultPrettyPrinter
  printer.indentArraysWith(new DefaultPrettyPrinter.Lf2SpacesIndenter)
  val writer = mapper.writer(printer)

  def apply(request: Request) = request.method -> Path(request.path) match {
    case Method.Get -> Root => Future.value {
      val response = Response()
      response.contentString = writer.writeValueAsString("NOT IMPLEMENTED") // FIXME
      response
    }
    case Method.Get -> Root / "kv" / key => {
      //val localOption = request.params.getBoolean("local")  // FIXME
      val result = kvs.get(key)
      val response = Response()
      response.contentString = s"$result\n"
      Future value response
    }
    case Method.Post -> Root / "kv" / key / value => {
      kvs.put(key, value)
      Future value Response()
    }
//      case Method.Post -> Root / "members" / binding => {
//        raft.addMember(binding) map { value => Response() }
//      }
//      case Method.Delete -> Root / "members" / binding => {
//        raft.removeMember(binding) map { value => Response() }
//      }
    case _ =>
      Future value Response(Http11, NotFound)
  }

  private implicit def toTwitterFuture[T](scalaFuture: ScalaFuture[T]): Future[T] = {
    val promise = Promise[T]()
    scalaFuture.onComplete {
      case Success(value) => promise.setValue(value)
      case Failure(t) => promise.raise(t)
    }
    promise
  }
}