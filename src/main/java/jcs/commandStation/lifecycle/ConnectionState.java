package jcs.commandStation.lifecycle;

/** State of a single command-station connection. */
public enum ConnectionState {
  DISCONNECTED, DISCOVERING, CONNECTING, CONNECTED, RECONNECTING, STOPPING
}
