'''
Checks whether all strings in lang.yml is used
'''
import glob

def checklang(file):
  lang = open('./src/main/resources/lang.yml')
  lang_paths = [line.split(':')[0] for line in lang]
  file_lines = [line for line in file]
  names = []
  for i in file_lines:
    for j in lang_paths:
      if j == '' or j == '\n':
        continue
      elif j in i and j.strip() not in names:
        print(j)
        names.append(j)
  for name in names:
    with open('usedNames.txt', 'a') as usedNames:
      usedNames.write(name+'\n')

root_dir = './src/main/java'
with open('usedNames.txt', 'w') as usedNames:
  usedNames.write('')
for filename in glob.glob(root_dir + '/**/*.java', recursive=True):
  with open(filename, 'r') as f:
    checklang(f)
