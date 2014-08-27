/*
 *                        AT&T - PROPRIETARY
 *          THIS FILE CONTAINS PROPRIETARY INFORMATION OF
 *        AT&T AND IS NOT TO BE DISCLOSED OR USED EXCEPT IN
 *             ACCORDANCE WITH APPLICABLE AGREEMENTS.
 *
 *          Copyright (c) 2014 AT&T Knowledge Ventures
 *              Unpublished and Not for Publication
 *                     All Rights Reserved
 */
package com.att.research.is.composition.test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

public class Main {
	static JsonObject receiverConfig;
	static JsonObject senderConfig;
	static PlatformManager pm;
	
	public static void deploy() {
		try {
			receiverConfig = new JsonObject(new String(Files.readAllBytes(Paths.get("config.receiver.json"))));
			senderConfig = new JsonObject(new String(Files.readAllBytes(Paths.get("config.sender.json"))));
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
			return;
		}
				
		pm = PlatformLocator.factory.createPlatformManager(0, "localhost");
		
		pm.deployWorkerVerticle(false, 
				Receiver.class.getCanonicalName(),
				receiverConfig, 
				new URL[]{},
				1, 
				null, 
				new Handler<AsyncResult<String>>() {

					@Override
					public void handle(AsyncResult<String> event) {
						pm.deployWorkerVerticle(false, Sender.class.getCanonicalName(), senderConfig, new URL[]{}, 1, null, null);
					}
			
		});
	}

	public static void main(String[] args) {
		deploy();
	}

}
