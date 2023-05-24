package mikeshafter.iciwi.commands;

import java.util.ArrayList;

public class Odometer {
  protected ArrayList<Integer> distances;
  protected int lastRecord;
  protected int recorded = 0;
  protected boolean recording;

  public Odometer(ArrayList<Integer> distances, int lastRecord, boolean recording) {
    this.distances = distances;
    this.lastRecord = lastRecord;
    this.recording = recording;
  }
}
