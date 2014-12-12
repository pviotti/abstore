package fr.eurecom.abstore.kvs.raft

import ckite.rpc.WriteCommand

case class Put(key: String, value: String) extends WriteCommand[String]