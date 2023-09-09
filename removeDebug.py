import glob

def removeDebug(file):
  file_lines = [line for line in file]
  for i in range(len(file_lines)):
    if "todo: debug" in file_lines[i].lower():
      file_lines[i] = "//" + file_lines[i]
  return file_lines

root_dir = '.'
for filename in glob.glob(root_dir + '/**/*.java', recursive=True):
  with open(filename, 'r') as f:
    rf = removeDebug(f)
  with open(filename,'w') as fw:
   fw.write(''.join(rf))

