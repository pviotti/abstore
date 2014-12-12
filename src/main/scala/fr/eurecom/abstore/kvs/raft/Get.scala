package fr.eurecom.abstore.kvs.raft

import ckite.rpc.ReadCommand

case class Get(key: String) extends ReadCommand[String]