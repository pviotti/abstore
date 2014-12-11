package fr.eurecom.kvstore.smr.zab

/**
 * Created by paolo on 11/12/14.
 */
abstract class Command extends Serializable {
  private val serialVersionUID: Long = 0L
  def execute(db: ZabKVStore)
}
case class PutCommand(key: String, value: String) extends Command {
 override def execute(db: ZabKVStore) {
    db.putKeyValue(key, value)
  }
}


