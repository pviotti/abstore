package fr.eurecom.kvstore.http

import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.RichHttp
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Http
import java.net.InetSocketAddress
import com.twitter.util.Closable
import fr.eurecom.kvstore.smr.KVStore

class HttpServer(kvs: KVStore, address: String) {
  
  var closed = false
  var server: Closable = _
  
  def start() = {
    val restServerPort = address.split(":")(1).toInt + 1000
     server = ServerBuilder()
      .codec(RichHttp[Request](Http()))
      .bindTo(new InetSocketAddress(restServerPort))
      .name("HttpServer")
      .build(new HttpService(kvs))
  }
  
  def stop() = synchronized {
    if (!closed) {
    	server.close()
    	closed = true
    }
  }
  
}

object HttpServer {
  def apply(kvs: KVStore, address: String) = new HttpServer(kvs, address)
}
