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

public class Sender extends Verticle implements Handler<Long> {
	private Sender self = this;
	
	private long period = 1000;
	private long num = 10;
	private boolean send = true;
	private long timeout = 250;
	private boolean publish = true;
	private long total = 10000;
	private long rounds = 1;
	private long delay = 0;

	private long ping = 1;
	private long timeouts = 0;
	private long timerID = -1;

	@Override
	public void start() {
		//
		// Get our parameters
		//
		this.period = container.config().getLong("period", this.period);
		this.num = container.config().getLong("num", this.num);
		this.send = container.config().getBoolean("send", this.send);
		this.timeout = container.config().getLong("timeout", this.timeout);
		this.publish = container.config().getBoolean("publish", this.publish);
		this.total = container.config().getLong("total", this.total);
		this.rounds = container.config().getLong("rounds", this.rounds);
		this.delay = container.config().getLong("delay", this.delay);
	    //
	    // "Published" reply handler
	    //
	    vertx.eventBus().registerHandler("pong-address", new Handler<Message<String>>() {

			@Override
			public void handle(Message<String> reply) {
				System.out.println("message: " + reply.body());
				reply.reply();
			}
	    }, new Handler<AsyncResult<Void>>() {

			@Override
			public void handle(AsyncResult<Void> event) {
				System.out.println("pong-address " + (event.succeeded() ? " successfully registered." : " registration failed " + event.cause().getLocalizedMessage()));
				if (event.failed()) {
					return;
				}
				//
				// Start the timer
				//
			    timerID = vertx.setPeriodic(period, self);
			    System.out.println("Timer started with ID " + timerID);
			}
	    });
	    //
	    // Dump stats every once in awhile
	    //
	    vertx.setPeriodic(2000, new Handler<Long> () {

			@Override
			public void handle(Long event) {
				System.out.println("Total Sender Timeouts: " + timeouts);
			}
	    });
	}

	@Override
	public void handle(Long event) {
    	for (int i = 0; i < num; i++) {
    		System.out.println("pinging " + ping);
    		//
    		// Do they want us to "send" the messages?
    		//
    		if (send) {
    			final String msg = "send ping " + ping;
    			//
    			// Do they want a timeout?
    			//
    			if (timeout > 0) {
    				//
    				// Yes - send with a timeout
    				//
    				vertx.eventBus().sendWithTimeout("ping-address", msg, timeout, new Handler<AsyncResult<Message<String>>>() {

						@Override
						public void handle(AsyncResult<Message<String>> reply) {
    						System.out.println("asynch reply: " + reply.succeeded() + " " + (reply.result() != null ? reply.result().body() : msg));
    						if (reply.failed()) {
    							timeouts++;
    						}
						}

    				});
    			} else {
    				//
    				// No just send
    				//
    				vertx.eventBus().send("ping-address", msg, new Handler<Message<String>> () {
    					@Override
    					public void handle(Message<String> reply) {
    						System.out.println("reply: " + reply.body());
    					}
    				});
    			}
    		}
    		//
    		// Do they want us to "publish" the messages?
    		//
    		if (publish) {
    			vertx.eventBus().publish("ping-address", "publish ping " + ping);
    		}
    		//
    		// Move to the next
    		//
    		ping++;
    		//
    		// Limit hit?
    		//
    		if (ping > total) {
    			System.out.println("Done - cancelling timer.");
    			//
    			// Yes - cancel the timer
    			//
    			vertx.cancelTimer(timerID);
    			timerID = -1;
    			//
    			// Decrement the rounds
    			//
    			rounds--;
    			//
    			// More rounds?
    			//
    			if (rounds > 0) {
    				System.out.println("Waiting for next round");
    				vertx.setTimer(delay, new Handler<Long> () {

						@Override
						public void handle(Long event) {
							System.out.println("Starting next round");
							//
							// Reset
							//
							ping = 1;
							//
							// Start timer
							//
						    timerID = vertx.setPeriodic(period, self);
						    System.out.println("Timer started with ID " + timerID);							
						}
    					
    				});
    			} else {
    				System.out.println("No more rounds.");
    			}
    			//
    			// Break the loop
    			//
    			break;
    		}
    	}
	}

}
