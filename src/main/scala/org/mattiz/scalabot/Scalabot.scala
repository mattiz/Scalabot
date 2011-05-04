package org.mattiz.scalabot

import io.Source
import java.net._
import URLEncoder.encode
import org.jibble.pircbot.PircBot
import java.lang.String
import util.parsing.json.JSON


/**
 * An IRC bot that will interpret scala commands
 * 
 * @author Mathias Bjerke <mathias@gmail.com>
 */

object Scalabot extends PircBot {
	def eval(s: String):String = {
		val url = "http://www.simplyscala.com/interp?jsonp=&code="
		val rawData = Source.fromURL(new URL(url + encode(s, "UTF-8"))).mkString
		val json = JSON.parseFull( rawData.substring(1, rawData.length-2) )
		val map:Map[String,String] = json.get.asInstanceOf[Map[String, String]]

		map.get("result").get.trim
	}

	def formatOutput(s: String):String = {
		if( s.contains("error") )
			s.split("\n")(0)
		else
			s.replaceAll("\n", "\\\\n")
	}

	implicit def restrictLengthTo(s: String) = new {
		def restrictLengthTo(i :Int) = {
			if( s.length > i )
				s.substring( 0, i-3 ) + "..."
			else
				s
		}
	}

	def main(args: Array[String]) {
		setName("Scalabot_")
		setVerbose(true)
		setEncoding("UTF-8")
		connect("irc.homelien.no")
		identify("******")
		joinChannel("#java.no")
	}


	override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) = {
		if(message.startsWith(getName + ": ")) {
			val code = message.substring((getName + ": ").length)
			val result = formatOutput( eval( code ) ).restrictLengthTo(400)

			sendMessage(channel, result.replaceAll("res0: ", ""))
		}
	}
}