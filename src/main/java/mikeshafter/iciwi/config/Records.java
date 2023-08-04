package mikeshafter.iciwi.config;

public class Records extends CustomConfig {

  public Records(org.bukkit.plugin.Plugin plugin) {
    super("records.yml", plugin);
  }

  public Records() {
    super("records.yml");
  }

  public String getStation (String serial) {
    return super.getString("station."+serial);
  }

  public void setStation (String serial, String station) {
    super.set("station."+serial, station);
    super.save();
  }

  public String getPreviousStation (String serial) {
    return super.getString("previous-station."+serial);
  }

  public void setPreviousStation (String serial, String station) {
    super.set("previous-station."+serial, station);
    super.save();
  }

  public boolean getTransfer (String serial) {
    return super.getBoolean("has-transfer."+serial);
  }

  public void setTransfer (String serial, boolean hasTransfer) {
    super.set("has-transfer."+serial, hasTransfer);
    super.save();
  }

  public long getTimestamp (String serial) {
    return super.getLong("timestamp."+serial);
  }

  public void setTimestamp (String serial, long timestamp) {
    super.set("timestamp."+serial, timestamp);
    super.save();
  }

  public double getCurrentFare (String serial) {
    return super.getDouble("current-fare."+serial);
  }

  public void setCurrentFare (String serial, double fare) {
    super.set("current-fare."+serial, fare);
    super.save();
  }
}