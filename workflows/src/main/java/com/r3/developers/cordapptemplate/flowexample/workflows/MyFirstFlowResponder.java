package com.r3.developers.cordapptemplate.flowexample.workflows;

import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.InitiatedBy;
import net.corda.v5.application.flows.ResponderFlow;
import net.corda.v5.application.membership.MemberLookup;
import net.corda.v5.application.messaging.FlowSession;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.types.MemberX500Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// MyFirstFlowResponder is a responder flow, its corresponding initiating flow is called MyFirstFlow (defined in MyFirstFlow.java)
// to link the two sides of the flow together they need to have the same protocol.
@InitiatedBy(protocol = "my-first-flow")
// Responder flows must inherit from ResponderFlow
public class MyFirstFlowResponder implements ResponderFlow {

    // Log messages from the flows for debugging.
    private final static Logger log = LoggerFactory.getLogger(MyFirstFlowResponder.class);

    // MemberLookup looks for information about members of the virtual network which 
    // this CorDapp operates in. 
    @CordaInject
    public MemberLookup memberLookup;

    public MyFirstFlowResponder() {}

    // Responder flows are invoked when an initiating flow makes a call via a session set up with the virtual
    // node hosting the responder flow. When a responder flow is invoked its call() method is called.
    // call() methods must be marked as @Suspendable, this allows Corda to pause mid-execution to wait
    // for a response from the other flows and services.
    // The call() method has the flow session passed in as a parameter by Corda so the session is available to
    // responder flow code, you don't need to inject the FlowMessaging service.
    @Suspendable
    @Override
    public void call(FlowSession session) {

        // Follow what happens in the console or logs.
        log.info("MFF: MyFirstResponderFlow.call() called");

        // Receive the payload and deserialize it into a message class.
        Message receivedMessage = session.receive(Message.class);

        // Log the message as a proxy for performing some useful operation on it.
        log.info("MFF: Message received from " + receivedMessage.getSender() + ":" + receivedMessage.getMessage());

        // Get our identity from the MemberLookup service.
        MemberX500Name ourIdentity = memberLookup.myInfo().getName();

        // Create a message to greet the sender.
        Message response = new Message(ourIdentity,
                "Hello " + session.getCounterparty().getCommonName() + ", best wishes from " + ourIdentity.getCommonName());

        // Log the response to be sent.
        log.info("MFF: response.message: " + response.getMessage());

        // Send the response via the send method on the flow session
        session.send(response);
    }
}
