import glob

def checklog(file):
    file_lines = [line for line in file]
    changed = False
    for i,v in enumerate(file_lines):
        if 'lMap = Map' in v and 'lMap = Map.of("player", player.getUniqueId().toString()' not in v:
            print("Logger line "+i+" in file "+file+" does not contain a player reference!")
            print("Fixing...")
            line.replace("lMap = Map", 'lMap = Map.of("player", player.getUniqueId().toString()')
            changed = True
    return file_lines, changed

root_dir = './src/main/java'
for filename in glob.glob(root_dir + '/**/*.java', recursive=True):
    with open(filename, 'r') as f:
        file_lines, changed = checklog(f)
    if changed:
        with open(filename, 'w') as fw:
            fw.write(''.join(file_lines))
