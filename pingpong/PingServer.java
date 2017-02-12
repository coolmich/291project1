package pingpong;
import rmi.RMIException;
import java.io.Serializable;


public interface PingServer {//extends Serializable{
  public String ping(int idNumber) throws RMIException;
}
