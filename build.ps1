$ErrorActionPreference = "Stop"
Remove-Item -Recurse -Force build, dist -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force build/classes, build/test-classes, dist | Out-Null
Get-ChildItem -Recurse src/main/java -Filter *.java | Sort-Object FullName | ForEach-Object { $_.FullName } | Set-Content build/main-sources.txt
javac --release 17 -d build/classes '@build/main-sources.txt'
Get-ChildItem -Recurse src/test/java -Filter *.java | Sort-Object FullName | ForEach-Object { $_.FullName } | Set-Content build/test-sources.txt
javac --release 17 -cp build/classes -d build/test-classes '@build/test-sources.txt'
java -cp "build/classes;build/test-classes" io.github.shashank022.linkagelens.LinkageLensTest
jar --create --file dist/linkagelens.jar --main-class io.github.shashank022.linkagelens.Main -C build/classes .
Write-Host "Built dist/linkagelens.jar"
