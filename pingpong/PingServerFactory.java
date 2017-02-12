package pingpong;
import rmi.*;
public interface PingServerFactory{
  public PingServer makePingServer() throws RMIException;
}
