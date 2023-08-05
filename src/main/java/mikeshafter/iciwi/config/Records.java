package mikeshafter.iciwi.config;

public class Records extends CustomConfig {

  public Records(org.bukkit.plugin.Plugin plugin) {
    super("records.yml", plugin);
  }

  public Records() {
    super("records.yml");
  }

  public String getStation (String serial) {
    return super.getString(serial + ".station");
  }

  public void setStation (String serial, String station) {
    super.set(serial + ".station", station);
    super.save();
  }

  public String getPreviousStation (String serial) {
    return super.getString(serial + ".previous-station");
  }

  public void setPreviousStation (String serial, String station) {
    super.set(serial + ".previous-station", station);
    super.save();
  }

  public boolean getTransfer (String serial) {
    return super.getBoolean(serial + ".has-transfer");
  }

  public void setTransfer (String serial, boolean hasTransfer) {
    super.set(serial + ".has-transfer", hasTransfer);
    super.save();
  }

  public long getTimestamp (String serial) {
    return super.getLong(serial + ".timestamp");
  }

  public void setTimestamp (String serial, long timestamp) {
    super.set(serial + ".timestamp", timestamp);
    super.save();
  }

  public double getCurrentFare (String serial) {
    return super.getDouble(serial + ".current-fare");
  }

  public void setCurrentFare (String serial, double fare) {
    super.set(serial + ".current-fare", fare);
    super.save();
  }
}
