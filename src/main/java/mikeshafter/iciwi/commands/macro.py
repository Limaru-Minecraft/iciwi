'''
Macro file to speed up coding
'''
if __name__ == "__main__":
    with open("Commands.java", "r") as commands:
        r_string = commands.read()
        print(r_string)
        r_string = r_string.replace("lab(", ".then(LiteralArgumentBuilder.literal")
        r_string = r_string.replace("rab(", ".then(RequiredArgumentBuilder.argument")

        r_string = r_string.replace("bat", "BoolArgumentType.bool()")
        r_string = r_string.replace("dat", "DoubleArgumentType.doubleArg()")
        r_string = r_string.replace("fat", "FloatArgumentType.floatArg()")
        r_string = r_string.replace("iat", "IntegerArgumentType.integer()")
        r_string = r_string.replace("lat", "LongArgumentType.longArg()")
        r_string = r_string.replace("sat", "StringArgumentType.string()")
        r_string = r_string.replace("wat", "StringArgumentType.word()")
    with open("Commands.java", "w") as commands:
        commands.write(r_string)
