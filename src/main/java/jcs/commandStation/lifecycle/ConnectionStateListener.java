package jcs.commandStation.lifecycle;

@FunctionalInterface
public interface ConnectionStateListener {
  void onConnectionStateChanged(ConnectionStateEvent event);
}
