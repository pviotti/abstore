package fr.eurecom.abstore.kvs.zab

import java.io._
import java.nio.ByteBuffer
import java.util
import java.util.concurrent.ConcurrentHashMap

import com.github.zk1931.jzab._

import ckite.util.Logging  //FIXME

import fr.eurecom.abstore.kvs.KVStore


case class ZabKVStore(address: String, dataDir: String, members: String, bootstrap: Boolean)
  extends StateMachine with Logging with KVStore {

  val map = new ConcurrentHashMap[String, Array[Byte]]()

  var serverId: String = address
  var zab: Zab = _

  override def init(): Unit = {
    val config = new ZabConfig
    config.setLogDir(dataDir)
    val membs = if (bootstrap) address else members
    zab = new Zab(this, config, this.serverId, membs)
    serverId = zab.getServerId()
  }

  override def get(key: String): String = {
    new String(map.get(key))
  }

  override def put(key: String, value: String): Unit = {

    val bos: ByteArrayOutputStream = new ByteArrayOutputStream
    val oos: ObjectOutputStream = new ObjectOutputStream(bos)
    try {
      oos.writeObject(new PutCommand(key, value))
      oos.close
      val bb: ByteBuffer = ByteBuffer.wrap(bos.toByteArray)

      zab.send(bb, new Object) /// FIXME AyncContext
    } catch {
      case ex: ZabException => {
        LOG.error("Error: {}", ex.getMessage)
      }
    } finally {
      if (bos != null) bos.close()
      if (oos != null) oos.close()
    }
  }

  def putKeyValue(key: String, value: String) {
    map.put(key, value.getBytes())
  }

  def putAll(updates: ConcurrentHashMap[String, Array[Byte]]) {
    map.putAll(updates)
  }

  def preprocess(zxid: Zxid, message: ByteBuffer): ByteBuffer = {
    LOG.debug("Preprocessing a message: {}", message)
    message
  }

  def following(leader: String, clusterMembers: util.Set[String]): Unit = {
    LOG.info("FOLLOWING {}", leader)
    LOG.info("Cluster configuration change : ", clusterMembers.size)
    for (peer <- List(clusterMembers)) {
      LOG.info(" -- {}", peer)
    }
  }

  def flushed(byteBuffer: ByteBuffer, o: scala.Any): Unit = {}

  def recovering(pendingRequests: PendingRequests): Unit = {
    LOG.info("Recovering... [NOT IMPLEMENTED]")
  }

  def removed(s: String, o: scala.Any): Unit = {}

  def save(outputStream: OutputStream): Unit = {}

  def deliver(zxid: Zxid, stateUpdate: ByteBuffer, s: String, o: scala.Any): Unit = {
    LOG.info("Deliver...")

    val bytes: Array[Byte] = new Array[Byte](stateUpdate.remaining)
    stateUpdate.get(bytes)
    try {
      val bis: ByteArrayInputStream = new ByteArrayInputStream(bytes)
      val ois: ObjectInputStream = new ObjectInputStream(bis)
      try {
        val command: Command = ois.readObject.asInstanceOf[Command]
        command.execute(this)
      }
      catch {
        case ex: Any => {
          //LOG.error("Failed to deserialize: {}", stateUpdate, ex)
          throw new RuntimeException("Failed to deserialize ByteBuffer")
        }
      } finally {
        if (bis != null) bis.close()
        if (ois != null) ois.close()
      }
    }
  }

  def leading(activeFollowers: util.Set[String], clusterMembers: util.Set[String]): Unit = {
    LOG.info("LEADING with active followers : ")
    for (peer <- List(activeFollowers)) {
      LOG.info(" -- {}", peer)
    }
    LOG.info("Cluster configuration change : ", clusterMembers.size)
    for (peer <- List(clusterMembers)) {
      LOG.info(" -- {}", peer)
    }
  }

  def restore(inputStream: InputStream): Unit = {}

  def snapshotDone(s: String, o: scala.Any): Unit = {}
}
