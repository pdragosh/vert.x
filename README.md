vert.x
======

The ISC-Load Eclipse project is being used to test vert.x point-to-point messaging dialogues. I am experiencing timeouts using the sendWithTimeout() function. I understand that messages are transient and can get lost on the network, but I suspect that messages are actually getting lost within vert.x.

There are times where the system runs smoothly, with no timeouts occuring. But more often I can somewhat consistently (not 100%) demonstrate timeouts occurring. Using just send() and/or publish(), I never see any messages get lost, i.e. the sender sends 500,000 messages and the receiver receives 500,000 (or 1,000,000 if both send and publish are configured to run).

I first ran into this upon system startup where messages were getting lost. I would batch 50 messages, and the first 20 would timeout. I would batch 100 or more messages and the first 38 would timeout.

Upon further testing, we would see thousand's of messages being timed out when a new Sender() was brought into the cluster.

We just don't believe that actual packets are being lost on the network, or perhaps this is my lack of familiarity with vert.x that is at issue.

The ISC-Load has a Sender and a Receiver very similar to the vert.x eventbus_pointtopoint examples. But I added in  send()/sendWithTimeout() and publish() calls to simulate our environment.

There is a JSON configuration file that can be used to tweak what the Sender/Receiver do upon receiving a message.

For the SENDER:
{
	"period" : 50,
	"num" : 250,
	"send" : true,
	"timeout" : 20000,
	"publish" : true,
	"total" : 10000,
	"rounds" : 10000,
	"delay" : 5000
}

period - the periodic timer interval set. When the timer's handler is called, that is when a batch of messages is sent.
num - the number of messages to send every timer period.
send - If true, the sender will initially use send() or sendWithTimeout() to send the message to the receiver.
timeout - If >0 and send=true, use sendWithTimeout(). If >0 and send=true, use send(). Ignored if send=false
publish - If true, the sender will initially use publish() to send the message to the receiver.
total - the total number of messages to send overall during a round.
rounds - number of rounds to send "total" messages using period/num/send/timeout/publish.
delay - the delay between rounds.

For the RECEIVER:
{
	"send" : true,
	"timeout" : 20000,
	"publish" : true
}
send - If true, the receiver will use send() or sendWithTimeout() to send a message to the sender.
timeout - If >0 and send=true, use sendWithTimeout(). If >0 and send=true, use send(). Ignored if send=false
publish - If true, the receiver will use publish() to send a message to the sender.

The receiver ALWAYS does the reply. i.e message.reply(). But then using the configuration, it initiates another send()/sendWithTimeout() or publish() back to the sender. This simulates that our worker verticle gets a message, replies, does some processing and then sends another message back to the caller with the result.

I see the majority of the timeouts in the Receiver's sendWithTimeout(). But I do see timeouts in the Sender's use of sendWithTimeout().




