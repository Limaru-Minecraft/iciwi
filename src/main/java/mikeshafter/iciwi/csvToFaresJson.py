# Parking this here because I know everyone needs it
import json;

with open("fares.csv",'r') as fares:
  i = 0;
  for line in fares:
    if i == 0:
      # Header
      fromList = line.split(',');
      fromList.pop(0);
      # fromList is the list of stations players start from
    else:
      # All others
      fareList = line.split(',');
      # 0th element is always the ending station
      end = fareList[0];
      # TODO: add line to dict: from:{to:fare}

    i+=1;
