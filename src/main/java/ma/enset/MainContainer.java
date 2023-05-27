package ma.enset;

import jade.core.Runtime;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;

public class MainContainer extends Agent {
    public static void main(String[] args) throws ControllerException {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter("gui", "true");
        AgentContainer agentContainer = runtime.createMainContainer(profile);
        agentContainer.start();
    }
}
