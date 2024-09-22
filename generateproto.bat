@echo off

echo Backing up Handler files
if not exist ".\backup" md ".\backup"
for %%F in (
    "recv\Handler.recv.java=recv\Handler.java",
    "recv\HandlerUnionCmdNotify.java=recv\HandlerUnionCmdNotify.java",
    "recv\HandlerGetPlayerTokenReq.java=recv\HandlerGetPlayerTokenReq.java",
    "send\Handler.send.java=send\Handler.java",
    "send\HandlerGetPlayerTokenRsp.java=send\HandlerGetPlayerTokenRsp.java"
) do (
    for /f "tokens=1,2 delims==" %%A in ("%%F") do (
        if exist ".\src\main\java\emu\protoshift\server\packet\%%B" move /Y ".\src\main\java\emu\protoshift\server\packet\%%B" ".\backup\%%A"
    )
)

echo Cleaning existing directories
for %%D in (recv, send) do (
    if exist ".\src\main\java\emu\protoshift\server\packet\%%D" (
        rd /s /q ".\src\main\java\emu\protoshift\server\packet\%%D"
    )
    md ".\src\main\java\emu\protoshift\server\packet\%%D"
)

echo Updating Opcodes
if exist "tools\opcode_shift\opcode_shift.py" (
    cd tools\opcode_shift
    python opcode_shift.py
    cd ..\..\
)

echo Building proto files
if exist "tools\rename_proto_class\rename_proto_class.py" (
    cd tools\rename_proto_class
    python rename_proto_class.py
    cd ..\..\
)

if exist ".\src\generated" rd /s /q ".\src\generated"
md ".\src\generated"
if exist ".\tools\protoc\protoc.exe" (
    .\tools\protoc\protoc.exe -I=".\proto\new" --java_out=".\src\generated" ".\proto\new\*.proto"
    .\tools\protoc\protoc.exe -I=".\proto\old" --java_out=".\src\generated" ".\proto\old\*.proto"
)

echo Translating proto to json and then to java
if exist ".\tools\proto2json\output" rd /s /q ".\tools\proto2json\output"
if exist "tools\proto2json\proto2json.exe" (
    cd tools\proto2json
    proto2json.exe
    cd ..\..\
)

if exist "tools\protojson2java\protojson2java.py" (
    cd tools\protojson2java
    python protojson2java.py
    cd ..\..\
)

echo Recovering Handler files
for %%F in (
    "recv\Handler.java=recv\Handler.recv.java",
    "recv\HandlerUnionCmdNotify.java=recv\HandlerUnionCmdNotify.java",
    "recv\HandlerGetPlayerTokenReq.java=recv\HandlerGetPlayerTokenReq.java",
    "send\Handler.java=send\Handler.send.java",
    "send\HandlerGetPlayerTokenRsp.java=send\HandlerGetPlayerTokenRsp.java"
) do (
    for /f "tokens=1,2 delims==" %%A in ("%%F") do (
        if exist ".\backup\%%B" move /Y ".\backup\%%B" ".\src\main\java\emu\protoshift\server\packet\%%A"
    )
)

if exist ".\backup" rd /s /q ".\backup"

echo.
echo Process completed
pause
