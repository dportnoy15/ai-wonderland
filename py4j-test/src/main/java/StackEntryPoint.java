import py4j.GatewayServer;

public class StackEntryPoint {

    private Stack stack;

    public StackEntryPoint() {
      stack = new Stack();
      stack.push("Initial Item");
    }

    /*
    public void registerListener(ExampleListener listener) {
        listeners.add(listener);
    }

    public void notifyAllListeners() {
        for (ExampleListener listener: listeners) {
            Object returnValue = listener.notify(this);
            System.out.println(returnValue);
        }
    }
    */

    public void setValue(int val) {
        System.out.println("Value set to " + val + " from Python!!!");
    }

    public Stack getStack() {
        return stack;
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new StackEntryPoint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

}