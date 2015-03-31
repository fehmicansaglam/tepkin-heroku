package com.example

import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import com.twitter.finagle.http.Response
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future => TwitterFuture, Promise => TwitterPromise}
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.tepkin.{MongoClientUri, MongoClient}
import org.jboss.netty.handler.codec.http._
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.concurrent.{Future => ScalaFuture}
import scala.util.{Failure, Properties, Success}

object Server {
  def main(args: Array[String]): Unit = {
    val port = Properties.envOrElse("PORT", "8080").toInt
    println("Starting on port: " + port)

    val server = Http.serve(":" + port, new Hello)
    Await.ready(server)
    ()
  }
}

class Hello extends Service[HttpRequest, HttpResponse] {
  val uri = Properties.envOrElse("MONGOLAB_URI", "")
  val client = MongoClient(uri)
  val db = client(MongoClientUri(uri).database.get)
  val collection = db("tepkin")

  import client.ec

  implicit val timeout: Timeout = 5.seconds
  implicit val mat = ActorFlowMaterializer()(client.context)

  implicit def toTwitterFuture[A](sf: ScalaFuture[A]): TwitterFuture[A] = {
    val tp = new TwitterPromise[A]

    sf.onComplete {
      case Success(v) => tp.setValue(v)
      case Failure(t) => tp.setException(t)
    }

    tp
  }

  def apply(request: HttpRequest): TwitterFuture[HttpResponse] = {
    if (request.getUri.endsWith("/db")) {
      showDatabase(request)
    } else {
      showHome(request)
    }
  }

  def showHome(request: HttpRequest): TwitterFuture[HttpResponse] = {
    val response = Response()
    response.setStatusCode(200)
    response.setContentString("Hello from Scala!")
    TwitterFuture(response)
  }

  def showDatabase(request: HttpRequest): TwitterFuture[HttpResponse] = {
    for {
      insert <- collection.insert("created" := DateTime.now())
      source <- collection.find(BsonDocument.empty)
      out <- source.runFold("")(_ + _.map(_.pretty()).mkString("\n"))
    } yield {
      val response = Response()
      response.setStatusCode(200)
      response.setContentString(out)
      response.httpResponse
    }
  }
}
