import csv

NEWCMDID_PATH='..\\..\\proto\\newcmdid.csv'
OLDCMDID_PATH='..\\..\\proto\\oldcmdid.csv'


newcmdid = {}
oldcmdid = {}


def generateJava(cmdid):
    s = ''
    for i in cmdid:
        s += ' public static final int '+i+' = ' + cmdid[i]+';\r'
    return s


def generatePython(cmdid):
    s = ''
    for i in cmdid:
        s += '"'+i+'",'
    return s[:-1]


reader = csv.reader(open(NEWCMDID_PATH))
for line in reader:
    newcmdid[line[0]] = line[1]

reader = csv.reader(open(OLDCMDID_PATH))
for line in reader:
    oldcmdid[line[0]] = line[1]

def generateJava(cmd_ids):
    return '\n'.join(f'        public static final int {name} = {value};' for name, value in cmd_ids.items())

with open('..\\..\\src\\main\\java\\emu\\protoshift\\net\\packet\\PacketOpcodes.java', 'w') as file:
    file.write('''package emu.protoshift.net.packet;

public class PacketOpcodes {
    public int value;
    public int type;

    public PacketOpcodes(int value, int type) {
        this.value = value;
        this.type = type;
    }

    public static final int NONE = 0;

    public static class newOpcodes {
''' + generateJava(newcmdid) + '''
    }

    public static class oldOpcodes {
''' + generateJava(oldcmdid) + '''
    }
}
''')

with open('..\\protojson2java\\cmdIdList.py', 'w') as file:
    file.write('newcmdList=['+generatePython(newcmdid)+']'
               + '\r\r' +
               'oldcmdList=['+generatePython(oldcmdid)+']'
               )
