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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

public class Receiver extends Verticle {

	private boolean send = true;
	private long timeout = 250;
	private boolean publish = true;
	
	private long timeouts = 0;
	private long total = 0;

	@Override
	public void start() {
		//
		// Get our parameters
		//
		this.send = container.config().getBoolean("send", this.send);
		this.timeout = container.config().getLong("timeout", this.timeout);
		this.publish = container.config().getBoolean("publish", this.publish);
		//
		// Register our handler
		//
	    vertx.eventBus().registerHandler("ping-address", new Handler<Message<String>>() {

			@Override
			public void handle(final Message<String> reply) {
				//
				// Dump that we got it on our end
				//
				System.out.println("message: " + reply.body());
				//
				// Let caller know immediately that we got the message
				//
				reply.reply("ok " + reply.body());
				//
				// Increment that we received
				//
				total++;
				//
				// Check if we are configured to send result as a "send"
				//
				if (send) {
					//
					// Is there a timeout?
					//
					if (timeout > 0) {
						//
						// Yes, reply send with a timeout
						//
						vertx.eventBus().sendWithTimeout("pong-address", "send result " + reply.body(), timeout, new Handler<AsyncResult<Message<String>>>() {
							@Override
							public void handle(AsyncResult<Message<String>> event) {
								System.out.println("send result " + reply.body() + " " + event.succeeded());
								if (event.failed()) {
									timeouts++;
								}
							}
						});
					} else {
						//
						// No, reply send
						//
						vertx.eventBus().send("pong-address", "send result " + reply.body());
					}
				}
				//
				// Check if we are configured to send result as a "publish"
				//
				if (publish) {
					vertx.eventBus().publish("pong-address", "publish result " + reply.body());
				}
			}
	    }, new Handler<AsyncResult<Void>>() {

			@Override
			public void handle(AsyncResult<Void> event) {
				System.out.println("ping-address " + (event.succeeded() ? " successfully registered." : " registration failed " + event.cause().getLocalizedMessage()));
			}
	    });
	    //
	    // Dump stats every once in awhile
	    //
	    vertx.setPeriodic(2000, new Handler<Long> () {

			@Override
			public void handle(Long event) {
				System.out.println("Total Receiver Timeouts: " + timeouts + " Messages Received: " + total);
			}
	    });
	}

}
