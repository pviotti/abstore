package fr.eurecom.abstore

import java.io.File

import ckite.RaftBuilder
import fr.eurecom.abstore.http.HttpServer
import fr.eurecom.abstore.kvs.KVStore
import fr.eurecom.abstore.kvs.raft.RaftKVStore
import fr.eurecom.abstore.kvs.zab.ZabKVStore
import scopt.OptionParser

case class Config(smr: String = "raft", dataDir: String = "/tmp/kvs/n1",
                  address: String = "localhost:9091", bootstrap: Boolean = false, members: String = "")

/**
 * AbStore main class.
 * To launch it from command line using sbt:
 * $ sbt "run -r raft -a localhost:9091 -d /tmp/kvs/n1 -bootstrap"
 */
object AbStoreMain extends App {

  val parser = new OptionParser[Config]("abstore") {
    head("abstore", "0.1.0")
    opt[String]('r', "smr") required() action { (x, c) =>
      c.copy(smr = x) } text("SMR protocol (raft, zab)")
    opt[String]('d', "dataDir") required() action { (x, c) =>
      c.copy(dataDir = x) } text("Data directory")
    opt[String]('a', "address") required() action { (x, c) =>
      c.copy(address = x) } text("Listening address")
    opt[Unit]("bootstrap") action { (_, c) =>
      c.copy(bootstrap = true) } text("Bootstrap option (for the very first node)")
    opt[String]('m', "members") action { (x, c) =>
      c.copy(members = x) } text("Members addresses")
    checkConfig { c =>
      if (!c.members.isEmpty && c.bootstrap) failure("You set both bootstrap and members.") else success }
    help("help") text("A simple experimental datastore.")
  }

  parser.parse(args.toSeq, Config()) map { config =>

    this.removeDirectory(config.dataDir)

    var kvs : KVStore = null

    config.smr match {
      case "zab" =>
        kvs = new ZabKVStore(config.address, config.dataDir, config.members, config.bootstrap)
      case "raft" =>
        kvs = new RaftKVStore()
        val raft = RaftBuilder().listenAddress(config.address)
          .members(config.members.split(",")) // optional seeds to join the cluster
          .bootstrap(config.bootstrap)
          .dataDir(config.dataDir) // dataDir for persistent state (log, terms, snapshots, etc...)
          .stateMachine(kvs.asInstanceOf[RaftKVStore]) // KVStore is an implementation of the StateMachine trait
          .sync(false) // disables log sync to disk
          .build
        kvs.asInstanceOf[RaftKVStore].setRaft(raft)
    }

    kvs.init()
    val http = HttpServer(kvs, config.address)
    http.start

  } getOrElse { // arguments are bad, usage message will have been displayed
    sys.exit(-1)
  }

  def removeDirectory(path: String) = {
    def getRecursively(f: File): Seq[File] =
      f.listFiles.filter(_.isDirectory).flatMap(getRecursively) ++ f.listFiles

    if (new File(path).exists()) {
      getRecursively(new File(path)).foreach { f =>
        if (!f.delete())
          throw new RuntimeException("Failed to delete " + f.getAbsolutePath)
      }
      new File(path).delete()
    }
  }
}
